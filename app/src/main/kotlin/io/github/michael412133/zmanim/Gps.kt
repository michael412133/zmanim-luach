package io.github.michael412133.zmanim

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.TimeZone
import kotlin.coroutines.resume

/**
 * Finding where the phone is with its own GPS. This uses Android's location service directly,
 * so it needs no Google services, and the spot it finds never leaves the phone: the app has no
 * internet permission at all.
 */
object Gps {

    sealed interface Result {
        data class Found(val place: Place) : Result
        data object NoPermission : Result
        data object LocationOff : Result
        data object NoSignal : Result
    }

    /** Asked for together, so Android can offer precise or approximate location. */
    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    fun hasPermission(context: Context): Boolean =
        permissions.any { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }

    @SuppressLint("MissingPermission")
    suspend fun find(context: Context): Result {
        if (!hasPermission(context)) return Result.NoPermission
        val manager = context.getSystemService(LocationManager::class.java) ?: return Result.NoSignal
        if (!manager.isLocationEnabled) return Result.LocationOff

        val precise = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
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
        val location = recent ?: withTimeoutOrNull(SEARCH_MS) { current(context, manager, providers.first()) }
        return if (location == null) {
            Result.NoSignal
        } else {
            Result.Found(Places.gps(location.latitude, location.longitude, TimeZone.getDefault().id))
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun current(context: Context, manager: LocationManager, provider: String): Location? =
        suspendCancellableCoroutine { continuation ->
            val signal = CancellationSignal()
            continuation.invokeOnCancellation { signal.cancel() }
            manager.getCurrentLocation(provider, signal, context.mainExecutor) { location ->
                if (continuation.isActive) continuation.resume(location)
            }
        }

    private const val RECENT_MS = 30 * 60 * 1000L
    private const val SEARCH_MS = 90 * 1000L
}
