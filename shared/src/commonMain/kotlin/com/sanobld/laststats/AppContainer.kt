package com.sanobld.laststats

import com.sanobld.laststats.core.AppState
import com.sanobld.laststats.data.local.SettingsStore
import kotlinx.coroutines.CoroutineScope

/** Holds the long-lived objects of the app. Created once by [AppBootstrap]. */
class AppContainer(
    val appScope: CoroutineScope,
    val settings: SettingsStore,
    val state: AppState,
) {
    companion object {
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
