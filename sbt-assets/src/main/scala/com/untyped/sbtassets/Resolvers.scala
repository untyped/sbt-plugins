package com.untyped.sbtassets

import sbt._

object Resolvers extends Resolvers

trait Resolvers {
  object Empty extends Resolver {
    def expand(in: Path) = Nil
    def find(in: Path) = None
    def pathOf(in: File) = None
  }

  case class Dir(val root: File, val filter: FileFilter = "*") extends Resolver {
    def expand(in: Path) =
      in.pathFinder(root).get.filter(file => file.isFile && filter.accept(file)).flatMap(pathOf _).toList.distinct

    def find(in: Path) =
      in.pathFinder(root).get.filter(filter.accept _).headOption

    def pathOf(in: File) =
      IO.relativize(root, in).map(file(_)).map { file =>
        if(file.getParent == null) {
          Path("/" + file.base)
        } else {
          Path("/" + file.getParent + "/" + file.base)
        }
      }
  }

  case class Or(val resolvers: List[Resolver]) extends Resolver {
    def expand(in: Path) =
      resolvers.foldLeft(Option.empty[List[Path]]) {
        (memo, resolver) =>
          memo orElse Option(resolver.expand(in)).filterNot(_.isEmpty)
      }.getOrElse(Nil)

    def find(in: Path) =
      resolvers.flatMap(_.find(in)).headOption

    def pathOf(in: File) =
      resolvers.flatMap(_.pathOf(in)).headOption
  }
}
