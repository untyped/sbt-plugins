package com.untyped.sbtless

import org.mozilla.javascript.{ Callable, Context, Function, FunctionObject, JavaScriptException, NativeArray, NativeObject, Scriptable, ScriptableObject }
import org.mozilla.javascript.tools.shell.{ Environment, Global }
import java.nio.charset.Charset
import org.mozilla.javascript._
import sbt._
import scala.collection._
import scala.sys.process.Process

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

  val compileFunction: String =
    """
    |function compile(scriptName, code, min) {
    |  name = scriptName;
    |  var css = null;
    |  new less.Parser().parse(code, function (e, root) {
    |    if(e) { throw e; }
    |    css = root.toCSS({ compress: min || false })
    |  });
    |  return css;
    |}
    """.trim.stripMargin
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

  def compile: Option[File] = {
    val des = this.des getOrElse (throw new Exception("Could not determine destination filename for " + src))

    graph.log.info("Compiling %s source %s".format(graph.pluginName, des))

    val less =
      if (isTemplated) {
        renderTemplate(completeRawSource)
      } else {
        completeRawSource
      }

    if(graph.useCommandLine) {
      val temp = java.io.File.createTempFile(src.getName, ".less")
      val out = new java.io.PrintWriter(new java.io.FileWriter(temp))
      out.println(less)
      out.close()

      (Process(Seq("lessc", temp.getCanonicalPath, des.getCanonicalPath)) !) match {
        case 0 => Some(des)
        case n => sys.error("Could not compile %s source %s".format(graph.pluginName, des))
      }
    } else {
      val minify = !graph.prettyPrint

      withContext { ctx =>
        try {
          val scope =
            graph.lessVersion match {
              case Plugin.LessVersion.Less130 => less130Scope(ctx)
	          case Plugin.LessVersion.Less133 => less130Scope(ctx)
              case _                          => earlyLessScope(ctx)
            }

          val lessCompiler =
            scope.get("compile", scope).asInstanceOf[Callable]

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
          case e: JavaScriptException =>
            val error   = e.getValue.asInstanceOf[Scriptable]
            val line    = ScriptableObject.getProperty(error, "line"   ).asInstanceOf[Double].intValue
            val column  = ScriptableObject.getProperty(error, "column" ).asInstanceOf[Double].intValue
            val message = ScriptableObject.getProperty(error, "message").asInstanceOf[String]
            sys.error("%s error: %s [%s,%s]: %s".format(graph.pluginName, src.getName, line, column, message))
        }
      }
    }
  }

  private def less130Scope(ctx: Context) = {
    val global = new Global()
    global.init(ctx)

    val scope = ctx.initStandardObjects(global)

    ctx.evaluateReader(
      scope,
      new java.io.InputStreamReader(getClass().getResourceAsStream(graph.lessVersion.envjsUrl), Charset.forName("utf-8")),
      graph.lessVersion.envjsFilename,
      1,
      null)

    ctx.evaluateReader(
      scope,
      new java.io.InputStreamReader(getClass().getResourceAsStream(graph.lessVersion.url), Charset.forName("utf-8")),
      graph.lessVersion.filename,
      1,
      null)

    ctx.evaluateString(
      scope,
      LessSource.compileFunction,
      "compile.js",
      1,
      null)

    scope
  }

  private def earlyLessScope(ctx: Context) = {
    val scope = ShellEmulation.emulate(ctx.initStandardObjects())

    ctx.evaluateReader(
      scope,
      new java.io.InputStreamReader(getClass().getResourceAsStream(graph.lessVersion.url), Charset.forName("utf-8")),
      graph.lessVersion.filename,
      1,
      null)

    scope
  }

  private def withContext[T](f: Context => T): T = {
    val ctx = Context.enter()
    try {
      ctx.setOptimizationLevel(-1) // Do not compile to byte code (max 64kb methods)
      ctx.setLanguageVersion(Context.VERSION_1_7)
      f(ctx)
    } finally {
      Context.exit()
    }
  }

  override def toString =
    "LessSource(" + src + ")"
}
