package untyped.js

import com.google.javascript.jscomp.JSSourceFile
import sbt._
import scala.collection._
import org.jcoffeescript.{JCoffeeScriptCompiler, JCoffeeScriptCompileException}

object CoffeeSource {

  val requireRegex =
    """
    #[ \t]*require[ \t]*"([^"]+)"
    """.trim.r

  def parseRequire(line: String): Option[String] =
    requireRegex.findAllIn(line).matchData.map(data => data.group(1)).toList.headOption

}

case class CoffeeSource(val graph: Graph, val src: File) extends Source {

  lazy val parents: List[Source] =
    for {
      line <- IO.readLines(src).map(_.trim).toList
      name <- CoffeeSource.parseRequire(line)
    } yield graph.getSource(name, this)

  def coffeeCompile(in: String): String =
    try {
      (new JCoffeeScriptCompiler).compile(in)
    } catch {
      case exn: JCoffeeScriptCompileException =>
        sys.error("Error compiling Coffeescript: " + this.src + ": " + exn.getMessage)
    }


  /** Closure sources for this file (not its parents). */
  def closureSources: List[JSSourceFile] =
    if(this.isTemplated) {
      List(JSSourceFile.fromCode(src.toString, coffeeCompile(renderTemplate(src))))
    } else {
      List(JSSourceFile.fromCode(src.toString, coffeeCompile(IO.read(src))))
    }

  /** Is the source file templated? It's templated if the file name contains ".template", e.g. "foo.template.js" */
  def isTemplated: Boolean =
    src.toString.contains(".template")

  override def toString =
    "CoffeeSource(" + src + ")"

}
