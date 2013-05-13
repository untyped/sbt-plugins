package com.untyped.sbtassets

import sbt._

object Rules extends Rules

trait Rules {
  case class Append(val prereqs: List[Rule]) extends Rule {
    def assets = prereqAssets
    override def watchAssets = assets
    def compileRule(log: Logger) = ()
  }

  case class Coffee(val target: File, val prereq: Rule) extends OneToOneRule {
    val prereqs = List(prereq)

    def translateAsset(in: Asset) =
      Asset(in.path, target / (in.path withExtension ".js").toString, in.dependencies)

    def compileAsset(log: Logger, in: Asset, out: Asset) =
      List("coffee", "-bcp", in.file.getPath) #> out.file ! log
  }

  case class Cat(val targetFile: File, val prereq: Rule) extends ManyToOneRule {
    val prereqs = List(prereq)

    val target =
      Asset(Path.Root, targetFile, Nil)

    def compileAssets(log: Logger, in: List[Asset], out: Asset) =
      ("cat" :: in.map(_.file.getPath)) #> out.file ! log
  }

  case class Filter(val pred: Asset => Boolean, val prereq: Rule) extends Rule {
    val prereqs = List(prereq)
    def assets = prereqAssets.filter(pred)
    override def watchAssets = assets
    def compileRule(log: Logger) = ()
  }

  case class Rewrite(
    val target: File,
    val rewrite: (Asset, String) => String,
    val prereq: Rule
  ) extends OneToOneRule {
    val prereqs = List(prereq)

    def translateAsset(in: Asset) =
      Asset(in.path, target / (in.path withExtension ("." + in.file.ext)).toString, in.dependencies)

    def compileAsset(log: Logger, in: Asset, out: Asset) = {
      IO.createDirectory(out.file.getParentFile)
      IO.write(out.file, rewrite(in, IO.read(in.file)))
    }
  }

  case class UglifyJs(val targetFile: File, val prereq: Rule) extends ManyToOneRule {
    val prereqs = List(prereq)

    val target =
      Asset(Path.Root, targetFile, Nil)

    def compileAssets(log: Logger, in: List[Asset], out: Asset) =
      ("uglifyjs" :: prereq.assets.map(_.file.getPath)) #> out.file ! log
  }
}