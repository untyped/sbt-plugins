logLevel := Level.Debug

seq(mustacheSettings : _*)

(resourceManaged in (Compile, MustacheKeys.mustache)) <<= (target in Compile) { _ / "scripted" }

MustacheKeys.templateProperties in Compile := {
  val props = new java.util.Properties
  props.setProperty("test.user.name", "Mustache")
  props
}

includeFilter in (Compile, MustacheKeys.mustache) := "*.html" || "*.md"

MustacheKeys.filenameSuffix in Compile := ".out"

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
