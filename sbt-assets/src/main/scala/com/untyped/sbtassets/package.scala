package com.untyped

import sbt._

package object sbtassets {

  /** Resolvers resolve path names into files.
   */
  type Resolver = Path => Option[File]
}
