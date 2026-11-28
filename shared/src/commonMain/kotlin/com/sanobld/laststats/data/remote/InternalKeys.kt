package com.sanobld.laststats.data.remote

import com.sanobld.laststats.core.BuildKeys
import com.sanobld.laststats.data.local.BoolPref
import com.sanobld.laststats.data.local.SettingsStore
import com.sanobld.laststats.data.local.StringPref
import kotlin.random.Random

/**
 * Last.fm API keys shipped with the app (public on purpose, like the original project).
 * When the person has no key of their own, one of these is used.
 */
object InternalKeys {
    /** Key of the project. It is also in the public source code. */
    const val PUBLIC_KEY = "4b209f5fef22138422d78a337d7518a4"

    private val pickedKey = StringPref("ls_internal_key")
    private val fallbackEnabled = BoolPref("ls_internal_fallback")

    /** Extra keys given at build time (LS_KEYS, comma separated), plus the public one. */
    val pool: List<String> by lazy {
        (BuildKeys.LS_KEYS.split(',').map { it.trim() }.filter { it.length == 32 } + PUBLIC_KEY).distinct()
    }

    /** True while the "use internal keys" option can be offered. */
    const val enabled: Boolean = true

    fun isInternal(key: String): Boolean = key.isNotEmpty() && pool.contains(key)

    /** Key to use when the person has none. Stays the same once picked. */
    fun pick(settings: SettingsStore): String {
        val saved = settings.get(pickedKey)
        if (saved.isNotEmpty() && pool.contains(saved)) return saved
        val chosen = pool[Random.nextInt(pool.size)]
        settings.put(pickedKey, chosen)
        return chosen
    }

    /** Another key than [current], for when one is rejected. Null when there is no other. */
    fun otherThan(current: String): String? = pool.filter { it != current }.let { if (it.isEmpty()) null else it[Random.nextInt(it.size)] }

    fun fallbackOn(settings: SettingsStore): Boolean = settings.get(fallbackEnabled)

    fun setFallback(settings: SettingsStore, on: Boolean) {
        settings.put(fallbackEnabled, on)
        LastFmService.fallbackKey = if (on) pick(settings) else ""
    }

    /** Restores the fallback key at startup. */
    fun loadFallback(settings: SettingsStore) {
        LastFmService.fallbackKey = if (settings.get(fallbackEnabled)) pick(settings) else ""
    }
}
