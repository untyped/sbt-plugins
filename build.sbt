sbtPlugin    := true

organization := "com.github.btd"

name         := "sbt-less-plugin"

version      := "0.0.1"

licenses     += ("Apache License, Version 2.0", url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

seq(ScriptedPlugin.scriptedSettings: _*)

scalacOptions           ++= DefaultOptions.scalac

scalacOptions in Compile += Opts.compile.deprecation

scalacOptions in Compile += Opts.compile.unchecked

publishArtifact in (Compile, packageDoc) := false

publishArtifact in (Compile, packageSrc) := false

libraryDependencies += "org.lesscss" % "lesscss" % "1.3.0"

publishMavenStyle := false

scriptedBufferLog := false

