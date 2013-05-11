package com.untyped.sbtassets

import sbt._

object Rules extends Rules

trait Rules {
  case class Coffee(val target: File, val prereq: Rule ) extends Rule {
    def translate(in: Source) =
      Source(in.path, target / (in.path withExtension ".js").toString, in.dependencies)

    def sources =
      prereq.sources map (translate _)

    def compile =
      for {
        in  <- prereq.compile
        out <- List(translate(in)) if requiresRecompilation(in, out)
      } yield {
        println("coffee " + in.file + " => " + out.file)
        IO.createDirectory(out.file.getParentFile)
        ((List("coffee", "-bcp", in.file.getPath) #> out.file) !)
        out
      }
  }

  case class Cat(val target: File, val prereq: Rule) extends Rule {
    val targetSource =
      Source(Path.Root, target, Nil)

    val sources =
      List(targetSource)

    def compile =
      if(prereq.compile.find(requiresRecompilation(_, targetSource)).isDefined) {
        println("cat " + prereq.sources.map(_.file).mkString(" ") + " => " + target)
        IO.createDirectory(target.getParentFile)
        ((("cat" :: prereq.sources.map(_.file.getPath)) #> target) !)
        sources
      } else {
        println("cat NOOP")
        Nil
      }
  }

  case class UglifyJs(val target: File, val prereq: Rule) extends Rule {
    val targetSource =
      Source(Path.Root, target, Nil)

    val sources =
      List(targetSource)

    def compile =
      if(prereq.compile.find(requiresRecompilation(_, targetSource)).isDefined) {
        println("uglify " + prereq.sources.map(_.file).mkString(" ") + " => " + target)
        IO.createDirectory(target.getParentFile)
        ((("uglifyjs" :: prereq.sources.map(_.file.getPath)) #> target) !)
        sources
      } else {
        println("uglify NOOP")
        Nil
      }
  }

  case class Append(val prereqs: Seq[Rule]) extends Rule {
    def sources = {
      val ans = prereqs.foldLeft[List[Source]](Nil)((accum, rule) => accum ++ rule.sources)
      ans
    }

    def compile = {
      val ans = prereqs.foldLeft[List[Source]](Nil)((accum, rule) => accum ++ rule.compile)
      println("append " + ans.map(_.file).mkString(" "))
      ans
    }
  }

  case class Filter(val prereq: Rule, val pred: Source => Boolean) extends Rule {
    def sources = {
      val ans = prereq.sources.filter(pred)
      ans
    }

    def compile = {
      val in = prereq.compile
      val out = in.filter(pred)
      println("filter " + in.map(_.file).mkString(" ") + " => " + out.map(_.file).mkString(" "))
      out
    }
  }
}