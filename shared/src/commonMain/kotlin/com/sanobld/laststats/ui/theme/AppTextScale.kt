package com.sanobld.laststats.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/** True when the app is drawn with the dark theme. */
val LocalIsDarkTheme = staticCompositionLocalOf { false }

/**
 * Applies the person's text size on top of the system one, and makes the status
 * and navigation bar icons follow the app theme (not the phone's).
 */
@Composable
fun AppTextScale(scale: Float, dark: Boolean, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val scaled = remember(density, scale) { Density(density.density, density.fontScale * scale) }
    SystemBarsStyle(dark)
    CompositionLocalProvider(LocalDensity provides scaled, content = content)
}
