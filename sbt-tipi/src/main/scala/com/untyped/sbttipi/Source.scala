package com.untyped.sbttipi

import sbt.{ File, IO }
import scala.collection._
import scala.util.parsing.combinator._
import scala.util.parsing.input._
import tipi.core._

case class Source(val graph: Graph, val src: File) extends com.untyped.sbtgraph.Source {

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

  lazy val importTransform =
    new Transform {
      def isDefinedAt(in: (Env, Doc)): Boolean =
        true

      def apply(in: (Env, Doc)): (Env, Doc) = {
        val (env, doc) = in
        doc match {
          case Block(_, StringArgument(Id("source"), name) :: Nil, Range.Empty) =>
            val source = graph.getSource(name, Source.this)

            // Execute the source in its own environment:
            val (importedEnv, importedDoc) = graph.expand((source.env, source.doc))

            // Import everything except "def", "bind", and "import" into the current environment:
            val newEnv = env ++ (importedEnv -- Env.basic - Id("import"))

            // Continue expanding the document:
            (env ++ newEnv, importedDoc)

          case other =>
            sys.error("Bad import tag: " + other)
        }
      }
    }

  lazy val imports = {
    def loop(doc: Doc): List[String] = {
      doc match {
        case Block(Id("import"), StringArgument(Id("source"), filename) :: _, _) => List(filename)
        case Block(Id("import"), UnitArgument(Id(filename)) :: _, _)             => List(filename)
        case Range(children)                                                     => children.flatMap(loop _)
        case _ => Nil
      }
    }

    loop(doc)
  }

  lazy val parents: List[Source] = {
    println("DOC " + src + "\n" + doc)
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
