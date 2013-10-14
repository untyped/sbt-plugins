resolvers ++= Seq(
  Classpaths.typesafeResolver
)

addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.5.0-SNAPSHOT")

addSbtPlugin("com.untyped" %% "sbt-runmode" % "latest.integration")
