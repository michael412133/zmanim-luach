package io.github.michael412133.zmanim.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.LayoutDirection
import com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import io.github.michael412133.zmanim.CalendarMonth
import io.github.michael412133.zmanim.Language
import io.github.michael412133.zmanim.MonthStyle
import io.github.michael412133.zmanim.Place
import io.github.michael412133.zmanim.Places
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

    // The day's screen
    abstract fun dayTitle(date: LocalDate, withYear: Boolean): String
    abstract fun clockPattern(is24Hour: Boolean): String
    abstract val goToToday: String
    abstract fun afterShkia(hebrewDate: String): String
    abstract val previousDay: String
    abstract val nextDay: String
    abstract val openMonth: String
    abstract val previousPage: String
    abstract val nextPage: String
    abstract fun zmanName(kind: ZmanKind): String
    abstract fun zmanNote(kind: ZmanKind): String

    // Settings
    abstract val settings: String
    abstract val back: String
    abstract val location: String
    abstract val languageTitle: String
    abstract val languageName: String
    abstract val monthView: String
    abstract fun monthStyleName(style: MonthStyle): String
    abstract val about: String
    abstract val aboutNote: String

    // Towns and GPS
    abstract fun townName(place: Place): String
    abstract val myLocation: String
    abstract fun myLocationNear(town: String): String
    abstract val pickTown: String
    abstract val gpsHint: String
    abstract val gpsSearching: String
    abstract fun gpsUpdated(date: LocalDate): String
    abstract val gpsOff: String
    abstract val gpsDenied: String
    abstract val gpsNoSignal: String

    // The month view
    abstract val weekdays: List<String>
    abstract val previousMonth: String
    abstract val nextMonth: String
    abstract val previousYear: String
    abstract val nextYear: String
    abstract val chooseMonth: String
    abstract val today: String
    abstract val markedLegend: String
    protected abstract val gregorianMonths: List<String>

    // About
    abstract val appName: String
    abstract val aboutParagraphs: List<String>

    /** The words for a town from the list, or "My location, near ..." for the GPS spot. */
    fun placeName(place: Place): String {
        if (!place.isGps) return townName(place)
        val near = Places.nearest(place.latitude, place.longitude)
        return if (near != null) myLocationNear(townName(near)) else myLocation
    }

    private val formatter: HebrewDateFormatter
        get() = HebrewDateFormatter().apply { setHebrewFormat(language == Language.Hebrew) }

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

        override fun dayTitle(date: LocalDate, withYear: Boolean): String =
            days.getValue(date.dayOfWeek) + ", " + gregorianMonthName(date) + " " + date.dayOfMonth +
                if (withYear) ", " + date.year else ""

        override fun clockPattern(is24Hour: Boolean) = if (is24Hour) "H:mm" else "h:mm a"
        override val goToToday = "Go to today"
        override fun afterShkia(hebrewDate: String) = "After shkia: $hebrewDate"
        override val previousDay = "Previous day"
        override val nextDay = "Next day"
        override val openMonth = "Open the month"
        override val previousPage = "Previous page"
        override val nextPage = "Next page"

        override fun zmanName(kind: ZmanKind) = when (kind) {
            ZmanKind.Alos72 -> "Alos HaShachar"
            ZmanKind.Misheyakir -> "Misheyakir"
            ZmanKind.Netz -> "Netz HaChama"
            ZmanKind.SofZmanShmaMGA, ZmanKind.SofZmanShmaGRA -> "Sof Zman Krias Shema"
            ZmanKind.SofZmanTfilaMGA, ZmanKind.SofZmanTfilaGRA -> "Sof Zman Tefillah"
            ZmanKind.Chatzos -> "Chatzos"
            ZmanKind.MinchaGedola -> "Mincha Gedola"
            ZmanKind.MinchaKetana -> "Mincha Ketana"
            ZmanKind.PlagHamincha -> "Plag HaMincha"
            ZmanKind.CandleLighting, ZmanKind.CandleLightingAfterTzeis -> "Hadlakas Neiros"
            ZmanKind.Shkia -> "Shkias HaChama"
            ZmanKind.Tzeis -> "Tzeis HaKochavim"
            ZmanKind.RabbeinuTam -> "Tzeis Rabbeinu Tam"
        }

        override fun zmanNote(kind: ZmanKind) = when (kind) {
            ZmanKind.Alos72 -> "72 minutes before netz"
            ZmanKind.Misheyakir -> "Earliest tallis and tefillin, 11.5°"
            ZmanKind.Netz -> "Sunrise"
            ZmanKind.SofZmanShmaMGA, ZmanKind.SofZmanTfilaMGA -> "Magen Avraham"
            ZmanKind.SofZmanShmaGRA, ZmanKind.SofZmanTfilaGRA -> "Gra"
            ZmanKind.Chatzos -> "Midday"
            ZmanKind.MinchaGedola -> "Earliest mincha"
            ZmanKind.MinchaKetana, ZmanKind.PlagHamincha -> "Gra"
            ZmanKind.CandleLighting -> "18 minutes before shkia"
            ZmanKind.CandleLightingAfterTzeis -> "Not before tzeis, from an existing flame"
            ZmanKind.Shkia -> "Sunset"
            ZmanKind.Tzeis -> "Nightfall, 8.5°"
            ZmanKind.RabbeinuTam -> "72 minutes after shkia"
        }

        override val settings = "Settings"
        override val back = "Back"
        override val location = "Location"
        override val languageTitle = "Language"
        override val languageName = "English"
        override val monthView = "Month view"
        override fun monthStyleName(style: MonthStyle) = when (style) {
            MonthStyle.English -> "English months (October)"
            MonthStyle.Hebrew -> "Hebrew months (Tishrei)"
        }
        override val about = "About these times"
        override val aboutNote = "How they are worked out and rounded"

        override fun townName(place: Place) = place.name
        override val myLocation = "My location"
        override fun myLocationNear(town: String) = "My location, near $town"
        override val pickTown = "Pick the nearest town"
        override val gpsHint = "Uses the phone's GPS"
        override val gpsSearching = "Finding your location, this can take a minute"
        override fun gpsUpdated(date: LocalDate) =
            "Updated ${gregorianMonthName(date)} ${date.dayOfMonth}, tap to update"
        override val gpsOff = "Location is off, tap to turn it on in settings"
        override val gpsDenied = "Needs location permission, tap to allow it"
        override val gpsNoSignal = "No GPS signal, try by a window or outside"

        override val weekdays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Shab")
        override val previousMonth = "Previous month"
        override val nextMonth = "Next month"
        override val previousYear = "Previous year"
        override val nextYear = "Next year"
        override val chooseMonth = "Choose a month"
        override val today = "Today"
        override val markedLegend = "• Yom Tov, Rosh Chodesh or a fast"
        override val gregorianMonths = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December",
        )

        override val appName = "Zmanim & Luach"
        override val aboutParagraphs = listOf(
            "Every time is worked out on this phone with the KosherJava zmanim library. " +
                "Nothing is sent anywhere, and no internet is needed.",
            "Netz and shkia are at sea level, the way most luchos in America print them.",
            "Times are rounded to the safe side. A deadline, like sof zman krias shema, is shown " +
                "a minute earlier, and a starting time, like tzeis, a minute later.",
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

        override fun dayTitle(date: LocalDate, withYear: Boolean): String =
            days.getValue(date.dayOfWeek) + ", " + date.dayOfMonth + " ב" + gregorianMonthName(date) +
                if (withYear) " " + date.year else ""

        // Hebrew luchos print the time without AM or PM.
        override fun clockPattern(is24Hour: Boolean) = if (is24Hour) "H:mm" else "h:mm"
        override val goToToday = "חזרה להיום"
        override fun afterShkia(hebrewDate: String) = "אחרי השקיעה: $hebrewDate"
        override val previousDay = "היום הקודם"
        override val nextDay = "היום הבא"
        override val openMonth = "פתיחת החודש"
        override val previousPage = "העמוד הקודם"
        override val nextPage = "העמוד הבא"

        override fun zmanName(kind: ZmanKind) = when (kind) {
            ZmanKind.Alos72 -> "עלות השחר"
            ZmanKind.Misheyakir -> "משיכיר"
            ZmanKind.Netz -> "הנץ החמה"
            ZmanKind.SofZmanShmaMGA, ZmanKind.SofZmanShmaGRA -> "סוף זמן קריאת שמע"
            ZmanKind.SofZmanTfilaMGA, ZmanKind.SofZmanTfilaGRA -> "סוף זמן תפילה"
            ZmanKind.Chatzos -> "חצות היום"
            ZmanKind.MinchaGedola -> "מנחה גדולה"
            ZmanKind.MinchaKetana -> "מנחה קטנה"
            ZmanKind.PlagHamincha -> "פלג המנחה"
            ZmanKind.CandleLighting, ZmanKind.CandleLightingAfterTzeis -> "הדלקת נרות"
            ZmanKind.Shkia -> "שקיעת החמה"
            ZmanKind.Tzeis -> "צאת הכוכבים"
            ZmanKind.RabbeinuTam -> "רבינו תם"
        }

        override fun zmanNote(kind: ZmanKind) = when (kind) {
            ZmanKind.Alos72 -> "72 דקות לפני הנץ"
            ZmanKind.Misheyakir -> "זמן טלית ותפילין, 11.5°"
            ZmanKind.Netz -> "זריחה"
            ZmanKind.SofZmanShmaMGA, ZmanKind.SofZmanTfilaMGA -> "מגן אברהם"
            ZmanKind.SofZmanShmaGRA, ZmanKind.SofZmanTfilaGRA -> "הגר״א"
            ZmanKind.Chatzos -> "אמצע היום"
            ZmanKind.MinchaGedola -> "תחילת זמן מנחה"
            ZmanKind.MinchaKetana, ZmanKind.PlagHamincha -> "הגר״א"
            ZmanKind.CandleLighting -> "18 דקות לפני השקיעה"
            ZmanKind.CandleLightingAfterTzeis -> "אחרי צאת הכוכבים, מאש קיימת"
            ZmanKind.Shkia -> "שקיעה"
            ZmanKind.Tzeis -> "לילה, 8.5°"
            ZmanKind.RabbeinuTam -> "צאת הכוכבים, 72 דקות אחרי השקיעה"
        }

        override val settings = "הגדרות"
        override val back = "חזרה"
        override val location = "מיקום"
        override val languageTitle = "שפה"
        override val languageName = "עברית"
        override val monthView = "תצוגת חודש"
        override fun monthStyleName(style: MonthStyle) = when (style) {
            MonthStyle.English -> "חודשים לועזיים (אוקטובר)"
            MonthStyle.Hebrew -> "חודשים עבריים (תשרי)"
        }
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
            "lakewood" to "לייקווד",
            "boro-park" to "בורו פארק",
            "crown-heights" to "קראון הייטס",
            "flatbush" to "פלעטבוש",
            "williamsburg" to "וויליאמסבורג",
        )

        override fun townName(place: Place) = towns[place.id] ?: place.name
        override val myLocation = "המיקום שלי"
        override fun myLocationNear(town: String) = "המיקום שלי, ליד $town"
        override val pickTown = "בחרו את העיר הקרובה"
        override val gpsHint = "לפי ה־GPS של הטלפון"
        override val gpsSearching = "מחפש את המיקום, זה יכול לקחת דקה"
        override fun gpsUpdated(date: LocalDate) =
            "עודכן ב־${date.dayOfMonth} ב${gregorianMonthName(date)}, לחצו לעדכון"
        override val gpsOff = "המיקום כבוי, לחצו כדי להדליק אותו בהגדרות"
        override val gpsDenied = "צריך הרשאת מיקום, לחצו כדי לאשר"
        override val gpsNoSignal = "אין קליטת GPS, נסו ליד חלון או בחוץ"

        override val weekdays = listOf("א׳", "ב׳", "ג׳", "ד׳", "ה׳", "ו׳", "שבת")
        override val previousMonth = "החודש הקודם"
        override val nextMonth = "החודש הבא"
        override val previousYear = "השנה הקודמת"
        override val nextYear = "השנה הבאה"
        override val chooseMonth = "בחירת חודש"
        override val today = "היום"
        override val markedLegend = "• יום טוב, ראש חודש או תענית"
        override val gregorianMonths = listOf(
            "ינואר", "פברואר", "מרץ", "אפריל", "מאי", "יוני",
            "יולי", "אוגוסט", "ספטמבר", "אוקטובר", "נובמבר", "דצמבר",
        )

        override val appName = "זמנים ולוח"
        override val aboutParagraphs = listOf(
            "כל הזמנים מחושבים בטלפון עצמו בעזרת ספריית הזמנים KosherJava. " +
                "שום דבר לא נשלח לשום מקום, ואין צורך באינטרנט.",
            "הנץ והשקיעה מחושבים לגובה פני הים, כמו ברוב הלוחות באמריקה.",
            "הזמנים מעוגלים לחומרא: זמן אחרון, כמו סוף זמן קריאת שמע, מוקדם בדקה, " +
                "וזמן התחלה, כמו צאת הכוכבים, מאוחר בדקה.",
            "״המיקום שלי״ משתמש ב־GPS של הטלפון עצמו, והמיקום נשאר בטלפון.",
            "להלכה למעשה, יש לנהוג לפי הוראת הרב ולוח בית הכנסת.",
            "github.com/michael412133/zmanim-luach",
            "נבנה עם KosherJava Zmanim (LGPL 2.1) ועם Mudita Mindful Design (Apache 2.0).",
        )
    }
}

/** The words for the language chosen in the settings. */
val LocalStrings = staticCompositionLocalOf<Strings> { Strings.English }
