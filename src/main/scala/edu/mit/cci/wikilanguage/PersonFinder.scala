package edu.mit.cci.wikilanguage

import edu.mit.cci.wikilanguage.model.active.WikiCategory
import edu.mit.cci.wikilanguage.wiki.CategoryProcessor
import edu.mit.cci.wikilanguage.db.DAO

/**
 * User: pdeboer
 * Date: 10/16/13
 * Time: 6:52 PM
 */
object PersonFinder extends App{
  new DAO().clean()
  val processor = new CategoryProcessor()
  processor.process("Category:People_by_century")
}
