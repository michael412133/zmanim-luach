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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.text.TextMMD
import io.github.michael412133.zmanim.MonthStyle
import io.github.michael412133.zmanim.Place

private enum class Setting { Location, Language, MonthView, About }

/**
 * The settings: the town, the language, how the month view is divided, and the page about how
 * the times are worked out. Language and month view switch with a tap, and the change shows
 * at once.
 */
@Composable
fun SettingsScreen(
    place: Place,
    monthStyle: MonthStyle,
    onPlaces: () -> Unit,
    onSwitchLanguage: () -> Unit,
    onSwitchMonthStyle: () -> Unit,
    onAbout: () -> Unit,
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        TitleBar(strings.settings, onBack)
        PagedList(
            items = Setting.entries,
            rowHeight = rowHeight(64.dp),
            resetKey = Unit,
            modifier = Modifier.weight(1f),
        ) { setting ->
            when (setting) {
                Setting.Location -> SettingRow(strings.location, strings.placeName(place), opens = true, onPlaces)
                Setting.Language -> SettingRow(strings.languageTitle, strings.languageName, opens = false, onSwitchLanguage)
                Setting.MonthView -> SettingRow(
                    strings.monthView,
                    strings.monthStyleName(monthStyle),
                    opens = false,
                    onSwitchMonthStyle,
                )
                Setting.About -> SettingRow(strings.about, strings.aboutNote, opens = true, onAbout)
            }
        }
    }
}

/** A setting's name, its value under it, and an arrow when it opens another page. */
@Composable
private fun SettingRow(title: String, value: String, opens: Boolean, onClick: () -> Unit) {
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
