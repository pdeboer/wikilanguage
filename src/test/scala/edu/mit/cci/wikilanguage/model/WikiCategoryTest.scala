package edu.mit.cci.wikilanguage.model

import org.junit.runner.RunWith
import org.specs._
import org.specs.runner.{JUnitSuiteRunner, JUnit}
import edu.mit.cci.wikilanguage.model.active.WikiCategory
import edu.mit.cci.wikilanguage.wiki.CategoryProcessor

//import org.scalacheck.Gen


@RunWith(classOf[JUnitSuiteRunner])
class WikiCategoryTest extends Specification with JUnit /*with ScalaCheck*/ {
	"WikiCategory" should {

		"be able to fetch contents of a simple category" in {
			val wc = new WikiCategory("Category:3rd-century people")
			wc.contents.length > 5 must beTrue
		}

		"prepare a correct URL" in {
			val wc = new WikiCategory("Category:asdf asdf")
			wc.categoryClean mustEqual "Category:asdf%20asdf"
		}

		"be able to fetch contents of multi-page categories" in {
			val wc = new WikiCategory("Category:19th-century_German_painters")
			wc.contents.length > 400 must beTrue
		}

		"be able to fetch contents of special char-categories" in {
			val wc = new WikiCategory("Category:People_from_Bart%C4%B1n")
			wc.contents
			wc.contents.length > 1 must beTrue
		}
	}

}

object WikiCategoryTest {
	def main(args: Array[String]) {
		new WikiCategoryTest().main(args)
	}
}
