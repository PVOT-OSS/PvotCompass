/*
 * SPDX-FileCopyrightText: Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.prauga.compass.location

import android.content.Context
import androidx.core.content.edit

data class LastLocation(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val placeName: String?
)

class LastLocationStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun read(): LastLocation? {
        if (!prefs.contains(KEY_LATITUDE) || !prefs.contains(KEY_LONGITUDE)) return null
        return LastLocation(
            latitude = Double.fromBits(prefs.getLong(KEY_LATITUDE, 0L)),
            longitude = Double.fromBits(prefs.getLong(KEY_LONGITUDE, 0L)),
            altitude = if (prefs.contains(KEY_ALTITUDE)) {
                Double.fromBits(prefs.getLong(KEY_ALTITUDE, 0L))
            } else null,
            placeName = prefs.getString(KEY_PLACE_NAME, null)
        )
    }

    fun write(location: LastLocation) {
        prefs.edit {
            putLong(KEY_LATITUDE, location.latitude.toRawBits())
            putLong(KEY_LONGITUDE, location.longitude.toRawBits())
            location.altitude?.let { putLong(KEY_ALTITUDE, it.toRawBits()) }
            location.placeName?.let { putString(KEY_PLACE_NAME, it) }
        }
    }

    private companion object {
        const val PREFS_NAME = "last_location"
        const val KEY_LATITUDE = "latitude"
        const val KEY_LONGITUDE = "longitude"
        const val KEY_ALTITUDE = "altitude"
        const val KEY_PLACE_NAME = "place_name"
    }
}
