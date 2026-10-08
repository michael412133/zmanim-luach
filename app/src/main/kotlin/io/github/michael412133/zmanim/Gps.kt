package io.github.michael412133.zmanim

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.SystemClock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import java.util.TimeZone

/**
 * Finding where the phone is with its own GPS. This uses Android's location service directly,
 * so it needs no Google services, and the spot it finds never leaves the phone: the app has no
 * internet permission at all.
 *
 * In 0.2 this asked Android for "the current location", which Android 12 gives up on after 30
 * seconds. With no Google services there is no help from cell towers or wifi, so a GPS starting
 * cold needs longer than that to hear enough satellites. Now it keeps listening for up to five
 * minutes, and reports how many satellites it hears while it waits.
 */
object Gps {

    sealed interface Result {
        data class Found(val place: Place) : Result
        data object NoPermission : Result
        data object LocationOff : Result
        data object NoSignal : Result

        /** Only approximate location is allowed, and that found nothing. Precise location uses the GPS. */
        data object NeedsPrecise : Result
    }

    /** How the search is going: satellites heard, satellites good enough to use, and seconds so far. */
    data class Progress(val heard: Int, val used: Int, val seconds: Int)

    /** Asked for together, so Android can offer precise or approximate location. */
    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    fun hasPermission(context: Context): Boolean =
        permissions.any { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }

    private fun hasPrecise(context: Context): Boolean =
        context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    suspend fun find(context: Context, onProgress: (Progress) -> Unit = {}): Result {
        if (!hasPermission(context)) return Result.NoPermission
        val manager = context.getSystemService(LocationManager::class.java) ?: return Result.NoSignal
        if (!manager.isLocationEnabled) return Result.LocationOff

        val precise = hasPrecise(context)
        val providers = buildList {
            if (precise) add(LocationManager.GPS_PROVIDER)
            add(LocationManager.FUSED_PROVIDER)
            add(LocationManager.NETWORK_PROVIDER)
        }.filter { provider -> runCatching { manager.isProviderEnabled(provider) }.getOrDefault(false) }
        if (providers.isEmpty()) return Result.LocationOff

        // A spot the phone found in the last half hour, for this app or another, is close enough.
        val recent = providers
            .mapNotNull { runCatching { manager.getLastKnownLocation(it) }.getOrNull() }
            .filter { System.currentTimeMillis() - it.time < RECENT_MS }
            .maxByOrNull { it.time }
        val location = recent ?: listen(context, manager, providers, precise, onProgress)
        return when {
            location != null -> Result.Found(Places.gps(location.latitude, location.longitude, TimeZone.getDefault().id))
            precise -> Result.NoSignal
            else -> Result.NeedsPrecise
        }
    }

    /** Waits for the first spot from any of the providers, for up to five minutes. */
    @SuppressLint("MissingPermission")
    private suspend fun listen(
        context: Context,
        manager: LocationManager,
        providers: List<String>,
        precise: Boolean,
        onProgress: (Progress) -> Unit,
    ): Location? {
        val started = SystemClock.elapsedRealtime()
        val fix = CompletableDeferred<Location>()
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                fix.complete(location)
            }
        }
        // Hearing satellites needs precise location; with approximate only, the count is skipped.
        val satellites = if (!precise) {
            null
        } else {
            object : GnssStatus.Callback() {
                override fun onSatelliteStatusChanged(status: GnssStatus) {
                    var used = 0
                    for (i in 0 until status.satelliteCount) if (status.usedInFix(i)) used++
                    val seconds = ((SystemClock.elapsedRealtime() - started) / 1000).toInt()
                    onProgress(Progress(status.satelliteCount, used, seconds))
                }
            }
        }
        try {
            for (provider in providers) {
                runCatching { manager.requestLocationUpdates(provider, 1_000L, 0f, context.mainExecutor, listener) }
            }
            if (satellites != null) {
                runCatching { manager.registerGnssStatusCallback(context.mainExecutor, satellites) }
            }
            return withTimeoutOrNull(SEARCH_MS) { fix.await() }
        } finally {
            runCatching { manager.removeUpdates(listener) }
            if (satellites != null) runCatching { manager.unregisterGnssStatusCallback(satellites) }
        }
    }

    private const val RECENT_MS = 30 * 60 * 1000L
    const val SEARCH_MS = 5 * 60 * 1000L
}
