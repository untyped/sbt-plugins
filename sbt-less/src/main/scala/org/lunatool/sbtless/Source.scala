package org.lunatool.sbtless

import sbt._


class LessSourceFile(val lessFile: File, sourcesDir: File, cssDir: File) extends org.lesscss.LessSource(lessFile) {
  val relPath = IO.relativize(sourcesDir, lessFile).get

  lazy val cssFile = new File(cssDir, relPath.replaceFirst("\\.less$",".css"))
  //lazy val importsFile = new File(targetDir, relPath + ".imports")
  lazy val parentDir = lessFile.getParentFile

  def changed = this.getLastModifiedIncludingImports > cssFile.lastModified
  def path = lessFile.getPath.replace('\\', '/')

  override def toString = lessFile.toString
}
