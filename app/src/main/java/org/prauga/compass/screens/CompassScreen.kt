/*
 * SPDX-FileCopyrightText: Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.prauga.compass.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass
import org.prauga.compass.BuildConfig
import org.prauga.compass.components.CompassDial
import org.prauga.compass.location.toDms
import org.prauga.compass.viewmodel.CompassViewModel

@Composable
fun CompassScreen(viewModel: CompassViewModel) {

    val heading by viewModel.heading.collectAsState()
    val cumulativeHeading by viewModel.cumulativeHeading.collectAsState()
    val altitude by viewModel.altitude.collectAsState()
    val latitude by viewModel.latitude.collectAsState()
    val longitude by viewModel.longitude.collectAsState()
    val placeName by viewModel.placeName.collectAsState()
    val showLocationInfo = BuildConfig.SHOW_LOCATION_INFO
    val animatedHeading by
        animateFloatAsState(
            targetValue = cumulativeHeading,
            animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing),
        )

    // Location permission
    var locationGranted by rememberSaveable { mutableStateOf(false) }
    var locationRequested by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            locationGranted = granted
            if (granted) viewModel.startLocationUpdates()
        }

    // Haptic feedback
    val haptic = LocalHapticFeedback.current
    val currentSlot = (heading / 30f).toInt()
    var lastSlot by remember { mutableIntStateOf(currentSlot) }
    LaunchedEffect(currentSlot) {
        if (currentSlot != lastSlot) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            lastSlot = currentSlot
        }
    }

    LaunchedEffect(Unit) {
        viewModel.start()
        if (showLocationInfo) {
            if (locationGranted) {
                viewModel.startLocationUpdates()
            } else if (!locationRequested) {
                locationRequested = true
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
    DisposableEffect(Unit) { onDispose { viewModel.stop() } }

    CompassLayout(
        dialHeading = animatedHeading,
        heading = heading,
        showLocation = showLocationInfo && locationGranted,
        latitude = latitude,
        longitude = longitude,
        placeName = placeName,
        altitude = altitude,
    )
}

@Composable
private fun CompassLayout(
    dialHeading: Float,
    heading: Float,
    showLocation: Boolean,
    latitude: Double?,
    longitude: Double?,
    placeName: String?,
    altitude: Double?,
) {
    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    val compactHeight =
        !windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)
    val expandedWidth =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
    val expandedHeight =
        windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND)
    val large =
        !compactHeight &&
            windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    val sideBySide = compactHeight || (expandedWidth && !expandedHeight)

    val maxDialSize = if (large) 520.dp else 360.dp
    val headingFontSize = if (large) 56.sp else 42.sp
    val detailFontSize = if (large) 22.sp else 18.sp

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(Color.Black).safeDrawingPadding()
    ) {
        val dialSize =
            if (sideBySide) {
                minOf(maxHeight * 0.85f, maxWidth * 0.5f, maxDialSize)
            } else {
                minOf(maxWidth * 0.9f, maxHeight * 0.6f, maxDialSize)
            }
        val readings =
            @Composable {
                CompassReadings(
                    heading = heading,
                    showLocation = showLocation,
                    latitude = latitude,
                    longitude = longitude,
                    placeName = placeName,
                    altitude = altitude,
                    headingFontSize = headingFontSize,
                    detailFontSize = detailFontSize,
                )
            }

        if (sideBySide) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CompassDial(heading = dialHeading, modifier = Modifier.size(dialSize))
                readings()
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CompassDial(heading = dialHeading, modifier = Modifier.size(dialSize))
                Spacer(modifier = Modifier.height(24.dp))
                readings()
            }
        }
    }
}

@Composable
private fun CompassReadings(
    heading: Float,
    showLocation: Boolean,
    latitude: Double?,
    longitude: Double?,
    placeName: String?,
    altitude: Double?,
    headingFontSize: TextUnit,
    detailFontSize: TextUnit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "${heading.toInt()}° ${direction(heading)}",
            color = Color.White,
            fontSize = headingFontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        if (showLocation) {
            // Coordinates in DMS
            if (latitude != null && longitude != null) {
                Text(
                    text = "${toDms(latitude, "N", "S")} ${toDms(longitude, "E", "W")}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = detailFontSize,
                    textAlign = TextAlign.Center,
                )
            }
            // Place name
            placeName?.let {
                Text(
                    text = it,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = detailFontSize,
                    textAlign = TextAlign.Center,
                )
            }
            // Sea level
            val altText = altitude?.let { "${it.toInt()}m Elevation" } ?: "Acquiring altitude…"
            Text(
                text = altText,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = detailFontSize,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(name = "Phone", device = Devices.PHONE)
@Preview(name = "Phone landscape", device = "spec:width=891dp,height=411dp")
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.TABLET)
@Composable
private fun CompassLayoutPreview() {
    CompassLayout(
        dialHeading = 297f,
        heading = 297f,
        showLocation = true,
        latitude = 51.4779,
        longitude = -0.0015,
        placeName = "Greenwich, England",
        altitude = 46.0,
    )
}

private fun direction(deg: Float): String {
    val dirs = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    return dirs[((deg + 22.5f) / 45f).toInt() % 8]
}
