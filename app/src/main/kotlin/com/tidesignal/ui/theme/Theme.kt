package com.tidesignal.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme

/**
 * TideSignal app theme.
 *
 * Applies Material You color scheme and typography optimized for WearOS.
 *
 * @param isAmbient Whether the device is in Always-On Display (ambient) mode
 * @param content Composable content to theme
 */
@Composable
fun TideSignalTheme(
    isAmbient: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (isAmbient) AodColors else TideSignalColors

    MaterialTheme(
        colors = colors,
        typography = TideSignalTypography,
        content = content
    )
}
