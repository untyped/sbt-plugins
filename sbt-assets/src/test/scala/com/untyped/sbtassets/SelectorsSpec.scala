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
    it("should select sources") {
      Selectors.Deps(
        Path("/a"),
        Resolvers.Extensions(List(".js"), Resolvers.Dir(normalDir))
      ).sources must equal(List(
        Source(Path("/a"),     normalDir / "a.js",     List(Path("/lib/b"))),
        Source(Path("/lib/b"), normalDir / "lib/b.js", List(Path("/lib/c"), Path("/d"), Path("/e"))),
        Source(Path("/lib/c"), normalDir / "lib/c.js", List()),
        Source(Path("/d"),     normalDir / "d.js",     List()),
        Source(Path("/e"),     normalDir / "e.js",     List())
      ))
    }

    it("should prevent filesystem escapes") {
      intercept[Exception] {
        Selectors.Deps(
          Path("/a"),
          Resolvers.Extensions(List(".js"), Resolvers.Dir(escapeDir))
        ).sources
      }
    }
  }

  describe("Dir") {
    it("should select sources") {
      Selectors.Dir(normalDir).sources must equal(List(
        Source(Path("/a"),     normalDir / "a.js",     List(Path("/lib/b"))),
        Source(Path("/d"),     normalDir / "d.js",     List()),
        Source(Path("/e"),     normalDir / "e.js",     List()),
        Source(Path("/lib/b"), normalDir / "lib/b.js", List(Path("/lib/c"), Path("/d"), Path("/e"))),
        Source(Path("/lib/c"), normalDir / "lib/c.js", List())
      ))
    }

    it("should prevent filesystem escapes") {
      intercept[Exception] {
        Selectors.Dir(escapeDir).sources
      }
    }
  }
}