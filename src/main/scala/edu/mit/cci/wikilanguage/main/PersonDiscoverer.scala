package edu.mit.cci.wikilanguage.main

import edu.mit.cci.wikilanguage.db.DAO
import edu.mit.cci.wikilanguage.wiki.CategoryProcessor

/**
 * User: pdeboer
 * Date: 10/16/13
 * Time: 6:52 PM
 */
object PersonDiscoverer extends App {
	val processor = new CategoryProcessor()
	processor.process("Category:People_categories_by_parameter")
	processor.process("Category:Living_people")
}
