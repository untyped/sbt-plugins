package com.untyped.sbtassets

import sbt._

case class Source(
  val path: String,
  val file: File,
  val dependencies: List[String]
) {
  def filename: String = path.getName

  def filenameStem = {
    if(filename.indexOf('.') < 0) {
      filename
    } else {
      filename.substring(0, filename.lastIndexOf('.'))
    }
  }

  def filenameExtension = {
    if(filename.indexOf('.') < 0) {
      ""
    } else {
      filename.substring(filename.lastIndexOf('.') + 1)
    }
  }

  def relativePath: File = new File(name).getParentFile
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

  def orderedSources = sources
}

object Sources {
  def apply(sources: Source *): Sources =
    Sources(sources.toList)

  def apply(root: File, finder: PathFinder, reader: SourceReader): Sources =
    Sources(finder.get.map(reader(root, _)))
}