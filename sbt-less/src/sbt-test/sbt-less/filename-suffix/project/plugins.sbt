val pluginVersion = Option(System.getProperty("plugin.version")) getOrElse
  sys.error("'plugin.version' property not specified in scriptedLaunchOpts")

addSbtPlugin("com.untyped" % "sbt-less" % pluginVersion)