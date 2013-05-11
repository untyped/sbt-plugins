package com.untyped.sbtassets

import sbt._
import scala.collection.mutable

/** Rule that gets its sources straight from the filesystem. */
trait Selector extends Rule {
  /** Cache of last modified timestamps. */
  val cache = mutable.HashMap[Path, Long]()

  def compile = {
    val ans =
      sources
      // .filter { source =>
      //   val modified = source.file.lastModified
      //   if(modified > cache.getOrElse(source.path, 0L)) {
      //     cache.put(source.path, modified)
      //     true
      //   } else {
      //     false
      //   }
      // }
    println("selector " + ans.mkString(" "))
    ans
  }
}
