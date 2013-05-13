package com.untyped.sbtassets

import sbt._
import scala.collection.mutable

case class CompileState() {
  val completed = mutable.Set[Rule]()
}
