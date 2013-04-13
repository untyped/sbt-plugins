package com.untyped.sbtassets

import org.scalatest._
import org.scalatest.matchers._
import sbt._

class SourceSuite extends FunSuite with MustMatchers {

  test("Source.{name, base, ext}") {
    val coffee = Source("src/js/main.coffee", file("/home/developer/project"), Nil)
    val readme = Source("src/js/README", file("/home/developer/project"), Nil)

    coffee.name must equal ("main.coffee")
    coffee.base must equal ("main")
    coffee.ext  must equal ("coffee")

    readme.name must equal ("README")
    readme.base must equal ("README")
    readme.ext  must equal ("")
  }

  test("Sources.shadow") {
    val fooRoot = file("/home/developer/project")
    val fooA    = Source("src/js/a.coffee", fooRoot, Nil)
    val fooB    = Source("src/js/b.coffee", fooRoot, Nil)
    val foo     = Sources(List(fooA, fooB))

    val barRoot = file("/home/developer/project")
    val barB    = Source("src/js/b.coffee", barRoot, Nil)
    val barC    = Source("src/js/c.coffee", fooRoot, Nil)
    val bar     = Sources(List(barB, barC))

    (bar shadow foo) must equal (Sources(List(barB, barC, fooA)))
  }

  test("Sources.orderedSources") {
    val fooRoot = file("/home/developer/project")
    val fooA    = Source("src/js/a.coffee", fooRoot, Nil)
    val fooB    = Source("src/js/b.coffee", fooRoot, Nil)

    Sources(List(fooB, fooA)).orderedSources must equal (List(fooA, fooB))
  }

}