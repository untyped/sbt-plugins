package untyped.js

import com.google.javascript.jscomp._
import java.util.Properties
import sbt._
import scala.collection._

case class Graph(
    val log: Logger,
    val sourceDir: File,
    val targetDir: File,
    val templateProperties: Properties,
    val downloadDir: File,
    val compilerOptions: CompilerOptions
  ) extends untyped.graph.Graph {
  
  type S = untyped.js.Source
  
  override def createSource(src: File): Source =
    if(src.toString.trim.toLowerCase.endsWith(".jsm")) {
      JsmSource(this, src.getCanonicalFile, srcToDes(src).getCanonicalFile)
    } else {
      JsSource(this, src.getCanonicalFile, srcToDes(src).getCanonicalFile)
    }

  def srcFilenameToDesFilename(filename: String) =
    filename.replaceAll("[.]jsm(anifest)?$", ".js")

  val pluginName = "sbt-js"
  
  def closureLogLevel: java.util.logging.Level =
    java.util.logging.Level.OFF

  def closureExterns(a: Source): List[JSSourceFile] =
    (a :: ancestors(a)).reverse.flatMap(_.closureExterns)

  def closureSources(a: Source): List[JSSourceFile] =
    (a :: ancestors(a)).reverse.flatMap(_.closureSources)

}
