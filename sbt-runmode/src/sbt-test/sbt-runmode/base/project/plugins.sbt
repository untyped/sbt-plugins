libraryDependencies <+= sbtVersion(v => v match {
  case v if v startsWith "0.12" => Defaults.sbtPluginExtra("com.earldouglas" % "xsbt-web-plugin" % "0.4.2", "0.12", "2.9.2")
  case v if v startsWith "0.13" => Defaults.sbtPluginExtra("com.earldouglas" % "xsbt-web-plugin" % "0.4.2", "0.13", "2.10")
  case v => throw new Exception("Build.scala: don't know what version of xsbt-web-plugin to use for SBT " + v)
})

addSbtPlugin("com.untyped" % "sbt-runmode" % "0.7-M2")
