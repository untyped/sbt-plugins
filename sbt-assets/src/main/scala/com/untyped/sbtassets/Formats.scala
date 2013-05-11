package com.untyped.sbtassets

import sbt._
import scala.util.matching.Regex

object Formats extends Formats

trait Formats {
  object Js extends SimpleFormat {
    val filenameRegex = "[.]js$".r
    val requireRegex = """ ^[ \t]*//[ \t]*require[ \t]*"([^"]+)"[ \t]*$ """.trim.r
  }

  object Coffee extends SimpleFormat {
    val filenameRegex = "[.]coffee$".r
    val requireRegex = """ ^#[ \t]*require[ \t]*"([^"]+)"[ \t]*$ """.trim.r
  }

  object Css extends SimpleFormat {
    val filenameRegex = "[.]css$".r
    val requireRegex = """ ^[ \t]*@import[ \t]*"([^"]+)"[ \t]*$ """.trim.r
  }

  object Less extends SimpleFormat {
    val filenameRegex = "[.]less$".r
    val requireRegex = """ ^[ \t]*@import[ \t]*"([^"]+)"[ \t]*$ """.trim.r
  }

  case class Or(val formats: List[Format]) extends Format {
    def matches(in: File) = formats.find(_ matches in).isDefined
    def dependencies(dir: Path, in: File) = formats.find(_ matches in).map(_.dependencies(dir, in)).getOrElse(Nil)
  }

  object Any extends Or(List(Js, Coffee, Css, Less))
}
