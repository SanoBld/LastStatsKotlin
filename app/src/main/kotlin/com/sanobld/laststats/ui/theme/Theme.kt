package com.sanobld.laststats.ui.theme

import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import com.sanobld.laststats.AppContainer
import com.sanobld.laststats.core.AppConstants
import com.sanobld.laststats.data.local.CustomFontStore
import com.sanobld.laststats.data.local.Prefs
import com.sanobld.laststats.data.local.collectAsState
import java.time.LocalTime
import androidx.compose.runtime.collectAsState as collectFlowAsState
import androidx.compose.foundation.layout.fillMaxSize

/** Theme mode saved in settings: "system", "light" or "dark". */
fun isDarkFor(mode: String, systemDark: Boolean): Boolean = when (mode) {
    "light" -> false
    "dark" -> true
    else -> systemDark
}

/**
 * The colour scheme the person asked for: accent (with day and night variants),
 * system colours, harmony and OLED black.
 */
@Composable
private fun resolveColorScheme(dark: Boolean): ColorScheme {
    val container = AppContainer.instance
    val settings = container.settings
    val state = container.state
    val context = LocalContext.current

    val accentKey by settings.collectAsState(Prefs.accent)
    val accentDarkKey by settings.collectAsState(Prefs.accentDark)
    val useDayNight by settings.collectAsState(Prefs.useDayNightAccent)
    val useHours by settings.collectAsState(Prefs.dayNightUseHours)
    val dayStart by settings.collectAsState(Prefs.dayStartHour)
    val nightStart by settings.collectAsState(Prefs.nightStartHour)
    val useDynamic by settings.collectAsState(Prefs.useDynamicColor)
    val useNowPlaying by settings.collectAsState(Prefs.useNowPlayingColor)
    val oled by settings.collectAsState(Prefs.oledMode)
    val harmony by settings.collectAsState(Prefs.colorHarmony)
    val override by state.accentOverride.collectFlowAsState()
    val tick by state.minuteTick.collectFlowAsState()

    return remember(
        dark, accentKey, accentDarkKey, useDayNight, useHours, dayStart, nightStart,
        useDynamic, useNowPlaying, oled, harmony, override, if (useHours) tick else 0,
    ) {
        val accent = override ?: accentFromString(accentKey)
        val accentNight = if (accentDarkKey.isNotEmpty()) accentFromString(accentDarkKey) else accentFromString(accentKey)
        val nightNow = useHours && isNightByHour(dayStart, nightStart, LocalTime.now().hour)
        val music = useNowPlaying && !useDynamic
        val seed = when {
            !useDayNight || music -> accent
            useHours -> if (nightNow) accentNight else accent
            else -> if (dark) accentNight else accent
        }
        val base = if (useDynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val system = if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            buildSystemScheme(system, harmony, dark)
        } else {
            buildAppScheme(seed, dark, harmony)
        }
        if (dark && oled) applyOled(base, harmony) else base
    }
}

/** Slow fade when the colours change (500 ms, ease in out). */
@Composable
private fun animatedScheme(target: ColorScheme): ColorScheme {
    var from by remember { mutableStateOf(target) }
    var to by remember { mutableStateOf(target) }
    val progress = remember { Animatable(1f) }
    LaunchedEffect(target) {
        if (target == to) return@LaunchedEffect
        from = lerpScheme(from, to, progress.value)
        to = target
        progress.snapTo(0f)
        progress.animateTo(1f, tween(500, easing = M3Motion.EaseInOut))
    }
    return lerpScheme(from, to, progress.value)
}

@Composable
fun LastStatsTheme(content: @Composable () -> Unit) {
    val container = AppContainer.instance
    val settings = container.settings
    val state = container.state

    val mode by settings.collectAsState(Prefs.theme)
    val fontKey by settings.collectAsState(Prefs.appFont)
    val textScale by settings.collectAsState(Prefs.textScale)
    val blurSetting by settings.collectAsState(Prefs.motionBlur)
    val eco by state.ecoModeActive.collectFlowAsState()
    val customFamily by CustomFontStore.family.collectFlowAsState()
    val systemAnimsOff by rememberSystemAnimationsDisabled()

    val dark = isDarkFor(mode, isSystemInDarkTheme())
    val scheme = animatedScheme(resolveColorScheme(dark))
    val typography = remember(fontKey, customFamily) {
        appTypography(FontCatalog.familyFor(fontKey, customFamily))
    }
    val reduced = systemAnimsOff || eco
    val blurOn = blurSetting && !eco

    MaterialTheme(colorScheme = scheme, typography = typography, shapes = AppShapes) {
        val indication = rememberPressIndication()
        CompositionLocalProvider(
            LocalIsDarkTheme provides dark,
            LocalReducedMotion provides reduced,
            LocalBlurEnabled provides blurOn,
            LocalIndication provides indication,
        ) {
            AppTextScale(
                scale = textScale.toFloat().coerceIn(AppConstants.TEXT_SCALE_MIN.toFloat(), AppConstants.TEXT_SCALE_MAX.toFloat()),
                dark = dark,
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged {
                            M3Origin.rootWidth = it.width.toFloat()
                            M3Origin.rootHeight = it.height.toFloat()
                        },
                    color = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    content = content,
                )
            }
        }
    }
}

/** Component colours that follow the accent. */
object AppComponentColors {
    /** Calendar dialogs: tonal surface, tinted header, round selected day. */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun datePicker(): DatePickerColors {
        val cs = MaterialTheme.colorScheme
        return DatePickerDefaults.colors(
            containerColor = cs.surfaceContainerHigh,
            headlineContentColor = cs.onPrimaryContainer,
            selectedDayContainerColor = cs.primary,
            selectedDayContentColor = cs.onPrimary,
            todayContentColor = cs.primary,
            todayDateBorderColor = cs.primary,
            selectedYearContainerColor = cs.primary,
            selectedYearContentColor = cs.onPrimary,
            dayInSelectionRangeContainerColor = cs.primaryContainer,
            dividerColor = Color.Transparent,
        )
    }
}
