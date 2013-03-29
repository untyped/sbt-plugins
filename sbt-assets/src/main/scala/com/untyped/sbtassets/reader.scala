// package com.untyped.sbtassets

// // import java.util.regex.Pattern
// import java.io.File
// import sbt._
// import scala.util.matching.Regex

// /** Given the absolute and relative path to a source file, return a Source that represents that file. */
// trait SourceReader extends Function2[File, String, Source]

// /** Common abstraction over source readers. */
// trait SimpleSourceReader {
//   def filenameExtension: String

//   def requireRegex: Regex

//   def apply(file: File, path: String) =
//     Source(path, file, dependencies(abs))

//   def name(file: File) =
//     Util.normalizeSlashes(Util.stripExtension(file.getPath, filenameExtension))

//   def parseRequire(line: String): Option[String] =
//     requireRegex.findAllIn(line).matchData.map(data => data.group(1)).toList.headOption

//   def dependencies(abs: File) =
//     IO.readLines(abs).map(_.trim).toList.flatMap(parseRequire _)
// }

// object CoffeeReader extends SimpleSourceReader {
//   val filenameExtension = ".coffee"
//   val requireRegex = """ #[ \t]*require[ \t]*"([^"]+)" """.trim.r
// }

// object JsReader extends SimpleSourceReader {
//   val filenameExtension = ".js"
//   val requireRegex = """ //[ \t]*require[ \t]*"([^"]+)" """.trim.r
// }
