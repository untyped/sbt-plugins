package com.untyped.sbtassets

import sbt._
import scala.collection.mutable

object Selectors extends Selectors

trait Selectors {
  case class Const(val sources: List[Source]) extends Selector

  case class Deps(val main: Path, val resolver: Resolver, val format: Format = Formats.Any) extends Selector {
    def makeSource(path: Path) =
      for {
        file <- resolver(path)
      } yield Source(path, file, format.dependencies(path.parent, file))

    def sources =
      makeSource(main) match {
        case Some(main) =>
          val open = mutable.Queue(main)
          val closed = mutable.ArrayBuffer[Source]()

          while(!open.isEmpty) {
            val curr = open.dequeue
            closed += curr

            val next =
              curr.dependencies.
                flatMap(makeSource).
                filterNot(closed.contains _)

            open.enqueue(next : _*)
          }

          closed.toList

        case None =>
          Nil
      }
  }

  case class Dir(val dir: File, val finder: PathFinder, val format: Format = Formats.Any) extends Selector {
    def sources =
      finder.get.toList.map { file =>
        val path = Path(IO.relativize(dir, file).get).stripExtension
        val deps = format.dependencies(path.parent, file)
        Source(path, file, deps)
      }
  }
}