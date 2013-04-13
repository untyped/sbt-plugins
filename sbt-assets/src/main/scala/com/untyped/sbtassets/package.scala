package com.untyped

import sbt._

package object sbtassets {

  /** Resolvers resolve path names into files.
   *
   * Paths may be specified absolutely or relative to another path.
   * For example, "foo" or "../foo" as opposed "/foo".
   *
   * The frame of reference for relative paths is passed as a second argument.
   *
   * pathToFind myPath => option(soureFile)
   */
  type Resolver = (String, String) => Option[File]

  /** Dependency readers scan files for dependencies and return a list of paths.
   *
   * The isDefinedAt method provides a mechanism for telling whether the file
   * is in the right format for this reader (normally by testing the filename).
   */
  type DependencyReader = PartialFunction[File, List[String]]

}