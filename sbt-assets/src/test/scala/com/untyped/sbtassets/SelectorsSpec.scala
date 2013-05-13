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
    it("should select assets") {
      Selectors.Deps(
        Path("/a"),
        Resolvers.Extensions(List(".js"), Resolvers.Dir(normalDir))
      ).assets must equal(List(
        Asset(Path("/a"),     normalDir / "a.js",     List(Path("/lib/b"))),
        Asset(Path("/lib/b"), normalDir / "lib/b.js", List(Path("/lib/c"), Path("/d"), Path("/e"))),
        Asset(Path("/lib/c"), normalDir / "lib/c.js", List()),
        Asset(Path("/d"),     normalDir / "d.js",     List()),
        Asset(Path("/e"),     normalDir / "e.js",     List())
      ))
    }

    it("should prevent filesystem escapes") {
      intercept[Exception] {
        Selectors.Deps(
          Path("/a"),
          Resolvers.Extensions(List(".js"), Resolvers.Dir(escapeDir))
        ).assets
      }
    }
  }

  describe("Dir") {
    it("should select assets") {
      Selectors.Dir(normalDir).assets must equal(List(
        Asset(Path("/a"),     normalDir / "a.js",     List(Path("/lib/b"))),
        Asset(Path("/d"),     normalDir / "d.js",     List()),
        Asset(Path("/e"),     normalDir / "e.js",     List()),
        Asset(Path("/lib/b"), normalDir / "lib/b.js", List(Path("/lib/c"), Path("/d"), Path("/e"))),
        Asset(Path("/lib/c"), normalDir / "lib/c.js", List())
      ))
    }

    it("should prevent filesystem escapes") {
      intercept[Exception] {
        Selectors.Dir(escapeDir).assets
      }
    }
  }
}