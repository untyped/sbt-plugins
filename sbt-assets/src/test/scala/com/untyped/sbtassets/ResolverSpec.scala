package com.untyped.sbtassets

import sbt._
import scala.util.matching.Regex

class ResolverSpec extends BaseSpec {
  val dir =
    createTemporaryFiles(
      "foo/a.js" -> "a",
      "bar/b.coffee" -> "b",
      "baz/c.less" -> "c"
    )

  val otherDir =
    createTemporaryFiles(
      "etc/d.css" -> "d"
    )

  describe("Dir") {
    it("should resolve files") {
      val resolver = Resolvers.Dir(dir)

      resolver(Path("foo/a"), "js") must equal (Some(dir / "foo/a.js"))
      resolver(Path("bar/b"), "coffee") must equal (Some(dir / "bar/b.coffee"))
      resolver(Path("baz/c"), "") must equal (None)
      resolver(Path("etc/d"), "css") must equal (None)
    }
  }

  describe("Or") {
    it("should resolve files") {
      var resolver = Resolvers.Or(List(
        Resolvers.Dir(dir),
        Resolvers.Dir(otherDir)
      ))

      resolver(Path("foo/a"), "js") must equal (Some(dir / "foo/a.js"))
      resolver(Path("bar/b"), "coffee") must equal (Some(dir / "bar/b.coffee"))
      resolver(Path("baz/c"), "") must equal (None)
      resolver(Path("etc/d"), "css") must equal (Some(otherDir / "etc/d.css"))
    }
  }

  describe("Extensions") {
    it("should resolve files") {
      var resolver =
        Resolvers.Extensions(List("js", "coffee"), Resolvers.Dir(dir))

      // TODO: This test is silly - the fake resolvers always find the files.
      // Rewrite with create
      resolver(Path("foo/a"), "") must equal (Some(dir / "foo/a.js"))
      resolver(Path("bar/b"), "") must equal (Some(dir / "bar/b.coffee"))
      resolver(Path("baz/c"), "") must equal (None)
      resolver(Path("etc/d"), "css") must equal (None)
    }
  }
}