package com.untyped.sbtassets

import sbt._

object Rules extends Rules

trait Rules {
  case class Append(val prereqs: List[Rule]) extends Rule {
    def sources = prereqs.map(_.sources).flatten
    def compile = prereqs.map(_.compile).flatten
  }

  case class Coffee(val target: File, val prereq: Rule) extends Rule {
    def translate(in: Source) =
      Source(in.path, target / (in.path withExtension ".js").toString, in.dependencies)

    def sources =
      prereq.sources map (translate _)

    def compile = {
      prereq.compile
      for {
        in  <- prereq.sources
        out <- List(translate(in)) if requiresRecompilation(in, out)
      } yield {
        IO.createDirectory(out.file.getParentFile)
        ((List("coffee", "-bcp", in.file.getPath) #> out.file) !)
        out
      }
    }
  }

  case class Cat(val target: File, val prereq: Rule) extends Rule {
    val targetSource =
      Source(Path.Root, target, Nil)

    val sources =
      List(targetSource)

    def compile = {
      prereq.compile
      if(prereq.sources.find(requiresRecompilation(_, targetSource)).isDefined) {
        IO.createDirectory(target.getParentFile)
        ((("cat" :: prereq.sources.map(_.file.getPath)) #> target) !)
        sources
      } else Nil
    }
  }

  case class Filter(val prereq: Rule, val pred: Source => Boolean) extends Rule {
    def sources = prereq.sources.filter(pred)
    def compile = prereq.compile.filter(pred)
  }

  case class Rewrite(
    val target: File,
    val rewrite: (Source, String) => String,
    val prereq: Rule
  ) extends Rule {
    def translate(in: Source) =
      Source(in.path, target / (in.path withExtension ("." + in.file.ext)).toString, in.dependencies)

    def sources =
      prereq.sources map (translate _)

    def compile = {
      prereq.compile
      for {
        in  <- prereq.sources
        out <- List(translate(in)) if requiresRecompilation(in, out)
      } yield {
        IO.createDirectory(out.file.getParentFile)
        IO.write(out.file, rewrite(in, IO.read(in.file)))
        out
      }
    }
  }

  case class UglifyJs(val target: File, val prereq: Rule) extends Rule {
    val targetSource =
      Source(Path.Root, target, Nil)

    val sources =
      List(targetSource)

    def compile = {
      prereq.compile
      if(prereq.sources.find(requiresRecompilation(_, targetSource)).isDefined) {
        IO.createDirectory(target.getParentFile)
        ((("uglifyjs" :: prereq.sources.map(_.file.getPath)) #> target) !)
        sources
      } else Nil
    }
  }
}