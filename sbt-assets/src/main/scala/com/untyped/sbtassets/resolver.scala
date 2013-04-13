package com.untyped.sbtassets

import sbt._

object Resolvers extends Resolvers

trait Resolvers {

  /** Resolver that finds NOTHING. */
  val empty: Resolver =
    (path: String, myPath: String) => None

  /** Resolver that finds files relative to a root directory. */
  def root(root: File): Resolver =
    (path: String, myPath: String) =>
      for {
        path <- relativize(path, myPath)
        file <- Option(root / path).filter(_.isFile)
      } yield file

  /** Resolver that finds files using any of a set of other resolvers. */
  def or(args: Resolver *) =
    args.foldLeft(empty) { (a, b) =>
      (path: String, myPath: String) => a(path, myPath) orElse b(path, myPath)
    }

  /** Resolver that finds files with specified extensions. */
  def extensions(exts: String *)(inner: Resolver) =
    or(exts map { ext => (path: String, myPath: String) => inner(path + ext, myPath) } : _*)

  // Helpers ------------------------------------

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

}