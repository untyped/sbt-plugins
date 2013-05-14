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
    val mainSrc   = Selectors.Deps(Path.Root / "main", Resolvers.Extensions(List("js", "coffee"), Resolvers.Dir(srcDir)))
    val jsSrc     = Rules.Filter(_.file.ext == "js", mainSrc)
    val coffeeSrc = Rules.Filter(_.file.ext == "coffee", mainSrc)
    val coffeeJs  = Rules.Coffee(tempDir / "js", coffeeSrc)
    val dist      = Rules.Cat(distDir / "dist.js", Rules.Append(List(jsSrc, coffeeJs)))
    val distMin   = Rules.UglifyJs(distDir / "dist.min.js", dist)

    it("should return the correct lists of assets") {
      distMin.assets must equal (List(
        Asset(Path.Root, distDir / "dist.min.js", Nil)
      ))
      distMin.unmanagedAssets must equal (List(
        Asset(Path("/main"), srcDir / "main.coffee", List(Path("/a"), Path("/b"))),
        Asset(Path("/a"), srcDir / "a.coffee", List()),
        Asset(Path("/b"), srcDir / "b.coffee", List())
      ))
      distMin.managedAssets must equal (List(
       Asset(Path("/main"), tempDir / "js/main.js", List(Path("/a"), Path("/b"))),
       Asset(Path("/a"), tempDir / "js/a.js", List()),
       Asset(Path("/b"), tempDir / "js/b.js", List()),
       Asset(Path.Root, distDir / "dist.js", List()),
       Asset(Path.Root, distDir / "dist.min.js", List())
      ))
    }

    it("should produce a single minified output") {
      distMin.compile(log)
      IO.read(distMin.assets.head.file) must equal ("""(function(){alert("a")}).call(this);(function(){alert("b")}).call(this);(function(){}).call(this);""")
    }

    it("should clean managed files only") {
      (rootDir) must exist
      (rootDir / "src/main.coffee") must exist
      (rootDir / "src/a.coffee") must exist
      (rootDir / "src/b.coffee") must exist
      (tempDir) must exist
      (tempDir / "js/main.js") must exist
      (tempDir / "js/a.js") must exist
      (tempDir / "js/b.js") must exist
      (distDir) must exist
      (distDir / "dist.js") must exist
      (distDir / "dist.min.js") must exist

      distMin.clean(log)

      (rootDir) must exist
      (rootDir / "src/main.coffee") must exist
      (rootDir / "src/a.coffee") must exist
      (rootDir / "src/b.coffee") must exist
      (tempDir) must exist
      (tempDir / "js/main.js") must not (exist)
      (tempDir / "js/a.js") must not (exist)
      (tempDir / "js/b.js") must not (exist)
      (distDir) must exist
      (distDir / "dist.js") must not (exist)
      (distDir / "dist.min.js") must not (exist)
    }
  }
}
