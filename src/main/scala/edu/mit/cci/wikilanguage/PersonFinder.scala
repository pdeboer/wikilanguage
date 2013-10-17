package edu.mit.cci.wikilanguage

import edu.mit.cci.wikilanguage.model.active.WikiCategory
import edu.mit.cci.wikilanguage.wiki.CategoryInserter

/**
 * User: pdeboer
 * Date: 10/16/13
 * Time: 6:52 PM
 */
object PersonFinder extends App{
  val processor = new CategoryInserter()
  processor.process("Category:People_by_century")
}
