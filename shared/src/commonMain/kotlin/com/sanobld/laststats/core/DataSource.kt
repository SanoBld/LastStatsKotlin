package com.sanobld.laststats.core

import com.sanobld.laststats.data.local.Prefs
import com.sanobld.laststats.data.local.SettingsStore

/**
 * Which service the listening data comes from.
 * Last.fm: normal mode. ListenBrainz: no Last.fm request is made at all.
 */
object DataSource {
    var lb: Boolean = false
        private set

    /** Reads the saved choice. Call once at startup. */
    fun load(settings: SettingsStore) {
        if (!settings.contains(Prefs.source)) {
            val loggedIn = settings.get(Prefs.username).isNotEmpty() && settings.get(Prefs.apiKey).isNotEmpty()
            if (loggedIn) {
                settings.put(Prefs.source, "lfm")
                settings.put(Prefs.cacheSource, "lfm")
            }
        }
        lb = settings.get(Prefs.source) == "lb"
    }

    fun set(settings: SettingsStore, listenbrainz: Boolean) {
        lb = listenbrainz
        settings.put(Prefs.source, if (listenbrainz) "lb" else "lfm")
    }
}
