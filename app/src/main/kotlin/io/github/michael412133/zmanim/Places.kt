package io.github.michael412133.zmanim

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * The towns to choose from. Coordinates are from each town's Wikipedia entry; at these
 * distances a hundredth of a degree moves a zman by about two seconds, so the town
 * center is close enough for every street in it.
 */
object Places {

    val all: List<Place> = listOf(
        Place("airmont", "Airmont, NY", 41.09916, -74.10001),
        Place("monsey", "Monsey, NY", 41.11944, -74.06583),
        Place("new-hempstead", "New Hempstead, NY", 41.14595, -74.04664),
        Place("new-square", "New Square, NY", 41.13972, -74.02833),
        Place("pomona", "Pomona, NY", 41.18650, -74.05542),
        Place("spring-valley", "Spring Valley, NY", 41.11444, -74.04777),
        Place("suffern", "Suffern, NY", 41.11194, -74.14583),
        Place("wesley-hills", "Wesley Hills, NY", 41.15580, -74.07526),
        Place("kiryas-joel", "Kiryas Joel, NY", 41.34000, -74.16722),
        Place("lakewood", "Lakewood, NJ", 40.07707, -74.19851),
        Place("boro-park", "Boro Park, Brooklyn", 40.63389, -73.99306),
        Place("crown-heights", "Crown Heights, Brooklyn", 40.66940, -73.94240),
        Place("flatbush", "Flatbush, Brooklyn", 40.64150, -73.95940),
        Place("williamsburg", "Williamsburg, Brooklyn", 40.70810, -73.95710),
    )

    val default: Place = all.first { it.id == "monsey" }

    fun byId(id: String?): Place = all.firstOrNull { it.id == id } ?: default

    /** The spot the GPS found. Its time zone is the phone's, taken when the spot was found. */
    fun gps(latitude: Double, longitude: Double, timeZone: String): Place = Place(
        id = Place.GPS_ID,
        name = "My location",
        latitude = latitude,
        longitude = longitude,
        timeZone = timeZone,
        inIsrael = timeZone == "Asia/Jerusalem" || timeZone == "Asia/Tel_Aviv",
    )

    /** The town on the list nearest a spot, if one is within [withinKm]. */
    fun nearest(latitude: Double, longitude: Double, withinKm: Double = 30.0): Place? =
        all.map { it to distanceKm(latitude, longitude, it.latitude, it.longitude) }
            .filter { it.second <= withinKm }
            .minByOrNull { it.second }
            ?.first

    /** Distance along the ground between two spots, in kilometers. */
    fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return 2 * earthRadiusKm * asin(sqrt(a))
    }
}
