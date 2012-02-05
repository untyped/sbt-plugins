package untyped.js

import com.google.javascript.jscomp
import com.google.javascript.jscomp._
import sbt._
import scala.collection._

trait Source extends untyped.graph.Source {
  
  type S = untyped.js.Source
  type G = untyped.js.Graph

  def compile: Option[File] = {
    val des = this.des getOrElse (throw new Exception("Could not determine destination filename for " + src))
    
    graph.log.info("Compiling %s source %s".format(graph.pluginName, des))
    
    val compiler = new jscomp.Compiler
    
    jscomp.Compiler.setLoggingLevel(graph.closureLogLevel)
    
    val myExterns = graph.closureExterns(this)
    val mySources = graph.closureSources(this)
    
    graph.log.debug("  externs:")
    myExterns.foreach(x => graph.log.debug("    " + x)) 

    graph.log.debug("  sources:")
    mySources.foreach(x => graph.log.debug("    " + x)) 
    
    val result =
      compiler.compile(
        myExterns.toArray,
        mySources.toArray,
        graph.compilerOptions)
    
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
      
      IO.createDirectory(new File(des.getParent))
      IO.write(des, compiler.toSource)
      
      Some(des)
    }
  }

  // Helpers ------------------------------------
  
  /** Closure externs for this file (not its parents). */
  def closureExterns: List[JSSourceFile] = Nil
  
  /** Closure sources for this file (not its parents). */
  def closureSources: List[JSSourceFile]
  
  
}
