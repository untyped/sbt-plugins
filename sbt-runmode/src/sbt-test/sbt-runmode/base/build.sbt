import RunModeKeys._

scalaVersion := "2.9.1"

logLevel := Level.Debug

seq(webSettings : _*)

seq(runModeSettings : _*)

libraryDependencies ++= {
  val liftVersion = "2.4-M4"
  Seq(
    "org.mortbay.jetty" % "jetty" % "6.1.22" % "container",
    "javax.servlet" % "servlet-api" % "2.5" % "provided"
  )
}

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
