package com.untyped.sbtjs

import com.google.javascript.jscomp.{
  SourceFile => ClosureSource,
  CompilerOptions => ClosureOptions
}
import java.util.Properties
import org.jcoffeescript.{ Option => CoffeeOption }
import sbt._

case class Graph(
  log: Logger,
  sourceDirs: Seq[File],
  targetDir: File,
  templateProperties: Properties,
  downloadDir: File,
  filenameSuffix: String,
  coffeeVersion: Plugin.CoffeeVersion,
  coffeeOptions: List[CoffeeOption] = List(CoffeeOption.BARE),
  closureOptions: ClosureOptions
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
    filename.replaceAll("[.](js|jsm|jsmanifest|coffee)$", filenameSuffix+".js")

  val pluginName = "sbt-js"

  def closureLogLevel: java.util.logging.Level =
    java.util.logging.Level.OFF

  def closureExterns(a: Source): List[ClosureSource] =
    ancestors(a).flatMap(_.closureExterns)

  def closureSources(a: Source): List[ClosureSource] =
    ancestors(a).flatMap(_.closureSources)

}
