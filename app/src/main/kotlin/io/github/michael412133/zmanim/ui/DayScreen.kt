package io.github.michael412133.zmanim.ui

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.divider.HorizontalDividerMMD
import com.mudita.mmd.components.text.TextMMD
import io.github.michael412133.zmanim.Day
import io.github.michael412133.zmanim.Place
import io.github.michael412133.zmanim.Zman
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max

/**
 * The main screen: the date in both calendars at the top, then the day's zmanim a page at a
 * time. On today's list the next zman to come is in bold with a bar beside it, and the list
 * opens on its page. The date opens the month; the bar at the bottom opens the settings.
 */
@Composable
fun DayScreen(
    day: Day,
    place: Place,
    today: LocalDate,
    now: Date,
    resetKey: Any?,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    onOpenMonth: () -> Unit,
    onSettings: () -> Unit,
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val isToday = day.date == today
    val clock = remember(place.timeZone, strings) {
        SimpleDateFormat(strings.clockPattern(DateFormat.is24HourFormat(context)), Locale.US)
            .apply { timeZone = TimeZone.getTimeZone(place.timeZone) }
    }
    val next = if (isToday) day.zmanim.indexOfFirst { it.time?.after(now) == true } else -1
    val nextZman = day.zmanim.getOrNull(next)
    val afterShkia = isToday && day.shkia?.before(now) == true

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Header(
            day = day,
            isToday = isToday,
            withYear = day.date.year != today.year,
            afterShkia = afterShkia,
            onPreviousDay = onPreviousDay,
            onNextDay = onNextDay,
            onToday = onToday,
            onOpenMonth = onOpenMonth,
        )
        HorizontalDividerMMD()
        PagedList(
            items = day.zmanim,
            rowHeight = rowHeight(),
            resetKey = resetKey,
            modifier = Modifier.weight(1f),
            focus = max(next, 0),
            footer = { SettingsButton(strings.placeName(place), onSettings) },
        ) { zman ->
            ZmanRow(zman, next = zman == nextZman, clock = clock)
        }
    }
}

@Composable
private fun Header(
    day: Day,
    isToday: Boolean,
    withYear: Boolean,
    afterShkia: Boolean,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    onOpenMonth: () -> Unit,
) {
    val strings = LocalStrings.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconAction(Symbols.Previous, strings.previousDay, onPreviousDay)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(onClickLabel = strings.openMonth, onClick = onOpenMonth),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Symbols.Calendar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                TextMMD(
                    text = strings.dayTitle(day.date, withYear),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isToday) null else FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconAction(Symbols.Next, strings.nextDay, onNextDay)
        }
        Centered(day.hebrewDate, MaterialTheme.typography.titleMedium)
        when {
            !isToday -> TextMMD(
                text = strings.goToToday,
                modifier = Modifier
                    .clickable(onClick = onToday)
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                textDecoration = TextDecoration.Underline,
            )
            afterShkia -> Centered(strings.afterShkia(day.tonightHebrewDate), MaterialTheme.typography.labelSmall)
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

/** The bottom left of the main screen: the settings icon and the town the times are for. */
@Composable
private fun SettingsButton(placeName: String, onSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClickLabel = LocalStrings.current.settings, onClick = onSettings)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Symbols.Settings,
            contentDescription = LocalStrings.current.settings,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(10.dp))
        TextMMD(
            text = placeName,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ZmanRow(zman: Zman, next: Boolean, clock: SimpleDateFormat) {
    val strings = LocalStrings.current
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
                text = strings.zmanName(zman.kind),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = weight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            TextMMD(
                text = strings.zmanNote(zman.kind),
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
