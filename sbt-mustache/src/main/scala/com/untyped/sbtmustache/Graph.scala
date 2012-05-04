package com.untyped.sbtmustache

import java.util.Properties
import sbt._
import scala.collection._

case class Graph(
    val log: Logger,
    val sourceDirs: Seq[File],
    val targetDir: File,
    val templateProperties: Properties,
    val downloadDir: File
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
