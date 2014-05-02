package com.untyped.sbtjs

import com.google.javascript.jscomp.{ CompilerOptions => ClosureOptions, _ }
import org.jcoffeescript.{ Option => CoffeeOption }
import java.nio.charset.Charset
import java.util.Properties
import sbt._
import sbt.Keys._

object Plugin extends sbt.Plugin {

  object JsKeys {
    val js                     = TaskKey[Seq[File]]("js", "Compile Javascript sources and manifest files")
    val sourceGraph            = TaskKey[Graph]("js-source-graph", "Dependency graph of Javascript sources and manifest files")
    val charset                = SettingKey[Charset]("js-charset", "Sets the character encoding used in Javascript files (default utf-8)")
    val templateProperties     = SettingKey[Properties]("js-template-properties", "Properties to use in Javascript templates")
    val downloadDirectory      = SettingKey[File]("js-download-directory", "Temporary directory to download Javascript files to")
    // Coffee Script options:
    val coffeeVersion          = SettingKey[CoffeeVersion]("coffee-version", "The version of the Coffeescript compiler to use")
    val coffeeBare             = SettingKey[Boolean]("js-coffee-bare", "Whether to omit the top-level function wrappers in coffee script (default true)")
    val coffeeOptions          = SettingKey[List[CoffeeOption]]("js-coffee-options", "Options for the Coffee Script compiler")
    // Closure Compiler options:
    val variableRenamingPolicy = SettingKey[VariableRenamingPolicy]("js-variable-renaming-policy", "Javascript variable renaming policy (default local only)")
    val prettyPrint            = SettingKey[Boolean]("js-pretty-print", "Whether to pretty print Javascript (default false)")
    val strictMode             = SettingKey[Boolean]("js-strict-mode", "Whether to strict mode Javascript (default false)")
    val compilationLevel       = SettingKey[CompilationLevel]("js-compilation-level",  "Closure Compiler compilation level")
    val warningLevel           = SettingKey[WarningLevel]("js-warning-level", "Closure Compiler warning level")
    val closureOptions         = SettingKey[ClosureOptions]("js-closure-options", "Options for the Google Closure compiler")
  }

  sealed trait CoffeeVersion { def url: String }

  object CoffeeVersion {
    val Coffee110 = new CoffeeVersion { val url = "org/jcoffeescript/coffee-script-1.1.0.js" }
    val Coffee161 = new CoffeeVersion { val url = "org/jcoffeescript/coffee-script-1.6.1.js" }
  }

  /** Provide quick access to the enum values in com.google.javascript.jscomp.VariableRenamingPolicy */
  object VariableRenamingPolicy {
    val ALL         = com.google.javascript.jscomp.VariableRenamingPolicy.ALL
    val LOCAL       = com.google.javascript.jscomp.VariableRenamingPolicy.LOCAL
    val OFF         = com.google.javascript.jscomp.VariableRenamingPolicy.OFF
    val UNSPECIFIED = com.google.javascript.jscomp.VariableRenamingPolicy.UNSPECIFIED
  }

  object WarningLevel {
    val QUIET   = com.google.javascript.jscomp.WarningLevel.QUIET
    val DEFAULT = com.google.javascript.jscomp.WarningLevel.DEFAULT
    val VERBOSE = com.google.javascript.jscomp.WarningLevel.VERBOSE
  }

  object CompilationLevel {
    val WHITESPACE_ONLY        = com.google.javascript.jscomp.CompilationLevel.WHITESPACE_ONLY
    val SIMPLE_OPTIMIZATIONS   = com.google.javascript.jscomp.CompilationLevel.SIMPLE_OPTIMIZATIONS
    val ADVANCED_OPTIMIZATIONS = com.google.javascript.jscomp.CompilationLevel.ADVANCED_OPTIMIZATIONS
  }

  import JsKeys._

  def time[T](out: TaskStreams, msg: String)(func: => T): T = {
    val startTime = java.lang.System.currentTimeMillis
    val result = func
    val endTime = java.lang.System.currentTimeMillis
    out.log.debug("TIME sbt-js " + msg + ": " + (endTime - startTime) + "ms")
    result
  }

  def unmanagedSourcesTask = // : Def.Initialize[Task[Seq[File]]] =
    (streams, sourceDirectories in js, includeFilter in js, excludeFilter in js) map {
      (out, sourceDirs, includeFilter, excludeFilter) =>
        time(out, "unmanagedSourcesTask") {
          out.log.debug("sourceDirectories: " + sourceDirs)
          out.log.debug("includeFilter: " + includeFilter)
          out.log.debug("excludeFilter: " + excludeFilter)

          sourceDirs.foldLeft(Seq[File]()) {
            (accum, sourceDir) =>
              accum ++ com.untyped.sbtgraph.Descendents(sourceDir, includeFilter, excludeFilter).get
          }
        }
    }

  def sourceGraphTask = // : Def.Initialize[Task[Graph]] =
    (streams, sourceDirectories in js, resourceManaged in js, unmanagedSources in js, templateProperties, downloadDirectory, coffeeVersion, coffeeOptions, closureOptions) map {
      (out, sourceDirs, targetDir, sourceFiles, templateProperties, downloadDir, coffeeVersion, coffeeOptions, closureOptions) =>
        out.log.debug("sbt-js template properties " + templateProperties)

        time(out, "sourceGraphTask") {
          val graph = Graph(
            log                = out.log,
            sourceDirs         = sourceDirs,
            targetDir          = targetDir,
            templateProperties = templateProperties,
            downloadDir        = downloadDir,
            coffeeVersion      = coffeeVersion,
            coffeeOptions      = coffeeOptions,
            closureOptions     = closureOptions
          )

          sourceFiles.foreach(graph += _)

          graph
        }
    }

  def watchSourcesTask = // : Def.Initialize[Task[Seq[File]]] =
    (streams, sourceGraph in js) map {
      (out, graph) =>
        graph.sources.map(_.src) : Seq[File]
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
        graph.sources.foreach(_.clean())
    }

  def coffeeOptionsSetting = // : Def.Initialize[List[CoffeeOption]] =
    (streams, coffeeBare in js) apply {
      (out, bare) =>
        if(bare) List(CoffeeOption.BARE) else Nil
    }

  def closureOptionsSetting = // : Def.Initialize[ClosureOptions] =
    (streams,
      variableRenamingPolicy in js,
      prettyPrint in js,
      strictMode in js,
      warningLevel in js,
      compilationLevel in js
    ) apply {
      (out, variableRenamingPolicy, prettyPrint, strictMode, warningLevel, compilationLevel) =>
        val options = new ClosureOptions

        compilationLevel.setOptionsForCompilationLevel(options)
        warningLevel.setOptionsForWarningLevel(options)

        options.variableRenaming = variableRenamingPolicy
        options.prettyPrint = prettyPrint

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
      coffeeVersion            := CoffeeVersion.Coffee161,
      coffeeBare               := false,
      coffeeOptions           <<= coffeeOptionsSetting,
      variableRenamingPolicy   := VariableRenamingPolicy.LOCAL,
      prettyPrint              := false,
      strictMode               := false,
      warningLevel             := WarningLevel.QUIET,
      compilationLevel         := CompilationLevel.SIMPLE_OPTIMIZATIONS,
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
