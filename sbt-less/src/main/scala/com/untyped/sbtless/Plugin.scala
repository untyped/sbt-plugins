package com.untyped.sbtless

import java.util.Properties
import sbt._
import sbt.Keys._

object Plugin extends sbt.Plugin {

  object LessKeys {
    val less                = TaskKey[Seq[File]]("less", "Compile Less CSS sources.")
    val sourceGraph         = TaskKey[Graph]("less-source-graph", "List of Less CSS sources.")
    val templateProperties  = SettingKey[Properties]("less-template-properties", "Properties to use in Less CSS templates")
    val downloadDirectory   = SettingKey[File]("less-download-directory", "Temporary directory to download Less CSS files to")
    val filenameSuffix      = SettingKey[String]("less-filename-suffix", "Suffix to append to the output file names before '.css'")
    val prettyPrint         = SettingKey[Boolean]("less-pretty-print", "Whether to pretty print CSS (default false)")
    val lessVersion         = SettingKey[LessVersion]("less-version", "The version of the Less CSS compiler to use")
    val useCommandLine      = SettingKey[Boolean]("less-use-command-line", "Use the Less CSS command line script instead of Rhino")
  }

  sealed trait LessVersion {
    val filename: String
    lazy val url = "/" + filename
    lazy val envjsFilename = "env.rhino.1.2.js"
    lazy val envjsUrl = "/" + envjsFilename
  }

  object LessVersion {
    val Less113 = new LessVersion { val filename = "less-rhino-1.1.3.js" }
    val Less115 = new LessVersion { val filename = "less-rhino-1.1.5.js" }
    val Less130 = new LessVersion { val filename = "less-1.3.0.js" }
    val Less133 = new LessVersion { val filename = "less-1.3.3.js" }
    val Less142 = new LessVersion { val filename = "less-1.4.2.js" }
  }

  import LessKeys._

  def time[T](out: TaskStreams, msg: String)(func: => T): T = {
    val startTime = java.lang.System.currentTimeMillis
    val result    = func
    val endTime   = java.lang.System.currentTimeMillis

    out.log.debug("TIME sbt-less " + msg + ": " + (endTime - startTime) + "ms")

    result
  }

  def unmanagedSourcesTask = Def.task {
    val _out           = (streams).value
    val _sourceDirs    = (sourceDirectories in less).value
    val _includeFilter = (includeFilter in less).value
    val _excludeFilter = (excludeFilter in less).value

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
    val _sourceDirs         = (sourceDirectories in less).value
    val _targetDir          = (resourceManaged in less).value
    val _sourceFiles        = (unmanagedSources in less).value
    val _templateProperties = (templateProperties in less).value
    val _downloadDir        = (downloadDirectory in less).value
    val _filenameSuffix     = (filenameSuffix in less).value
    val _prettyPrint        = (prettyPrint in less).value
    val _lessVersion        = (lessVersion in less).value
    val _useCommandLine     = (useCommandLine in less).value

    time(_out, "sourceGraphTask") {
      _out.log.debug("sbt-less template properties " + _templateProperties)

      val graph = Graph(
        log                = _out.log,
        sourceDirs         = _sourceDirs,
        targetDir          = _targetDir,
        templateProperties = _templateProperties,
        downloadDir        = _downloadDir,
        filenameSuffix     = _filenameSuffix,
        lessVersion        = _lessVersion,
        prettyPrint        = _prettyPrint,
        useCommandLine     = _useCommandLine
      )

      _sourceFiles.foreach(graph += _)

      graph
    }
  }

  def watchSourcesTask = Def.task {
    val _out   = (streams).value
    val _graph = (sourceGraph in less).value

    _graph.sources.map(_.src) : Seq[File]
  }

  def compileTask = Def.task {
    val _out         = (streams).value
    val _sourceFiles = (unmanagedSources in less).value
    val _graph       = (sourceGraph in less).value

    time(_out, "compileTask") {
      _graph.compileAll(_sourceFiles)
    }
  }

  def cleanTask = Def.task {
    val _out   = (streams).value
    val _graph = (sourceGraph in less).value

    _graph.sources.foreach(_.clean())
  }

  def lessSettingsIn(conf: Configuration): Seq[Setting[_]] = {
    inConfig(conf)(Seq(
      prettyPrint                  :=  false,
      includeFilter in less        :=  "*.less",
      excludeFilter in less        :=  (".*" - ".") || "_*" || HiddenFileFilter,
      lessVersion in less          :=  LessVersion.Less142,
      useCommandLine in less       :=  false,
      sourceDirectory in less      <<= (sourceDirectory in conf),
      sourceDirectories in less    <<= (sourceDirectory in (conf, less)) { Seq(_) },
      unmanagedSources in less     <<= unmanagedSourcesTask,
      resourceManaged in less      <<= (resourceManaged in conf),
      templateProperties           :=  new Properties,
      downloadDirectory            <<= (target in conf) { _ / "sbt-less" / "downloads" },
      filenameSuffix               := "",
      sourceGraph                  <<= sourceGraphTask,
      sources in less              <<= watchSourcesTask,
      watchSources in less         <<= watchSourcesTask,
      clean in less                <<= cleanTask,
      less                         <<= compileTask
    )) ++ Seq(
      cleanFiles                   <+=  (resourceManaged in less in conf),
      watchSources                 <++= (watchSources in less in conf)
    )
  }

  def lessSettings: Seq[Setting[_]] =
    lessSettingsIn(Compile) ++
    lessSettingsIn(Test)

}
