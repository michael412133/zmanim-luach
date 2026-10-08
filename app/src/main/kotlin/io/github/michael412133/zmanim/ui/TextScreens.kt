package io.github.michael412133.zmanim.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.text.TextMMD
import io.github.michael412133.zmanim.BuildConfig
import io.github.michael412133.zmanim.Paging

/** How to use the app, a page at a time. */
@Composable
fun HelpScreen(onBack: () -> Unit) {
    val strings = LocalStrings.current
    TextScreen(strings.howToUse, strings.help, onBack)
}

/** How the times are worked out. */
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val strings = LocalStrings.current
    val paragraphs = listOf<Pair<String?, String>>(strings.appName + " " + BuildConfig.VERSION_NAME to "") +
        strings.aboutParagraphs.map { null to it }
    TextScreen(strings.about, paragraphs, onBack)
}

/**
 * Paragraphs that turn a page at a time instead of scrolling. Each paragraph is measured at
 * the screen's width, so as many whole paragraphs as fit go on a page, and the segmented bar
 * on the side shows which page this is. A swipe up or down turns the page.
 */
@Composable
private fun TextScreen(title: String, paragraphs: List<Pair<String?, String>>, onBack: () -> Unit) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val titleStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
    val textStyle = MaterialTheme.typography.bodySmall
    val gap = 14.dp

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        TitleBar(title, onBack)
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            // Measured at the width left beside the page bar, so a paragraph is never taller than planned.
            val width = (constraints.maxWidth - with(density) { (32.dp + 22.dp).roundToPx() }).coerceAtLeast(1)
            val heights = paragraphs.map { (heading, text) ->
                val headingHeight = if (heading.isNullOrBlank()) {
                    0
                } else {
                    measurer.measure(text = heading, style = titleStyle, constraints = Constraints(maxWidth = width)).size.height
                }
                val textHeight = if (text.isBlank()) {
                    0
                } else {
                    measurer.measure(text = text, style = textStyle, constraints = Constraints(maxWidth = width)).size.height
                }
                with(density) { (headingHeight + textHeight).toDp() } + gap
            }
            val pageHeight = maxHeight - 16.dp
            val pages = remember(heights, pageHeight) { Paging.pages(heights.map { it.value }, pageHeight.value) }
            var page by remember(pages.size) { mutableIntStateOf(0) }
            val shown = page.coerceIn(0, pages.size - 1)
            val turn by rememberUpdatedState<(Swipe) -> Unit>({ swipe ->
                if (swipe == Swipe.Up && shown < pages.size - 1) page = shown + 1
                if (swipe == Swipe.Down && shown > 0) page = shown - 1
            })

            Row(Modifier.fillMaxSize()) {
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                        .pointerInput(Unit) { detectSwipes { turn(it) } },
                ) {
                    for (index in pages[shown]) {
                        val (heading, text) = paragraphs[index]
                        Column(Modifier.fillMaxWidth().height(heights[index])) {
                            if (!heading.isNullOrBlank()) {
                                TextMMD(text = heading, style = titleStyle)
                            }
                            if (text.isNotBlank()) {
                                TextMMD(text = text, style = textStyle)
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
}
