package com.untyped.sbtassets

import org.scalatest._
import org.scalatest.matchers._
import sbt._

class DependencyReaderSuite extends FunSuite with MustMatchers {

  def dependencies[T](
    reader: DependencyReader,
    prefix: String,
    postfix: String
  )(content: String): List[String] = {
    IO.withTemporaryFile(prefix, postfix) { file =>
      IO.write(file, content)
      reader(file)
    }
  }

  test("CoffeeReader") {
    dependencies(CoffeeReader, "test", ".coffee") {
      """
      |# require "foo/bar"
      |  # require "../baz"
      |// require "error"
      |alert '# require "error"'
      """.trim.stripMargin
    } must equal (List("foo/bar", "../baz"))
  }

  test("JsReader") {
    dependencies(JsReader, "test", ".js") {
      """
      |// require "foo/bar"
      |  // require "../baz"
      |# require "error"
      |alert '// require "error"'
      """.trim.stripMargin
    } must equal (List("foo/bar", "../baz"))
  }

}
