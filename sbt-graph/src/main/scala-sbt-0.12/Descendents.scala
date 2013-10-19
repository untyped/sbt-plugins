package com.untyped.sbtgraph

import java.io.File
import sbt._

object Descendents {
  /** Workaround for spelling change in SBT 0.11 => 0.12 */
  def apply(dir: File, includeFilter: FileFilter, excludeFilter: FileFilter) =
    dir.descendantsExcept(includeFilter, excludeFilter)
}