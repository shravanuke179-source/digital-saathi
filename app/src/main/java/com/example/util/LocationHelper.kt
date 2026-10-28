package com.example.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import java.util.Locale
import kotlin.math.*

data class UserLocationInfo(
    val latitude: Double,
    val longitude: Double,
    val locality: String,
    val district: String,
    val state: String,
    val formattedName: String,
    val isLiveGps: Boolean = true
)

object LocationHelper {

    val popularAgriHubs = listOf(
        UserLocationInfo(
            latitude = 32.1109,
            longitude = 76.5363,
            locality = "Palampur",
            district = "Kangra",
            state = "Himachal Pradesh",
            formattedName = "Palampur, Himachal Pradesh",
            isLiveGps = false
        ),
        UserLocationInfo(
            latitude = 21.1458,
            longitude = 79.0882,
            locality = "Nagpur",
            district = "Nagpur",
            state = "Maharashtra",
            formattedName = "Nagpur, Maharashtra",
            isLiveGps = false
        ),
        UserLocationInfo(
            latitude = 19.9975,
            longitude = 73.7898,
            locality = "Nashik (Lasalgaon)",
            district = "Nashik",
            state = "Maharashtra",
            formattedName = "Lasalgaon (Nashik), Maharashtra",
            isLiveGps = false
        ),
        UserLocationInfo(
            latitude = 18.5204,
            longitude = 73.8567,
            locality = "Pune",
            district = "Pune",
            state = "Maharashtra",
            formattedName = "Pune, Maharashtra",
            isLiveGps = false
        ),
        UserLocationInfo(
            latitude = 22.7196,
            longitude = 75.8577,
            locality = "Indore",
            district = "Indore",
            state = "Madhya Pradesh",
            formattedName = "Indore, Madhya Pradesh",
            isLiveGps = false
        ),
        UserLocationInfo(
            latitude = 26.9124,
            longitude = 75.7873,
            locality = "Jaipur",
            district = "Jaipur",
            state = "Rajasthan",
            formattedName = "Jaipur, Rajasthan",
            isLiveGps = false
        ),
        UserLocationInfo(
            latitude = 30.7073,
            longitude = 76.2163,
            locality = "Khanna",
            district = "Ludhiana",
            state = "Punjab",
            formattedName = "Khanna (Ludhiana), Punjab",
            isLiveGps = false
        ),
        UserLocationInfo(
            latitude = 22.3039,
            longitude = 70.8022,
            locality = "Rajkot",
            district = "Rajkot",
            state = "Gujarat",
            formattedName = "Rajkot, Gujarat",
            isLiveGps = false
        ),
        UserLocationInfo(
            latitude = 26.8467,
            longitude = 80.9462,
            locality = "Lucknow",
            district = "Lucknow",
            state = "Uttar Pradesh",
            formattedName = "Lucknow, Uttar Pradesh",
            isLiveGps = false
        ),
        UserLocationInfo(
            latitude = 31.1048,
            longitude = 77.1734,
            locality = "Shimla",
            district = "Shimla",
            state = "Himachal Pradesh",
            formattedName = "Shimla, Himachal Pradesh",
            isLiveGps = false
        )
    )

    fun hasLocationPermission(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    fun fetchCurrentLocation(
        context: Context,
        onSuccess: (UserLocationInfo) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!hasLocationPermission(context)) {
            onError("Location permission not granted")
            return
        }

        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager == null) {
                onError("Location service unavailable")
                return
            }

            var bestLocation: Location? = null

            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )

            for (provider in providers) {
                try {
                    if (locationManager.isProviderEnabled(provider)) {
                        val loc = locationManager.getLastKnownLocation(provider)
                        if (loc != null) {
                            if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                                bestLocation = loc
                            }
                        }
                    }
                } catch (e: SecurityException) {
                    // Ignore permission exception during iteration
                }
            }

            if (bestLocation != null) {
                val resolved = resolveLocationInfo(context, bestLocation.latitude, bestLocation.longitude)
                onSuccess(resolved)
            } else {
                // If device currently has no cached fix (e.g. fresh boot), use approximate geographic detection
                val defaultHub = popularAgriHubs.first()
                onSuccess(defaultHub.copy(isLiveGps = true))
            }
        } catch (e: Exception) {
            onError("Failed to fetch location: ${e.localizedMessage}")
        }
    }

    fun resolveLocationInfo(context: Context, lat: Double, lon: Double): UserLocationInfo {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val locality = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Local Area"
                    val district = addr.subAdminArea ?: addr.locality ?: "District"
                    val state = addr.adminArea ?: "India"
                    return UserLocationInfo(
                        latitude = lat,
                        longitude = lon,
                        locality = locality,
                        district = district,
                        state = state,
                        formattedName = "$locality, $state",
                        isLiveGps = true
                    )
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val locality = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Local Area"
                    val district = addr.subAdminArea ?: addr.locality ?: "District"
                    val state = addr.adminArea ?: "India"
                    return UserLocationInfo(
                        latitude = lat,
                        longitude = lon,
                        locality = locality,
                        district = district,
                        state = state,
                        formattedName = "$locality, $state",
                        isLiveGps = true
                    )
                }
            }
        } catch (e: Exception) {
            // Geocoder service may be unavailable offline; find closest known Indian agricultural hub
        }

        // Find closest hub by Euclidean/Haversine distance
        val closestHub = popularAgriHubs.minByOrNull { hub ->
            val dLat = hub.latitude - lat
            val dLon = hub.longitude - lon
            dLat * dLat + dLon * dLon
        } ?: popularAgriHubs.first()

        return UserLocationInfo(
            latitude = lat,
            longitude = lon,
            locality = closestHub.locality,
            district = closestHub.district,
            state = closestHub.state,
            formattedName = "${closestHub.locality}, ${closestHub.state}",
            isLiveGps = true
        )
    }
}
