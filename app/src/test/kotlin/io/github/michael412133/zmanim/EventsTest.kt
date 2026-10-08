package io.github.michael412133.zmanim

import com.kosherjava.zmanim.hebrewcalendar.JewishDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Yahrzeits and birthdays on the hard dates, against Hebcal's yahrzeit calculator
 * (hebcal.com/yahrzeit, October 2026), which listed each one for the 13 Hebrew years from 5786.
 */
class EventsTest {

    private fun event(type: EventType, date: LocalDate, adar: AdarChoice? = null) =
        Event(1, type, "x", hebrew = true, date = date, adar = adar)

    private fun next13(event: Event): String = (5786..5798).flatMap { Events.hebrewDates(event, it) }.joinToString(", ")

    @Test
    fun yahrzeitOn30Cheshvan() {
        // 30 Cheshvan 5770; the year after had a 30 Cheshvan, so it stays the 30th, or 1 Kislev without one.
        assertEquals(
            "2025-11-21, 2026-11-10, 2027-11-30, 2028-11-19, 2029-11-08, 2030-11-26, 2031-11-16, 2032-11-04, " +
                "2033-11-22, 2034-11-12, 2035-12-02, 2036-11-20, 2037-11-08",
            next13(event(EventType.Yahrzeit, LocalDate.of(2009, 11, 17))),
        )
        // 30 Cheshvan 5771; the year after had none, so it is always the last day of Cheshvan.
        assertEquals(
            "2025-11-20, 2026-11-10, 2027-11-30, 2028-11-18, 2029-11-07, 2030-11-26, 2031-11-15, 2032-11-03, " +
                "2033-11-22, 2034-11-12, 2035-12-01, 2036-11-19, 2037-11-08",
            next13(event(EventType.Yahrzeit, LocalDate.of(2010, 11, 7))),
        )
    }

    @Test
    fun yahrzeitOn30Kislev() {
        assertEquals(
            "2025-12-20, 2026-12-10, 2027-12-30, 2028-12-18, 2029-12-07, 2030-12-26, 2031-12-15, 2032-12-03, " +
                "2033-12-22, 2034-12-12, 2035-12-31, 2036-12-19, 2037-12-08",
            next13(event(EventType.Yahrzeit, LocalDate.of(2010, 12, 7))), // 30 Kislev 5771
        )
        assertEquals(
            "2025-12-20, 2026-12-10, 2027-12-30, 2028-12-18, 2029-12-06, 2030-12-26, 2031-12-15, 2032-12-02, " +
                "2033-12-22, 2034-12-12, 2035-12-31, 2036-12-18, 2037-12-08",
            next13(event(EventType.Yahrzeit, LocalDate.of(2011, 12, 26))), // 30 Kislev 5772
        )
    }

    @Test
    fun yahrzeitsInAdar() {
        // 10 Adar 5770, a year with one Adar: Adar I in a year with two.
        assertEquals(
            "2026-02-27, 2027-02-17, 2028-03-08, 2029-02-25, 2030-02-13, 2031-03-05, 2032-02-22, 2033-02-09, " +
                "2034-03-01, 2035-02-19, 2036-03-09, 2037-02-25, 2038-02-15",
            next13(event(EventType.Yahrzeit, LocalDate.of(2010, 2, 24))),
        )
        // 10 Adar II 5771: Adar II, or Adar.
        assertEquals(
            "2026-02-27, 2027-03-19, 2028-03-08, 2029-02-25, 2030-03-15, 2031-03-05, 2032-02-22, 2033-03-11, " +
                "2034-03-01, 2035-03-21, 2036-03-09, 2037-02-25, 2038-03-17",
            next13(event(EventType.Yahrzeit, LocalDate.of(2011, 3, 16))),
        )
        // 30 Adar I 5771: 30 Shevat in a year with one Adar.
        assertEquals(
            "2026-02-17, 2027-03-09, 2028-02-27, 2029-02-15, 2030-03-05, 2031-02-23, 2032-02-12, 2033-03-01, " +
                "2034-02-19, 2035-03-11, 2036-02-28, 2037-02-15, 2038-03-07",
            next13(event(EventType.Yahrzeit, LocalDate.of(2011, 3, 6))),
        )
    }

    @Test
    fun birthdays() {
        // 10 Adar 5770: Adar II in a year with two.
        assertEquals(
            "2026-02-27, 2027-03-19, 2028-03-08, 2029-02-25, 2030-03-15, 2031-03-05, 2032-02-22, 2033-03-11, " +
                "2034-03-01, 2035-03-21, 2036-03-09, 2037-02-25, 2038-03-17",
            next13(event(EventType.Birthday, LocalDate.of(2010, 2, 24))),
        )
        // 30 Adar I 5771: 1 Nissan in a year with one Adar.
        assertEquals(
            "2026-03-19, 2027-03-09, 2028-03-28, 2029-03-17, 2030-03-05, 2031-03-25, 2032-03-13, 2033-03-01, " +
                "2034-03-21, 2035-03-11, 2036-03-29, 2037-03-17, 2038-03-07",
            next13(event(EventType.Birthday, LocalDate.of(2011, 3, 6))),
        )
        // 30 Cheshvan 5770: 1 Kislev in a year without a 30th.
        assertEquals(
            "2025-11-21, 2026-11-10, 2027-11-30, 2028-11-19, 2029-11-08, 2030-11-26, 2031-11-16, 2032-11-04, " +
                "2033-11-22, 2034-11-12, 2035-12-02, 2036-11-20, 2037-11-08",
            next13(event(EventType.Birthday, LocalDate.of(2009, 11, 17))),
        )
    }

    @Test
    fun theAdarChoiceDecidesTheYearsWithTwo() {
        val tenthOfAdar = LocalDate.of(2010, 2, 24) // 10 Adar 5770
        // 5787 has two Adars, 5786 one.
        val first = Events.hebrewDates(event(EventType.Yahrzeit, tenthOfAdar, AdarChoice.AdarI), 5787)
        val second = Events.hebrewDates(event(EventType.Yahrzeit, tenthOfAdar, AdarChoice.AdarII), 5787)
        val both = Events.hebrewDates(event(EventType.Yahrzeit, tenthOfAdar, AdarChoice.Both), 5787)
        assertEquals(listOf(JewishDate(5787, JewishDate.ADAR, 10).localDate), first)
        assertEquals(listOf(JewishDate(5787, JewishDate.ADAR_II, 10).localDate), second)
        assertEquals(first + second, both)
        assertEquals(1, Events.hebrewDates(event(EventType.Yahrzeit, tenthOfAdar, AdarChoice.Both), 5786).size)
        assertTrue(Events.occursOn(event(EventType.Yahrzeit, tenthOfAdar, AdarChoice.Both), second.single()))
    }

    @Test
    fun theFirstAdarChoiceMatchesHebcal() {
        assertEquals(AdarChoice.AdarI, Events.defaultAdar(EventType.Yahrzeit, LocalDate.of(2010, 2, 24)))
        assertEquals(AdarChoice.AdarII, Events.defaultAdar(EventType.Birthday, LocalDate.of(2010, 2, 24)))
        assertEquals(AdarChoice.AdarII, Events.defaultAdar(EventType.Yahrzeit, LocalDate.of(2011, 3, 16))) // Adar II
        assertEquals(AdarChoice.AdarI, Events.defaultAdar(EventType.Birthday, LocalDate.of(2011, 3, 6))) // Adar I
    }

    @Test
    fun englishDates() {
        val birthday = Event(2, EventType.Birthday, "x", hebrew = false, date = LocalDate.of(1990, 10, 7))
        assertTrue(Events.occursOn(birthday, LocalDate.of(2026, 10, 7)))
        assertTrue(!Events.occursOn(birthday, LocalDate.of(2026, 10, 8)))
        val leapDay = Event(3, EventType.Anniversary, "x", hebrew = false, date = LocalDate.of(2024, 2, 29))
        assertTrue(Events.occursOn(leapDay, LocalDate.of(2027, 2, 28)))
        assertTrue(Events.occursOn(leapDay, LocalDate.of(2028, 2, 29)))
    }

    @Test
    fun aHebrewDateComesBackOnItsDay() {
        // 26 Tishrei 5787 is October 7, 2026; 26 Tishrei 5788 is October 27, 2027.
        val event = Event(4, EventType.Other, "x", hebrew = true, date = LocalDate.of(2026, 10, 7))
        assertTrue(Events.occursOn(event, LocalDate.of(2026, 10, 7)))
        assertTrue(Events.occursOn(event, LocalDate.of(2027, 10, 27)))
        assertTrue(!Events.occursOn(event, LocalDate.of(2027, 10, 7)))
    }

    @Test
    fun savingAndReadingBack() {
        val events = listOf(
            Event(1, EventType.Yahrzeit, "Moshe ben Avraham", true, LocalDate.of(2010, 2, 24), AdarChoice.Both),
            Event(2, EventType.Other, "A name with a\ttab, a\nline and a \\ backslash", false, LocalDate.of(2026, 10, 7)),
            Event(3, EventType.Birthday, "שרה", true, LocalDate.of(2026, 10, 7)),
        )
        assertEquals(events, Events.decode(Events.encode(events)))
        assertEquals(emptyList<Event>(), Events.decode(null))
        assertEquals(emptyList<Event>(), Events.decode("not an event"))
    }
}
