package edu.mit.cci.wikilanguage.wiki

import org.junit.runner.RunWith
import org.specs._
import org.specs.runner.{JUnitSuiteRunner, JUnit}
import edu.mit.cci.wikilanguage.model.{Person, Category}
import java.util.Date
import java.text.SimpleDateFormat

//import org.scalacheck.Gen


@RunWith(classOf[JUnitSuiteRunner])
class PersonLinkAnnotationProcessorTest extends Specification with JUnit /*with ScalaCheck*/ {
	val sdf = new SimpleDateFormat("yyyy G")

	"PersonLinkTimestampDeterminer's commonWindow function " should {
		"identify a window when both are defined" in {
			val p1 = Person("t1")(categories = Array(Category("Category:1960_births")(), Category("Category:1990_deaths")()))
			val p2 = Person("t2")(categories = Array(Category("Category:1962_births")(), Category("Category:2000_deaths")()))

			val pp = new PersonLinkTimestampDeterminer(p1)
			sdf.format(pp.commonWindow(p2).from) == "1962 AD" must beTrue
			sdf.format(pp.commonWindow(p2).to) == "1990 AD" must beTrue
		}

		"identify a window when one death is undefined" in {
			val p1 = Person("t1")(categories = Array(Category("Category:1960_births")(), Category("Category:1990_deaths")()))
			val p2 = Person("t2")(categories = Array(Category("Category:1962_births")()))

			val pp = new PersonLinkTimestampDeterminer(p1)
			sdf.format(pp.commonWindow(p2).from) == "1962 AD" must beTrue
			sdf.format(pp.commonWindow(p2).to) == "1990 AD" must beTrue
		}

		"identify a window when both deaths are undefined" in {
			val p1 = Person("t1")(categories = Array(Category("Category:1960_births")()))
			val p2 = Person("t2")(categories = Array(Category("Category:1962_births")()))

			val pp = new PersonLinkTimestampDeterminer(p1)
			sdf.format(pp.commonWindow(p2).from) == "1962 AD" must beTrue
			pp.commonWindow(p2).to must beNull
		}

		"identify a window when both births are undefined" in {
			val p1 = Person("t1")(categories = Array(Category("Category:1960_deaths")()))
			val p2 = Person("t2")(categories = Array(Category("Category:1962_deaths")()))

			val pp = new PersonLinkTimestampDeterminer(p1)
			pp.commonWindow(p2).from must beNull
			sdf.format(pp.commonWindow(p2).to) == "1960 AD" must beTrue
		}

		"identify a window when everything is undefined" in {
			val p1 = Person("t1")(categories = Array())
			val p2 = Person("t2")(categories = Array())

			val pp = new PersonLinkTimestampDeterminer(p1)
			pp.commonWindow(p2).from must beNull
			pp.commonWindow(p2).to must beNull
		}
	}

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

object PersonLinkAnnotationProcessorTest {
	def main(args: Array[String]) {
		new PersonLinkAnnotationProcessorTest().main(args)
	}
}
