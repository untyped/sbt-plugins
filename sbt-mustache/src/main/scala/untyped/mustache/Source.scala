package untyped.mustache

import sbt._
import scala.collection._

object JsSource {

  val requireRegex = """[{][:][>][ \t]*([^:]+)[ \t]*[:][}]""".trim.r

  def parseRequire(line: String): Option[String] = {
    println("=======<" + line + ">========")

    val ans =
      requireRegex.findAllIn(line).matchData.map(data => data.group(1)).toList.headOption.map(_.trim)

    println("=======[" + ans + "]========")

    ans
  }

}

case class Source(val graph: Graph, val src: File) extends untyped.graph.Source {

  type S = untyped.mustache.Source
  type G = untyped.mustache.Graph

  def isTemplated = true

  lazy val parents: List[Source] = {
    graph.log.debug("Source.parents " + src)
    val ans =
      for {
        line <- IO.readLines(src).map(_.trim).toList
        name <- JsSource.parseRequire(line)
      } yield graph.getSource(name, this)
    graph.log.debug(" --> " + ans)
    ans
  }

  def compiledContent: String = {
    graph.log.debug("Source.compiledContent [" + src + "]")
    graph.log.debug(" ... exists " + src.exists)

    val ans =
      JsSource.requireRegex.replaceAllIn(
        IO.read(src),
        data => {
          graph.log.debug(" ... match " + data.group(1))
          graph.getSource(data.group(1).trim, this).compiledContent
        })

    graph.log.debug(" --> " + ans)

    ans
  }

  def compile: Option[File] =
    des map { des =>
      graph.log.debug("Source.compile " + src + " " + des)
      val content = compiledContent
      graph.log.debug(" ... writing " + content)
      IO.write(des, content)
      des
    }

}
