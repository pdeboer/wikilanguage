package edu.mit.cci.wikilanguage.db


import org.junit.runner.RunWith
import org.specs._
import org.specs.runner.{JUnitSuiteRunner, JUnit}
import edu.mit.cci.wikilanguage.model.active.{WikiArticle, WikiCategory}
import edu.mit.cci.wikilanguage.model.{Category, Person}


/**
 * User: pdeboer
 * Date: 10/14/13
 * Time: 8:24 PM
 */
@RunWith(classOf[JUnitSuiteRunner])
class DAOTest  extends Specification with JUnit /*with ScalaCheck*/ {
  "DAO" should {
    val dao = new DAO
    dao.clean()

    "be able to add new category" in {
      val c = new Category("category", lang="de")
      val cId = dao.insertCategory(c)

      val catDB = dao.categoryByName(c.name)

      catDB.id mustEqual cId
      catDB.name mustEqual c.name
      catDB.lang mustEqual c.lang
      catDB.id mustNotEq c.id
    }

    "be able to add a new person" in {
      val p = new Person("person", lang="de")
      val pId = dao.insertPerson(p)

      val pDB = dao.personByName(p.name)

      pDB.id mustEqual pId
      pDB.name mustEqual p.name
      pDB.lang mustEqual p.lang
      pDB.id mustNotEq p.id
    }
  }
}


object DAOTest {
  def main(args: Array[String]) {
    new DAOTest().main(args)
  }

}
