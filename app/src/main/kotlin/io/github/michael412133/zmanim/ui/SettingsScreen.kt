package io.github.michael412133.zmanim.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
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
import com.mudita.mmd.components.text.TextMMD
import io.github.michael412133.zmanim.Group
import io.github.michael412133.zmanim.Language
import io.github.michael412133.zmanim.MonthStyle
import io.github.michael412133.zmanim.OmerWording
import io.github.michael412133.zmanim.Opinion
import io.github.michael412133.zmanim.Opinions
import io.github.michael412133.zmanim.Place
import io.github.michael412133.zmanim.ZmanKind

/** A line of the settings list. */
private sealed interface SettingRow {
    data class Header(val title: String) : SettingRow
    data object Location : SettingRow
    data object LanguageChoice : SettingRow
    data object MonthView : SettingRow
    data class OpinionChoice(val group: Group) : SettingRow
    data object Omer : SettingRow
    data object TimesToShow : SettingRow
    data object MyEvents : SettingRow
    data object HowToUse : SettingRow
    data object About : SettingRow
}

/** What the settings have open on top of them. */
private sealed interface SettingPopup {
    data object LanguageChoice : SettingPopup
    data object MonthView : SettingPopup
    data class OpinionChoice(val group: Group) : SettingPopup
    data object Omer : SettingPopup
    data object TimesToShow : SettingPopup
}

/**
 * The settings, in three sections. Every choice opens a pop-up with Cancel and Save, and
 * nothing changes until Save. The town, the events, how to use and about open pages of their
 * own.
 */
@Composable
fun SettingsScreen(
    place: Place,
    language: Language,
    monthStyle: MonthStyle,
    opinions: Opinions,
    omerWording: OmerWording,
    hidden: Set<ZmanKind>,
    eventCount: Int,
    onPlaces: () -> Unit,
    onLanguage: (Language) -> Unit,
    onMonthStyle: (MonthStyle) -> Unit,
    onOpinion: (Opinion) -> Unit,
    onOmerWording: (OmerWording) -> Unit,
    onHidden: (Set<ZmanKind>) -> Unit,
    onEvents: () -> Unit,
    onHelp: () -> Unit,
    onAbout: () -> Unit,
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    var popup by remember { mutableStateOf<SettingPopup?>(null) }

    val rows = buildList {
        add(SettingRow.Header(strings.general))
        add(SettingRow.Location)
        add(SettingRow.LanguageChoice)
        add(SettingRow.MonthView)
        add(SettingRow.Header(strings.opinions))
        Group.entries.forEach { add(SettingRow.OpinionChoice(it)) }
        add(SettingRow.Omer)
        add(SettingRow.Header(strings.more))
        add(SettingRow.TimesToShow)
        add(SettingRow.MyEvents)
        add(SettingRow.HowToUse)
        add(SettingRow.About)
    }
    val lineHeight = rowHeight(60.dp)
    val headerHeight = rowHeight(40.dp)

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        TitleBar(strings.settings, onBack)
        PagedList(
            items = rows,
            rowHeight = lineHeight,
            resetKey = Unit,
            modifier = Modifier.weight(1f),
            heightOf = { if (it is SettingRow.Header) headerHeight else lineHeight },
            isHeader = { it is SettingRow.Header },
        ) { row ->
            when (row) {
                is SettingRow.Header -> SectionHeader(row.title)
                SettingRow.Location -> SettingLine(strings.location, strings.placeName(place), opens = true, onPlaces)
                SettingRow.LanguageChoice -> SettingLine(strings.languageTitle, strings.languageName, opens = false) {
                    popup = SettingPopup.LanguageChoice
                }
                SettingRow.MonthView -> SettingLine(strings.monthView, strings.monthStyleName(monthStyle), opens = false) {
                    popup = SettingPopup.MonthView
                }
                is SettingRow.OpinionChoice -> SettingLine(
                    strings.groupName(row.group),
                    strings.opinionName(opinions[row.group]),
                    opens = false,
                ) { popup = SettingPopup.OpinionChoice(row.group) }
                SettingRow.Omer -> SettingLine(strings.omerWordingTitle, omerWordingLabel(omerWording), opens = false) {
                    popup = SettingPopup.Omer
                }
                SettingRow.TimesToShow -> SettingLine(strings.timesToShow, strings.hiddenCount(hidden.size), opens = false) {
                    popup = SettingPopup.TimesToShow
                }
                SettingRow.MyEvents -> SettingLine(strings.myEvents, strings.eventCount(eventCount), opens = true, onEvents)
                SettingRow.HowToUse -> SettingLine(strings.howToUse, strings.howToUseNote, opens = true, onHelp)
                SettingRow.About -> SettingLine(strings.about, strings.aboutNote, opens = true, onAbout)
            }
        }
    }

    val close = { popup = null }
    when (val open = popup) {
        SettingPopup.LanguageChoice -> ChoicePopup(
            title = strings.languageTitle,
            options = Language.entries,
            chosen = language,
            label = { strings.languageOption(it) },
            note = null,
            onSave = onLanguage,
            onDismiss = close,
        )
        SettingPopup.MonthView -> ChoicePopup(
            title = strings.monthView,
            options = MonthStyle.entries,
            chosen = monthStyle,
            label = { strings.monthStyleName(it) },
            note = null,
            onSave = onMonthStyle,
            onDismiss = close,
        )
        is SettingPopup.OpinionChoice -> ChoicePopup(
            title = strings.groupName(open.group),
            options = open.group.opinions,
            chosen = opinions[open.group],
            label = { strings.opinionName(it) },
            note = strings.groupNote(open.group),
            onSave = onOpinion,
            onDismiss = close,
        )
        SettingPopup.Omer -> ChoicePopup(
            title = strings.omerWordingTitle,
            options = OmerWording.entries,
            chosen = omerWording,
            label = { omerWordingLabel(it) },
            note = null,
            onSave = onOmerWording,
            onDismiss = close,
        )
        SettingPopup.TimesToShow -> ChecklistPopup(
            title = strings.timesToShow,
            items = ZmanKind.toggles,
            hidden = hidden,
            label = { strings.zmanName(it) },
            onSave = onHidden,
            onDismiss = close,
        )
        null -> Unit
    }
}

/** How the omer is counted, in the words of the count itself. */
private fun omerWordingLabel(wording: OmerWording): String = when (wording) {
    OmerWording.La -> "לעומר"
    OmerWording.Ba -> "בעומר"
}

/** A setting's name, its value under it, and an arrow when it opens another page. */
@Composable
private fun SettingLine(title: String, value: String, opens: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            TextMMD(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            TextMMD(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (opens) {
            Icon(
                imageVector = Symbols.Next,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
