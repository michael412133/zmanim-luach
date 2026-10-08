package io.github.michael412133.zmanim.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kosherjava.zmanim.hebrewcalendar.JewishDate
import com.mudita.mmd.components.text.TextMMD
import io.github.michael412133.zmanim.Event
import io.github.michael412133.zmanim.Events
import java.time.LocalDate

/**
 * The reader's events, the next one to come first. Each line has the event and when it comes
 * back; tapping one opens it to change or delete.
 */
@Composable
fun EventsScreen(
    events: List<Event>,
    today: LocalDate,
    onSave: (Event) -> Unit,
    onDelete: (Event) -> Unit,
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    var editing by remember { mutableStateOf<Event?>(null) }
    val sorted = remember(events, today) { events.sortedBy { nextDate(it, today) } }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        TitleBar(strings.myEvents, onBack)
        if (sorted.isEmpty()) {
            TextMMD(
                text = strings.noEvents,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(16.dp),
            )
        } else {
            PagedList(
                items = sorted,
                rowHeight = rowHeight(60.dp),
                resetKey = Unit,
                modifier = Modifier.weight(1f),
            ) { event ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { editing = event }
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start,
                ) {
                    TextMMD(
                        text = strings.eventLine(event),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    TextMMD(
                        text = strings.eventRepeats(event),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }

    editing?.let { event ->
        EventFlow(
            start = event,
            isNew = false,
            onSave = onSave,
            onDelete = onDelete,
            onClose = { editing = null },
        )
    }
}

/** The next day an event falls on, from today on. */
private fun nextDate(event: Event, today: LocalDate): LocalDate {
    if (!event.hebrew) {
        if (event.date.isAfter(today)) return event.date
        val thisYear = Events.englishDate(event, today.year)
        return if (thisYear.isBefore(today)) Events.englishDate(event, today.year + 1) else thisYear
    }
    val year = JewishDate(today).jewishYear
    return (Events.hebrewDates(event, year) + Events.hebrewDates(event, year + 1))
        .filter { !it.isBefore(today) }
        .minOrNull() ?: event.date
}
