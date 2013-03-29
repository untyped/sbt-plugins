// package com.untyped.sbtassets

// import java.io.{ File, PrintWriter, FileWriter }

// trait CompileAll extends Function1[Sources, Sources]

// /** Compile a source relative to a set of sources, producing another source. */
// trait CompileOne extends Function2[Sources, Source, Source] {
//   def lift = new CompileAll {
//     def apply(in: Sources) =
//       Sources(in.sources.map(source => CompileOne.this.apply(in, source)))
//   }
// }

// trait CompileLast extends Function1[Sources, File]

// case class Coffee(val root: File) extends CompileOne {
//   def apply(source: Source, sources: Sources) = {
//     val des = new File(root, source.name)
//     des.createNewFile
//     Util.exec("coffee", "-bcp", source.path.getPath, "--", des.getPath) {
//       Source(source.name, des, source.original, source.dependencies)
//     }
//   }
// }

// class Concat(val des: File) extends CompileLast {
//   def apply(in: Sources) =
//     Util.exec(Seq("cat") ++ in.orderedSources.map(_.path.getPath) ++ Seq(">", des.getPath)) {
//       des
//     }
// }