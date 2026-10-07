package io.github.michael412133.zmanim

import com.kosherjava.zmanim.ComplexZmanimCalendar
import com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import com.kosherjava.zmanim.util.GeoLocation
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/**
 * A town the times are worked out for. Every time is figured at sea level, the way most
 * printed luchos in America do it, so a place needs no elevation.
 */
data class Place(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val timeZone: String = "America/New_York",
    val inIsrael: Boolean = false,
) {
    val zone: ZoneId get() = ZoneId.of(timeZone)
}

/** Which way a time is rounded to the minute. Always toward the safe side. */
enum class Rounding {
    /** A deadline, like the last time for krias shema, is shown a minute early rather than late. */
    Earlier,

    /** A starting time, like nightfall, is shown a minute late rather than early. */
    Later,
}

/** One line of the list: what it is, which opinion, and the time already rounded to the minute. */
data class Zman(
    val name: String,
    val note: String,
    /** Null only where the sun never gets that low, which does not happen in these towns. */
    val time: Date?,
)

/** Everything the screen shows for one day in one place. */
data class Day(
    val date: LocalDate,
    /** The Hebrew date in Hebrew letters, like כ״ו תשרי תשפ״ז. */
    val hebrewDate: String,
    /** The Hebrew date that starts at tonight's shkia, without the year. */
    val tonightHebrewDate: String,
    /** Yom tov, fast, Rosh Chodesh, Chanukah or sefira, joined with a dot. Empty on a plain day. */
    val special: String,
    /** This week's parsha (or the yom tov that replaces it) and the daf yomi. */
    val parsha: String,
    val daf: String,
    val zmanim: List<Zman>,
    /** Unrounded shkia, used to tell when the Hebrew date turns over. */
    val shkia: Date?,
)

object Luach {

    fun day(place: Place, date: LocalDate): Day {
        val zmanim = calendarFor(place, date)
        val jewish = jewishCalendar(place, date)
        val hebrew = HebrewDateFormatter().apply { setHebrewFormat(true) }

        val special = listOf(
            hebrew.formatYomTov(jewish),
            hebrew.formatRoshChodesh(jewish),
            hebrew.formatOmer(jewish),
        ).filter { it.isNotBlank() }.joinToString(" · ")

        val tonight = jewishCalendar(place, date.plusDays(1))
        val tonightDate = hebrew.formatHebrewNumber(tonight.jewishDayOfMonth) + " " + hebrew.formatMonth(tonight)

        return Day(
            date = date,
            hebrewDate = hebrew.format(jewish),
            tonightHebrewDate = tonightDate,
            special = special,
            parsha = parshaOfTheWeek(place, date, hebrew),
            daf = "דף יומי " + hebrew.formatDafYomiBavli(jewish.dafYomiBavli),
            zmanim = zmanimFor(zmanim, jewish, date),
            shkia = zmanim.seaLevelSunset,
        )
    }

    /** Rounds to the whole minute toward the safe side. */
    fun round(time: Date?, rounding: Rounding): Date? {
        if (time == null) return null
        val minute = 60_000L
        val floor = Math.floorDiv(time.time, minute) * minute
        return when (rounding) {
            Rounding.Earlier -> Date(floor)
            Rounding.Later -> if (floor == time.time) Date(floor) else Date(floor + minute)
        }
    }

    private fun zmanimFor(z: ComplexZmanimCalendar, jewish: JewishCalendar, date: LocalDate): List<Zman> {
        val list = mutableListOf<Zman>()
        fun add(name: String, note: String, time: Date?, rounding: Rounding) {
            list += Zman(name, note, round(time, rounding))
        }

        add("Alos HaShachar", "72 minutes before netz", z.alos72, Rounding.Earlier)
        add("Misheyakir", "Earliest tallis and tefillin, 11.5°", z.misheyakir11Point5Degrees, Rounding.Later)
        add("Netz HaChama", "Sunrise", z.seaLevelSunrise, Rounding.Later)
        add("Sof Zman Krias Shema", "Magen Avraham", z.sofZmanShmaMGA, Rounding.Earlier)
        add("Sof Zman Krias Shema", "Gra", z.sofZmanShmaGRA, Rounding.Earlier)
        add("Sof Zman Tefillah", "Magen Avraham", z.sofZmanTfilaMGA, Rounding.Earlier)
        add("Sof Zman Tefillah", "Gra", z.sofZmanTfilaGRA, Rounding.Earlier)
        add("Chatzos", "Midday", z.chatzos, Rounding.Earlier)
        add("Mincha Gedola", "Earliest mincha", z.minchaGedola, Rounding.Later)
        add("Mincha Ketana", "Gra", z.minchaKetana, Rounding.Later)
        add("Plag HaMincha", "Gra", z.plagHamincha, Rounding.Later)

        // Candles are lit 18 minutes before shkia on Erev Shabbos and Erev Yom Tov, and also on
        // a Friday that is itself Yom Tov. When Yom Tov follows Shabbos or another day of Yom
        // Tov, they are lit only after tzeis, from a flame that was already burning.
        if (jewish.hasCandleLighting()) {
            val beforeShkia = date.dayOfWeek == DayOfWeek.FRIDAY || !jewish.isAssurBemelacha
            if (beforeShkia) {
                add("Hadlakas Neiros", "18 minutes before shkia", z.candleLighting, Rounding.Earlier)
            }
        }

        add("Shkias HaChama", "Sunset", z.seaLevelSunset, Rounding.Earlier)
        add("Tzeis HaKochavim", "Nightfall, 8.5°", z.tzaisGeonim8Point5Degrees, Rounding.Later)

        if (jewish.hasCandleLighting() && date.dayOfWeek != DayOfWeek.FRIDAY && jewish.isAssurBemelacha) {
            add("Hadlakas Neiros", "Not before tzeis, from an existing flame", z.tzaisGeonim8Point5Degrees, Rounding.Later)
        }

        add("Tzeis Rabbeinu Tam", "72 minutes after shkia", z.tzais72, Rounding.Later)
        return list
    }

    /**
     * The parsha read on the Shabbos of this week. When that Shabbos is a Yom Tov with no parsha
     * of its own, its name is shown instead, the way a printed luach does.
     */
    private fun parshaOfTheWeek(place: Place, date: LocalDate, hebrew: HebrewDateFormatter): String {
        val shabbos = jewishCalendar(place, date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)))
        val parsha = hebrew.formatParsha(shabbos)
        if (parsha.isNullOrBlank()) return hebrew.formatYomTov(shabbos)
        val special = hebrew.formatSpecialParsha(shabbos)
        return if (special.isNullOrBlank()) "פרשת $parsha" else "פרשת $parsha · $special"
    }

    private fun jewishCalendar(place: Place, date: LocalDate): JewishCalendar =
        JewishCalendar(date).apply { setInIsrael(place.inIsrael) }

    private fun calendarFor(place: Place, date: LocalDate): ComplexZmanimCalendar {
        val timeZone = TimeZone.getTimeZone(place.timeZone)
        val location = GeoLocation(place.name, place.latitude, place.longitude, 0.0, timeZone)
        val calendar = Calendar.getInstance(timeZone).apply {
            clear()
            set(date.year, date.monthValue - 1, date.dayOfMonth, 12, 0, 0)
        }
        return ComplexZmanimCalendar(location).apply { setCalendar(calendar) }
    }
}
