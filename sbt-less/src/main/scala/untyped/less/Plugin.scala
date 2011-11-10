package untyped.less

import java.nio.charset.Charset
import sbt._
import sbt.Keys._
import sbt.Project.Initialize

object Plugin extends sbt.Plugin {

  object LessKeys {
    val less                = TaskKey[List[File]]("less", "Compile Less CSS sources.")
    val sourceGraph         = TaskKey[Graph]("source-graph", "List of Less CSS sources.")
    val propertiesDirectory = SettingKey[File]("properties-directory", "Directory containing properties for use in templated Less CSS sources")
    val downloadDirectory   = SettingKey[File]("download-directory", "Temporary directory to download Javascript files to")
    val prettyPrint         = SettingKey[Boolean]("minify-css", "Whether to minify the CSS output from Less")
  }
  
  import LessKeys._
  
  def sourcesTask: Initialize[Task[Seq[File]]] =
    (streams, sourceGraph in less) map {
      (out, graph) =>
        graph.sources.map(_.src)
    }
  
  def sourceGraphTask: Initialize[Task[Graph]] =
    (streams, sourceDirectory in less, resourceManaged in less, includeFilter in less, excludeFilter in less, propertiesDirectory in less, downloadDirectory in less, prettyPrint in less) map {
      (out, sourceDir, targetDir, includeFilter, excludeFilter, propertiesDir, downloadDir, prettyPrint) =>
        val graph = Graph(
          log            = out.log,
          sourceDir      = sourceDir, 
          targetDir      = targetDir,
          propertiesDir  = propertiesDir,
          downloadDir    = downloadDir,
          prettyPrint    = prettyPrint
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
      prettyPrint in less            := false,
      includeFilter in less          :=  "*.less",
      excludeFilter in less          :=  (".*" - ".") || HiddenFileFilter,
      sourceDirectory in less        <<= (sourceDirectory in conf),
      resourceManaged in less        <<= (resourceManaged in conf),
      propertiesDirectory in less    <<= (resourceDirectory in conf),
      downloadDirectory in less      <<= (target in conf) { _ / "sbt-less" / "downloads" },
      sourceGraph in less            <<= sourceGraphTask,
      unmanagedSources in less       <<= sourcesTask,
      clean in less                  <<= cleanTask,
      less                           <<= compileTask
    ))
  
  def lessSettings: Seq[Setting[_]] =
    lessSettingsIn(Compile) ++
    lessSettingsIn(Test)
    
}
