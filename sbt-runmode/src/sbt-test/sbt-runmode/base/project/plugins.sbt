resolvers ++= Seq(
  Classpaths.typesafeResolver,
  "Web plugin" at "http://siasia.github.com/maven2"
)

libraryDependencies <+= sbtVersion(v => v match {
  case "0.11.0"                 => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.0-0.2.8"
  case "0.11.1"                 => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.1-0.2.10"
  case "0.11.2"                 => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.2-0.2.11"
  case "0.11.3"                 => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.3-0.2.11.1"
  case v if v startsWith "0.12" => "com.github.siasia" %% "xsbt-web-plugin" % "0.12.0-0.2.11.1"
  case v => throw new Exception("Build.scala: don't know what version of xsbt-web-plugin to use for SBT " + v)
})

addSbtPlugin("com.untyped" % "sbt-runmode" % "0.6-M6")
