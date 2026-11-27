package com.sanobld.laststats.core

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.core.os.LocaleListCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.sanobld.laststats.AndroidPlatform
import com.sanobld.laststats.data.local.FlutterPrefsMigration
import okio.Path.Companion.toPath
import java.io.File
import java.util.Locale

actual object PlatformCaps {
    actual val name: String = "android"
    actual val dynamicColor: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    actual val blur: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}

actual fun createSettingsDataStore(): DataStore<Preferences> {
    val app = AndroidPlatform.app
    return PreferenceDataStoreFactory.createWithPath(
        migrations = listOf(FlutterPrefsMigration(app)),
        produceFile = {
            val dir = File(app.filesDir, "datastore").apply { mkdirs() }
            File(dir, "laststats_settings.preferences_pb").absolutePath.toPath()
        },
    )
}

actual fun platformApplyLocale(code: String) {
    val wanted = LocaleListCompat.forLanguageTags(code)
    if (AppCompatDelegate.getApplicationLocales().toLanguageTags() != wanted.toLanguageTags()) {
        AppCompatDelegate.setApplicationLocales(wanted)
    }
}

actual fun platformSystemLocaleChoice(): String? {
    val tags = AppCompatDelegate.getApplicationLocales()
    if (tags.isEmpty) return null
    return tags[0]?.language
}

actual fun platformDeviceLanguage(): String? = Locale.getDefault().language

@OptIn(ExperimentalTextApi::class)
actual fun fontFromBytes(identity: String, data: ByteArray, weight: FontWeight, style: FontStyle): Font {
    val dir = File(AndroidPlatform.app.cacheDir, "fonts").apply { mkdirs() }
    val file = File(dir, identity.replace(Regex("[^A-Za-z0-9._-]"), "_"))
    if (!file.exists() || file.length() != data.size.toLong()) file.writeBytes(data)
    return Font(file, weight, style)
}

actual fun appDataDirectory(): String = AndroidPlatform.app.filesDir.absolutePath
