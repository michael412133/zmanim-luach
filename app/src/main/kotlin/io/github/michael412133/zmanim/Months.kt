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
    /** Yom Tov (with Chol Hamoed, Chanukah and Purim), Rosh Chodesh or a fast day. */
    val marked: Boolean,
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

    fun cells(month: CalendarMonth, inIsrael: Boolean): List<MonthCell> {
        val hebrew = HebrewDateFormatter().apply { setHebrewFormat(true) }
        return (0 until month.length).map { offset ->
            val date = month.firstDay.plusDays(offset.toLong())
            val jewish = JewishCalendar(date).apply { setInIsrael(inIsrael) }
            MonthCell(
                date = date,
                hebrewDay = hebrew.formatHebrewNumber(jewish.jewishDayOfMonth),
                marked = jewish.isYomTov || jewish.isTaanis || jewish.isRoshChodesh,
            )
        }
    }
}
