package com.untyped.sbtassets

import org.scalatest._
import org.scalatest.matchers._
import sbt._
import scala.util.matching.Regex

class ResolverSuite extends FunSuite with MustMatchers {

  object Resolvers extends Resolvers {

    // Fake version of Resolver.root for use in these tests:
    def fake(root: File, regex: Regex): Resolver =
      (path: String, myPath: String) =>
        for {
          path <- relativize(path, myPath)
          file <- if(regex.findFirstIn(path).isDefined) Some(root / path) else None
        } yield file

  }

  val cwd = file(".")

  test("Resolvers.relativize") {
    Resolvers.relativize("a", "") must equal (Some("a"))
    Resolvers.relativize("/a", "") must equal (Some("a"))
    Resolvers.relativize("b", "a") must equal (Some("a/b"))
    Resolvers.relativize("/b", "a") must equal (Some("b"))
    Resolvers.relativize("c/d", "a/b") must equal (Some("a/b/c/d"))
    Resolvers.relativize("/c/d", "a/b") must equal (Some("c/d"))
  }

  test("Resolvers.root") {
    val resolver = Resolvers.root(cwd)

    resolver("README.markdown", "") must equal (Some(cwd / "README.markdown"))
    resolver("DONTREADME.markdown", "") must equal (None)
  }

  test("Resolvers.fake") {
    val resolver = Resolvers.fake(file("/foo"), "bar.*".r)

    resolver("bar", "") must equal (Some(file("/foo/bar")))
    resolver("baz", "") must equal (None)
    resolver("abc", "bar") must equal (Some(file("/foo/bar/abc")))
    resolver("abc", "baz") must equal (None)
  }

  test("Resolvers.or") {
    var resolver = Resolvers.or(
      Resolvers.fake(file("/foo"), "a".r),
      Resolvers.fake(file("/bar"), "b".r)
    )

    resolver("a", "") must equal (Some(file("/foo/a")))
    resolver("b", "") must equal (Some(file("/bar/b")))
    resolver("c", "") must equal (None)
  }

  test("Resolvers.extensions") {
    var resolver = Resolvers.extensions(".js", ".coffee")(Resolvers.or(
      Resolvers.fake(file("/foo"), "a[.]coffee".r),
      Resolvers.fake(file("/bar"), "b[.]js".r)
    ))

    resolver("a", "") must equal (Some(file("/foo/a.coffee")))
    resolver("b", "") must equal (Some(file("/bar/b.js")))
    resolver("c", "") must equal (None)
  }

}