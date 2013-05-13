package com.untyped.sbtassets

import sbt._

trait Rule {
  def prereqs: List[Rule]

  /** All assets output by this rule. */
  def assets: List[Asset]

  /** All assets output by this rule's prerequisites. */
  def prereqAssets =
    prereqs.map(_.assets).flatten

  /** All assets output by this rule and its prerequisites. */
  def watchAssets: List[Asset] =
    (prereqAssets ++ assets).distinct

  final def watchSources =
    watchAssets.map(_.file)

  /** Recompile this rule and all prereqs as necessary. */
  final def compile(log: Logger, state: CompileState = new CompileState()): Unit =
    if(!state.completed.contains(this)) {
      state.completed.add(this)
      prereqs.foreach(_.compile(log, state))

      log.info(getClass.getSimpleName + ".compile:")
      val start = System.currentTimeMillis
      compileRule(log)
      val end = System.currentTimeMillis
      log.info("  completed in " + (end - start) + "ms")
    }

  final def clean(log: Logger, state: CompileState = new CompileState()): Unit = {
    if(!state.completed.contains(this)) {
      state.completed.add(this)
      prereqs.foreach(_.clean(log, state))

      log.info(getClass.getSimpleName + ".clean:")
      val start = System.currentTimeMillis
      cleanRule(log)
      val end = System.currentTimeMillis
      log.info("  completed in " + (end - start) + "ms")
    }
  }

  def cleanRule(log: Logger) =
    assets.map(_.file).filter(_.exists).foreach { file =>
      log.info("  delete " + file)
      file.delete
    }

  /** Recompile as necessary. Assume prereqs have been compiled. */
  def compileRule(log: Logger): Unit

  protected def recompileAsset(in: Asset, out: Asset) =
    !out.file.exists || (in.file newerThan out.file)
}

trait OneToOneRule extends Rule {
  def translateAsset(in: Asset): Asset

  def assets =
    prereqAssets map (translateAsset _)

  def compileRule(log: Logger): Unit =
    for {
      in  <- prereqs.map(_.assets).flatten
      out  = translateAsset(in) if recompileAsset(in, out)
    } {
      log.info("  compile " + in.file + " => " + out.file)

      IO.createDirectory(out.file.getParentFile)
      compileAsset(log, in, out)
    }

  def compileAsset(log: Logger, in: Asset, out: Asset): Unit
}

trait ManyToOneRule extends Rule {
  def target: Asset

  def assets =
    List(target)

  def compileRule(log: Logger): Unit = {
    val in = AssetGraph(prereqAssets).sorted
    val out = target

    if(in.find(recompileAsset(_, out)).isDefined) {
      log.info("  compile " + in.map(_.file) + " => " + out.file)

      IO.createDirectory(out.file.getParentFile)
      compileAssets(log, in, out)
    }
  }

  def compileAssets(log: Logger, in: List[Asset], out: Asset): Unit
}