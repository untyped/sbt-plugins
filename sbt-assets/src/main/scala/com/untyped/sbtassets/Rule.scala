package com.untyped.sbtassets

import sbt._

trait Rule {
  /** Return a list of all sources output by this rule. */
  def sources: List[Source]

  /** Recompile as necessary and return a list of all updated sources. */
  def compile: List[Source]

  protected def requiresRecompilation(in: Source, out: Source) =
    !out.file.exists || (in.file newerThan out.file)
}
