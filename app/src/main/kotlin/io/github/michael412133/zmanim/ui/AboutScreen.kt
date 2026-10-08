package io.github.michael412133.zmanim.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.text.TextMMD
import io.github.michael412133.zmanim.BuildConfig

/** How the times are worked out, kept short enough to fit on one screen. */
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val strings = LocalStrings.current
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        TitleBar(strings.about, onBack)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TextMMD(
                text = strings.appName + " " + BuildConfig.VERSION_NAME,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
            strings.aboutParagraphs.forEach { paragraph ->
                TextMMD(text = paragraph, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
