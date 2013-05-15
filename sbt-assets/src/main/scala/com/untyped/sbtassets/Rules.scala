package com.untyped.sbtassets

import sbt._

object Rules extends Rules

trait Rules {
  case class Append(val prereqs: List[Rule]) extends Rule {
    def assets = prereqAssets
    def compileRule(log: Logger) = ()
    def cleanRule(log: Logger) = ()
  }

  case class Cat(val targetFile: File, val prereq: Rule) extends ManyToOneRule {
    val prereqs = List(prereq)

    val target =
      Asset(Path.Root, targetFile, Nil)

    def compileAssets(log: Logger, in: List[Asset], out: Asset) = {
      val command = "cat" :: in.map(_.file.getPath)
      log.debug(command.mkString(" "))
      command #> out.file ! log
    }
  }

  case class Coffee(
    val target: File,
    val prereq: Rule,
    val bare: Boolean = false
  ) extends SimpleManyToManyRule {
    val step =
      Steps.Or(List(
        Steps.Coffee(bare),
        Steps.Copy()
      ))
  }

  case class CommonJs(
    val target: File,
    val prereq: Rule,
    val test: Asset => Boolean = (in: Asset) => true
  ) extends SimpleManyToManyRule {
    val step =
      Steps.Or(List(
        Steps.If(test, Steps.CommonJs),
        Steps.Copy()
      ))
  }

  case class Copy(
    val target: File,
    val prereq: Rule,
    val rename: Option[Asset => String] = None,
    val rewrite: Option[(Asset, List[String]) => List[String]] = None
  ) extends SimpleManyToManyRule {
    val step =
      Steps.Copy(rename, rewrite)
  }

  case class LessCss(
    val mainPath: Path,
    val targetFile: File,
    val prereq: Rule,
    val prettify: Boolean = true
  ) extends ManyToOneRule {
    val prereqs = List(prereq)

    val target =
      Asset(Path.Root, targetFile, Nil)

    def compileAssets(log: Logger, in: List[Asset], out: Asset) = {
      val mainAsset = in.find(_.path == mainPath).getOrElse(sys.error("Main asset not found: " + mainPath))
      var command = List("-O2", mainAsset.file.getPath, out.file.getPath)
      if(!prettify) command = "--yui-compress" :: command
      command = "lessc" :: command
      log.debug(command.mkString(" "))
      command ! log
    }
  }

  case class NormalizeLessCss(
    val targetFile: File,
    val prereq: Rule
  ) extends ManyToOneRule {
    val prereqs = List(prereq)

    val target = Asset(Path.Root, targetFile, Nil)

    def compileAssets(log: Logger, in: List[Asset], out: Asset) = {
      IO.delete(out.file)
      for(in <- in) {
        var echoCommand = List("echo", "/* Start: " + in.path + " */")
        echoCommand #>> out.file ! log

        var grepCommand = List("grep", "-v", "@import", in.file.getPath)
        log.debug(grepCommand.mkString(" "))
        grepCommand #>> out.file ! log

        echoCommand = List("echo", "/* End: " + in.path + " */")
        echoCommand #>> out.file ! log
      }
    }
  }

  case class UglifyJs(
    val targetFile: File,
    val prereq: Rule,
    val prettify: Boolean = false,
    val mangle: Boolean = false,
    val lint: Boolean = false
  ) extends ManyToOneRule {
    val prereqs = List(prereq)

    val target = Asset(Path.Root, targetFile, Nil)

    def compileAssets(log: Logger, in: List[Asset], out: Asset) = {
      var command = prereq.assets.map(_.file.getPath)
      if(lint) command = "--lint" :: command
      if(mangle) command = "--mangle" :: command
      if(prettify) command = "--beautify" :: command
      command = "uglifyjs" :: command
      log.debug(command.mkString(" "))
      command #> out.file ! log
    }
  }
}