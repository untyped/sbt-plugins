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
  ) extends ManyToManyRule {
    val prereqs = List(prereq)

    def translateAsset(in: Asset) =
      Asset(in.path, target / (in.path.toString + ".js"), in.dependencies)

    def compileAsset(log: Logger, in: Asset, out: Asset) = {
      in.file.ext match {
        case "coffee" =>
          var command = List("--compile", "--print", in.file.getPath)
          if(bare) command = "--bare" :: command
          command = "coffee" :: command
          log.debug(command.mkString(" "))
          command #> out.file ! log

        case _ =>
          IO.copy(List(in.file -> out.file))
      }
    }
  }

  case class CommonJs(
    val target: File,
    val prereq: Rule,
    val test: Asset => Boolean = (in: Asset) => true
  ) extends ManyToManyRule {
    val prereqs = List(prereq)

    def translateAsset(in: Asset) =
      Asset(in.path, target / (in.path.toString + ".js"), in.dependencies)

    def compileAsset(log: Logger, in: Asset, out: Asset) = {
      if(test(in)) {
        IO.writeLines(
          out.file,
          List("window.require.register(\"" + in.path.toString.substring(1) + "\", (exports, require, module) {") ++
          IO.readLines(in.file).map("  " + _) ++
          List("});")
        )
      } else {
        IO.copy(List(in.file -> out.file))
      }
    }
  }

  case class Copy(
    val target: File,
    val prereq: Rule,
    val rewriteFilename: Option[Asset => String] = None,
    val rewriteContent: Option[(Asset, List[String]) => List[String]] = None
  ) extends ManyToManyRule {
    val prereqs = List(prereq)

    def translateAsset(in: Asset) =
      rewriteFilename match {
        case Some(fn) => Asset(in.path, target / (in.path.parent.toString + fn(in)), in.dependencies)
        case None     => Asset(in.path, target / in.path.toString, in.dependencies)
      }

    def compileAsset(log: Logger, in: Asset, out: Asset) =
      rewriteContent match {
        case Some(fn) => IO.writeLines(out.file, fn(in, IO.readLines(in.file)))
        case None     => IO.copy(List(in.file -> out.file))
      }
  }

  case class Filter(val pred: Asset => Boolean, val prereq: Rule) extends Rule {
    val prereqs = List(prereq)
    def assets = prereqAssets.filter(pred)
    def compileRule(log: Logger) = ()
    def cleanRule(log: Logger) = ()
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