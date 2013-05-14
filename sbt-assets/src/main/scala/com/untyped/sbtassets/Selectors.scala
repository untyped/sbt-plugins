package com.untyped.sbtassets

import sbt._
import scala.collection.mutable

object Selectors extends Selectors

trait Selectors {
  case class Const(val assets: List[Asset]) extends Selector {
    val format = Formats.Any
  }

  case class Deps(val main: Path, val resolver: Resolver, val format: Format = Formats.Any) extends SimpleSelector {
    def makeAsset(path: Path) = {
      val file = resolver(path, "") getOrElse sys.error("Cannot find file for path: " + path)
      Asset(path, file, dependencies(path.parent, file))
    }

    def assets = {
      val open = mutable.Queue(makeAsset(main))
      val closed = mutable.ArrayBuffer[Asset]()

      while(!open.isEmpty) {
        val curr = open.dequeue
        closed += curr

        val next = curr.dependencies.map(makeAsset).filterNot(closed.contains _)

        open.enqueue(next : _*)
      }

      closed.toList
    }
  }

  case class Dir(val dir: File, val format: Format = Formats.Any) extends SimpleSelector {
    def assets =
      (dir ***).get.filter(_.isFile).toList.map { file =>
        val unnorm = IO.relativize(dir, file).get
        val norm = format.stripExtension(unnorm) getOrElse unnorm
        val path = Path("/" + norm)
        val deps = dependencies(path.parent, file)
        Asset(path, file, deps)
      }
  }
}