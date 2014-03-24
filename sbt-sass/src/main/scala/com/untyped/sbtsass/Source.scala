package com.untyped.sbtsass

import sbt._

trait Source extends com.untyped.sbtgraph.Source {

  type S = com.untyped.sbtsass.Source
  type G = com.untyped.sbtsass.Graph

  val graph: G

  def compile: Option[File]

  def importsassRawSource: String =
    IO.readLines(src).map { line =>
      SassSource.onlinerImportRegex.replaceAllIn(line, "")
    }.mkString("\n")

  def completeRawSource: String =
    graph.ancestors(this).foldLeft("")(_ + _.importsassRawSource)
}
