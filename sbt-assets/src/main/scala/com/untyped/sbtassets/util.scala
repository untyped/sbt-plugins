// package com.untyped.sbtassets

// import java.io.File
// import java.lang.System
// import java.util.regex.Pattern
// import scala.sys.process.Process

// object Util {
//   val separator = File.separatorChar.toString
//   val separatorRegex = Pattern.quote(separator).r
//   val extensionRegex = ("[.]" + Pattern.quote(separator) + "+$").r

//   def stripExtension(path: String, ext: String) =
//     extensionRegex.replaceFirstIn(path, "")

//   def normalizeSlashes(path: String) =
//     if(separator == "/") path else separatorRegex.replaceAllIn(path, "/")

//   def exec[T](args: String*)(fn: => T): T =
//     exec(args.toList)(fn)

//   def exec[T](args: Seq[String])(fn: => T): T =
//     (Process(args) !) match {
//       case 0 => fn
//       case n => sys.error("Command '" + args.mkString(" ") + "' returned " + n)
//     }
// }