package com.tidesignal.utils

/**
 * Format height for display with units.
 *
 * @param height Height in feet (MLLW datum)
 * @param useMetric Whether to display in meters (true) or feet (false)
 * @return Formatted string with unit (e.g., "2.5 ft" or "0.8 m")
 */
fun formatHeight(height: Double, useMetric: Boolean): String {
    return if (useMetric) {
        val meters = height * 0.3048 // Convert feet to meters
        "%.1f m".format(meters)
    } else {
        "%.1f ft".format(height)
    }
}
