
resolvers ++= Seq(
  Classpaths.typesafeResolver,
  "Untyped"    at "http://repo.untyped.com",
  "Web plugin" at "http://siasia.github.com/maven2"
)

libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % (v+"-0.2.4"))

addSbtPlugin("untyped" % "sbt-runmode" % "0.1-SNAPSHOT")
