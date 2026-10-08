package io.github.michael412133.zmanim.ui

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudita.mmd.components.divider.HorizontalDividerMMD
import com.mudita.mmd.components.text.TextMMD
import io.github.michael412133.zmanim.CalendarMonth
import io.github.michael412133.zmanim.Day
import io.github.michael412133.zmanim.Event
import io.github.michael412133.zmanim.EventType
import io.github.michael412133.zmanim.Events
import io.github.michael412133.zmanim.Language
import io.github.michael412133.zmanim.MonthCell
import io.github.michael412133.zmanim.MonthStyle
import io.github.michael412133.zmanim.Months
import io.github.michael412133.zmanim.OmerWording
import io.github.michael412133.zmanim.Paging
import io.github.michael412133.zmanim.Place
import io.github.michael412133.zmanim.Zman
import io.github.michael412133.zmanim.ZmanKind
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** What the main screen has open on top of it. */
private sealed interface MainPopup {
    data class Opinions(val zman: Zman) : MainPopup
    data class OmerText(val day: Int) : MainPopup
    data class EditEvent(val event: Event, val isNew: Boolean) : MainPopup
}

/**
 * The main screen. The month is on top, or just the week when it is folded, and under it the
 * day: both dates, Yom Tov or a fast in a black box, the reader's events, the parsha and the
 * daf, the town, and then the zmanim a page at a time. The bar along the bottom is always
 * there: Month, Today, Add and the settings.
 *
 * Swiping up folds the month into its week and then turns the pages of the times; swiping
 * down turns back, and on the first page opens the month again. Sideways, a swipe on the
 * month changes the month (or the week), and a swipe on the day changes the day.
 */
@Composable
fun MainScreen(
    day: Day,
    place: Place,
    style: MonthStyle,
    today: LocalDate,
    now: Date,
    monthOpen: Boolean,
    page: Int,
    hidden: Set<ZmanKind>,
    events: List<Event>,
    omerWording: OmerWording,
    resetKey: Any?,
    onSelect: (LocalDate) -> Unit,
    onMonthOpen: (Boolean) -> Unit,
    onPage: (Int) -> Unit,
    onToday: () -> Unit,
    onChooseMonth: () -> Unit,
    onPlaces: () -> Unit,
    onSettings: () -> Unit,
    onSaveEvent: (Event) -> Unit,
    onDeleteEvent: (Event) -> Unit,
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val rightToLeft = strings.direction == LayoutDirection.Rtl
    val date = day.date
    val isToday = date == today

    val clock = remember(place.timeZone, strings) {
        SimpleDateFormat(strings.clockPattern(DateFormat.is24HourFormat(context)), Locale.US)
            .apply { timeZone = TimeZone.getTimeZone(place.timeZone) }
    }
    // The molad is in Yerushalayim's own time, so its hours are shown as they are, not moved to this town's clock.
    val moladClock = remember(strings) {
        SimpleDateFormat(strings.moladClockPattern, Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
    }

    val month = remember(style, date) { Months.containing(style, date) }
    val cells = remember(month, monthOpen, date, place.inIsrael, events) {
        if (monthOpen) {
            Months.cells(month, place.inIsrael, events)
        } else {
            Months.cells(Months.weekStart(date), 7, place.inIsrael, events)
        }
    }
    val dayEvents = remember(date, events) { events.filter { Events.occursOn(it, date) } }
    val zmanim = remember(day, hidden) { day.zmanim.filter { it.kind.shownAs !in hidden } }
    val next = if (isToday) zmanim.firstOrNull { it.time?.after(now) == true } else null
    val afterShkia = isToday && day.shkia?.before(now) == true

    var popup by remember(resetKey) { mutableStateOf<MainPopup?>(null) }

    fun goToMonth(target: CalendarMonth) = onSelect(if (today in target) today else target.firstDay)

    val calendarSwipe by rememberUpdatedState<(Swipe) -> Unit>({ swipe ->
        when {
            swipe == Swipe.Up && monthOpen -> onMonthOpen(false)
            swipe == Swipe.Down && !monthOpen -> onMonthOpen(true)
            swipe.isForward(rightToLeft) -> if (monthOpen) goToMonth(Months.next(month)) else onSelect(date.plusDays(7))
            swipe.isBack(rightToLeft) -> if (monthOpen) goToMonth(Months.previous(month)) else onSelect(date.minusDays(7))
        }
    })

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        val headerHeight = 52.dp
        val weekdaysHeight = 22.dp
        val barHeight = 57.dp
        val available = maxHeight - headerHeight - weekdaysHeight - barHeight - 1.dp
        val weeks = if (monthOpen) month.weeks else 1
        // Open, the month takes a little under half of what is left, so the day still shows under it.
        val cellHeight = if (monthOpen) maxOf(32.dp, minOf(52.dp, available * 0.45f / weeks)) else 46.dp

        Column(Modifier.fillMaxSize()) {
            CalendarHeader(
                month = month,
                monthOpen = monthOpen,
                height = headerHeight,
                onPrevious = { if (monthOpen) goToMonth(Months.previous(month)) else onSelect(date.minusDays(7)) },
                onNext = { if (monthOpen) goToMonth(Months.next(month)) else onSelect(date.plusDays(7)) },
                onChoose = onChooseMonth,
            )
            WeekdayLabels(weekdaysHeight)
            CalendarGrid(
                cells = cells,
                leadingBlanks = if (monthOpen) month.leadingBlanks else 0,
                weeks = weeks,
                cellHeight = cellHeight,
                style = style,
                today = today,
                selected = date,
                onPick = onSelect,
                onSwipe = { calendarSwipe(it) },
            )
            HorizontalDividerMMD()
            DayPanel(
                day = day,
                isToday = isToday,
                afterShkia = afterShkia,
                withYear = date.year != today.year,
                dayEvents = dayEvents,
                place = place,
                zmanim = zmanim,
                next = next,
                clock = clock,
                moladClock = moladClock,
                monthOpen = monthOpen,
                page = page,
                onPage = onPage,
                onSwipe = { swipe, shown, pageCount ->
                    when {
                        swipe.isForward(rightToLeft) -> onSelect(date.plusDays(1))
                        swipe.isBack(rightToLeft) -> onSelect(date.minusDays(1))
                        swipe == Swipe.Up && monthOpen -> onMonthOpen(false)
                        swipe == Swipe.Up && shown < pageCount - 1 -> onPage(shown + 1)
                        swipe == Swipe.Down && !monthOpen && shown > 0 -> onPage(shown - 1)
                        swipe == Swipe.Down && !monthOpen -> onMonthOpen(true)
                    }
                },
                onZman = { zman ->
                    popup = when {
                        zman.kind == ZmanKind.Omer -> MainPopup.OmerText(zman.omerDay)
                        zman.opinions.size > 1 -> MainPopup.Opinions(zman)
                        else -> null
                    }
                },
                onEvent = { popup = MainPopup.EditEvent(it, isNew = false) },
                onPlaces = onPlaces,
                modifier = Modifier.weight(1f),
            )
            BottomBar(
                monthOpen = monthOpen,
                onMonth = { onMonthOpen(!monthOpen) },
                onToday = onToday,
                onAdd = {
                    popup = MainPopup.EditEvent(
                        Event(System.currentTimeMillis(), EventType.Yahrzeit, "", hebrew = true, date = date),
                        isNew = true,
                    )
                },
                onSettings = onSettings,
            )
        }
    }

    when (val open = popup) {
        is MainPopup.Opinions -> OpinionsPopup(open.zman, date, clock) { popup = null }
        is MainPopup.OmerText -> OmerPopup(open.day, omerWording) { popup = null }
        is MainPopup.EditEvent -> EventFlow(
            start = open.event,
            isNew = open.isNew,
            onSave = onSaveEvent,
            onDelete = onDeleteEvent,
            onClose = { popup = null },
        )
        null -> Unit
    }
}

/** The month's name with arrows on each side. Tapping the name opens the month chooser. */
@Composable
private fun CalendarHeader(
    month: CalendarMonth,
    monthOpen: Boolean,
    height: Dp,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onChoose: () -> Unit,
) {
    val strings = LocalStrings.current
    Row(
        modifier = Modifier.fillMaxWidth().height(height),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconAction(Symbols.Previous, if (monthOpen) strings.previousMonth else strings.previousWeek, onPrevious)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(onClickLabel = strings.chooseMonth, onClick = onChoose),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            TextMMD(
                text = strings.monthTitle(month),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            TextMMD(
                text = strings.monthSubtitle(month),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconAction(Symbols.Next, if (monthOpen) strings.nextMonth else strings.nextWeek, onNext)
    }
}

@Composable
private fun WeekdayLabels(height: Dp) {
    Row(Modifier.fillMaxWidth().height(height), verticalAlignment = Alignment.CenterVertically) {
        LocalStrings.current.weekdays.forEach { name ->
            TextMMD(
                text = name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

/** The month's weeks, or only the week of the day being looked at, Sunday to Shabbos. */
@Composable
private fun CalendarGrid(
    cells: List<MonthCell>,
    leadingBlanks: Int,
    weeks: Int,
    cellHeight: Dp,
    style: MonthStyle,
    today: LocalDate,
    selected: LocalDate,
    onPick: (LocalDate) -> Unit,
    onSwipe: (Swipe) -> Unit,
) {
    val swipe by rememberUpdatedState(onSwipe)
    Column(
        Modifier
            .fillMaxWidth()
            .height(cellHeight * weeks)
            .pointerInput(Unit) { detectSwipes { swipe(it) } },
    ) {
        for (week in 0 until weeks) {
            Row(Modifier.fillMaxWidth().height(cellHeight)) {
                for (weekday in 0 until 7) {
                    val cell = cells.getOrNull(week * 7 + weekday - leadingBlanks)
                    Box(Modifier.weight(1f).fillMaxHeight()) {
                        if (cell != null) {
                            DayCell(
                                cell = cell,
                                style = style,
                                isToday = cell.date == today,
                                isSelected = cell.date == selected,
                                compact = cellHeight < 40.dp,
                                onPick = onPick,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * One day's square: the date the month goes by in big, the other one small under it. Today is
 * black, the day being looked at has a frame, a special day is grey, and a dot in the corner
 * is one of the reader's events.
 */
@Composable
private fun DayCell(
    cell: MonthCell,
    style: MonthStyle,
    isToday: Boolean,
    isSelected: Boolean,
    compact: Boolean,
    onPick: (LocalDate) -> Unit,
) {
    val ink = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val fill = when {
        isToday -> MaterialTheme.colorScheme.primary
        cell.marked -> Grey
        else -> Color.Transparent
    }
    val shape = RoundedCornerShape(5.dp)
    val big = if (style == MonthStyle.English) cell.date.dayOfMonth.toString() else cell.hebrewDay
    val small = if (style == MonthStyle.English) cell.hebrewDay else cell.date.dayOfMonth.toString()
    val bigStyle = MaterialTheme.typography.bodyMedium.copy(
        fontSize = if (compact) 15.sp else 17.sp,
        lineHeight = if (compact) 17.sp else 20.sp,
    )
    val smallStyle = MaterialTheme.typography.labelSmall.copy(
        fontSize = if (compact) 11.sp else 12.sp,
        lineHeight = if (compact) 12.sp else 14.sp,
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(1.dp)
            .then(if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, shape) else Modifier)
            .clickable { onPick(cell.date) }
            .padding(if (isSelected) 3.dp else 1.dp)
            .background(fill, shape),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TextMMD(
                text = big,
                color = ink,
                style = bigStyle,
                fontWeight = if (cell.date.dayOfWeek == DayOfWeek.SATURDAY) FontWeight.Bold else null,
                maxLines = 1,
            )
            TextMMD(text = small, color = ink, style = smallStyle, maxLines = 1)
        }
        if (cell.hasEvent) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .size(5.dp)
                    .background(ink, CircleShape),
            )
        }
    }
}

/** The heights of the lines at the top of the day, so the times can be fitted to the page around them. */
private class DetailLines(
    val dates: Dp,
    val afterShkia: Dp,
    val special: Dp,
    val event: Dp,
    val text: Dp,
    val town: Dp,
    val padding: Dp,
)

/**
 * The day under the month: its details first, then its zmanim, a page at a time. With the
 * month open only the first page shows; with the month folded the segmented bar on the side
 * shows which page this is.
 */
@Composable
private fun DayPanel(
    day: Day,
    isToday: Boolean,
    afterShkia: Boolean,
    withYear: Boolean,
    dayEvents: List<Event>,
    place: Place,
    zmanim: List<Zman>,
    next: Zman?,
    clock: SimpleDateFormat,
    moladClock: SimpleDateFormat,
    monthOpen: Boolean,
    page: Int,
    onPage: (Int) -> Unit,
    onSwipe: (swipe: Swipe, shown: Int, pageCount: Int) -> Unit,
    onZman: (Zman) -> Unit,
    onEvent: (Event) -> Unit,
    onPlaces: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale = maxOf(1f, LocalDensity.current.fontScale)
    val lines = DetailLines(
        dates = 28.dp * scale,
        afterShkia = 20.dp * scale,
        special = 32.dp * scale,
        event = 26.dp * scale,
        text = 22.dp * scale,
        town = 32.dp * scale,
        padding = 10.dp,
    )
    val lineHeight = rowHeight(52.dp)
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val textStyle = MaterialTheme.typography.bodySmall

    BoxWithConstraints(modifier.fillMaxWidth()) {
        // The parsha and the daf share a line when they fit on one, beside the page bar.
        val width = constraints.maxWidth - with(density) { (32.dp + 22.dp).roundToPx() }
        val words = listOf(day.parsha, day.daf).filter { it.isNotBlank() }
        val joined = words.joinToString(" · ")
        val oneLine = measurer.measure(text = joined, style = textStyle, softWrap = false).size.width <= width
        val textLines = if (oneLine || words.size < 2) listOf(joined) else words

        val detailsHeight = lines.padding + lines.dates +
            (if (isToday && afterShkia) lines.afterShkia else 0.dp) +
            (if (day.special.isNotBlank()) lines.special else 0.dp) +
            lines.event * dayEvents.size +
            lines.text * textLines.size +
            lines.town
        val heights = listOf(detailsHeight) + zmanim.map { lineHeight }
        val pages = remember(heights, maxHeight) { Paging.pages(heights.map { it.value }, maxHeight.value) }
        val shown = if (monthOpen) 0 else page.coerceIn(0, pages.size - 1)

        val swipe by rememberUpdatedState<(Swipe) -> Unit>({ onSwipe(it, shown, pages.size) })

        Row(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .pointerInput(Unit) { detectSwipes { swipe(it) } },
            ) {
                for (index in pages[shown]) {
                    if (index == 0) {
                        Details(day, isToday && afterShkia, withYear, dayEvents, place, textLines, lines, onEvent, onPlaces)
                    } else {
                        val zman = zmanim[index - 1]
                        Box(Modifier.fillMaxWidth().height(lineHeight)) {
                            ZmanRow(zman, next = zman == next, clock = clock, moladClock = moladClock, onClick = { onZman(zman) })
                            if (index > pages[shown].first) {
                                DottedDivider(Modifier.align(Alignment.TopCenter).padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
            }
            if (!monthOpen && pages.size > 1) {
                SegmentedBar(pages.size, shown, onPage, Modifier.fillMaxHeight())
            }
        }
    }
}

/** The day's details: both dates, its name, the reader's events, the parsha and daf, and the town. */
@Composable
private fun Details(
    day: Day,
    afterShkia: Boolean,
    withYear: Boolean,
    dayEvents: List<Event>,
    place: Place,
    textLines: List<String>,
    lines: DetailLines,
    onEvent: (Event) -> Unit,
    onPlaces: () -> Unit,
) {
    val strings = LocalStrings.current
    val english = strings.dayTitle(day.date, withYear)
    // The date in the app's own language comes first.
    val (first, second) = if (strings.language == Language.Hebrew) day.hebrewDate to english else english to day.hebrewDate
    Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = lines.padding / 2, bottom = lines.padding / 2)) {
        Row(Modifier.fillMaxWidth().height(lines.dates), verticalAlignment = Alignment.CenterVertically) {
            TextMMD(
                text = first,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            TextMMD(
                text = second,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        if (afterShkia) {
            Box(Modifier.fillMaxWidth().height(lines.afterShkia), contentAlignment = Alignment.CenterStart) {
                TextMMD(text = strings.afterShkia(day.tonightHebrewDate), style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
        }
        if (day.special.isNotBlank()) {
            Box(Modifier.fillMaxWidth().height(lines.special), contentAlignment = Alignment.CenterStart) {
                TextMMD(
                    text = day.special,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                )
            }
        }
        dayEvents.forEach { event ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(lines.event)
                    .clickable { onEvent(event) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(6.dp).background(MaterialTheme.colorScheme.onSurface, CircleShape))
                Spacer(Modifier.width(8.dp))
                TextMMD(
                    text = strings.eventLine(event),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        textLines.forEach { text ->
            Box(Modifier.fillMaxWidth().height(lines.text), contentAlignment = Alignment.CenterStart) {
                TextMMD(text = text, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(lines.town)
                .clickable(onClickLabel = strings.changeLocation, onClick = onPlaces),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Symbols.Pin,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(6.dp))
            TextMMD(
                text = strings.placeName(place),
                style = MaterialTheme.typography.bodySmall,
                textDecoration = TextDecoration.Underline,
            )
        }
    }
}

/**
 * One zman: its name, the opinion it follows under it, and the time. A star means there are
 * other opinions, which a tap shows. Special lines are grey, and the next zman to come today
 * is in bold with a bar beside it.
 */
@Composable
private fun ZmanRow(zman: Zman, next: Boolean, clock: SimpleDateFormat, moladClock: SimpleDateFormat, onClick: () -> Unit) {
    val strings = LocalStrings.current
    val weight = if (next) FontWeight.Bold else null
    val molad = zman.molad
    val name = if (molad != null) strings.moladTitle(molad) else strings.zmanName(zman.kind)
    val note = if (molad != null) strings.moladWhen(molad, moladClock) else strings.note(zman)
    val tappable = zman.kind == ZmanKind.Omer || zman.opinions.size > 1
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(if (zman.kind.special) Grey else Color.Transparent)
            .then(if (tappable) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextMMD(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = weight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (zman.opinions.size > 1) {
                    Icon(
                        imageVector = Symbols.Star,
                        contentDescription = strings.otherOpinions,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 4.dp).size(13.dp),
                    )
                }
            }
            TextMMD(
                text = note,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        TextMMD(
            text = zman.time?.let { clock.format(it) } ?: "",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = weight,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

/** The bar along the bottom: Month, Today, Add and the settings. Month is black while the month is open. */
@Composable
private fun BottomBar(
    monthOpen: Boolean,
    onMonth: () -> Unit,
    onToday: () -> Unit,
    onAdd: () -> Unit,
    onSettings: () -> Unit,
) {
    val strings = LocalStrings.current
    Column(Modifier.fillMaxWidth()) {
        HorizontalDividerMMD(thickness = 1.dp)
        Row(Modifier.fillMaxWidth().height(56.dp)) {
            BarButton(Symbols.Month, strings.month, monthOpen, onMonth)
            BarButton(Symbols.Today, strings.today, false, onToday)
            BarButton(Symbols.Plus, strings.add, false, onAdd)
            BarButton(Symbols.Gear, strings.settings, false, onSettings)
        }
    }
}

@Composable
private fun RowScope.BarButton(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val ink = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(4.dp)
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = ink, modifier = Modifier.size(24.dp))
        TextMMD(
            text = label,
            color = ink,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, lineHeight = 14.sp),
            maxLines = 1,
        )
    }
}

/** The steps of adding or changing an event: the editor, then which Adar for a date in Adar, or a check before deleting. */
@Composable
fun EventFlow(
    start: Event,
    isNew: Boolean,
    onSave: (Event) -> Unit,
    onDelete: (Event) -> Unit,
    onClose: () -> Unit,
) {
    val strings = LocalStrings.current
    var step by remember(start) { mutableStateOf<EventStep>(EventStep.Edit(start)) }
    when (val current = step) {
        is EventStep.Edit -> EventPopup(
            event = current.event,
            isNew = isNew,
            onSave = { edited ->
                if (edited.hebrew && edited.isInAdar) {
                    step = EventStep.Adar(edited)
                } else {
                    onSave(edited.copy(adar = null))
                    onClose()
                }
            },
            onDelete = if (isNew) null else ({ step = EventStep.Delete(current.event) }),
            onDismiss = onClose,
        )
        is EventStep.Adar -> AdarPopup(
            chosen = current.event.adar ?: Events.defaultAdar(current.event.type, current.event.date),
            onSave = { choice ->
                onSave(current.event.copy(adar = choice))
                onClose()
            },
            onDismiss = { step = EventStep.Edit(current.event) },
        )
        is EventStep.Delete -> ConfirmPopup(
            question = strings.deleteQuestion,
            confirm = strings.delete,
            onConfirm = {
                onDelete(current.event)
                onClose()
            },
            onDismiss = { step = EventStep.Edit(current.event) },
        )
    }
}

private sealed interface EventStep {
    data class Edit(val event: Event) : EventStep
    data class Adar(val event: Event) : EventStep
    data class Delete(val event: Event) : EventStep
}
