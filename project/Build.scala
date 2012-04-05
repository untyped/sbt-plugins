import sbt._
import sbt.Keys._

object Build extends Build {

  import ScriptedPlugin._

  val pluginsVersion = "0.1-SNAPSHOT"

  // Libraries ----------------------------------
  val lesscss_java  = "org.lesscss" % "lesscss" % "1.3.0"
  val scalatest     = "org.scalatest" %% "scalatest" % "1.6.1"
  // val jCoffeescript = "org.jcoffeescript" % "jcoffeescript" % "1.1"

  def webPlugin(sbtVersion: String) =
    sbtVersion match {
      case "0.11.0" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.0-0.2.8"
      case "0.11.1" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.1-0.2.10"
      case "0.11.2" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.2-0.2.11"
    }

  // Settings -----------------------------------

  def defaultSettings =
    Project.defaultSettings ++
    scriptedSettings ++
    Seq(
      sbtPlugin := true,
      organization := "org.lunatool",
      version := pluginsVersion,
      scalaVersion := "2.9.1",
      
      publishTo <<= (version) { version: String =>
         val scalasbt = "http://scalasbt.artifactoryonline.com/scalasbt/"
         val (name, url) = if (version.contains("-SNAPSHOT"))
                             ("sbt-plugin-snapshots", scalasbt+"sbt-plugin-snapshots")
                           else
                             ("sbt-plugin-releases", scalasbt+"sbt-plugin-releases")
         Some(Resolver.url(name, new URL(url))(Resolver.ivyStylePatterns))
      },
      publishMavenStyle := false,
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
    sbtLess
  )

 
  lazy val sbtLess = Project(
    id = "sbt-less",
    base = file("sbt-less"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Seq(
        lesscss_java,
        scalatest % "test"
      )
    )
  )

 

}
