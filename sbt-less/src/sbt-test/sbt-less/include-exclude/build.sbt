name := "include-exclude"

scalaVersion := "2.9.1"

seq(lessSettings : _*)

(includeFilter in (Compile, LessKeys.less)) := ("*.include.less": FileFilter)
                                                                             
(excludeFilter in (Compile, LessKeys.less)) := ("*.exclude*": FileFilter)    

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
