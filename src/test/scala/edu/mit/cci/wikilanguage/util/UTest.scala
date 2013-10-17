package edu.mit.cci.wikilanguage.util

import org.junit.runner.RunWith
import org.specs._
import org.specs.runner.{JUnitSuiteRunner, JUnit}
import edu.mit.cci.wikilanguage.model.active.WikiCategory
import edu.mit.cci.util.U

//import org.scalacheck.Gen


@RunWith(classOf[JUnitSuiteRunner])
class UTest extends Specification with JUnit /*with ScalaCheck*/ {
  "U" should {
    "escape URLs correctly" in {
      U.entityEscape("bla bla") mustEqual "bla%20bla"
      U.entityEscape("bluppäöü") mustEqual "%C3%A4%C3%B6%C3%BC"
    }
  }
}

object WikiCategoryTest {
  def main(args: Array[String]) {
    new UTest().main(args)
  }
}
