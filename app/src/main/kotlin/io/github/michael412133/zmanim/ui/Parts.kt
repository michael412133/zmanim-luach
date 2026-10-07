package io.github.michael412133.zmanim.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.divider.HorizontalDividerMMD
import com.mudita.mmd.components.text.TextMMD
import kotlin.math.max
import kotlin.math.roundToInt

/** The bar under every list: a label on the left, the page buttons on the right. */
private val FooterHeight = 56.dp

/** A list row's height, grown with the phone's font size so two lines of text always fit. */
@Composable
fun rowHeight(base: Dp = 56.dp): Dp = base * max(1f, LocalDensity.current.fontScale)

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

/** The top of a screen you came to from the main one: a back arrow, the title, a black rule. */
@Composable
fun TitleBar(title: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconAction(Symbols.Previous, "Back", onBack)
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

/**
 * A list that turns a page at a time instead of scrolling, which is what e-ink wants: as many
 * whole rows as fit, dotted lines between them, and the page buttons at the bottom. A swipe
 * up or down turns the page too. It opens on the page holding [focus], and goes back there
 * whenever [resetKey] changes.
 */
@Composable
fun <T> PagedList(
    items: List<T>,
    rowHeight: Dp,
    resetKey: Any?,
    modifier: Modifier = Modifier,
    focus: Int = 0,
    footer: @Composable () -> Unit = {},
    row: @Composable (T) -> Unit,
) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val perPage = max(1, ((maxHeight - FooterHeight) / rowHeight).toInt())
        val pageCount = max(1, (items.size + perPage - 1) / perPage)
        var page by remember(resetKey, perPage) {
            mutableIntStateOf((max(focus, 0) / perPage).coerceIn(0, pageCount - 1))
        }
        val shown = page.coerceIn(0, pageCount - 1)

        Column(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(pageCount) {
                        val threshold = 48.dp.toPx()
                        var dragged = 0f
                        detectVerticalDragGestures(
                            onDragStart = { dragged = 0f },
                            onDragEnd = {
                                if (dragged < -threshold) page = (page + 1).coerceAtMost(pageCount - 1)
                                if (dragged > threshold) page = (page - 1).coerceAtLeast(0)
                            },
                        ) { change, amount ->
                            change.consume()
                            dragged += amount
                        }
                    },
            ) {
                items.drop(shown * perPage).take(perPage).forEachIndexed { index, item ->
                    Box(Modifier.fillMaxWidth().height(rowHeight)) {
                        row(item)
                        if (index > 0) {
                            DottedDivider(Modifier.align(Alignment.TopCenter).padding(horizontal = 16.dp))
                        }
                    }
                }
            }

            HorizontalDividerMMD(thickness = 1.dp)
            Row(
                modifier = Modifier.fillMaxWidth().height(FooterHeight - 1.dp).padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f)) { footer() }
                if (pageCount > 1) {
                    IconAction(
                        icon = Symbols.Previous,
                        description = "Previous page",
                        onClick = if (shown > 0) ({ page = shown - 1 }) else null,
                    )
                    TextMMD(text = "${shown + 1} / $pageCount", style = MaterialTheme.typography.labelMedium)
                    IconAction(
                        icon = Symbols.Next,
                        description = "Next page",
                        onClick = if (shown < pageCount - 1) ({ page = shown + 1 }) else null,
                    )
                }
            }
        }
    }
}
