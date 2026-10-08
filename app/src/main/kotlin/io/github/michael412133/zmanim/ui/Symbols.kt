package io.github.michael412133.zmanim.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** The few icons the app draws. Everything else on the screen is words. */
object Symbols {

    // Material Symbols (Apache 2.0) are drawn on a 960 grid that runs from -960 to 0
    // vertically, so each drawing is moved down by 960 to land in the grid Compose uses.
    // Arrows are mirrored in Hebrew, so "back" and "next" point the way the page reads.
    private fun symbol(name: String, pathData: String, mirrored: Boolean): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f,
            autoMirror = mirrored,
        )
            .addGroup(name = name, translationY = 960f)
            .addPath(
                pathData = PathParser().parsePathString(pathData).toNodes(),
                fill = SolidColor(Color.Black),
            )
            .clearGroup()
            .build()

    val Previous: ImageVector =
        symbol("Previous", "M560-240 320-480l240-240 56 56-184 184 184 184-56 56Z", mirrored = true)

    val Next: ImageVector =
        symbol("Next", "M504-480 320-664l56-56 240 240-240 240-56-56 184-184Z", mirrored = true)

    val Check: ImageVector =
        symbol("Check", "M382-240 154-468l57-57 171 171 367-367 57 57-424 424Z", mirrored = false)

    /** A page of a wall calendar: a box, a band across the top and two rings. */
    val Calendar: ImageVector = ImageVector.Builder(
        name = "Calendar",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Square) {
        moveTo(4f, 6f)
        lineTo(20f, 6f)
        lineTo(20f, 20f)
        lineTo(4f, 20f)
        close()
        moveTo(4f, 10f)
        lineTo(20f, 10f)
        moveTo(8f, 3f)
        lineTo(8f, 6f)
        moveTo(16f, 3f)
        lineTo(16f, 6f)
    }.path(fill = SolidColor(Color.Black)) {
        moveTo(7f, 13f)
        lineTo(10f, 13f)
        lineTo(10f, 16f)
        lineTo(7f, 16f)
        close()
    }.build()

    /** Three sliders, for the settings. */
    val Settings: ImageVector = ImageVector.Builder(
        name = "Settings",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f) {
        moveTo(3f, 6f)
        lineTo(21f, 6f)
        moveTo(3f, 12f)
        lineTo(21f, 12f)
        moveTo(3f, 18f)
        lineTo(21f, 18f)
    }.path(fill = SolidColor(Color.Black)) {
        moveTo(6f, 3f)
        lineTo(10f, 3f)
        lineTo(10f, 9f)
        lineTo(6f, 9f)
        close()
        moveTo(14f, 9f)
        lineTo(18f, 9f)
        lineTo(18f, 15f)
        lineTo(14f, 15f)
        close()
        moveTo(8f, 15f)
        lineTo(12f, 15f)
        lineTo(12f, 21f)
        lineTo(8f, 21f)
        close()
    }.build()
}
