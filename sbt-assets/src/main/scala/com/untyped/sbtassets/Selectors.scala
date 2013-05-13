package com.untyped.sbtassets

import sbt._
import scala.collection.mutable

object Selectors extends Selectors

trait Selectors {
  case class Const(val sources: List[Source]) extends Selector {
    val format = Formats.Any
  }

  case class Deps(val main: Path, val resolver: Resolver, val format: Format = Formats.Any) extends SimpleSelector {
    def makeSource(path: Path) = {
      val file = resolver(path) getOrElse sys.error("Cannot find file for path: " + path)
      Source(path, file, dependencies(path.parent, file))
    }

    def sources = {
      val open = mutable.Queue(makeSource(main))
      val closed = mutable.ArrayBuffer[Source]()

      while(!open.isEmpty) {
        val curr = open.dequeue
        closed += curr

        val next = curr.dependencies.map(makeSource).filterNot(closed.contains _)

        open.enqueue(next : _*)
      }

      closed.toList
    }
  }

  case class Dir(val dir: File, val format: Format = Formats.Any) extends SimpleSelector {
    def sources =
      (dir ***).get.filter(_.isFile).toList.map { file =>
        val path = Path("/" + IO.relativize(dir, file).get).stripExtension
        val deps = dependencies(path.parent, file)
        Source(path, file, deps)
      }
  }
}