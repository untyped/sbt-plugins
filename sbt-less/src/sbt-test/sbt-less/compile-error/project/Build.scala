import sbt._
import sbt.Keys._
import com.untyped.sbtless.Plugin._
import com.untyped.sbtless.Plugin.LessKeys._

object Build extends Build {

  val runTest = TaskKey[Unit]("run-test", "Run the scripted test.")

  def runTestTask = // : Def.Initialize[Task[Unit]] =
    (less in Compile, target in Compile) map {
      (compiledCss, targetDir) =>
        IO.write(targetDir / "run-test-task-completed", "run-test-task-completed")
    }

  lazy val main = Project(
    id = "test-project",
    base = file("."),
    settings =
      Project.defaultSettings ++
      com.untyped.sbtless.Plugin.lessSettings ++
      Seq(
        logLevel := Level.Debug,
        (resourceManaged in (Compile, less)) <<= (target in Compile) { _ / "scripted" },
        runTest <<= runTestTask
      )
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
}