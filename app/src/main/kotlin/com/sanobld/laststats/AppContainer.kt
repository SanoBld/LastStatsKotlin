package com.sanobld.laststats

import android.app.Application
import com.sanobld.laststats.data.local.SettingsStore
import kotlinx.coroutines.CoroutineScope

/** Holds the long-lived objects of the app. Created once in [LastStatsApp]. */
class AppContainer(
    val app: Application,
    val appScope: CoroutineScope,
    val settings: SettingsStore,
) {
    companion object {
        @Volatile
        private var current: AppContainer? = null

        val instance: AppContainer
            get() = current ?: error("AppContainer is not installed yet")

        val instanceOrNull: AppContainer?
            get() = current

        fun install(container: AppContainer) {
            current = container
        }
    }
}
