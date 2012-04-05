package org.lunatool.sbtless

import java.nio.charset.Charset
import java.util.Properties
import sbt._
import sbt.Keys._
import sbt.Project.Initialize

object Plugin extends sbt.Plugin {

  object LessKeys {
    val less                = TaskKey[Seq[File]]("less", "Compile Less CSS sources.")
    val prettyPrint         = SettingKey[Boolean]("less-pretty-print", "Whether to pretty print CSS (default false)")
  }

  import LessKeys._

  def compileTask =
    (streams, sourceDirectories in less, resourceManaged in less, includeFilter in less, excludeFilter in less, prettyPrint in less) map {
      (out, sourceDirs, cssDir, includeFilter, excludeFilter, notCompress) =>
        
        val compiler = new org.lesscss.LessCompiler

        compiler.setCompress(!notCompress)

        for {
          sourceDir <- sourceDirs
          src <- sourceDir.descendentsExcept(includeFilter, excludeFilter).get
          lessSrc = new LessSourceFile(src, sourceDir, cssDir)
          if lessSrc.changed
        } yield {
          compiler.compile(lessSrc, lessSrc.cssFile)
          lessSrc.cssFile
        }
    }

  def cleanTask =
    (streams, sourceDirectories in less, resourceManaged in less, includeFilter in less, excludeFilter in less) map {
      (out, sourceDirs, cssDir, includeFilter, excludeFilter) =>
        for {
          sourceDir <- sourceDirs
          src <- sourceDir.descendentsExcept(includeFilter, excludeFilter).get
          lessSrc = new LessSourceFile(src, sourceDir, cssDir)
        } {
          IO.delete(lessSrc.cssFile)
        }
    }

  def lessSettingsIn(conf: Configuration): Seq[Setting[_]] =
    inConfig(conf)(Seq(
      prettyPrint                  :=  false,
      includeFilter in less        :=  "*.less",
      excludeFilter in less        :=  (".*" - ".") || "_*" || HiddenFileFilter,
      sourceDirectory in less      <<= (sourceDirectory in conf),
      sourceDirectories in less    <<= (sourceDirectory in conf) { Seq(_) },
      resourceManaged in less      <<= (resourceManaged in conf),
      clean in less                <<= cleanTask,
      less                         <<= compileTask
    )) ++ Seq(
      cleanFiles                   <+=  (resourceManaged in less in conf)
    )

  def lessSettings: Seq[Setting[_]] =
    lessSettingsIn(Compile) ++
    lessSettingsIn(Test)

}
