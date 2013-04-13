package com.untyped.sbtassets

import sbt._

case class Source(
  val path: String,
  val file: File,
  val dependencies: List[String]
) {
  val name = sbt.file(path).name
  val (base, ext) = sbt.file(path).baseAndExt
}

case class Sources(val sources: Seq[Source]) {
  def get(name: String) =
    sources.find(_.name == name)

  def shadow(that: Sources) =
    Sources(
      this.sources ++
      that.sources.filterNot { source => get(source.name).isDefined }
    )

  def ++ (that: Sources) =
    Sources(this.sources ++ that.sources)

  def orderedSources = sources.sortBy(_.path)
}

object Sources {
  def apply(
    main: String,
    resolver: Resolver,
    dependencyReader: DependencyReader
  ): Sources = {
    val files = finder(root).files
    Sources()
  }
}