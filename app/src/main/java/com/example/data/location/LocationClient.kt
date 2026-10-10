package com.example.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.example.data.model.UserLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

class LocationClient(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { continuation ->
        try {
            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                if (continuation.isActive) {
                    continuation.resume(location)
                }
            }.addOnFailureListener {
                if (continuation.isActive) {
                    // Fallback to last known location
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                        continuation.resume(lastLoc)
                    }.addOnFailureListener {
                        continuation.resume(null)
                    }
                }
            }

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        } catch (e: Exception) {
            if (continuation.isActive) {
                continuation.resume(null)
            }
        }
    }

    suspend fun reverseGeocode(latitude: Double, longitude: Double): UserLocation = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                var resolved: UserLocation? = null
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        val address = addresses.firstOrNull()
                        val cityName = address?.locality ?: address?.subAdminArea ?: address?.adminArea ?: "My Location"
                        val countryName = address?.countryName ?: ""
                        val admin1 = address?.adminArea
                        val userLoc = UserLocation(
                            id = "current_gps",
                            name = cityName,
                            country = countryName,
                            admin1 = admin1,
                            latitude = latitude,
                            longitude = longitude,
                            isCurrentLocation = true,
                            isDefault = true
                        )
                        if (cont.isActive) cont.resume(userLoc)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val address = addresses?.firstOrNull()
                val cityName = address?.locality ?: address?.subAdminArea ?: address?.adminArea ?: "My Location"
                val countryName = address?.countryName ?: ""
                val admin1 = address?.adminArea
                UserLocation(
                    id = "current_gps",
                    name = cityName,
                    country = countryName,
                    admin1 = admin1,
                    latitude = latitude,
                    longitude = longitude,
                    isCurrentLocation = true,
                    isDefault = true
                )
            }
        } catch (e: Exception) {
            UserLocation(
                id = "current_gps",
                name = "Current Location",
                country = "",
                admin1 = null,
                latitude = latitude,
                longitude = longitude,
                isCurrentLocation = true,
                isDefault = true
            )
        }
    }
}
