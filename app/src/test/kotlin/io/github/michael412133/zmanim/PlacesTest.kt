package io.github.michael412133.zmanim

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class PlacesTest {

    @Test
    fun everyTownIsComplete() {
        assertEquals(Places.all.size, Places.all.map { it.id }.toSet().size)
        for (place in Places.all) {
            // An unknown time zone would quietly turn into GMT, so each one must be a real one.
            assertEquals(place.timeZone, TimeZone.getTimeZone(place.timeZone).id)
            assertTrue(place.latitude in -90.0..90.0 && place.longitude in -180.0..180.0)
            assertEquals(place.region == Region.Israel, place.inIsrael)
            assertEquals(place.region == Region.Israel, place.timeZone == "Asia/Jerusalem")
        }
    }

    @Test
    fun theSections() {
        assertEquals(42, Places.all.size)
        assertEquals(listOf(Region.Rockland, Region.NewYork, Region.America, Region.Israel, Region.Europe), Places.all.map { it.region }.distinct())
        assertEquals("monsey", Places.default.id)
        assertEquals(Places.default, Places.byId("no such town"))
    }

    @Test
    fun nearestTown() {
        assertEquals("airmont", Places.nearest(41.1002, -74.0985)?.id)
        assertEquals("yerushalayim", Places.nearest(31.778, 35.235)?.id)
        assertEquals(null, Places.nearest(0.0, 0.0))
    }

    @Test
    fun theGpsSpotInIsraelKeepsIsraelsYomTov() {
        assertTrue(Places.gps(31.78, 35.22, "Asia/Jerusalem").inIsrael)
        assertTrue(!Places.gps(41.1, -74.1, "America/New_York").inIsrael)
    }
}
