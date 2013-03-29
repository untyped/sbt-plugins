package com.untyped

import sbt._

package object sbtassets {

  /** pathToFind myPath => option(soureFile) */
  type Resolver = (String, String) => Option[File]

}