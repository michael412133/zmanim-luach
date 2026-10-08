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
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import io.github.michael412133.zmanim.ui.AboutScreen
import io.github.michael412133.zmanim.ui.EventsScreen
import io.github.michael412133.zmanim.ui.HelpScreen
import io.github.michael412133.zmanim.ui.LocalStrings
import io.github.michael412133.zmanim.ui.MainScreen
import io.github.michael412133.zmanim.ui.MonthPickerScreen
import io.github.michael412133.zmanim.ui.PlacesScreen
import io.github.michael412133.zmanim.ui.SettingsScreen
import io.github.michael412133.zmanim.ui.Strings
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.util.Date

private enum class Screen { Main, MonthPicker, Settings, Places, Events, Help, About }

/**
 * The whole app: which screens are open, the settings, the reader's events, and which day is
 * being looked at. [resumes] goes up every time the app comes back to the front, so "today"
 * and "now" are read again then. [freshStarts] goes up when it comes back after a long time
 * away, which also returns it to today with the month open.
 */
@Composable
fun ZmanimApp(prefs: Prefs, resumes: Int, freshStarts: Int) {
    var place by remember { mutableStateOf(prefs.place) }
    var language by remember { mutableStateOf(prefs.language) }
    var monthStyle by remember { mutableStateOf(prefs.monthStyle) }
    var savedGps by remember { mutableStateOf(prefs.gpsPlace) }
    var gpsUpdatedAt by remember { mutableStateOf(prefs.gpsUpdatedAt) }
    var opinions by remember { mutableStateOf(prefs.opinions) }
    var omerWording by remember { mutableStateOf(prefs.omerWording) }
    var hidden by remember { mutableStateOf(prefs.hidden) }
    var events by remember { mutableStateOf(prefs.events) }

    // The screens open on top of each other; Back closes the top one. Each keeps its own state,
    // like the page its list is on, while a screen opened from it is on top.
    val screens = remember { mutableStateListOf(Screen.Main) }
    val saved = rememberSaveableStateHolder()
    var selected by remember { mutableStateOf<LocalDate?>(null) }
    var monthOpen by remember { mutableStateOf(true) }
    var page by remember { mutableIntStateOf(0) }
    var pickerYear by remember { mutableIntStateOf(0) }

    LaunchedEffect(freshStarts) {
        if (freshStarts > 0) {
            selected = null
            monthOpen = true
            page = 0
            screens.drop(1).forEach { saved.removeState(it.name) }
            screens.clear()
            screens.add(Screen.Main)
        }
    }

    // "Now" is read again when the app comes back, when another day is picked, and on its own
    // when the next zman comes up or the day changes, so the bold line and today stay right.
    var now by remember { mutableStateOf(Date()) }
    LaunchedEffect(resumes, selected) { now = Date() }
    val today = remember(now, place) { now.toInstant().atZone(place.zone).toLocalDate() }
    val date = selected ?: today
    val day = remember(place, date, opinions) { Luach.day(place, date, opinions) }
    LaunchedEffect(now, day, today) {
        val midnight = today.plusDays(1).atStartOfDay(place.zone).toInstant().toEpochMilli()
        val todaysTimes = if (date == today) day.zmanim.mapNotNull { it.time?.time } + listOfNotNull(day.shkia?.time) else emptyList()
        val wake = (todaysTimes + midnight).filter { it > now.time }.minOrNull() ?: return@LaunchedEffect
        delay(wake - now.time + 1_000L)
        now = Date()
    }
    val strings = Strings.of(language)

    fun open(screen: Screen) {
        screens.add(screen)
    }

    fun close() {
        if (screens.size > 1) {
            saved.removeState(screens.last().name)
            screens.removeAt(screens.lastIndex)
        }
    }

    fun select(newDate: LocalDate) {
        selected = if (newDate == today) null else newDate
    }

    fun saveEvent(event: Event) {
        events = events.filter { it.id != event.id } + event
        prefs.events = events
    }

    fun deleteEvent(event: Event) {
        events = events.filter { it.id != event.id }
        prefs.events = events
    }

    BackHandler(enabled = screens.size > 1 || selected != null || !monthOpen) {
        if (screens.size > 1) {
            close()
        } else {
            selected = null
            monthOpen = true
            page = 0
        }
    }

    CompositionLocalProvider(
        LocalStrings provides strings,
        LocalLayoutDirection provides strings.direction,
    ) {
        val top = screens.last()
        saved.SaveableStateProvider(top.name) {
            when (top) {
                Screen.Main -> MainScreen(
                    day = day,
                    place = place,
                    style = monthStyle,
                    today = today,
                    now = now,
                    monthOpen = monthOpen,
                    page = page,
                    hidden = hidden,
                    events = events,
                    omerWording = omerWording,
                    resetKey = freshStarts,
                    onSelect = { select(it) },
                    onMonthOpen = {
                        monthOpen = it
                        page = 0
                    },
                    onPage = { page = it },
                    onToday = {
                        selected = null
                        page = 0
                    },
                    onChooseMonth = {
                        pickerYear = Months.yearOf(Months.containing(monthStyle, date))
                        open(Screen.MonthPicker)
                    },
                    onPlaces = { open(Screen.Places) },
                    onSettings = { open(Screen.Settings) },
                    onSaveEvent = { saveEvent(it) },
                    onDeleteEvent = { deleteEvent(it) },
                )

                Screen.MonthPicker -> MonthPickerScreen(
                    style = monthStyle,
                    year = pickerYear,
                    current = Months.containing(monthStyle, date),
                    onYear = { pickerYear = it },
                    onPick = { month ->
                        select(if (today in month) today else month.firstDay)
                        monthOpen = true
                        close()
                    },
                    onBack = { close() },
                )

                Screen.Settings -> SettingsScreen(
                    place = place,
                    language = language,
                    monthStyle = monthStyle,
                    opinions = opinions,
                    omerWording = omerWording,
                    hidden = hidden,
                    eventCount = events.size,
                    onPlaces = { open(Screen.Places) },
                    onLanguage = {
                        language = it
                        prefs.language = it
                    },
                    onMonthStyle = {
                        monthStyle = it
                        prefs.monthStyle = it
                    },
                    onOpinion = {
                        opinions = opinions.with(it)
                        prefs.opinions = opinions
                    },
                    onOmerWording = {
                        omerWording = it
                        prefs.omerWording = it
                    },
                    onHidden = {
                        hidden = it
                        prefs.hidden = it
                    },
                    onEvents = { open(Screen.Events) },
                    onHelp = { open(Screen.Help) },
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

                Screen.Events -> EventsScreen(
                    events = events,
                    today = today,
                    onSave = { saveEvent(it) },
                    onDelete = { deleteEvent(it) },
                    onBack = { close() },
                )

                Screen.Help -> HelpScreen(onBack = { close() })

                Screen.About -> AboutScreen(onBack = { close() })
            }
        }
    }
}
