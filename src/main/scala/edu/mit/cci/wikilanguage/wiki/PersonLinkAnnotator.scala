package edu.mit.cci.wikilanguage.wiki

import edu.mit.cci.wikilanguage.db.DAO
import java.util.{Calendar, Date}
import edu.mit.cci.wikilanguage.model.Category
import edu.mit.cci.util.U

/**
 * @author pdeboer
 *         First created on 03/12/13 at 17:26
 */
class PersonLinkAnnotator {
	def processPerson(id: Int) {

	}
}

class PersonLinkTimestampDeterminer(val personId: Int) {
	val person = DAO.personById(personId, fetchCategories = true)

	def outlinks = DAO.getPersonOutlinks(personId)

	def determine = {
		val targetDates = person.categories.map(c => {
			val date = dateFromCategory(c)
			if (date == null) null
			else if (c.name.contains("birth")) FromTo(date, null)
			else if (c.name.contains("death")) FromTo(null, date)
			else null
		}).filter(_ != null)

		if (targetDates.size > 0) {
			val minBirth = targetDates.min(Ordering.by(_.from)).from
			val maxDeath = targetDates.max(Ordering.by(_.to)).to

			FromTo(minBirth, maxDeath)
		} else FromTo(null, null)
	}

	/**
	 * works only for people that currently have a category BIRTH or DEATH
	 * @param c
	 * @return
	 */
	def dateFromCategory(c: Category): Date = {
		if (U.containsNumber(c.name) && U.checkStringContains(c.name, Array("birth", "death"))) {
			val numberOfCharacters = "birth".length //luckily, birth and death have the same length

			val withoutBeginning = c.name.substring("Category:".length)
			val withoutEnding = withoutBeginning.substring(0, withoutBeginning.length - numberOfCharacters)

			val isBC = withoutEnding.contains("BC")

			val candidate = new Date(
				U.getNumbers(withoutEnding).toInt * (if(withoutEnding.contains("century")) 100 else 1),
				0, 0 )

			if(isBC) new Date(candidate.getYear * -1, candidate.getMonth, candidate.getDay)
			else candidate
		} else null
	}

}

case class FromTo(from: Date, to: Date)
