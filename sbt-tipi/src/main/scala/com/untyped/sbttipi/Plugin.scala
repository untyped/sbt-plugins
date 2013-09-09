package com.untyped.sbttipi

import java.nio.charset.Charset
import java.util.Properties
import sbt._
import sbt.Keys._
import sbt.Project.Initialize
import tipi.core._

object Plugin extends sbt.Plugin {

  object TipiKeys {
    val tipi              = TaskKey[Seq[File]]("tipi", "Compile Tipi sources and manifest files")
    val sourceGraph       = TaskKey[Graph]("tipi-source-graph", "Dependency graph of Tipi sources and manifest files")
    val charset           = SettingKey[Charset]("tipi-charset", "Sets the character encoding used in Tipi files (default utf-8)")
    val environment       = SettingKey[Env]("tipi-environment", "Global environment to use for Tipi processing")
    val downloadDirectory = SettingKey[File]("tipi-download-directory", "Temporary directory to download Tipi files to")
  }

  import TipiKeys._

  def unmanagedSourcesTask: Initialize[Task[Seq[File]]] =
    (streams, sourceDirectories in tipi, includeFilter in tipi, excludeFilter in tipi) map {
      (out, sourceDirs, includeFilter, excludeFilter) =>
        out.log.debug("sourceDirectories: " + sourceDirs)
        out.log.debug("includeFilter: " + includeFilter)
        out.log.debug("excludeFilter: " + excludeFilter)

        sourceDirs.foldLeft(Seq[File]()) {
          (accum, sourceDir) =>
            accum ++ com.untyped.sbtgraph.Descendents(sourceDir, includeFilter, excludeFilter).get
        }
    }

  def sourceGraphTask: Initialize[Task[Graph]] =
    (streams, sourceDirectories in tipi, resourceManaged in tipi, unmanagedSources in tipi, environment, downloadDirectory) map {
      (out, sourceDirs, targetDir, sourceFiles, environment, downloadDir) =>
        val graph = Graph(
          log         = out.log,
          sourceDirs  = sourceDirs,
          targetDir   = targetDir,
          environment = environment,
          downloadDir = downloadDir
        )

        sourceFiles.foreach(graph += _)

        graph
    }

  def watchSourcesTask: Initialize[Task[Seq[File]]] =
    (streams, sourceGraph in tipi) map {
      (out, graph) =>
        graph.sources.map(_.src)
    }

  def compileTask =
    (streams, unmanagedSources in tipi, sourceGraph in tipi) map {
      (out, sourceFiles, graph: Graph) =>
        graph.compileAll(sourceFiles)
    }

  def cleanTask =
    (streams, sourceGraph in tipi) map {
      (out, graph) =>
        graph.sources.foreach(_.clean)
    }

  def tipiSettingsIn(conf: Configuration): Seq[Setting[_]] =
    inConfig(conf)(Seq(
      charset                   :=   Charset.forName("utf-8"),
      includeFilter in tipi     :=   "*.html",
      excludeFilter in tipi     :=   (".*" - ".") || "_*" || HiddenFileFilter,
      sourceDirectory in tipi   <<=  (sourceDirectory in conf),
      sourceDirectories in tipi <<=  (sourceDirectory in (conf, tipi)) { Seq(_) },
      unmanagedSources in tipi  <<=  unmanagedSourcesTask,
      resourceManaged in tipi   <<=  (resourceManaged in conf),
      environment               :=   Env.Basic,
      // templateProperties        :=   new Properties,
      downloadDirectory         <<=  (target in conf) { _ / "sbt-tipi" / "downloads" },
      sourceGraph               <<=  sourceGraphTask,
      sources in tipi           <<=  watchSourcesTask,
      watchSources in tipi      <<=  watchSourcesTask,
      clean in tipi             <<=  cleanTask,
      tipi                      <<=  compileTask
    )) ++ Seq(
      cleanFiles                <+=  (resourceManaged in tipi in conf),
      watchSources              <++= (watchSources    in tipi in conf)
    )

  def tipiSettings: Seq[Setting[_]] =
    tipiSettingsIn(Compile) ++
    tipiSettingsIn(Test)

}
