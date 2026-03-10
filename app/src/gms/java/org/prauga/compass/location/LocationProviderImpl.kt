package org.prauga.compass.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationProviderImpl(context: Context) : LocationProvider {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    private var callback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    override fun start(onLocation: (lat: Double, lng: Double, alt: Double?) -> Unit) {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(2000L)
            .build()

        val cb = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    onLocation(
                        loc.latitude,
                        loc.longitude,
                        if (loc.hasAltitude()) loc.altitude else null
                    )
                }
            }
        }
        callback = cb
        fusedClient.requestLocationUpdates(request, cb, Looper.getMainLooper())
    }

    override fun stop() {
        callback?.let { fusedClient.removeLocationUpdates(it) }
        callback = null
    }
}
