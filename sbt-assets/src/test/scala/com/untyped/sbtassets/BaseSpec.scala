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
}
