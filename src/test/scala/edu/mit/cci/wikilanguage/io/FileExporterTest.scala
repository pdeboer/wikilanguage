package edu.mit.cci.wikilanguage.io

import org.junit.runner.RunWith
import org.specs._
import org.specs.runner.{JUnitSuiteRunner, JUnit}
import edu.mit.cci.wikilanguage.model.Category
import edu.mit.cci.util.U
import edu.mit.cci.wikilanguage.db.DAO
import scala.io.Source
import java.io.File


/**
 * User: pdeboer
 * Date: 10/14/13
 * Time: 8:24 PM
 */
@RunWith(classOf[JUnitSuiteRunner])
class FileExporterTest extends Specification with JUnit /*with ScalaCheck*/ {
	"GraphVizExporter " should {
		val exp = new FileExporter
		exp.init(List(DAO.personByName("Moses").id))
		"should be able to assemble list from DAO" in {
			exp.data.size > 0 must beTrue
			exp.data.forall(_.from == "Moses") must beTrue
		}

		"should be able to export stuff" in {
			exp.exportDot("out.dot")

			val lines = Source.fromFile("out.dot").getLines().toList
			lines(0).startsWith("digraph ") must beTrue
			lines(0).endsWith("{") must beTrue
			lines(1).contains("Moses") must beTrue
			lines(lines.size - 1).endsWith("}") must beTrue

			new File("out.dot").delete()
		}
	}
}


object FileExporterTest {
	def main(args: Array[String]) {
		new FileExporterTest().main(args)
	}

}
