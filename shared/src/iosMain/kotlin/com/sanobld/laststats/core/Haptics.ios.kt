@file:OptIn(ExperimentalForeignApi::class)

package com.sanobld.laststats.core

import kotlinx.cinterop.ExperimentalForeignApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UISelectionFeedbackGenerator

@Composable
actual fun rememberPlatformHaptics(): (M3Haptic) -> Unit = remember {
    { type: M3Haptic ->
        when (type) {
            M3Haptic.Selection -> UISelectionFeedbackGenerator().selectionChanged()
            M3Haptic.Light -> UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight).impactOccurred()
            M3Haptic.Medium -> UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium).impactOccurred()
            M3Haptic.Heavy -> UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy).impactOccurred()
        }
    }
}
