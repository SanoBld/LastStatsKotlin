package com.sanobld.laststats.core

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.sanobld.laststats.LastStatsApp
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Holds the app language. Changing it applies the per-app locale, which
 * recreates the activity, so every screen reads fresh strings through [L].
 */
object LocaleManager {
    private val _code = MutableStateFlow(AppConstants.DEFAULT_LOCALE)
    val code: StateFlow<String> = _code

    @Volatile
    private var localized: Context? = null

    /** Context whose resources follow the current app language. */
    val context: Context
        get() = localized ?: LastStatsApp.instance

    fun init(base: Context, code: String) {
        val safe = supportedLocaleFor(code).code
        _code.value = safe
        localized = localize(base, safe)
    }

    /** Switches language and recreates the visible activity. */
    fun apply(base: Context, code: String) {
        val safe = supportedLocaleFor(code).code
        init(base, safe)
        val wanted = LocaleListCompat.forLanguageTags(safe)
        if (AppCompatDelegate.getApplicationLocales().toLanguageTags() != wanted.toLanguageTags()) {
            AppCompatDelegate.setApplicationLocales(wanted)
        }
    }

    /** Language chosen in the system "App languages" screen, if it is one we support. */
    fun systemChoice(): String? {
        val tags = AppCompatDelegate.getApplicationLocales()
        if (tags.isEmpty) return null
        val lang = tags[0]?.language ?: return null
        return SupportedLocales.firstOrNull { it.code == lang }?.code
    }

    private fun localize(base: Context, code: String): Context {
        val app = base.applicationContext
        val config = Configuration(app.resources.configuration)
        val locale = Locale.forLanguageTag(code)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return app.createConfigurationContext(config)
    }
}
