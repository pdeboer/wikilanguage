package edu.mit.cci.wikilanguage.model

import org.junit.runner.RunWith
import org.specs._
import org.specs.runner.{JUnitSuiteRunner, JUnit}

//import org.scalacheck.Gen


@RunWith(classOf[JUnitSuiteRunner])
class WikiCategoryTest extends Specification with JUnit /*with ScalaCheck*/ {
  "WikiCategory" should {
    "be able to fetch contents of a simple category" in {
      val wc = new WikiCategory("Category:3rd-century people")
      wc.contents.length mustEq 16
    }

    "prepare a correct URL" in {
      val wc = new WikiCategory("Category:asdf asdf")
      wc.categoryClean mustEqual "Category:asdf%20asdf"
    }

    "be able to fetch contents of multi-page categories" in {
      val wc = new WikiCategory("Category:19th-century_German_painters")
      wc.contents.length mustEq 501
    }
  }
}

object WikiCategoryTest {
  def main(args: Array[String]) {
    new WikiCategoryTest().main(args)
  }
}
