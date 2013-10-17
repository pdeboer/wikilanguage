package edu.mit.cci.wikilanguage.wiki

import edu.mit.cci.wiki.ArticleCache
import edu.mit.cci.wikilanguage.model.active.WikiCategory
import edu.mit.cci.wikilanguage.db.DAO
import edu.mit.cci.wikilanguage.model.Conversions._
import edu.mit.cci.util.U

/**
 * User: pdeboer
 * Date: 10/13/13
 * Time: 10:17 AM
 */
class CategoryProcessor(val lang: String = "en") {
  def process(name: String) {
    val cat = new WikiCategory(name, lang = lang)
    val dao = new DAO()

    dao.insertCategory(cat)

    println("analyzing category " + cat.name + ", found " + cat.contents.length + " children")

    cat.contents.par.foreach(c => {
      if (c.startsWith("Category:")) {
        if (isPersonCategory(c)) {
          try {
            process(c)
          }
          catch {
            case e: Exception => e.printStackTrace()
          }
        }
      }
      else if(isPerson(c)) {
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

  def isPerson(name:String) = checkString(name, Array(":", "list", "wikipedia"))

  def isPersonCategory(name: String):Boolean =
    checkString(name,Array("cleanup")) && U.containsNumber(name)

  private def checkString(str:String, notContains:Array[String]):Boolean = {
    val lowerCaseString = str.toLowerCase()

    notContains.forall(!lowerCaseString.contains(_))
  }
}
