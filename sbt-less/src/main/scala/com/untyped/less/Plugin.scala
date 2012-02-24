package com.untyped.less

import java.nio.charset.Charset
import java.util.Properties
import sbt._
import sbt.Keys._
import sbt.Project.Initialize

object Plugin extends sbt.Plugin {

  object LessKeys {
    val less                = TaskKey[Seq[File]]("less", "Compile Less CSS sources.")
    val sourceGraph         = TaskKey[Graph]("less-source-graph", "List of Less CSS sources.")
    val templateProperties  = SettingKey[Properties]("less-template-properties", "Properties to use in Less CSS templates")
    val downloadDirectory   = SettingKey[File]("less-download-directory", "Temporary directory to download Less CSS files to")
    val prettyPrint         = SettingKey[Boolean]("less-pretty-print", "Whether to pretty print CSS (default false)")
    val lessVersion         = SettingKey[LessVersion]("less-version", "The version of the Less CSS compiler to use")
  }

  sealed trait LessVersion {
    val filename: String
    lazy val url = "/" + filename
  }
  
  object LessVersion {
    val Less113 = new LessVersion { val filename = "less-rhino-1.1.3.js" }
    val Less115 = new LessVersion { val filename = "less-rhino-1.1.5.js" }
    val Less121 = new LessVersion { val filename = "less-rhino-1.2.1.js" } // Note: this version doesn't work yet.
  }

  import LessKeys._

  def unmanagedSourcesTask: Initialize[Task[Seq[File]]] =
    (streams, sourceDirectory in less, includeFilter in less, excludeFilter in less) map {
      (out, sourceDir, includeFilter, excludeFilter) =>
        out.log.debug("sourceDirectory: " + sourceDir)
        out.log.debug("includeFilter: " + includeFilter)
        out.log.debug("excludeFilter: " + excludeFilter)

        sourceDir.descendentsExcept(includeFilter, excludeFilter).get
    }

  def sourceGraphTask: Initialize[Task[Graph]] =
    (streams, sourceDirectory in less, resourceManaged in less, unmanagedSources in less, templateProperties in less, downloadDirectory in less, prettyPrint in less, lessVersion in less) map {
      (out, sourceDir, targetDir, sourceFiles, templateProperties, downloadDir, prettyPrint, lessVersion) =>
        out.log.debug("sbt-less template properties " + templateProperties)

        val graph = Graph(
          log                = out.log,
          sourceDir          = sourceDir,
          targetDir          = targetDir,
          templateProperties = templateProperties,
          downloadDir        = downloadDir,
          lessVersion        = lessVersion,
          prettyPrint        = prettyPrint
        )

        sourceFiles.foreach(graph += _)

        graph
    }

  def watchSourcesTask: Initialize[Task[Seq[File]]] =
    (streams, sourceGraph in less) map {
      (out, graph) =>
        graph.sources.map(_.src)
    }

  def compileTask =
    (streams, unmanagedSources in less, sourceGraph in less) map {
      (out, sourceFiles, graph: Graph) =>
        out.log.debug("sourceFiles for sbt-less:")
        sourceFiles.foreach { file =>
          out.log.debug("  " + file)
        }

        graph.dump

        sourceFiles.flatMap(graph.findSource _).filter(_.requiresRecompilation) match {
          case Nil =>
            out.log.info("No Less CSS sources requiring compilation")
            Nil

          case toCompile =>
            var compiled = toCompile.flatMap(_.compile)

            if(compiled.length < toCompile.length) {
              sys.error("Some Less CSS sources could not be compiled")
            } else {
              compiled
            }
        }
    }

  def cleanTask =
    (streams, sourceGraph in less) map {
      (out, graph) =>
        graph.sources.foreach(_.clean)
    }

  def lessSettingsIn(conf: Configuration): Seq[Setting[_]] =
    inConfig(conf)(Seq(
      prettyPrint                  :=  false,
      includeFilter in less        :=  "*.less",
      excludeFilter in less        :=  (".*" - ".") || "_*" || HiddenFileFilter,
      lessVersion in less          :=  LessVersion.Less115,
      sourceDirectory in less      <<= (sourceDirectory in conf),
      unmanagedSources in less     <<= unmanagedSourcesTask,
      resourceManaged in less      <<= (resourceManaged in conf),
      templateProperties           :=  new Properties,
      downloadDirectory            <<= (target in conf) { _ / "sbt-less" / "downloads" },
      sourceGraph                  <<= sourceGraphTask,
      sources in less              <<= watchSourcesTask,
      watchSources in less         <<= watchSourcesTask,
      clean in less                <<= cleanTask,
      less                         <<= compileTask
    )) ++ Seq(
      cleanFiles                   <+=  (resourceManaged in less in conf),
      watchSources                 <++= (watchSources in less in conf)
    )

  def lessSettings: Seq[Setting[_]] =
    lessSettingsIn(Compile) ++
    lessSettingsIn(Test)

}
