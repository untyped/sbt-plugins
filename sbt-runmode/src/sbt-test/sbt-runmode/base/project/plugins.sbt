
resolvers ++= Seq(
  Classpaths.typesafeResolver,
  "Untyped"    at "http://repo.untyped.com",
  "Web plugin" at "http://siasia.github.com/maven2"
)

libraryDependencies <+= sbtVersion(v => v match {
  case "0.11.0" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.0-0.2.8"
  case "0.11.1" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.1-0.2.10"
  case "0.11.2" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.2-0.2.11"
})

addSbtPlugin("untyped" %% "sbt-runmode" % "0.2")
