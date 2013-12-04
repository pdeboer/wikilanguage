package edu.mit.cci.wikilanguage.wiki

import org.junit.runner.RunWith
import org.specs._
import org.specs.runner.{JUnitSuiteRunner, JUnit}
import edu.mit.cci.wikilanguage.model.{Person, Category}
import java.util.Date
import java.text.SimpleDateFormat

//import org.scalacheck.Gen


@RunWith(classOf[JUnitSuiteRunner])
class PersonLinkAnnotatorTest extends Specification with JUnit /*with ScalaCheck*/ {
	"PersonLinkTimestampDeterminer's determine function " should {
		"be able to find easy from-time" in {
			???
		}
	}

	"PersonLinkTimestampDeterminer's dateFromCategory function " should {
		val p = new PersonLinkTimestampDeterminer(null)
		val sdf = new SimpleDateFormat("yyyy G")

		"be able to extract a basic date from birth year" in {
			sdf.format(p.dateFromCategory(Category("Category:1990_births")())) == "1990 AD" must beTrue
		}

		"be able to deal with deaths" in {
			sdf.format(p.dateFromCategory(Category("Category:1990_deaths")())) == "1990 AD" must beTrue
		}

		"return null if confusing stuff happens" in {
			p.dateFromCategory(Category("Category:asdbasdbasdb_123_asdgas")()) must beNull
		}

		"deal with centuries AD" in {
			sdf.format(p.dateFromCategory(Category("Category:17th-century_births")())) == "1700 AD" must beTrue
		}

		"deal with BC year numbers" in {
			sdf.format(p.dateFromCategory(Category("Category:46_BC_births")())) == "0046 BC" must beTrue
		}

		"deal with BC centuries" in {
			sdf.format(p.dateFromCategory(Category("Category:46th_century_BC_births")())) == "4600 BC" must beTrue
		}
	}
}

object PersonLinkAnnotatorTest {
	def main(args: Array[String]) {
		new PersonLinkAnnotatorTest().main(args)
	}
}
