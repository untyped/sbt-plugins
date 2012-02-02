package untyped
package less

import sbt._
import scala.collection._

object CssSource {

  val importRegex = """^@import "([^"]+)";$""".r

  def parseImport(line: String): Option[String] =
    importRegex.findAllIn(line).matchData.map(_.group(1)).toList.headOption

}

case class CssSource(val graph: Graph, val src: File, val des: File) extends Source {
    
  lazy val parents: List[Source] =
    for {
      line <- IO.readLines(src).map(_.trim).toList
      name <- CssSource.parseImport(line)
    } yield graph.getSource(name, this)

  def isTemplated: Boolean =
    src.toString.contains(".template")
  
  def compile: Option[File] = {
    graph.log.info("Copying %s source %s".format(graph.pluginName, des))

    IO.copy(List((src, des)))
    Some(des)
  }
  
  override def toString =
    "CssSource(" + src + ", " + des + ")"
  
}
