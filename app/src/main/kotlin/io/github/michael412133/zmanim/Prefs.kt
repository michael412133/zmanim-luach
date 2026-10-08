package io.github.michael412133.zmanim

import android.content.Context

/** The language the whole app is shown in. */
enum class Language { English, Hebrew }

/** The settings the app keeps: the town (or the GPS spot), the language and the month view. */
class Prefs(context: Context) {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var place: Place
        get() {
            val id = prefs.getString(KEY_PLACE, null)
            return if (id == Place.GPS_ID) gpsPlace ?: Places.default else Places.byId(id)
        }
        set(value) {
            prefs.edit().putString(KEY_PLACE, value.id).apply()
        }

    /** The last spot the GPS found, kept even while a town from the list is chosen. */
    val gpsPlace: Place?
        get() {
            if (!prefs.contains(KEY_GPS_LATITUDE)) return null
            return Places.gps(
                latitude = prefs.getFloat(KEY_GPS_LATITUDE, 0f).toDouble(),
                longitude = prefs.getFloat(KEY_GPS_LONGITUDE, 0f).toDouble(),
                timeZone = prefs.getString(KEY_GPS_TIME_ZONE, null) ?: Places.default.timeZone,
            )
        }

    /** When the GPS spot was found, in milliseconds, or 0 if never. */
    val gpsUpdatedAt: Long
        get() = prefs.getLong(KEY_GPS_UPDATED, 0L)

    fun saveGps(place: Place, updatedAt: Long) {
        prefs.edit()
            .putFloat(KEY_GPS_LATITUDE, place.latitude.toFloat())
            .putFloat(KEY_GPS_LONGITUDE, place.longitude.toFloat())
            .putString(KEY_GPS_TIME_ZONE, place.timeZone)
            .putLong(KEY_GPS_UPDATED, updatedAt)
            .apply()
    }

    var language: Language
        get() = Language.entries.firstOrNull { it.name == prefs.getString(KEY_LANGUAGE, null) } ?: Language.English
        set(value) {
            prefs.edit().putString(KEY_LANGUAGE, value.name).apply()
        }

    var monthStyle: MonthStyle
        get() = MonthStyle.entries.firstOrNull { it.name == prefs.getString(KEY_MONTH_STYLE, null) } ?: MonthStyle.English
        set(value) {
            prefs.edit().putString(KEY_MONTH_STYLE, value.name).apply()
        }

    private companion object {
        const val KEY_PLACE = "place"
        const val KEY_LANGUAGE = "language"
        const val KEY_MONTH_STYLE = "month_style"
        const val KEY_GPS_LATITUDE = "gps_latitude"
        const val KEY_GPS_LONGITUDE = "gps_longitude"
        const val KEY_GPS_TIME_ZONE = "gps_time_zone"
        const val KEY_GPS_UPDATED = "gps_updated"
    }
}
