package samples

import org.junit.runner.RunWith
import org.specs._
import org.scalatest._
import org.specs.runner.{JUnitSuiteRunner, JUnit}
import edu.mit.cci.irc.WikiMessageParser
import edu.mit.cci.time.{TimedListTest, TimedList}

//import org.scalacheck.Gen



@RunWith(classOf[JUnitSuiteRunner])
class MySpecMain extends Specification with JUnit /*with ScalaCheck*/ {
  //nothing yet
}

object MySpecMain {
	def main(args: Array[String]) {
		new MySpecMain().main(args)
	}
}
