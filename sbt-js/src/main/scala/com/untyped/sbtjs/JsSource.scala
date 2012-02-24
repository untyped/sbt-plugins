package com.untyped.sbtjs

import com.google.javascript.jscomp.JSSourceFile
import sbt._
import scala.collection._

object JsSource {

  val requireRegex =
    """
    //[ \t]*require[ \t]*"([^"]+)"
    """.trim.r

  def parseRequire(line: String): Option[String] =
    requireRegex.findAllIn(line).matchData.map(data => data.group(1)).toList.headOption

}

case class JsSource(val graph: Graph, val src: File) extends Source {
  
  lazy val parents: List[Source] =
    for {
      line <- IO.readLines(src).map(_.trim).toList
      name <- JsSource.parseRequire(line)
    } yield graph.getSource(name, this)
  
  /** Closure sources for this file (not its parents). */
  def closureSources: List[JSSourceFile] =
    if(this.isTemplated) {
      List(JSSourceFile.fromCode(src.toString, renderTemplate(src)))
    } else {
      List(JSSourceFile.fromFile(src))
    }
  
  /** Is the source file templated? It's templated if the file name contains ".template", e.g. "foo.template.js" */
  def isTemplated: Boolean =
    src.toString.contains(".template")
  
  override def toString =
    "JsSource(" + src + ")"
  
}
