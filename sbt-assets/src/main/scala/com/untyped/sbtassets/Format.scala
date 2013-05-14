package com.untyped.sbtassets

import sbt._
import scala.util.matching.Regex

trait Format {
  def matches(name: String): Boolean

  def stripExtension(name: String): Option[String]

  def dependencies(dir: Path, in: File): List[Path]
}

trait SimpleFormat extends Format {
  def filenameRegex: Regex

  def requireRegex: Regex

  def matches(name: String) =
    (filenameRegex findFirstIn name).isDefined

  def stripExtension(name: String): Option[String] =
    if(this matches name) {
      Some(filenameRegex.replaceAllIn(name, ""))
    } else None

  def dependencies(dir: Path, file: File) =
    IO.readLines(file).map(_.trim).toList.flatMap(parseRequire _).map(p => Path.relativize(p, dir))

  def parseRequire(line: String): Option[Path] =
    requireRegex.
    findAllIn(line).
    matchData.
    map(data => data.group(1)).
    toList.headOption.
    map(createPath _)

  def createPath(in: String): Path =
    Path(in)
}
