package com.untyped.sbtassets

import org.scalatest._
import org.scalatest.matchers._
import sbt._

class FormatsSpec extends BaseSpec {
  describe("Coffee") {
    it("should produce paths") {
      IO.withTemporaryFile("test", ".coffee") { file =>
        IO.write(
          file,
          """
          |# require "foo/bar.js"
          |  # require "../baz.coffee"
          |// require "error"
          |alert '# require "error"'
          """.trim.stripMargin
        )
        Formats.Coffee.dependencies(Path("/a/b/c"), file)
      } must equal (List(Path("/a/b/c/foo/bar"), Path("/a/b/baz")))
    }
  }

  describe("Js") {
    it("should produce paths") {
      IO.withTemporaryFile("test", ".js") { file =>
        IO.write(
          file,
          """
          |// require "foo/bar.js"
          |  // require "../baz.coffee"
          |# require "error"
          |alert '// require "error"'
          """.trim.stripMargin
        )
        Formats.Js.dependencies(Path("/a/b/c"), file)
      } must equal (List(Path("/a/b/c/foo/bar"), Path("/a/b/baz")))
    }
  }
}
