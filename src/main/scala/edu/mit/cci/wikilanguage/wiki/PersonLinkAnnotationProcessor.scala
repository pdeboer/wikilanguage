package edu.mit.cci.wikilanguage.wiki

import java.util.Date
import edu.mit.cci.wikilanguage.model.{Person, Category}
import edu.mit.cci.util.U
import java.text.SimpleDateFormat
import edu.mit.cci.wikilanguage.db.DAO

/**
 * @author pdeboer
 *         First created on 03/12/13 at 17:26
 */
class PersonLinkAnnotationProcessor {
	def processPerson(id: Int) {
		val source = DAO.personById(id, true)
		val sourceTimestamps = new PersonLinkTimestampDeterminer(source)

		DAO.getPersonOutlinks(id).foreach(t => {
			val target = DAO.personById(t.personToId, true)
			val window = sourceTimestamps.commonWindow(target)

			if (window.from != null || window.to != null) {
				DAO.updatePeopleConnection(t.id, window.from, window.to)
			}
		})

		println("Processed Person " + id)
	}
}

class PersonLinkTimestampDeterminer(val person: Person) {
	def commonWindow(other: Person) = {
		val plt = new PersonLinkTimestampDeterminer(other)

		val meDet = determine
		val otherDet = plt.determine


		val ret = FromTo(smallerDate(otherDet.from, meDet.from, -1), smallerDate(otherDet.to, meDet.to))
		//make sure that windows are overlapping
		if (ret.from != null && ret.to != null && ret.from.after(ret.to)) FromTo(null, null) //they aren't
		else ret //they are
	}

	def determine = {
		var targetDates = person.categories.map(c => {
			val date = dateFromCategory(c)

			if (date == null) null
			else if (c.name.contains("birth")) FromTo(date, null)
			else if (c.name.contains("death")) FromTo(null, date)
			else null
		}).filter(_ != null)

		//in case we have no birth - death date. check for other heuristics
		if (targetDates.size == 0) {
			val ONE_CENTURY = 1000l * 60 * 60 * 24 * 365 * 100
			targetDates = person.categories.map(c => {
				val date = dateFromCategory(c)

				if (date == null) null
				//for BC people
				else if (c.name.contains("century") && c.name.contains("BC")) FromTo(date, new Date(date.getTime + ONE_CENTURY))
				else null
			}).filter(_ != null)
		}

		if (targetDates.size > 0) {

			//smallest birth-year
			val minBirth: Date = targetDates.foldLeft[Date](null)((r, c) =>
				if (c.from == null) r else if (r == null) c.from else smallerDate(r, c.from, 1))

			//greatest death-year
			val maxDeath: Date = targetDates.foldLeft[Date](null)((r, c) =>
				if (c.to == null) r else if (r == null) c.to else smallerDate(r, c.to, -1))

			FromTo(minBirth, maxDeath)
		} else FromTo(null, null)
	}

	//function that returns the smaller of the given dates. if mul=-1, it returns the greater one
	private def smallerDate(d1: Date, d2: Date, mul: Int = 1) = {
		if (d1 == null) d2
		else if (d2 == null) d1
		else if (d1.compareTo(d2) * mul <= 0) d1 else d2
	}

	/**
	 * works only for people that currently have a category BIRTH or DEATH
	 * @param c
	 * @return
	 */
	def dateFromCategory(c: Category): Date = {
		val sdf = new SimpleDateFormat("yyyy G")
		try {
			if (U.containsNumber(c.name) && U.checkStringContainsOne(c.name, Array("births", "deaths"))) {
				val numberOfCharacters = "births".length //luckily, birth and death have the same length

				val withoutBeginning = c.name.substring("Category:".length)
				val withoutEnding = withoutBeginning.substring(0, withoutBeginning.length - numberOfCharacters)

				val isBC = withoutEnding.contains("BC")

				val candidateYear =
					U.getNumbers(withoutEnding).toInt * (if (withoutEnding.contains("century")) 100 else 1)

				sdf.parse(candidateYear + (if (isBC) " BC" else " AD"))
			} else if (U.containsNumber(c.name) && U.checkStringContainsAll(c.name, Array("century", "BC", "th"))) {
				// a lot of the religious history is only designated by century BC, without exact birth year
				sdf.parse((U.getNumbers(c.name).toInt * 100) + " BC")
			} else null
		}
		catch {
			case e: Throwable => null
		}
	}

}

case class FromTo(from: Date, to: Date)
