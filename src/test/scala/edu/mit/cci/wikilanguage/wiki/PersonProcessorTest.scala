package edu.mit.cci.wikilanguage.wiki

import org.junit.runner.RunWith
import org.specs._
import org.specs.runner.{JUnitSuiteRunner, JUnit}
import edu.mit.cci.wikilanguage.model.active.WikiCategory
import edu.mit.cci.util.U

//import org.scalacheck.Gen


@RunWith(classOf[JUnitSuiteRunner])
class PersonProcessorTest extends Specification with JUnit /*with ScalaCheck*/ {
  "PersonProcessor" should {
    "find outlinks for a basic use case" in {
      //TODO test
    }
  }
}

object PersonProcessorTest {
  def main(args: Array[String]) {
    new PersonProcessorTest().main(args)
  }
}
