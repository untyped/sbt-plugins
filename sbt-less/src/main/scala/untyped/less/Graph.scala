package untyped.less

import sbt._
import scala.collection._

case class Graph(
    val log: Logger,
    val sourceDir: File,
    val targetDir: File,
    val propertiesDir: File,
    val downloadDir: File,
    val prettyPrint: Boolean
  ) extends untyped.graph.Graph {

  type S = untyped.less.Source

  def createSource(src: File): Source =
    if(src.toString.trim.toLowerCase.endsWith(".less")) {
      LessSource(this, src.getCanonicalFile, srcToDes(src).getCanonicalFile)
    } else {
      CssSource(this, src.getCanonicalFile, srcToDes(src).getCanonicalFile)
    }

  def srcFilenameToDesFilename(filename: String) =
    filename.replaceAll("[.]less$", ".css")

  val pluginName = "sbt-less"
  
}