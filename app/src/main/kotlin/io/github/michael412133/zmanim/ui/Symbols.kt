package io.github.michael412133.zmanim.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/** The three icons the app draws, from Material Symbols (Apache 2.0). Everything else is words. */
object Symbols {

    // Material Symbols are drawn on a 960 grid that runs from -960 to 0 vertically, so each
    // drawing is moved down by 960 to land in the grid Compose uses.
    private fun symbol(name: String, pathData: String): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f,
        )
            .addGroup(name = name, translationY = 960f)
            .addPath(
                pathData = PathParser().parsePathString(pathData).toNodes(),
                fill = SolidColor(Color.Black),
            )
            .clearGroup()
            .build()

    val Previous: ImageVector = symbol("Previous", "M560-240 320-480l240-240 56 56-184 184 184 184-56 56Z")

    val Next: ImageVector = symbol("Next", "M504-480 320-664l56-56 240 240-240 240-56-56 184-184Z")

    val Check: ImageVector = symbol("Check", "M382-240 154-468l57-57 171 171 367-367 57 57-424 424Z")
}
