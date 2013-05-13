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

  /** Recompile this rule and all prereqs as necessary. */
  final def compile(state: CompileState = new CompileState()): Unit = {
    if(!state.completed.contains(this)) {
      state.completed.add(this)

      prereqs.foreach(_.compile(state))
      val start = System.currentTimeMillis
      compileRule(state)
      val end = System.currentTimeMillis
      state.log.info(getClass.getSimpleName + ": compiled in " + (end - start) + "ms")
    }
  }

  /** Recompile as necessary. Assume prereqs have been compiled. */
  def compileRule(state: CompileState): Unit

  protected def recompileAsset(in: Asset, out: Asset) =
    !out.file.exists || (in.file newerThan out.file)
}

trait OneToOneRule extends Rule {
  def translateAsset(in: Asset): Asset

  def assets =
    prereqAssets map (translateAsset _)

  def compileRule(state: CompileState): Unit =
    for {
      in  <- prereqs.map(_.assets).flatten
      out  = translateAsset(in) if recompileAsset(in, out)
    } {
      IO.createDirectory(out.file.getParentFile)
      compileAsset(state, in, out)
    }

  def compileAsset(state: CompileState, in: Asset, out: Asset): Unit
}

trait ManyToOneRule extends Rule {
  def target: Asset

  def assets =
    List(target)

  def compileRule(state: CompileState): Unit = {
    val in = prereqAssets
    val out = target
    if(in.find(recompileAsset(_, out)).isDefined) {
      IO.createDirectory(out.file.getParentFile)
      compileAssets(state, in, out)
    }
  }

  def compileAssets(state: CompileState, in: List[Asset], out: Asset): Unit
}