package com.untyped.sbssass

import org.scalatest.{Matchers, FunSpec}
import com.untyped.sbtsass.SassSource._
import scala.reflect.io.File
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

    it("should parse partial imports") {
      val partialImportFile =
        createImportFile(
          """
            | @import 'vars';
          """.stripMargin)

      parseImportsFromFile(partialImportFile) should be (List("vars"))
    }

    it("should parse file imports") {
      val fileImportFile =
        createImportFile(
          """
            | @import 'master.scss';
          """.stripMargin)
      parseImportsFromFile(fileImportFile) should be (List("master.scss"))
    }

    it("should parse several imports on one line") {
      val severalImportsOnOneLineFile =
        createImportFile(
          """
            | @import "master.scss", 'vars', "hepp";
          """.stripMargin)
      parseImportsFromFile(severalImportsOnOneLineFile) should be (List("master.scss", "vars", "hepp"))
    }

    it("should parse several oneliner imports") {
      val severalOnelinerImportsFile =
        createImportFile(
          """
            | @import "master.scss";
            | @import 'vars', "hepp";
          """.stripMargin)
      parseImportsFromFile(severalOnelinerImportsFile) should be (List("master.scss", "vars", "hepp"))
    }

    it("should parse multiliner imports") {
      val severalOnelinerImportsFile =
        createImportFile(
          """
            | @import "master.scss";
            | @import 'vars',
            | "hepp";
          """.stripMargin)
      parseImportsFromFile(severalOnelinerImportsFile) should be (List("master.scss", "vars", "hepp"))
    }

    def createImportFile(importSection: String) = {
      val file = File.makeTemp().jfile
      IO.append(file,
        importSection +
        """
          |#extra {
          | width: 100px;
          |}
        """.stripMargin)
      file
    }

  }

}
