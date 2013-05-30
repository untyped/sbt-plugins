package com.untyped.sbtassets

import sbt._
import scala.util.matching.Regex

case class Path(val abs: Boolean, val parts: List[String]) {
  lazy val name = parts.last

  def parent: Path =
    if(parts.isEmpty) {
      Path(abs, parts :+ "..")
    } else {
      Path(abs, parts.dropRight(1))
    }

  def pathFinder(root: File): PathFinder = {
    val name = this.name
    val finder = parts.dropRight(1).foldLeft(PathFinder(root))(_ / _)
    this.name match {
      case "**" => finder ***
      case "*"  => finder * "*"
      case _    => finder * (name || name + ".*")
    }
  }

  def regex: Regex = {
    val temp =
      parts.foldLeft("") { (accum, part) =>
        accum + (part match {
          case "*"  => "[/]+[^/]*"
          case "**" => "[/]+.*"
          case str  => "[/]+" + java.util.regex.Pattern.quote(str)
        })
      }

    ("^" + temp + "$").r
  }

  // Expand a path into a list of paths matching actual files on the filesystem:
  def expand(root: File): List[Path] =
    pathFinder(root).get.map { file =>
      if(file == root) {
        Path("/")
      } else {
        Path("/" + file.getParent + "/" + file.base)
      }
    }.toList

  def find(root: File): Option[File] =
    pathFinder(root).get.headOption

  def isRoot =
    abs && parts.isEmpty

  def normalize = {
    val norm = Path.normalize(parts)
    if(abs && norm.headOption == Some("..")) {
      sys.error("Possible attempt to escape the filesystem: " + this)
    } else {
      new Path(abs, norm)
    }
  }

  def makeAbsolute =
    if(abs) this else Path(true, parts)

  // def stripExtension =
  //   Path(abs, parts.dropRight(1) :+ base)

  // def withExtension(ext: String) =
  //   Path(abs, parts.dropRight(1) :+ (base + ext))

  def /(in: String): Path =
    this / Path(in)

  def /(in: Path): Path =
    Path(this.abs, this.parts ++ in.parts).normalize

  def toFile =
    file(toString)

  override lazy val toString =
    (if(abs) "/" else "") + (parts mkString "/")
}

object Path {
  val Root = Path(true, Nil)

  def apply(in: String): Path =
    Path(in startsWith "/", (in split "/").toList).normalize

  def relativize(path: Path, dir: Path): Path =
    if(path.abs) {
      path
    } else {
      dir / path
    }

  def normalize(in: List[String]) = {
    val memo0 = (0, List[String]())

    val (ups, parts) =
      in.map(_.trim).filterNot(_ == "").foldRight(memo0) {
        (part, memo) =>
          memo match {
            case (toRemove, closed) =>
              if(part == ".") {
                (toRemove, closed)
              } else if(part == "..") {
                (toRemove + 1, closed)
              } else if(toRemove > 0) {
                (toRemove - 1, closed)
              } else {
                (toRemove, part :: closed)
              }
          }
      }

    List.fill(ups)("..") ++ parts
  }

  implicit object order extends Ordering[Path] {
    def compare(x: Path, y: Path): Int =
      x.toString compare y.toString
  }
}