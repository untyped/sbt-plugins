package com.untyped.sbttipi

// Need to be specific here because sbt.Doc conflicts with tipi.core.Doc:
import sbt.{ File, IO, file, singleFileFinder, globFilter }
import scala.collection._
import scala.util.parsing.combinator._
import scala.util.parsing.input._
import tipi.core._
import tipi.core.Implicits._

case class Source(val graph: Graph, val src: File) extends com.untyped.sbtgraph.Source with tipi.core.Implicits {

  type S = com.untyped.sbttipi.Source
  type G = com.untyped.sbttipi.Graph

  def isTemplated = true

  lazy val env: Env =
    graph.environment ++ ImportEnv

  lazy val doc: Doc = {
    val source = io.Source.fromFile(src)
    val input = new CharSequenceReader(source.mkString)

    try {
      graph.parse(input) match {
        case graph.parse.Success(doc, _) => doc
        case err: graph.parse.NoSuccess =>
          sys.error("%s [%s,%s]: %s".format(src.toString, input.pos.line, input.pos.column, err.msg))
      }
    } finally {
      source.close()
    }
  }

  def findFile(path: String): File =
    new File(srcDirectory, path).getCanonicalFile

  def findFiles(pattern: String): Seq[File] =
    (srcDirectory ** pattern) get

  private def inClassLoader[T](cls: Class[_])(fn: => T): T = {
    val prev = Thread.currentThread.getContextClassLoader
    try {
      Thread.currentThread.setContextClassLoader(cls.getClassLoader)
      fn
    } finally {
      Thread.currentThread.setContextClassLoader(prev)
    }
  }

  object ImportEnv extends Env.Custom {
    def `import`(envIn: Env, docIn: Doc): (Env, Doc) = docIn match {
      case Block(_, args, Range.Empty) =>
        args.string(env, "source") match {
          case Some(filename) =>
            val source = graph.getSource(filename, Source.this)
            val prefix = args.string(envIn, "prefix").getOrElse("")

            // Execute the source in its own environment:
            val (importedEnv, importedDoc) = graph.expand((source.env, source.doc))

            // Import everything except "def", "bind", and "import" into the current environment:
            val newEnv = env ++ importedEnv.except("def", "bind", "import").prefix(prefix)

            // Continue expanding the document:
            (env ++ newEnv, importedDoc)

          case _ =>
            try {
              // Default to the built-in definition of import in Tipi:
              inClassLoader(Source.this.getClass) {
                Env.Basic.`import`(envIn, docIn)
              }
            } catch {
              case exn =>
                // Provide an informative error mesage:
                args.string(envIn, Id("class")) match {
                  case None => sys.error("Bad import tag: no 'source' or 'class' parameter")
                  case _    => sys.error("Bad import tag: " + docIn + ": " + exn.toString)
                }
            }
        }

      case _ =>
        sys.error("Bad import tag: " + docIn)
    }
  }

  lazy val imports = {
    def loop(doc: Doc): List[String] = {
      doc match {
        case Block(Id("import"), args, _) =>
          args.string(env, "source").map(List(_)).getOrElse(Nil)

        case Block(_, _, body) =>
          loop(body)

        case Range(children) =>
          children.flatMap(loop _)

        case _ => Nil
      }
    }

    loop(doc)
  }

  lazy val parents: List[Source] = {
    imports.map(graph.getSource(_, this))
  }

  def compiledContent: String =
    graph.render(graph.expand((env, doc)))

  def compile: Option[File] =
    des map { des =>
      graph.log.info("Compiling %s source %s".format(graph.pluginName, des))
      IO.write(des, compiledContent)
      des
    }

  override def toString =
    "Source(%s)".format(src)
}
