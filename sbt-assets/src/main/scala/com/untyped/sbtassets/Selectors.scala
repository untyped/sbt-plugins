package com.untyped.sbtassets

import sbt._
import scala.collection.mutable

object Selectors extends Selectors

trait Selectors {
  case class Const(val assets: List[Asset]) extends Selector {
    val format = Formats.Any
  }

  case class Deps(
    val main: Path,
    val resolver: Resolver,
    val format: Format = Formats.Any
  ) extends SimpleSelector {
    def makeAssets(path: Path) =
      for {
        path <- resolver.expand(path) match {
                  case Nil   => sys.error("Cannot find file(s) for path: " + path)
                  case files => files
                }
        file <- resolver.find(path)
      } yield Asset(path, file, dependencies(path.parent, file).flatMap(resolver.expand _))

    def assets = {
      val open = mutable.Queue(makeAssets(main) : _*)
      val closed = mutable.ArrayBuffer[Asset]()

      while(!open.isEmpty) {
        val curr = open.dequeue
        closed += curr

        val next = curr.dependencies.flatMap(makeAssets _).filterNot(closed.contains _)

        open.enqueue(next : _*)
      }

      closed.toList
    }
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