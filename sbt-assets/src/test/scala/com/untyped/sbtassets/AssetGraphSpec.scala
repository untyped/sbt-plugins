package com.untyped.sbtassets

import sbt._

class AssetGraphSpec extends BaseSpec {
  describe("AssetGraph.assets") {
    it("should return sorted assets") {
      val dummy = file("dummy")
      val a = Asset(Path("/a"), dummy, List(Path("/b")))
      val b = Asset(Path("/b"), dummy, List(Path("/c")))
      val c = Asset(Path("/c"), dummy, Nil)
      val x = Asset(Path("/x"), dummy, Nil)
      val y = Asset(Path("/y"), dummy, List(Path("/x")))
      val z = Asset(Path("/z"), dummy, List(Path("/y")))

      val graph  = AssetGraph(List(a, x, b, y, c, z))
      val assets = graph.sorted

      println(graph.transitiveDependents)
      println(graph.transitivePrecedents)
      println(graph.unsorted)
      println(graph.sorted)

      graph.compare(a, b) must be > (0)
      graph.compare(b, a) must be < (0)
      graph.compare(a, c) must be > (0)
      graph.compare(c, a) must be < (0)

      graph.compare(x, y) must be < (0)
      graph.compare(y, x) must be > (0)
      graph.compare(x, z) must be < (0)
      graph.compare(z, x) must be > (0)

      graph.compare(a, x) must be < (0)
      graph.compare(x, a) must be > (0)

      assets.indexOf(a) must be > (assets.indexOf(b))
      assets.indexOf(a) must be > (assets.indexOf(c))
      assets.indexOf(b) must be > (assets.indexOf(c))
      assets.indexOf(x) must be < (assets.indexOf(y))
      assets.indexOf(x) must be < (assets.indexOf(z))
      assets.indexOf(y) must be < (assets.indexOf(z))
    }
  }
}