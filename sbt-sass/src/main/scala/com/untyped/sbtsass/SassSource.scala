package com.untyped.sbtsass

import java.nio.charset.Charset
import sbt._
import scala.collection._
import scala.sys.process.Process

object SassSource {

  val importRegex = """^[ \t]*@import "([^"]+)";.*$""".r

  def parseImport(line: String): Option[String] =
    importRegex.findAllIn(line).matchData.map(_.group(1)).toList.headOption

  val compileFunction: String = ""
}

/**
 * Sass CSS compiler.
 */
case class SassSource(graph: Graph, src: File) extends Source {

  lazy val parents: List[Source] =
    for {
      line <- IO.readLines(src).map(_.trim).toList
      name <- SassSource.parseImport(line)
    } yield {
      val srcFileEnding = src.getName.split("\\.").reverse.head
      val sassImport = "_" + name + "." + srcFileEnding
      graph.getSource(sassImport, this)
    }

  def isTemplated: Boolean =
    src.toString.contains(".template")

  def compile: Option[File] = {
    val des = this.des getOrElse (throw new Exception("Could not determine destination filename for " + src))

    graph.log.info("Compiling %s source %s".format(graph.pluginName, des))

    val sass =
      if (isTemplated) {
        renderTemplate(completeRawSource)
      } else {
        completeRawSource
      }

//    if(graph.useCommandLine) {
//      val temp = java.io.File.createTempFile(src.getName, ".sass")
//      val out = new java.io.PrintWriter(new java.io.FileWriter(temp))
//      out.println(sass)
//      out.close()
//
//      Process(Seq("lessc", temp.getCanonicalPath, des.getCanonicalPath)).! match {
//        case 0 => Some(des)
//        case n => sys.error("Could not compile %s source %s".format(graph.pluginName, des))
//      }
//    } else {
//      val minify = !graph.prettyPrint
//
//      withContext { ctx =>
//        try {
//          val scope =
//            graph.sassVersion match {
//              case Plugin.SassVersion.Sass3124 => less130Scope(ctx)
//              case _                          => earlyLessScope(ctx)
//            }
//
//          val sassCompiler =
//            scope.get("compile", scope).asInstanceOf[Callable]
//
//          val css =
//            sassCompiler.call(
//              ctx,
//              scope,
//              scope,
//              Array(src.getPath, sass, minify.asInstanceOf[AnyRef])
//            ).toString
//
//          IO.write(des, css)
//          Some(des)
//        } catch {
//          case e: JavaScriptException =>
//            val error   = e.getValue.asInstanceOf[Scriptable]
//            val line    = ScriptableObject.getProperty(error, "line"   ).asInstanceOf[Double].intValue
//            val column  = ScriptableObject.getProperty(error, "column" ).asInstanceOf[Double].intValue
//            val message = ScriptableObject.getProperty(error, "message").asInstanceOf[String]
//            sys.error("%s error: %s [%s,%s]: %s".format(graph.pluginName, src.getName, line, column, message))
//        }
//      }
//    }
    Some(des)
  }

//  private def less140Scope(ctx: Context) =
//    less130Scope(ctx)
//
//  private def less130Scope(ctx: Context) = {
//    val global = new Global()
//    global.init(ctx)
//
//    val scope = ctx.initStandardObjects(global)
//
//    ctx.evaluateReader(
//      scope,
//      new java.io.InputStreamReader(getClass.getResourceAsStream(graph.lessVersion.envjsUrl), Charset.forName("utf-8")),
//      graph.lessVersion.envjsFilename,
//      1,
//      null)
//
//    ctx.evaluateReader(
//      scope,
//      new java.io.InputStreamReader(getClass.getResourceAsStream(graph.lessVersion.url), Charset.forName("utf-8")),
//      graph.lessVersion.filename,
//      1,
//      null)
//
//    ctx.evaluateString(
//      scope,
//      SassSource.compileFunction,
//      "compile.js",
//      1,
//      null)
//
//    scope
//  }

//  private def earlyLessScope(ctx: Context) = {
//    val scope = ShellEmulation.emulate(ctx.initStandardObjects())
//
//    ctx.evaluateReader(
//      scope,
//      new java.io.InputStreamReader(getClass.getResourceAsStream(graph.lessVersion.url), Charset.forName("utf-8")),
//      graph.lessVersion.filename,
//      1,
//      null)
//
//    scope
//  }
//
//  private def withContext[T](f: Context => T): T = {
//    val ctx = Context.enter()
//    try {
//      ctx.setOptimizationLevel(-1) // Do not compile to byte code (max 64kb methods)
//      ctx.setLanguageVersion(Context.VERSION_1_7)
//      f(ctx)
//    } finally {
//      Context.exit()
//    }
//  }

  override def toString =
    "SassSource(" + src + ")"
}
