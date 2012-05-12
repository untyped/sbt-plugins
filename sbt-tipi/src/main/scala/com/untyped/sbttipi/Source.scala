package com.untyped.sbttipi

// Need to be specific here because sbt.Doc conflicts with tipi.core.Doc:
import sbt.{ File, IO, file, singleFileFinder, globFilter }
import scala.collection._
import scala.util.parsing.combinator._
import scala.util.parsing.input._
import tipi.core._

case class Source(val graph: Graph, val src: File) extends com.untyped.sbtgraph.Source with tipi.core.Implicits {

  type S = com.untyped.sbttipi.Source
  type G = com.untyped.sbttipi.Graph

  def isTemplated = true

  lazy val env: Env =
    graph.environment ++ Env(immutable.Map(Id("import") -> importTransform))

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

  def loadEnv(name: String): Env = {
    try {
      val clazz = Class.forName(name)
      try {
        clazz.getConstructor(classOf[Source]).newInstance(Source.this).asInstanceOf[Env]
      } catch {
        case exn: NoSuchMethodException =>
          clazz.newInstance.asInstanceOf[Env]
      }
    } catch {
      case exn: ClassNotFoundException =>
        sys.error("Could not import class: " + name + " not found")
    }
  }

  lazy val importTransform = Transform.Full {
    case (env, Block(_, args, Range.Empty)) =>
      if(args.contains[String](env, "source")) {
        val source = graph.getSource(args[String](env, "source"), Source.this)
        val prefix = args.get[String](env, "prefix").getOrElse("")

        // Execute the source in its own environment:
        val (importedEnv, importedDoc) = graph.expand((source.env, source.doc))

        // Import everything except "def", "bind", and "import" into the current environment:
        val newEnv = env ++ (importedEnv -- Env.basic - Id("import")).prefixWith(prefix)

        // Continue expanding the document:
        (env ++ newEnv, importedDoc)
      } else if(args.contains[String](env, "class")) {
        val prefix = args.get[String](env, "prefix").getOrElse("")
        (env ++ loadEnv(args[String](env, "class")).prefixWith(prefix), Range.Empty)
      } else {
        sys.error("Bad import tag: no 'source' or 'class' parameter")
      }

    case other =>
      sys.error("Bad import tag: " + other)
  }

  lazy val imports = {
    def loop(doc: Doc): List[String] = {
      doc match {
        case Block(Id("import"), args, _) =>
          if(args.contains[Any](env, "source")) {
            List(args[String](env, "source"))
          } else {
            Nil
          }
        case Block(_, _, body) =>
          loop(body)
        case Range(children) =>
          children.flatMap(loop _)
        case _ =>
          Nil
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
