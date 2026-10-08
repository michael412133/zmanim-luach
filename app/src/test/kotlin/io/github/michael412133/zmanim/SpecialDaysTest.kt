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
 * The grey lines only some days have, all in Monsey. Where Hebcal prints the same thing
 * (hebcal.com, with its fast, havdalah, molad and Kiddush Levana times), it is noted; Hebcal
 * rounds to the nearest minute, and this app to the safe side.
 */
class SpecialDaysTest {

    private val monsey = Places.byId("monsey")

    private fun clock(time: Date?): String {
        requireNotNull(time)
        return SimpleDateFormat("MM-dd HH:mm", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("America/New_York") }
            .format(time)
    }

    private fun day(year: Int, month: Int, dayOfMonth: Int) = Luach.day(monsey, LocalDate.of(year, month, dayOfMonth))

    private fun Day.line(kind: ZmanKind): Zman = zmanim.single { it.kind == kind }

    private fun Day.at(kind: ZmanKind): String = clock(line(kind).time).substring(6)

    private fun Day.at(kind: ZmanKind, opinion: Opinion): String =
        clock(line(kind).opinions.single { it.first == opinion }.second)

    private fun Day.has(kind: ZmanKind): Boolean = zmanim.any { it.kind == kind }

    private fun Day.special(): List<ZmanKind> = zmanim.filter { it.kind.special }.map { it.kind }

    @Test
    fun shabbosMevarchimHasTheMoladAndShabbosEnds() {
        val shabbos = day(2026, 10, 10)
        // Hebcal: Molad Cheshvan, Sunday 9:43am and 2 chalakim.
        val molad = shabbos.line(ZmanKind.Molad).molad!!
        assertEquals(LocalDate.of(2026, 10, 12), molad.month) // 1 Cheshvan
        assertEquals(LocalDate.of(2026, 10, 11), molad.date)
        assertEquals(9, molad.hours)
        assertEquals(43, molad.minutes)
        assertEquals(2, molad.chalakim)
        assertEquals(ZmanKind.Molad, shabbos.zmanim.first().kind)
        // Hebcal's havdalah at 8.5°: 7:04pm, exactly 19:04:08.
        assertEquals("19:05", shabbos.at(ZmanKind.ShabbosEnds))
        assertEquals("10-10 19:14", shabbos.at(ZmanKind.ShabbosEnds, Opinion.Ends50)) // shkia 18:23:17
        assertEquals("10-10 19:24", shabbos.at(ZmanKind.ShabbosEnds, Opinion.Ends60))
        assertEquals("10-10 19:36", shabbos.at(ZmanKind.ShabbosEnds, Opinion.Ends72))
        assertEquals(shabbos.at(ZmanKind.RabbeinuTam), shabbos.at(ZmanKind.ShabbosEnds, Opinion.Ends72).substring(6))
    }

    @Test
    fun shabbosIntoYomTovAndYomTovEnds() {
        // Shabbos Shemini Atzeres: candles for Simchas Torah after tzeis. Hebcal: 7:15pm, exactly 19:15:25.
        val shabbos = day(2026, 10, 3)
        assertEquals("19:16", shabbos.at(ZmanKind.CandleLightingAfterTzeis))
        assertTrue(!shabbos.has(ZmanKind.ShabbosEnds))
        assertTrue(!shabbos.has(ZmanKind.ShabbosAndYomTovEnd))
        // Simchas Torah ends. Hebcal: 7:14pm, exactly 19:13:47.
        assertEquals("19:14", day(2026, 10, 4).at(ZmanKind.YomTovEnds))
        // Hoshana Rabba, Erev Shemini Atzeres. Hebcal: 6:18pm.
        assertEquals("18:18", day(2026, 10, 2).at(ZmanKind.CandleLighting))
        // Shavuos 5787's second day is Shabbos.
        assertTrue(day(2027, 6, 12).has(ZmanKind.ShabbosAndYomTovEnd))
    }

    @Test
    fun aMinorFast() {
        // Asara B'Teves 5787. Hebcal: fast begins 5:49am (alos 16.1°, exactly 5:48:34), ends 5:08pm (7.083°, 17:07:54).
        val fast = day(2026, 12, 20)
        assertEquals("12-20 05:48", fast.at(ZmanKind.FastBegins, Opinion.Alos16Point1))
        assertEquals("12-20 17:08", fast.at(ZmanKind.FastEnds, Opinion.FastEnds7Point083))
        // The first opinions: alos 72 minutes, and the end at 8.5°.
        assertEquals("06:05", fast.at(ZmanKind.FastBegins))
        assertEquals("17:17", fast.at(ZmanKind.FastEnds))
        assertEquals(fast.at(ZmanKind.Alos), fast.at(ZmanKind.FastBegins))
        assertTrue(!fast.has(ZmanKind.FastBeginsAtShkia))
    }

    @Test
    fun tishaBavBeginsTheEveningBefore() {
        // Tisha B'Av 5787 is a Thursday. Hebcal: fast begins Wednesday 8:01pm, at shkia, exactly 20:00:39.
        val erev = day(2027, 8, 11)
        assertEquals("20:00", erev.at(ZmanKind.FastBeginsAtShkia))
        assertEquals(erev.at(ZmanKind.Shkia), erev.at(ZmanKind.FastBeginsAtShkia))
        val fast = day(2027, 8, 12)
        assertEquals("20:44", fast.at(ZmanKind.FastEnds)) // 8.5°, exactly 20:43:59
        assertTrue(!fast.has(ZmanKind.FastBegins))
    }

    @Test
    fun tishaBavPushedOffFromShabbos() {
        // 9 Av 5785 was Shabbos, so the fast was Sunday: it begins at shkia on Shabbos, as Shabbos ends.
        val shabbos = day(2025, 8, 2)
        assertTrue(shabbos.has(ZmanKind.FastBeginsAtShkia))
        assertTrue(shabbos.has(ZmanKind.ShabbosEnds))
        assertTrue(!shabbos.has(ZmanKind.FastEnds))
        assertTrue(day(2025, 8, 3).has(ZmanKind.FastEnds))
    }

    @Test
    fun yomKippur() {
        val erev = day(2027, 10, 10)
        assertEquals("18:05", erev.at(ZmanKind.CandleLighting)) // shkia 18:23:40
        assertEquals("18:23", erev.at(ZmanKind.FastBeginsAtShkia))
        val yomKippur = day(2027, 10, 11)
        assertEquals("19:03", yomKippur.at(ZmanKind.YomKippurEnds)) // 8.5°, exactly 19:02:57
        assertTrue(!yomKippur.has(ZmanKind.FastEnds))
        assertTrue(!yomKippur.has(ZmanKind.YomTovEnds))
    }

    @Test
    fun erevPesach() {
        val erev = day(2027, 4, 21)
        // Magen Avraham: eating until 4 of his hours, burning until 5. Exactly 10:15:37 and 11:35:28.
        assertEquals("10:15", erev.at(ZmanKind.AchilasChametz))
        assertEquals("11:35", erev.at(ZmanKind.BiurChametz))
        assertEquals(erev.at(ZmanKind.SofZmanTefillah), erev.at(ZmanKind.AchilasChametz))
        // Gra: from netz 6:08:11 to shkia 19:42:28, 4 and 5 hours of 67m51s are 10:39:37 and 11:47:28.
        assertEquals("04-21 10:39", erev.at(ZmanKind.AchilasChametz, Opinion.Gra))
        assertEquals("04-21 11:47", erev.at(ZmanKind.BiurChametz, Opinion.Gra))
        assertTrue(!day(2027, 4, 20).has(ZmanKind.AchilasChametz))
    }

    @Test
    fun sefirasHaOmer() {
        // The first count is the night after the first Seder, the last the night before Erev Shavuos.
        val first = day(2027, 4, 22)
        assertEquals(1, first.line(ZmanKind.Omer).omerDay)
        assertEquals(first.at(ZmanKind.Tzeis), first.at(ZmanKind.Omer))
        assertEquals(49, day(2027, 6, 9).line(ZmanKind.Omer).omerDay)
        assertTrue(!day(2027, 4, 21).has(ZmanKind.Omer))
        assertTrue(!day(2027, 6, 10).has(ZmanKind.Omer))
        // Right after tzeis in the list.
        val list = first.zmanim.map { it.kind }
        assertEquals(list.indexOf(ZmanKind.Tzeis) + 1, list.indexOf(ZmanKind.Omer))
    }

    @Test
    fun kiddushLevana() {
        // Hebcal: the latest time, halfway between the moldos, is October 25 at 9:44pm; exactly 21:44:11.
        // It is after tzeis, so that night is the last one, and it goes on October 25.
        val end = day(2026, 10, 25)
        assertEquals("21:44", end.at(ZmanKind.KiddushLevanaEnd))
        // The other opinion is the next morning, and the pop-up shows it with its day.
        assertEquals("10-26 03:22", end.at(ZmanKind.KiddushLevanaEnd, Opinion.Levana15Days))
        assertTrue(!day(2026, 10, 26).has(ZmanKind.KiddushLevanaEnd))
        // 3 days after the molad is Wednesday October 14 at 3:22:10am. That is the night after
        // Tuesday, so it goes on Tuesday's list, after chatzos halaila, and not on Wednesday's.
        val tuesday = day(2026, 10, 13)
        assertEquals("10-14 03:23", clock(tuesday.line(ZmanKind.KiddushLevanaStart).time))
        assertTrue(!day(2026, 10, 14).has(ZmanKind.KiddushLevanaStart))
        assertEquals(ZmanKind.KiddushLevanaStart, tuesday.zmanim.last().kind)
        assertEquals("10-18 03:23", tuesday.at(ZmanKind.KiddushLevanaStart, Opinion.Levana7Days))
        // With 7 days chosen, it is Motzei Shabbos's night.
        val seven = Luach.day(monsey, LocalDate.of(2026, 10, 17), Opinions.Default.with(Opinion.Levana7Days))
        assertEquals("10-18 03:23", clock(seven.line(ZmanKind.KiddushLevanaStart).time))
        // An end in the small hours: the night before is the last one.
        assertEquals("04-21 02:08", clock(day(2027, 4, 20).line(ZmanKind.KiddushLevanaEnd).time))
        assertTrue(!day(2027, 4, 21).has(ZmanKind.KiddushLevanaEnd))
        // An end in the daytime: the night before was the last one too.
        assertEquals("11-24 09:28", clock(day(2026, 11, 23).line(ZmanKind.KiddushLevanaEnd).time))
        // A start in the daytime is said from that night, so it stays on its own day.
        assertEquals("11-12 15:07", clock(day(2026, 11, 12).line(ZmanKind.KiddushLevanaStart).time))
    }

    @Test
    fun chatzosHalailaIsLast() {
        val day = day(2026, 10, 25)
        assertEquals(ZmanKind.ChatzosHalaila, day.zmanim.last().kind)
    }

    @Test
    fun aPlainDayHasNoGreyLines() {
        assertEquals(emptyList<ZmanKind>(), day(2026, 10, 7).special())
    }
}
