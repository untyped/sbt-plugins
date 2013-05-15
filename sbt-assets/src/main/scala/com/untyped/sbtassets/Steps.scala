package com.untyped.sbtassets

import sbt._

object Steps extends Steps

trait Steps {
  case class Coffee(val bare: Boolean = false) extends Step {
    def isDefinedAt(in: Asset): Boolean =
      in.file.ext == "coffee"

    def rename(in: Asset): String =
      in.path.toString + ".js"

    def compile(log: Logger, in: Asset, out: Asset): Unit = {
      var command = List("--compile", "--print", in.file.getPath)
      if(bare) command = "--bare" :: command
      command = "coffee" :: command
      log.debug(command.mkString(" "))
      command #> out.file ! log
    }
  }

  case object CommonJs extends Step {
    def isDefinedAt(in: Asset): Boolean =
      in.file.ext == "js" || in.file.ext == "coffee"

    def rename(in: Asset): String =
      in.file.name

    def compile(log: Logger, in: Asset, out: Asset): Unit =
      if(in.file.ext == "js") {
        IO.writeLines(
          out.file,
          List("window.require.register(\"" + in.path.toString.substring(1) + "\", function(exports, require, module) {") ++
          IO.readLines(in.file).map("  " + _) ++
          List("});")
        )
      } else {
        IO.writeLines(
          out.file,
          List("window.require.register \"" + in.path.toString.substring(1) + "\", (exports, require, module) ->") ++
          IO.readLines(in.file).map("  " + _)
        )
      }
  }

  case class Handlebars() extends Step {
    def isDefinedAt(in: Asset): Boolean =
      in.file.ext == "handlebars"

    def rename(in: Asset): String =
      in.file.base + ".js"

    def compile(log: Logger, in: Asset, out: Asset): Unit = {
      var command = List("Handlebars", "--s", in.file.getPath)
      log.debug(command.mkString(" "))
      IO.writeLines(
        out.file,
        List(
          "module.exports = Handlebars.template(",
          (command !! log),
          ");"
        )
      )
    }
  }

  case class Copy(
    val optRename: Option[Asset => String] = None,
    val optRewrite: Option[(Asset, List[String]) => List[String]] = None
  ) extends Step {
    def isDefinedAt(in: Asset): Boolean =
      true

    def rename(in: Asset): String =
      optRename.map(_(in)).getOrElse(in.file.name)

    def compile(log: Logger, in: Asset, out: Asset): Unit =
      optRewrite match {
        case Some(fn) => IO.writeLines(out.file, fn(in, IO.readLines(in.file)))
        case None     => IO.copy(List(in.file -> out.file))
      }
  }

  case class If(val test: Asset => Boolean, val step: Step) extends Step {
    def isDefinedAt(in: Asset) =
      test(in) && step.isDefinedAt(in)

    def rename(in: Asset) =
      step.rename(in)

    def compile(log: Logger, in: Asset, out: Asset): Unit =
      step.compile(log, in, out)
  }

  case class Or(val steps: List[Step]) extends Step {
    def isDefinedAt(in: Asset): Boolean =
      steps.find(_ isDefinedAt in).isDefined

    def rename(in: Asset): String =
      steps.find(_ isDefinedAt in).get.rename(in)

    def compile(log: Logger, in: Asset, out: Asset): Unit =
      steps.find(_ isDefinedAt in).get.compile(log, in, out)
  }
}
