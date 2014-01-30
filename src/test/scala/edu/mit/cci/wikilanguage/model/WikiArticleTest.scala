package edu.mit.cci.wikilanguage.model

import org.junit.runner.RunWith
import org.specs._
import org.specs.runner.{JUnitSuiteRunner, JUnit}
import edu.mit.cci.wikilanguage.model.active.WikiArticle
import edu.mit.cci.wiki.ArticleCache
import edu.mit.cci.wikilanguage.db.DAO

//import org.scalacheck.Gen


@RunWith(classOf[JUnitSuiteRunner])
class WikiArticleTest extends Specification with JUnit /*with ScalaCheck*/ {
	"WikiArticle" should {
		"be able to retrieve contents of an article" in {
			val w = new WikiArticle("Google")

			w.categories.length mustNotEq 0
		}

		"find outlinks for a basic use case" in {
			val article = ArticleCache.get("Thomas W. Malone")
			val outlinks = article.likelyPersonOutlinks()
			outlinks.size > 10 must beTrue
		}

		"find all redirections for article" in {
			val article = ArticleCache.get("Steffi Graf")
			val redirects = article.redirects
			redirects.length > 0 must beTrue
			redirects.exists(_=="Stefi Graf") must beTrue
		}
	}

	"DAO to insert redirections" should {
	"insert them" in {
		val article = ArticleCache.get("Steffi Graf")
		val person: Person = Conversions.WikiArticle2Person(article)
		person.id = DAO.insertPerson(person)
		DAO.insertPersonMeta(person, resolveCategories = true, resolveRedirects = true)
	}}
}

object WikiArticleTest {
	def main(args: Array[String]) {
		new WikiCategoryTest().main(args)
	}
}
