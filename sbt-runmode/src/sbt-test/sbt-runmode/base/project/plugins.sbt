resolvers ++= Seq(
  Classpaths.typesafeResolver
)

addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.4.2")

addSbtPlugin("com.untyped" %% "sbt-runmode" % "latest.integration")
