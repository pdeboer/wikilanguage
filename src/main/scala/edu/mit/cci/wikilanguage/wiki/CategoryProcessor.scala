package edu.mit.cci.wikilanguage.wiki

import edu.mit.cci.wiki.ArticleCache
import edu.mit.cci.wikilanguage.model.active.WikiCategory

/**
 * User: pdeboer
 * Date: 10/13/13
 * Time: 10:17 AM
 */
class CategoryProcessor(val lang:String="en"){
  def process(name:String) {
    val cat = new WikiCategory(name, lang = lang)
    cat.contents.par.foreach(c => {
      if(c.startsWith("Category:"))
        process(c)
      else {
        val article = ArticleCache.get(c, lang)
        //TODO continue here
      }
    })
  }
}
