package com.untyped.sbtassets

import sbt._
import scala.util.matching.Regex

trait SimpleDependencyReader extends DependencyReader {
  def filenameRegex: Regex

  def requireRegex: Regex

  def isDefinedAt(file: File) =
    (filenameRegex findFirstIn file.name).isDefined

  def apply(file: File) =
    IO.readLines(file).map(_.trim).toList.flatMap(parseRequire _)

  def parseRequire(line: String): Option[String] =
    requireRegex.findAllIn(line).matchData.map(data => data.group(1)).toList.headOption
}

object CoffeeReader extends SimpleDependencyReader {
  val filenameRegex = "[.]coffee$".r
  val requireRegex = """ ^#[ \t]*require[ \t]*"([^"]+)"[ \t]*$ """.trim.r
}

object JsReader extends SimpleDependencyReader {
  val filenameRegex = "[.]js$".r
  val requireRegex = """ ^[ \t]*//[ \t]*require[ \t]*"([^"]+)"[ \t]*$ """.trim.r
}
