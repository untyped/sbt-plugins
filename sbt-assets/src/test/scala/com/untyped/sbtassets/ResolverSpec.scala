package com.untyped.sbtassets

import sbt._
import scala.util.matching.Regex

class ResolverSpec extends BaseSpec {
  object Resolvers extends Resolvers {
    def Fake(root: File, regex: Regex): Resolver =
      (path: Path) =>
        for {
          file <- regex.findFirstIn(path.toString).map { _ =>
                    root / path.toString
                  }
        } yield file
  }

  describe("Dir") {
    it("should resolve files") {
      val resolver = Resolvers.Dir(cwd)
      resolver(Path("README.markdown")) must equal (Some(cwd / "README.markdown"))
      resolver(Path("DONTREADME.markdown")) must equal (None)
    }
  }

  describe("Fake") {
    it("should resolve files") {
      val resolver = Resolvers.Fake(file("/foo"), "bar.*".r)
      resolver(Path("bar")) must equal (Some(file("/foo/bar")))
      resolver(Path("baz")) must equal (None)
      resolver(Path("bar/abc")) must equal (Some(file("/foo/bar/abc")))
      resolver(Path("baz/abc")) must equal (None)
    }
  }

  describe("Or") {
    it("should resolve files") {
      var resolver = Resolvers.Or(List(
        Resolvers.Fake(file("/foo"), "a".r),
        Resolvers.Fake(file("/bar"), "b".r)
      ))

      resolver(Path("a")) must equal (Some(file("/foo/a")))
      resolver(Path("b")) must equal (Some(file("/bar/b")))
      resolver(Path("c")) must equal (None)
    }
  }

  describe("Extensions") {
    it("should resolve files") {
      var resolver = Resolvers.Extensions(List(".js", ".coffee"), Resolvers.Or(List(
        Resolvers.Fake(file("/foo"), "a[.]coffee".r),
        Resolvers.Fake(file("/bar"), "b[.]js".r)
      )))

      resolver(Path("a")) must equal (Some(file("/foo/a.coffee")))
      resolver(Path("b")) must equal (Some(file("/bar/b.js")))
      resolver(Path("c")) must equal (None)
    }
  }
}