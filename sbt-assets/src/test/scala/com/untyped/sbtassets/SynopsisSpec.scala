package com.untyped.sbtassets

import sbt._

class SynopsisSpec extends BaseSpec {
  describe("coffeescript project") {
    val rootDir   = createTemporaryFiles(
                      "src/main.coffee" -> "#require \"a.coffee\"\n#require \"b.coffee\"",
                      "src/a.coffee"    -> "alert 'a'",
                      "src/b.coffee"    -> "alert 'b'"
                    )
    val srcDir    = rootDir / "src"
    val tempDir   = rootDir / "temp"
    val distDir   = rootDir / "dist"
    val mainSrc   = Selectors.Deps(Path.Root / "main", Resolvers.Extensions(List(".js", ".coffee"), Resolvers.Dir(srcDir)))
    val jsSrc     = Rules.Filter(_.file.ext == "js", mainSrc)
    val coffeeSrc = Rules.Filter(_.file.ext == "coffee", mainSrc)
    val coffeeJs  = Rules.Coffee(tempDir / "js", coffeeSrc)
    val dist      = Rules.Cat(distDir / "dist.js", Rules.Append(List(jsSrc, coffeeJs)))
    val distMin   = Rules.UglifyJs(distDir / "dist.min.js", dist)

    it("should produce a single minified output") {
      println("-----")
      distMin.compile()
      println("-----")

      println("-----")
      distMin.compile()
      println("-----")

      IO.read(distMin.assets.head.file) must equal ("alert(\"a\");alert(\"b\");")
    }
  }
}
