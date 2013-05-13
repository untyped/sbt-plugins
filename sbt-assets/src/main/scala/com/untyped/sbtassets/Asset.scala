package com.untyped.sbtassets

import sbt._
import scala.collection.mutable

case class Asset(
  val path: Path,
  val file: File,
  val dependencies: List[Path]
)
