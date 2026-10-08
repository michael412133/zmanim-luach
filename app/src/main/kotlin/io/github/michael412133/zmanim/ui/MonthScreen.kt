package io.github.michael412133.zmanim.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.mudita.mmd.components.divider.HorizontalDividerMMD
import com.mudita.mmd.components.text.TextMMD
import io.github.michael412133.zmanim.CalendarMonth
import io.github.michael412133.zmanim.MonthStyle
import io.github.michael412133.zmanim.Months

/**
 * Jumping straight to any month: the year at the top with arrows, and that year's months as
 * buttons. The month being looked at is black. A swipe sideways or up and down changes the year.
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
        val forward = swipe == Swipe.Up || swipe.isForward(rightToLeft)
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
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (chosen) MaterialTheme.colorScheme.primary else Color.Transparent, shape)
            .border(1.dp, MaterialTheme.colorScheme.onSurface, shape)
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
