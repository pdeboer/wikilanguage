package edu.mit.cci.wikilanguage.wiki

import org.junit.runner.RunWith
import org.specs._
import org.specs.runner.{JUnitSuiteRunner, JUnit}
import edu.mit.cci.wikilanguage.model.{Category, Person}
import edu.mit.cci.wikilanguage.db.DAO


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


	"PersonDiscoverer" should {
		"allow peoples names" in {
			val w = new CategoryContentProcessor(null)
			w.isPerson("Elmar_Ledergerber") must beTrue
		}

		"discover all the categories" in {
			val cp = new CategoryContentProcessor(Category("Category:1944_births")(), insertDB=true)

			val res = cp.call()
			res.people.contains(Person("Elmar Ledergerber")()) must beTrue
		}
	}
}


object CategoryProcessorTest {
	def main(args: Array[String]) {
		new CategoryProcessorTest().main(args)
	}

}
