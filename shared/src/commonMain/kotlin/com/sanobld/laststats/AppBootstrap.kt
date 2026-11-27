package com.sanobld.laststats

import com.sanobld.laststats.core.AppState
import com.sanobld.laststats.core.DataSource
import com.sanobld.laststats.core.LocaleManager
import com.sanobld.laststats.core.createSettingsDataStore
import com.sanobld.laststats.data.local.CustomFontStore
import com.sanobld.laststats.data.local.Prefs
import com.sanobld.laststats.data.local.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking

/** Starts the shared part of the app. Every platform calls [start] once before showing the UI. */
object AppBootstrap {
    private var started = false

    fun start() {
        if (started) return
        started = true
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val settings = runBlocking { SettingsStore.create(createSettingsDataStore(), scope) }
        val state = AppState(settings, scope)
        AppContainer.install(AppContainer(scope, settings, state))
        DataSource.load(settings)
        state.init()

        val explicit = LocaleManager.systemChoice()
        if (explicit != null) {
            if (explicit != settings.get(Prefs.locale)) settings.put(Prefs.locale, explicit)
        } else if (!settings.contains(Prefs.locale)) {
            LocaleManager.deviceLanguage()?.let { settings.put(Prefs.locale, it) }
        }
        LocaleManager.init(settings.get(Prefs.locale))
        CustomFontStore.load(settings)
    }
}
