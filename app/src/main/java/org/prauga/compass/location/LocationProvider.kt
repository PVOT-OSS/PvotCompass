/*
 * SPDX-FileCopyrightText: Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.prauga.compass.location

interface LocationProvider {
    fun start(onLocation: (lat: Double, lng: Double, alt: Double?) -> Unit)

    fun stop()
}
