package untyped.js

import com.google.javascript.jscomp._
import java.nio.charset.Charset
import java.util.Properties
import sbt._
import sbt.Keys._
import sbt.Project.Initialize

object Plugin extends sbt.Plugin {

  object JsKeys {
    val js                     = TaskKey[List[File]]("js", "Compile Javascript sources and manifest files")
    val sourceGraph            = TaskKey[Graph]("js-source-graph", "Dependency graph of Javascript sources and manifest files")
    val charset                = SettingKey[Charset]("js-charset", "Sets the character encoding used in Javascript files (default utf-8)")
    val templateProperties     = SettingKey[Properties]("js-template-properties", "Properties to use in Javascript templates")
    val downloadDirectory      = SettingKey[File]("js-download-directory", "Temporary directory to download Javascript files to")
    val variableRenamingPolicy = SettingKey[VariableRenamingPolicy]("js-variable-renaming-policy", "Options for the Google Closure compiler")
    val prettyPrint            = SettingKey[Boolean]("js-pretty-print", "Whether to pretty print Javascript (default false)")
    val compilerOptions        = SettingKey[CompilerOptions]("js-compiler-options", "Options for the Google Closure compiler")
  }
  
  /** Provide quick access to the enum values in com.google.javascript.jscomp.VariableRenamingPolicy */
  object VariableRenamingPolicy {
    val ALL         = com.google.javascript.jscomp.VariableRenamingPolicy.ALL
    val LOCAL       = com.google.javascript.jscomp.VariableRenamingPolicy.LOCAL
    val OFF         = com.google.javascript.jscomp.VariableRenamingPolicy.OFF
    val UNSPECIFIED = com.google.javascript.jscomp.VariableRenamingPolicy.UNSPECIFIED
  }
  
  import JsKeys._
  
  def sourcesTask: Initialize[Task[Seq[File]]] =
    (streams, sourceGraph in js) map {
      (out, graph) =>
        graph.sources.map(_.src)
    }
  
  def sourceGraphTask: Initialize[Task[Graph]] =
    (streams, sourceDirectory in js, resourceManaged in js, includeFilter in js, excludeFilter in js, templateProperties, downloadDirectory, compilerOptions) map {
      (out, sourceDir, targetDir, includeFilter, excludeFilter, templateProperties, downloadDir, compilerOptions) =>
        out.log.info("JS TEMPLATE PROPERTIES " + templateProperties)
      
        val graph = Graph(
          log                = out.log,
          sourceDir          = sourceDir, 
          targetDir          = targetDir,
          templateProperties = templateProperties,
          downloadDir        = downloadDir,
          compilerOptions    = compilerOptions
        )
        
        for {
          src <- sourceDir.descendentsExcept(includeFilter, excludeFilter).get
        } graph += src
      
        graph
    }
  
  def compileTask =
    (streams, sourceGraph in js) map {
      (out, graph: Graph) =>
        graph.dump
        
        graph.sourcesRequiringRecompilation match {
          case Nil =>
            out.log.info("No Javascript sources requiring compilation")
            Nil
          
          case toCompile =>
            toCompile.flatMap(_.compile)
        }
    }
  
  def cleanTask =
    (streams, sourceGraph in js) map {
      (out, graph) =>
        graph.sources.foreach(_.clean)
    }
  
  def compilerOptionsSetting: Initialize[CompilerOptions] =
    (streams, variableRenamingPolicy in js, prettyPrint in js) apply {
      (out, variableRenamingPolicy, prettyPrint) =>
        val options = new CompilerOptions
        options.variableRenaming = variableRenamingPolicy
        options.prettyPrint = prettyPrint
        options
    }

  def jsSettingsIn(conf: Configuration): Seq[Setting[_]] =
    inConfig(conf)(Seq(
      charset                      :=  Charset.forName("utf-8"),
      includeFilter in js          :=  "*.js" || "*.jsm" || "*.jsmanifest",
      excludeFilter in js          :=  (".*" - ".") || HiddenFileFilter,
      sourceDirectory in js        <<= (sourceDirectory in conf),
      resourceManaged in js        <<= (resourceManaged in conf),
      templateProperties           :=  new Properties,
      downloadDirectory            <<= (target in conf) { _ / "sbt-js" / "downloads" },
      sourceGraph                  <<= sourceGraphTask,
      unmanagedSources in js       <<= sourcesTask,
      variableRenamingPolicy       :=  VariableRenamingPolicy.LOCAL,
      prettyPrint                  :=  false,
      compilerOptions              <<= compilerOptionsSetting,
      clean in js                  <<= cleanTask,
      js                           <<= compileTask
    )) ++
    inConfig(conf)(Seq(
      cleanFiles   <+=  resourceManaged in js,
      watchSources <++= unmanagedSources in js
    ))
  
  def jsSettings: Seq[Setting[_]] =
    jsSettingsIn(Compile) ++
    jsSettingsIn(Test)
    
}
