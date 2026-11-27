package com.sanobld.laststats.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.sanobld.laststats.AppContainer
import com.sanobld.laststats.data.local.Prefs

enum class M3Haptic { Selection, Light, Medium, Heavy }

/** The vibration of this platform. Does nothing where there is no motor. */
@Composable
expect fun rememberPlatformHaptics(): (M3Haptic) -> Unit

/** One place for vibration. It follows the "haptic feedback" setting. */
@Composable
fun rememberHaptics(): (M3Haptic) -> Unit {
    val platform = rememberPlatformHaptics()
    return remember(platform) {
        { type: M3Haptic ->
            val settings = AppContainer.instanceOrNull?.settings
            if (settings != null && settings.get(Prefs.hapticFeedback)) platform(type)
        }
    }
}
