package com.sanobld.laststats.core

import android.view.HapticFeedbackConstants
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

@Composable
actual fun rememberPlatformHaptics(): (M3Haptic) -> Unit {
    val view = LocalView.current
    return remember(view) {
        { type: M3Haptic ->
            val constant = when (type) {
                M3Haptic.Selection -> HapticFeedbackConstants.CLOCK_TICK
                M3Haptic.Light -> HapticFeedbackConstants.VIRTUAL_KEY
                M3Haptic.Medium -> HapticFeedbackConstants.KEYBOARD_TAP
                M3Haptic.Heavy -> HapticFeedbackConstants.LONG_PRESS
            }
            view.performHapticFeedback(constant)
        }
    }
}
