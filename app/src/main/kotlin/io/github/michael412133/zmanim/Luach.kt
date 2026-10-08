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
    val region: Region = Region.Rockland,
) {
    val zone: ZoneId get() = ZoneId.of(timeZone)

    /** The spot the phone's GPS found, rather than a town from the list. */
    val isGps: Boolean get() = id == GPS_ID

    companion object {
        const val GPS_ID = "gps"
    }
}

/** The sections the town list is divided into. */
enum class Region { Rockland, NewYork, America, Israel, Europe }

/** Which way a time is rounded to the minute. Always toward the safe side. */
enum class Rounding {
    /** A deadline, like the last time for krias shema, is shown a minute early rather than late. */
    Earlier,

    /** A starting time, like nightfall, is shown a minute late rather than early. */
    Later,
}

/**
 * Every line the list can show, in the order lines with the same time are listed. The words
 * for each, in English and in Hebrew, are in the app's strings, so this file only says which
 * zman, how it is rounded, and which choice in the settings it follows.
 *
 * Special lines, the ones only some days have, are drawn on grey.
 */
enum class ZmanKind(val group: Group?, val rounding: Rounding, val special: Boolean = false) {
    /** On Shabbos Mevarchim, the molad of the coming month. It has no time of its own today. */
    Molad(null, Rounding.Earlier, special = true),
    KiddushLevanaStart(Group.LevanaStart, Rounding.Later, special = true),
    KiddushLevanaEnd(Group.LevanaEnd, Rounding.Earlier, special = true),

    /** A minor fast begins at alos. */
    FastBegins(Group.Alos, Rounding.Earlier, special = true),
    Alos(Group.Alos, Rounding.Earlier),
    Misheyakir(Group.Misheyakir, Rounding.Later),
    Netz(null, Rounding.Later),
    SofZmanShema(Group.ShemaTefillah, Rounding.Earlier),
    SofZmanTefillah(Group.ShemaTefillah, Rounding.Earlier),
    AchilasChametz(Group.ShemaTefillah, Rounding.Earlier, special = true),
    BiurChametz(Group.ShemaTefillah, Rounding.Earlier, special = true),
    Chatzos(null, Rounding.Earlier),
    MinchaGedola(Group.Afternoon, Rounding.Later),
    MinchaKetana(Group.Afternoon, Rounding.Later),
    PlagHamincha(Group.Afternoon, Rounding.Later),
    CandleLighting(Group.Candles, Rounding.Earlier, special = true),

    /** Yom Kippur and Tisha B'Av begin at shkia the evening before. */
    FastBeginsAtShkia(null, Rounding.Earlier, special = true),
    Shkia(null, Rounding.Earlier),
    Tzeis(Group.Tzeis, Rounding.Later),

    /** Tonight's count, after tzeis. */
    Omer(Group.Tzeis, Rounding.Later, special = true),
    FastEnds(Group.FastEnds, Rounding.Later, special = true),
    ShabbosEnds(Group.ShabbosEnds, Rounding.Later, special = true),
    YomTovEnds(Group.ShabbosEnds, Rounding.Later, special = true),
    ShabbosAndYomTovEnd(Group.ShabbosEnds, Rounding.Later, special = true),
    YomKippurEnds(Group.ShabbosEnds, Rounding.Later, special = true),

    /** The second night of Yom Tov, or Yom Tov right after Shabbos: candles only once the day before is out. */
    CandleLightingAfterTzeis(Group.ShabbosEnds, Rounding.Later, special = true),
    RabbeinuTam(null, Rounding.Later),
    ChatzosHalaila(null, Rounding.Earlier),
    ;

    /**
     * The line that stands for this one in the list of times to show, so hiding "Candle
     * lighting" hides both kinds of it, and hiding "Shabbos and Yom Tov end" hides all four.
     */
    val shownAs: ZmanKind
        get() = when (this) {
            CandleLightingAfterTzeis -> CandleLighting
            YomTovEnds, ShabbosAndYomTovEnd, YomKippurEnds -> ShabbosEnds
            FastBeginsAtShkia, FastEnds -> FastBegins
            BiurChametz -> AchilasChametz
            KiddushLevanaEnd -> KiddushLevanaStart
            else -> this
        }

    companion object {
        /** The lines in the settings' list of times to show, one for each kind of line. */
        val toggles: List<ZmanKind> = listOf(
            Alos, Misheyakir, Netz, SofZmanShema, SofZmanTefillah, Chatzos, MinchaGedola,
            MinchaKetana, PlagHamincha, Shkia, Tzeis, RabbeinuTam, ChatzosHalaila,
            CandleLighting, ShabbosEnds, FastBegins, AchilasChametz, Omer, KiddushLevanaStart, Molad,
        )
    }
}

/** The molad as it is announced in shul: the day, and the time in Yerushalayim with its chalakim. */
data class Molad(
    /** The first day of the month the molad is for. */
    val month: LocalDate,
    /** The day of the molad by the clock (a molad at 9 at night is still that evening's date). */
    val date: LocalDate,
    val hours: Int,
    val minutes: Int,
    val chalakim: Int,
)

/** One line of the list, with its time already rounded to the minute. */
data class Zman(
    val kind: ZmanKind,
    /** Null only where the sun never gets that low, which does not happen in these towns, and for the molad. */
    val time: Date?,
    /** The opinion this time follows, when the settings offer a choice for it. */
    val opinion: Opinion? = null,
    /** Every opinion the settings offer for this line, each with its own time, for the pop-up. */
    val opinions: List<Pair<Opinion, Date?>> = emptyList(),
    /** For the omer line: the day counted tonight. */
    val omerDay: Int = 0,
    val molad: Molad? = null,
)

/** Everything the screen shows for one day in one place, apart from the reader's own events. */
data class Day(
    val date: LocalDate,
    /** The Hebrew date in Hebrew letters, like כ״ו תשרי תשפ״ז. */
    val hebrewDate: String,
    /** The Hebrew date that starts at tonight's shkia, without the year. */
    val tonightHebrewDate: String,
    /** Yom Tov, a fast, Rosh Chodesh or Chanukah, joined with a dot. Empty on a plain day. */
    val special: String,
    /** This week's parsha (or the Yom Tov that replaces it) and the daf yomi. */
    val parsha: String,
    val daf: String,
    /** In order through the day, the molad first. */
    val zmanim: List<Zman>,
    /** Unrounded shkia, used to tell when the Hebrew date turns over. */
    val shkia: Date?,
)

object Luach {

    fun day(place: Place, date: LocalDate, opinions: Opinions = Opinions.Default): Day {
        val calendar = calendarFor(place, date)
        val jewish = jewishCalendar(place, date)
        val hebrew = HebrewDateFormatter().apply { setHebrewFormat(true) }

        val special = listOf(
            hebrew.formatYomTov(jewish),
            hebrew.formatRoshChodesh(jewish),
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
            zmanim = zmanimFor(calendar, jewish, tonight, date, opinions),
            shkia = calendar.seaLevelSunset,
        )
    }

    /**
     * A day the month view draws in grey: Yom Tov and Chol Hamoed, a fast, Rosh Chodesh, and the
     * days with a name of their own like Chanukah, Purim or Lag BaOmer. Erev Yom Tov is not
     * grey, though its name still shows with the day.
     */
    fun isMarked(jewish: JewishCalendar): Boolean {
        val index = jewish.yomTovIndex
        val named = index != -1 && index !in erevDays && index != JewishCalendar.ISRU_CHAG
        return named || jewish.isRoshChodesh
    }

    private val erevDays = setOf(
        JewishCalendar.EREV_PESACH,
        JewishCalendar.EREV_SHAVUOS,
        JewishCalendar.EREV_ROSH_HASHANA,
        JewishCalendar.EREV_YOM_KIPPUR,
        JewishCalendar.EREV_SUCCOS,
    )

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

    private fun zmanimFor(
        z: ComplexZmanimCalendar,
        jewish: JewishCalendar,
        tomorrow: JewishCalendar,
        date: LocalDate,
        opinions: Opinions,
    ): List<Zman> {
        val list = mutableListOf<Zman>()

        /** A line whose time follows the opinion chosen for its group, with every other opinion for the pop-up. */
        fun add(kind: ZmanKind) {
            val group = kind.group
            if (group == null) {
                list += Zman(kind, round(timeOf(z, kind, null), kind.rounding))
                return
            }
            val chosen = opinions[group]
            val all = group.opinions.map { it to round(timeOf(z, kind, it), kind.rounding) }
            list += Zman(
                kind = kind,
                time = all.first { it.first == chosen }.second,
                opinion = chosen,
                opinions = all,
            )
        }

        val index = jewish.yomTovIndex
        val minorFast = jewish.isTaanis && index != JewishCalendar.YOM_KIPPUR && index != JewishCalendar.TISHA_BEAV

        if (jewish.isShabbosMevorchim) list += Zman(ZmanKind.Molad, null, molad = moladAfter(jewish, date))
        if (minorFast) add(ZmanKind.FastBegins)
        add(ZmanKind.Alos)
        add(ZmanKind.Misheyakir)
        add(ZmanKind.Netz)
        add(ZmanKind.SofZmanShema)
        add(ZmanKind.SofZmanTefillah)
        if (index == JewishCalendar.EREV_PESACH) {
            add(ZmanKind.AchilasChametz)
            add(ZmanKind.BiurChametz)
        }
        add(ZmanKind.Chatzos)
        add(ZmanKind.MinchaGedola)
        add(ZmanKind.MinchaKetana)
        add(ZmanKind.PlagHamincha)

        // Candles are lit before shkia on Erev Shabbos and Erev Yom Tov, and on a Friday that is
        // itself Yom Tov. When Yom Tov follows Shabbos or another day of Yom Tov, they are lit
        // only once that day is out, from a flame that was already burning.
        val candles = jewish.hasCandleLighting()
        val afterTzeis = candles && date.dayOfWeek != DayOfWeek.FRIDAY && jewish.isAssurBemelacha
        if (candles && !afterTzeis) add(ZmanKind.CandleLighting)

        val fastTomorrow = tomorrow.yomTovIndex == JewishCalendar.TISHA_BEAV
        if (index == JewishCalendar.EREV_YOM_KIPPUR || fastTomorrow) add(ZmanKind.FastBeginsAtShkia)
        add(ZmanKind.Shkia)
        add(ZmanKind.Tzeis)

        // Tonight's count is the day of the omer of the date that begins tonight.
        val omer = tomorrow.dayOfOmer
        if (omer > 0) {
            val tzeis = list.last { it.kind == ZmanKind.Tzeis }
            list += Zman(ZmanKind.Omer, tzeis.time, opinion = tzeis.opinion, omerDay = omer)
        }

        if (minorFast || index == JewishCalendar.TISHA_BEAV) add(ZmanKind.FastEnds)

        // The end of Shabbos or Yom Tov, when tomorrow is a weekday again.
        if (jewish.isAssurBemelacha && !tomorrow.isAssurBemelacha) {
            val shabbos = date.dayOfWeek == DayOfWeek.SATURDAY
            add(
                when {
                    index == JewishCalendar.YOM_KIPPUR -> ZmanKind.YomKippurEnds
                    shabbos && jewish.isYomTovAssurBemelacha -> ZmanKind.ShabbosAndYomTovEnd
                    shabbos -> ZmanKind.ShabbosEnds
                    else -> ZmanKind.YomTovEnds
                },
            )
        }
        if (afterTzeis) add(ZmanKind.CandleLightingAfterTzeis)
        add(ZmanKind.RabbeinuTam)

        // Kiddush Levana's times come only on the day they fall on; KosherJava leaves them out otherwise.
        kiddushLevana(z, jewish, ZmanKind.KiddushLevanaStart, opinions)?.let { list += it }
        kiddushLevana(z, jewish, ZmanKind.KiddushLevanaEnd, opinions)?.let { list += it }

        add(ZmanKind.ChatzosHalaila)

        // In order through the day. Lines with the same time keep the order they were added in,
        // and the molad, which has no time, stays first.
        return list.sortedBy { it.time?.time ?: Long.MIN_VALUE }
    }

    /**
     * The time of one line by one opinion. Every one of these is KosherJava's own calculation;
     * this only picks which of them a choice in the settings stands for.
     */
    @Suppress("DEPRECATION") // KosherJava marks the Magen Avraham's plag as one to use l'chumra only.
    private fun timeOf(z: ComplexZmanimCalendar, kind: ZmanKind, opinion: Opinion?): Date? = when (kind) {
        ZmanKind.Alos, ZmanKind.FastBegins -> when (opinion) {
            Opinion.Alos90 -> z.alos90
            Opinion.Alos16Point1 -> z.alos16Point1Degrees
            Opinion.Alos19Point8 -> z.alos19Point8Degrees
            else -> z.alos72
        }
        ZmanKind.Misheyakir -> when (opinion) {
            Opinion.Misheyakir11 -> z.misheyakir11Degrees
            Opinion.Misheyakir10Point2 -> z.misheyakir10Point2Degrees
            else -> z.misheyakir11Point5Degrees
        }
        ZmanKind.Netz -> z.seaLevelSunrise
        ZmanKind.SofZmanShema -> when (opinion) {
            Opinion.Mga16Point1 -> z.sofZmanShmaMGA16Point1Degrees
            Opinion.Gra -> z.sofZmanShmaGRA
            Opinion.BaalHatanya -> z.sofZmanShmaBaalHatanya
            else -> z.sofZmanShmaMGA
        }
        ZmanKind.SofZmanTefillah -> when (opinion) {
            Opinion.Mga16Point1 -> z.sofZmanTfilaMGA16Point1Degrees
            Opinion.Gra -> z.sofZmanTfilaGRA
            Opinion.BaalHatanya -> z.sofZmanTfilaBaalHatanya
            else -> z.sofZmanTfilaMGA
        }
        ZmanKind.AchilasChametz -> when (opinion) {
            Opinion.Mga16Point1 -> z.sofZmanAchilasChametzMGA16Point1Degrees
            Opinion.Gra -> z.sofZmanAchilasChametzGRA
            Opinion.BaalHatanya -> z.sofZmanAchilasChametzBaalHatanya
            else -> z.sofZmanAchilasChametzMGA72Minutes
        }
        ZmanKind.BiurChametz -> when (opinion) {
            Opinion.Mga16Point1 -> z.sofZmanBiurChametzMGA16Point1Degrees
            Opinion.Gra -> z.sofZmanBiurChametzGRA
            Opinion.BaalHatanya -> z.sofZmanBiurChametzBaalHatanya
            else -> z.sofZmanBiurChametzMGA72Minutes
        }
        ZmanKind.Chatzos -> z.chatzos
        ZmanKind.MinchaGedola -> when (opinion) {
            Opinion.AfternoonMga -> z.minchaGedola72Minutes
            Opinion.AfternoonBaalHatanya -> z.minchaGedolaBaalHatanya
            else -> z.minchaGedola
        }
        ZmanKind.MinchaKetana -> when (opinion) {
            Opinion.AfternoonMga -> z.minchaKetana72Minutes
            Opinion.AfternoonBaalHatanya -> z.minchaKetanaBaalHatanya
            else -> z.minchaKetana
        }
        ZmanKind.PlagHamincha -> when (opinion) {
            Opinion.AfternoonMga -> z.plagHamincha72Minutes
            Opinion.AfternoonBaalHatanya -> z.plagHaminchaBaalHatanya
            else -> z.plagHamincha
        }
        ZmanKind.CandleLighting -> {
            z.candleLightingOffset = (opinion ?: Opinion.Candles18).candleMinutes.toDouble()
            z.candleLighting
        }
        ZmanKind.FastBeginsAtShkia, ZmanKind.Shkia -> z.seaLevelSunset
        ZmanKind.Tzeis, ZmanKind.Omer -> when (opinion) {
            Opinion.Tzeis7Point083 -> z.tzaisGeonim7Point083Degrees
            Opinion.Tzeis50 -> z.tzais50
            else -> z.tzaisGeonim8Point5Degrees
        }
        ZmanKind.FastEnds -> when (opinion) {
            Opinion.FastEnds7Point083 -> z.tzaisGeonim7Point083Degrees
            Opinion.FastEnds50 -> z.tzais50
            else -> z.tzaisGeonim8Point5Degrees
        }
        ZmanKind.ShabbosEnds, ZmanKind.YomTovEnds, ZmanKind.ShabbosAndYomTovEnd, ZmanKind.YomKippurEnds,
        ZmanKind.CandleLightingAfterTzeis -> when (opinion) {
            Opinion.Ends50 -> z.tzais50
            Opinion.Ends60 -> z.tzais60
            Opinion.Ends72 -> z.tzais72
            else -> z.tzaisGeonim8Point5Degrees
        }
        ZmanKind.RabbeinuTam -> z.tzais72
        ZmanKind.ChatzosHalaila -> z.solarMidnight
        ZmanKind.KiddushLevanaStart -> when (opinion) {
            Opinion.Levana7Days -> z.tchilasZmanKidushLevana7Days
            else -> z.tchilasZmanKidushLevana3Days
        }
        ZmanKind.KiddushLevanaEnd -> when (opinion) {
            Opinion.Levana15Days -> z.sofZmanKidushLevana15Days
            else -> z.sofZmanKidushLevanaBetweenMoldos
        }
        ZmanKind.Molad -> null
    }

    /**
     * The start or end of Kiddush Levana, on the day the chosen opinion's time falls on. The
     * other opinion's time is usually on another day, so the pop-up gets it straight from the
     * month's molad rather than from today's zmanim.
     */
    private fun kiddushLevana(
        z: ComplexZmanimCalendar,
        jewish: JewishCalendar,
        kind: ZmanKind,
        opinions: Opinions,
    ): Zman? {
        val group = kind.group ?: return null
        val chosen = opinions[group]
        val time = round(timeOf(z, kind, chosen), kind.rounding) ?: return null
        // On the 30th, the times belong to the month that starts tomorrow.
        val month = (jewish.clone() as JewishCalendar).apply {
            if (jewishDayOfMonth == 30) forward(Calendar.DATE, 1)
        }
        val all = group.opinions.map { opinion ->
            val moment = when (opinion) {
                Opinion.Levana3Days -> month.tchilasZmanKidushLevana3Days
                Opinion.Levana7Days -> month.tchilasZmanKidushLevana7Days
                Opinion.Levana15Days -> month.sofZmanKidushLevana15Days
                else -> month.sofZmanKidushLevanaBetweenMoldos
            }
            opinion to if (opinion == chosen) time else round(moment, kind.rounding)
        }
        return Zman(kind, time, opinion = chosen, opinions = all)
    }

    /** The molad of the month after this one, announced on Shabbos Mevarchim. */
    private fun moladAfter(jewish: JewishCalendar, date: LocalDate): Molad {
        val firstOfNext = date.plusDays((jewish.daysInJewishMonth - jewish.jewishDayOfMonth + 1).toLong())
        val molad = JewishCalendar(firstOfNext).molad
        return Molad(
            month = firstOfNext,
            date = molad.localDate,
            hours = molad.moladHours,
            minutes = molad.moladMinutes,
            chalakim = molad.moladChalakim,
        )
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
