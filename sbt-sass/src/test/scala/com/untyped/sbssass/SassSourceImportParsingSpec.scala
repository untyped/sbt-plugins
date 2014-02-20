package com.untyped.sbssass

import org.scalatest.{Matchers, FunSpec}
import com.untyped.sbtsass.SassSource._
import com.untyped.sbtsass.{SassSource, Graph}
import sbt.IO

class SassSourceImportParsingSpec extends FunSpec with Matchers {

  describe("The import regex") {

    it("should match partial imports") {
      """@import "vars"; """  should fullyMatch regex onlinerImportRegex
      """ @import "vars"; """ should fullyMatch regex onlinerImportRegex
      """ @import 'vars'; """ should fullyMatch regex onlinerImportRegex
    }

    it("should match file imports") {
      """@import "master.scss"; """  should fullyMatch regex onlinerImportRegex
      """ @import "master.scss"; """ should fullyMatch regex onlinerImportRegex
      """ @import 'master.scss'; """ should fullyMatch regex onlinerImportRegex
    }

    it("should match several imports on one line") {
      """@import "master.scss", 'vars'; """  should fullyMatch regex onlinerImportRegex
    }

  }

  describe("The import parser") {

    it("should parse partial imports") {
      parseImport("""@import "vars"; """)   should be (List("vars"))
      parseImport(""" @import "vars"; """)  should be (List("vars"))
      parseImport(""" @import 'vars'; """)  should be (List("vars"))
    }

    it("should parse file imports") {
      parseImport("""@import "master.scss"; """)   should be (List("master.scss"))
      parseImport(""" @import "master.scss"; """)  should be (List("master.scss"))
      parseImport(""" @import 'master.scss'; """)  should be (List("master.scss"))
    }

    it("should parse several imports on one line") {
      parseImport("""@import "master.scss", 'vars', "hepp"; """) should be (List("master.scss", "vars", "hepp"))
    }

  }

  describe("The imports from file parser") {

    val graph = Graph(
      log                = null,
      sourceDirs         = Seq(new sbt.File(".")),
      targetDir          = null,
      templateProperties = null,
      downloadDir        = null,
      sassVersion        = null,
      prettyPrint        = false,
      useCommandLine     = false
    )

    def findParents(file: sbt.File) =  SassSource(graph, file).parents.map(_.src.getName)

    it("should parse partial imports") {
      val partialImportFile =
        createFileWithImports(
          """
            | @import 'vars';
          """.stripMargin)

      findParents(partialImportFile) should be (List("_vars.scss"))
    }

    it("should parse file imports") {
      val fileImportFile =
        createFileWithImports(
          """
            | @import 'master.scss';
          """.stripMargin)
      findParents(fileImportFile) should be (List("master.scss"))
    }

    it("should parse several imports on one line") {
      val severalImportsOnOneLineFile =
        createFileWithImports(
          """
            | @import "master.scss", 'vars', "other/hepp";
          """.stripMargin)
      findParents(severalImportsOnOneLineFile) should be (List("master.scss", "_vars.scss", "_hepp.scss"))
    }

    it("should parse several oneliner imports") {
      val severalOnelinerImportsFile =
        createFileWithImports(
          """
            | @import "master.scss";
            | @import 'vars', "other/hepp";
          """.stripMargin)
      findParents(severalOnelinerImportsFile) should be (List("master.scss", "_vars.scss", "_hepp.scss"))
    }

    it("should parse multiliner imports") {
      val severalOnelinerImportsFile =
        createFileWithImports(
          """
            | @import "master.scss";
            | @import 'vars',
            | "other/hepp";
          """.stripMargin)
      findParents(severalOnelinerImportsFile) should be (List("master.scss", "_vars.scss", "_hepp.scss"))
    }

    it("should parse no imports") {
      val severalOnelinerImportsFile =
        createFileWithImports("")
      findParents(severalOnelinerImportsFile) should be (List())
    }

    def createFileWithImports(importSection: String) = {
      val file = new sbt.File(getClass.getClassLoader.getResource("main.scss").getPath)
      IO.write(file, importSection +
        """
          |@if
          |@extend
          |@debug
          |@warn
          |@for
          |@each
          |@while
          |@include
          |@function
          |@return
          |
          |@mixin box-shadow($value) {}
          |
          |#extra {
          |  width: 100px;
          |}
        """.stripMargin)
      file
    }

  }

}
