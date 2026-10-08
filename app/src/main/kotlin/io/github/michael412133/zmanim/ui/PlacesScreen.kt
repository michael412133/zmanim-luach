package io.github.michael412133.zmanim.ui

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.text.TextMMD
import io.github.michael412133.zmanim.Gps
import io.github.michael412133.zmanim.Place
import io.github.michael412133.zmanim.Places
import io.github.michael412133.zmanim.Region
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

/** A row of the town list: a section's name, the GPS spot, or a town. */
private sealed interface PlaceRow {
    data class Header(val region: Region?) : PlaceRow
    data object MyLocation : PlaceRow
    data class Town(val place: Place) : PlaceRow
}

private enum class GpsState { Idle, Searching, Off, Denied, NoSignal, NeedsPrecise }

/**
 * Choosing where the times are for: "My location" from the phone's GPS, or a town from the
 * list, in sections. The chosen one is in bold with a check. Android asks for the location
 * permission only when "My location" is tapped. While the GPS looks, the line under it counts
 * the satellites it hears; tapping it again stops looking.
 */
@Composable
fun PlacesScreen(
    current: Place,
    savedGps: Place?,
    gpsUpdatedAt: Long,
    onPick: (Place) -> Unit,
    onGpsFound: (Place) -> Unit,
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var gps by remember { mutableStateOf(GpsState.Idle) }
    var progress by remember { mutableStateOf<Gps.Progress?>(null) }
    var search by remember { mutableStateOf<Job?>(null) }

    fun find() {
        gps = GpsState.Searching
        progress = null
        search = scope.launch {
            val result = Gps.find(context) { progress = it }
            gps = when (result) {
                is Gps.Result.Found -> GpsState.Idle
                is Gps.Result.LocationOff -> GpsState.Off
                is Gps.Result.NoPermission -> GpsState.Denied
                is Gps.Result.NoSignal -> GpsState.NoSignal
                is Gps.Result.NeedsPrecise -> GpsState.NeedsPrecise
            }
            if (result is Gps.Result.Found) onGpsFound(result.place)
        }
    }

    // The screen stays on while the GPS looks. Asleep, Android would let it hear only now and then.
    val view = LocalView.current
    val searching = gps == GpsState.Searching
    DisposableEffect(searching) {
        view.keepScreenOn = searching
        onDispose { view.keepScreenOn = false }
    }

    val askPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        gps = GpsState.Denied
        if (Gps.hasPermission(context)) find()
    }

    val onMyLocation: () -> Unit = {
        val allowed = Gps.hasPermission(context)
        when {
            // Tapping again while it looks stops looking.
            gps == GpsState.Searching -> {
                search?.cancel()
                gps = GpsState.Idle
            }
            // Asked once and refused: Android will not ask again, so open the app's settings.
            !allowed && gps == GpsState.Denied -> openAppSettings(context)
            !allowed -> askPermission.launch(Gps.permissions)
            gps == GpsState.NeedsPrecise -> openAppSettings(context)
            gps == GpsState.Off && !isLocationOn(context) -> openLocationSettings(context)
            else -> find()
        }
    }

    val gpsNote = when (gps) {
        GpsState.Searching -> progress?.let { strings.gpsProgress(it) } ?: strings.gpsSearching
        GpsState.Off -> strings.gpsOff
        GpsState.Denied -> strings.gpsDenied
        GpsState.NoSignal -> strings.gpsNoSignal
        GpsState.NeedsPrecise -> strings.gpsNeedsPrecise
        GpsState.Idle -> if (savedGps != null && gpsUpdatedAt > 0) {
            strings.gpsUpdated(Instant.ofEpochMilli(gpsUpdatedAt).atZone(ZoneId.systemDefault()).toLocalDate())
        } else {
            strings.gpsHint
        }
    }

    val rows = remember {
        buildList<PlaceRow> {
            add(PlaceRow.Header(null))
            add(PlaceRow.MyLocation)
            Places.all.groupBy { it.region }.forEach { (region, towns) ->
                add(PlaceRow.Header(region))
                towns.forEach { add(PlaceRow.Town(it)) }
            }
        }
    }
    val focus = if (current.isGps) 1 else rows.indexOfFirst { it is PlaceRow.Town && it.place.id == current.id }
    val lineHeight = rowHeight(56.dp)
    val headerHeight = rowHeight(40.dp)

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        TitleBar(strings.location, onBack)
        PagedList(
            items = rows,
            rowHeight = lineHeight,
            resetKey = current.id,
            modifier = Modifier.weight(1f),
            focus = focus,
            heightOf = { if (it is PlaceRow.Header) headerHeight else lineHeight },
            isHeader = { it is PlaceRow.Header },
        ) { row ->
            when (row) {
                is PlaceRow.Header -> SectionHeader(row.region?.let { strings.regionName(it) } ?: strings.myLocation)
                PlaceRow.MyLocation -> ChoiceRow(
                    title = if (savedGps != null) strings.placeName(savedGps) else strings.myLocation + " (GPS)",
                    note = gpsNote,
                    chosen = current.isGps,
                    onClick = onMyLocation,
                )
                is PlaceRow.Town -> ChoiceRow(
                    title = strings.townName(row.place),
                    note = null,
                    chosen = row.place.id == current.id,
                    onClick = { onPick(row.place) },
                )
            }
        }
    }
}

@Composable
private fun ChoiceRow(title: String, note: String?, chosen: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            TextMMD(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (chosen) FontWeight.Bold else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (note != null) {
                TextMMD(
                    text = note,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (chosen) {
            Icon(
                imageVector = Symbols.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

private fun isLocationOn(context: Context): Boolean =
    context.getSystemService(LocationManager::class.java)?.isLocationEnabled == true

private fun openLocationSettings(context: Context) {
    runCatching {
        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

private fun openAppSettings(context: Context) {
    runCatching {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
