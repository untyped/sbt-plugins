package com.untyped.sbtassets

import sbt._

object Resolvers extends Resolvers

trait Resolvers {
  object Empty extends Resolver {
    def apply(in: Path, ext: String) = None
  }

  case class Dir(val dir: File) extends Resolver {
    def apply(in: Path, ext: String) = {
      Option(dir / (in.toString + "." + ext)).filter(_.isFile)
    }
  }

  case class Extensions(val extensions: List[String], val inner: Resolver) extends Resolver {
    def apply(in: Path, ext: String) =
      extensions.foldLeft(Option.empty[File]) { (memo, ext) =>
        memo orElse inner(in, ext)
      }
  }

  case class Or(val resolvers: List[Resolver]) extends Resolver {
    def apply(in: Path, ext: String) =
      resolvers.foldLeft(Option.empty[File]) { (memo, resolver) =>
        memo orElse resolver(in, ext)
      }
  }
}
