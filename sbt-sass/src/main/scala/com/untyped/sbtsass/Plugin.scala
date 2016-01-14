package com.untyped.sbtsass

import java.util.Properties
import sbt._
import sbt.Keys._

object Plugin extends sbt.Plugin {

  object SassKeys {
    val sass                = TaskKey[Seq[File]]("sass", "Compile Sass CSS sources.")
    val sourceGraph         = TaskKey[Graph]("sass-source-graph", "List of Sass CSS sources.")
    val templateProperties  = SettingKey[Properties]("sass-template-properties", "Properties to use in Sass CSS templates")
    val downloadDirectory   = SettingKey[File]("sass-download-directory", "Temporary directory to download Sass CSS files to")
    val filenameSuffix      = SettingKey[String]("sass-filename-suffix", "Suffix to append to the output file names before '.css'")
    val prettyPrint         = SettingKey[Boolean]("sass-pretty-print", "Whether to pretty print CSS (default false)")
    val sassVersion         = SettingKey[SassVersion]("sass-version", "The version of the Sass CSS compiler to use")
    val useCommandLine      = SettingKey[Boolean]("sass-use-command-line", "Use the Sass CSS command line script instead of Rhino")
    val sassOutputStyle     = SettingKey[Symbol]("sass-output-style", "Sets output style used when compiling")
  }

  sealed trait SassVersion {
    val version: String
    override def toString = version
  }

  object SassVersion {
    val Sass3214   = new SassVersion { val version = "3.2.14" }
    val Sass332 = new SassVersion { val version = "3.3.2" }
  }

  import SassKeys._

  def time[T](out: TaskStreams, msg: String)(func: => T): T = {
    val startTime = java.lang.System.currentTimeMillis
    val result    = func
    val endTime   = java.lang.System.currentTimeMillis

    out.log.debug("TIME sbt-sass " + msg + ": " + (endTime - startTime) + "ms")
    result
  }

  def unmanagedSourcesTask = Def.task {
    val _out           = (streams).value
    val _sourceDirs    = (sourceDirectories in sass).value
    val _includeFilter = (includeFilter in sass).value
    val _excludeFilter = (excludeFilter in sass).value

    time(_out, "unmanagedSourcesTask") {
      _out.log.debug("sourceDirectories: " + _sourceDirs)
      _out.log.debug("includeFilter: " + _includeFilter)
      _out.log.debug("excludeFilter: " + _excludeFilter)

      _sourceDirs.foldLeft(Seq[File]()) { (accum, sourceDir) =>
        import com.untyped.sbtgraph.Descendents

        accum ++ Descendents(sourceDir, _includeFilter, _excludeFilter).get
      }
    }
  }

  def sourceGraphTask = Def.task {
    val _out                = (streams).value
    val _sourceDirs         = (sourceDirectories in sass).value
    val _targetDir          = (resourceManaged in sass).value
    val _sourceFiles        = (unmanagedSources in sass).value
    val _templateProperties = (templateProperties in sass).value
    val _downloadDir        = (downloadDirectory in sass).value
    val _filenameSuffix     = (filenameSuffix in sass).value
    val _prettyPrint        = (prettyPrint in sass).value
    val _sassVersion        = (sassVersion in sass).value
    val _useCommandLine     = (useCommandLine in sass).value
    val _sassOutputStyle    = (sassOutputStyle in sass).value

    time(_out, "sourceGraphTask") {
      _out.log.debug("sbt-sass template properties " + _templateProperties)

      val graph = Graph(
        log                = _out.log,
        sourceDirs         = _sourceDirs,
        targetDir          = _targetDir,
        templateProperties = _templateProperties,
        downloadDir        = _downloadDir,
        filenameSuffix     = _filenameSuffix,
        sassVersion        = _sassVersion,
        prettyPrint        = _prettyPrint,
        useCommandLine     = _useCommandLine,
        compilerOptions    = Map(":style" -> (":" + _sassOutputStyle.name))
      )

      _sourceFiles.foreach(graph += _)

      graph
    }
  }

  def watchSourcesTask = Def.task {
    val _out   = (streams).value
    val _graph = (sourceGraph in sass).value

    _graph.sources.map(_.src) : Seq[File]
  }

  def compileTask = Def.task {
    val _out         = (streams).value
    val _sourceFiles = (unmanagedSources in sass).value
    val _graph       = (sourceGraph in sass).value

    time(_out, "compileTask") {
      _graph.compileAll(_sourceFiles.filterNot(_.getName.startsWith("_")))
    }
  }

  def cleanTask = Def.task {
    val _out   = (streams).value
    val _graph = (sourceGraph in sass).value

    _graph.sources.foreach(_.clean())
  }

  def sassSettingsIn(conf: Configuration): Seq[Setting[_]] = {
    inConfig(conf)(Seq(
      prettyPrint                  :=  false,
      includeFilter in sass        :=  "*.sass" || "*.scss",
      excludeFilter in sass        :=  (".*" - ".") || HiddenFileFilter,
      sassVersion in sass          :=  SassVersion.Sass332,
      sassOutputStyle in sass      :=  'nested,
      useCommandLine in sass       :=  false,
      sourceDirectory in sass      <<= (sourceDirectory in conf),
      sourceDirectories in sass    <<= (sourceDirectory in (conf, sass)) { Seq(_) },
      unmanagedSources in sass     <<= unmanagedSourcesTask,
      resourceManaged in sass      <<= (resourceManaged in conf),
      templateProperties           :=  new Properties,
      downloadDirectory            <<= (target in conf) { _ / "sbt-sass" / "downloads" },
      filenameSuffix               := "",
      sourceGraph                  <<= sourceGraphTask,
      sources in sass              <<= watchSourcesTask,
      watchSources in sass         <<= watchSourcesTask,
      clean in sass                <<= cleanTask,
      sass                         <<= compileTask
    )) ++ Seq(
      cleanFiles                   <+=  (resourceManaged in sass in conf),
      watchSources                 <++= (watchSources in sass in conf)
    )
  }

  def sassSettings: Seq[Setting[_]] =
    sassSettingsIn(Compile) ++
    sassSettingsIn(Test)

}
