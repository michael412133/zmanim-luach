package io.github.michael412133.zmanim

import com.kosherjava.zmanim.hebrewcalendar.JewishDate
import java.time.LocalDate
import java.time.Month

enum class EventType { Yahrzeit, Birthday, Anniversary, Other }

/** For a Hebrew date in Adar: which Adar it falls in, in a year that has two. */
enum class AdarChoice { AdarI, AdarII, Both }

/**
 * Something the reader keeps track of every year: a yahrzeit, a birthday, an anniversary, or
 * anything else. It comes back each year on the same Hebrew date, or on the same English date.
 */
data class Event(
    val id: Long,
    val type: EventType,
    val name: String,
    /** True when it comes back on the Hebrew date, false on the English date. */
    val hebrew: Boolean,
    /** The day it was saved for. Its Hebrew date is the one in the day's square. */
    val date: LocalDate,
    /** Only for a Hebrew date in Adar. */
    val adar: AdarChoice? = null,
) {
    val isInAdar: Boolean
        get() = JewishDate(date).jewishMonth.let { it == JewishDate.ADAR || it == JewishDate.ADAR_II }
}

/**
 * When an event comes back each year. KosherJava works out every Hebrew date and month length;
 * it has no rules for yahrzeits or birthdays, so these follow Hebcal's, which are the ones
 * from Reingold and Dershowitz's "Calendrical Calculations":
 *
 * - A yahrzeit on 30 Cheshvan or 30 Kislev depends on the first year after the death. If that
 *   year had no 30th, the yahrzeit is always the last day of the month; if it did, the yahrzeit
 *   is the 30th, and in a year without one, the 1st of the next month.
 * - A birthday or anniversary on a 30th that a year does not have moves to the 1st of the next
 *   month.
 * - A 30 Adar I in a year with one Adar is 30 Shevat for a yahrzeit and 1 Nissan for a birthday.
 * - In a year with two Adars, Adar follows the choice saved with the event. Its first choice
 *   matches Hebcal: Adar I for a yahrzeit from a year with one Adar, Adar II for a birthday,
 *   and the same Adar for a date that was already in Adar I or Adar II.
 */
object Events {

    fun occursOn(event: Event, day: LocalDate): Boolean =
        if (event.hebrew) {
            day in hebrewDates(event, JewishDate(day).jewishYear)
        } else {
            day.year >= event.date.year && englishDate(event, day.year) == day
        }

    /** The first choice offered for Adar, matching Hebcal's rules. */
    fun defaultAdar(type: EventType, date: LocalDate): AdarChoice {
        val jewish = JewishDate(date)
        return when {
            jewish.jewishMonth == JewishDate.ADAR_II -> AdarChoice.AdarII
            jewish.isJewishLeapYear -> AdarChoice.AdarI
            type == EventType.Yahrzeit -> AdarChoice.AdarI
            else -> AdarChoice.AdarII
        }
    }

    /** The day the event falls on in an English year. February 29 is on the 28th in other years. */
    fun englishDate(event: Event, year: Int): LocalDate {
        val date = event.date
        return if (date.month == Month.FEBRUARY && date.dayOfMonth == 29 && !java.time.Year.isLeap(year.toLong())) {
            LocalDate.of(year, 2, 28)
        } else {
            LocalDate.of(year, date.month, date.dayOfMonth)
        }
    }

    /**
     * The days the event falls on in a Hebrew year: one, or two for "both" in a year with two
     * Adars, and none in a year before the one it was saved in, as with Hebcal.
     */
    fun hebrewDates(event: Event, year: Int): List<LocalDate> {
        val original = JewishDate(event.date)
        if (year < original.jewishYear) return emptyList()
        val month = original.jewishMonth
        val day = original.jewishDayOfMonth
        val yahrzeit = event.type == EventType.Yahrzeit

        if (month == JewishDate.ADAR || month == JewishDate.ADAR_II) {
            if (!isLeap(year)) {
                return listOf(adarDay(year, JewishDate.ADAR, day, yahrzeit))
            }
            val months = when (event.adar ?: defaultAdar(event.type, event.date)) {
                AdarChoice.AdarI -> listOf(JewishDate.ADAR)
                AdarChoice.AdarII -> listOf(JewishDate.ADAR_II)
                AdarChoice.Both -> listOf(JewishDate.ADAR, JewishDate.ADAR_II)
            }
            return months.map { adarDay(year, it, day, yahrzeit) }
        }

        if (year == original.jewishYear) return listOf(event.date)

        if (yahrzeit) {
            var m = month
            var d = day
            if (month == JewishDate.CHESHVAN && day == 30 && !cheshvanLong(original.jewishYear + 1)) {
                // The first yahrzeit had no 30th, so it is always the last day of Cheshvan.
                d = if (cheshvanLong(year)) 30 else 29
            } else if (month == JewishDate.KISLEV && day == 30 && kislevShort(original.jewishYear + 1)) {
                d = if (kislevShort(year)) 29 else 30
            }
            if (m == JewishDate.CHESHVAN && d == 30 && !cheshvanLong(year)) {
                m = JewishDate.KISLEV
                d = 1
            } else if (m == JewishDate.KISLEV && d == 30 && kislevShort(year)) {
                m = JewishDate.TEVES
                d = 1
            }
            return listOf(date(year, m, d))
        }

        return listOf(
            when {
                month == JewishDate.CHESHVAN && day == 30 && !cheshvanLong(year) -> date(year, JewishDate.KISLEV, 1)
                month == JewishDate.KISLEV && day == 30 && kislevShort(year) -> date(year, JewishDate.TEVES, 1)
                else -> date(year, month, day)
            },
        )
    }

    /**
     * A day of Adar in a given year. Only Adar I has a 30th, so a 30th in a month without one
     * goes to the day before the month starts for a yahrzeit (the first day of Rosh Chodesh),
     * and to 1 Nissan for anything else.
     */
    private fun adarDay(year: Int, month: Int, day: Int, yahrzeit: Boolean): LocalDate {
        if (day <= daysIn(year, month)) return date(year, month, day)
        return if (yahrzeit) date(year, month, 1).minusDays(1) else date(year, JewishDate.NISSAN, 1)
    }

    private fun date(year: Int, month: Int, day: Int): LocalDate = JewishDate(year, month, day).localDate

    private fun daysIn(year: Int, month: Int): Int = JewishDate(year, month, 1).daysInJewishMonth

    private fun isLeap(year: Int): Boolean = JewishDate(year, JewishDate.TISHREI, 1).isJewishLeapYear

    private fun cheshvanLong(year: Int): Boolean = JewishDate(year, JewishDate.TISHREI, 1).isCheshvanLong

    private fun kislevShort(year: Int): Boolean = JewishDate(year, JewishDate.TISHREI, 1).isKislevShort

    // Saved one event to a line, its parts split by tabs. A name is the only part that can hold a
    // tab, a line break or a backslash, so those are written as \t, \n and \\.

    fun encode(events: List<Event>): String = events.joinToString("\n") { event ->
        listOf(
            event.id.toString(),
            event.type.name,
            if (event.hebrew) "H" else "E",
            event.date.toString(),
            event.adar?.name ?: "-",
            escape(event.name),
        ).joinToString("\t")
    }

    fun decode(text: String?): List<Event> = text.orEmpty().lines().mapNotNull { line ->
        val parts = line.split('\t')
        if (parts.size != 6) return@mapNotNull null
        runCatching {
            Event(
                id = parts[0].toLong(),
                type = EventType.valueOf(parts[1]),
                hebrew = parts[2] == "H",
                date = LocalDate.parse(parts[3]),
                adar = if (parts[4] == "-") null else AdarChoice.valueOf(parts[4]),
                name = unescape(parts[5]),
            )
        }.getOrNull()
    }

    private fun escape(text: String): String =
        text.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n").replace("\r", "")

    private fun unescape(text: String): String {
        val out = StringBuilder()
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (c == '\\' && i + 1 < text.length) {
                when (text[i + 1]) {
                    't' -> out.append('\t')
                    'n' -> out.append('\n')
                    else -> out.append(text[i + 1])
                }
                i += 2
            } else {
                out.append(c)
                i++
            }
        }
        return out.toString()
    }
}
