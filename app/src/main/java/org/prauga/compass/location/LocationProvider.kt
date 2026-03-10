package org.prauga.compass.location

interface LocationProvider {
    fun start(onLocation: (lat: Double, lng: Double, alt: Double?) -> Unit)
    fun stop()
}
