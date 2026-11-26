package com.sanobld.laststats.core

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import com.sanobld.laststats.AppContainer
import com.sanobld.laststats.data.local.Prefs

enum class M3Haptic { Selection, Light, Medium, Heavy }

/** One place for vibration. It follows the "haptic feedback" setting. */
object Haptics {
    fun perform(view: View, type: M3Haptic = M3Haptic.Selection) {
        val settings = AppContainer.instanceOrNull?.settings ?: return
        if (!settings.get(Prefs.hapticFeedback)) return
        val constant = when (type) {
            M3Haptic.Selection -> HapticFeedbackConstants.CLOCK_TICK
            M3Haptic.Light -> HapticFeedbackConstants.VIRTUAL_KEY
            M3Haptic.Medium -> HapticFeedbackConstants.KEYBOARD_TAP
            M3Haptic.Heavy -> HapticFeedbackConstants.LONG_PRESS
        }
        view.performHapticFeedback(constant)
    }
}

@Composable
fun rememberHaptics(): (M3Haptic) -> Unit {
    val view = LocalView.current
    return remember(view) { { type: M3Haptic -> Haptics.perform(view, type) } }
}
