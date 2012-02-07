package untyped.less

import java.util.Properties
import sbt._
import scala.collection._

case class Graph(
    val log: Logger,
    val sourceDir: File,
    val targetDir: File,
    val templateProperties: Properties,
    val downloadDir: File,
    val lessVersion: Plugin.LessVersion,
    val prettyPrint: Boolean
  ) extends untyped.graph.Graph {

  type S = untyped.less.Source

  def createSource(src: File): Source =
    if(src.toString.trim.toLowerCase.endsWith(".less")) {
      LessSource(this, src.getCanonicalFile)
    } else {
      CssSource(this, src.getCanonicalFile)
    }

  def srcFilenameToDesFilename(filename: String) =
    filename.replaceAll("[.]less$", ".css")

  val pluginName = "sbt-less"
  
}