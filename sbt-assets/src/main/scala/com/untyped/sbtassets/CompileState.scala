package com.untyped.sbtassets

import sbt._
import scala.collection.mutable

case class CompileState() {
  val completed = mutable.Set[Rule]()

  object log extends ProcessLogger {
    def info(in: => String) = println(in)
    def error(in: => String) = println(in)
    def buffer[T](f: => T): T = f
  }
}
