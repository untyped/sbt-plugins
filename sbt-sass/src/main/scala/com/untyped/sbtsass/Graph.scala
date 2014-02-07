package com.untyped.sbtsass

import java.util.Properties
import sbt._

case class Graph(
  log: Logger,
  sourceDirs: Seq[File],
  targetDir: File,
  templateProperties: Properties,
  downloadDir: File,
  sassVersion: Plugin.SassVersion,
  prettyPrint: Boolean,
  useCommandLine: Boolean = false
) extends com.untyped.sbtgraph.Graph {

  type S = com.untyped.sbtsass.Source

  def createSource(src: File): S = {
    val srcLowerCase: String = src.toString.trim.toLowerCase
    if (srcLowerCase.endsWith(".sass") || srcLowerCase.endsWith(".scss")) {
      SassSource(this, src.getCanonicalFile)
    } else {
      CssSource(this, src.getCanonicalFile)
    }
  }

  def srcFilenameToDesFilename(filename: String) =
    filename.replaceAll("[.]sass$|[.]scss$", ".css")

  val pluginName = "sbt-sass"

  override def dump(): Unit = {
    log.debug("Graph for " + pluginName + ":")

    log.debug("  sassVersion:")
    log.debug("    " + sassVersion.filename)

    log.debug("  prettyPrint:")
    log.debug("    " + prettyPrint)

    log.debug("  templateProperties:")
    log.debug("    " + templateProperties)

    log.debug("  downloadDir:")
    log.debug("    " + downloadDir)

    sources.foreach(dumpSource)
  }

}