package com.sanobld.laststats.core

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font as SkiaFont
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import java.io.File
import java.util.Locale

actual object PlatformCaps {
    actual val name: String = "desktop"
    actual val dynamicColor: Boolean = false
    actual val blur: Boolean = true
}

/** `%APPDATA%\LastStats`, `~/Library/Application Support/LastStats` or `~/.local/share/LastStats`. */
actual fun appDataDirectory(): String {
    val os = System.getProperty("os.name").lowercase()
    val home = System.getProperty("user.home")
    val base = when {
        os.contains("win") -> System.getenv("APPDATA") ?: "$home\\AppData\\Roaming"
        os.contains("mac") -> "$home/Library/Application Support"
        else -> System.getenv("XDG_DATA_HOME") ?: "$home/.local/share"
    }
    return File(base, "LastStats").apply { mkdirs() }.absolutePath
}

actual fun createSettingsDataStore(): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { File(appDataDirectory(), "laststats_settings.preferences_pb").absolutePath.toPath() },
    )

actual fun platformApplyLocale(code: String) {}

actual fun platformSystemLocaleChoice(): String? = null

actual fun platformDeviceLanguage(): String? = Locale.getDefault().language

actual fun fontFromBytes(identity: String, data: ByteArray, weight: FontWeight, style: FontStyle): Font =
    SkiaFont(identity = identity, data = data, weight = weight, style = style)
