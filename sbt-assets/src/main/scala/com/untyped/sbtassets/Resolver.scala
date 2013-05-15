package com.untyped.sbtassets

import sbt._

trait Resolver {
  def expand(in: Path): List[Path]
  def find(in: Path): Option[File]
  def pathOf(in: File): Option[Path]
}
