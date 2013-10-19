import sbt._
import sbt.Keys._
import net.virtualvoid.sbt.cross._

object Build extends Build {

  import ScriptedPlugin._

  val pluginsVersion = "0.6-M6"

  // Libraries ----------------------------------

  val untyped   = Resolver.url("Untyped", url("http://ivy.untyped.com"))(Resolver.ivyStylePatterns)

  val closure   = "com.google.javascript" % "closure-compiler"   % "v20130227"
  val mustache  = "com.samskivert"        % "jmustache"          % "1.3"
  val rhino     = "org.mozilla"           % "rhino"              % "1.7R3"

  def scalatest(sbtVersion: String) =
    sbtVersion match {
      case v if v startsWith "0.12" => "org.scalatest" %% "scalatest" % "1.9.1" % "test"
      case v if v startsWith "0.13" => "org.scalatest" %% "scalatest" % "1.9.1" % "test"
      case v => throw new Exception("Build.scala: don't know what version of scalatest to use for SBT " + v)
    }

  def webPlugin(sbtVersion: String) =
    sbtVersion match {
      case v if v startsWith "0.12" => Defaults.sbtPluginExtra("com.earldouglas" % "xsbt-web-plugin" % "0.4.2", "0.12", "2.9.2")
      case v if v startsWith "0.13" => Defaults.sbtPluginExtra("com.earldouglas" % "xsbt-web-plugin" % "0.4.2", "0.13", "2.10")
      case v => throw new Exception("Build.scala: don't know what version of xsbt-web-plugin to use for SBT " + v)
    }

  // This is a Jetty/Orbit thing:
  // http://stackoverflow.com/questions/9889674/sbt-jetty-and-servlet-3-0
  def jettyOrbit =
    "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" artifacts (Artifact("javax.servlet", "jar", "jar"))

  def snapshotPublishTo =
    for {
      host    <- Option(System.getenv("DEFAULT_IVY_REPO_HOST"))
      path    <- Option(System.getenv("DEFAULT_IVY_REPO_PATH"))
      user    <- Option(System.getenv("DEFAULT_IVY_REPO_USER"))
      keyfile <- Option(System.getenv("DEFAULT_IVY_REPO_KEYFILE"))
    } yield Resolver.sftp("UntypedPublish", host, path)(Resolver.ivyStylePatterns).as(user, file(keyfile))

  def releasePublishTo =
    Some(Resolver.url(
      "sbt-plugin-releases",
      new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"
    ))(Resolver.ivyStylePatterns))

  // Settings -----------------------------------

  def isSnapshot(version: String) =
    version.contains("-SNAPSHOT") || version.contains("-RC") || version.contains("-M")

  def defaultSettings =
    Project.defaultSettings ++
    CrossPlugin.crossBuildingSettings ++
    CrossBuilding.scriptedSettings ++
    Seq(
      fork                           := true,
      fork in scripted               := true,
      sbtPlugin                      := true,
      organization                   := "com.untyped",
      version                        := pluginsVersion,
      CrossBuilding.crossSbtVersions := Seq("0.12", "0.13"),
      resolvers                      += untyped,
      scalacOptions                 ++= Seq("-deprecation"),
      publishTo                     <<= version { v => if (isSnapshot(v)) snapshotPublishTo else releasePublishTo },
      publishMavenStyle              := false,
      scriptedBufferLog              := false,
      scalacOptions                  += "-deprecation",
      scalacOptions                  += "-unchecked",
      scriptedLaunchOpts             += "-XX:MaxPermSize=256m"
    )

  // Projects -----------------------------------

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = defaultSettings ++ Seq(publish := ())
  ) aggregate (
    sbtJs,
    sbtLess,
    sbtMustache,
    sbtRunmode
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
    id = "sbt-less",
    base = file("sbt-less"),
    settings = defaultSettings ++ Seq(
      libraryDependencies                    ++= Seq(rhino, mustache),
      libraryDependencies                    <+= (sbtVersion in sbtPlugin)(scalatest),
      // Make sure the classes for sbt-graph get packaged in the artifacts for sbt-less:
      unmanagedSourceDirectories in Compile <++= (unmanagedSourceDirectories in (sbtGraph, Compile))
    )
  )

  lazy val sbtJs = Project(
    id = "sbt-js",
    base = file("sbt-js"),
    settings = defaultSettings ++ Seq(
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
      libraryDependencies                    ++= Seq(rhino, mustache),
      libraryDependencies                    <+= (sbtVersion in sbtPlugin)(scalatest),
      // Make sure the classes for sbt-graph get packaged in the artifacts for sbt-mustache:
      unmanagedSourceDirectories in Compile <++= (unmanagedSourceDirectories in (sbtGraph, Compile))
    )
  )

  lazy val sbtRunmode = Project(
    id = "sbt-runmode",
    base = file("sbt-runmode"),
    settings = defaultSettings ++ Seq(
      // This is a Jetty/Orbit thing:
      // http://stackoverflow.com/questions/9889674/sbt-jetty-and-servlet-3-0
      classpathTypes                          ~= (_ + "orbit"),
      libraryDependencies                    <+= (sbtVersion in sbtPlugin)(webPlugin),
      libraryDependencies                    <+= (sbtVersion in sbtPlugin)(scalatest),
      // This is a Jetty/Orbit thing:
      // http://stackoverflow.com/questions/9889674/sbt-jetty-and-servlet-3-0
      libraryDependencies                     += jettyOrbit
    )
  ).dependsOn(sbtLess, sbtJs)

}
