package io.github.michael412133.zmanim

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * The towns to choose from, in the sections the list shows them in.
 *
 * Each town's coordinates are the ones printed in its Wikipedia article, with three
 * exceptions, noted next to them: Wikipedia has no coordinates of its own for the Five Towns,
 * Beit Shemesh or Petach Tikva. At these distances a hundredth of a degree moves a zman by
 * about two seconds, so the town center is close enough for every street in it.
 */
object Places {

    private const val NY = "America/New_York"
    private const val IL = "Asia/Jerusalem"

    private fun rockland(id: String, name: String, lat: Double, lon: Double) =
        Place(id, name, lat, lon, NY, region = Region.Rockland)

    private fun newYork(id: String, name: String, lat: Double, lon: Double) =
        Place(id, name, lat, lon, NY, region = Region.NewYork)

    private fun america(id: String, name: String, lat: Double, lon: Double, zone: String) =
        Place(id, name, lat, lon, zone, region = Region.America)

    private fun israel(id: String, name: String, lat: Double, lon: Double) =
        Place(id, name, lat, lon, IL, inIsrael = true, region = Region.Israel)

    private fun europe(id: String, name: String, lat: Double, lon: Double, zone: String) =
        Place(id, name, lat, lon, zone, region = Region.Europe)

    val all: List<Place> = listOf(
        rockland("airmont", "Airmont, NY", 41.09916, -74.10001),
        rockland("monsey", "Monsey, NY", 41.11944, -74.06583),
        rockland("new-hempstead", "New Hempstead, NY", 41.14595, -74.04664),
        rockland("new-square", "New Square, NY", 41.13972, -74.02833),
        rockland("pomona", "Pomona, NY", 41.18650, -74.05542),
        rockland("spring-valley", "Spring Valley, NY", 41.11444, -74.04777),
        rockland("suffern", "Suffern, NY", 41.11194, -74.14583),
        rockland("wesley-hills", "Wesley Hills, NY", 41.15580, -74.07526),
        rockland("kiryas-joel", "Kiryas Joel, NY", 41.34000, -74.16722),
        rockland("monroe", "Monroe, NY", 41.32417, -74.18694),

        newYork("boro-park", "Boro Park, Brooklyn", 40.63389, -73.99306),
        newYork("crown-heights", "Crown Heights, Brooklyn", 40.66940, -73.94240),
        newYork("flatbush", "Flatbush, Brooklyn", 40.64150, -73.95940),
        newYork("williamsburg", "Williamsburg, Brooklyn", 40.70810, -73.95710),
        newYork("queens", "Queens, NY", 40.71361, -73.82806),
        newYork("far-rockaway", "Far Rockaway, NY", 40.601, -73.757),
        // Cedarhurst, in the middle of the Five Towns.
        newYork("five-towns", "Five Towns, NY", 40.62583, -73.72833),
        newYork("lakewood", "Lakewood, NJ", 40.07707, -74.19851),
        newYork("passaic", "Passaic, NJ", 40.857552, -74.129089),
        newYork("teaneck", "Teaneck, NJ", 40.890317, -74.011478),

        america("baltimore", "Baltimore, MD", 39.28944, -76.61528, NY),
        america("chicago", "Chicago, IL", 41.88194, -87.62778, "America/Chicago"),
        america("cleveland", "Cleveland, OH", 41.4992, -81.6947, NY),
        america("los-angeles", "Los Angeles, CA", 34.050, -118.250, "America/Los_Angeles"),
        america("miami-beach", "Miami Beach, FL", 25.82556, -80.13250, NY),
        america("toronto", "Toronto, ON", 43.65250, -79.38167, "America/Toronto"),
        // Montreal keeps the same clock as Toronto, and the time zone database files it there.
        america("montreal", "Montreal, QC", 45.50889, -73.55417, "America/Toronto"),

        israel("yerushalayim", "Yerushalayim", 31.77889, 35.22556),
        israel("bnei-brak", "Bnei Brak", 32.083, 34.833),
        // From GeoNames: Wikipedia's article has no coordinates of its own.
        israel("beit-shemesh", "Beit Shemesh", 31.73072, 34.99293),
        israel("modiin-illit", "Modiin Illit", 31.93056, 35.04167),
        israel("beitar-illit", "Beitar Illit", 31.69778, 35.11556),
        israel("elad", "Elad", 32.05222, 34.95111),
        israel("tzfas", "Tzfas", 32.96583, 35.49833),
        israel("haifa", "Haifa", 32.81917, 34.99917),
        israel("tel-aviv", "Tel Aviv", 32.08000, 34.78000),
        israel("netanya", "Netanya", 32.32861, 34.85667),
        israel("ashdod", "Ashdod", 31.80000, 34.65000),
        // From GeoNames: Wikipedia's article has no coordinates of its own.
        israel("petach-tikva", "Petach Tikva", 32.0888, 34.88666),

        europe("london", "London", 51.50722, -0.12750, "Europe/London"),
        europe("manchester", "Manchester", 53.47944, -2.24528, "Europe/London"),
        europe("antwerp", "Antwerp", 51.21778, 4.40028, "Europe/Brussels"),
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
        inIsrael = timeZone == IL || timeZone == "Asia/Tel_Aviv",
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
