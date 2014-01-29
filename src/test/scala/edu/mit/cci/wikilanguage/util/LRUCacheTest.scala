package edu.mit.cci.wikilanguage.util

import org.junit.runner.RunWith
import org.specs._
import org.specs.runner.{JUnitSuiteRunner, JUnit}
import edu.mit.cci.util.U
import edu.mit.cci.wikilanguage.model.Person

//import org.scalacheck.Gen


@RunWith(classOf[JUnitSuiteRunner])
class LRUCacheTest extends Specification with JUnit /*with ScalaCheck*/ {
	val cachekey: String = "insertperson"
	"LRUCacheFactory" should {
		"be able to retrieve the correct cache" in {
			val cache = LRUCacheFactory.get[Person](cachekey)
			cache mustEq LRUCacheFactory.get[Person](cachekey)
			cache mustNotEq LRUCacheFactory.get[Person](cachekey + "ddd")
		}
	}

	"LRUCache" should {
		"be able to cache simple things" in {
			val cache = LRUCacheFactory.get[Person](cachekey)
			val key1: String = "key"
			val value1: Person = Person("Blablupp")()

			val key2: String = "key2"
			val value2: Person = Person("Blablupp2")()

			cache.put(key1, value1)
			cache.put(key2, value2)

			cache.get(key1) mustEq value1
			cache.get(key2) mustEq value2
			cache.get(key1+key2+"2") must beNull
		}

		"be able to deal with limits" in {
			val cache = new LRUCache[String](4)

			cache.put("1", "1")
			cache.put("2", "2")
			cache.put("3", "3")

			//make sure '2' stays in cache
			cache.get("2") mustEq "2"

			cache.put("4", "")
			cache.put("5","")

			// should still be there in last position
			cache.get("2") mustEq "2"

			cache.put("8","")

			// now it's overwritten
			cache.get("2") must beNull
		}
	}
}

object LRUCacheTest {
	def main(args: Array[String]) {
		new LRUCacheTest().main(args)
	}
}
