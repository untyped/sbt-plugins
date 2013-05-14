package com.untyped.sbtassets

import org.scalatest._
import org.scalatest.matchers._
import sbt._

class SelectorsSpec extends BaseSpec {
  val normalDir = createTemporaryFiles(
    "a.js"     -> """
                  // require "lib/b"
                  """,
    "lib/b.js" -> """
                  // require "c"
                  // require "../d"
                  // require "/e"
                  """,
    "lib/c.js" -> "",
    "d.js"     -> "",
    "e.js"     -> ""
  )

  val escapeDir = createTemporaryFiles(
    "a.js"     -> """
                  // require "lib/b"
                  """,
    "lib/b.js" -> """
                  // require "../../c"
                  """
  )

  describe("Deps") {
    def makeSelector(dir: File) =
      Selectors.Deps(Path("/a"), Resolvers.Extensions(List("js"), Resolvers.Dir(dir)))

    it("should select assets") {
      val expected =
        List(
          Asset(Path("/a"),     normalDir / "a.js",     List(Path("/lib/b"))),
          Asset(Path("/lib/b"), normalDir / "lib/b.js", List(Path("/lib/c"), Path("/d"), Path("/e"))),
          Asset(Path("/lib/c"), normalDir / "lib/c.js", List()),
          Asset(Path("/d"),     normalDir / "d.js",     List()),
          Asset(Path("/e"),     normalDir / "e.js",     List())
        )

      makeSelector(normalDir).assets must equal (expected)
      makeSelector(normalDir).unmanagedAssets must equal (expected)
      makeSelector(normalDir).managedAssets must equal (Nil)
    }

    it("should prevent filesystem escapes") {
      intercept[Exception] {
        makeSelector(escapeDir).assets
      }
    }

    it("should clean the correct assets") {
      makeSelector(normalDir).clean(log)

      (normalDir / "a.js") must exist
      (normalDir / "lib/b.js") must exist
      (normalDir / "lib/c.js") must exist
      (normalDir / "d.js") must exist
      (normalDir / "e.js") must exist
    }
  }

  describe("Dir") {
    def makeSelector(dir: File) =
      Selectors.Dir(dir)

    it("should select assets") {
      val expected =
        List(
          Asset(Path("/a"),     normalDir / "a.js",     List(Path("/lib/b"))),
          Asset(Path("/d"),     normalDir / "d.js",     List()),
          Asset(Path("/e"),     normalDir / "e.js",     List()),
          Asset(Path("/lib/b"), normalDir / "lib/b.js", List(Path("/lib/c"), Path("/d"), Path("/e"))),
          Asset(Path("/lib/c"), normalDir / "lib/c.js", List())
        )

      makeSelector(normalDir).assets must equal (expected)
      makeSelector(normalDir).unmanagedAssets must equal (expected)
      makeSelector(normalDir).managedAssets must equal (Nil)
    }

    it("should prevent filesystem escapes") {
      intercept[Exception] {
        makeSelector(escapeDir).assets
      }
    }

    it("should clean the correct assets") {
      makeSelector(normalDir).clean(log)

      (normalDir / "a.js") must exist
      (normalDir / "lib/b.js") must exist
      (normalDir / "lib/c.js") must exist
      (normalDir / "d.js") must exist
      (normalDir / "e.js") must exist
    }
  }
}