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
    val filenameSuffix         = SettingKey[String]("mustache-filename-suffix", "Suffix to append to the output file names before the existing filename extension")
  }

  import MustacheKeys._

  def unmanagedSourcesTask = Def.task {
    val _out           = (streams).value
    val _sourceDirs    = (sourceDirectories in mustache).value
    val _includeFilter = (includeFilter in mustache).value
    val _excludeFilter = (excludeFilter in mustache).value

    _out.log.debug("sourceDirectories: " + _sourceDirs)
    _out.log.debug("includeFilter: " + _includeFilter)
    _out.log.debug("excludeFilter: " + _excludeFilter)

    _sourceDirs.foldLeft(Seq[File]()) { (accum, sourceDir) =>
      import com.untyped.sbtgraph.Descendents

      accum ++ Descendents(sourceDir, _includeFilter, _excludeFilter).get
    }
  }

  def sourceGraphTask = Def.task {
    val _out                = (streams).value
    val _sourceDirs         = (sourceDirectories in mustache).value
    val _targetDir          = (resourceManaged in mustache).value
    val _sourceFiles        = (unmanagedSources in mustache).value
    val _templateProperties = (templateProperties).value
    val _downloadDir        = (downloadDirectory).value
    val _filenameSuffix     = (filenameSuffix).value

    _out.log.debug("sbt-mustache template properties " + _templateProperties)

    val graph = Graph(
      log                = _out.log,
      sourceDirs         = _sourceDirs,
      targetDir          = _targetDir,
      templateProperties = _templateProperties,
      downloadDir        = _downloadDir,
      filenameSuffix     = _filenameSuffix
    )

    _sourceFiles.foreach(graph +=)

    graph
  }

  def watchSourcesTask = Def.task {
    val _out   = (streams).value
    val _graph = (sourceGraph in mustache).value

    _graph.sources.map(_.src) : Seq[File]
  }

  def compileTask = Def.task {
    val _out         = (streams).value
    val _sourceFiles = (unmanagedSources in mustache).value
    val _graph       = (sourceGraph in mustache).value

    _graph.compileAll(_sourceFiles)
  }

  def cleanTask = Def.task {
    val _out   = (streams).value
    val _graph = (sourceGraph in mustache).value

    _graph.sources.foreach(_.clean())
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
      filenameSuffix                :=   "",
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
