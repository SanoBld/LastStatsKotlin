package com.sanobld.laststats

import android.app.Application

/** Gives the shared code the Android application. Set it in Application.onCreate before AppBootstrap.start(). */
object AndroidPlatform {
    lateinit var app: Application
}
