package com.untyped.sbtless

import sbt._

trait Source extends com.untyped.sbtgraph.Source {

  type S = com.untyped.sbtless.Source
  type G = com.untyped.sbtless.Graph

  val graph: G

  def compile: Option[File]

  def importlessRawSource: String =
    IO.readLines(src).map { line =>
      LessSource.importRegex.replaceAllIn(line, "")
    }.mkString("\n")

  def completeRawSource: String =
    graph.ancestors(this).foldLeft("")(_ + _.importlessRawSource)

}
