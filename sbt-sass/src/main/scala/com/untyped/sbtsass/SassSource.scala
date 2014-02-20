package com.untyped.sbtsass

import sbt._
import scala.sys.process.Process
import org.jruby.embed.{LocalVariableBehavior, LocalContextScope, ScriptingContainer}
import org.jruby.exceptions.RaiseException
import sbt.File
import scala.util.parsing.combinator.RegexParsers

object SassSource {

  val importNameRegex = """["']{1}([^"']+)["']{1}""".r
  val onlinerImportRegex = """(^[ \t]*@import.*;.*$)""".r
  val multilineImportRegex = """(^[ \t]*@import[^;]*$)""".r
  val semicolonRegex = """(.*;)$""".r

  def parseImport(line: String): List[String] = {
    if(onlinerImportRegex.pattern.matcher(line).matches()) {
      (for {
        m <- importNameRegex.findAllIn(line).matchData
        e <- m.subgroups
      } yield {m.group(1)}).toList
    } else List.empty
  }

  object ImportParser extends RegexParsers {

    trait Parsed
    case class Import(value: String) extends Parsed
    case object Ignore extends Parsed

    def importStatement: Parser[String] = "^@import".r
    def singleQuoteImport = "'" ~> """[^']+""".r <~ "'" ^^ Import
    def doubleQuoteImport = "\"" ~> """[^"]+""".r <~ "\"" ^^ Import
    def importPart: Parser[Import] = singleQuoteImport | doubleQuoteImport

    lazy val ignore: Parser[List[Parsed]] = """\S+""".r ^^ (_ => Ignore :: Nil)
    def importParts: Parser[List[Import]] = importStatement ~> rep1sep(importPart, ",") <~ ";"
    def imports: Parser[List[Import]] = rep1(importParts ||| ignore) ^^ (i => {i.flatten.collect{case i: Import => i}})

    def parseImports(fileContent: String, filePath: String) = {
      val result = parseAll(imports, fileContent)
      try {
        result.get.map(_.value)
      } catch {
        case t: Throwable =>
          throw new Exception("Had problems parsing imports from: %s \n%s".format(filePath, result.toString), t)
      }
    }
  }

  def parseImportsFromFile(src: File): List[String] = {
    val fileContent = IO.readLines(src).map(_.trim).mkString("\n")
    ImportParser.parseImports(fileContent, src.getAbsolutePath)
  }

}

/**
 * Sass CSS compiler.
 */
case class SassSource(graph: Graph, src: File) extends Source {

  val srcFileEnding = src.getName.split("\\.").reverse.head

  def regularOrPartialImport(importName: String): Option[String] = {
    def fileExists(in: String) = new File(this.srcDirectory, in).exists()
    def asRegularScss(in: String ) = in + "." + srcFileEnding
    def asPartialScss(in: String) = {
      asRegularScss((in.split("/").toList.reverse match {
        case head :: tail => ("_"+head) :: tail
        case Nil => Nil
      }).reverse.mkString("/"))
    }

    if (fileExists(importName))
      Some(importName)
    else if (fileExists(asPartialScss(importName)))
      Some(asPartialScss(importName))
    else if (fileExists(asRegularScss(importName)))
      Some(asRegularScss(importName))
    else
      None
  }

  lazy val parents: List[Source] = {
    for {
      importName <- SassSource.parseImportsFromFile(src)
      ropi <- regularOrPartialImport(importName)
    } yield {
        graph.getSource(ropi, this)
    }
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

    if(graph.useCommandLine) {
      val temp = java.io.File.createTempFile(src.getName, srcFileEnding)
      val out = new java.io.PrintWriter(new java.io.FileWriter(temp))
      out.println(sass)
      out.close()

      // This will try to call the sass command.
      // Sass is a gem that could be installed with `gem install sass`
      Process(Seq("sass", temp.getCanonicalPath, des.getCanonicalPath)).! match {
        case 0 => Some(des)
        case n => sys.error("Could not compile %s source %s".format(graph.pluginName, des))
      }
    } else {
      import SassCompiler._

      handleException(requireSassGem(graph.sassVersion.version))


      val syntaxOptions = Map(":syntax" -> (":"+srcFileEnding))
      val css = handleException(renderCssFromScssFile(src, syntaxOptions))
      IO.write(des, css)
      Some(des)
    }
  }

  override def toString =
    "SassSource(" + src + ")"

  private def handleException[T](func: => T) = try {
    func
  } catch {
    case e: Throwable => e.getCause match {
      case raiseEx: RaiseException =>
        sys.error("%s error: %s JRuby exception: %s".format(graph.pluginName, src.getName, raiseEx.getStackTraceString))
      case _ => throw e
    }
  }
}

object SassCompiler {

  def requireSassGem(version: String) {
    require("sass-"+version+"/lib/sass.rb")
  }

  def renderCssFromSassString(sass: String, options: Map[String, String]) = {
    val sassEngine = createSassStringEngine(sass, options)
    callRender(sassEngine)
  }

  def renderCssFromScssFile(file: File, options: Map[String, String]) = {
    val sassEngine = createSassFileEngine(file, options)
    callRender(sassEngine)
  }

  // JRuby interaction code inspired by https://github.com/mcamou/scuby which we sadly couldn't use
  // due to cross compiling issues

  import org.jruby.RubyObject

  // Need this to avoid: org.jruby.exceptions.RaiseException: (LoadError) no such file to load -- jruby/java
  // Don't ask why, found the trick here: http://stackoverflow.com/questions/20479495/loaderror-no-such-file-to-load-jruby-java
  val ruby = {
    val r = new ScriptingContainer(LocalContextScope.SINGLETON, LocalVariableBehavior.TRANSIENT)
    r.setClassLoader(r.getClass.getClassLoader)
    r
  }

  private def createSassStringEngine(sass: String, options: Map[String, String]) = {
    verifyRubyObj(callRuby(evalRuby("Sass::Engine"), "new", Seq(sass, evalOptions(options))))
  }

  private def createSassFileEngine(file: File, options: Map[String, String]) = {
    verifyRubyObj(callRuby(evalRuby("Sass::Engine"), "for_file", Seq(file.getAbsolutePath, evalOptions(options))))
  }

  private def require(file: String) = {
    verifyBoolean(ruby.runScriptlet("require '%s'".format(file)))
  }

  private def callRender(engine: RubyObject): String = {
    verifyString(callRuby(engine, "render", Seq.empty))
  }

  private def callRuby(obj: RubyObject, name: String, args: Seq[_ <: AnyRef]) = {
    ruby.callMethod(obj, name, args:_*)
  }

  private def evalOptions(options: Map[String, String]) = {
    val optionElements = options.map{case (k,v) => k+" => "+v}.mkString(", ")
    evalRuby("{"+optionElements+"}")
  }

  private def evalRuby(expression: String): RubyObject = {
    verifyRubyObj(ruby.runScriptlet(expression))
  }

  private def verifyRubyObj(obj: Any): RubyObject = {
    obj match {
      case null => null
      case obj: RubyObject => obj
      case _ => throw new IllegalTypeConversion(obj.getClass, classOf[RubyObject])
    }
  }

  private def verifyString(obj: Any): String = {
    obj match {
      case s: String => s
      case _ => throw new IllegalTypeConversion(obj.getClass, classOf[String])
    }
  }

  private def verifyBoolean(obj: Any): Boolean = {
    obj match {
      case s: Boolean => s
      case _ => throw new IllegalTypeConversion(obj.getClass, classOf[Boolean])
    }
  }

  class IllegalTypeConversion(from: Class[_], to: Class[_])
    extends RuntimeException("Illegal type conversion. Ruby returned a %s, call was expecting a %s".format(from.getName, to.getName))

  class UnwrappedCallException(method: String)
    extends RuntimeException("If you expect to get back a RubyObj/RubyObject, use the specialized method "+method)

}
