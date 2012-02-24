package com.untyped.sbtrunmode

import com.github.siasia._
import com.github.siasia.{PluginKeys=>WebKeys}
import java.nio.charset.Charset
import java.util.Properties
import sbt._
import sbt.Keys._
import sbt.Keys.{`package` => packageKey}
import sbt.Project.Initialize
import com.untyped.sbtjs.Plugin.JsKeys
import com.untyped.sbtless.Plugin.LessKeys

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
  
  val Development = config("development") extend(Compile)
  val Production  = config("production")  extend(Compile)
  val Pilot       = config("pilot")       extend(Compile)
  
  // Alias to make RunMode visible in .sbt files:
  val RunMode = com.untyped.sbtrunmode.RunMode
  
  def propertiesSetting: Initialize[Properties] = 
    (streams, propertiesPath, runMode) apply {
      (out, propertiesPath, runMode) =>
        Props.properties(propertiesPath, runMode)
    }
  
  def updateRunModeTask: Initialize[Task[Unit]] =
    (streams, runMode, jettyWebPath, charset) map {
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
  
  /**
   * Generate the settings for a run-mode configuration containing the following wrapper tasks:
   *
   *  - compile - runs compile:compile;
   *  - js      - runs js;
   *  - less    - runs less;
   *  - clean   - runs compile:clean;
   *  - start   - sets the run mode, compiles JS and CSS, and runs container:start;
   *  - stop    - runs container:stop;
   *  - package - sets the run mode, compiles JS and CSS, and runs compile:package.
   */
  def runModeSettingsIn(conf: Configuration, container: Container, mode: RunMode): Seq[Setting[_]] =
    inConfig(conf)(
      com.untyped.sbtjs.Plugin.jsSettingsIn(conf)     ++
      com.untyped.sbtless.Plugin.lessSettingsIn(conf) ++
      Seq(
        charset                                    :=  Charset.forName("utf-8"),
        propertiesPath                             <<= (sourceDirectory in Compile)(_ / "resources"),
        jettyWebPath                               <<= (sourceDirectory in Compile)(_ / "webapp" / "WEB-INF" / "jetty-web.xml"),
        runMode                                    :=  mode,
        properties                                 <<= propertiesSetting,
        updateRunMode                              <<= updateRunModeTask,
        JsKeys.templateProperties                  <<= (RunModeKeys.properties in conf),
        sourceDirectory               in JsKeys.js <<= (sourceDirectory in Compile)(_ / "js"),
        resourceManaged               in JsKeys.js <<= (sourceDirectory in Compile)(_ / "webapp"),
        JsKeys.prettyPrint            in JsKeys.js :=  (mode == RunMode.Development),
        JsKeys.variableRenamingPolicy in JsKeys.js :=  (if(mode == RunMode.Development) com.untyped.sbtjs.Plugin.VariableRenamingPolicy.OFF else com.untyped.sbtjs.Plugin.VariableRenamingPolicy.LOCAL),
        LessKeys.templateProperties                <<= (RunModeKeys.properties in conf),
        sourceDirectory           in LessKeys.less <<= (sourceDirectory in Compile)(_ / "css"),
        resourceManaged           in LessKeys.less <<= (sourceDirectory in Compile)(_ / "webapp"),
        LessKeys.prettyPrint      in LessKeys.less :=  (mode == RunMode.Development),
        JsKeys.js                                  <<= JsKeys.js,
        LessKeys.less                              <<= LessKeys.less,
    		compile                                    <<= (compile         in Compile),
        clean                                      <<= (clean           in Compile),
        packageKey                                 <<= (packageKey      in Compile)                 dependsOn (updateRunMode in conf) dependsOn (JsKeys.js in conf) dependsOn (LessKeys.less in conf),
        WebKeys.start                              <<= (WebKeys.start   in container.Configuration) dependsOn (updateRunMode in conf) dependsOn (JsKeys.js in conf) dependsOn (LessKeys.less in conf),
        WebKeys.stop                               <<= (WebKeys.stop    in container.Configuration)
      ))

  /**
   * Cut-down version of `runModeSettingsIn` for configrations in which:
   *
   *  - we need a run mode, but;
   *  - we don't run a full container, and;
   *  - we don't need to compile Javascript or Less CSS.
   *
   * Essentially, redefines foo:test to depend on updateRunMode.
   */
  def runModeTestSettingsIn(conf: Configuration, mode: RunMode): Seq[Setting[_]] =
    inConfig(conf)(
      Seq(
        charset                             :=  Charset.forName("utf-8"),
        propertiesPath                      <<= (sourceDirectory in Compile)(_ / "resources"),
        jettyWebPath                        <<= (sourceDirectory in Compile)(_ / "webapp" / "WEB-INF" / "jetty-web.xml"),
        runMode                             :=  mode,
        properties                          <<= propertiesSetting,
        updateRunMode                       <<= updateRunModeTask,
        test                                <<= test dependsOn (updateRunModeTask)
      ))
  
  def runModeSettings: Seq[Setting[_]] =
    WebPlugin.webSettings ++
    runModeSettingsIn(Development, WebPlugin.container, RunMode.Development)  ++
    runModeSettingsIn(Pilot,       WebPlugin.container, RunMode.Pilot) ++
    runModeSettingsIn(Production,  WebPlugin.container, RunMode.Production) ++
    runModeTestSettingsIn(Test,                         RunMode.Test)
    
}
