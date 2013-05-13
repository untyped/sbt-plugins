package com.untyped.sbtassets

import sbt._
import sbt.Keys._
import sbt.Project.Initialize

object Plugin extends sbt.Plugin {
  object AssetsKeys {
    // val assetsConfig = SettingKey[AssetsConfig]("assets-config", "Asset compilation configuration.")
    val assetsRule   = TaskKey[Rule]("assets-rule", "Asset compilation rule.")
    val assets       = TaskKey[Unit]("assets", "Compile all assets.")
  }

  import AssetsKeys._

  def watchSourcesTask: Initialize[Task[Seq[File]]] =
    (streams, assetsRule) map { (out, rule) =>
      rule.watchSources
    }

  def compileTask =
    (streams, assetsRule) map { (out, rule) =>
      rule.compile(out.log)
    }

  def cleanTask =
    (streams, assetsRule) map { (out, rule) =>
      rule.clean(out.log)
    }

  def assetsSettingsIn(conf: Configuration): Seq[Setting[_]] =
    inConfig(conf)(Seq(
      clean        in assets <<= cleanTask,
      watchSources in assets <<= watchSourcesTask,
      assets                 <<= compileTask
    )) ++ Seq(
      watchSources          <++= watchSources in assets in conf
    )

  def assetsSettings: Seq[Setting[_]] =
    assetsSettingsIn(Compile) ++
    assetsSettingsIn(Test)
}
