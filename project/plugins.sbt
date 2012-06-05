libraryDependencies <+= sbtVersion { v => v match {
  case "0.11.0" => "org.scala-tools.sbt" %% "scripted-plugin" % v
  case "0.11.1" => "org.scala-tools.sbt" %% "scripted-plugin" % v
  case "0.11.2" => "org.scala-tools.sbt" %% "scripted-plugin" % v
  case "0.11.3" => "com.scala-sbt"       %% "scripted-plugin" % v
}}