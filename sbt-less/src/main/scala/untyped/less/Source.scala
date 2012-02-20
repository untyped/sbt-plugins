package untyped.less

import sbt._

trait Source extends untyped.graph.Source {

  type S = untyped.less.Source
  type G = untyped.less.Graph

  val graph: G

  def compile: Option[File]

  def importlessRawSource: String =
    IO.readLines(src).map { line =>
      LessSource.importRegex.replaceAllIn(line, "")
    }.mkString("\n")

  def completeRawSource: String =
    graph.ancestors(this).foldLeft("")(_ + _.importlessRawSource)

}
