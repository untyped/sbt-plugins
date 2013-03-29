package com.untyped.sbtassets

import java.io.FileNotFoundException
import sbt._

object Resolvers extends Resolvers

trait Resolvers {

  def relativize(path: String, myPath: String): Option[String] =
    if(path startsWith "/") {
      Some(path.split("/").toList.filterNot(_ == "").mkString("/"))
    } else {
      val parts = path.split("/").toList.filterNot(_ == "")
      val myParts = myPath.split("/").toList.filterNot(_ == "")

      val countUp = parts.filter(_ == "..").length

      if(countUp > myParts.length) {
        None
      } else {
        Some((myParts ::: parts).mkString("/"))
      }
    }

  val empty: Resolver =
    (path: String, myPath: String) => None

  def root(root: File): Resolver =
    (path: String, myPath: String) =>
      for {
        path <- relativize(path, myPath)
        file <- Option(root / path).filter(_.isFile)
      } yield file

  def or(args: Resolver *) =
    args.foldLeft(empty) { (a, b) =>
      (path: String, myPath: String) => a(path, myPath) orElse b(path, myPath)
    }

  def extensions(exts: String *)(inner: Resolver) =
    or(exts map { ext => (path: String, myPath: String) => inner(path + ext, myPath) } : _*)

}