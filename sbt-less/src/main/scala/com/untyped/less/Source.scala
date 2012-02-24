package com.untyped.less

import sbt._

trait Source extends com.untyped.graph.Source {

  type S = com.untyped.less.Source
  type G = com.untyped.less.Graph

  val graph: G

  def compile: Option[File]

  def importlessRawSource: String =
    IO.readLines(src).map { line =>
      LessSource.importRegex.replaceAllIn(line, "")
    }.mkString("\n")

  def completeRawSource: String =
    graph.ancestors(this).foldLeft("")(_ + _.importlessRawSource)

}
