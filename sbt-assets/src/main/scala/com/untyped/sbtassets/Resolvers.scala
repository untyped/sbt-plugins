package com.untyped.sbtassets

import sbt._

object Resolvers extends Resolvers

trait Resolvers {
  object Empty extends Resolver {
    def apply(in: Path) = None
  }

  case class Dir(val dir: File) extends Resolver {
    def apply(in: Path) =
      Option(dir / in.toString).filter(_.isFile)
  }

  case class Extensions(val extensions: List[String], val inner: Resolver) extends Resolver {
    def apply(in: Path) =
      extensions.foldLeft(Option.empty[File]) { (memo, ext) =>
        memo orElse inner(in withExtension ext)
      }
  }

  case class Or(val resolvers: List[Resolver]) extends Resolver {
    def apply(in: Path) =
      resolvers.foldLeft(Option.empty[File]) { (memo, resolver) =>
        memo orElse resolver(in)
      }
  }
}
