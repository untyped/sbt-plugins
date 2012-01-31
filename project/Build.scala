import sbt._
import sbt.Keys._

object Build extends Build {
  
  import ScriptedPlugin._
  
  // Libraries ----------------------------------
  
  val closure    = "com.google.javascript" % "closure-compiler" % "r706"
  val mustache   = "com.samskivert" % "jmustache" % "1.3"
  val rhino      = "rhino" % "js" % "1.7R2"
  val scalatest  = "org.scalatest" %% "scalatest" % "1.6.1"
  
  val webPlugin  = "com.github.siasia" %% "xsbt-web-plugin" % "0.11.2-0.2.10"
  
  // Settings -----------------------------------
  
  def defaultSettings =
    Project.defaultSettings ++
    scriptedSettings ++
    Seq(
      sbtPlugin := true,
      organization := "untyped",
      scalaVersion := "2.9.1",
      resolvers += "Untyped Public Repo" at "http://repo.untyped.com",
      publishTo := {
        val host = System.getenv("DEFAULT_REPO_HOST")
        val path = System.getenv("DEFAULT_REPO_PATH")
        val user = System.getenv("DEFAULT_REPO_USER")
        val keyfile = new File(System.getenv("DEFAULT_REPO_KEYFILE"))
        Some(Resolver.sftp("Default Repo", host, path).as(user, keyfile))
      },
      scriptedBufferLog := false
    )
  
  // Projects -----------------------------------
  
  lazy val root = Project(
    id = "root",
    base = file(".")
  ) aggregate(sbtJs, sbtLess, sbtRunmode)
  
  // lazy val sbtGraph = Project(
  //   id = "sbt-graph",
  //   base = file("sbt-graph"),
  //   settings = defaultSettings ++ Seq(
  //     version := "0.1-SNAPSHOT"
  //   )
  // )
  
  lazy val sbtLess = Project(
    id = "sbt-less",
    base = file("sbt-less"),
    settings = defaultSettings ++ Seq(
      version := "0.2-SNAPSHOT",
      libraryDependencies ++= Seq(
        rhino,
        mustache,
        scalatest % "test"
      )
    )
  )
  
  lazy val sbtJs = Project(
    id = "sbt-js",
    base = file("sbt-js"),
    settings = defaultSettings ++ Seq(
      version := "0.1-SNAPSHOT",
      libraryDependencies ++= Seq(
        closure,
        mustache,
        scalatest % "test"
      )
    )
  )
  
  lazy val sbtRunmode = Project(
    id = "sbt-runmode",
    base = file("sbt-runmode"),
    settings = defaultSettings ++ Seq(
      version := "0.1-SNAPSHOT",
      libraryDependencies ++= Seq(
        webPlugin,
        scalatest % "test"
      )
    )
  ) dependsOn(sbtLess, sbtJs)
  
}
