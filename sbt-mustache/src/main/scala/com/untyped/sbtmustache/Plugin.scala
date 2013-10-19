package com.untyped.sbtmustache

import java.nio.charset.Charset
import java.util.Properties
import sbt._
import sbt.Keys._

object Plugin extends sbt.Plugin {

  object MustacheKeys {
    val mustache               = TaskKey[Seq[File]]("mustache", "Compile Mustache sources and manifest files")
    val sourceGraph            = TaskKey[Graph]("mustache-source-graph", "Dependency graph of Mustache sources and manifest files")
    val charset                = SettingKey[Charset]("mustache-charset", "Sets the character encoding used in Mustache files (default utf-8)")
    val templateProperties     = SettingKey[Properties]("mustache-template-properties", "Properties to use in Mustache templates")
    val downloadDirectory      = SettingKey[File]("mustache-download-directory", "Temporary directory to download Mustache files to")
  }

  import MustacheKeys._

  def unmanagedSourcesTask: Def.Initialize[Task[Seq[File]]] =
    (streams, sourceDirectories in mustache, includeFilter in mustache, excludeFilter in mustache) map {
      (out, sourceDirs, includeFilter, excludeFilter) =>
        out.log.debug("sourceDirectories: " + sourceDirs)
        out.log.debug("includeFilter: " + includeFilter)
        out.log.debug("excludeFilter: " + excludeFilter)

        sourceDirs.foldLeft(Seq[File]()) {
          (accum, sourceDir) =>
            accum ++ sourceDir.descendantsExcept(includeFilter, excludeFilter).get
        }
    }

  def sourceGraphTask: Def.Initialize[Task[Graph]] =
    (streams, sourceDirectories in mustache, resourceManaged in mustache, unmanagedSources in mustache, templateProperties, downloadDirectory) map {
      (out, sourceDirs, targetDir, sourceFiles, templateProperties, downloadDir) =>
        out.log.debug("sbt-mustache template properties " + templateProperties)

        val graph = Graph(
          log                = out.log,
          sourceDirs         = sourceDirs,
          targetDir          = targetDir,
          templateProperties = templateProperties,
          downloadDir        = downloadDir
        )

        sourceFiles.foreach(graph +=)

        graph
    }

  def watchSourcesTask: Def.Initialize[Task[Seq[File]]] =
    (streams, sourceGraph in mustache) map {
      (out, graph) =>
        graph.sources.map(_.src)
    }

  def compileTask =
    (streams, unmanagedSources in mustache, sourceGraph in mustache) map {
      (out, sourceFiles, graph: Graph) =>
        graph.compileAll(sourceFiles)
    }

  def cleanTask =
    (streams, sourceGraph in mustache) map {
      (out, graph) =>
        graph.sources.foreach(_.clean())
    }

  def mustacheSettingsIn(conf: Configuration): Seq[Setting[_]] =
    inConfig(conf)(Seq(
      charset                       :=   Charset.forName("utf-8"),
      includeFilter in mustache     :=   "*.html",
      excludeFilter in mustache     :=   (".*" - ".") || "_*" || HiddenFileFilter,
      sourceDirectory in mustache   <<=  (sourceDirectory in conf),
      sourceDirectories in mustache <<=  (sourceDirectory in (conf, mustache)) { Seq(_) },
      unmanagedSources in mustache  <<=  unmanagedSourcesTask,
      resourceManaged in mustache   <<=  (resourceManaged in conf),
      templateProperties            :=   new Properties,
      downloadDirectory             <<=  (target in conf) { _ / "sbt-mustache" / "downloads" },
      sourceGraph                   <<=  sourceGraphTask,
      sources in mustache           <<=  watchSourcesTask,
      watchSources in mustache      <<=  watchSourcesTask,
      clean in mustache             <<=  cleanTask,
      mustache                      <<=  compileTask
    )) ++ Seq(
      cleanFiles                    <+=  (resourceManaged in mustache in conf),
      watchSources                  <++= (watchSources    in mustache in conf)
    )

  def mustacheSettings: Seq[Setting[_]] =
    mustacheSettingsIn(Compile) ++
    mustacheSettingsIn(Test)

}
