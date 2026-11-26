package com.sanobld.laststats.core

import androidx.compose.ui.graphics.Color
import com.sanobld.laststats.data.local.BoolPref
import com.sanobld.laststats.data.local.Prefs
import com.sanobld.laststats.data.local.SettingsStore
import com.sanobld.laststats.ui.theme.accentFromString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

private val legacyShowAllPlatformLinks = BoolPref("ls_show_all_platform_links", false)

/**
 * Values that are not saved settings: they change while the app runs.
 * Saved settings are read from [SettingsStore] (see [Prefs]).
 */
class AppState(
    private val settings: SettingsStore,
    private val scope: CoroutineScope,
) {
    /** Accent taken from the music (cover colour). Null = use the saved accent. */
    private val _accentOverride = MutableStateFlow<Color?>(null)
    val accentOverride: StateFlow<Color?> = _accentOverride

    /** Ticks once a minute so the hour based accent switch re-checks the clock. */
    private val _minuteTick = MutableStateFlow(0)
    val minuteTick: StateFlow<Int> = _minuteTick

    /** True when eco mode is really active right now. Read only this one. */
    private val _ecoModeActive = MutableStateFlow(false)
    val ecoModeActive: StateFlow<Boolean> = _ecoModeActive

    /** Why eco mode is on: "", "manual", "system" or "battery". */
    private val _ecoModeReason = MutableStateFlow("")
    val ecoModeReason: StateFlow<String> = _ecoModeReason

    /** Last known battery level (0 to 100, -1 = unknown). */
    private val _ecoBatteryLevel = MutableStateFlow(-1)
    val ecoBatteryLevel: StateFlow<Int> = _ecoBatteryLevel

    private val _ecoSystemSaver = MutableStateFlow(false)
    val ecoSystemSaver: StateFlow<Boolean> = _ecoSystemSaver

    /** Keys of the loved tracks ("artist|track", lower case). */
    private val _lovedTrackKeys = MutableStateFlow<Set<String>>(emptySet())
    val lovedTrackKeys: StateFlow<Set<String>> = _lovedTrackKeys

    private var started = false

    /** Startup rules and one-time migrations. Call once from Application.onCreate. */
    fun init() {
        if (started) return
        started = true

        if (!settings.contains(Prefs.accent) && settings.get(Prefs.username).isEmpty()) {
            settings.put(Prefs.accent, AppConstants.FIRST_LAUNCH_ACCENTS[Random.nextInt(AppConstants.FIRST_LAUNCH_ACCENTS.size)])
        }

        if (DataSource.lb) {
            val lbUser = settings.get(Prefs.lbUsername)
            val lbToken = settings.get(Prefs.lbToken)
            if (lbUser.isNotEmpty() && lbUser != settings.get(Prefs.username)) settings.put(Prefs.username, lbUser)
            if (lbToken.isNotEmpty() && lbToken != settings.get(Prefs.apiKey)) settings.put(Prefs.apiKey, lbToken)
        }

        if (settings.get(legacyShowAllPlatformLinks)) {
            settings.put(Prefs.musicPlatform, "all")
            settings.remove(legacyShowAllPlatformLinks)
        }

        if (!settings.get(Prefs.favStatMigrated)) {
            val favHidden = settings.contains(Prefs.showFavoritesStat) && !settings.get(Prefs.showFavoritesStat)
            if (settings.get(Prefs.sessionKey).isNotEmpty() && !favHidden && settings.contains(Prefs.statCards)) {
                val cards = settings.get(Prefs.statCards)
                if (!cards.contains("favorites_count")) settings.put(Prefs.statCards, cards + "favorites_count")
            }
            settings.put(Prefs.favStatMigrated, true)
        }

        if (settings.get(Prefs.useNowPlayingColor)) {
            val last = settings.get(Prefs.lastArtworkColor)
            val fallback = settings.get(Prefs.nowPlayingFallbackColor)
            if (settings.get(Prefs.keepLastArtworkColor) && last.isNotEmpty()) {
                _accentOverride.value = accentFromString(last)
            } else if (fallback.isNotEmpty()) {
                _accentOverride.value = accentFromString(fallback)
            }
        }

        scope.launch {
            while (true) {
                delay(60_000)
                _minuteTick.update { it + 1 }
            }
        }
    }

    // ── Accent ───────────────────────────────────────────────────────────────

    /** Accent the theme uses right now (music colour first, then the saved accent). */
    fun currentAccent(): Color = _accentOverride.value ?: accentFromString(settings.get(Prefs.accent))

    /** Colour used by "colour from the music" when nothing plays. */
    fun nowPlayingFallbackColor(): Color {
        val hex = settings.get(Prefs.nowPlayingFallbackColor)
        return if (hex.isNotEmpty()) accentFromString(hex) else accentFromString(settings.get(Prefs.accent))
    }

    fun setAccentOverride(color: Color?) {
        _accentOverride.value = color
    }

    // ── Eco mode (written by the eco mode controller) ────────────────────────

    fun setEco(active: Boolean, reason: String) {
        _ecoModeActive.value = active
        _ecoModeReason.value = if (active) reason else ""
    }

    fun setBattery(level: Int, systemSaver: Boolean) {
        _ecoBatteryLevel.value = level
        _ecoSystemSaver.value = systemSaver
    }

    // ── Platform links ───────────────────────────────────────────────────────

    /** True when every platform link pill shows, whatever was picked. */
    fun platformLinksShowAll(): Boolean {
        val v = settings.get(Prefs.musicPlatform)
        if (v.isEmpty() || v == "all") return true
        val parts = v.split(',')
        return parts.contains("all") || parts.contains("other")
    }

    fun platformLinkEnabled(key: String): Boolean =
        platformLinksShowAll() || settings.get(Prefs.musicPlatform).split(',').contains(key)

    // ── Loved tracks ─────────────────────────────────────────────────────────

    val favoritesEnabled: Boolean
        get() = DataSource.lb || settings.get(Prefs.sessionKey).isNotEmpty()

    fun setLovedTracks(keys: Set<String>) {
        _lovedTrackKeys.value = keys
    }

    fun addLoved(artist: String, track: String) {
        _lovedTrackKeys.update { it + lovedKey(artist, track) }
    }

    fun removeLoved(artist: String, track: String) {
        _lovedTrackKeys.update { it - lovedKey(artist, track) }
    }

    fun isTrackLoved(artist: String, track: String): Boolean =
        _lovedTrackKeys.value.contains(lovedKey(artist, track))
}

fun lovedKey(artist: String, track: String): String =
    "${artist.trim().lowercase()}|${track.trim().lowercase()}"
