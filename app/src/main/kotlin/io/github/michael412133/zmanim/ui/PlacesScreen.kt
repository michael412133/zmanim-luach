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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.text.TextMMD
import io.github.michael412133.zmanim.Place
import io.github.michael412133.zmanim.Places

/** Choosing the town the times are worked out for. The chosen one is in bold with a check. */
@Composable
fun PlacesScreen(current: Place, onPick: (Place) -> Unit, onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        TitleBar("Location", onBack)
        PagedList(
            items = Places.all,
            rowHeight = rowHeight(52.dp),
            resetKey = current.id,
            modifier = Modifier.weight(1f),
            focus = Places.all.indexOf(current),
            footer = {
                TextMMD(
                    text = "Pick the nearest town",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
        ) { place ->
            val chosen = place.id == current.id
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onPick(place) }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextMMD(
                    text = place.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (chosen) FontWeight.Bold else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (chosen) {
                    Icon(
                        imageVector = Symbols.Check,
                        contentDescription = "Chosen",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}
