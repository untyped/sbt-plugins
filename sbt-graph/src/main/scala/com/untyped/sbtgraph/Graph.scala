package com.untyped.sbtgraph

import java.util.Properties
import sbt._
import scala.collection._

trait Graph {

  type S <: Source

  val log: Logger
  val sourceDirs: Seq[File]
  val targetDir: File
  val templateProperties: Properties
  val downloadDir: File

  // Adding sources -----------------------------

  var sources: List[S] = Nil

  def +=(file: File): Unit =
    this += createSource(unshadowSourceFile(file))

  def +=(url: URL): Unit =
    this += createSource(downloadAndCache(url))

  private def +=(source: S): Unit =
    if(!sources.contains(source)) {
      sources = source :: sources
      parents(source).foreach(this += _)
    }

  def getSource(src: String, referredToBy: S): S =
    if(src.matches("^https?:.*")) {
      getSource(new URL(src))
    } else {
      getSource(new File(referredToBy.srcDirectory, src).getCanonicalFile)
    }

  def getSource(src: URL): S =
    getSource(downloadAndCache(src))

  def getSource(src: File): S = {
    val unshadowedSrc = unshadowSourceFile(src)
    sources find (_.src == unshadowedSrc) getOrElse createSource(unshadowedSrc)
  }

  def createSource(src: File): S

  /**
   * If there are multiple directories in our search path,
   * one source file may be shadowed by another further up the path list.
   * Retrieve the unshadowed version of any given file.
   */
  def unshadowSourceFile(file: File): File =
    splitSourceFile(file) match {
      // The file is inside a sourceDir - find an unshadowed version of it:
      case Some((fileDir, filePath)) =>
        // We've managed to split `file` into a dir and a path,
        // we should always be able to recombine them:
        findSourceFile(filePath).getOrElse(sys.error(s"Could not find ${file} on the ${pluginName} path"))

      // The file is outside sourceDirs - nothing can shadow it:
      case None =>
        file
    }

  /** Splits a source filename into a sourceDir and relative path. */
  def splitSourceFile(file: File): Option[(File, String)] =
    if(file == null) {
      None
    } else if(sourceDirs.contains(file)) {
      Some((file, ""))
    } else {
      for {
        (parentDir, parentRel) <- splitSourceFile(file.getParentFile)
      } yield {
        (parentDir, parentRel + "/" + file.getName)
      }
    }

  /**
   * Scans the available sourceDirs and finds the first file that
   * exists at the specified path.
   */
  def findSourceFile(path: String): Option[File] =
    sourceDirs.foldLeft(None : Option[File]) {
      (ans, dir) =>
        ans match {
          case None =>
            val file = new File(dir, path)
            if(file.isFile) Some(file) else None
          case ans_ => ans_
        }
    }

  /**
   * Translates the `src` filename to a `des` filename.
   * Returns `None' if `des` could not be determined.
   * This typically occurs if `src` is outside of `sourceDir`.
   */
  def srcToDes(file: File): Option[File] =
    for {
      (dir, _) <- splitSourceFile(file)
      rel      <- IO.relativize(dir, file)
    } yield {
      new File(targetDir, srcFilenameToDesFilename(rel).
          replaceAll("[.]template", "")).
          getCanonicalFile
    }

  def srcFilenameToDesFilename(filename: String): String

  // Compiling files ----------------------------

  /**
   * Compile the specified files (where they appear in this graph).
   * Return a list of the generated files.
   *
   * This method is provided as an immediate convenience to plugins
   * using Graph functionality. It obeys the contract set out for
   * SBT resource generators.
   */
  def compileAll(files: Seq[File]): Seq[File] = {
    log.debug("Source files for %s:".format(pluginName))
    files.foreach { file =>
      log.debug("  " + file)
    }

    dump()

    val sources = files.flatMap(findSource)

    sources.filter(_.requiresRecompilation) match {
      case Nil =>
        log.debug("No %s sources requiring compilation".format(pluginName))

      case toCompile =>
        val compiled = toCompile.flatMap(_.compile)

        if (compiled.length < toCompile.length) {
          sys.error("Some %s sources could not be compiled".format(pluginName))
        }
    }

    sources.flatMap(_.des)
  }

  // Downloading and caching URLs ---------------

  def downloadAndCache(url: URL): File = {
    val file = downloadDir / url.toString.replaceAll("""[^-A-Za-z0-9.]""", "_")

    if(!file.exists) {
      val content = scala.io.Source.fromInputStream(url.openStream).mkString
      IO.createDirectory(downloadDir)
      IO.write(file, content)
    }

    file
  }

  // Reasoning about sources --------------------

  def findSource(file: File): Option[S] =
    sources find (source => source.src == file)

  def sourcesRequiringRecompilation: List[S] =
    sources filter (_.requiresRecompilation)

  def parents(a: S): List[S] =
    a.parents.asInstanceOf[List[S]]

  def children(a: S): List[S] =
    sources filter(b => b.parents.contains(a))

  def ancestors(a: S): List[S] =
    postorder(a, parents)

  def descendants(a: S): List[S] =
    postorder(a, children)

  def postorder(node: S, succ: (S) => List[S]): List[S] = {
    var accum: Seq[S] = Seq()
    var visited: Seq[S] = Seq()

    def visit(node: S): Unit = {
      if(!visited.contains(node)) {
        visited = visited ++ Seq(node)
        succ(node).foreach(visit)
        accum = accum ++ Seq(node)
      }
    }

    visit(node)

    accum.toList
  }

  def pluginName: String

  def dump(): Unit = {
    log.debug("Graph for " + pluginName + ":")

    log.debug("  templateProperties:")
    log.debug("    " + templateProperties)

    log.debug("  downloadDir:")
    log.debug("    " + downloadDir)

    sources.foreach(dumpSource)
  }

  def dumpSource(source: S): Unit = {
    log.debug("  source:")

    log.debug("    src:")
    log.debug("      " + source.src)

    log.debug("    des:")
    log.debug("      " + source.des.map(_.toString).getOrElse("NONE"))

    log.debug("    templated?:")
    log.debug("      " + source.isTemplated)

    log.debug("    recompile?:")
    log.debug("      " + source.requiresRecompilation)

    log.debug("    parents:")
    parents(source).foreach(src => log.debug("      " + src))

    log.debug("    children:")
    children(source).foreach(src => log.debug("      " + src))

    log.debug("    ancestors:")
    ancestors(source).foreach(src => log.debug("      " + src))

    log.debug("    descendants:")
    descendants(source).foreach(src => log.debug("      " + src))
  }

}