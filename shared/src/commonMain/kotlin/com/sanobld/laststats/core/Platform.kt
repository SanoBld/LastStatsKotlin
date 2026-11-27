package com.sanobld.laststats.core

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/** What the running platform can do. */
expect object PlatformCaps {
    /** "android", "desktop" or "ios". */
    val name: String
    /** Colours taken from the wallpaper (Android 12 and later). */
    val dynamicColor: Boolean
    /** Soft blur effects are drawn. */
    val blur: Boolean
}

/** Settings file of this platform (with the old Flutter import on Android). */
expect fun createSettingsDataStore(): DataStore<Preferences>

/** Applies the language to the system side (per-app locale on Android). */
expect fun platformApplyLocale(code: String)

/** Language the person picked in the system "app languages" screen (Android only), else null. */
expect fun platformSystemLocaleChoice(): String?

/** Language of the device, two letters, or null. */
expect fun platformDeviceLanguage(): String?

/** Builds a font from the bytes of a .ttf or .otf file. */
expect fun fontFromBytes(identity: String, data: ByteArray, weight: FontWeight, style: FontStyle): Font

/** Folder where the app keeps its files (cache, history, backups). */
expect fun appDataDirectory(): String
