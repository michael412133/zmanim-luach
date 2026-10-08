package io.github.michael412133.zmanim

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * The expected times were checked against Hebcal (hebcal.com) for the same place and day.
 * Hebcal rounds to the nearest minute and this app rounds to the safe side, so where the two
 * differ by a minute the exact time is noted, and the difference is the rounding.
 */
class LuachTest {

    private val airmont = Places.byId("airmont")
    private val monsey = Places.byId("monsey")

    private fun clock(time: Date?): String {
        requireNotNull(time)
        return SimpleDateFormat("HH:mm", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("America/New_York") }
            .format(time)
    }

    private fun Day.at(kind: ZmanKind): String = clock(zmanim.single { it.kind == kind }.time)

    private fun Day.has(kind: ZmanKind): Boolean = zmanim.any { it.kind == kind }

    @Test
    fun hebrewDateParshaAndDaf() {
        val day = Luach.day(airmont, LocalDate.of(2026, 10, 7))
        // כ״ו תשרי תשפ״ז
        assertEquals("כ״ו תשרי תשפ״ז", day.hebrewDate)
        // כ״ז תשרי, the date that starts at shkia
        assertEquals("כ״ז תשרי", day.tonightHebrewDate)
        // פרשת בראשית
        assertEquals("פרשת בראשית", day.parsha)
        // דף יומי בכורות י״ט (Hebcal: Bechorot 19)
        assertEquals("דף יומי בכורות י״ט", day.daf)
        assertEquals("", day.special)
    }

    @Test
    fun roshChodesh() {
        val day = Luach.day(monsey, LocalDate.of(2026, 10, 12))
        // ראש חודש חשון
        assertEquals("ראש חודש חשון", day.special)
    }

    @Test
    fun noDafYomiBeforeTheFirstCycle() {
        assertEquals("", Luach.day(monsey, LocalDate.of(1920, 5, 3)).daf)
    }

    @Test
    fun zmanimInAirmontMatchHebcal() {
        val day = Luach.day(airmont, LocalDate.of(2026, 10, 7))
        assertEquals("05:47", day.at(ZmanKind.Alos72)) // 5:47:24
        assertEquals("06:03", day.at(ZmanKind.Misheyakir)) // Hebcal 6:03
        assertEquals("07:00", day.at(ZmanKind.Netz)) // Hebcal 6:59, exactly 6:59:24
        assertEquals("09:15", day.at(ZmanKind.SofZmanShmaMGA)) // Hebcal 9:16, exactly 9:15:37
        assertEquals("09:51", day.at(ZmanKind.SofZmanShmaGRA)) // Hebcal 9:52, exactly 9:51:37
        assertEquals("10:25", day.at(ZmanKind.SofZmanTfilaMGA)) // Hebcal 10:25
        assertEquals("10:49", day.at(ZmanKind.SofZmanTfilaGRA)) // Hebcal 10:49
        assertEquals("12:44", day.at(ZmanKind.Chatzos)) // Hebcal 12:44
        assertEquals("13:13", day.at(ZmanKind.MinchaGedola)) // Hebcal 1:13
        assertEquals("16:05", day.at(ZmanKind.MinchaKetana)) // Hebcal 4:05
        assertEquals("17:17", day.at(ZmanKind.PlagHamincha)) // Hebcal 5:17
        assertEquals("18:28", day.at(ZmanKind.Shkia)) // Hebcal 6:28
        assertEquals("19:10", day.at(ZmanKind.Tzeis)) // Hebcal 7:09, exactly 7:09:02
        assertEquals("19:41", day.at(ZmanKind.RabbeinuTam)) // Hebcal 7:40, exactly 7:40:17
    }

    @Test
    fun candleLightingOnErevShabbos() {
        val friday = Luach.day(monsey, LocalDate.of(2026, 10, 9))
        assertEquals("18:06", friday.at(ZmanKind.CandleLighting)) // Hebcal 6:06
    }

    @Test
    fun noCandleLightingOnAPlainWeekday() {
        val wednesday = Luach.day(monsey, LocalDate.of(2026, 10, 7))
        assertTrue(!wednesday.has(ZmanKind.CandleLighting))
        assertTrue(!wednesday.has(ZmanKind.CandleLightingAfterTzeis))
    }

    @Test
    fun candlesAfterTzeisWhenYomTovFollowsYomTov() {
        // The first day of Pesach 5787, a Thursday: candles for the second night only after tzeis.
        val firstDay = Luach.day(monsey, LocalDate.of(2027, 4, 22))
        assertEquals(firstDay.at(ZmanKind.Tzeis), firstDay.at(ZmanKind.CandleLightingAfterTzeis))
        assertTrue(!firstDay.has(ZmanKind.CandleLighting))
        // Erev Pesach: the usual 18 minutes before shkia.
        val erev = Luach.day(monsey, LocalDate.of(2027, 4, 21))
        assertTrue(erev.has(ZmanKind.CandleLighting))
        assertTrue(!erev.has(ZmanKind.CandleLightingAfterTzeis))
    }

    @Test
    fun roundingGoesToTheSafeSide() {
        val minute = 60_000L
        val floor = 1_791_399_960_000L // a whole minute
        val time = Date(floor + 25_000L) // 25 seconds past it
        assertEquals(floor, Luach.round(time, Rounding.Earlier)!!.time)
        assertEquals(floor + minute, Luach.round(time, Rounding.Later)!!.time)
        val onTheMinute = Date(floor)
        assertEquals(floor, Luach.round(onTheMinute, Rounding.Later)!!.time)
    }
}
