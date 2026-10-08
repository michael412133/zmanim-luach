package io.github.michael412133.zmanim.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
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

    // Material Icons (Apache 2.0), drawn on a 24 grid.
    private fun icon(name: String, pathData: String): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        )
            .addPath(
                pathData = PathParser().parsePathString(pathData).toNodes(),
                fill = SolidColor(Color.Black),
            )
            .build()

    val Previous: ImageVector =
        symbol("Previous", "M560-240 320-480l240-240 56 56-184 184 184 184-56 56Z", mirrored = true)

    val Next: ImageVector =
        symbol("Next", "M504-480 320-664l56-56 240 240-240 240-56-56 184-184Z", mirrored = true)

    val Check: ImageVector =
        symbol("Check", "M382-240 154-468l57-57 171 171 367-367 57 57-424 424Z", mirrored = false)

    /** A month on a wall calendar, with a row of days. */
    val Month: ImageVector = icon(
        "Month",
        "M9 11H7v2h2v-2zm4 0h-2v2h2v-2zm4 0h-2v2h2v-2zm2-7h-1V2h-2v2H8V2H6v2H5c-1.11 0-1.99.9-1.99 2L3 20" +
            "c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V9h14v11z",
    )

    /** A calendar page with one day filled in. */
    val Today: ImageVector = icon(
        "Today",
        "M19 3h-1V1h-2v2H8V1H6v2H5c-1.11 0-1.99.9-1.99 2L3 19c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5" +
            "c0-1.1-.9-2-2-2zm0 16H5V8h14v11zM7 10h5v5H7z",
    )

    val Plus: ImageVector = icon("Plus", "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z")

    val Gear: ImageVector = icon(
        "Gear",
        "M19.14,12.94c0.04-0.3,0.06-0.61,0.06-0.94c0-0.32-0.02-0.64-0.07-0.94l2.03-1.58c0.18-0.14,0.23-0.41," +
            "0.12-0.61l-1.92-3.32c-0.12-0.22-0.37-0.29-0.59-0.22l-2.39,0.96c-0.5-0.38-1.03-0.7-1.62-0.94L14.4,2.81" +
            "c-0.04-0.24-0.24-0.41-0.48-0.41h-3.84c-0.24,0-0.43,0.17-0.47,0.41L9.25,5.35C8.66,5.59,8.12,5.92,7.63,6.29" +
            "L5.24,5.33c-0.22-0.08-0.47,0-0.59,0.22L2.74,8.87C2.62,9.08,2.66,9.34,2.86,9.48l2.03,1.58" +
            "C4.84,11.36,4.8,11.69,4.8,12s0.02,0.64,0.07,0.94l-2.03,1.58c-0.18,0.14-0.23,0.41-0.12,0.61l1.92,3.32" +
            "c0.12,0.22,0.37,0.29,0.59,0.22l2.39-0.96c0.5,0.38,1.03,0.7,1.62,0.94l0.36,2.54c0.05,0.24,0.24,0.41,0.48,0.41" +
            "h3.84c0.24,0,0.44-0.17,0.47-0.41l0.36-2.54c0.59-0.24,1.13-0.56,1.62-0.94l2.39,0.96c0.22,0.08,0.47,0,0.59-0.22" +
            "l1.92-3.32c0.12-0.22,0.07-0.47-0.12-0.61L19.14,12.94z" +
            "M12,15.6c-1.98,0-3.6-1.62-3.6-3.6s1.62-3.6,3.6-3.6s3.6,1.62,3.6,3.6S13.98,15.6,12,15.6z",
    )

    /** A map pin, for the town under the date. */
    val Pin: ImageVector = icon(
        "Pin",
        "M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5" +
            "c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z",
    )

    /** Next to a time that has other opinions. */
    val Star: ImageVector = icon(
        "Star",
        "M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z",
    )
}
