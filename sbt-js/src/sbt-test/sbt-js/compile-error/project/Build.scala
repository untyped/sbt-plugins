import sbt._
import sbt.Keys._
import sbt.Project.Initialize
import com.untyped.sbtjs.Plugin._
import com.untyped.sbtjs.Plugin.JsKeys._

object Build extends Build {
  
  val runTest = TaskKey[Unit]("run-test", "Run the scripted test.")
  
  def runTestTask: Initialize[Task[Unit]] =
    (js in Compile, target in Compile) map {
      (compiledJs, targetDir) =>
        IO.write(targetDir / "run-test-task-completed", "run-test-task-completed")
    }
  
  lazy val main = Project(
    id = "test-project",
    base = file("."),
    settings = 
      Project.defaultSettings ++ 
      com.untyped.sbtjs.Plugin.jsSettings ++
      Seq(
        logLevel := Level.Debug,
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