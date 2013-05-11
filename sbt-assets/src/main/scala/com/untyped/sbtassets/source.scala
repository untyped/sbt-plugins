package com.untyped.sbtassets

import sbt._
import scala.collection.mutable

case class Source(
  val path: Path,
  val file: File,
  val dependencies: List[Path]
)
