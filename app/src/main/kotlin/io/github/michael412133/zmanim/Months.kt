package io.github.michael412133.zmanim

import com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import com.kosherjava.zmanim.hebrewcalendar.JewishDate
import java.time.LocalDate

/** How the month view is divided: by English months (October) or Hebrew ones (Tishrei). */
enum class MonthStyle { English, Hebrew }

/** One month of either calendar, known by its first day and how many days it has. */
data class CalendarMonth(val style: MonthStyle, val firstDay: LocalDate, val length: Int) {

    val lastDay: LocalDate get() = firstDay.plusDays(length - 1L)

    /** How many empty squares come before the first day, with Sunday first. */
    val leadingBlanks: Int get() = firstDay.dayOfWeek.value % 7

    /** How many rows of seven the month takes. */
    val weeks: Int get() = (leadingBlanks + length + 6) / 7

    operator fun contains(date: LocalDate): Boolean = !date.isBefore(firstDay) && !date.isAfter(lastDay)
}

/** One square of the month view. */
data class MonthCell(
    val date: LocalDate,
    /** The Hebrew day of the month in letters, like כ״ו. */
    val hebrewDay: String,
    /** Drawn in grey: Yom Tov, Chol Hamoed, a fast, Rosh Chodesh or a day with a name of its own. */
    val marked: Boolean,
    /** One of the reader's own events falls on it. */
    val hasEvent: Boolean = false,
)

object Months {

    fun containing(style: MonthStyle, date: LocalDate): CalendarMonth = when (style) {
        MonthStyle.English -> CalendarMonth(style, date.withDayOfMonth(1), date.lengthOfMonth())
        MonthStyle.Hebrew -> {
            val jewish = JewishCalendar(date)
            CalendarMonth(style, date.minusDays(jewish.jewishDayOfMonth - 1L), jewish.daysInJewishMonth)
        }
    }

    fun next(month: CalendarMonth): CalendarMonth = containing(month.style, month.lastDay.plusDays(1))

    fun previous(month: CalendarMonth): CalendarMonth = containing(month.style, month.firstDay.minusDays(1))

    /** The year a month belongs to: 2026 for an English month, 5787 for a Hebrew one. */
    fun yearOf(month: CalendarMonth): Int = when (month.style) {
        MonthStyle.English -> month.firstDay.year
        MonthStyle.Hebrew -> JewishCalendar(month.firstDay).jewishYear
    }

    /** A year's months in order: January to December, or Tishrei to Elul (13 in a leap year). */
    fun monthsOfYear(style: MonthStyle, year: Int): List<CalendarMonth> = when (style) {
        MonthStyle.English -> (1..12).map { containing(style, LocalDate.of(year, it, 1)) }
        MonthStyle.Hebrew -> {
            val months = mutableListOf<CalendarMonth>()
            var month = containing(style, JewishCalendar(year, JewishDate.TISHREI, 1).localDate)
            while (JewishCalendar(month.firstDay).jewishYear == year) {
                months += month
                month = next(month)
            }
            months
        }
    }

    fun cells(month: CalendarMonth, inIsrael: Boolean, events: List<Event> = emptyList()): List<MonthCell> =
        cells(month.firstDay, month.length, inIsrael, events)

    /** The squares for [count] days in a row, starting from [first]: a month, or one week. */
    fun cells(first: LocalDate, count: Int, inIsrael: Boolean, events: List<Event> = emptyList()): List<MonthCell> {
        val hebrew = HebrewDateFormatter().apply { setHebrewFormat(true) }
        return (0 until count).map { offset ->
            val date = first.plusDays(offset.toLong())
            val jewish = JewishCalendar(date).apply { setInIsrael(inIsrael) }
            MonthCell(
                date = date,
                hebrewDay = hebrew.formatHebrewNumber(jewish.jewishDayOfMonth),
                marked = Luach.isMarked(jewish),
                hasEvent = events.any { Events.occursOn(it, date) },
            )
        }
    }

    /** The Sunday that starts the week a date is in. */
    fun weekStart(date: LocalDate): LocalDate = date.minusDays((date.dayOfWeek.value % 7).toLong())
}
