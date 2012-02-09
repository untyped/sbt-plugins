package untyped.graph

import java.util.Properties
import sbt._
import scala.collection._

trait Graph {

  type S <: Source

  val log: Logger
  val sourceDir: File
  val targetDir: File
  val templateProperties: Properties
  val downloadDir: File

  // Adding sources -----------------------------

  var sources: List[S] = Nil

  def +=(file: File): Unit =
    this += createSource(file)

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

  def getSource(src: File): S =
    sources find (_.src == src) getOrElse createSource(src)

  def createSource(src: File): S

  /**
   * Translates the `src` filename to a `des` filename.
   * Returns `None' if `des` could not be determined.
   * This typically occurs if `src` is outside of `srcDirectory`.
   */
  def srcToDes(file: File): Option[File] =
    for(rel <- IO.relativize(sourceDir, file)) yield {
      new File(targetDir, srcFilenameToDesFilename(rel).
          replaceAll("[.]template", "")).
          getCanonicalFile
    }

  def srcFilenameToDesFilename(filename: String): String

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

  def parents(a: S): List[S] =
    a.parents.asInstanceOf[List[S]]

  def children(a: S): List[S] =
    sources filter(b => b.parents.contains(a))

  def ancestors(a: S): List[S] =
    postorder(a, parents _)

  def descendents(a: S): List[S] =
    postorder(a, children _)

  def postorder(node: S, succ: (S) => List[S]): List[S] = {
    var accum: Seq[S] = Seq()
    var visited: Seq[S] = Seq()

    def visit(node: S): Unit = {
      if(!visited.contains(node)) {
        visited = visited ++ Seq(node)
        succ(node).foreach(visit _)
        accum = accum ++ Seq(node)
      }
    }

    visit(node)

    accum.toList
  }

  def pluginName: String

  def dump: Unit = {
    log.debug("Graph for " + pluginName + ":")

    log.debug("  templateProperties:")
    log.debug("    " + templateProperties)

    log.debug("  downloadDir:")
    log.debug("    " + downloadDir)

    sources.foreach(dumpSource _)
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

    log.debug("    descendents:")
    descendents(source).foreach(src => log.debug("      " + src))
  }

}