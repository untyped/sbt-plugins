package com.untyped.sbssass

import org.scalatest.{Matchers, FunSpec}
import com.untyped.sbtsass.SassSource._

class SassSourceImportParsingSpec extends FunSpec with Matchers {

  describe("The import regex") {

    it("should match partial imports") {
      """@import "vars"; """  should fullyMatch regex importRegex
      """ @import "vars"; """ should fullyMatch regex importRegex
      """ @import 'vars'; """ should fullyMatch regex importRegex
    }

    it("should match file imports") {
      """@import "master.scss"; """  should fullyMatch regex importRegex
      """ @import "master.scss"; """ should fullyMatch regex importRegex
      """ @import 'master.scss'; """ should fullyMatch regex importRegex
    }

    it("should match several imports on one line") {
      """@import "master.scss", 'vars'; """  should fullyMatch regex importRegex
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

}
