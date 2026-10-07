package io.github.michael412133.zmanim.ui

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.divider.HorizontalDividerMMD
import com.mudita.mmd.components.text.TextMMD
import io.github.michael412133.zmanim.Day
import io.github.michael412133.zmanim.Place
import io.github.michael412133.zmanim.Zman
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** One line of the day's list: a zman, or the way into the page that explains them. */
private sealed interface Line {
    data class Time(val zman: Zman, val next: Boolean) : Line
    data object About : Line
}

/**
 * The main screen: the date in both calendars at the top, then the day's zmanim a page at a
 * time. On today's list the next zman to come is in bold with a bar beside it, and the list
 * opens on its page.
 */
@Composable
fun TodayScreen(
    day: Day,
    place: Place,
    isToday: Boolean,
    now: Date,
    resetKey: Any?,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    onPlaces: () -> Unit,
    onAbout: () -> Unit,
) {
    val context = LocalContext.current
    val clock = remember(place.timeZone) {
        val pattern = if (DateFormat.is24HourFormat(context)) "H:mm" else "h:mm a"
        SimpleDateFormat(pattern, Locale.US).apply { timeZone = TimeZone.getTimeZone(place.timeZone) }
    }
    val next = if (isToday) day.zmanim.indexOfFirst { it.time?.after(now) == true } else -1
    val lines = remember(day, next) {
        buildList<Line> {
            day.zmanim.forEachIndexed { index, zman -> add(Line.Time(zman, index == next)) }
            add(Line.About)
        }
    }
    val afterShkia = isToday && day.shkia?.before(now) == true

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Header(day, isToday, afterShkia, onPreviousDay, onNextDay, onToday)
        HorizontalDividerMMD()
        PagedList(
            items = lines,
            rowHeight = rowHeight(),
            resetKey = resetKey,
            modifier = Modifier.weight(1f),
            focus = if (next >= 0) next else 0,
            footer = {
                TextMMD(
                    text = place.name,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .clickable(onClick = onPlaces)
                        .padding(vertical = 14.dp),
                )
            },
        ) { line ->
            when (line) {
                is Line.Time -> ZmanRow(line.zman, line.next, clock)
                Line.About -> AboutRow(onAbout)
            }
        }
    }
}

@Composable
private fun Header(
    day: Day,
    isToday: Boolean,
    afterShkia: Boolean,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
) {
    val date = remember(day.date) { day.date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.US)) }
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconAction(Symbols.Previous, "Previous day", onPreviousDay)
            TextMMD(
                text = date,
                modifier = Modifier
                    .weight(1f)
                    .then(if (isToday) Modifier else Modifier.clickable(onClick = onToday)),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday) null else FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            IconAction(Symbols.Next, "Next day", onNextDay)
        }
        Centered(day.hebrewDate, MaterialTheme.typography.titleMedium)
        when {
            !isToday -> Centered("Tap the date to go back to today", MaterialTheme.typography.labelSmall)
            afterShkia -> Centered("After shkia: ${day.tonightHebrewDate}", MaterialTheme.typography.labelSmall)
        }
        if (day.special.isNotBlank()) {
            Centered(day.special, MaterialTheme.typography.bodySmall)
        }
        Centered(
            listOf(day.parsha, day.daf).filter { it.isNotBlank() }.joinToString(" · "),
            MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun Centered(text: String, style: TextStyle) {
    TextMMD(
        text = text,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        style = style,
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun ZmanRow(zman: Zman, next: Boolean, clock: SimpleDateFormat) {
    val weight = if (next) FontWeight.Bold else null
    Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (next) {
            Box(
                Modifier
                    .padding(end = 8.dp)
                    .width(4.dp)
                    .height(32.dp)
                    .background(MaterialTheme.colorScheme.onSurface),
            )
        }
        Column(Modifier.weight(1f)) {
            TextMMD(
                text = zman.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = weight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            TextMMD(
                text = zman.note,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        TextMMD(
            text = zman.time?.let { clock.format(it) } ?: "--:--",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = weight,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun AboutRow(onAbout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onAbout)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            TextMMD(text = "About these times", style = MaterialTheme.typography.bodyMedium)
            TextMMD(text = "How they are worked out and rounded", style = MaterialTheme.typography.labelSmall)
        }
        Icon(
            imageVector = Symbols.Next,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp),
        )
    }
}
