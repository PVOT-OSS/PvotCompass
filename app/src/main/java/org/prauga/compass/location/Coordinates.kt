package org.prauga.compass.location

fun toDms(decimal: Double, positive: String, negative: String): String {
    val dir = if (decimal >= 0) positive else negative
    val abs = kotlin.math.abs(decimal)
    val degrees = abs.toInt()
    val minutesFull = (abs - degrees) * 60
    val minutes = minutesFull.toInt()
    val seconds = ((minutesFull - minutes) * 60).toInt()
    return "$degrees°$minutes'$seconds\"$dir"
}
