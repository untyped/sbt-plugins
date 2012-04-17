libraryDependencies <+= sbtVersion(v=>
  "org.scala-tools.sbt" %% "scripted-plugin" % v
)

resolvers += "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.0.0")