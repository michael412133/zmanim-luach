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
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        TitleBar("About these times", onBack)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextMMD(
                text = "Zmanim & Luach ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
            Paragraph(
                "Every time is worked out on this phone with the KosherJava zmanim library. " +
                    "Nothing is sent anywhere, and no internet is needed.",
            )
            Paragraph("Netz and shkia are at sea level, the way most luchos in America print them.")
            Paragraph(
                "Times are rounded to the safe side. A deadline, like sof zman krias shema, is shown " +
                    "a minute earlier, and a starting time, like tzeis, a minute later.",
            )
            Paragraph("For halacha l'maaseh, follow your rav and your shul's luach.")
            Paragraph("github.com/michael412133/zmanim-luach")
            Paragraph("Made with KosherJava Zmanim (LGPL 2.1) and Mudita Mindful Design (Apache 2.0).")
        }
    }
}

@Composable
private fun Paragraph(text: String) {
    TextMMD(text = text, style = MaterialTheme.typography.bodySmall)
}
