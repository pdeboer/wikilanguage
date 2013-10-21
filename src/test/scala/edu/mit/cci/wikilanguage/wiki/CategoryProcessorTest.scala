package edu.mit.cci.wikilanguage.wiki

import org.junit.runner.RunWith
import org.specs._
import org.specs.runner.{JUnitSuiteRunner, JUnit}
import edu.mit.cci.wikilanguage.model.{Category, Person}


/**
 * User: pdeboer
 * Date: 10/14/13
 * Time: 8:24 PM
 */
@RunWith(classOf[JUnitSuiteRunner])
class CategoryProcessorTest extends Specification with JUnit /*with ScalaCheck*/ {
	"Category Inserter" should {
		val ci = new CategoryContentProcessor(Category("asdf")())
		val cp = new CategoryProcessor()
		"detect promising category names" in {
			ci.isPersonCategory("bla CleAnup bl11a") must beFalse
			ci.isPersonCategory("12th century deaths") must beTrue
		}

		"detect promising person names" in {
			ci.isPerson("bla bla") must beTrue
			ci.isPerson("List of people") must beFalse
			ci.isPerson("Wikipedia blabla") must beFalse
			ci.isPerson("Talk: blabla") must beFalse
		}

		"queue management works" in {
			val cat = Category("bla")()
			cp.addToQueue(cat)

			cp.isProcessed(cat) must beTrue
		}
	}
}


object CategoryProcessorTest {
	def main(args: Array[String]) {
		new CategoryProcessorTest().main(args)
	}

}
