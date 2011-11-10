package untyped.runmode

import java.nio.charset.Charset
import sbt._
import sbt.Keys._
import sbt.Project.Initialize

object Plugin extends sbt.Plugin {
  
  object RunModeKeys {
    lazy val updateRunMode = TaskKey[Unit]("update-run-mode", "Install/remove a custom jetty-web.xml file to reflect the current run mode")
    lazy val jettyWebPath  = SettingKey[File]("jetty-web-path", "Path of jetty-web.xml")
    lazy val runMode       = SettingKey[Option[String]]("run-mode", "Desired Lift run-mode")
    lazy val charset       = SettingKey[Charset]("charset", "Character set to use for jetty-web.xml")
  }
  
  import RunModeKeys._
  
  def updateRunModeTask: Initialize[Task[Unit]] =
    (streams, runMode in updateRunMode, jettyWebPath in updateRunMode, charset in updateRunMode) map {
      (out, runMode, jettyWebPath, charset) =>
        runMode match {
          case Some(mode) =>
            out.log.info("Creating jetty-web.xml - setting run.mode = " + mode)
            IO.write(jettyWebPath, jettyWebXml(mode), charset, false)
          case None =>
            out.log.info("Removing jetty-web.xml - setting run.mode = <blank>")
            IO.delete(jettyWebPath)
        }
    }

  def jettyWebXml(runMode: String) =
    """|<?xml version="1.0"?>
       |<!DOCTYPE Configure PUBLIC "-//Mort Bay Consulting//DTD Configure//EN" "http://jetty.mortbay.org/configure.dtd">
       |<Configure class="org.mortbay.jetty.webapp.WebAppContext">
       |  <Call class="java.lang.System" name="setProperty">
       |    <Arg>run.mode</Arg>
       |    <Arg>%s</Arg>
       |  </Call>
       |</Configure>""".stripMargin.format(runMode)

  def runModeSettingsIn(conf: Configuration): Seq[Setting[_]] =
    inConfig(conf)(Seq(
      charset      in updateRunMode :=  Charset.forName("utf-8"),
      jettyWebPath in updateRunMode <<= (sourceDirectory in conf) { _ / "webapp" / "WEB-INF" / "jetty-web.xml" },
      runMode                       :=  None,
      updateRunMode                 <<= updateRunModeTask
    ))
  
  def runModeSettings: Seq[Setting[_]] =
    runModeSettingsIn(Compile) ++
    runModeSettingsIn(Test)
    
}
