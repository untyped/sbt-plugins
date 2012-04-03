package com.untyped.sbtless

import sbt._

trait Source extends com.untyped.sbtgraph.Source {

  type S = com.untyped.sbtless.Source
  type G = com.untyped.sbtless.Graph

  val graph: G

  def compile: Option[File]

  def completeRawSource: String = 
    IO.readLines(src).map { line =>
      
      LessSource.parseImport(line.trim) match {
        case Some(parentName) => graph.getSource(parentName, this).lines.mkString("\n")
        case _ => line
      }
    }.mkString("\n")
    
 

}
