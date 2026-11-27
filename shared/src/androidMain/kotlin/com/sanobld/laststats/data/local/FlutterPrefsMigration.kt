package com.sanobld.laststats.data.local

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

// ── Old Flutter settings ─────────────────────────────────────────────────────

private const val FLUTTER_FILE = "FlutterSharedPreferences"
private const val FLUTTER_PREFIX = "flutter."
private const val LIST_PREFIX = "VGhpcyBpcyB0aGUgcHJlZml4IGZvciBhIGxpc3Qu"
private const val DOUBLE_PREFIX = "VGhpcyBpcyB0aGUgcHJlZml4IGZvciBEb3VibGUu"
private const val BIG_INT_PREFIX = "VGhpcyBpcyB0aGUgcHJlZml4IGZvciBCaWdJbnRlZ2Vy"

/**
 * One-time import of the settings written by the Flutter version of the app
 * (same application id), so an update keeps every setting.
 */
class FlutterPrefsMigration(private val context: Context) : DataMigration<Preferences> {
    private val doneKey = booleanPreferencesKey("ls_flutter_migrated")

    private fun old() = context.getSharedPreferences(FLUTTER_FILE, Context.MODE_PRIVATE)

    override suspend fun shouldMigrate(currentData: Preferences): Boolean =
        currentData[doneKey] != true && old().all.keys.any { it.startsWith(FLUTTER_PREFIX) }

    override suspend fun migrate(currentData: Preferences): Preferences {
        val m = currentData.toMutablePreferences()
        for ((k, v) in old().all) {
            if (!k.startsWith(FLUTTER_PREFIX)) continue
            val name = k.removePrefix(FLUTTER_PREFIX)
            when (v) {
                is Boolean -> m[booleanPreferencesKey(name)] = v
                is Long -> m[longPreferencesKey(name)] = v
                is Int -> m[longPreferencesKey(name)] = v.toLong()
                is Float -> m[doublePreferencesKey(name)] = v.toDouble()
                is String -> when {
                    v.startsWith(LIST_PREFIX) -> m[stringPreferencesKey(name)] = v.removePrefix(LIST_PREFIX)
                    v.startsWith(DOUBLE_PREFIX) ->
                        v.removePrefix(DOUBLE_PREFIX).toDoubleOrNull()?.let { m[doublePreferencesKey(name)] = it }
                    v.startsWith(BIG_INT_PREFIX) ->
                        v.removePrefix(BIG_INT_PREFIX).toLongOrNull()?.let { m[longPreferencesKey(name)] = it }
                    else -> m[stringPreferencesKey(name)] = v
                }
                is Set<*> -> m[stringPreferencesKey(name)] =
                    Json.encodeToString(ListSerializer(String.serializer()), v.filterIsInstance<String>())
            }
        }
        m[doneKey] = true
        return m
    }

    override suspend fun cleanUp() {}
}

