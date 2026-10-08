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
    fun markedDays() {
        val october = Months.containing(MonthStyle.English, LocalDate.of(2026, 10, 1))
        val cells = Months.cells(october, inIsrael = false)
        assertEquals(31, cells.size)
        assertTrue(cells.single { it.date == LocalDate.of(2026, 10, 12) }.marked) // Rosh Chodesh
        assertTrue(cells.single { it.date == LocalDate.of(2026, 10, 3) }.marked) // Shemini Atzeres
        assertTrue(!cells.single { it.date == LocalDate.of(2026, 10, 7) }.marked) // a plain Wednesday
        // כ״ו
        assertEquals("כ״ו", cells.single { it.date == LocalDate.of(2026, 10, 7) }.hebrewDay)
    }

    @Test
    fun nearestTown() {
        assertEquals("airmont", Places.nearest(41.1002, -74.0985)?.id)
        assertEquals(null, Places.nearest(31.778, 35.235)) // Yerushalayim is not on the list
    }
}
