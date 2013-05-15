package com.untyped.sbtassets

import sbt._
import scala.util.matching.Regex

class ResolverSpec extends BaseSpec {
  val dir =
    createTemporaryFiles(
      "foo/a.js" -> "a",
      "foo/a.coffee" -> "a",
      "foo/z" -> "z",
      "bar/b.coffee" -> "b",
      "baz/c.less" -> "c"
    )

  val otherDir =
    createTemporaryFiles(
      "foo/a" -> "a",
      "etc/d.css" -> "d"
    )

  describe("Dir") {
    it("should expand paths") {
      val resolver = Resolvers.Dir(dir)

      resolver.expand(Path("/foo/a")) must equal (List(Path("/foo/a")))
      resolver.expand(Path("/foo/*")) must equal (List(Path("/foo/a"), Path("/foo/z")))
      resolver.expand(Path("/**")) must equal (List(
        Path("/bar/b"),
        Path("/baz/c"),
        Path("/foo/a"),
        Path("/foo/z")
      ))
    }

    it("should find files") {
      val resolver = Resolvers.Dir(dir)

      resolver.find(Path("foo/a")) must equal (Some(dir / "foo/a.coffee"))
      resolver.find(Path("bar/b")) must equal (Some(dir / "bar/b.coffee"))
      resolver.find(Path("baz/c")) must equal (Some(dir / "baz/c.less"))
      resolver.find(Path("etc/d")) must equal (None)
    }
  }

  describe("Or") {
    it("should find files") {
      var resolver = Resolvers.Or(List(
        Resolvers.Dir(dir),
        Resolvers.Dir(otherDir)
      ))

      resolver.find(Path("foo/a")) must equal (Some(dir / "foo/a.coffee"))
      resolver.find(Path("bar/b")) must equal (Some(dir / "bar/b.coffee"))
      resolver.find(Path("baz/c")) must equal (Some(dir / "baz/c.less"))
      resolver.find(Path("etc/d")) must equal (Some(otherDir / "etc/d.css"))
    }
  }
}