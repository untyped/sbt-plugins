name := "variable-renaming"

logLevel := Level.Debug

seq(jsSettings : _*)

(resourceManaged in (Compile, JsKeys.js)) <<= (target in Compile) { _ / "scripted" }

JsKeys.variableRenamingPolicy in Compile := VariableRenamingPolicy.OFF

JsKeys.templateProperties in Compile := {
  val props = new java.util.Properties
  props.setProperty("test.user.name", "Mustache")
  props
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
