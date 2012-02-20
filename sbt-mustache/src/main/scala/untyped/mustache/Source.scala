package untyped.mustache

import sbt._
import scala.collection._

object JsSource {

  val requireRegex = """[{][:][>][ \t]*([^:]+)[ \t]*[:][}]""".trim.r

  def parseRequire(line: String): Option[String] =
    requireRegex.findAllIn(line).matchData.map(data => data.group(1)).toList.headOption.map(_.trim)

}

case class Source(val graph: Graph, val src: File) extends untyped.graph.Source {

  type S = untyped.mustache.Source
  type G = untyped.mustache.Graph

  def isTemplated = true

  lazy val parents: List[Source] =
    for {
      line <- IO.readLines(src).map(_.trim).toList
      name <- JsSource.parseRequire(line)
    } yield graph.getSource(name, this)

  def compiledContent: String =
    JsSource.requireRegex.replaceAllIn(
      IO.read(src),
      data => graph.getSource(data.group(1).trim, this).compiledContent
    )

  def compile: Option[File] =
    des map { des =>
      graph.log.info("Compiling %s source %s".format(graph.pluginName, des))
      IO.write(des, compiledContent)
      des
    }

}
