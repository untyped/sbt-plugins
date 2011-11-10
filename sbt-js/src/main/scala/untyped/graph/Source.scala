package untyped.graph

import com.samskivert.mustache.{Mustache,Template}
import java.util.Properties
import sbt._
import scala.collection._

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
  def des: File

  lazy val srcDirectory: File =
    src.getParentFile

  /** Lines in the source file. */
  def lines: List[String] =
    IO.readLines(src).map(_.trim).toList
  
  /** Files that the source file depends on. */
  def parents: List[G#S]
  
  def isTemplated: Boolean
  
  def requiresRecompilation: Boolean =
    !des.exists ||
    (src newerThan des) ||
    (isTemplated && graph.props.file.map(_ newerThan this.des).getOrElse(false)) ||
    parents.exists(_.requiresRecompilation)
  
  def compile: Option[File]
  
  def clean: Unit = {
    graph.log.info("Cleaning %s source %s".format(graph.pluginName, des))
    IO.delete(des)
  }
  
  // Templating and properties ------------------
  
  def renderTemplate(src: File): String =
    renderTemplate(IO.read(src))

  def renderTemplate(src: String): String =
    Source.mustacheCompiler.compile(src).execute(attributes)

  /** Instantiate the properties used for Mustache templating */
  def attributes: Properties = {
    val props = new Props(graph.propertiesDir)
    props.properties.getOrElse {
      graph.log.warn("sbt-js: no properties file found in search path: " + props.searchPaths)
      new Properties
    }
  }

}
