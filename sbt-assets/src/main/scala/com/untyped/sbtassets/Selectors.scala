package com.untyped.sbtassets

import sbt._
import scala.collection.mutable

object Selectors extends Selectors

trait Selectors {
  case class Const(val assets: List[Asset]) extends Selector {
    val format = Formats.Any
  }

  case class Dir(
    val dir: File,
    val finder: (File) => PathFinder = (in: File) => in ***,
    val format: Format = Formats.Any
  ) extends SimpleSelector {
    def assets =
      finder(dir).get.filter(_.isFile).toList.map { file =>
        val unnorm = IO.relativize(dir, file).get
        val norm = format.stripExtension(unnorm) getOrElse unnorm
        val path = Path("/" + norm)
        val deps = dependencies(path.parent, file)
        Asset(path, file, deps)
      }
  }
}