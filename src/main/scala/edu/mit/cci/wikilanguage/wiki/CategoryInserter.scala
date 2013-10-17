package edu.mit.cci.wikilanguage.wiki

import edu.mit.cci.wiki.ArticleCache
import edu.mit.cci.wikilanguage.model.active.WikiCategory
import edu.mit.cci.wikilanguage.db.DAO
import edu.mit.cci.wikilanguage.model.Conversions._

/**
 * User: pdeboer
 * Date: 10/13/13
 * Time: 10:17 AM
 */
class CategoryInserter(val lang: String = "en") {
  //TODO: needs check if category is actually referring to people.
  // ended up with stars just before -.-
  def process(name: String) {
    val cat = new WikiCategory(name, lang = lang)
    val dao = new DAO()
    dao.insertCategory(cat)

    println("analyzing category " + cat.name + ", found " + cat.contents.length + " children")

    cat.contents.par.foreach(c => {
      if (c.startsWith("Category:"))
        try {
          process(c)
        }
        catch {
          case e: Exception => e.printStackTrace()
        }
      else {
        try {
          val article = ArticleCache.get(c, lang)
          dao.insertPerson(article)
        }
        catch {
          case e: Exception => e.printStackTrace()
        }
      }
    })
  }
}
