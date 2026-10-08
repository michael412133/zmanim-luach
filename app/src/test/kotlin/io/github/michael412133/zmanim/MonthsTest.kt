package io.github.michael412133.zmanim

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Month boundaries checked against Hebcal's converter: 1 Tishrei 5787 is September 12, 2026. */
class MonthsTest {

    @Test
    fun englishMonth() {
        val october = Months.containing(MonthStyle.English, LocalDate.of(2026, 10, 7))
        assertEquals(LocalDate.of(2026, 10, 1), october.firstDay)
        assertEquals(31, october.length)
        assertEquals(4, october.leadingBlanks) // October 1, 2026 is a Thursday
        assertEquals(5, october.weeks)
        assertEquals(LocalDate.of(2026, 11, 1), Months.next(october).firstDay)
        assertEquals(LocalDate.of(2026, 9, 1), Months.previous(october).firstDay)
    }

    @Test
    fun hebrewMonth() {
        val tishrei = Months.containing(MonthStyle.Hebrew, LocalDate.of(2026, 10, 7))
        assertEquals(LocalDate.of(2026, 9, 12), tishrei.firstDay)
        assertEquals(30, tishrei.length)
        assertEquals(6, tishrei.leadingBlanks) // Rosh Hashana 5787 was on Shabbos
        assertEquals(LocalDate.of(2026, 10, 12), Months.next(tishrei).firstDay) // 1 Cheshvan
        assertEquals(LocalDate.of(2026, 8, 14), Months.previous(tishrei).firstDay) // 1 Elul 5786
        assertEquals(5787, Months.yearOf(tishrei))
    }

    @Test
    fun monthsOfAYear() {
        assertEquals(12, Months.monthsOfYear(MonthStyle.English, 2026).size)
        // 5787 is a leap year, with Adar I and Adar II.
        val months5787 = Months.monthsOfYear(MonthStyle.Hebrew, 5787)
        assertEquals(13, months5787.size)
        assertEquals(LocalDate.of(2026, 9, 12), months5787.first().firstDay)
        assertEquals(12, Months.monthsOfYear(MonthStyle.Hebrew, 5786).size)
    }

    @Test
    fun greyDays() {
        val cells = Months.cells(Months.containing(MonthStyle.English, LocalDate.of(2026, 10, 1)), inIsrael = false)
        assertEquals(31, cells.size)
        fun marked(day: Int) = cells.single { it.date == LocalDate.of(2026, 10, day) }.marked
        assertTrue(marked(12)) // Rosh Chodesh
        assertTrue(marked(3)) // Shemini Atzeres
        assertTrue(marked(1)) // Chol Hamoed
        assertTrue(marked(2)) // Hoshana Rabba
        assertTrue(!marked(5)) // Isru Chag
        assertTrue(!marked(7)) // a plain Wednesday
        // כ״ו
        assertEquals("כ״ו", cells.single { it.date == LocalDate.of(2026, 10, 7) }.hebrewDay)
        // Erev Yom Kippur has its name with the day, but is not grey; Yom Kippur is.
        val september = Months.cells(Months.containing(MonthStyle.English, LocalDate.of(2026, 9, 1)), inIsrael = false)
        assertTrue(!september.single { it.date == LocalDate.of(2026, 9, 20) }.marked)
        assertTrue(september.single { it.date == LocalDate.of(2026, 9, 21) }.marked)
    }

    @Test
    fun eventDots() {
        val yahrzeit = Event(1, EventType.Yahrzeit, "x", hebrew = true, date = LocalDate.of(2025, 10, 18)) // 26 Tishrei 5786
        val cells = Months.cells(Months.containing(MonthStyle.English, LocalDate.of(2026, 10, 1)), false, listOf(yahrzeit))
        assertEquals(listOf(LocalDate.of(2026, 10, 7)), cells.filter { it.hasEvent }.map { it.date })
    }

    @Test
    fun aWeek() {
        assertEquals(LocalDate.of(2026, 10, 4), Months.weekStart(LocalDate.of(2026, 10, 7)))
        assertEquals(LocalDate.of(2026, 10, 4), Months.weekStart(LocalDate.of(2026, 10, 4))) // Sunday
        assertEquals(LocalDate.of(2026, 10, 4), Months.weekStart(LocalDate.of(2026, 10, 10))) // Shabbos
        val week = Months.cells(LocalDate.of(2026, 10, 4), 7, inIsrael = false)
        assertEquals(LocalDate.of(2026, 10, 10), week.last().date)
    }
}
