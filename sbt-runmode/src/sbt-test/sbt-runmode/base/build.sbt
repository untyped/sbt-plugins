import RunModeKeys._

scalaVersion := "2.10.3"

logLevel := Level.Debug

seq(webSettings : _*)

seq(runModeSettings : _*)

// This is a Jetty/Orbit thing:
// http://stackoverflow.com/questions/9889674/sbt-jetty-and-servlet-3-0
classpathTypes ~= (_ + "orbit")

// This is a Jetty/Orbit thing:
// http://stackoverflow.com/questions/9889674/sbt-jetty-and-servlet-3-0
libraryDependencies ++= Seq(
  "org.eclipse.jetty" % "jetty-webapp" % "8.1.7.v20120910" % "container,test",
  "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,compile" artifacts Artifact("javax.servlet", "jar", "jar")
)

InputKey[Unit]("contents") <<= inputTask { (argsTask: TaskKey[Seq[String]]) =>
  (argsTask, streams) map { (args, out) =>
    args match {
      case Seq(actual, expected) =>
        if(IO.read(file(actual)).trim.equals(IO.read(file(expected)).trim)) {
          out.log.debug("Contents match")
        } else {
          error("\nContents of %s\n%s\ndoes not match %s\n%s\n".format(
                actual,
                IO.read(file(actual)),
                expected,
                IO.read(file(expected))))
        }
    }
  }
}
