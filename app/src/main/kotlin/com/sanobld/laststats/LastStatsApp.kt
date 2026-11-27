package com.sanobld.laststats

import android.app.Application

class LastStatsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        AndroidPlatform.app = this
        AppBootstrap.start()
    }

    companion object {
        lateinit var instance: LastStatsApp
            private set
    }
}
