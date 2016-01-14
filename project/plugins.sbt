resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin("net.virtual-void" % "sbt-cross-building" % "0.8.1")

addSbtPlugin("com.eed3si9n" % "sbt-dirty-money" % "0.0.1")

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")
