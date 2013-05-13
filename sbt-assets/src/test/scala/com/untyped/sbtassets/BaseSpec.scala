package com.untyped.sbtassets

import org.scalatest._
import org.scalatest.matchers._
import sbt._

trait BaseSpec extends FunSpec with MustMatchers {
  val cwd = file(".")

  def createTemporaryFiles(spec: (String, String) *): File = {
    val dir = IO.createTemporaryDirectory
    spec.foreach { case (path, content) =>
      val file = dir / path
      IO.write(file, content)
    }
    dir
  }

  object log extends Logger {
    def trace(t: => Throwable) = t.printStackTrace
    def success(message: => String) = println(message)
    def log(level: Level.Value, message: => String) = println(level + ": " + message)
  }
}
