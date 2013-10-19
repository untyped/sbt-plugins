package com.untyped.sbtmustache

import java.util.Properties
import sbt._

case class Graph(
  log: Logger,
  sourceDirs: Seq[File],
  targetDir: File,
  templateProperties: Properties,
  downloadDir: File
) extends com.untyped.sbtgraph.Graph {

  type S = com.untyped.sbtmustache.Source

  override def createSource(src: File): Source = {
    log.debug("Graph.createSource " + src)
    Source(this, src.getCanonicalFile)
  }

  def srcFilenameToDesFilename(filename: String) =
    filename

  val pluginName = "sbt-mustache"

}
