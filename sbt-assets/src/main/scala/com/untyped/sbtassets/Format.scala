package com.untyped.sbtassets

import sbt._
import scala.util.matching.Regex

trait Format {
  def matches(in: File): Boolean

  def dependencies(dir: Path, in: File): List[Path]
}

trait SimpleFormat extends Format {
  def filenameRegex: Regex

  def requireRegex: Regex

  def matches(file: File) =
    (filenameRegex findFirstIn file.name).isDefined

  def dependencies(dir: Path, file: File) =
    IO.readLines(file).map(_.trim).toList.flatMap(parseRequire _).map(p => Path.relativize(p.stripExtension, dir))

  def parseRequire(line: String): Option[Path] =
    requireRegex.findAllIn(line).matchData.map(data => data.group(1)).toList.headOption.map(Path.apply _)
}
