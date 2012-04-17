import sbt._
import sbt.Keys._

object Build extends Build {

  import ScriptedPlugin._

  val pluginsVersion = "0.5-SNAPSHOT"

  // Libraries ----------------------------------

  val closure       = "com.google.javascript" % "closure-compiler" % "r1592"
  val mustache      = "com.samskivert" % "jmustache" % "1.3"
  val rhino         = "org.mozilla" % "rhino" % "1.7R3"
  val scalatest     = "org.scalatest" %% "scalatest" % "1.6.1"
  // val jCoffeescript = "org.jcoffeescript" % "jcoffeescript" % "1.1"

  def webPlugin(sbtVersion: String) =
    sbtVersion match {
      case "0.11.0" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.0-0.2.8"
      case "0.11.1" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.1-0.2.10"
      case "0.11.2" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.2-0.2.11"
    }

  // Settings -----------------------------------

  def isSnapshot(version: String) =
    version.contains("-SNAPSHOT") || version.contains("-RC") || version.contains("-M")

  def defaultSettings =
    Project.defaultSettings ++
    scriptedSettings ++
    Seq(
      sbtPlugin := true,
      organization := "com.untyped",
      version := pluginsVersion,
      scalaVersion := "2.9.1",
      resolvers += "Untyped Public Repo" at "http://repo.untyped.com",
      // publishTo := {
      //   for {
      //     host <- Option(System.getenv("DEFAULT_REPO_HOST"))
      //     path <- Option(System.getenv("DEFAULT_REPO_PATH"))
      //     user <- Option(System.getenv("DEFAULT_REPO_USER"))
      //     keyfile <- Option(System.getenv("DEFAULT_REPO_KEYFILE"))
      //   } yield {
      //     Resolver.sftp("Default Repo", host, path).as(user, new File(keyfile))
      //   }
      // },
      publishTo <<= (version) { version: String =>
       if (isSnapshot(version)) {
         for {
           host    <- Option(System.getenv("DEFAULT_REPO_HOST"))
           path    <- Option(System.getenv("DEFAULT_REPO_PATH"))
           user    <- Option(System.getenv("DEFAULT_REPO_USER"))
           keyfile <- Option(System.getenv("DEFAULT_REPO_KEYFILE"))
         } yield Resolver.sftp("Default Repo", host, path).as(user, file(keyfile))
       } else {
         Some(Resolver.url(
            "sbt-plugin-releases",
            new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"
         ))(Resolver.ivyStylePatterns))
       }
      },
      publishMavenStyle <<= (version)(isSnapshot _),
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
    // sbtGraph,
    sbtJs,
    sbtLess,
    sbtMustache,
    sbtRunmode
  )

  lazy val sbtGraph = Project(
    id = "sbt-graph",
    base = file("sbt-graph"),
    settings = defaultSettings ++ Seq(
      publishArtifact in (Compile) := false,
      publishArtifact in (Compile, packageBin) := false,
      publishArtifact in (Compile, packageSrc) := false,
      publishArtifact in (Compile, packageDoc) := false,
      libraryDependencies ++= Seq(
        mustache,
        scalatest % "test"
      )
    )
  )

  lazy val sbtLess = Project(
    id = "sbt-less",
    base = file("sbt-less"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Seq(
        rhino,
        mustache,
        scalatest % "test"
      ),
      // Make sure the classes for sbt-graph get packaged in the artifacts for sbt-less:
      unmanagedSourceDirectories in Compile <++= (unmanagedSourceDirectories in (sbtGraph, Compile))
    )
  )// .dependsOn(sbtGraph)

  lazy val sbtJs = Project(
    id = "sbt-js",
    base = file("sbt-js"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Seq(
        closure,
        rhino,
        // jCoffeescript,
        mustache,
        scalatest % "test"
      ),
      // Make sure the classes for sbt-graph get packaged in the artifacts for sbt-js:
      unmanagedSourceDirectories in Compile <++= (unmanagedSourceDirectories in (sbtGraph, Compile))
    )
  )// .dependsOn(sbtGraph)

  lazy val sbtMustache = Project(
    id = "sbt-mustache",
    base = file("sbt-mustache"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Seq(
        mustache,
        scalatest % "test"
      ),
      // Make sure the classes for sbt-graph get packaged in the artifacts for sbt-mustache:
      unmanagedSourceDirectories in Compile <++= (unmanagedSourceDirectories in (sbtGraph, Compile))
    )
  )// .dependsOn(sbtGraph)

  lazy val sbtRunmode = Project(
    id = "sbt-runmode",
    base = file("sbt-runmode"),
    settings = defaultSettings ++ Seq(
      libraryDependencies <+= sbtVersion(v => webPlugin(v)),
      libraryDependencies += scalatest % "test"
    )
  ).dependsOn(sbtLess, sbtJs)

}
