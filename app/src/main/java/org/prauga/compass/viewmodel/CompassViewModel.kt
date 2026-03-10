package org.prauga.compass.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.prauga.compass.location.LocationProvider
import org.prauga.compass.location.LocationProviderImpl
import java.util.Locale

class CompassViewModel(
    context: Context
) : ViewModel(), SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val rotationSensor =
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val _heading = MutableStateFlow(0f)
    val heading: StateFlow<Float> = _heading

    private var _cumulative = 0f
    private val _cumulativeHeading = MutableStateFlow(0f)
    val cumulativeHeading: StateFlow<Float> = _cumulativeHeading

    private val _altitude = MutableStateFlow<Double?>(null)
    val altitude: StateFlow<Double?> = _altitude

    private val _latitude = MutableStateFlow<Double?>(null)
    val latitude: StateFlow<Double?> = _latitude

    private val _longitude = MutableStateFlow<Double?>(null)
    val longitude: StateFlow<Double?> = _longitude

    private val _placeName = MutableStateFlow<String?>(null)
    val placeName: StateFlow<String?> = _placeName

    private val locationProvider: LocationProvider = LocationProviderImpl(context)
    private val geocoder = Geocoder(context, Locale.getDefault())

    fun start() {
        sensorManager.registerListener(
            this,
            rotationSensor,
            SensorManager.SENSOR_DELAY_UI
        )
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        locationProvider.start { lat, lng, alt ->
            _latitude.value = lat
            _longitude.value = lng
            if (alt != null) _altitude.value = alt
            reverseGeocode(lat, lng)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        locationProvider.stop()
    }

    private fun reverseGeocode(lat: Double, lng: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val parts = listOfNotNull(addr.locality, addr.adminArea)
                    if (parts.isNotEmpty()) {
                        _placeName.value = parts.joinToString(", ")
                    }
                }
            } catch (_: Exception) {
                // Geocoding unavailable
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

        val orientationAngles = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        val azimuthRad = orientationAngles[0]
        val azimuthDeg = (Math.toDegrees(azimuthRad.toDouble()) + 360) % 360

        val newHeading = azimuthDeg.toFloat()
        _heading.value = newHeading

        val current = ((_cumulative % 360f) + 360f) % 360f
        var delta = newHeading - current
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f
        _cumulative += delta
        _cumulativeHeading.value = _cumulative
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
