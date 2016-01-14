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
    val filenameSuffix         = SettingKey[String]("js-filename-suffix", "Suffix to append to the output file names before '.js'")
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

  def unmanagedSourcesTask = Def.task {
    val _out           = (streams).value
    val _sourceDirs    = (sourceDirectories in js).value
    val _includeFilter = (includeFilter in js).value
    val _excludeFilter = (excludeFilter in js).value

    time(_out, "unmanagedSourcesTask") {
      _out.log.debug("sourceDirectories: " + _sourceDirs)
      _out.log.debug("includeFilter: " + _includeFilter)
      _out.log.debug("excludeFilter: " + _excludeFilter)

      _sourceDirs.foldLeft(Seq[File]()) { (accum, sourceDir) =>
        import com.untyped.sbtgraph.Descendents

        accum ++ com.untyped.sbtgraph.Descendents(sourceDir, _includeFilter, _excludeFilter).get
      }
    }
  }

  def sourceGraphTask = Def.task {
    val _out                = (streams).value
    val _sourceDirs         = (sourceDirectories in js).value
    val _targetDir          = (resourceManaged in js).value
    val _sourceFiles        = (unmanagedSources in js).value
    val _templateProperties = (templateProperties).value
    val _downloadDir        = (downloadDirectory).value
    val _filenameSuffix     = (filenameSuffix).value
    val _coffeeVersion      = (coffeeVersion).value
    val _coffeeOptions      = (coffeeOptions).value
    val _closureOptions     = (closureOptions).value

    _out.log.debug("sbt-js template properties " + _templateProperties)

    time(_out, "sourceGraphTask") {
      val graph = Graph(
        log                = _out.log,
        sourceDirs         = _sourceDirs,
        targetDir          = _targetDir,
        templateProperties = _templateProperties,
        downloadDir        = _downloadDir,
        filenameSuffix     = _filenameSuffix,
        coffeeVersion      = _coffeeVersion,
        coffeeOptions      = _coffeeOptions,
        closureOptions     = _closureOptions
      )

      _sourceFiles.foreach(graph += _)

      graph
    }
  }

  def watchSourcesTask = Def.task {
    val _out = (streams).value
    val _graph = (sourceGraph in js).value

    _graph.sources.map(_.src) : Seq[File]
  }

  def compileTask = Def.task {
    val _out         = (streams).value
    val _sourceFiles = (unmanagedSources in js).value
    val _graph       = (sourceGraph in js).value

    time(_out, "compileTask") {
      _graph.compileAll(_sourceFiles)
    }
  }

  def cleanTask = Def.task {
    val _out   = (streams).value
    val _graph = (sourceGraph in js).value

    _graph.sources.foreach(_.clean())
  }

  def coffeeOptionsSetting = Def.setting {
    val _bare = (coffeeBare in js).value

    if(_bare) List(CoffeeOption.BARE) else Nil
  }

  def closureOptionsSetting = Def.setting {
    val _variableRenamingPolicy = (variableRenamingPolicy in js).value
    val _prettyPrint            = (prettyPrint in js).value
    val _strictMode             = (strictMode in js).value
    val _warningLevel           = (warningLevel in js).value
    val _compilationLevel       = (compilationLevel in js).value

    val options = new ClosureOptions

    _compilationLevel.setOptionsForCompilationLevel(options)
    _warningLevel.setOptionsForWarningLevel(options)

    options.variableRenaming = _variableRenamingPolicy
    options.prettyPrint      = _prettyPrint

    if(_strictMode) {
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
      filenameSuffix           := "",
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
