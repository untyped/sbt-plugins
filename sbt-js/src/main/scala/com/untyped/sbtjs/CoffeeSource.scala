package com.untyped.sbtjs

import com.google.javascript.jscomp.{ SourceFile => ClosureSource }
import sbt._
import org.jcoffeescript.{JCoffeeScriptCompiler, JCoffeeScriptCompileException}

object CoffeeSource {

  val requireRegex =
    """
    #[ \t]*require[ \t]*"([^"]+)"
    """.trim.r

  def parseRequire(line: String): Option[String] =
    requireRegex.findAllIn(line).matchData.map(data => data.group(1)).toList.headOption

}

case class CoffeeSource(graph: Graph, src: File) extends Source {
  lazy val parents: List[Source] =
    for {
      line <- IO.readLines(src).map(_.trim).toList
      name <- CoffeeSource.parseRequire(line)
    } yield graph.getSource(name, this)

  def coffeeCompile(in: String): String =
    try {
      import scala.collection.JavaConversions._
      graph.log.debug("Compiling %s with Coffeescript %s and options %s".format(src, graph.coffeeVersion.url, graph.coffeeOptions))
      new JCoffeeScriptCompiler(graph.coffeeVersion.url, graph.coffeeOptions).compile(in)
    } catch {
      case exn: JCoffeeScriptCompileException =>
        sys.error("Error compiling Coffeescript: " + this.src + ": " + exn.getMessage)
    }

  /** Closure sources for this file (not its parents). */
  def closureSources: List[ClosureSource] =
    if(this.isTemplated) {
      List(ClosureSource.fromCode(src.toString, coffeeCompile(renderTemplate(src))))
    } else {
      List(ClosureSource.fromCode(src.toString, coffeeCompile(IO.read(src))))
    }

  /** Is the source file templated? It's templated if the file name contains ".template", e.g. "foo.template.js" */
  def isTemplated: Boolean =
    src.toString.contains(".template")

  override def toString =
    "CoffeeSource(" + src + ")"

}
