package org.prauga.compass.widget

import android.content.Context
import android.graphics.Bitmap
import android.view.WindowManager
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import org.prauga.compass.components.drawDialCenter
import org.prauga.compass.components.drawDialFace
import kotlin.math.min
import kotlin.math.sqrt

private const val DIAL_SIZE_DP = 240
private const val LABEL_INSET_DP = 16

fun renderCompassDial(context: Context): Bitmap {
    val density = Density(context)
    val sizePx = min(
        (DIAL_SIZE_DP * density.density).toInt(),
        sqrt(widgetBitmapCapBytes(context) / 2.0 / 4.0).toInt()
    )
    val bitmap = ImageBitmap(sizePx, sizePx)
    CanvasDrawScope().draw(
        density,
        LayoutDirection.Ltr,
        Canvas(bitmap),
        Size(sizePx.toFloat(), sizePx.toFloat())
    ) {
        inset(LABEL_INSET_DP * density.density) {
            drawDialFace(0f)
            drawDialCenter()
        }
    }
    return bitmap.asAndroidBitmap()
}

private fun widgetBitmapCapBytes(context: Context): Long {
    val bounds = context.getSystemService(WindowManager::class.java)?.currentWindowMetrics?.bounds
    if (bounds != null && bounds.width() > 0 && bounds.height() > 0) {
        return 6L * bounds.width() * bounds.height()
    }
    val metrics = context.resources.displayMetrics
    return 6L * metrics.widthPixels * metrics.heightPixels
}
