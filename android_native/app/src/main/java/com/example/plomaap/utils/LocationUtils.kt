package com.example.plomaap.utils

import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import com.example.plomaap.BuildConfig
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object LocationUtils {

    /**
     * Initializes and returns Google Places client safely
     */
    fun getPlacesClient(context: Context): PlacesClient? {
        return try {
            if (!Places.isInitialized()) {
                val ai = context.packageManager.getApplicationInfo(
                    context.packageName,
                    PackageManager.GET_META_DATA
                )
                val apiKey = ai.metaData?.getString("com.google.android.geo.API_KEY")
                    ?: BuildConfig.MAPS_API_KEY
                if (!apiKey.isNullOrEmpty()) {
                    Places.initializeWithNewPlacesApiEnabled(context, apiKey)
                }
            }
            if (Places.isInitialized()) Places.createClient(context) else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Obtains human-readable address from coordinates using native Android Geocoder (free of API costs).
     */
    suspend fun getAddressFromCoordinates(context: Context, latLng: LatLng): String = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale("es", "CO"))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                @Suppress("DEPRECATION")
                val list = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (!list.isNullOrEmpty()) {
                    val addr = list[0]
                    addr.getAddressLine(0) ?: "${addr.thoroughfare ?: ""} ${addr.subThoroughfare ?: ""}, ${addr.locality ?: ""}".trim()
                } else {
                    "${String.format(Locale.US, "%.5f", latLng.latitude)}, ${String.format(Locale.US, "%.5f", latLng.longitude)}"
                }
            } else {
                @Suppress("DEPRECATION")
                val list = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (!list.isNullOrEmpty()) {
                    val addr = list[0]
                    addr.getAddressLine(0) ?: "${addr.thoroughfare ?: ""} ${addr.subThoroughfare ?: ""}, ${addr.locality ?: ""}".trim()
                } else {
                    "${String.format(Locale.US, "%.5f", latLng.latitude)}, ${String.format(Locale.US, "%.5f", latLng.longitude)}"
                }
            }
        } catch (e: Exception) {
            "${String.format(Locale.US, "%.5f", latLng.latitude)}, ${String.format(Locale.US, "%.5f", latLng.longitude)}"
        }
    }
}
