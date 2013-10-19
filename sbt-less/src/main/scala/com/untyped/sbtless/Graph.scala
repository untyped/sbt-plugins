package com.untyped.sbtless

import java.util.Properties
import sbt._
import scala.collection._

case class Graph(
    log: Logger,
    sourceDirs: Seq[File],
    targetDir: File,
    templateProperties: Properties,
    downloadDir: File,
    lessVersion: Plugin.LessVersion,
    prettyPrint: Boolean,
    useCommandLine: Boolean = false
  ) extends com.untyped.sbtgraph.Graph {

  type S = com.untyped.sbtless.Source

  def createSource(src: File): Source =
    if(src.toString.trim.toLowerCase.endsWith(".less")) {
      LessSource(this, src.getCanonicalFile)
    } else {
      CssSource(this, src.getCanonicalFile)
    }

  def srcFilenameToDesFilename(filename: String) =
    filename.replaceAll("[.]less$", ".css")

  val pluginName = "sbt-less"

  override def dump(): Unit = {
    log.debug("Graph for " + pluginName + ":")

    log.debug("  lessVersion:")
    log.debug("    " + lessVersion.filename)

    log.debug("  prettyPrint:")
    log.debug("    " + prettyPrint)

    log.debug("  templateProperties:")
    log.debug("    " + templateProperties)

    log.debug("  downloadDir:")
    log.debug("    " + downloadDir)

    sources.foreach(dumpSource)
  }

}