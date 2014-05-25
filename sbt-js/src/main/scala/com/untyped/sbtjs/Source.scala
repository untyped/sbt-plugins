package com.untyped.sbtjs

import com.google.javascript.jscomp.{
  SourceFile => ClosureSource,
  Compiler => ClosureCompiler
}
import sbt._
import scala.collection.JavaConversions._

trait Source extends com.untyped.sbtgraph.Source {

  type S = com.untyped.sbtjs.Source
  type G = com.untyped.sbtjs.Graph

  def compile: Option[File] = {
    val des = this.des getOrElse (throw new Exception("Could not determine destination filename for " + src))

    graph.log.info("Compiling %s source %s".format(graph.pluginName, des))

    val compiler = new ClosureCompiler

    ClosureCompiler.setLoggingLevel(graph.closureLogLevel)

    val myExterns = graph.closureExterns(this)
    val mySources = graph.closureSources(this)

    graph.log.debug("  externs:")
    myExterns.foreach(x => graph.log.debug("    " + x))

    graph.log.debug("  sources:")
    mySources.foreach(x => graph.log.debug("    " + x))

    val result =
      compiler.compile(
        myExterns,
        mySources,
        graph.closureOptions)

    val errors = result.errors.toList
    val warnings = result.warnings.toList

    if(!errors.isEmpty) {
      graph.log.error(errors.length + " errors compiling " + src + ":")
      errors.foreach(err => graph.log.error(err.toString))

      None
    } else {
      if(!warnings.isEmpty) {
        graph.log.warn(warnings.length + " warnings compiling " + src + ":")
        warnings.foreach(err => graph.log.warn(err.toString))
      }

      val mapDes = new File(des.getPath+".map")

      IO.createDirectory(new File(des.getParent))
      IO.write(des, compiler.toSource+"\n"+"//@ sourceMappingURL="+mapDes.getName)

      val mapWriter = new java.io.FileWriter(mapDes)
      compiler.getSourceMap.appendTo(mapWriter, des.getName)
      mapWriter.close()

      Some(des)
    }
  }

  // Helpers ------------------------------------

  /** Closure externs for this file (not its parents). */
  def closureExterns: List[ClosureSource] = Nil

  /** Closure sources for this file (not its parents). */
  def closureSources: List[ClosureSource]


}
