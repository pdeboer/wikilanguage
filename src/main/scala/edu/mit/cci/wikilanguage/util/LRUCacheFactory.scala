package edu.mit.cci.wikilanguage.util

import scala.collection.parallel.mutable

/**
 * @author pdeboer
 *         First created on 29/01/14 at 17:01
 */
object LRUCacheFactory {
	private val caches = new java.util.HashMap[String, LRUCache[Any]]()

	def get[A](name:String) = if(caches.containsKey(name)) caches.get(name).asInstanceOf[LRUCache[A]] else {
		val c = new LRUCache[A]()
		caches.put(name, c.asInstanceOf[LRUCache[Any]])
		c
	}
}
