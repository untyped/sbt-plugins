import sbt._
import sbt.Keys._

object Build extends Build {

  import ScriptedPlugin._

  // Libraries ----------------------------------

  val closure       = "com.google.javascript" % "closure-compiler" % "r1592"
  val mustache      = "com.samskivert" % "jmustache" % "1.3"
  val rhino         = "rhino" % "js" % "1.7R2"
  val scalatest     = "org.scalatest" %% "scalatest" % "1.6.1"
  val jCoffeescript = "org.jcoffeescript" % "jcoffeescript" % "1.1" from "http://cloud.github.com/downloads/yeungda/jcoffeescript/jcoffeescript-1.1.jar"
  val webPlugin     = "com.github.siasia" %% "xsbt-web-plugin" % "0.11.2-0.2.10"

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
        for {
          host <- Option(System.getenv("DEFAULT_REPO_HOST"))
          path <- Option(System.getenv("DEFAULT_REPO_PATH"))
          user <- Option(System.getenv("DEFAULT_REPO_USER"))
          keyfile <- Option(System.getenv("DEFAULT_REPO_KEYFILE"))
        } yield {
          Resolver.sftp("Default Repo", host, path).as(user, new File(keyfile))
        }
      },
      scriptedBufferLog := false,
      scalacOptions += "-deprecation",
      scalacOptions += "-unchecked"
    )

  // Projects -----------------------------------

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = defaultSettings ++ Seq(
      publishArtifact in (Compile) := false,
      publishArtifact in (Compile, packageBin) := false,
      publishArtifact in (Compile, packageSrc) := false,
      publishArtifact in (Compile, packageDoc) := false
    )
  ) aggregate (
    sbtJs,
    sbtLess,
    sbtMustache,
    sbtRunmode
  )

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
        jCoffeescript,
        mustache,
        scalatest % "test"
      )
    )
  )

  lazy val sbtMustache = Project(
    id = "sbt-mustache",
    base = file("sbt-mustache"),
    settings = defaultSettings ++ Seq(
      version := "0.1-SNAPSHOT",
      libraryDependencies ++= Seq(
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
