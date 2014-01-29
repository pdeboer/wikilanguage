package edu.mit.cci.wikilanguage.util

import java.util
import java.util.Collections
import java.util.Map.Entry

/**
 * @author pdeboer
 *         First created on 29/01/14 at 16:54
 */
class LRUCache[A](val maxEntries:Int = 1000) {
	private val map = Collections.synchronizedMap(new util.LinkedHashMap[String, A](maxEntries) {
		override def removeEldestEntry(eldest: Entry[String, A]): Boolean = {
			super.size() > maxEntries
		}
	})

	def get(key:String):A = this.synchronized{ map.get(key) }

	def put(key:String, payload:A) {
		this.synchronized {
			if(!map.containsKey(key)) map.put(key, payload)
		}
	}
}
