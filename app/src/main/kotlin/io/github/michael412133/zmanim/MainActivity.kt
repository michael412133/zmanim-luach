package io.github.michael412133.zmanim

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.mudita.mmd.ThemeMMD
import io.github.michael412133.zmanim.ui.Monochrome

class MainActivity : ComponentActivity() {

    private var resumes by mutableIntStateOf(0)
    private var freshStarts by mutableIntStateOf(0)
    private var pausedAt = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = Prefs(this)
        setContent {
            ThemeMMD(colorScheme = Monochrome) {
                ZmanimApp(prefs = prefs, resumes = resumes, freshStarts = freshStarts)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (pausedAt != 0L && SystemClock.elapsedRealtime() - pausedAt > AWAY_LONG_ENOUGH) {
            freshStarts++
        }
        resumes++
    }

    override fun onPause() {
        super.onPause()
        pausedAt = SystemClock.elapsedRealtime()
    }

    private companion object {
        /** Back after this long, the app opens on today again rather than the day last looked at. */
        const val AWAY_LONG_ENOUGH = 10 * 60 * 1000L
    }
}
