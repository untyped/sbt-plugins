package com.untyped.sbtassets

import org.scalatest._
import org.scalatest.matchers._
import sbt._
import scala.util.matching.Regex

class PathSpec extends BaseSpec {
  describe("Path.normalize") {
    it("should pass through Root") {
      Path.Root.normalize must equal (Path.Root)
    }

    it("should trim whitespace") {
      Path(false, List("a", " ", "b")).normalize must equal (Path(false, List("a", "b")))
    }

    it("should normalize '..'") {
      Path(true, List("a", "..", "b", "c", "d", "..", "..", "e")).normalize must equal (Path(true, List("b", "e")))
      Path(false, List("..", "b", "c")).normalize must equal (Path(false, List("..", "b", "c")))
    }
  }

  describe("Path.relativize") {
    it("should work with relative paths") {
      Path.relativize(Path("a"), Path("/")) must equal (Path("/a"))
      Path.relativize(Path("b"), Path("/a")) must equal (Path("/a/b"))
      Path.relativize(Path("c"), Path("/a/b")) must equal (Path("/a/b/c"))
      Path.relativize(Path("c/d"), Path("/a/b")) must equal (Path("/a/b/c/d"))
      Path.relativize(Path("../../c"), Path("/a/b")) must equal (Path("/c"))
    }

    it("should work with absolute paths") {
      Path.relativize(Path("/a"), Path("/")) must equal (Path("/a"))
      Path.relativize(Path("/b"), Path("/a")) must equal (Path("/b"))
      Path.relativize(Path("/c/d"), Path("/a/b")) must equal (Path("/c/d"))
    }
  }

  describe("Path.{name, base, ext}") {
    val coffee = Path("src/js/main.coffee")
    val readme = Path("src/js/README")

    it("should work with filenames that have extensions") {
      coffee.name must equal ("main.coffee")
      coffee.base must equal ("main")
      coffee.ext  must equal ("coffee")
    }

    it("should work with filenames that have no extensions") {
      readme.name must equal ("README")
      readme.base must equal ("README")
      readme.ext  must equal ("")
    }
  }
}