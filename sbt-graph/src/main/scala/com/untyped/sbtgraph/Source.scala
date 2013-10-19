package com.untyped.sbtgraph

import com.samskivert.mustache.Mustache
import sbt._

object Source {

  /**
   * By default the JMustache implementation will treat
   * variables named like.this as a two part name and look
   * for a variable called this within one called like
   * (called compound variables in the docs). This breaks
   * things with the default naming conventions for
   * Java/Lift properties so we turn it off.
   */
  lazy val mustacheCompiler =
    Mustache.compiler().standardsMode(true)

}

trait Source {

  type S <: Source
  type G <: Graph { type S = Source.this.S }

  def graph: G

  def src: File

  /**
   * Destination filename, or None if src filename
   * cannot be translated to destination filename.
   *
   * Failure to translate normally occurs if src
   * filename is outside srcDirectory.
   */
  final def des: Option[File] =
    graph.srcToDes(src)

  lazy val srcDirectory: File =
    src.getParentFile

  /** Lines in the source file. */
  def lines: List[String] =
    IO.readLines(src).map(_.trim).toList

  /** Files that the source file depends on. */
  def parents: List[G#S]

  def isTemplated: Boolean

  def requiresRecompilation: Boolean = {
    des exists {
      des =>
        requiresRecompilationFor(des)
    }
  }

  def requiresRecompilationFor(des: File): Boolean = {
    !des.exists ||
    (src newerThan des) ||
    isTemplated ||
    parents.exists(_.requiresRecompilationFor(des))
  }

  def compile: Option[File]

  def clean(): Unit =
    des foreach { des =>
      graph.log.info("Cleaning %s source %s".format(graph.pluginName, des))
      IO.delete(des)
    }

  // Templating and properties ------------------

  def renderTemplate(src: File): String =
    renderTemplate(IO.read(src))

  def renderTemplate(src: String): String =
    Source.mustacheCompiler.compile(src).execute(graph.templateProperties)

}
