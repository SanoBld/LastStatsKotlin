package com.sanobld.laststats

import android.app.Application
import com.sanobld.laststats.core.AppState
import com.sanobld.laststats.core.DataSource
import com.sanobld.laststats.core.LocaleManager
import com.sanobld.laststats.data.local.CustomFontStore
import com.sanobld.laststats.data.local.Prefs
import com.sanobld.laststats.data.local.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking

class LastStatsApp : Application() {

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        instance = this
        val settings = runBlocking { SettingsStore.create(this@LastStatsApp, appScope) }
        val state = AppState(settings, appScope)
        AppContainer.install(AppContainer(this, appScope, settings, state))
        DataSource.load(settings)
        state.init()
        val systemChoice = LocaleManager.systemChoice()
        if (systemChoice != null && systemChoice != settings.get(Prefs.locale)) {
            settings.put(Prefs.locale, systemChoice)
        }
        LocaleManager.init(this, systemChoice ?: settings.get(Prefs.locale))
        CustomFontStore.load(this, settings)
    }

    companion object {
        lateinit var instance: LastStatsApp
            private set
    }
}
