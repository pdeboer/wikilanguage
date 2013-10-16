package edu.mit.cci.wikilanguage

import edu.mit.cci.wikilanguage.model.active.WikiCategory

/**
 * User: pdeboer
 * Date: 10/16/13
 * Time: 6:52 PM
 */
object PersonFinder extends App{
  val root = new WikiCategory("Category:People_by_century")
  root.contents
}
