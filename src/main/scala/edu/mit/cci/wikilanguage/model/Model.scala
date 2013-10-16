package edu.mit.cci.wikilanguage.model

import edu.mit.cci.wikilanguage.model.active.{WikiCategory, WikiArticle}

/**
 * User: pdeboer
 * Date: 10/16/13
 * Time: 6:12 PM
 */

case class Person(name: String, lang: String = "en", var id: Int = -1, var categories: Array[Category] = Array.empty[Category])
case class Category(name: String, lang: String = "en", var id: Int = -1)

object Conversions {
  implicit def WikiArticle2Person(article: WikiArticle) = new Person(article.name, article.lang,
    categories = Array.empty[Category] ++ article.categories.map(WikiCategory2Category(_)))
  implicit def WikiCategory2Category(category: WikiCategory) = new Category(category.category, category.lang)
}