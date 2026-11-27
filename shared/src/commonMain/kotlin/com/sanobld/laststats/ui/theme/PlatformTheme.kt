package com.sanobld.laststats.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

/** True while the system "remove animations" setting is on. */
@Composable
expect fun rememberSystemAnimationsDisabled(): State<Boolean>

/** Colour scheme made from the wallpaper (Android 12 and later), else null. */
@Composable
expect fun systemColorScheme(dark: Boolean): ColorScheme?

/** Makes the status and navigation bar icons readable on the app theme. */
@Composable
expect fun SystemBarsStyle(dark: Boolean)
