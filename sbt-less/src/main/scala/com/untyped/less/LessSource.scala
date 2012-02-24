package com.untyped
package less

import org.mozilla.javascript.{ Callable, Context, Function, FunctionObject, JavaScriptException, NativeArray, NativeObject, Scriptable, ScriptableObject }
import org.mozilla.javascript.tools.shell.{ Environment, Global }
import java.io.InputStreamReader
import java.nio.charset.Charset
import org.mozilla.javascript._
import sbt._
import scala.collection._

/**
 * Stub out a basic environment for Rhino that emulates running it on the command line.
 *
 * This code was stolen mostly intact from less-sbt:
 *   https://github.com/softprops/less-sbt/blob/master/src/main/scala/compiler.scala
 */
object ShellEmulation {
  /** common functions the rhino shell defines */
  val ShellFunctions = Array(
    "doctest",
    "gc",
    "load",
    "loadClass",
    "print",
    "quit",
    "readFile",
    "readUrl",
    "runCommand",
    "seal",
    "sync",
    "toint32",
    "version")

  /** Most `rhino friendly` js libraries make liberal use
   *  if non emca script properties and functions that the
   *  rhino shell env defines. Unfortunately we are not
   *  evaluating these sources in a rhino shell.
   *  instead of crying me a river, provide an interface
   *  that enables emulation of the shell env */
  def emulate(s: ScriptableObject) = {
    // define rhino shell functions
    s.defineFunctionProperties(ShellFunctions, classOf[Global], ScriptableObject.DONTENUM)
    // make rhino `detectable` - http://github.com/ringo/ringojs/issues/#issue/88
    Environment.defineClass(s)
    s.defineProperty("environment", new Environment(s), ScriptableObject.DONTENUM)
    s
  }
}

object LessSource {

  val importRegex = """^[ \t]*@import "([^"]+)";.*$""".r

  def parseImport(line: String): Option[String] =
    importRegex.findAllIn(line).matchData.map(_.group(1)).toList.headOption

}

/**
 * Less CSS compiler.
 *
 * This code was adapted from less-sbt:
 *   https://github.com/softprops/less-sbt/blob/master/src/main/scala/compiler.scala
 */
case class LessSource(val graph: Graph, val src: File) extends Source {

  lazy val parents: List[Source] =
    for {
      line <- IO.readLines(src).map(_.trim).toList
      name <- LessSource.parseImport(line)
    } yield graph.getSource(name, this)

  def isTemplated: Boolean =
    src.toString.contains(".template")

  def compile: Option[File] =
    withContext { ctx =>
      val des = this.des getOrElse (throw new Exception("Could not determine destination filename for " + src))

      graph.log.info("Compiling %s source %s".format(graph.pluginName, des))

      val scope = ShellEmulation.emulate(ctx.initStandardObjects())

      ctx.evaluateReader(
        scope,
        new InputStreamReader(getClass().getResourceAsStream(graph.lessVersion.url), Charset.forName("utf-8")),
        graph.lessVersion.filename,
        1,
        null)

      val lessCompiler = scope.get("compile", scope).asInstanceOf[Callable]

      try {
        val less =
          if(isTemplated) {
            renderTemplate(completeRawSource)
          } else {
            completeRawSource
          }

        val minify =
          !graph.prettyPrint

        val css =
          lessCompiler.call(
            ctx,
            scope,
            scope,
            Array(src.getPath, less, minify.asInstanceOf[AnyRef])
          ).toString

        IO.write(des, css)
        Some(des)
      } catch {
        case e : JavaScriptException =>
          e.getValue match {
            case value: Scriptable =>
              graph.log.error("Less CSS error: " + ScriptableObject.getProperty(value, "message").toString)
              graph.log.error("Stack trace: " + ScriptableObject.getProperty(value, "stack").toString)

            case value =>
              graph.log.error("Unknown exception compiling Less CSS: " + value)
          }

          None
      }
    }

  private def withContext[T](f: Context => T): T = {
    val ctx = Context.enter()
    try {
      ctx.setOptimizationLevel(-1) // Do not compile to byte code (max 64kb methods)
      f(ctx)
    } finally {
      Context.exit()
    }
  }

  override def toString =
    "LessSource(" + src + ")"
}
