package io.github.michael412133.zmanim.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.divider.HorizontalDividerMMD
import com.mudita.mmd.components.text.TextMMD
import io.github.michael412133.zmanim.Paging
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * The light grey of special days and special lines. It is one of the 16 greys the e-ink screen
 * shows as they are, so it draws as a flat grey instead of a pattern of dots.
 */
val Grey = Color(0xFFDDDDDD)

/** A list row's height, grown with the phone's font size so two lines of text always fit. */
@Composable
fun rowHeight(base: Dp = 56.dp): Dp = base * max(1f, LocalDensity.current.fontScale)

/** Which way a finger moved across the screen. */
enum class Swipe { Up, Down, Left, Right }

/** Whether a sideways swipe goes forward, to the next day or month. In Hebrew the page reads the other way. */
fun Swipe.isForward(rightToLeft: Boolean): Boolean =
    (this == Swipe.Left && !rightToLeft) || (this == Swipe.Right && rightToLeft)

/** Whether a sideways swipe goes back, to the previous day or month. */
fun Swipe.isBack(rightToLeft: Boolean): Boolean =
    (this == Swipe.Right && !rightToLeft) || (this == Swipe.Left && rightToLeft)

/**
 * Watches for a swipe and reports which way it went, measured from where the finger first
 * touched to where it lifted. A short, quick swipe counts as well as a long one. A touch that
 * hardly moves is left alone, so it stays a tap; once a touch has moved far enough to be a
 * swipe, it is taken, so a row it started on does not also count it as a tap.
 */
suspend fun PointerInputScope.detectSwipes(onSwipe: (Swipe) -> Unit) {
    val distance = 24.dp.toPx()
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        var moved = Offset.Zero
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            moved = change.position - down.position
            if (moved.getDistance() > distance) change.consume()
            if (!change.pressed) break
        }
        if (moved.getDistance() > distance) {
            onSwipe(
                if (abs(moved.y) >= abs(moved.x)) {
                    if (moved.y < 0) Swipe.Up else Swipe.Down
                } else {
                    if (moved.x < 0) Swipe.Left else Swipe.Right
                },
            )
        }
    }
}

/**
 * A line of square dots between list items. Each dot is a whole number of pixels, so it
 * draws pure black on e-ink instead of a grey smear.
 */
@Composable
fun DottedDivider(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.onSurface
    Canvas(modifier.fillMaxWidth().height(2.dp)) {
        val dot = max(1, 1.dp.toPx().roundToInt()).toFloat()
        val step = dot * 3
        val top = ((size.height - dot) / 2).toInt().toFloat()
        var x = 0f
        while (x + dot <= size.width) {
            drawRect(color = color, topLeft = Offset(x, top), size = Size(dot, dot))
            x += step
        }
    }
}

/** A 48dp square button with an icon. With no action it keeps its space but draws nothing. */
@Composable
fun IconAction(icon: ImageVector, description: String, onClick: (() -> Unit)?) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        if (onClick != null) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

/** The top of a screen reached from the main one: a back arrow, the title and a black rule. */
@Composable
fun TitleBar(title: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconAction(Symbols.Previous, LocalStrings.current.back, onBack)
            TextMMD(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        HorizontalDividerMMD()
    }
}

/** A section's name in a list, in bold, with a black rule under it. */
@Composable
fun SectionHeader(title: String) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.Bottom) {
        TextMMD(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        HorizontalDividerMMD(thickness = 1.dp, modifier = Modifier.padding(top = 4.dp, bottom = 2.dp))
    }
}

/**
 * Which page of a list is showing, as a column of rounded segments down the side, one for
 * each page, with the page showing filled in. Tapping a segment turns to its page.
 */
@Composable
fun SegmentedBar(count: Int, current: Int, onPick: (Int) -> Unit, modifier: Modifier = Modifier) {
    val ink = MaterialTheme.colorScheme.onSurface
    val shape = RoundedCornerShape(3.dp)
    Column(modifier.width(22.dp).padding(vertical = 6.dp)) {
        for (index in 0 until count) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable { onPick(index) }
                    .padding(vertical = 3.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .then(
                            if (index == current) {
                                Modifier.background(ink, shape)
                            } else {
                                Modifier.border(1.dp, ink, shape)
                            },
                        ),
                )
            }
        }
    }
}

/**
 * A list that turns a page at a time instead of scrolling, which is what e-ink wants: as many
 * whole rows as fit, dotted lines between them, and the segmented bar down the side. A swipe
 * up or down turns the page too. It opens on the page holding [focus], and goes back there
 * whenever [resetKey] changes. Rows can have heights of their own through [heightOf], and a
 * section header gets no dotted line next to it.
 */
@Composable
fun <T> PagedList(
    items: List<T>,
    rowHeight: Dp,
    resetKey: Any?,
    modifier: Modifier = Modifier,
    focus: Int = 0,
    heightOf: ((T) -> Dp)? = null,
    isHeader: (T) -> Boolean = { false },
    row: @Composable (T) -> Unit,
) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val heights = items.map { heightOf?.invoke(it) ?: rowHeight }
        val pageHeight = maxHeight
        val pages = remember(heights, pageHeight) { Paging.pages(heights.map { it.value }, pageHeight.value) }
        var page by remember(resetKey, pages.size) { mutableIntStateOf(Paging.pageOf(pages, focus)) }
        val shown = page.coerceIn(0, pages.size - 1)

        // The swipe watcher runs for as long as the list is on screen, so it reaches the page
        // through this, which always holds the newest one.
        val turn by rememberUpdatedState<(Swipe) -> Unit>({ swipe ->
            if (swipe == Swipe.Up && shown < pages.size - 1) page = shown + 1
            if (swipe == Swipe.Down && shown > 0) page = shown - 1
        })

        Row(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .pointerInput(Unit) { detectSwipes { turn(it) } },
            ) {
                val range = pages[shown]
                for (index in range) {
                    val item = items[index]
                    Box(Modifier.fillMaxWidth().height(heights[index])) {
                        row(item)
                        val previous = items.getOrNull(index - 1)
                        if (index > range.first && previous != null && !isHeader(item) && !isHeader(previous)) {
                            DottedDivider(Modifier.align(Alignment.TopCenter).padding(horizontal = 16.dp))
                        }
                    }
                }
            }
            if (pages.size > 1) {
                SegmentedBar(pages.size, shown, { page = it }, Modifier.fillMaxHeight())
            }
        }
    }
}
