import sbt._
import sbt.Keys._
import net.virtualvoid.sbt.cross._
import bintray.BintrayPlugin
import bintray.BintrayKeys._

object Build extends Build {

  import ScriptedPlugin._

  val pluginsVersion = "0.9-SNAPSHOT"

  // Libraries ----------------------------------

  val closure   = "com.google.javascript" % "closure-compiler"   % "v20151216"
  val mustache  = "com.samskivert"        % "jmustache"          % "1.8"
  val rhino     = "org.mozilla"           % "rhino"              % "1.7R4"
  val jruby     = "org.jruby"             % "jruby-complete"     % "1.7.10"

  def scalatest(sbtVersion: String) =
    sbtVersion match {
      case v if v startsWith "0.13" => "org.scalatest" %% "scalatest" % "2.0"   % "test"
      case v => throw new Exception("Build.scala: don't know what version of scalatest to use for SBT " + v)
    }

  // Settings -----------------------------------

  def defaultSettings =
    Project.defaultSettings ++
    CrossPlugin.crossBuildingSettings ++
    CrossBuilding.scriptedSettings ++
    BintrayPlugin.bintrayPublishSettings ++
    Seq(
      fork                            := true,
      fork in scripted                := true,
      sbtPlugin                       := true,
      publishMavenStyle               := true,
      organization       in ThisBuild := "com.untyped",
      version            in ThisBuild := pluginsVersion,
      scriptedBufferLog               := false,
      scalacOptions                  ++= Seq("-deprecation", "-unchecked"),
      scriptedLaunchOpts             ++= Seq("-Xmx1024M", s"-Dplugin.version=${version.value}"),
      scripted                       <<= scripted dependsOn publishLocal,
      // Bintray:
      licenses                        += ("Apache-2.0", url("http://apache.org/licenses/LICENSE-2.0")),
      bintrayPackageLabels            := Seq("scala", "sbt", "build", "web"),
      bintrayOrganization             := Some("untyped"),
      bintrayRepository               := "maven"
    )

  // Projects -----------------------------------

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = defaultSettings ++ Seq(publish := (), bintrayReleaseOnPublish := false)
  ) aggregate (
    sbtJs,
    sbtLess,
    sbtSass,
    sbtMustache
  )

  lazy val sbtGraph = Project(
    id = "sbt-graph",
    base = file("sbt-graph"),
    settings = defaultSettings ++ Seq(
      publishArtifact in Compile               := false,
      publishArtifact in (Compile, packageBin) := false,
      publishArtifact in (Compile, packageSrc) := false,
      publishArtifact in (Compile, packageDoc) := false,
      libraryDependencies                      += mustache,
      libraryDependencies                     <+= (sbtVersion in sbtPlugin)(scalatest)
    )
  )

  lazy val sbtLess = Project(
    id   = "sbt-less",
    base = file("sbt-less"),
    settings = defaultSettings ++ Seq(
      name                                    := "sbt-less",
      libraryDependencies                    ++= Seq(rhino, mustache),
      libraryDependencies                    <+= (sbtVersion in sbtPlugin)(scalatest),
      // Make sure the classes for sbt-graph get packaged in the artifacts for sbt-less:
      unmanagedSourceDirectories in Compile <++= (unmanagedSourceDirectories in (sbtGraph, Compile))
    )
  )

  lazy val sbtSass = Project(
    id = "sbt-sass",
    base = file("sbt-sass"),
    settings = defaultSettings ++ Seq(
      name                                    := "sbt-sass",
      libraryDependencies                    ++= Seq(jruby, mustache),
      libraryDependencies                    <+= (sbtVersion in sbtPlugin)(scalatest),
      // Make sure the classes for sbt-graph get packaged in the artifacts for sbt-less:
      unmanagedSourceDirectories in Compile <++= (unmanagedSourceDirectories in (sbtGraph, Compile))
    )
  )

  lazy val sbtJs = Project(
    id = "sbt-js",
    base = file("sbt-js"),
    settings = defaultSettings ++ Seq(
      name                                    := "sbt-js",
      libraryDependencies                    ++= Seq(closure, rhino, mustache),
      libraryDependencies                    <+= (sbtVersion in sbtPlugin)(scalatest),
      // Make sure the classes for sbt-graph get packaged in the artifacts for sbt-js:
      unmanagedSourceDirectories in Compile <++= (unmanagedSourceDirectories in (sbtGraph, Compile))
    )
  )

  lazy val sbtMustache = Project(
    id = "sbt-mustache",
    base = file("sbt-mustache"),
    settings = defaultSettings ++ Seq(
      name                                    := "sbt-mustache",
      libraryDependencies                    ++= Seq(rhino, mustache),
      libraryDependencies                    <+= (sbtVersion in sbtPlugin)(scalatest),
      // Make sure the classes for sbt-graph get packaged in the artifacts for sbt-mustache:
      unmanagedSourceDirectories in Compile <++= (unmanagedSourceDirectories in (sbtGraph, Compile))
    )
  )

}
