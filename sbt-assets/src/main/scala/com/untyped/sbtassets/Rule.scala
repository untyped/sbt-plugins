package com.untyped.sbtassets

import sbt._

trait Rule {
  def prereqs: List[Rule]

  /** All sources output by this rule. */
  def sources: List[Source]

  /** All sources output by this rule's prerequisites. */
  def prereqSources =
    prereqs.map(_.sources).flatten

  /** All sources output by this rule and its prerequisites. */
  def watchSources: List[Source] =
    (prereqSources ++ sources).distinct

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

  protected def recompileSource(in: Source, out: Source) =
    !out.file.exists || (in.file newerThan out.file)
}

trait OneToOneRule extends Rule {
  def translateSource(in: Source): Source

  def sources =
    prereqSources map (translateSource _)

  def compileRule(state: CompileState): Unit =
    for {
      in  <- prereqs.map(_.sources).flatten
      out  = translateSource(in) if recompileSource(in, out)
    } {
      IO.createDirectory(out.file.getParentFile)
      compileSource(state, in, out)
    }

  def compileSource(state: CompileState, in: Source, out: Source): Unit
}

trait ManyToOneRule extends Rule {
  def target: Source

  def sources =
    List(target)

  def compileRule(state: CompileState): Unit = {
    val in = prereqSources
    val out = target
    if(in.find(recompileSource(_, out)).isDefined) {
      IO.createDirectory(out.file.getParentFile)
      compileSources(state, in, out)
    }
  }

  def compileSources(state: CompileState, in: List[Source], out: Source): Unit
}