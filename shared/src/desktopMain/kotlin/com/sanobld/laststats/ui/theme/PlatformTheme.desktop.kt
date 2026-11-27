package com.sanobld.laststats.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
actual fun rememberSystemAnimationsDisabled(): State<Boolean> = remember { mutableStateOf(false) }

@Composable
actual fun systemColorScheme(dark: Boolean): ColorScheme? = null

@Composable
actual fun SystemBarsStyle(dark: Boolean) {}
