package com.sanobld.laststats.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily

private fun TextStyle.with(family: FontFamily): TextStyle = copy(fontFamily = family)

/** Material 3 text styles using the font the person chose. Null keeps the system font. */
fun appTypography(family: FontFamily?): Typography {
    val base = Typography()
    if (family == null) return base
    return base.copy(
        displayLarge = base.displayLarge.with(family),
        displayMedium = base.displayMedium.with(family),
        displaySmall = base.displaySmall.with(family),
        headlineLarge = base.headlineLarge.with(family),
        headlineMedium = base.headlineMedium.with(family),
        headlineSmall = base.headlineSmall.with(family),
        titleLarge = base.titleLarge.with(family),
        titleMedium = base.titleMedium.with(family),
        titleSmall = base.titleSmall.with(family),
        bodyLarge = base.bodyLarge.with(family),
        bodyMedium = base.bodyMedium.with(family),
        bodySmall = base.bodySmall.with(family),
        labelLarge = base.labelLarge.with(family),
        labelMedium = base.labelMedium.with(family),
        labelSmall = base.labelSmall.with(family),
    )
}
