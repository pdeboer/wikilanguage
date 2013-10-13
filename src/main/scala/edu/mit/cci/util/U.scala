package edu.mit.cci.util

import org.jsoup.select.Elements
import org.jsoup.nodes.Element

/**
 * User: pdeboer
 * Date: 8/6/13
 * Time: 7:47 PM
 */
object U {
	def foreachJSoupElement(lis:Elements,f:(Element => Unit)) = {
		for {i <- 0 to lis.size()-1; li = lis.get(i)} f(li)
	}

	def convertJSoupToList(lis:Elements):List[Element] = {
		var ret:List[Element] = Nil
		foreachJSoupElement(lis, ret ::= _)
		ret
	}
}
