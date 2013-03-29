package com.untyped.sbtassets

import java.io.FileNotFoundException
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

  test("relativize") {
    Resolvers.relativize("a", "") must equal (Some("a"))
    Resolvers.relativize("/a", "") must equal (Some("a"))
    Resolvers.relativize("b", "a") must equal (Some("a/b"))
    Resolvers.relativize("/b", "a") must equal (Some("b"))
    Resolvers.relativize("c/d", "a/b") must equal (Some("a/b/c/d"))
    Resolvers.relativize("/c/d", "a/b") must equal (Some("c/d"))
  }

  test("root") {
    import Resolvers._

    val resolver = root(cwd)

    resolver("README.markdown", "") must equal (Some(cwd / "README.markdown"))
    resolver("DONEREADME.markdown", "") must equal (None)
  }

  test("fake") {
    import Resolvers._

    val resolver = fake(file("/foo"), "bar.*".r)

    resolver("bar", "") must equal (Some(file("/foo/bar")))
    resolver("baz", "") must equal (None)
    resolver("abc", "bar") must equal (Some(file("/foo/bar/abc")))
    resolver("abc", "baz") must equal (None)
  }

  test("or") {
    import Resolvers._

    var resolver = or(
      fake(file("/foo"), "a".r),
      fake(file("/bar"), "b".r)
    )

    resolver("a", "") must equal (Some(file("/foo/a")))
    resolver("b", "") must equal (Some(file("/bar/b")))
    resolver("c", "") must equal (None)
  }

  test("extensions") {
    import Resolvers._

    var resolver = extensions(".js", ".coffee")(or(
      fake(file("/foo"), "a[.]coffee".r),
      fake(file("/bar"), "b[.]js".r)
    ))

    resolver("a", "") must equal (Some(file("/foo/a.coffee")))
    resolver("b", "") must equal (Some(file("/bar/b.js")))
    resolver("c", "") must equal (None)
  }

}