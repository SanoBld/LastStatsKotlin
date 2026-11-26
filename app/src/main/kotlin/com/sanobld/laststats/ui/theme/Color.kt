package com.sanobld.laststats.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.google.android.material.color.utilities.Blend
import com.google.android.material.color.utilities.DynamicScheme
import com.google.android.material.color.utilities.Hct
import com.google.android.material.color.utilities.SchemeNeutral
import com.google.android.material.color.utilities.SchemeTonalSpot
import com.google.android.material.color.utilities.SchemeVibrant
import com.google.android.material.color.utilities.TonalPalette

val DefaultAccent = Color(0xFF7C3AED)

/** Named accents shown in the pickers, in display order. */
val AccentSwatches: Map<String, Color> = linkedMapOf(
    "purple" to Color(0xFF7C3AED),
    "blue" to Color(0xFF1D4ED8),
    "green" to Color(0xFF059669),
    "red" to Color(0xFFDC2626),
    "orange" to Color(0xFFD97706),
    "pink" to Color(0xFFDB2777),
    "teal" to Color(0xFF0F766E),
    "neutral" to Color(0xFF607D8B),
)

/** Accepts a named key ("purple", "blue"...) or a hex code "#RRGGBB". */
fun accentFromString(s: String?): Color {
    if (s.isNullOrEmpty()) return DefaultAccent
    if (s.startsWith("#") && s.length == 7) {
        val v = s.substring(1).toLongOrNull(16)
        if (v != null) return Color((0xFF000000L or v).toInt())
    }
    return AccentSwatches[s] ?: DefaultAccent
}

/** Uppercase "#RRGGBB". */
fun colorToHex(c: Color): String {
    val argb = c.toArgb()
    return String.format("#%02X%02X%02X", (argb shr 16) and 0xFF, (argb shr 8) and 0xFF, argb and 0xFF)
}

// ── HSL helpers ──────────────────────────────────────────────────────────────

private class Hsl(val h: Float, val s: Float, val l: Float)

private fun Color.toHsl(): Hsl {
    val out = FloatArray(3)
    ColorUtils.colorToHSL(toArgb(), out)
    return Hsl(out[0], out[1], out[2])
}

private fun hsl(h: Float, s: Float, l: Float): Color =
    Color(ColorUtils.HSLToColor(floatArrayOf(h, s.coerceIn(0f, 1f), l.coerceIn(0f, 1f))))

/** True for colours with (near) zero saturation: black, white and greys. */
fun isNeutralColor(c: Color): Boolean = c.toHsl().s < 0.03f

/** Same rule as Flutter's estimateBrightnessForColor. */
fun isDarkColor(c: Color): Boolean {
    val v = c.luminance() + 0.05f
    return v * v <= 0.15f
}

private fun toNeutralGray(c: Color): Color = hsl(0f, 0f, c.toHsl().l)

// ── Colour scheme from Material colour utilities ─────────────────────────────

private fun tone(p: TonalPalette, t: Int): Color = Color(p.tone(t))

private fun schemeOf(ds: DynamicScheme, dark: Boolean): ColorScheme {
    val p = ds.primaryPalette
    val s = ds.secondaryPalette
    val t = ds.tertiaryPalette
    val n = ds.neutralPalette
    val nv = ds.neutralVariantPalette
    val e = ds.errorPalette
    return if (dark) {
        darkColorScheme(
            primary = tone(p, 80), onPrimary = tone(p, 20),
            primaryContainer = tone(p, 30), onPrimaryContainer = tone(p, 90),
            inversePrimary = tone(p, 40),
            secondary = tone(s, 80), onSecondary = tone(s, 20),
            secondaryContainer = tone(s, 30), onSecondaryContainer = tone(s, 90),
            tertiary = tone(t, 80), onTertiary = tone(t, 20),
            tertiaryContainer = tone(t, 30), onTertiaryContainer = tone(t, 90),
            background = tone(n, 6), onBackground = tone(n, 90),
            surface = tone(n, 6), onSurface = tone(n, 90),
            surfaceVariant = tone(nv, 30), onSurfaceVariant = tone(nv, 80),
            surfaceTint = tone(p, 80),
            inverseSurface = tone(n, 90), inverseOnSurface = tone(n, 20),
            error = tone(e, 80), onError = tone(e, 20),
            errorContainer = tone(e, 30), onErrorContainer = tone(e, 90),
            outline = tone(nv, 60), outlineVariant = tone(nv, 30),
            scrim = Color.Black,
            surfaceBright = tone(n, 24), surfaceDim = tone(n, 6),
            surfaceContainerLowest = tone(n, 4), surfaceContainerLow = tone(n, 10),
            surfaceContainer = tone(n, 12), surfaceContainerHigh = tone(n, 17),
            surfaceContainerHighest = tone(n, 22),
        )
    } else {
        lightColorScheme(
            primary = tone(p, 40), onPrimary = tone(p, 100),
            primaryContainer = tone(p, 90), onPrimaryContainer = tone(p, 10),
            inversePrimary = tone(p, 80),
            secondary = tone(s, 40), onSecondary = tone(s, 100),
            secondaryContainer = tone(s, 90), onSecondaryContainer = tone(s, 10),
            tertiary = tone(t, 40), onTertiary = tone(t, 100),
            tertiaryContainer = tone(t, 90), onTertiaryContainer = tone(t, 10),
            background = tone(n, 98), onBackground = tone(n, 10),
            surface = tone(n, 98), onSurface = tone(n, 10),
            surfaceVariant = tone(nv, 90), onSurfaceVariant = tone(nv, 30),
            surfaceTint = tone(p, 40),
            inverseSurface = tone(n, 20), inverseOnSurface = tone(n, 95),
            error = tone(e, 40), onError = tone(e, 100),
            errorContainer = tone(e, 90), onErrorContainer = tone(e, 10),
            outline = tone(nv, 50), outlineVariant = tone(nv, 80),
            scrim = Color.Black,
            surfaceBright = tone(n, 98), surfaceDim = tone(n, 87),
            surfaceContainerLowest = tone(n, 100), surfaceContainerLow = tone(n, 96),
            surfaceContainer = tone(n, 94), surfaceContainerHigh = tone(n, 92),
            surfaceContainerHighest = tone(n, 90),
        )
    }
}

private fun dynamicSchemeFor(seed: Color, dark: Boolean, harmony: String): DynamicScheme {
    val hct = Hct.fromInt(seed.toArgb())
    return when (harmony) {
        "vivid" -> SchemeVibrant(hct, dark, 0.0)
        "soft" -> SchemeNeutral(hct, dark, 0.0)
        else -> SchemeTonalSpot(hct, dark, 0.0)
    }
}

/** Applies [f] to the roles that carry a hue, so a grey seed can be made truly grey. */
private fun ColorScheme.mapAll(f: (Color) -> Color): ColorScheme = copy(
    primary = f(primary), onPrimary = f(onPrimary),
    primaryContainer = f(primaryContainer), onPrimaryContainer = f(onPrimaryContainer),
    secondary = f(secondary), onSecondary = f(onSecondary),
    secondaryContainer = f(secondaryContainer), onSecondaryContainer = f(onSecondaryContainer),
    tertiary = f(tertiary), onTertiary = f(onTertiary),
    tertiaryContainer = f(tertiaryContainer), onTertiaryContainer = f(onTertiaryContainer),
    outline = f(outline), outlineVariant = f(outlineVariant),
    surfaceTint = f(surfaceTint),
    background = f(background), onBackground = f(onBackground),
    surface = f(surface), onSurface = f(onSurface),
    surfaceVariant = f(surfaceVariant), onSurfaceVariant = f(onSurfaceVariant),
    surfaceDim = f(surfaceDim), surfaceBright = f(surfaceBright),
    surfaceContainerLowest = f(surfaceContainerLowest), surfaceContainerLow = f(surfaceContainerLow),
    surfaceContainer = f(surfaceContainer), surfaceContainerHigh = f(surfaceContainerHigh),
    surfaceContainerHighest = f(surfaceContainerHighest),
)

/**
 * The colour utilities add a faint hue even to a seed with no chroma.
 * For a black, white or grey accent this removes that tint again.
 */
fun neutralizeIfGraySeed(scheme: ColorScheme, seed: Color): ColorScheme =
    if (!isNeutralColor(seed)) scheme else scheme.mapAll(::toNeutralGray)

/** Same fix for a scheme that came from the system (wallpaper colours). */
fun neutralizeIfGraySchemeItself(scheme: ColorScheme): ColorScheme =
    neutralizeIfGraySeed(scheme, scheme.primary)

/**
 * Gives every accent role the hue of the primary (only lightness and saturation
 * change). Pure greys stay as they are.
 */
fun monochromeAccents(s: ColorScheme, dark: Boolean): ColorScheme {
    if (isNeutralColor(s.primary)) return s
    val p = s.primary.toHsl()

    fun same(ref: Color, sat: Float = 1f, dl: Float = 0f): Color {
        val r = ref.toHsl()
        return hsl(p.h, (p.s * sat).coerceIn(0f, 1f), (r.l + dl).coerceIn(0f, 1f))
    }

    fun on(c: Color): Color = if (isDarkColor(c)) Color.White else Color.Black

    val secondary = same(s.secondary, sat = 0.6f)
    val secondaryContainer = same(s.secondaryContainer, sat = 0.6f)
    val tertiary = same(s.primary, dl = if (dark) -0.16f else 0.16f)
    val tertiaryContainer = same(s.primaryContainer, sat = 0.9f, dl = if (dark) 0.09f else -0.07f)
    return s.copy(
        secondary = secondary, onSecondary = on(secondary),
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = same(s.onSecondaryContainer, sat = 0.6f),
        tertiary = tertiary, onTertiary = on(tertiary),
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = same(s.onPrimaryContainer, sat = 0.8f),
    )
}

/**
 * Gives every neutral role a faint tint of the primary hue, so the UI never
 * shows a dead grey. Lightness is kept.
 */
fun tintNeutrals(s: ColorScheme, dark: Boolean): ColorScheme {
    if (isNeutralColor(s.primary)) return s
    val h = s.primary.toHsl().h

    fun t(c: Color, sat: Float = 0.30f): Color {
        val l = c.toHsl()
        if (l.l < 0.02f) return c
        val target = if (dark) sat * 0.75f else sat
        return hsl(h, target, l.l)
    }
    return s.copy(
        outline = t(s.outline, 0.22f),
        outlineVariant = t(s.outlineVariant),
        background = t(s.background),
        surface = t(s.surface), surfaceDim = t(s.surfaceDim), surfaceBright = t(s.surfaceBright),
        surfaceVariant = t(s.surfaceVariant),
        surfaceContainerLowest = t(s.surfaceContainerLowest),
        surfaceContainerLow = t(s.surfaceContainerLow),
        surfaceContainer = t(s.surfaceContainer),
        surfaceContainerHigh = t(s.surfaceContainerHigh),
        surfaceContainerHighest = t(s.surfaceContainerHighest),
        onSurface = t(s.onSurface, 0.22f),
        onSurfaceVariant = t(s.onSurfaceVariant, 0.2f),
        inverseSurface = t(s.inverseSurface, 0.2f),
        inverseOnSurface = t(s.inverseOnSurface, 0.2f),
    )
}

/** "classic" keeps the scheme, "tinted" uses one hue and tints the neutrals, others use one hue. */
fun applyColorHarmony(s: ColorScheme, harmony: String, dark: Boolean): ColorScheme = when (harmony) {
    "classic" -> s
    "tinted" -> tintNeutrals(monochromeAccents(s, dark), dark)
    else -> monochromeAccents(s, dark)
}

/** Builds the app colour scheme for a seed and a harmony style. */
fun buildAppScheme(seed: Color, dark: Boolean, harmony: String): ColorScheme {
    val base = neutralizeIfGraySeed(schemeOf(dynamicSchemeFor(seed, dark, harmony), dark), seed)
    return applyColorHarmony(base, harmony, dark)
}

/** Moves the error colours towards the primary, like the system colour "harmonize". */
fun harmonizeErrorColors(s: ColorScheme): ColorScheme {
    val source = s.primary.toArgb()
    fun h(c: Color) = Color(Blend.harmonize(c.toArgb(), source))
    return s.copy(
        error = h(s.error), onError = h(s.onError),
        errorContainer = h(s.errorContainer), onErrorContainer = h(s.onErrorContainer),
    )
}

/** Scheme from the system wallpaper colours with the chosen harmony. */
fun buildSystemScheme(system: ColorScheme, harmony: String, dark: Boolean): ColorScheme =
    applyColorHarmony(neutralizeIfGraySchemeItself(harmonizeErrorColors(system)), harmony, dark)

/** Pure black surfaces for OLED screens. */
fun applyOled(dark: ColorScheme, harmony: String): ColorScheme {
    val oled = dark.copy(
        background = Color.Black,
        surface = Color.Black,
        surfaceDim = Color.Black,
        surfaceBright = Color(0xFF1C1C1C),
        surfaceContainerLowest = Color.Black,
        surfaceContainerLow = Color(0xFF080808),
        surfaceContainer = Color(0xFF0D0D0D),
        surfaceContainerHigh = Color(0xFF141414),
        surfaceContainerHighest = Color(0xFF1C1C1C),
    )
    return if (harmony == "tinted") tintNeutrals(oled, true) else oled
}

/** True when the "night" window applies at [hour] (it can wrap past midnight). */
fun isNightByHour(dayStartHour: Int, nightStartHour: Int, hour: Int): Boolean {
    if (nightStartHour == dayStartHour) return false
    return if (nightStartHour > dayStartHour) {
        hour >= nightStartHour || hour < dayStartHour
    } else {
        hour >= nightStartHour && hour < dayStartHour
    }
}

/** Blend of two schemes, used to animate an accent change. */
fun lerpScheme(a: ColorScheme, b: ColorScheme, f: Float): ColorScheme = when {
    f <= 0f -> a
    f >= 1f -> b
    else -> a.copy(
        primary = lerp(a.primary, b.primary, f), onPrimary = lerp(a.onPrimary, b.onPrimary, f),
        primaryContainer = lerp(a.primaryContainer, b.primaryContainer, f),
        onPrimaryContainer = lerp(a.onPrimaryContainer, b.onPrimaryContainer, f),
        inversePrimary = lerp(a.inversePrimary, b.inversePrimary, f),
        secondary = lerp(a.secondary, b.secondary, f), onSecondary = lerp(a.onSecondary, b.onSecondary, f),
        secondaryContainer = lerp(a.secondaryContainer, b.secondaryContainer, f),
        onSecondaryContainer = lerp(a.onSecondaryContainer, b.onSecondaryContainer, f),
        tertiary = lerp(a.tertiary, b.tertiary, f), onTertiary = lerp(a.onTertiary, b.onTertiary, f),
        tertiaryContainer = lerp(a.tertiaryContainer, b.tertiaryContainer, f),
        onTertiaryContainer = lerp(a.onTertiaryContainer, b.onTertiaryContainer, f),
        background = lerp(a.background, b.background, f), onBackground = lerp(a.onBackground, b.onBackground, f),
        surface = lerp(a.surface, b.surface, f), onSurface = lerp(a.onSurface, b.onSurface, f),
        surfaceVariant = lerp(a.surfaceVariant, b.surfaceVariant, f),
        onSurfaceVariant = lerp(a.onSurfaceVariant, b.onSurfaceVariant, f),
        surfaceTint = lerp(a.surfaceTint, b.surfaceTint, f),
        inverseSurface = lerp(a.inverseSurface, b.inverseSurface, f),
        inverseOnSurface = lerp(a.inverseOnSurface, b.inverseOnSurface, f),
        error = lerp(a.error, b.error, f), onError = lerp(a.onError, b.onError, f),
        errorContainer = lerp(a.errorContainer, b.errorContainer, f),
        onErrorContainer = lerp(a.onErrorContainer, b.onErrorContainer, f),
        outline = lerp(a.outline, b.outline, f), outlineVariant = lerp(a.outlineVariant, b.outlineVariant, f),
        scrim = lerp(a.scrim, b.scrim, f),
        surfaceBright = lerp(a.surfaceBright, b.surfaceBright, f), surfaceDim = lerp(a.surfaceDim, b.surfaceDim, f),
        surfaceContainer = lerp(a.surfaceContainer, b.surfaceContainer, f),
        surfaceContainerHigh = lerp(a.surfaceContainerHigh, b.surfaceContainerHigh, f),
        surfaceContainerHighest = lerp(a.surfaceContainerHighest, b.surfaceContainerHighest, f),
        surfaceContainerLow = lerp(a.surfaceContainerLow, b.surfaceContainerLow, f),
        surfaceContainerLowest = lerp(a.surfaceContainerLowest, b.surfaceContainerLowest, f),
    )
}

/** Colour of a tonal step of [seed], used to tint cards from a cover colour. */
fun tonalColor(seed: Color, tone: Int): Color =
    Color(TonalPalette.fromHct(Hct.fromInt(seed.toArgb())).tone(tone))
