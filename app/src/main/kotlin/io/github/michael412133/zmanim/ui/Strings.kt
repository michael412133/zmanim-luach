package io.github.michael412133.zmanim.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.LayoutDirection
import com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import com.kosherjava.zmanim.hebrewcalendar.JewishDate
import io.github.michael412133.zmanim.AdarChoice
import io.github.michael412133.zmanim.CalendarMonth
import io.github.michael412133.zmanim.Event
import io.github.michael412133.zmanim.EventType
import io.github.michael412133.zmanim.Gps
import io.github.michael412133.zmanim.Group
import io.github.michael412133.zmanim.Language
import io.github.michael412133.zmanim.Molad
import io.github.michael412133.zmanim.MonthStyle
import io.github.michael412133.zmanim.Opinion
import io.github.michael412133.zmanim.Place
import io.github.michael412133.zmanim.Places
import io.github.michael412133.zmanim.Region
import io.github.michael412133.zmanim.Zman
import io.github.michael412133.zmanim.ZmanKind
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Every word the app shows, in English and in Hebrew. The screens ask for the words they need
 * here, so switching the language switches everything at once, the layout direction included.
 */
sealed class Strings {
    abstract val language: Language
    abstract val direction: LayoutDirection

    // The main screen
    abstract fun dayTitle(date: LocalDate, withYear: Boolean): String
    abstract fun clockPattern(is24Hour: Boolean): String
    abstract val moladClockPattern: String
    abstract fun weekdayShort(day: DayOfWeek): String
    abstract fun afterShkia(hebrewDate: String): String
    abstract val previousDay: String
    abstract val nextDay: String
    abstract val previousWeek: String
    abstract val nextWeek: String
    abstract val month: String
    abstract val today: String
    abstract val add: String
    abstract val changeLocation: String
    abstract fun zmanName(kind: ZmanKind): String
    protected abstract fun fixedNote(kind: ZmanKind): String
    abstract fun moladName(monthName: String): String
    abstract fun moladTime(day: DayOfWeek, clock: String, chalakim: Int): String
    abstract fun omerNote(day: Int): String
    /** The note under a Kiddush Levana line, which says whether it is the start or the end. */
    abstract fun levanaNote(opinion: Opinion): String
    /** The name of a line in the list of times to show, which covers every line it hides. */
    abstract fun toggleName(kind: ZmanKind): String
    abstract val nusachTitle: String
    abstract fun omerEnglish(day: Int): String?
    abstract val otherOpinions: String
    abstract val changeDefaultHint: String

    // Opinions
    abstract fun groupName(group: Group): String
    abstract fun groupNote(group: Group): String?
    abstract fun opinionName(opinion: Opinion): String
    abstract val omerWordingTitle: String

    // Events
    abstract fun eventTypeName(type: EventType): String
    abstract val addEvent: String
    abstract val editEvent: String
    abstract val eventName: String
    abstract val repeatsOn: String
    abstract val hebrewDate: String
    abstract val englishDate: String
    abstract val needsName: String
    abstract val afterShkiaHint: String
    abstract val deleteQuestion: String
    abstract fun everyYearOn(date: String): String
    abstract val adarTitle: String
    abstract val adarQuestion: String
    abstract fun adarName(choice: AdarChoice): String
    abstract val adarHint: String
    abstract val myEvents: String
    abstract val noEvents: String
    abstract fun eventCount(count: Int): String

    // Buttons
    abstract val done: String
    abstract val cancel: String
    abstract val save: String
    abstract val delete: String

    // Settings
    abstract val settings: String
    abstract val back: String
    abstract val general: String
    abstract val opinions: String
    abstract val more: String
    abstract val location: String
    abstract val languageTitle: String
    abstract val languageName: String
    abstract fun languageOption(language: Language): String
    abstract val monthView: String
    abstract fun monthStyleName(style: MonthStyle): String
    abstract val timesToShow: String
    abstract fun hiddenCount(count: Int): String
    abstract val howToUse: String
    abstract val howToUseNote: String
    abstract val about: String
    abstract val aboutNote: String

    // Towns and GPS
    abstract fun townName(place: Place): String
    abstract fun regionName(region: Region): String
    abstract val myLocation: String
    abstract fun myLocationNear(town: String): String
    abstract val gpsHint: String
    abstract val gpsSearching: String
    abstract fun gpsProgress(progress: Gps.Progress): String
    abstract fun gpsUpdated(date: LocalDate): String
    abstract val gpsOff: String
    abstract val gpsDenied: String
    abstract val gpsNoSignal: String
    abstract val gpsNeedsPrecise: String

    // The month view
    abstract val weekdays: List<String>
    abstract val previousMonth: String
    abstract val nextMonth: String
    abstract val previousYear: String
    abstract val nextYear: String
    abstract val chooseMonth: String
    abstract val previousPage: String
    abstract val nextPage: String
    protected abstract val gregorianMonths: List<String>

    // How to use, and about
    abstract val appName: String
    abstract val help: List<Pair<String, String>>
    abstract val aboutParagraphs: List<String>

    /** The words for a town from the list, or "My location, near ..." for the GPS spot. */
    fun placeName(place: Place): String {
        if (!place.isGps) return townName(place)
        val near = Places.nearest(place.latitude, place.longitude)
        return if (near != null) myLocationNear(townName(near)) else myLocation
    }

    /** The small line under a zman's name: the opinion it follows, or what it is. */
    fun note(zman: Zman): String = when {
        zman.kind == ZmanKind.Omer -> omerNote(zman.omerDay)
        zman.kind == ZmanKind.CandleLightingAfterTzeis -> fixedNote(zman.kind)
        (zman.kind == ZmanKind.KiddushLevanaStart || zman.kind == ZmanKind.KiddushLevanaEnd) && zman.opinion != null ->
            levanaNote(zman.opinion)
        zman.opinion != null -> opinionName(zman.opinion)
        else -> fixedNote(zman.kind)
    }

    /** The molad line's name, like "Molad Cheshvan". */
    fun moladTitle(molad: Molad): String = moladName(hebrewMonthName(molad.month))

    /** When the molad is, like "Sunday 9:43 AM and 2 chalakim", using the clock pattern given. */
    fun moladWhen(molad: Molad, clock: java.text.SimpleDateFormat): String {
        val calendar = java.util.Calendar.getInstance(clock.timeZone).apply {
            clear()
            set(molad.date.year, molad.date.monthValue - 1, molad.date.dayOfMonth, molad.hours, molad.minutes, 0)
        }
        return moladTime(molad.date.dayOfWeek, clock.format(calendar.time), molad.chalakim)
    }

    private val formatter: HebrewDateFormatter
        get() = HebrewDateFormatter().apply { setHebrewFormat(language == Language.Hebrew) }

    private val hebrewFormatter: HebrewDateFormatter
        get() = HebrewDateFormatter().apply { setHebrewFormat(true) }

    /** A number in Hebrew letters, like י״ג. */
    fun hebrewNumber(number: Int): String = hebrewFormatter.formatHebrewNumber(number)

    /** A day of a Hebrew month, like "כ״ו תשרי", always in Hebrew letters. */
    fun hebrewDayAndMonth(date: LocalDate): String {
        val jewish = JewishDate(date)
        return hebrewFormatter.formatHebrewNumber(jewish.jewishDayOfMonth) + " " + hebrewFormatter.formatMonth(jewish)
    }

    fun gregorianMonthName(date: LocalDate): String = gregorianMonths[date.monthValue - 1]

    fun hebrewMonthName(date: LocalDate): String = formatter.formatMonth(JewishCalendar(date))

    fun yearName(style: MonthStyle, year: Int): String = when {
        style == MonthStyle.English -> year.toString()
        language == Language.Hebrew -> formatter.formatHebrewNumber(year)
        else -> year.toString()
    }

    /** "October 2026", or "Tishrei 5787" for a Hebrew month. */
    fun monthTitle(month: CalendarMonth): String = when (month.style) {
        MonthStyle.English -> gregorianMonthName(month.firstDay) + " " + month.firstDay.year
        MonthStyle.Hebrew -> hebrewMonthName(month.firstDay) + " " +
            yearName(MonthStyle.Hebrew, JewishCalendar(month.firstDay).jewishYear)
    }

    /** The other calendar's months that the month runs across, like "Tishrei · Cheshvan 5787". */
    fun monthSubtitle(month: CalendarMonth): String {
        val first = month.firstDay
        val last = month.lastDay
        return when (month.style) {
            MonthStyle.English -> {
                val firstYear = JewishCalendar(first).jewishYear
                val lastYear = JewishCalendar(last).jewishYear
                val firstName = hebrewMonthName(first)
                val lastName = hebrewMonthName(last)
                when {
                    firstName == lastName -> firstName + " " + yearName(MonthStyle.Hebrew, firstYear)
                    firstYear == lastYear -> "$firstName · $lastName " + yearName(MonthStyle.Hebrew, lastYear)
                    else -> firstName + " " + yearName(MonthStyle.Hebrew, firstYear) + " · " +
                        lastName + " " + yearName(MonthStyle.Hebrew, lastYear)
                }
            }
            MonthStyle.Hebrew -> {
                val firstName = gregorianMonthName(first)
                val lastName = gregorianMonthName(last)
                when {
                    first.month == last.month -> "$firstName ${first.year}"
                    first.year == last.year -> "$firstName · $lastName ${last.year}"
                    else -> "$firstName ${first.year} · $lastName ${last.year}"
                }
            }
        }
    }

    /** The name on a month's button in the month chooser. */
    fun monthButton(month: CalendarMonth): String = when (month.style) {
        MonthStyle.English -> gregorianMonthName(month.firstDay)
        MonthStyle.Hebrew -> hebrewMonthName(month.firstDay)
    }

    /** One of the reader's events, the way the day shows it: "Yahrzeit: Moshe ben Avraham". */
    fun eventLine(event: Event): String =
        if (event.type == EventType.Other) event.name else eventTypeName(event.type) + ": " + event.name

    /** When an event comes back, like "Every year on כ״ו תשרי". */
    fun eventRepeats(event: Event): String {
        val date = if (event.hebrew) hebrewDayAndMonth(event.date) else englishDayAndMonth(event.date)
        val adar = event.adar
        return if (event.hebrew && event.isInAdar && adar != null) {
            everyYearOn(date) + " · " + adarName(adar)
        } else {
            everyYearOn(date)
        }
    }

    abstract fun englishDayAndMonth(date: LocalDate): String

    companion object {
        fun of(language: Language): Strings = when (language) {
            Language.English -> English
            Language.Hebrew -> Hebrew
        }
    }

    data object English : Strings() {
        override val language = Language.English
        override val direction = LayoutDirection.Ltr

        private val days = mapOf(
            DayOfWeek.SUNDAY to "Sunday",
            DayOfWeek.MONDAY to "Monday",
            DayOfWeek.TUESDAY to "Tuesday",
            DayOfWeek.WEDNESDAY to "Wednesday",
            DayOfWeek.THURSDAY to "Thursday",
            DayOfWeek.FRIDAY to "Friday",
            DayOfWeek.SATURDAY to "Shabbos",
        )

        private val shortMonths = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

        override fun dayTitle(date: LocalDate, withYear: Boolean): String =
            if (withYear) {
                weekdayShort(date.dayOfWeek) + ", " + shortMonths[date.monthValue - 1] + " " + date.dayOfMonth + ", " + date.year
            } else {
                days.getValue(date.dayOfWeek) + ", " + shortMonths[date.monthValue - 1] + " " + date.dayOfMonth
            }

        override fun englishDayAndMonth(date: LocalDate) = gregorianMonthName(date) + " " + date.dayOfMonth

        override fun clockPattern(is24Hour: Boolean) = if (is24Hour) "H:mm" else "h:mm a"
        override val moladClockPattern = "h:mm a"
        override fun weekdayShort(day: DayOfWeek) = when (day) {
            DayOfWeek.SUNDAY -> "Sun"
            DayOfWeek.MONDAY -> "Mon"
            DayOfWeek.TUESDAY -> "Tue"
            DayOfWeek.WEDNESDAY -> "Wed"
            DayOfWeek.THURSDAY -> "Thu"
            DayOfWeek.FRIDAY -> "Fri"
            DayOfWeek.SATURDAY -> "Shabbos"
        }
        override fun afterShkia(hebrewDate: String) = "After shkia: $hebrewDate"
        override val previousDay = "Previous day"
        override val nextDay = "Next day"
        override val previousWeek = "Previous week"
        override val nextWeek = "Next week"
        override val month = "Month"
        override val today = "Today"
        override val add = "Add"
        override val changeLocation = "Change the location"

        override fun zmanName(kind: ZmanKind) = when (kind) {
            ZmanKind.Molad -> "Molad"
            ZmanKind.KiddushLevanaStart, ZmanKind.KiddushLevanaEnd -> "Kiddush Levana"
            ZmanKind.FastBegins, ZmanKind.FastBeginsAtShkia -> "Fast begins"
            ZmanKind.Alos -> "Alos HaShachar"
            ZmanKind.Misheyakir -> "Misheyakir"
            ZmanKind.Netz -> "Netz HaChama"
            ZmanKind.SofZmanShema -> "Sof Zman Shema"
            ZmanKind.SofZmanTefillah -> "Sof Zman Tefillah"
            ZmanKind.AchilasChametz -> "Eat chametz until"
            ZmanKind.BiurChametz -> "Burn chametz by"
            ZmanKind.Chatzos -> "Chatzos"
            ZmanKind.MinchaGedola -> "Mincha Gedola"
            ZmanKind.MinchaKetana -> "Mincha Ketana"
            ZmanKind.PlagHamincha -> "Plag HaMincha"
            ZmanKind.CandleLighting, ZmanKind.CandleLightingAfterTzeis -> "Hadlakas Neiros"
            ZmanKind.Shkia -> "Shkias HaChama"
            ZmanKind.Tzeis -> "Tzeis HaKochavim"
            ZmanKind.Omer -> "Sefiras HaOmer"
            ZmanKind.FastEnds -> "Fast ends"
            ZmanKind.ShabbosEnds -> "Shabbos ends"
            ZmanKind.YomTovEnds -> "Yom Tov ends"
            ZmanKind.ShabbosAndYomTovEnd -> "Shabbos & YT end"
            ZmanKind.YomKippurEnds -> "Yom Kippur ends"
            ZmanKind.RabbeinuTam -> "Tzeis Rabbeinu Tam"
            ZmanKind.ChatzosHalaila -> "Chatzos HaLaila"
        }

        override fun fixedNote(kind: ZmanKind) = when (kind) {
            ZmanKind.Netz -> "Sunrise"
            ZmanKind.Chatzos -> "Midday"
            ZmanKind.Shkia -> "Sunset"
            ZmanKind.FastBeginsAtShkia -> "At shkia"
            ZmanKind.CandleLightingAfterTzeis -> "After this, from a lit flame"
            ZmanKind.RabbeinuTam -> "72 minutes after shkia"
            ZmanKind.ChatzosHalaila -> "Midnight"
            else -> ""
        }

        override fun moladName(monthName: String) = "Molad $monthName"
        override fun moladTime(day: DayOfWeek, clock: String, chalakim: Int) =
            days.getValue(day) + " " + clock + if (chalakim == 1) " and 1 chelek" else " and $chalakim chalakim"
        override fun omerNote(day: Int) = "Day $day, tap for the nusach"
        override fun levanaNote(opinion: Opinion) = when (opinion) {
            Opinion.Levana3Days -> "From 3 days after the molad"
            Opinion.Levana7Days -> "From 7 days after the molad"
            Opinion.Levana15Days -> "Until 15 days after the molad"
            else -> "Until halfway to next molad"
        }
        override fun toggleName(kind: ZmanKind) = when (kind) {
            ZmanKind.SofZmanShema -> "Sof Zman Krias Shema"
            ZmanKind.ShabbosEnds -> "Shabbos and Yom Tov end"
            ZmanKind.FastBegins -> "Fasts begin and end"
            ZmanKind.AchilasChametz -> "Chametz times on Erev Pesach"
            else -> zmanName(kind)
        }
        override val nusachTitle = "Sefiras HaOmer"
        override fun omerEnglish(day: Int): String {
            val weeks = day / 7
            val rest = day % 7
            val total = if (day == 1) "1 day" else "$day days"
            if (weeks == 0) return "Tonight is $total of the omer."
            val weekText = if (weeks == 1) "1 week" else "$weeks weeks"
            val restText = when (rest) {
                0 -> ""
                1 -> " and 1 day"
                else -> " and $rest days"
            }
            return "Tonight is $total, which is $weekText$restText of the omer."
        }
        override val otherOpinions = "Every opinion"
        override val changeDefaultHint = "To change the one shown, open Settings, then Opinions."

        override fun groupName(group: Group) = when (group) {
            Group.Alos -> "Alos HaShachar"
            Group.Misheyakir -> "Misheyakir"
            Group.ShemaTefillah -> "Krias Shema and Tefillah"
            Group.Afternoon -> "Mincha and Plag"
            Group.Tzeis -> "Tzeis HaKochavim"
            Group.Candles -> "Candle lighting"
            Group.ShabbosEnds -> "Shabbos and Yom Tov end"
            Group.FastEnds -> "Fast ends"
            Group.LevanaStart -> "Kiddush Levana begins"
            Group.LevanaEnd -> "Kiddush Levana ends"
        }

        override fun groupNote(group: Group) = when (group) {
            Group.Alos -> "Also when a fast day begins."
            Group.ShemaTefillah -> "Also used for the chametz times on Erev Pesach."
            Group.Afternoon -> "KosherJava warns the Magen Avraham's plag can be very late in summer, so use it only l'chumra."
            Group.Tzeis -> "Also the time shown for sefiras haomer."
            Group.Candles -> "In Yerushalayim many light 40 minutes before shkia."
            Group.ShabbosEnds -> "Also the earliest candle lighting for a second night of Yom Tov."
            else -> null
        }

        override fun opinionName(opinion: Opinion) = when (opinion) {
            Opinion.Alos72 -> "72 minutes before netz"
            Opinion.Alos90 -> "90 minutes before netz"
            Opinion.Alos16Point1 -> "16.1° below the horizon"
            Opinion.Alos19Point8 -> "19.8° below the horizon"
            Opinion.Misheyakir11Point5 -> "11.5° below the horizon"
            Opinion.Misheyakir11 -> "11° below the horizon"
            Opinion.Misheyakir10Point2 -> "10.2° below the horizon"
            Opinion.Mga72 -> "Magen Avraham, 72 minutes"
            Opinion.Mga16Point1 -> "Magen Avraham, 16.1°"
            Opinion.Gra -> "Gra"
            Opinion.BaalHatanya -> "Baal HaTanya"
            Opinion.AfternoonGra -> "Gra"
            Opinion.AfternoonMga -> "Magen Avraham, 72 minutes"
            Opinion.AfternoonBaalHatanya -> "Baal HaTanya"
            Opinion.Tzeis8Point5, Opinion.FastEnds8Point5, Opinion.Ends8Point5 -> "8.5° below the horizon"
            Opinion.Tzeis7Point083, Opinion.FastEnds7Point083 -> "7.083° below the horizon"
            Opinion.Tzeis50, Opinion.FastEnds50, Opinion.Ends50 -> "50 minutes after shkia"
            Opinion.Ends60 -> "60 minutes after shkia"
            Opinion.Ends72 -> "72 minutes, Rabbeinu Tam"
            Opinion.Candles18, Opinion.Candles20, Opinion.Candles22, Opinion.Candles30, Opinion.Candles40 ->
                "${opinion.candleMinutes} minutes before shkia"
            Opinion.Levana3Days -> "3 days after the molad"
            Opinion.Levana7Days -> "7 days after the molad"
            Opinion.LevanaBetweenMoldos -> "Halfway to the next molad"
            Opinion.Levana15Days -> "15 days after the molad"
        }

        override val omerWordingTitle = "Sefiras HaOmer"

        override fun eventTypeName(type: EventType) = when (type) {
            EventType.Yahrzeit -> "Yahrzeit"
            EventType.Birthday -> "Birthday"
            EventType.Anniversary -> "Anniversary"
            EventType.Other -> "Other"
        }
        override val addEvent = "Add an event"
        override val editEvent = "Edit the event"
        override val eventName = "Name"
        override val repeatsOn = "Comes back every year on the"
        override val hebrewDate = "Hebrew date"
        override val englishDate = "English date"
        override val needsName = "Type a name first"
        override val afterShkiaHint = "If it was after shkia, pick the next day."
        override val deleteQuestion = "Delete this event?"
        override fun everyYearOn(date: String) = "Every year on $date"
        override val adarTitle = "Which Adar?"
        override val adarQuestion = "This date is in Adar. In a year with two months of Adar, when should it show?"
        override fun adarName(choice: AdarChoice) = when (choice) {
            AdarChoice.AdarI -> "Adar I"
            AdarChoice.AdarII -> "Adar II"
            AdarChoice.Both -> "Both"
        }
        override val adarHint = "Many keep a yahrzeit in Adar I, some in both, and a birthday in Adar II. Ask your rav if unsure."
        override val myEvents = "My events"
        override val noEvents = "No events yet. To add one, pick its day on the main screen and tap Add."
        override fun eventCount(count: Int) = when (count) {
            0 -> "None yet"
            1 -> "1 event"
            else -> "$count events"
        }

        override val done = "Done"
        override val cancel = "Cancel"
        override val save = "Save"
        override val delete = "Delete"

        override val settings = "Settings"
        override val back = "Back"
        override val general = "General"
        override val opinions = "Opinions"
        override val more = "More"
        override val location = "Location"
        override val languageTitle = "Language"
        override val languageName = "English"
        override fun languageOption(language: Language) = when (language) {
            Language.English -> "English"
            Language.Hebrew -> "עברית"
        }
        override val monthView = "Month view"
        override fun monthStyleName(style: MonthStyle) = when (style) {
            MonthStyle.English -> "English months (October)"
            MonthStyle.Hebrew -> "Hebrew months (Tishrei)"
        }
        override val timesToShow = "Times to show"
        override fun hiddenCount(count: Int) = if (count == 0) "All of them" else "$count hidden"
        override val howToUse = "How to use"
        override val howToUseNote = "The buttons, the swipes and the settings"
        override val about = "About these times"
        override val aboutNote = "How they are worked out and rounded"

        override fun townName(place: Place) = place.name
        override fun regionName(region: Region) = when (region) {
            Region.Rockland -> "Rockland and Orange"
            Region.NewYork -> "New York and New Jersey"
            Region.America -> "US and Canada"
            Region.Israel -> "Eretz Yisroel"
            Region.Europe -> "Europe"
        }
        override val myLocation = "My location"
        override fun myLocationNear(town: String) = "My location, near $town"
        override val gpsHint = "Uses the phone's GPS"
        override val gpsSearching = "Looking for satellites, this can take a few minutes"
        override fun gpsProgress(progress: Gps.Progress) =
            "Hearing ${progress.heard} satellites, ${progress.used} in use · " +
                "${progress.seconds / 60}:${(progress.seconds % 60).toString().padStart(2, '0')}"
        override fun gpsUpdated(date: LocalDate) =
            "Updated ${gregorianMonthName(date)} ${date.dayOfMonth}, tap to update"
        override val gpsOff = "Location is off, tap to turn it on in settings"
        override val gpsDenied = "Needs location permission, tap to allow it"
        override val gpsNoSignal = "No GPS fix after 5 minutes. Try outside, then tap again"
        override val gpsNeedsPrecise = "Only approximate location is allowed. Tap to allow precise location"

        override val weekdays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Shab")
        override val previousMonth = "Previous month"
        override val nextMonth = "Next month"
        override val previousYear = "Previous year"
        override val nextYear = "Next year"
        override val chooseMonth = "Choose a month"
        override val previousPage = "Previous page"
        override val nextPage = "Next page"
        override val gregorianMonths = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December",
        )

        override val appName = "Zmanim & Luach"
        override val help = listOf(
            "The month and the day" to
                "The month is on top, and the day you tap is below it. Grey squares are Yom Tov, " +
                "Rosh Chodesh and fasts, and a small dot is one of your own events. Tap the month's " +
                "name to jump to any month.",
            "Swiping" to
                "Swipe up to fold the month into one week and see more of the day's times, and keep " +
                "swiping up for the next page. Swipe down to go back. Swipe left or right on the times " +
                "to change the day, or on the month to change the month.",
            "The buttons at the bottom" to
                "Month opens and closes the month. Today goes back to today. Add saves a yahrzeit, a " +
                "birthday or anything else on the day you picked. The gear opens the settings.",
            "The times" to
                "A star means there are other opinions: tap the line to see all of them. Grey lines " +
                "are only for that day, like candle lighting, the end of Shabbos, fasts, the chametz " +
                "times, Kiddush Levana, the molad and sefiras haomer. Tap the omer line for the nusach.",
            "Opinions and times to show" to
                "In the settings, under Opinions, choose which opinion each time follows. Times to show " +
                "hides the lines you don't need.",
            "Location" to
                "Tap the town under the date to change it. My location uses the phone's GPS; the first " +
                "time, stand outside or by a window, and give it a few minutes.",
            "Your events" to
                "Pick the day, tap Add, and choose whether it comes back on the Hebrew date or the " +
                "English date. For a date in Adar it asks which Adar to use in a year that has two. " +
                "To change or delete an event, tap it under the date, or open My events in the settings.",
        )
        override val aboutParagraphs = listOf(
            "Every time is worked out on this phone with the KosherJava zmanim library. " +
                "Nothing is sent anywhere, and no internet is needed.",
            "Netz and shkia are at sea level, the way most luchos in America print them.",
            "Times are rounded to the safe side. A deadline, like sof zman krias shema, is shown " +
                "a minute earlier, and a starting time, like tzeis, a minute later.",
            "The times were checked against Hebcal for every opinion the settings offer.",
            "My location uses the phone's own GPS, and the spot stays on the phone.",
            "For halacha l'maaseh, follow your rav and your shul's luach.",
            "github.com/michael412133/zmanim-luach",
            "Made with KosherJava Zmanim (LGPL 2.1) and Mudita Mindful Design (Apache 2.0).",
        )
    }

    data object Hebrew : Strings() {
        override val language = Language.Hebrew
        override val direction = LayoutDirection.Rtl

        private val days = mapOf(
            DayOfWeek.SUNDAY to "יום ראשון",
            DayOfWeek.MONDAY to "יום שני",
            DayOfWeek.TUESDAY to "יום שלישי",
            DayOfWeek.WEDNESDAY to "יום רביעי",
            DayOfWeek.THURSDAY to "יום חמישי",
            DayOfWeek.FRIDAY to "יום שישי",
            DayOfWeek.SATURDAY to "שבת קודש",
        )

        // With the year it is written the short way, 30.9.2027, so it still fits beside the Hebrew date.
        override fun dayTitle(date: LocalDate, withYear: Boolean): String =
            if (withYear) {
                val day = if (date.dayOfWeek == DayOfWeek.SATURDAY) "שבת" else "יום " + weekdayShort(date.dayOfWeek)
                day + ", " + date.dayOfMonth + "." + date.monthValue + "." + date.year
            } else {
                days.getValue(date.dayOfWeek) + ", " + date.dayOfMonth + " ב" + gregorianMonthName(date)
            }

        override fun englishDayAndMonth(date: LocalDate) = date.dayOfMonth.toString() + " ב" + gregorianMonthName(date)

        // Hebrew luchos print the time without AM or PM. The molad has no other clue to the hour, so it uses 24 hours.
        override fun clockPattern(is24Hour: Boolean) = if (is24Hour) "H:mm" else "h:mm"
        override val moladClockPattern = "H:mm"
        override fun weekdayShort(day: DayOfWeek) = when (day) {
            DayOfWeek.SUNDAY -> "א׳"
            DayOfWeek.MONDAY -> "ב׳"
            DayOfWeek.TUESDAY -> "ג׳"
            DayOfWeek.WEDNESDAY -> "ד׳"
            DayOfWeek.THURSDAY -> "ה׳"
            DayOfWeek.FRIDAY -> "ו׳"
            DayOfWeek.SATURDAY -> "שבת"
        }
        override fun afterShkia(hebrewDate: String) = "אחרי השקיעה: $hebrewDate"
        override val previousDay = "היום הקודם"
        override val nextDay = "היום הבא"
        override val previousWeek = "השבוע הקודם"
        override val nextWeek = "השבוע הבא"
        override val month = "חודש"
        override val today = "היום"
        override val add = "הוספה"
        override val changeLocation = "החלפת מיקום"

        override fun zmanName(kind: ZmanKind) = when (kind) {
            ZmanKind.Molad -> "מולד"
            ZmanKind.KiddushLevanaStart -> "תחילת זמן קידוש לבנה"
            ZmanKind.KiddushLevanaEnd -> "סוף זמן קידוש לבנה"
            ZmanKind.FastBegins, ZmanKind.FastBeginsAtShkia -> "תחילת הצום"
            ZmanKind.Alos -> "עלות השחר"
            ZmanKind.Misheyakir -> "משיכיר"
            ZmanKind.Netz -> "הנץ החמה"
            ZmanKind.SofZmanShema -> "סוף זמן קריאת שמע"
            ZmanKind.SofZmanTefillah -> "סוף זמן תפילה"
            ZmanKind.AchilasChametz -> "סוף זמן אכילת חמץ"
            ZmanKind.BiurChametz -> "סוף זמן ביעור חמץ"
            ZmanKind.Chatzos -> "חצות היום"
            ZmanKind.MinchaGedola -> "מנחה גדולה"
            ZmanKind.MinchaKetana -> "מנחה קטנה"
            ZmanKind.PlagHamincha -> "פלג המנחה"
            ZmanKind.CandleLighting, ZmanKind.CandleLightingAfterTzeis -> "הדלקת נרות"
            ZmanKind.Shkia -> "שקיעת החמה"
            ZmanKind.Tzeis -> "צאת הכוכבים"
            ZmanKind.Omer -> "ספירת העומר"
            ZmanKind.FastEnds -> "סוף הצום"
            ZmanKind.ShabbosEnds -> "מוצאי שבת"
            ZmanKind.YomTovEnds -> "מוצאי יום טוב"
            ZmanKind.ShabbosAndYomTovEnd -> "מוצאי שבת ויום טוב"
            ZmanKind.YomKippurEnds -> "מוצאי יום הכיפורים"
            ZmanKind.RabbeinuTam -> "רבינו תם"
            ZmanKind.ChatzosHalaila -> "חצות הלילה"
        }

        override fun fixedNote(kind: ZmanKind) = when (kind) {
            ZmanKind.Netz -> "זריחה"
            ZmanKind.Chatzos -> "אמצע היום"
            ZmanKind.Shkia -> "שקיעה"
            ZmanKind.FastBeginsAtShkia -> "בשקיעה"
            ZmanKind.CandleLightingAfterTzeis -> "לא לפני זמן זה, מאש קיימת"
            ZmanKind.RabbeinuTam -> "72 דקות אחרי השקיעה"
            ZmanKind.ChatzosHalaila -> "אמצע הלילה"
            else -> ""
        }

        override fun moladName(monthName: String) = "מולד $monthName"
        override fun moladTime(day: DayOfWeek, clock: String, chalakim: Int) =
            days.getValue(day) + " " + clock + if (chalakim == 1) " וחלק אחד" else " ו־$chalakim חלקים"
        override fun omerNote(day: Int) = "הלילה יום ${hebrewNumber(day)}, לחצו לנוסח"
        override fun levanaNote(opinion: Opinion) = opinionName(opinion)
        override fun toggleName(kind: ZmanKind) = when (kind) {
            ZmanKind.ShabbosEnds -> "מוצאי שבת ויום טוב"
            ZmanKind.FastBegins -> "תחילת וסוף התעניות"
            ZmanKind.AchilasChametz -> "זמני חמץ בערב פסח"
            ZmanKind.KiddushLevanaStart -> "קידוש לבנה"
            else -> zmanName(kind)
        }
        override val nusachTitle = "ספירת העומר"
        override fun omerEnglish(day: Int): String? = null
        override val otherOpinions = "כל השיטות"
        override val changeDefaultHint = "כדי לשנות את השיטה המוצגת, פתחו את ההגדרות ואז שיטות."

        override fun groupName(group: Group) = when (group) {
            Group.Alos -> "עלות השחר"
            Group.Misheyakir -> "משיכיר"
            Group.ShemaTefillah -> "קריאת שמע ותפילה"
            Group.Afternoon -> "מנחה ופלג המנחה"
            Group.Tzeis -> "צאת הכוכבים"
            Group.Candles -> "הדלקת נרות"
            Group.ShabbosEnds -> "מוצאי שבת ויום טוב"
            Group.FastEnds -> "סוף הצום"
            Group.LevanaStart -> "תחילת זמן קידוש לבנה"
            Group.LevanaEnd -> "סוף זמן קידוש לבנה"
        }

        override fun groupNote(group: Group) = when (group) {
            Group.Alos -> "וגם תחילת התענית."
            Group.ShemaTefillah -> "וגם לזמני החמץ בערב פסח."
            Group.Afternoon -> "KosherJava מזהירה שפלג המנחה של המגן אברהם מאוחר מאוד בקיץ, ולכן רק לחומרא."
            Group.Tzeis -> "וגם הזמן שמוצג לספירת העומר."
            Group.Candles -> "בירושלים רבים מדליקים 40 דקות לפני השקיעה."
            Group.ShabbosEnds -> "וגם הזמן להדלקת נרות בליל יום טוב שני."
            else -> null
        }

        override fun opinionName(opinion: Opinion) = when (opinion) {
            Opinion.Alos72 -> "72 דקות לפני הנץ"
            Opinion.Alos90 -> "90 דקות לפני הנץ"
            Opinion.Alos16Point1 -> "16.1° מתחת לאופק"
            Opinion.Alos19Point8 -> "19.8° מתחת לאופק"
            Opinion.Misheyakir11Point5 -> "11.5° מתחת לאופק"
            Opinion.Misheyakir11 -> "11° מתחת לאופק"
            Opinion.Misheyakir10Point2 -> "10.2° מתחת לאופק"
            Opinion.Mga72 -> "מגן אברהם, 72 דקות"
            Opinion.Mga16Point1 -> "מגן אברהם, 16.1°"
            Opinion.Gra -> "הגר״א"
            Opinion.BaalHatanya -> "בעל התניא"
            Opinion.AfternoonGra -> "הגר״א"
            Opinion.AfternoonMga -> "מגן אברהם, 72 דקות"
            Opinion.AfternoonBaalHatanya -> "בעל התניא"
            Opinion.Tzeis8Point5, Opinion.FastEnds8Point5, Opinion.Ends8Point5 -> "8.5° מתחת לאופק"
            Opinion.Tzeis7Point083, Opinion.FastEnds7Point083 -> "7.083° מתחת לאופק"
            Opinion.Tzeis50, Opinion.FastEnds50, Opinion.Ends50 -> "50 דקות אחרי השקיעה"
            Opinion.Ends60 -> "60 דקות אחרי השקיעה"
            Opinion.Ends72 -> "72 דקות, רבינו תם"
            Opinion.Candles18, Opinion.Candles20, Opinion.Candles22, Opinion.Candles30, Opinion.Candles40 ->
                "${opinion.candleMinutes} דקות לפני השקיעה"
            Opinion.Levana3Days -> "3 ימים אחרי המולד"
            Opinion.Levana7Days -> "7 ימים אחרי המולד"
            Opinion.LevanaBetweenMoldos -> "חצי הזמן עד המולד הבא"
            Opinion.Levana15Days -> "15 ימים אחרי המולד"
        }

        override val omerWordingTitle = "ספירת העומר"

        override fun eventTypeName(type: EventType) = when (type) {
            EventType.Yahrzeit -> "יארצייט"
            EventType.Birthday -> "יום הולדת"
            EventType.Anniversary -> "יום נישואין"
            EventType.Other -> "אחר"
        }
        override val addEvent = "הוספת אירוע"
        override val editEvent = "עריכת האירוע"
        override val eventName = "שם"
        override val repeatsOn = "חוזר כל שנה לפי"
        override val hebrewDate = "התאריך העברי"
        override val englishDate = "התאריך הלועזי"
        override val needsName = "קודם כתבו שם"
        override val afterShkiaHint = "אם זה היה אחרי השקיעה, בחרו את היום הבא."
        override val deleteQuestion = "למחוק את האירוע?"
        override fun everyYearOn(date: String) = "כל שנה ב־$date"
        override val adarTitle = "איזה אדר?"
        override val adarQuestion = "התאריך הזה באדר. בשנה מעוברת, עם שני חודשי אדר, מתי להציג אותו?"
        override fun adarName(choice: AdarChoice) = when (choice) {
            AdarChoice.AdarI -> "אדר א׳"
            AdarChoice.AdarII -> "אדר ב׳"
            AdarChoice.Both -> "בשניהם"
        }
        override val adarHint = "רבים נוהגים יארצייט באדר א׳, יש שבשניהם, ויום הולדת באדר ב׳. בספק, שאלו את הרב."
        override val myEvents = "האירועים שלי"
        override val noEvents = "אין עדיין אירועים. כדי להוסיף, בחרו את היום במסך הראשי ולחצו הוספה."
        override fun eventCount(count: Int) = when (count) {
            0 -> "אין עדיין"
            1 -> "אירוע אחד"
            else -> "$count אירועים"
        }

        override val done = "סיום"
        override val cancel = "ביטול"
        override val save = "שמירה"
        override val delete = "מחיקה"

        override val settings = "הגדרות"
        override val back = "חזרה"
        override val general = "כללי"
        override val opinions = "שיטות"
        override val more = "עוד"
        override val location = "מיקום"
        override val languageTitle = "שפה"
        override val languageName = "עברית"
        override fun languageOption(language: Language) = when (language) {
            Language.English -> "English"
            Language.Hebrew -> "עברית"
        }
        override val monthView = "תצוגת חודש"
        override fun monthStyleName(style: MonthStyle) = when (style) {
            MonthStyle.English -> "חודשים לועזיים (אוקטובר)"
            MonthStyle.Hebrew -> "חודשים עבריים (תשרי)"
        }
        override val timesToShow = "זמנים להצגה"
        override fun hiddenCount(count: Int) = if (count == 0) "כולם" else "$count מוסתרים"
        override val howToUse = "איך משתמשים"
        override val howToUseNote = "הכפתורים, ההחלקות וההגדרות"
        override val about = "על הזמנים"
        override val aboutNote = "איך הם מחושבים ומעוגלים"

        private val towns = mapOf(
            "airmont" to "איירמונט",
            "monsey" to "מאנסי",
            "new-hempstead" to "ניו המפסטד",
            "new-square" to "ניו סקווער",
            "pomona" to "פומונה",
            "spring-valley" to "ספרינג וואלי",
            "suffern" to "סאפרן",
            "wesley-hills" to "וועסלי הילס",
            "kiryas-joel" to "קרית יואל",
            "monroe" to "מונרו",
            "boro-park" to "בורו פארק",
            "crown-heights" to "קראון הייטס",
            "flatbush" to "פלעטבוש",
            "williamsburg" to "וויליאמסבורג",
            "queens" to "קווינס",
            "far-rockaway" to "פאר ראקאוועי",
            "five-towns" to "פייוו טאונס",
            "lakewood" to "לייקווד",
            "passaic" to "פסייק",
            "teaneck" to "טינעק",
            "baltimore" to "בולטימור",
            "chicago" to "שיקגו",
            "cleveland" to "קליבלנד",
            "los-angeles" to "לוס אנג׳לס",
            "miami-beach" to "מיאמי ביץ׳",
            "toronto" to "טורונטו",
            "montreal" to "מונטריאול",
            "yerushalayim" to "ירושלים",
            "bnei-brak" to "בני ברק",
            "beit-shemesh" to "בית שמש",
            "modiin-illit" to "מודיעין עילית",
            "beitar-illit" to "ביתר עילית",
            "elad" to "אלעד",
            "tzfas" to "צפת",
            "haifa" to "חיפה",
            "tel-aviv" to "תל אביב",
            "netanya" to "נתניה",
            "ashdod" to "אשדוד",
            "petach-tikva" to "פתח תקווה",
            "london" to "לונדון",
            "manchester" to "מנצ׳סטר",
            "antwerp" to "אנטווערפן",
        )

        override fun townName(place: Place) = towns[place.id] ?: place.name
        override fun regionName(region: Region) = when (region) {
            Region.Rockland -> "רוקלנד ואורנג׳"
            Region.NewYork -> "ניו יורק וניו ג׳רזי"
            Region.America -> "ארצות הברית וקנדה"
            Region.Israel -> "ארץ ישראל"
            Region.Europe -> "אירופה"
        }
        override val myLocation = "המיקום שלי"
        override fun myLocationNear(town: String) = "המיקום שלי, ליד $town"
        override val gpsHint = "לפי ה־GPS של הטלפון"
        override val gpsSearching = "מחפש לוויינים, זה יכול לקחת כמה דקות"
        override fun gpsProgress(progress: Gps.Progress) =
            "${progress.heard} לוויינים נקלטו, ${progress.used} בשימוש · " +
                "${progress.seconds / 60}:${(progress.seconds % 60).toString().padStart(2, '0')}"
        override fun gpsUpdated(date: LocalDate) =
            "עודכן ב־${date.dayOfMonth} ב${gregorianMonthName(date)}, לחצו לעדכון"
        override val gpsOff = "המיקום כבוי, לחצו כדי להדליק אותו בהגדרות"
        override val gpsDenied = "צריך הרשאת מיקום, לחצו כדי לאשר"
        override val gpsNoSignal = "אין קליטת GPS אחרי 5 דקות. נסו בחוץ ולחצו שוב"
        override val gpsNeedsPrecise = "מותר רק מיקום משוער. לחצו כדי לאשר מיקום מדויק"

        override val weekdays = listOf("א׳", "ב׳", "ג׳", "ד׳", "ה׳", "ו׳", "שבת")
        override val previousMonth = "החודש הקודם"
        override val nextMonth = "החודש הבא"
        override val previousYear = "השנה הקודמת"
        override val nextYear = "השנה הבאה"
        override val chooseMonth = "בחירת חודש"
        override val previousPage = "העמוד הקודם"
        override val nextPage = "העמוד הבא"
        override val gregorianMonths = listOf(
            "ינואר", "פברואר", "מרץ", "אפריל", "מאי", "יוני",
            "יולי", "אוגוסט", "ספטמבר", "אוקטובר", "נובמבר", "דצמבר",
        )

        override val appName = "זמנים ולוח"
        override val help = listOf(
            "החודש והיום" to
                "החודש למעלה, והיום שלוחצים עליו מוצג מתחתיו. ריבועים אפורים הם יום טוב, ראש חודש " +
                "ותעניות, ונקודה קטנה היא אירוע שלכם. לחיצה על שם החודש פותחת בחירה של כל חודש.",
            "החלקות" to
                "החלקה למעלה מקפלת את החודש לשבוע אחד ומראה עוד זמנים, והחלקה נוספת למעלה עוברת לעמוד " +
                "הבא. החלקה למטה מחזירה. החלקה הצידה על הזמנים מחליפה יום, ועל החודש מחליפה חודש.",
            "הכפתורים למטה" to
                "חודש פותח וסוגר את החודש. היום חוזר להיום. הוספה שומרת יארצייט, יום הולדת או כל " +
                "דבר אחר ביום שבחרתם. גלגל השיניים פותח את ההגדרות.",
            "הזמנים" to
                "כוכבית אומרת שיש עוד שיטות: לחיצה על השורה מראה את כולן. שורות אפורות הן רק לאותו " +
                "יום, כמו הדלקת נרות, מוצאי שבת, תעניות, זמני חמץ, קידוש לבנה, המולד וספירת העומר. " +
                "לחיצה על שורת העומר מראה את הנוסח.",
            "שיטות וזמנים להצגה" to
                "בהגדרות, תחת שיטות, בוחרים לפי איזו שיטה כל זמן מוצג. בזמנים להצגה אפשר להסתיר " +
                "שורות שלא צריך.",
            "מיקום" to
                "לחיצה על העיר שמתחת לתאריך מחליפה אותה. המיקום שלי משתמש ב־GPS של הטלפון; בפעם " +
                "הראשונה עמדו בחוץ או ליד חלון, ותנו לו כמה דקות.",
            "האירועים שלכם" to
                "בוחרים את היום, לוחצים הוספה, ובוחרים אם הוא חוזר לפי התאריך העברי או הלועזי. לתאריך " +
                "באדר הוא שואל איזה אדר לקחת בשנה מעוברת. כדי לשנות או למחוק, לוחצים על האירוע " +
                "שמתחת לתאריך, או פותחים את האירועים שלי בהגדרות.",
        )
        override val aboutParagraphs = listOf(
            "כל הזמנים מחושבים בטלפון עצמו בעזרת ספריית הזמנים KosherJava. " +
                "שום דבר לא נשלח לשום מקום, ואין צורך באינטרנט.",
            "הנץ והשקיעה מחושבים לגובה פני הים, כמו ברוב הלוחות באמריקה.",
            "הזמנים מעוגלים לחומרא: זמן אחרון, כמו סוף זמן קריאת שמע, מוקדם בדקה, " +
                "וזמן התחלה, כמו צאת הכוכבים, מאוחר בדקה.",
            "הזמנים נבדקו מול Hebcal לכל שיטה שבהגדרות.",
            "״המיקום שלי״ משתמש ב־GPS של הטלפון עצמו, והמיקום נשאר בטלפון.",
            "להלכה למעשה, יש לנהוג לפי הוראת הרב ולוח בית הכנסת.",
            "github.com/michael412133/zmanim-luach",
            "נבנה עם KosherJava Zmanim (LGPL 2.1) ועם Mudita Mindful Design (Apache 2.0).",
        )
    }
}

/** The words for the language chosen in the settings. */
val LocalStrings = staticCompositionLocalOf<Strings> { Strings.English }
