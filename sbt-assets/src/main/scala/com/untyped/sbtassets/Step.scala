package com.untyped.sbtassets

import sbt._

trait Step {
  def isDefinedAt(in: Asset): Boolean
  def rename(in: Asset): String
  def compile(log: Logger, in: Asset, out: Asset): Unit
}
