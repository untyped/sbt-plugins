package untyped
package less

import java.io.InputStreamReader
import java.nio.charset.Charset
import org.mozilla.javascript._
import sbt._
import scala.collection._

object LessSource {

  val importRegex = """^@import "([^"]+)";$""".r

  def parseImport(line: String): Option[String] =
    importRegex.findAllIn(line).matchData.map(_.group(1)).toList.headOption

}

case class LessSource(val graph: Graph, val src: File, val des: File) extends Source {
  
  lazy val parents: List[Source] =
    for {
      line <- IO.readLines(src).map(_.trim).toList
      name <- LessSource.parseImport(line)
    } yield graph.getSource(name, this)

  def isTemplated: Boolean =
    src.toString.contains(".template")
  
  def compile: Option[File] =
    withContext { ctx =>
      graph.log.info("Compiling %s source %s".format(graph.pluginName, des))

      val scope = ctx.initStandardObjects()
      
      ctx.evaluateReader(
        scope,
        new InputStreamReader(getClass().getResourceAsStream("/less-rhino-1.1.3.js"), Charset.forName("utf-8")),
        "less-rhino-1.1.3.js",
        1,
        null)
  
      val lessCompiler = scope.get("compile", scope).asInstanceOf[Callable]

      try {
        val less =
          if(isTemplated) {
            renderTemplate(IO.read(src))
          } else {
            IO.read(src)
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
            case value: Scriptable => graph.log.error(ScriptableObject.getProperty(value, "message").toString)
            case value             => graph.log.error("Unknown exception compiling Less CSS: " + value)
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
    "LessSource(" + src + ", " + des + ")"
  
}
