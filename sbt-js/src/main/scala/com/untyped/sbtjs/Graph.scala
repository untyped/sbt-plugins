package com.untyped.sbtjs

import com.google.javascript.jscomp._
import java.util.Properties
import sbt._
import scala.collection._

case class Graph(
    val log: Logger,
    val sourceDirs: Seq[File],
    val targetDir: File,
    val templateProperties: Properties,
    val downloadDir: File,
    val compilerOptions: CompilerOptions
  ) extends com.untyped.sbtgraph.Graph {

  type S = com.untyped.sbtjs.Source

  override def createSource(src: File): Source =
    if(src.toString.trim.toLowerCase.endsWith(".jsm")) {
      JsmSource(this, src.getCanonicalFile)
    } else if(src.toString.trim.toLowerCase.endsWith(".coffee")) {
      CoffeeSource(this, src.getCanonicalFile)
    } else {
      JsSource(this, src.getCanonicalFile)
    }

  def srcFilenameToDesFilename(filename: String) =
    filename.replaceAll("[.](js|jsm|jsmanifest|coffee)$", ".js")

  val pluginName = "sbt-js"

  def closureLogLevel: java.util.logging.Level =
    java.util.logging.Level.OFF

  def closureExterns(a: Source): List[JSSourceFile] =
    ancestors(a).flatMap(_.closureExterns)

  def closureSources(a: Source): List[JSSourceFile] =
    ancestors(a).flatMap(_.closureSources)

}
