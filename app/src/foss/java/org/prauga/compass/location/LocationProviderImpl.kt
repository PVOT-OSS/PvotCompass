package org.prauga.compass.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper

class LocationProviderImpl(context: Context) : LocationProvider {

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var listener: LocationListener? = null

    @SuppressLint("MissingPermission")
    override fun start(onLocation: (lat: Double, lng: Double, alt: Double?) -> Unit) {
        val provider = when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ->
                LocationManager.GPS_PROVIDER
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ->
                LocationManager.NETWORK_PROVIDER
            else -> return
        }

        val lst = LocationListener { loc: Location ->
            onLocation(
                loc.latitude,
                loc.longitude,
                if (loc.hasAltitude()) loc.altitude else null
            )
        }
        listener = lst
        locationManager.requestLocationUpdates(provider, 2000L, 0f, lst, Looper.getMainLooper())
    }

    override fun stop() {
        listener?.let { locationManager.removeUpdates(it) }
        listener = null
    }
}
