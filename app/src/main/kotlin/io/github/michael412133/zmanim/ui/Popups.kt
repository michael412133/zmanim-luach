package io.github.michael412133.zmanim.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.mudita.mmd.components.buttons.OutlinedButtonMMD
import com.mudita.mmd.components.checkbox.CheckboxMMD
import com.mudita.mmd.components.radio_button.RadioButtonMMD
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.text_field.TextFieldMMD
import io.github.michael412133.zmanim.AdarChoice
import io.github.michael412133.zmanim.Event
import io.github.michael412133.zmanim.EventType
import io.github.michael412133.zmanim.Omer
import io.github.michael412133.zmanim.OmerWording
import io.github.michael412133.zmanim.Zman
import io.github.michael412133.zmanim.ZmanKind
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.util.Date

/**
 * A pop-up with no dimmed backdrop, the way wander's Kompakt apps draw them: a white panel
 * with a black rim. The stock dim is a sheet of grey over the whole screen, which on e-ink
 * repaints everything behind the pop-up and leaves it muddy afterwards.
 *
 * A pop-up is a window of its own, which would take its reading direction from the phone
 * rather than from the app, so the app's language and direction are handed on to it here.
 */
@Composable
fun EInkDialog(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val strings = LocalStrings.current
    val direction = LocalLayoutDirection.current
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val view = LocalView.current
        SideEffect {
            (view.parent as? DialogWindowProvider)?.window?.setDimAmount(0f)
        }
        CompositionLocalProvider(LocalStrings provides strings, LocalLayoutDirection provides direction) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    content = content,
                )
            }
        }
    }
}

@Composable
private fun PopupTitle(text: String) {
    TextMMD(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun PopupNote(text: String) {
    TextMMD(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(top = 8.dp),
    )
}

/** The buttons along the bottom of a pop-up, side by side and the same width. */
@Composable
private fun PopupButtons(vararg buttons: Pair<String, () -> Unit>) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        buttons.forEach { (label, onClick) ->
            OutlinedButtonMMD(onClick = onClick, modifier = Modifier.weight(1f).height(48.dp)) {
                TextMMD(text = label, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
        }
    }
}

/** One choice in a list of choices: a round button and its words. */
@Composable
private fun OptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButtonMMD(selected = selected, onClick = null)
        Spacer(Modifier.width(10.dp))
        TextMMD(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else null,
        )
    }
}

/** A box to tap among a few, black when it is the one chosen. */
@Composable
private fun Chip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val ink = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .height(44.dp)
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent, shape)
            .border(1.dp, MaterialTheme.colorScheme.onSurface, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        TextMMD(
            text = label,
            color = ink,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (selected) FontWeight.Bold else null,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

/** Choosing one of a few options, with Cancel and Save. Nothing changes until Save. */
@Composable
fun <T> ChoicePopup(
    title: String,
    options: List<T>,
    chosen: T,
    label: (T) -> String,
    note: String?,
    onSave: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = LocalStrings.current
    var picked by remember { mutableStateOf(chosen) }
    EInkDialog(onDismiss = onDismiss) {
        PopupTitle(title)
        options.forEach { option -> OptionRow(label(option), option == picked) { picked = option } }
        if (note != null) PopupNote(note)
        PopupButtons(
            strings.cancel to onDismiss,
            strings.save to {
                onSave(picked)
                onDismiss()
            },
        )
    }
}

/**
 * Every opinion for one line, each with its own time, the one shown on the list in bold with a
 * check. A Kiddush Levana time that falls on another day has the day in front of it.
 */
@Composable
fun OpinionsPopup(zman: Zman, day: LocalDate, clock: SimpleDateFormat, onDismiss: () -> Unit) {
    val strings = LocalStrings.current
    EInkDialog(onDismiss = onDismiss) {
        PopupTitle(strings.zmanName(zman.kind))
        zman.opinions.forEach { (opinion, time) ->
            val chosen = opinion == zman.opinion
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) {
                    if (chosen) {
                        Icon(
                            imageVector = Symbols.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Spacer(Modifier.width(6.dp))
                TextMMD(
                    text = strings.opinionName(opinion),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (chosen) FontWeight.Bold else null,
                )
                TextMMD(
                    text = timeLabel(time, day, clock, strings),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (chosen) FontWeight.Bold else null,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        PopupNote(strings.changeDefaultHint)
        PopupButtons(strings.done to onDismiss)
    }
}

/** A time for the opinions pop-up, with its day in front when it is not on the day being shown. */
private fun timeLabel(time: Date?, day: LocalDate, clock: SimpleDateFormat, strings: Strings): String {
    if (time == null) return "--:--"
    val date = Instant.ofEpochMilli(time.time).atZone(clock.timeZone.toZoneId()).toLocalDate()
    val text = clock.format(time)
    return if (date == day) text else strings.weekdayShort(date.dayOfWeek) + " " + text
}

/** Tonight's count in the siddur's words, with its sefira. */
@Composable
fun OmerPopup(day: Int, wording: OmerWording, onDismiss: () -> Unit) {
    val strings = LocalStrings.current
    EInkDialog(onDismiss = onDismiss) {
        PopupTitle(strings.nusachTitle)
        TextMMD(
            text = Omer.count(day, wording),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Right,
        )
        TextMMD(
            text = Omer.sefira(day),
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Right,
        )
        strings.omerEnglish(day)?.let { PopupNote(it) }
        PopupButtons(strings.done to onDismiss)
    }
}

/**
 * Adding or changing one of the reader's events: what kind it is, its name, and whether it
 * comes back on the Hebrew date or the English one. The day is the one picked on the main
 * screen, shown at the top in both calendars.
 */
@Composable
fun EventPopup(
    event: Event,
    isNew: Boolean,
    onSave: (Event) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    val strings = LocalStrings.current
    val focus = LocalFocusManager.current
    var type by remember { mutableStateOf(event.type) }
    var name by remember { mutableStateOf(event.name) }
    var hebrew by remember { mutableStateOf(event.hebrew) }
    var missingName by remember { mutableStateOf(false) }

    EInkDialog(onDismiss = onDismiss) {
        PopupTitle(if (isNew) strings.addEvent else strings.editEvent)
        TextMMD(
            text = strings.dayTitle(event.date, withYear = true) + " · " + strings.hebrewDayAndMonth(event.date),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        EventType.entries.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth().padding(bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                pair.forEach { option ->
                    Chip(strings.eventTypeName(option), option == type, { type = option }, Modifier.weight(1f))
                }
            }
        }
        TextFieldMMD(
            value = name,
            onValueChange = {
                name = it
                missingName = false
            },
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            singleLine = true,
            placeholder = { TextMMD(text = strings.eventName) },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
            // Done on the keyboard puts it away: on a screen this small it covers the buttons.
            keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
        )
        if (missingName) PopupNote(strings.needsName)
        TextMMD(
            text = strings.repeatsOn,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 10.dp, bottom = 6.dp),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Chip(strings.hebrewDayAndMonth(event.date), hebrew, { hebrew = true }, Modifier.weight(1f))
            Chip(strings.englishDayAndMonth(event.date), !hebrew, { hebrew = false }, Modifier.weight(1f))
        }
        val save = {
            if (name.isBlank()) {
                missingName = true
            } else {
                focus.clearFocus()
                onSave(event.copy(type = type, name = name.trim(), hebrew = hebrew))
            }
        }
        if (onDelete != null) {
            PopupButtons(strings.delete to onDelete, strings.cancel to onDismiss, strings.save to save)
        } else {
            PopupButtons(strings.cancel to onDismiss, strings.save to save)
        }
    }
}

/** For a Hebrew date in Adar: which Adar it comes in, in a year that has two. */
@Composable
fun AdarPopup(chosen: AdarChoice, onSave: (AdarChoice) -> Unit, onDismiss: () -> Unit) {
    val strings = LocalStrings.current
    var picked by remember { mutableStateOf(chosen) }
    EInkDialog(onDismiss = onDismiss) {
        PopupTitle(strings.adarTitle)
        TextMMD(
            text = strings.adarQuestion,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        AdarChoice.entries.forEach { option -> OptionRow(strings.adarName(option), option == picked) { picked = option } }
        PopupNote(strings.adarHint)
        PopupButtons(strings.cancel to onDismiss, strings.save to { onSave(picked) })
    }
}

/** A yes or no question, like deleting an event. */
@Composable
fun ConfirmPopup(question: String, confirm: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val strings = LocalStrings.current
    EInkDialog(onDismiss = onDismiss) {
        PopupTitle(question)
        PopupButtons(strings.cancel to onDismiss, confirm to onConfirm)
    }
}

/**
 * Which lines the list shows, as a list of checks, with Cancel and Save. It is longer than a
 * pop-up can hold, so it turns a page at a time like the other lists.
 */
@Composable
fun ChecklistPopup(
    title: String,
    items: List<ZmanKind>,
    hidden: Set<ZmanKind>,
    label: (ZmanKind) -> String,
    onSave: (Set<ZmanKind>) -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = LocalStrings.current
    var off by remember { mutableStateOf(hidden) }
    val lineHeight = rowHeight(44.dp)
    // Room for the title and buttons, and as many rows as fit in the rest of the screen.
    val screen = LocalConfiguration.current.screenHeightDp.dp
    val listHeight = minOf(lineHeight * items.size, maxOf(lineHeight * 3, screen - 210.dp))
    EInkDialog(onDismiss = onDismiss) {
        PopupTitle(title)
        Box(Modifier.fillMaxWidth().height(listHeight)) {
            PagedList(items = items, rowHeight = lineHeight, resetKey = Unit) { kind ->
                val shown = kind !in off
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { off = if (shown) off + kind else off - kind },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CheckboxMMD(checked = shown, onCheckedChange = null)
                    Spacer(Modifier.width(8.dp))
                    TextMMD(
                        text = label(kind),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        PopupButtons(
            strings.cancel to onDismiss,
            strings.save to {
                onSave(off)
                onDismiss()
            },
        )
    }
}
