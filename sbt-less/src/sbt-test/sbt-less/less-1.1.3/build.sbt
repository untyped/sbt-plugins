name := "less-1.1.3"

scalaVersion := "2.9.1"

seq(lessSettings : _*)

LessKeys.lessVersion in (Compile, LessKeys.less) := LessVersion.Less113

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
