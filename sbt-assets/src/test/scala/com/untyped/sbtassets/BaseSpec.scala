package com.untyped.sbtassets

import org.scalatest._
import org.scalatest.matchers._
import sbt._

trait BaseSpec extends FunSpec with MustMatchers {
  def createTemporaryFiles(spec: (String, String) *): File = {
    val dir = IO.createTemporaryDirectory
    spec.foreach { case (path, content) =>
      val file = dir / path
      IO.write(file, content)
    }
    dir
  }

  // Logging:

  def logEnabled = false

  object log extends Logger {
    def trace(t: => Throwable) =
      if(logEnabled) t.printStackTrace

    def success(message: => String) =
      if(logEnabled) println(message)

    def log(level: Level.Value, message: => String) =
      if(logEnabled) println(level + ": " + message)
  }

  // Custom assertions:

  class FileExistsMatcher extends Matcher[java.io.File] {
    def apply(left: java.io.File) = {
      val fileOrDir = if (left.isFile) "file" else "directory"

      val failureMessageSuffix =
        fileOrDir + " named " + left.getName + " did not exist"

      val negatedFailureMessageSuffix =
        fileOrDir + " named " + left.getName + " existed"

      MatchResult(
        left.exists,
        "The " + failureMessageSuffix,
        "The " + negatedFailureMessageSuffix,
        "the " + failureMessageSuffix,
        "the " + negatedFailureMessageSuffix
      )
    }
  }

  val exist = new FileExistsMatcher

  class FileChangeMatcher(val fn: () => Any) extends Matcher[java.io.File] {
    def apply(left: java.io.File) = {
      val fileOrDir = if (left.isFile) "file" else "directory"

      val failureMessageSuffix =
        fileOrDir + " named " + left.getName + " did not change"

      val negatedFailureMessageSuffix =
        fileOrDir + " named " + left.getName + " changed"

      val start = left.lastModified
      fn()
      val end = left.lastModified

      MatchResult(
        end > start,
        "The " + failureMessageSuffix,
        "The " + negatedFailureMessageSuffix,
        "the " + failureMessageSuffix,
        "the " + negatedFailureMessageSuffix
      )
    }
  }

  def changeDuring(fn: => Any) = new FileChangeMatcher(() => fn)
}
