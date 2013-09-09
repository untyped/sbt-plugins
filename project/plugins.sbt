resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

// Replaced by the version of scripted-plugin in sbt-cross-building:
// libraryDependencies <+= sbtVersion { v =>
//   v match {
//     case "0.11.0"                 => "org.scala-tools.sbt" %% "scripted-plugin" % v
//     case "0.11.1"                 => "org.scala-tools.sbt" %% "scripted-plugin" % v
//     case "0.11.2"                 => "org.scala-tools.sbt" %% "scripted-plugin" % v
//     case v if v startsWith "0.12" => "org.scala-tools.sbt" %% "scripted-plugin" % v
//     case v if v startsWith "0.13" => "org.scala-sbt" %% "scripted-plugin" % v
//     case other => throw new Exception("plugins.sbt: don't know what version of scripted-plugin to use for SBT " + other)
//   }
// }

addSbtPlugin("net.virtual-void" % "sbt-cross-building" % "0.8.0")

addSbtPlugin("com.eed3si9n" % "sbt-dirty-money" % "0.0.1")
