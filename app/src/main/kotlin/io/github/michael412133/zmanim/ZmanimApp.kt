package io.github.michael412133.zmanim

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import io.github.michael412133.zmanim.ui.AboutScreen
import io.github.michael412133.zmanim.ui.DayScreen
import io.github.michael412133.zmanim.ui.LocalStrings
import io.github.michael412133.zmanim.ui.MonthPickerScreen
import io.github.michael412133.zmanim.ui.MonthScreen
import io.github.michael412133.zmanim.ui.PlacesScreen
import io.github.michael412133.zmanim.ui.SettingsScreen
import io.github.michael412133.zmanim.ui.Strings
import java.time.LocalDate
import java.util.Date

private enum class Screen { Day, Month, MonthPicker, Settings, Places, About }

/**
 * The whole app: which screens are open, the town, the language, and which day and month are
 * being looked at. [resumes] goes up every time the app comes back to the front, so "today"
 * and "now" are read again then. [freshStarts] goes up when it comes back after a long time
 * away, which also returns it to today.
 */
@Composable
fun ZmanimApp(prefs: Prefs, resumes: Int, freshStarts: Int) {
    var place by remember { mutableStateOf(prefs.place) }
    var language by remember { mutableStateOf(prefs.language) }
    var monthStyle by remember { mutableStateOf(prefs.monthStyle) }
    var savedGps by remember { mutableStateOf(prefs.gpsPlace) }
    var gpsUpdatedAt by remember { mutableStateOf(prefs.gpsUpdatedAt) }

    // The screens open on top of each other; Back closes the top one.
    val screens = remember { mutableStateListOf(Screen.Day) }
    var shownDate by remember { mutableStateOf<LocalDate?>(null) }
    var month by remember { mutableStateOf<CalendarMonth?>(null) }
    var pickerYear by remember { mutableIntStateOf(0) }

    LaunchedEffect(freshStarts) {
        if (freshStarts > 0) {
            shownDate = null
            screens.clear()
            screens.add(Screen.Day)
        }
    }

    val today = remember(resumes, place) { LocalDate.now(place.zone) }
    val now = remember(resumes, place, shownDate) { Date() }
    val date = shownDate ?: today
    val day = remember(place, date) { Luach.day(place, date) }
    val strings = Strings.of(language)

    fun open(screen: Screen) {
        screens.add(screen)
    }

    fun close() {
        if (screens.size > 1) screens.removeAt(screens.lastIndex)
    }

    fun show(newDate: LocalDate) {
        shownDate = if (newDate == today) null else newDate
    }

    BackHandler(enabled = screens.size > 1 || shownDate != null) {
        if (screens.size > 1) close() else shownDate = null
    }

    CompositionLocalProvider(
        LocalStrings provides strings,
        LocalLayoutDirection provides strings.direction,
    ) {
        when (screens.last()) {
            Screen.Day -> DayScreen(
                day = day,
                place = place,
                today = today,
                now = now,
                resetKey = listOf(date, place.id, freshStarts),
                onPreviousDay = { show(date.minusDays(1)) },
                onNextDay = { show(date.plusDays(1)) },
                onToday = { shownDate = null },
                onOpenMonth = {
                    month = Months.containing(monthStyle, date)
                    open(Screen.Month)
                },
                onSettings = { open(Screen.Settings) },
            )

            Screen.Month -> {
                val shownMonth = month ?: Months.containing(monthStyle, date)
                MonthScreen(
                    month = shownMonth,
                    selected = date,
                    today = today,
                    inIsrael = place.inIsrael,
                    onPrevious = { month = Months.previous(shownMonth) },
                    onNext = { month = Months.next(shownMonth) },
                    onChooseMonth = {
                        pickerYear = Months.yearOf(shownMonth)
                        open(Screen.MonthPicker)
                    },
                    onPick = {
                        show(it)
                        close()
                    },
                    onToday = {
                        shownDate = null
                        close()
                    },
                )
            }

            Screen.MonthPicker -> {
                val shownMonth = month ?: Months.containing(monthStyle, date)
                MonthPickerScreen(
                    style = shownMonth.style,
                    year = pickerYear,
                    current = shownMonth,
                    onYear = { pickerYear = it },
                    onPick = {
                        month = it
                        close()
                    },
                    onBack = { close() },
                )
            }

            Screen.Settings -> SettingsScreen(
                place = place,
                monthStyle = monthStyle,
                onPlaces = { open(Screen.Places) },
                onSwitchLanguage = {
                    language = if (language == Language.English) Language.Hebrew else Language.English
                    prefs.language = language
                },
                onSwitchMonthStyle = {
                    monthStyle = if (monthStyle == MonthStyle.English) MonthStyle.Hebrew else MonthStyle.English
                    prefs.monthStyle = monthStyle
                },
                onAbout = { open(Screen.About) },
                onBack = { close() },
            )

            Screen.Places -> PlacesScreen(
                current = place,
                savedGps = savedGps,
                gpsUpdatedAt = gpsUpdatedAt,
                onPick = {
                    place = it
                    prefs.place = it
                    close()
                },
                onGpsFound = {
                    val foundAt = System.currentTimeMillis()
                    prefs.saveGps(it, foundAt)
                    prefs.place = it
                    savedGps = it
                    gpsUpdatedAt = foundAt
                    place = it
                    close()
                },
                onBack = { close() },
            )

            Screen.About -> AboutScreen(onBack = { close() })
        }
    }
}
