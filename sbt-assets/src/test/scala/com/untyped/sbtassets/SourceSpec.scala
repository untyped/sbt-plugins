package com.untyped.sbtassets

import org.scalatest._
import org.scalatest.matchers._
import sbt._

class SourceSpec extends BaseSpec {
  // test("Sources.shadow") {
  //   val fooRoot = file("/home/developer/project")
  //   val fooA    = Source(Path("/src/js/a.coffee"), fooRoot, Nil)
  //   val fooB    = Source(Path("/src/js/b.coffee"), fooRoot, Nil)
  //   val foo     = Sources(List(fooA, fooB))

  //   val barRoot = file("/home/developer/project")
  //   val barB    = Source(Path("/src/js/b.coffee"), barRoot, Nil)
  //   val barC    = Source(Path("/src/js/c.coffee"), fooRoot, Nil)
  //   val bar     = Sources(List(barB, barC))

  //   (bar shadow foo) must equal (Sources(List(barB, barC, fooA)))
  // }

  // test("Sources.orderedSources") {
  //   val fooRoot = file("/home/developer/project")
  //   val fooA    = Source(Path("/src/js/a.coffee"), fooRoot, Nil)
  //   val fooB    = Source(Path("/src/js/b.coffee"), fooRoot, Nil)

  //   Sources(List(fooB, fooA)).orderedSources must equal (List(fooA, fooB))
  // }
}