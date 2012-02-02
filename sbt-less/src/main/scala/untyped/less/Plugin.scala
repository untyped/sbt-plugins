package untyped.less

import java.nio.charset.Charset
import java.util.Properties
import sbt._
import sbt.Keys._
import sbt.Project.Initialize

object Plugin extends sbt.Plugin {

  object LessKeys {
    val less                = TaskKey[Seq[File]]("less", "Compile Less CSS sources.")
    val sourceGraph         = TaskKey[Graph]("less-source-graph", "List of Less CSS sources.")
    val templateProperties  = SettingKey[Properties]("less-template-properties", "Properties to use in Less CSS templates")
    val downloadDirectory   = SettingKey[File]("less-download-directory", "Temporary directory to download Less CSS files to")
    val prettyPrint         = SettingKey[Boolean]("less-pretty-priny", "Whether to pretty print CSS (default false)")
  }
  
  import LessKeys._
  
  def sourcesTask: Initialize[Task[Seq[File]]] =
    (streams, sourceDirectory in less, includeFilter in less, excludeFilter in less) map {
      (out, sourceDir, includeFilter, excludeFilter) =>
        sourceDir.descendentsExcept(includeFilter, excludeFilter).get
    }
  
  def sourceGraphTask: Initialize[Task[Graph]] =
    (streams, sourceDirectory in less, resourceManaged in less, unmanagedSources in less, templateProperties in less, downloadDirectory in less, prettyPrint in less) map {
      (out, sourceDir, targetDir, sourceFiles, templateProperties, downloadDir, prettyPrint) =>
        out.log.debug("sbt-less template properties " + templateProperties)
      
        val graph = Graph(
          log                = out.log,
          sourceDir          = sourceDir, 
          targetDir          = targetDir,
          templateProperties = templateProperties,
          downloadDir        = downloadDir,
          prettyPrint        = prettyPrint
        )
        
        sourceFiles.foreach(graph += _)
      
        graph
    }
  
  def compileTask =
    (streams, unmanagedSources in less, sourceGraph in less) map {
      (out, sourceFiles, graph: Graph) =>
        graph.dump
        
        sourceFiles.flatMap(graph.findSource _).filter(_.requiresRecompilation) match {
          case Nil =>
            out.log.info("No Less CSS sources requiring compilation")
            Nil
          
          case toCompile =>
            var compiled = toCompile.flatMap(_.compile)
            if(compiled.length < toCompile.length) {
              error("Some Less CSS sources could not be compiled")
            } else {
              compiled
            }
        }
    }
  
  def cleanTask =
    (streams, sourceGraph in less) map {
      (out, graph) =>
        graph.sources.foreach(_.clean)
    }

  def lessSettingsIn(conf: Configuration): Seq[Setting[_]] =
    inConfig(conf)(Seq(
      prettyPrint                :=  false,
      includeFilter in less      :=  "*.less",
      excludeFilter in less      :=  (".*" - ".") || "_*" || HiddenFileFilter,
      sourceDirectory in less    <<= (sourceDirectory in conf),
      unmanagedSources in less   <<= sourcesTask,
      resourceManaged in less    <<= (resourceManaged in conf),
      templateProperties         :=  new Properties,
      downloadDirectory          <<= (target in conf) { _ / "sbt-less" / "downloads" },
      sourceGraph                <<= sourceGraphTask,
      clean in less              <<= cleanTask,
      less                       <<= compileTask
    ))
  
  def lessSettings: Seq[Setting[_]] =
    lessSettingsIn(Compile) ++
    lessSettingsIn(Test)
    
}
