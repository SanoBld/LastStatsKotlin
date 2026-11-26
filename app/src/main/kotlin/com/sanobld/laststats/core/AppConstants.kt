package com.sanobld.laststats.core

import com.sanobld.laststats.BuildConfig

object AppConstants {
    const val APP_NAME = "LastStats"
    const val DEFAULT_LOCALE = "fr"
    const val DEFAULT_ACCENT_ARGB = 0xFF7C3AEDL

    const val REPO_OWNER = "SanoBld"
    const val REPO_NAME = "LastStats"
    const val REPO_URL = "https://github.com/SanoBld/LastStats"
    const val APP_REPO_URL = "https://github.com/SanoBld/LastStats-App"
    const val SITE_URL = "https://sanobld.github.io/LastStats/"
    const val NEWS_URL = "https://sanobld.github.io/LastStats/news.json"
    const val RELEASES_API = "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/releases"

    val USER_AGENT: String
        get() = "LastStats/${BuildConfig.VERSION_NAME} ( $REPO_URL )"

    const val NAV_RAIL_MIN_WIDTH_DP = 720
    const val TEXT_SCALE_MIN = 0.8
    const val TEXT_SCALE_MAX = 1.5
    const val CUSTOM_FONT_MAX_BYTES = 3 * 1024 * 1024

    val FIRST_LAUNCH_ACCENTS = listOf("purple", "blue", "green", "red", "orange", "pink", "teal")
}
