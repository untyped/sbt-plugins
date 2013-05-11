package com.untyped.sbtassets

import sbt._

class SynopsisSpec extends BaseSpec {
  describe("coffeescript project") {
    val rootDir = createTemporaryFiles(
      "src/main.coffee" -> "#require \"a.coffee\"\n#require \"b.coffee\"",
      "src/a.coffee"    -> "alert 'a'",
      "src/b.coffee"    -> "alert 'b'"
    )

    val srcDir  = rootDir / "src"
    val tempDir = rootDir / "temp"
    val distDir = rootDir / "dist"

    val mainSrc   = Selectors.Deps(Path.Root / "main", Resolvers.Extensions(List(".js", ".coffee"), Resolvers.Dir(srcDir)))

    val jsSrc     = Rules.Filter(mainSrc, _.file.ext == "js")
    val coffeeSrc = Rules.Filter(mainSrc, _.file.ext == "coffee")

    val coffeeJs  = Rules.Coffee(tempDir / "js", coffeeSrc)
    val dist      = Rules.Cat(distDir / "dist.js", Rules.Append(List(jsSrc, coffeeJs)))
    val distMin   = Rules.UglifyJs(distDir / "dist.min.js", dist)

    it("should produce a single minified output") {
      distMin.compile
      IO.read(distMin.sources.head.file) must equal ("alert(\"a\");alert(\"b\");")
    }
  }
}