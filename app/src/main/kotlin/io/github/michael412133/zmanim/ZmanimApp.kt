package io.github.michael412133.zmanim

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.michael412133.zmanim.ui.AboutScreen
import io.github.michael412133.zmanim.ui.PlacesScreen
import io.github.michael412133.zmanim.ui.TodayScreen
import java.time.LocalDate
import java.util.Date

private enum class Screen { Today, Places, About }

/**
 * The whole app: which screen is up, which town, and which day. [resumes] goes up every time
 * the app comes back to the front, so "today" and "now" are read again then. [freshStarts]
 * goes up when it comes back after a long time away, which also returns it to today.
 */
@Composable
fun ZmanimApp(prefs: Prefs, resumes: Int, freshStarts: Int) {
    var place by remember { mutableStateOf(prefs.place) }
    var screen by remember { mutableStateOf(Screen.Today) }
    var shownDate by remember { mutableStateOf<LocalDate?>(null) }

    LaunchedEffect(freshStarts) {
        if (freshStarts > 0) {
            shownDate = null
            screen = Screen.Today
        }
    }

    val today = remember(resumes, place) { LocalDate.now(place.zone) }
    val now = remember(resumes, place, shownDate) { Date() }
    val date = shownDate ?: today
    val day = remember(place, date) { Luach.day(place, date) }

    BackHandler(enabled = screen != Screen.Today || shownDate != null) {
        if (screen != Screen.Today) screen = Screen.Today else shownDate = null
    }

    when (screen) {
        Screen.Today -> TodayScreen(
            day = day,
            place = place,
            isToday = date == today,
            now = now,
            resetKey = listOf(date, place.id, freshStarts),
            onPreviousDay = { date.minusDays(1).let { shownDate = if (it == today) null else it } },
            onNextDay = { date.plusDays(1).let { shownDate = if (it == today) null else it } },
            onToday = { shownDate = null },
            onPlaces = { screen = Screen.Places },
            onAbout = { screen = Screen.About },
        )

        Screen.Places -> PlacesScreen(
            current = place,
            onPick = {
                place = it
                prefs.place = it
                screen = Screen.Today
            },
            onBack = { screen = Screen.Today },
        )

        Screen.About -> AboutScreen(onBack = { screen = Screen.Today })
    }
}
