package com.untyped.sbtassets

import sbt._
import scala.collection.mutable

/** Rule that gets its assets straight from the filesystem. */
trait Selector extends Rule {
  def prereqs = Nil
  override def unmanagedAssets = assets
  override def managedAssets = Nil
  def compileRule(log: Logger) = {
    for(asset <- assets) {
      log.info("  select " + asset.path + " => " + asset.file)
    }
  }
  override def cleanRule(log: Logger) = ()
}

/** Rule that gets its assets straight from the filesystem. */
trait SimpleSelector extends Selector {
  def format: Format

  val cachedDependencies = mutable.HashMap[File, (Long, List[Path])]()

  def dependencies(dir: Path, file: File) = {
    cachedDependencies.get(file) match {
      case Some((modified, deps)) if modified == file.lastModified =>
        deps

      case _ =>
        val deps = format.dependencies(dir, file)
        cachedDependencies.put(file, (file.lastModified, deps))
        deps
    }
  }
}
