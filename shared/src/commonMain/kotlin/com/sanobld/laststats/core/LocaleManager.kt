package com.sanobld.laststats.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Holds the app language. The root screen is rebuilt when [code] changes, so
 * every screen reads fresh strings through [L].
 */
object LocaleManager {
    private val _code = MutableStateFlow(AppConstants.DEFAULT_LOCALE)
    val code: StateFlow<String> = _code

    /** Loads the strings of [code] without touching the system. */
    fun init(code: String) {
        val safe = supportedLocaleFor(code).code
        StringTable.load(safe)
        _code.value = safe
    }

    /** Switches language everywhere. */
    fun apply(code: String) {
        val safe = supportedLocaleFor(code).code
        init(safe)
        platformApplyLocale(safe)
    }

    /** Language chosen in the system "app languages" screen, if it is one we support. */
    fun systemChoice(): String? = platformSystemLocaleChoice()?.takeIf { c -> SupportedLocales.any { it.code == c } }

    /** Language of the device when we support it. */
    fun deviceLanguage(): String? = platformDeviceLanguage()?.takeIf { c -> SupportedLocales.any { it.code == c } }
}
