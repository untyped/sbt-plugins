package com.untyped.sbtassets

import sbt._
import scala.util.matching.Regex

object Formats extends Formats

trait Formats {
  object Js extends SimpleFormat {
    val filenameRegex = "[.]js$".r
    val requireRegex = """ //[ \t]*require[ \t]*[(]?[ \t]*['"]([^'"]+?)([.]js|[.]coffee|)['"][ \t]*[)]? """.trim.r
  }

  object Coffee extends SimpleFormat {
    val filenameRegex = "[.]coffee$".r
    val requireRegex = """ #[ \t]*require[ \t]*[(]?[ \t]*['"]([^'"]+?)([.]js|[.]coffee|)['"][ \t]*[)]? """.trim.r
  }

  object Css extends SimpleFormat {
    val filenameRegex = "[.]css$".r
    val requireRegex = """ ^[ \t]*@import[ \t]*"([^"]+?)([.]css|[.]less|)" """.trim.r
  }

  object Less extends SimpleFormat {
    val filenameRegex = "[.]less$".r
    val requireRegex = """ ^[ \t]*@import[ \t]*"([^"]+?)([.]css|[.]less|)" """.trim.r
  }

  object Handlebars extends Format {
    val filenameRegex = "[.]handlebars$".r

    def matches(name: String): Boolean =
      (filenameRegex findFirstIn name).isDefined

    def stripExtension(name: String): Option[String] =
      if(this matches name) {
        Some(filenameRegex.replaceAllIn(name, ""))
      } else None

    def dependencies(dir: Path, in: File): List[Path] =
      Nil
  }

  case class Or(val formats: List[Format]) extends Format {
    def matches(name: String) = formats.find(_ matches name).isDefined
    def stripExtension(name: String) = formats.flatMap(_ stripExtension name).headOption
    def dependencies(dir: Path, in: File) = formats.find(_ matches in.name).map(_.dependencies(dir, in)).getOrElse(Nil)
  }

  object Any extends Or(List(Js, Coffee, Css, Less, Handlebars))
}
