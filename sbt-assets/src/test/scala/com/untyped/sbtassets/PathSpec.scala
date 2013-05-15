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

  describe("Path.{ parent, name }") {
    it("should work with filenames that have extensions") {
      Path("src/js/main").parent must equal (Path("src/js"))
      Path("src/js/main").name must equal ("main")
    }
  }

  describe("Path.pathFinder") {
    val dir = createTemporaryFiles(
      "foo/a"        -> "a",
      "foo/a.js"     -> "a.js",
      "foo/b"        -> "b",
      "foo/bar/c"    -> "c",
      "baz/a"        -> "a"
    )

    it("should select files with all extensions") {
      Path("foo/a").pathFinder(dir).get.toList must equal (List(dir / "foo/a", dir / "foo/a.js"))
    }

    it("should work with single depth wildcard filenames") {
      Path("foo/*").pathFinder(dir).get.toList must equal (List(
        dir / "foo/a",
        dir / "foo/a.js",
        dir / "foo/b",
        dir / "foo/bar"
      ))
    }

    it("should work with arbitrary depth wildcard filenames") {
      Path("foo/**").pathFinder(dir).get.toList must equal (List(
        dir / "foo",
        dir / "foo/a",
        dir / "foo/a.js",
        dir / "foo/b",
        dir / "foo/bar",
        dir / "foo/bar/c"
      ))
    }
  }
}