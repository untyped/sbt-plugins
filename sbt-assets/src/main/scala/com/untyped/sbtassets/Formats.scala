package com.untyped.sbtassets

import sbt._
import scala.util.matching.Regex

object Formats extends Formats

trait Formats {
  object Js extends SimpleFormat {
    val filenameRegex = "[.]js$".r
    // Support any of the following formats with double or single quotes:
    //     // require "foo"
    //     foo = require("bar")
    val requireRegex = """ require[ \t]*[(]?[ \t]*['"]([^'"]+?)([.]js|[.]coffee|)['"][ \t]*[)]? """.trim.r
    override def createPath(in: String): Path =
      if(in startsWith "./") { // relative CommonJS-style
        Path(in substring 2)
      } else if(in startsWith "/") { // absolute filesystem-style
        Path(in)
      } else { // absolute CommonJS-style
        Path("/" + in)
      }
  }

  object Coffee extends SimpleFormat {
    val filenameRegex = "[.]coffee$".r
    // Support any of the following formats with double or single quotes:
    //     // require "foo"
    //     foo = require("bar")
    //     foo = require "bar"
    val requireRegex = """ require[ \t]*[(]?[ \t]*['"]([^'"]+?)([.]js|[.]coffee|)['"][ \t]*[)]? """.trim.r
    override def createPath(in: String): Path =
      if(in startsWith "./") { // relative CommonJS-style
        Path(in substring 2)
      } else if(in startsWith "/") { // absolute filesystem-style
        Path(in)
      } else { // absolute CommonJS-style
        Path("/" + in)
      }
  }

  object Css extends SimpleFormat {
    val filenameRegex = "[.]css$".r
    val requireRegex = """ ^[ \t]*@import[ \t]*"([^"]+?)([.]css|[.]less|)" """.trim.r
  }

  object Less extends SimpleFormat {
    val filenameRegex = "[.]less$".r
    val requireRegex = """ ^[ \t]*@import[ \t]*"([^"]+?)([.]css|[.]less|)" """.trim.r
  }

  case class Or(val formats: List[Format]) extends Format {
    def matches(name: String) = formats.find(_ matches name).isDefined
    def stripExtension(name: String) = formats.flatMap(_ stripExtension name).headOption
    def dependencies(dir: Path, in: File) = formats.find(_ matches in.name).map(_.dependencies(dir, in)).getOrElse(Nil)
  }

  object Any extends Or(List(Js, Coffee, Css, Less))
}
