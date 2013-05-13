package com.untyped.sbtassets

import sbt._

object Rules extends Rules

trait Rules {
  case class Append(val prereqs: List[Rule]) extends Rule {
    def sources = prereqSources
    override def watchSources = sources
    def compileRule(state: CompileState) = ()
  }

  case class Coffee(val target: File, val prereq: Rule) extends OneToOneRule {
    val prereqs = List(prereq)

    def translateSource(in: Source) =
      Source(in.path, target / (in.path withExtension ".js").toString, in.dependencies)

    def compileSource(state: CompileState, in: Source, out: Source) =
      List("coffee", "-bcp", in.file.getPath) #> out.file ! state.log
  }

  case class Cat(val targetFile: File, val prereq: Rule) extends ManyToOneRule {
    val prereqs = List(prereq)

    val target =
      Source(Path.Root, targetFile, Nil)

    def compileSources(state: CompileState, in: List[Source], out: Source) =
      ("cat" :: in.map(_.file.getPath)) #> out.file ! state.log
  }

  case class Filter(val pred: Source => Boolean, val prereq: Rule) extends Rule {
    val prereqs = List(prereq)
    def sources = prereqSources.filter(pred)
    override def watchSources = sources
    def compileRule(state: CompileState) = ()
  }

  case class Rewrite(
    val target: File,
    val rewrite: (Source, String) => String,
    val prereq: Rule
  ) extends OneToOneRule {
    val prereqs = List(prereq)

    def translateSource(in: Source) =
      Source(in.path, target / (in.path withExtension ("." + in.file.ext)).toString, in.dependencies)

    def compileSource(state: CompileState, in: Source, out: Source) = {
      IO.createDirectory(out.file.getParentFile)
      IO.write(out.file, rewrite(in, IO.read(in.file)))
    }
  }

  case class UglifyJs(val targetFile: File, val prereq: Rule) extends ManyToOneRule {
    val prereqs = List(prereq)

    val target =
      Source(Path.Root, targetFile, Nil)

    def compileSources(state: CompileState, in: List[Source], out: Source) =
      ("uglifyjs" :: prereq.sources.map(_.file.getPath)) #> out.file ! state.log
  }
}