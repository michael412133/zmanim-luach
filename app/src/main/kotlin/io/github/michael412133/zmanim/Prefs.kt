package io.github.michael412133.zmanim

import android.content.Context

/** The language the whole app is shown in. */
enum class Language { English, Hebrew }

/**
 * Everything the app keeps on the phone: the town (or the GPS spot), the language, the month
 * view, the opinion chosen for each group of zmanim, the times hidden from the list, and the
 * reader's own events.
 */
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

    /** One saved name per group, like "opinion_Alos" = "Alos90". A group never saved uses its first opinion. */
    var opinions: Opinions
        get() {
            var opinions = Opinions.Default
            for (group in Group.entries) {
                val name = prefs.getString(KEY_OPINION + group.name, null) ?: continue
                val opinion = Opinion.entries.firstOrNull { it.name == name && it.group == group } ?: continue
                opinions = opinions.with(opinion)
            }
            return opinions
        }
        set(value) {
            val edit = prefs.edit()
            for (group in Group.entries) edit.putString(KEY_OPINION + group.name, value[group].name)
            edit.apply()
        }

    var omerWording: OmerWording
        get() = OmerWording.entries.firstOrNull { it.name == prefs.getString(KEY_OMER, null) } ?: OmerWording.La
        set(value) {
            prefs.edit().putString(KEY_OMER, value.name).apply()
        }

    /** The kinds of lines left out of the list, by the names in [ZmanKind.toggles]. */
    var hidden: Set<ZmanKind>
        get() = prefs.getStringSet(KEY_HIDDEN, emptySet()).orEmpty()
            .mapNotNull { name -> ZmanKind.entries.firstOrNull { it.name == name } }
            .toSet()
        set(value) {
            prefs.edit().putStringSet(KEY_HIDDEN, value.map { it.name }.toSet()).apply()
        }

    var events: List<Event>
        get() = Events.decode(prefs.getString(KEY_EVENTS, null))
        set(value) {
            prefs.edit().putString(KEY_EVENTS, Events.encode(value)).apply()
        }

    private companion object {
        const val KEY_PLACE = "place"
        const val KEY_LANGUAGE = "language"
        const val KEY_MONTH_STYLE = "month_style"
        const val KEY_GPS_LATITUDE = "gps_latitude"
        const val KEY_GPS_LONGITUDE = "gps_longitude"
        const val KEY_GPS_TIME_ZONE = "gps_time_zone"
        const val KEY_GPS_UPDATED = "gps_updated"
        const val KEY_OPINION = "opinion_"
        const val KEY_OMER = "omer_wording"
        const val KEY_HIDDEN = "hidden_times"
        const val KEY_EVENTS = "events"
    }
}
