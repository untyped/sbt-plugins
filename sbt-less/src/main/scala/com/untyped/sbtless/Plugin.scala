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
    val result = func
    val endTime = java.lang.System.currentTimeMillis
    out.log.debug("TIME sbt-less " + msg + ": " + (endTime - startTime) + "ms")
    result
  }

  def unmanagedSourcesTask = // : Def.Initialize[Task[Seq[File]]] =
    (streams, sourceDirectories in less, includeFilter in less, excludeFilter in less) map {
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
    (streams, sourceDirectories in less, resourceManaged in less, unmanagedSources in less, templateProperties in less, downloadDirectory in less, filenameSuffix in less, prettyPrint in less, lessVersion in less, useCommandLine in less) map {
      (out, sourceDirs, targetDir, sourceFiles, templateProperties, downloadDir, filenameSuffix, prettyPrint, lessVersion, useCommandLine) =>
        time(out, "sourceGraphTask") {
          out.log.debug("sbt-less template properties " + templateProperties)

          val graph = Graph(
            log                = out.log,
            sourceDirs         = sourceDirs,
            targetDir          = targetDir,
            templateProperties = templateProperties,
            downloadDir        = downloadDir,
            filenameSuffix     = filenameSuffix,
            lessVersion        = lessVersion,
            prettyPrint        = prettyPrint,
            useCommandLine     = useCommandLine
          )

          sourceFiles.foreach(graph += _)

          graph
        }
    }

  def watchSourcesTask = // : Def.Initialize[Task[Seq[File]]] =
    (streams, sourceGraph in less) map {
      (out, graph) =>
        graph.sources.map(_.src) : Seq[File]
    }

  def compileTask =
    (streams, unmanagedSources in less, sourceGraph in less) map {
      (out, sourceFiles, graph: Graph) =>
        time(out, "compileTask") {
          graph.compileAll(sourceFiles)
        }
    }

  def cleanTask =
    (streams, sourceGraph in less) map {
      (out, graph) =>
        graph.sources.foreach(_.clean())
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
