package untyped.less

import sbt._

trait Source extends untyped.graph.Source {
  
  type S = untyped.less.Source
  type G = untyped.less.Graph

  def compile: Option[File]
  
}
