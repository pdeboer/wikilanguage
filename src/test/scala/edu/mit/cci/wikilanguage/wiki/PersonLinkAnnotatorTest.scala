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
	val sdf = new SimpleDateFormat("yyyy G")

	"PersonLinkTimestampDeterminer's determine function " should {
		"be able to find easy from-time" in {
			val p = Person("test")()
			p.categories = Array( Category("Category:1990_births")(), Category("asbasbd")() )

			val d = new PersonLinkTimestampDeterminer(p)
			sdf.format(d.determine.from) == "1990 AD" must beTrue
			d.determine.to must beNull
		}

		"be able to find easy to-time" in {
			val p = Person("test")()
			p.categories = Array( Category("Category:1990_deaths")(), Category("asbasbd")() )

			val d = new PersonLinkTimestampDeterminer(p)
			sdf.format(d.determine.to) == "1990 AD" must beTrue
			d.determine.from must beNull
		}

		"be able to find easy from-to-time" in {
			val p = Person("test")()
			p.categories = Array( Category("Category:1990_deaths")(), Category("Category:1970_births")() )

			val d = new PersonLinkTimestampDeterminer(p)
			sdf.format(d.determine.to) == "1990 AD" must beTrue
			sdf.format(d.determine.from) == "1970 AD" must beTrue
		}

		"be able to work with multiple dates (use first birth and last death)" in {
			val p = Person("test")()
			p.categories = Array( Category("Category:1990_deaths")(),Category("Category:1987_deaths")(), Category("Category:1970_births")(), Category("Category:1977_births")() )

			val d = new PersonLinkTimestampDeterminer(p)
			sdf.format(d.determine.to) == "1990 AD" must beTrue
			sdf.format(d.determine.from) == "1970 AD" must beTrue
		}
	}

	"PersonLinkTimestampDeterminer's dateFromCategory function " should {
		val p = new PersonLinkTimestampDeterminer(null)

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
