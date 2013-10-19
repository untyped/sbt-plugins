package com.untyped.sbtrunmode

import java.io.{File,FileInputStream}
import java.net.InetAddress
import java.util.Properties
import scala.language.implicitConversions

// Heavily based on Lift's Props.
// 
// Reimplemented here as there were a few things in 
// Lift's properties handling that made it hard to reuse.

case class RunMode(name: String) {
  
  lazy val dottedName: String =
    if(name == "development") "" else name + "."
  
}

object RunMode {
  val Development = RunMode("development")
  val Test        = RunMode("test")
  val Staging     = RunMode("staging")
  val Production  = RunMode("production")
  val Pilot       = RunMode("pilot")
  val Profile     = RunMode("profile")
}

object Props {

  def addDot(str: Option[String]): String =
    str match {
      case None => ""
      case Some("") => ""
      case Some(s) => s + "."
    }
  
  implicit def fileToString(in: File) =
    in.getCanonicalPath
  
  def defaultUserName: Option[String] =
    Option(System.getProperty("user.name"))
  
  def defaultHostName: Option[String] =
    Option(InetAddress.getLocalHost.getHostName)
  
  def searchPaths(
    basePath: File,
    mode: RunMode,
    userName: Option[String] = defaultUserName,
    hostName: Option[String] = defaultHostName
  ): List[String] = {
    val modeDot = mode.dottedName
    val userNameDot = addDot(userName)
    val hostNameDot = addDot(hostName)
    
    List(
      basePath + "/props/" + modeDot + userNameDot + hostNameDot + "props",
      basePath + "/props/" + modeDot + userNameDot + "props",
      basePath + "/props/" + modeDot + hostNameDot + "props",
      basePath + "/props/" + modeDot + "default." + "props",
      basePath + "/" + modeDot + userNameDot + hostNameDot + "props",
      basePath + "/" + modeDot + userNameDot + "props",
      basePath + "/" + modeDot + hostNameDot + "props",
      basePath + "/" + modeDot + "default.props")
  }
  
  def file(
    basePath: File,
    mode: RunMode,
    userName: Option[String] = defaultUserName,
    hostName: Option[String] = defaultHostName
  ): Option[File] = searchPaths(basePath, mode, userName, hostName).map(new File(_)).find(_.exists)

  def properties(
    basePath: File,
    mode: RunMode,
    userName: Option[String] = defaultUserName,
    hostName: Option[String] = defaultHostName
  ): Properties =
    file(basePath, mode, userName, hostName) map { propFile =>
      val props = new Properties
      val in = new FileInputStream(new File(propFile))
      props.load(in)
      props.setProperty("run.mode", mode.name)
      in.close()
      props
    } getOrElse {
      val props = new Properties
      props.setProperty("run.mode", mode.name)
      props
    }
  
}