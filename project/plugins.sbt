libraryDependencies <+= sbtVersion { v =>
  v match {
    case "0.11.0" => "org.scala-tools.sbt" %% "scripted-plugin" % v
    case "0.11.1" => "org.scala-tools.sbt" %% "scripted-plugin" % v
    case "0.11.2" => "org.scala-tools.sbt" %% "scripted-plugin" % v
    case "0.11.3" => "org.scala-sbt"        % "scripted-plugin" % v
    case "0.12.1" => "org.scala-sbt"        % "scripted-plugin" % v
    case other    => throw new Exception("plugins.sbt: don't know what version of scripted-plugin to use for SBT " + other)
  }
}

addSbtPlugin("com.eed3si9n" % "sbt-dirty-money" % "0.0.1")

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.2.0-SNAPSHOT")
