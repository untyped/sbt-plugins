package untyped.less

import java.nio.charset.Charset
import java.util.Properties
import sbt._
import sbt.Keys._
import sbt.Project.Initialize

object Plugin extends sbt.Plugin {

  object LessKeys {
    val less                = TaskKey[List[File]]("less", "Compile Less CSS sources.")
    val sourceGraph         = TaskKey[Graph]("less-source-graph", "List of Less CSS sources.")
    val templateProperties  = SettingKey[Properties]("less-template-properties", "Properties to use in Less CSS templates")
    val downloadDirectory   = SettingKey[File]("less-download-directory", "Temporary directory to download Less CSS files to")
    val prettyPrint         = SettingKey[Boolean]("less-pretty-priny", "Whether to pretty print CSS (default false)")
  }
  
  import LessKeys._
  
  def sourcesTask: Initialize[Task[Seq[File]]] =
    (streams, sourceGraph in less) map {
      (out, graph) =>
        graph.sources.map(_.src)
    }
  
  def sourceGraphTask: Initialize[Task[Graph]] =
    (streams, sourceDirectory in less, resourceManaged in less, includeFilter in less, excludeFilter in less, templateProperties in less, downloadDirectory in less, prettyPrint in less) map {
      (out, sourceDir, targetDir, includeFilter, excludeFilter, templateProperties, downloadDir, prettyPrint) =>
        out.log.debug("sbt-less template properties " + templateProperties)
      
        val graph = Graph(
          log                = out.log,
          sourceDir          = sourceDir, 
          targetDir          = targetDir,
          templateProperties = templateProperties,
          downloadDir        = downloadDir,
          prettyPrint        = prettyPrint
        )
        
        for {
          src <- sourceDir.descendentsExcept(includeFilter, excludeFilter).get
        } graph += src
      
        graph
    }
  
  def compileTask =
    (streams, sourceGraph in less) map {
      (out, graph: Graph) =>
        graph.dump
        
        graph.sourcesRequiringRecompilation match {
          case Nil =>
            out.log.info("No Less CSS sources requiring compilation")
            Nil
          
          case toCompile =>
            toCompile.flatMap(_.compile)
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
      excludeFilter in less      :=  (".*" - ".") || HiddenFileFilter,
      sourceDirectory in less    <<= (sourceDirectory in conf),
      resourceManaged in less    <<= (resourceManaged in conf),
      templateProperties         :=  new Properties,
      downloadDirectory          <<= (target in conf) { _ / "sbt-less" / "downloads" },
      sourceGraph                <<= sourceGraphTask,
      unmanagedSources in less   <<= sourcesTask,
      clean in less              <<= cleanTask,
      less                       <<= compileTask
    ))
  
  def lessSettings: Seq[Setting[_]] =
    lessSettingsIn(Compile) ++
    lessSettingsIn(Test)
    
}
