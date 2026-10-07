package io.github.michael412133.zmanim

import android.content.Context

/** The one setting the app keeps: which town. */
class Prefs(context: Context) {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var place: Place
        get() = Places.byId(prefs.getString(KEY_PLACE, null))
        set(value) {
            prefs.edit().putString(KEY_PLACE, value.id).apply()
        }

    private companion object {
        const val KEY_PLACE = "place"
    }
}
