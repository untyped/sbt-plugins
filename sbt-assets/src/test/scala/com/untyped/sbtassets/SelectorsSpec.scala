package com.untyped.sbtassets

import org.scalatest._
import org.scalatest.matchers._
import sbt._

class SelectorsSpec extends BaseSpec {
  describe("Deps") {
    it("should select sources") {
      val dir = createTemporaryFiles(
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

      Selectors.Deps(
        Path("/a"),
        Resolvers.Extensions(List(".js"), Resolvers.Dir(dir))
      ).sources must equal(List(
        Source(Path("/a"),     dir / "a.js",     List(Path("/lib/b"))),
        Source(Path("/lib/b"), dir / "lib/b.js", List(Path("/lib/c"), Path("/d"), Path("/e"))),
        Source(Path("/lib/c"), dir / "lib/c.js", List()),
        Source(Path("/d"),     dir / "d.js",     List()),
        Source(Path("/e"),     dir / "e.js",     List())
      ))
    }

    it("should prevent filesystem escapes") {
      val dir = createTemporaryFiles(
        "a.js"     -> """
                      // require "lib/b"
                      """,
        "lib/b.js" -> """
                      // require "../../c"
                      """
      )

      intercept[Exception] {
        Selectors.Deps(
          Path("/a"),
          Resolvers.Extensions(List(".js"), Resolvers.Dir(dir))
        ).sources
      }
    }
  }
}