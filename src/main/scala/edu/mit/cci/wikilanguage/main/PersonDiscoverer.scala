package edu.mit.cci.wikilanguage.main

import edu.mit.cci.wikilanguage.db.DAO
import edu.mit.cci.wikilanguage.wiki.CategoryProcessor

/**
 * User: pdeboer
 * Date: 10/16/13
 * Time: 6:52 PM
 */
object PersonDiscoverer extends App {
	new DAO().clean()
	val processor = new CategoryProcessor()
	processor.process("Category:People_by_century")
	processor.process("Category:Living_people")
	processor.process("Category:Dead_people")
}
