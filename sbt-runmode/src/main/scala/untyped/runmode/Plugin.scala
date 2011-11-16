package untyped.runmode

import java.nio.charset.Charset
import java.util.Properties
import sbt._
import sbt.Keys._
import sbt.Project.Initialize

object Plugin extends sbt.Plugin {
  
  object RunModeKeys {
    lazy val updateRunMode  = TaskKey[Unit]("update-run-mode", "Install/remove a custom jetty-web.xml file to reflect the current run mode")
    lazy val jettyWebPath   = SettingKey[File]("run-mode-jetty-web-path", "Path of jetty-web.xml")
    lazy val propertiesPath = SettingKey[File]("run-mode-properties-path", "Base path to search for properties files")
    lazy val runMode        = SettingKey[RunMode]("run-mode", "Desired Lift run-mode")
    lazy val properties     = SettingKey[Properties]("run-mode-properties", "System properties with the run mode set correctly")
    lazy val charset        = SettingKey[Charset]("run-mode-charset", "Character set to use for jetty-web.xml")
  }
  
  import RunModeKeys._
  
  val RunMode = untyped.runmode.RunMode
  
  def propertiesSetting: Initialize[Properties] = 
    (streams, propertiesPath, runMode) apply {
      (out, propertiesPath, runMode) =>
        Props.properties(propertiesPath, runMode)
    }
  
  def updateRunModeTask: Initialize[Task[Unit]] =
    (streams, runMode in updateRunMode, jettyWebPath in updateRunMode, charset in updateRunMode) map {
      (out, runMode, jettyWebPath, charset) =>
        runMode match {
          case RunMode.Development =>
            out.log.info("Removing jetty-web.xml - setting run.mode = <blank> (i.e. development)")
            IO.delete(jettyWebPath)
          
          case mode =>
            out.log.info("Creating jetty-web.xml - setting run.mode = " + mode.name)
            IO.write(jettyWebPath, jettyWebXml(mode), charset, false)
        }
    }
  
  def jettyWebXml(mode: RunMode) =
    """|<?xml version="1.0"?>
       |<!DOCTYPE Configure PUBLIC "-//Mort Bay Consulting//DTD Configure//EN" "http://jetty.mortbay.org/configure.dtd">
       |<Configure class="org.mortbay.jetty.webapp.WebAppContext">
       |  <Call class="java.lang.System" name="setProperty">
       |    <Arg>run.mode</Arg>
       |    <Arg>%s</Arg>
       |  </Call>
       |</Configure>""".stripMargin.format(mode.name)

  def runModeSettingsIn(conf: Configuration, mode: RunMode): Seq[Setting[_]] =
    inConfig(conf)(Seq(
      charset        :=  Charset.forName("utf-8"),
      propertiesPath <<= (sourceDirectory in conf) { _ / "resources" },
      jettyWebPath   <<= (sourceDirectory in conf) { _ / "webapp" / "WEB-INF" / "jetty-web.xml" },
      runMode        :=  mode,
      properties     <<= propertiesSetting,
      updateRunMode  <<= updateRunModeTask
    ))
  
  def runModeSettings: Seq[Setting[_]] =
    runModeSettingsIn(Compile, RunMode.Development) ++
    runModeSettingsIn(Test, RunMode.Test)
    
}
