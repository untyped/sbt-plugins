package untyped.mustache

import java.util.Properties
import sbt._
import scala.collection._

case class Graph(
    val log: Logger,
    val sourceDir: File,
    val targetDir: File,
    val templateProperties: Properties,
    val downloadDir: File
  ) extends untyped.graph.Graph {

  type S = untyped.mustache.Source

  override def createSource(src: File): Source = {
    log.debug("Graph.createSource " + src)
    Source(this, src.getCanonicalFile)
  }

  def srcFilenameToDesFilename(filename: String) = {
    log.debug("Graph.srcFilenameToDesFilename " + filename)
    filename
  }

  val pluginName = "sbt-mustache"

}
