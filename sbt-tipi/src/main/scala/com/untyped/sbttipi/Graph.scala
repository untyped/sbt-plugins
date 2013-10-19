package com.untyped.sbttipi

import sbt._
import scala.collection._
import tipi.core._

case class Graph(
  log: Logger,
  sourceDirs: Seq[File],
  targetDir: File,
  environment: Env,
  downloadDir: File
) extends com.untyped.sbtgraph.Graph {
  type S = com.untyped.sbttipi.Source

  // Not used:
  val templateProperties = null

  val parse  = Parser()
  val expand = Expand
  val render = Render

  override def createSource(src: File): Source =
    Source(this, src.getCanonicalFile)

  def srcFilenameToDesFilename(filename: String) =
    filename

  val pluginName = "sbt-tipi"

  override def toString =
    "Graph(%s, %s)".format(pluginName, sources)
}
