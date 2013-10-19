name := "less-1.3.0"

scalaVersion := "2.9.2"

seq(lessSettings : _*)

(resourceManaged in (Compile, LessKeys.less)) <<= (target in Compile) { _ / "scripted" }

LessKeys.lessVersion in (Compile, LessKeys.less) := LessVersion.Less130

(includeFilter in (Compile, LessKeys.less)) := "bootstrap.less"

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
