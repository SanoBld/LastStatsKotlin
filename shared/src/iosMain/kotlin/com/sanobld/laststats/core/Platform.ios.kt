@file:OptIn(ExperimentalForeignApi::class)

package com.sanobld.laststats.core

import kotlinx.cinterop.ExperimentalForeignApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font as SkiaFont
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSLocale
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.preferredLanguages

actual object PlatformCaps {
    actual val name: String = "ios"
    actual val dynamicColor: Boolean = false
    actual val blur: Boolean = true
}

actual fun appDataDirectory(): String {
    val dirs = NSSearchPathForDirectoriesInDomains(NSApplicationSupportDirectory, NSUserDomainMask, true)
    val base = (dirs.firstOrNull() as? String) ?: "/tmp"
    NSFileManager.defaultManager.createDirectoryAtPath(base, withIntermediateDirectories = true, attributes = null, error = null)
    return base
}

actual fun createSettingsDataStore(): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { "${appDataDirectory()}/laststats_settings.preferences_pb".toPath() },
    )

actual fun platformApplyLocale(code: String) {}

actual fun platformSystemLocaleChoice(): String? = null

actual fun platformDeviceLanguage(): String? =
    (NSLocale.preferredLanguages.firstOrNull() as? String)?.take(2)?.lowercase()

actual fun fontFromBytes(identity: String, data: ByteArray, weight: FontWeight, style: FontStyle): Font =
    SkiaFont(identity = identity, data = data, weight = weight, style = style)
