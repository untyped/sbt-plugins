package com.untyped.sbtmustache

import java.util.Properties
import sbt._

case class Graph(
  log: Logger,
  sourceDirs: Seq[File],
  targetDir: File,
  templateProperties: Properties,
  downloadDir: File,
  filenameSuffix: String
) extends com.untyped.sbtgraph.Graph {

  type S = com.untyped.sbtmustache.Source

  override def createSource(src: File): Source = {
    log.debug("Graph.createSource " + src)
    Source(this, src.getCanonicalFile)
  }

  def srcFilenameToDesFilename(filename: String) = {
    val parts = filename.split(".").toList
    if(parts.length > 1) {
      parts.dropRight(1).mkString(".") + filenameSuffix + parts.last
    } else {
      filename
    }
  }

  val pluginName = "sbt-mustache"

}
