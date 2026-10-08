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

    /** The spot the phone's GPS found, rather than a town from the list. */
    val isGps: Boolean get() = id == GPS_ID

    companion object {
        const val GPS_ID = "gps"
    }
}

/** Which way a time is rounded to the minute. Always toward the safe side. */
enum class Rounding {
    /** A deadline, like the last time for krias shema, is shown a minute early rather than late. */
    Earlier,

    /** A starting time, like nightfall, is shown a minute late rather than early. */
    Later,
}

/**
 * Every line the list can show. The words for each, in English and in Hebrew, are in the
 * app's strings, so this file only says which zman and when.
 */
enum class ZmanKind {
    Alos72,
    Misheyakir,
    Netz,
    SofZmanShmaMGA,
    SofZmanShmaGRA,
    SofZmanTfilaMGA,
    SofZmanTfilaGRA,
    Chatzos,
    MinchaGedola,
    MinchaKetana,
    PlagHamincha,
    CandleLighting,
    CandleLightingAfterTzeis,
    Shkia,
    Tzeis,
    RabbeinuTam,
}

/** One line of the list, with the time already rounded to the minute. */
data class Zman(
    val kind: ZmanKind,
    /** Null only where the sun never gets that low, which does not happen in these towns. */
    val time: Date?,
)

/** Everything the day's screen shows for one day in one place. */
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

        // The daf yomi cycle began in 1923; there is none to show before then.
        val daf = runCatching { "דף יומי " + hebrew.formatDafYomiBavli(jewish.dafYomiBavli) }.getOrDefault("")

        return Day(
            date = date,
            hebrewDate = hebrew.format(jewish),
            tonightHebrewDate = tonightDate,
            special = special,
            parsha = parshaOfTheWeek(place, date, hebrew),
            daf = daf,
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
        fun add(kind: ZmanKind, time: Date?, rounding: Rounding) {
            list += Zman(kind, round(time, rounding))
        }

        add(ZmanKind.Alos72, z.alos72, Rounding.Earlier)
        add(ZmanKind.Misheyakir, z.misheyakir11Point5Degrees, Rounding.Later)
        add(ZmanKind.Netz, z.seaLevelSunrise, Rounding.Later)
        add(ZmanKind.SofZmanShmaMGA, z.sofZmanShmaMGA, Rounding.Earlier)
        add(ZmanKind.SofZmanShmaGRA, z.sofZmanShmaGRA, Rounding.Earlier)
        add(ZmanKind.SofZmanTfilaMGA, z.sofZmanTfilaMGA, Rounding.Earlier)
        add(ZmanKind.SofZmanTfilaGRA, z.sofZmanTfilaGRA, Rounding.Earlier)
        add(ZmanKind.Chatzos, z.chatzos, Rounding.Earlier)
        add(ZmanKind.MinchaGedola, z.minchaGedola, Rounding.Later)
        add(ZmanKind.MinchaKetana, z.minchaKetana, Rounding.Later)
        add(ZmanKind.PlagHamincha, z.plagHamincha, Rounding.Later)

        // Candles are lit 18 minutes before shkia on Erev Shabbos and Erev Yom Tov, and also on
        // a Friday that is itself Yom Tov. When Yom Tov follows Shabbos or another day of Yom
        // Tov, they are lit only after tzeis, from a flame that was already burning.
        val candles = jewish.hasCandleLighting()
        val afterTzeis = candles && date.dayOfWeek != DayOfWeek.FRIDAY && jewish.isAssurBemelacha
        if (candles && !afterTzeis) add(ZmanKind.CandleLighting, z.candleLighting, Rounding.Earlier)

        add(ZmanKind.Shkia, z.seaLevelSunset, Rounding.Earlier)
        add(ZmanKind.Tzeis, z.tzaisGeonim8Point5Degrees, Rounding.Later)
        if (afterTzeis) add(ZmanKind.CandleLightingAfterTzeis, z.tzaisGeonim8Point5Degrees, Rounding.Later)
        add(ZmanKind.RabbeinuTam, z.tzais72, Rounding.Later)
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

    fun jewishCalendar(place: Place, date: LocalDate): JewishCalendar =
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
