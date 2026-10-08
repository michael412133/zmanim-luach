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
 * The expected times were checked against Hebcal (hebcal.com/zmanim, with seconds) for the
 * same place and day. Hebcal rounds to the nearest minute and this app rounds to the safe side,
 * so the exact time is noted next to each, and where the two differ by a minute it is the
 * rounding.
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

    private fun Day.line(kind: ZmanKind): Zman = zmanim.single { it.kind == kind }

    private fun Day.at(kind: ZmanKind): String = clock(line(kind).time)

    private fun Day.at(kind: ZmanKind, opinion: Opinion): String =
        clock(line(kind).opinions.single { it.first == opinion }.second)

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
        assertEquals("05:47", day.at(ZmanKind.Alos)) // 72 minutes before netz, 5:47:24
        assertEquals("06:03", day.at(ZmanKind.Misheyakir)) // 11.5°, Hebcal 6:02:38
        assertEquals("07:00", day.at(ZmanKind.Netz)) // Hebcal 6:59:24
        assertEquals("09:15", day.at(ZmanKind.SofZmanShema)) // Magen Avraham, Hebcal 9:15:37
        assertEquals("10:25", day.at(ZmanKind.SofZmanTefillah)) // Magen Avraham, Hebcal 10:25:01
        assertEquals("12:44", day.at(ZmanKind.Chatzos)) // 12:44:18, the sun's transit
        assertEquals("13:13", day.at(ZmanKind.MinchaGedola)) // Gra, Hebcal 13:12:32
        assertEquals("16:05", day.at(ZmanKind.MinchaKetana)) // Gra, Hebcal 16:04:45
        assertEquals("17:17", day.at(ZmanKind.PlagHamincha)) // Gra, Hebcal 17:16:31
        assertEquals("18:28", day.at(ZmanKind.Shkia)) // Hebcal 18:28:17
        assertEquals("19:10", day.at(ZmanKind.Tzeis)) // 8.5°, Hebcal 19:09:02
        assertEquals("19:41", day.at(ZmanKind.RabbeinuTam)) // Hebcal 19:40:17
        assertEquals("00:44", day.at(ZmanKind.ChatzosHalaila)) // the night after; Hebcal 00:44:22 on October 8
    }

    @Test
    fun everyOpinionInAirmontMatchesHebcal() {
        val day = Luach.day(airmont, LocalDate.of(2026, 10, 7))
        assertEquals("05:38", day.at(ZmanKind.Alos, Opinion.Alos16Point1)) // Hebcal 5:38:07
        assertEquals("05:29", day.at(ZmanKind.Alos, Opinion.Alos90)) // netz 6:59:24 less 90 minutes
        assertEquals("05:18", day.at(ZmanKind.Alos, Opinion.Alos19Point8)) // 5:18:14
        assertEquals("06:10", day.at(ZmanKind.Misheyakir, Opinion.Misheyakir10Point2)) // Hebcal 6:09:33
        assertEquals("06:06", day.at(ZmanKind.Misheyakir, Opinion.Misheyakir11)) // 6:05:18
        assertEquals("09:10", day.at(ZmanKind.SofZmanShema, Opinion.Mga16Point1)) // Hebcal 9:10:56
        assertEquals("09:51", day.at(ZmanKind.SofZmanShema, Opinion.Gra)) // Hebcal 9:51:37
        assertEquals("09:49", day.at(ZmanKind.SofZmanShema, Opinion.BaalHatanya)) // Hebcal 9:49:36
        assertEquals("10:21", day.at(ZmanKind.SofZmanTefillah, Opinion.Mga16Point1)) // Hebcal 10:21:53
        assertEquals("10:49", day.at(ZmanKind.SofZmanTefillah, Opinion.Gra)) // Hebcal 10:49:01
        assertEquals("10:47", day.at(ZmanKind.SofZmanTefillah, Opinion.BaalHatanya)) // Hebcal 10:47:41
        assertEquals("13:19", day.at(ZmanKind.MinchaGedola, Opinion.AfternoonMga)) // Hebcal 13:18:32
        assertEquals("13:13", day.at(ZmanKind.MinchaGedola, Opinion.AfternoonBaalHatanya)) // Hebcal 13:12:52
        assertEquals("16:47", day.at(ZmanKind.MinchaKetana, Opinion.AfternoonMga)) // Hebcal 16:46:45
        assertEquals("16:08", day.at(ZmanKind.MinchaKetana, Opinion.AfternoonBaalHatanya)) // Hebcal 16:07:05
        assertEquals("17:20", day.at(ZmanKind.PlagHamincha, Opinion.AfternoonBaalHatanya)) // Hebcal 17:19:41
        // 10.75 of the Magen Avraham's hours after 5:47:24, the hours running to 19:40:17: 18:13:31
        assertEquals("18:14", day.at(ZmanKind.PlagHamincha, Opinion.AfternoonMga))
        assertEquals("19:02", day.at(ZmanKind.Tzeis, Opinion.Tzeis7Point083)) // Hebcal 19:01:31
        assertEquals("19:19", day.at(ZmanKind.Tzeis, Opinion.Tzeis50)) // Hebcal 19:18:17
    }

    @Test
    fun aChosenOpinionIsTheOneShown() {
        val opinions = Opinions.Default.with(Opinion.Gra).with(Opinion.Alos16Point1).with(Opinion.Tzeis50)
        val day = Luach.day(airmont, LocalDate.of(2026, 10, 7), opinions)
        assertEquals("09:51", day.at(ZmanKind.SofZmanShema))
        assertEquals("10:49", day.at(ZmanKind.SofZmanTefillah))
        assertEquals("05:38", day.at(ZmanKind.Alos))
        assertEquals("19:19", day.at(ZmanKind.Tzeis))
        assertEquals(Opinion.Gra, day.line(ZmanKind.SofZmanShema).opinion)
        // Lines with only one way of working them out have no other opinions to show.
        assertTrue(day.line(ZmanKind.Netz).opinions.isEmpty())
        assertEquals(4, day.line(ZmanKind.SofZmanShema).opinions.size)
    }

    @Test
    fun candleLightingOnErevShabbos() {
        val friday = Luach.day(monsey, LocalDate.of(2026, 10, 9))
        assertEquals("18:06", friday.at(ZmanKind.CandleLighting)) // Hebcal 6:06, shkia 18:24:53
        assertEquals("18:04", friday.at(ZmanKind.CandleLighting, Opinion.Candles20))
        assertEquals("18:02", friday.at(ZmanKind.CandleLighting, Opinion.Candles22))
        assertEquals("17:54", friday.at(ZmanKind.CandleLighting, Opinion.Candles30))
        assertEquals("17:44", friday.at(ZmanKind.CandleLighting, Opinion.Candles40))
    }

    @Test
    fun noCandleLightingOnAPlainWeekday() {
        val wednesday = Luach.day(monsey, LocalDate.of(2026, 10, 7))
        assertTrue(!wednesday.has(ZmanKind.CandleLighting))
        assertTrue(!wednesday.has(ZmanKind.CandleLightingAfterTzeis))
        assertTrue(wednesday.zmanim.none { it.kind.special })
    }

    @Test
    fun candlesAfterTzeisWhenYomTovFollowsYomTov() {
        // The first day of Pesach 5787, a Thursday: candles for the second night only after tzeis.
        val firstDay = Luach.day(monsey, LocalDate.of(2027, 4, 22))
        assertEquals(firstDay.at(ZmanKind.Tzeis), firstDay.at(ZmanKind.CandleLightingAfterTzeis))
        assertTrue(!firstDay.has(ZmanKind.CandleLighting))
        assertTrue(!firstDay.has(ZmanKind.YomTovEnds))
        // Erev Pesach: before shkia, the usual way.
        val erev = Luach.day(monsey, LocalDate.of(2027, 4, 21))
        assertTrue(erev.has(ZmanKind.CandleLighting))
        assertTrue(!erev.has(ZmanKind.CandleLightingAfterTzeis))
    }

    @Test
    fun linesAreInOrderThroughTheDay() {
        var date = LocalDate.of(2026, 9, 1)
        while (date.isBefore(LocalDate.of(2027, 10, 31))) {
            val times = Luach.day(monsey, date).zmanim.mapNotNull { it.time?.time }
            assertEquals(times.sorted(), times)
            date = date.plusDays(1)
        }
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
