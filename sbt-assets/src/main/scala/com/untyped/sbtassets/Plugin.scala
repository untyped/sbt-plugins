package com.untyped.sbtassets

import sbt._
import sbt.Keys._
import sbt.Project.Initialize

object Plugin extends sbt.Plugin {
  object AssetsKeys {
    // val assetsConfig = SettingKey[AssetsConfig]("assets-config", "Asset compilation configuration.")
    val assetsRule   = TaskKey[Rule]("assets-rule", "Asset compilation rule.")
    val assets       = TaskKey[Seq[File]]("assets", "Compile all assets.")
  }

  import AssetsKeys._

  def unmanagedSourcesTask: Initialize[Task[Seq[File]]] =
    (streams, assetsRule) map { (out, rule) =>
      rule.unmanagedAssets.map(_.file)
    }

  def managedSourcesTask: Initialize[Task[Seq[File]]] =
    (streams, assetsRule) map { (out, rule) =>
      rule.managedAssets.map(_.file)
    }

  def compileTask: Initialize[Task[Seq[File]]] =
    (streams, assetsRule) map { (out, rule) =>
      rule.compile(out.log)
      rule.assets.map(_.file)
    }

  def cleanTask: Initialize[Task[Unit]] =
    (streams, assetsRule) map { (out, rule) =>
      rule.clean(out.log)
    }

  def assetsSettingsIn(conf: Configuration): Seq[Setting[_]] =
    inConfig(conf)(Seq(
      clean            in assets <<= cleanTask,
      unmanagedSources in assets <<= unmanagedSourcesTask,
      managedSources   in assets <<= unmanagedSourcesTask,
      assets                     <<= compileTask
    )) ++ Seq(
      watchSources              <++= (unmanagedSources in assets in conf)
    )

  def assetsSettings: Seq[Setting[_]] =
    assetsSettingsIn(Compile) ++
    assetsSettingsIn(Test)
}
