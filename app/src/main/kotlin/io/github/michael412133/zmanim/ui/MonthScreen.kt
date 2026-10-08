package io.github.michael412133.zmanim.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.buttons.OutlinedButtonMMD
import com.mudita.mmd.components.divider.HorizontalDividerMMD
import com.mudita.mmd.components.text.TextMMD
import io.github.michael412133.zmanim.CalendarMonth
import io.github.michael412133.zmanim.MonthCell
import io.github.michael412133.zmanim.MonthStyle
import io.github.michael412133.zmanim.Months
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * A whole month, Sunday to Shabbos, like a wall calendar. Every square has both dates; the
 * one the month goes by is big. Today is black, the day being looked at has a frame, and Yom
 * Tov, Rosh Chodesh and fast days have a dot. The arrows or a swipe go a month at a time, the
 * month's name opens the chooser, and a square opens that day's zmanim.
 */
@Composable
fun MonthScreen(
    month: CalendarMonth,
    selected: LocalDate,
    today: LocalDate,
    inIsrael: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onChooseMonth: () -> Unit,
    onPick: (LocalDate) -> Unit,
    onToday: () -> Unit,
) {
    val strings = LocalStrings.current
    val cells = remember(month, inIsrael) { Months.cells(month, inIsrael) }
    val rightToLeft = strings.direction == LayoutDirection.Rtl

    // Up, or toward where the page reads from, is the next month; down or back is the one before.
    val turn by rememberUpdatedState<(Swipe) -> Unit>({ swipe ->
        val forward = swipe == Swipe.Up ||
            (swipe == Swipe.Left && !rightToLeft) || (swipe == Swipe.Right && rightToLeft)
        if (forward) onNext() else onPrevious()
    })

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconAction(Symbols.Previous, strings.previousMonth, onPrevious)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(onClickLabel = strings.chooseMonth, onClick = onChooseMonth),
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
            IconAction(Symbols.Next, strings.nextMonth, onNext)
        }
        HorizontalDividerMMD()

        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            strings.weekdays.forEach { name ->
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

        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(Unit) { detectSwipes { turn(it) } },
        ) {
            val weekHeight = maxHeight / month.weeks
            Column {
                for (week in 0 until month.weeks) {
                    Row(Modifier.fillMaxWidth().height(weekHeight)) {
                        for (weekday in 0 until 7) {
                            val cell = cells.getOrNull(week * 7 + weekday - month.leadingBlanks)
                            Box(Modifier.weight(1f).fillMaxHeight()) {
                                if (cell != null) {
                                    DayCell(
                                        cell = cell,
                                        style = month.style,
                                        isToday = cell.date == today,
                                        isSelected = cell.date == selected,
                                        onPick = onPick,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        HorizontalDividerMMD(thickness = 1.dp)
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextMMD(
                text = strings.markedLegend,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            OutlinedButtonMMD(onClick = onToday) {
                TextMMD(text = strings.today)
            }
        }
    }
}

@Composable
private fun DayCell(
    cell: MonthCell,
    style: MonthStyle,
    isToday: Boolean,
    isSelected: Boolean,
    onPick: (LocalDate) -> Unit,
) {
    val ink = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val big = if (style == MonthStyle.English) cell.date.dayOfMonth.toString() else cell.hebrewDay
    val small = if (style == MonthStyle.English) cell.hebrewDay else cell.date.dayOfMonth.toString()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp)
            .background(if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent)
            .then(if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface) else Modifier)
            .clickable { onPick(cell.date) },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TextMMD(
                text = big,
                color = ink,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (cell.date.dayOfWeek == DayOfWeek.SATURDAY) FontWeight.Bold else null,
                maxLines = 1,
            )
            TextMMD(text = small, color = ink, style = MaterialTheme.typography.labelSmall, maxLines = 1)
            if (cell.marked) {
                Box(
                    Modifier
                        .padding(top = 2.dp)
                        .size(5.dp)
                        .background(ink, CircleShape),
                )
            }
        }
    }
}

/**
 * Jumping straight to any month: the year at the top with arrows, and that year's months as
 * buttons. The month being looked at is black.
 */
@Composable
fun MonthPickerScreen(
    style: MonthStyle,
    year: Int,
    current: CalendarMonth,
    onYear: (Int) -> Unit,
    onPick: (CalendarMonth) -> Unit,
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    val months = remember(style, year) { Months.monthsOfYear(style, year) }
    val turn by rememberUpdatedState<(Swipe) -> Unit>({ swipe ->
        val rightToLeft = strings.direction == LayoutDirection.Rtl
        val forward = swipe == Swipe.Up ||
            (swipe == Swipe.Left && !rightToLeft) || (swipe == Swipe.Right && rightToLeft)
        onYear(if (forward) year + 1 else year - 1)
    })

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        TitleBar(strings.chooseMonth, onBack)
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconAction(Symbols.Previous, strings.previousYear) { onYear(year - 1) }
            TextMMD(
                text = strings.yearName(style, year),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            IconAction(Symbols.Next, strings.nextYear) { onYear(year + 1) }
        }
        HorizontalDividerMMD(thickness = 1.dp)

        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
                .pointerInput(Unit) { detectSwipes { turn(it) } },
        ) {
            val columns = 3
            val rows = (months.size + columns - 1) / columns
            val buttonHeight = minOf(maxHeight / rows, 72.dp)
            Column {
                for (row in 0 until rows) {
                    Row(Modifier.fillMaxWidth().height(buttonHeight)) {
                        for (column in 0 until columns) {
                            val month = months.getOrNull(row * columns + column)
                            Box(Modifier.weight(1f).fillMaxHeight().padding(4.dp)) {
                                if (month != null) {
                                    MonthButton(
                                        name = strings.monthButton(month),
                                        chosen = month == current,
                                        onClick = { onPick(month) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthButton(name: String, chosen: Boolean, onClick: () -> Unit) {
    val ink = if (chosen) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (chosen) MaterialTheme.colorScheme.primary else Color.Transparent)
            .border(1.dp, MaterialTheme.colorScheme.onSurface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        TextMMD(
            text = name,
            color = ink,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
