/*
 * SPDX-FileCopyrightText: Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.prauga.compass.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import org.prauga.compass.BuildConfig
import org.prauga.compass.MainActivity
import org.prauga.compass.R
import org.prauga.compass.location.LastLocation
import org.prauga.compass.location.LastLocationStore
import org.prauga.compass.location.toDms

private val MIN_DETAILS_WIDTH = 180.dp
private val REVISION_KEY = intPreferencesKey("revision")

class CompassWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val store = LastLocationStore(context)
        val dial = renderCompassDial(context)
        provideContent {
            val revision = currentState<Preferences>()[REVISION_KEY] ?: 0
            val location =
                remember(revision) { if (BuildConfig.SHOW_LOCATION_INFO) store.read() else null }
            CompassWidgetContent(dial, location)
        }
    }

    companion object {
        suspend fun refresh(context: Context) {
            val widget = CompassWidget()
            for (glanceId in
                GlanceAppWidgetManager(context).getGlanceIds(CompassWidget::class.java)) {
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[REVISION_KEY] = (prefs[REVISION_KEY] ?: 0) + 1
                }
                widget.update(context, glanceId)
            }
        }
    }
}

@Composable
private fun CompassWidgetContent(dial: Bitmap, location: LastLocation?) {
    val size = LocalSize.current
    val showDetails = size.width >= MIN_DETAILS_WIDTH
    val stacked = showDetails && size.height > size.width
    val modifier =
        GlanceModifier.fillMaxSize()
            .appWidgetBackground()
            .background(Color.Black)
            .cornerRadius(16.dp)
            .clickable(actionStartActivity<MainActivity>())
            .padding(12.dp)

    when {
        stacked ->
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CompassRose(dial, min(size.width, size.height / 2) - 24.dp)
                Spacer(modifier = GlanceModifier.height(12.dp))
                CompassDetails(location, Alignment.CenterHorizontally)
            }
        showDetails ->
            Row(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CompassRose(dial, min(size.height, size.width / 2) - 24.dp)
                Spacer(modifier = GlanceModifier.width(12.dp))
                CompassDetails(location, Alignment.Start)
            }
        else ->
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                CompassRose(dial, min(size.width, size.height) - 24.dp)
            }
    }
}

@Composable
private fun CompassRose(dial: Bitmap, size: Dp) {
    Image(
        provider = ImageProvider(dial),
        contentDescription = LocalContext.current.getString(R.string.widget_open_compass),
        modifier = GlanceModifier.size(size.coerceAtLeast(24.dp)),
    )
}

@Composable
private fun CompassDetails(location: LastLocation?, alignment: Alignment.Horizontal) {
    val context = LocalContext.current
    val primary =
        TextStyle(
            color = ColorProvider(Color.White),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    val secondary =
        TextStyle(color = ColorProvider(Color.White.copy(alpha = 0.7f)), fontSize = 12.sp)

    Column(horizontalAlignment = alignment) {
        if (location == null) {
            Text(text = context.getString(R.string.app_name), style = primary)
        } else {
            Text(text = toDms(location.latitude, "N", "S"), style = primary)
            Text(text = toDms(location.longitude, "E", "W"), style = primary)
            location.placeName?.let { Text(text = it, style = secondary, maxLines = 1) }
            location.altitude?.let { Text(text = "${it.toInt()}m Elevation", style = secondary) }
        }
    }
}
