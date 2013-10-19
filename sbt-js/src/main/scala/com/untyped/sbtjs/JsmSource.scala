package com.untyped.sbtjs

import com.google.javascript.jscomp.{ SourceFile => ClosureSource }
import sbt._

case class JsmSource(graph: Graph, src: File) extends Source {

  lazy val parents: List[Source] =
    for {
      line <- lines.map(stripComments).filterNot(isSkippable)
    } yield graph.getSource(line, this)

  def isTemplated = false

  /** Closure sources for this file (not its parents). */
  def closureSources: List[ClosureSource] =
    Nil

  // Helpers ------------------------------------

  /** Strip JSM comments (that start with a # symbol). */
  def stripComments(line: String): String =
    "#.*$".r.replaceAllIn(line, "").trim

  /** Is this line skippable - i.e. does it not contain content? Assumes comments have been stripped. */
  def isSkippable(line: String): Boolean =
    line == ""

  /** Is this line a URL? Assumes comments have been stripped. */
  def isUrl(line: String): Boolean =
    line.matches("^https?:.*")

  override def toString =
    "JsmSource(" + src + ")"

}
