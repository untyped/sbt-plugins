package com.untyped.sbtjs

import com.google.javascript.jscomp.{
  CompilerOptions => ClosureOptions,
  _
}
import org.jcoffeescript.{ Option => CoffeeOption }
import java.nio.charset.Charset
import java.util.Properties
import sbt._
import sbt.Keys._
import sbt.Project.Initialize

object Plugin extends sbt.Plugin {

  object JsKeys {
    val js                     = TaskKey[Seq[File]]("js", "Compile Javascript sources and manifest files")
    val sourceGraph            = TaskKey[Graph]("js-source-graph", "Dependency graph of Javascript sources and manifest files")
    val charset                = SettingKey[Charset]("js-charset", "Sets the character encoding used in Javascript files (default utf-8)")
    val templateProperties     = SettingKey[Properties]("js-template-properties", "Properties to use in Javascript templates")
    val downloadDirectory      = SettingKey[File]("js-download-directory", "Temporary directory to download Javascript files to")
    // Coffee Script options:
    val coffeeBare             = SettingKey[Boolean]("js-coffee-bare", "Whether to omit the top-level function wrappers in coffee script (default true)")
    val coffeeOptions          = SettingKey[List[CoffeeOption]]("js-coffee-options", "Options for the Coffee Script compiler")
    // Closure Compiler options:
    val variableRenamingPolicy = SettingKey[VariableRenamingPolicy]("js-variable-renaming-policy", "Javascript variable renaming policy (default local only)")
    val prettyPrint            = SettingKey[Boolean]("js-pretty-print", "Whether to pretty print Javascript (default false)")
    val strictMode             = SettingKey[Boolean]("js-strict-mode", "Whether to strict mode Javascript (default false)")
    val optimisationLevel      = SettingKey[Int]("js-optimisation-level",  "optimisation Javascript level (0 = whitespace only, simple = 1, advanced = 2, default 1)")
    val warningLevel           = SettingKey[Int]("js-warning-level", "warning Javascript level (0 = quiet, 1 = default, 2 = verbose, default quiet)")
    val closureOptions         = SettingKey[ClosureOptions]("js-closure-options", "Options for the Google Closure compiler")
  }

  /** Provide quick access to the enum values in com.google.javascript.jscomp.VariableRenamingPolicy */
  object VariableRenamingPolicy {
    val ALL         = com.google.javascript.jscomp.VariableRenamingPolicy.ALL
    val LOCAL       = com.google.javascript.jscomp.VariableRenamingPolicy.LOCAL
    val OFF         = com.google.javascript.jscomp.VariableRenamingPolicy.OFF
    val UNSPECIFIED = com.google.javascript.jscomp.VariableRenamingPolicy.UNSPECIFIED
  }

  import JsKeys._

  def time[T](out: TaskStreams, msg: String)(func: => T): T = {
    val startTime = java.lang.System.currentTimeMillis
    val result = func
    val endTime = java.lang.System.currentTimeMillis
    out.log.debug("TIME sbt-js " + msg + ": " + (endTime - startTime) + "ms")
    result
  }

  def unmanagedSourcesTask: Initialize[Task[Seq[File]]] =
    (streams, sourceDirectories in js, includeFilter in js, excludeFilter in js) map {
      (out, sourceDirs, includeFilter, excludeFilter) =>
        time(out, "unmanagedSourcesTask") {
          out.log.debug("sourceDirectories: " + sourceDirs)
          out.log.debug("includeFilter: " + includeFilter)
          out.log.debug("excludeFilter: " + excludeFilter)

          sourceDirs.foldLeft(Seq[File]()) {
            (accum, sourceDir) =>
              accum ++ sourceDir.descendantsExcept(includeFilter, excludeFilter).get
          }
        }
    }

  def sourceGraphTask: Initialize[Task[Graph]] =
    (streams, sourceDirectories in js, resourceManaged in js, unmanagedSources in js, templateProperties, downloadDirectory, closureOptions) map {
      (out, sourceDirs, targetDir, sourceFiles, templateProperties, downloadDir, closureOptions) =>
        out.log.debug("sbt-js template properties " + templateProperties)

        time(out, "sourceGraphTask") {
          val graph = Graph(
            log                = out.log,
            sourceDirs         = sourceDirs,
            targetDir          = targetDir,
            templateProperties = templateProperties,
            downloadDir        = downloadDir,
            closureOptions    = closureOptions
          )

          sourceFiles.foreach(graph += _)

          graph
        }
    }

  def watchSourcesTask: Initialize[Task[Seq[File]]] =
    (streams, sourceGraph in js) map {
      (out, graph) =>
        graph.sources.map(_.src)
    }

  def compileTask =
    (streams, unmanagedSources in js, sourceGraph in js) map {
      (out, sourceFiles, graph: Graph) =>
        time(out, "compileTask") {
          graph.compileAll(sourceFiles)
        }
    }

  def cleanTask =
    (streams, sourceGraph in js) map {
      (out, graph) =>
        graph.sources.foreach(_.clean)
    }

  def coffeeOptionsSetting: Initialize[List[CoffeeOption]] =
    (streams, coffeeBare in js) apply {
      (out, bare) =>
        if(bare) List(CoffeeOption.BARE) else Nil
    }

  def closureOptionsSetting: Initialize[ClosureOptions] =
    (streams,
      variableRenamingPolicy in js,
      prettyPrint in js,
      strictMode in js,
      warningLevel in js,
      optimisationLevel in js
    ) apply {
      (out, variableRenamingPolicy, prettyPrint, strictMode, warningLevel, optimisationLevel) =>
        val options = new ClosureOptions

        options.variableRenaming = variableRenamingPolicy
        options.prettyPrint = prettyPrint

        optimisationLevel match {
          case 0 => CompilationLevel.WHITESPACE_ONLY.setOptionsForCompilationLevel(options)
          case 1 => CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options)
          case 2 => CompilationLevel.ADVANCED_OPTIMIZATIONS.setOptionsForCompilationLevel(options)
          case _ => CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options)
        }

        warningLevel match {
          case 0 => WarningLevel.QUIET.setOptionsForWarningLevel(options)
          case 1 => WarningLevel.DEFAULT.setOptionsForWarningLevel(options)
          case 2 => WarningLevel.VERBOSE.setOptionsForWarningLevel(options)
          case _ => WarningLevel.DEFAULT.setOptionsForWarningLevel(options)
        }

        if(strictMode) {
          options.setLanguageIn(ClosureOptions.LanguageMode.ECMASCRIPT5_STRICT)
        } else {
          options.setLanguageIn(ClosureOptions.LanguageMode.ECMASCRIPT5)
        }

        options
    }

  def jsSettingsIn(conf: Configuration): Seq[Setting[_]] =
    inConfig(conf)(Seq(
      charset                  := Charset.forName("utf-8"),
      includeFilter in js      := "*.js" || "*.jsm" || "*.jsmanifest" || "*.coffee",
      excludeFilter in js      := (".*" - ".") || "_*" || HiddenFileFilter,
      sourceDirectory in js   <<= (sourceDirectory in conf),
      sourceDirectories in js <<= (sourceDirectory in (conf, js)) { Seq(_) },
      unmanagedSources in js  <<= unmanagedSourcesTask,
      resourceManaged in js   <<= (resourceManaged in conf),
      templateProperties       := new Properties,
      downloadDirectory       <<= (target in conf) { _ / "sbt-js" / "downloads" },
      sourceGraph             <<= sourceGraphTask,
      sources in js           <<= watchSourcesTask,
      watchSources in js      <<= watchSourcesTask,
      coffeeBare               := false,
      coffeeOptions           <<= coffeeOptionsSetting,
      variableRenamingPolicy   := VariableRenamingPolicy.LOCAL,
      prettyPrint              := false,
      strictMode               := false,
      warningLevel             := 0,
      optimisationLevel        := 1,
      closureOptions          <<= closureOptionsSetting,
      clean in js             <<= cleanTask,
      js                      <<= compileTask
    )) ++ Seq(
      cleanFiles              <+= (resourceManaged in js in conf),
      watchSources           <++= (watchSources in js in conf)
    )

  def jsSettings: Seq[Setting[_]] =
    jsSettingsIn(Compile) ++
    jsSettingsIn(Test)

}
