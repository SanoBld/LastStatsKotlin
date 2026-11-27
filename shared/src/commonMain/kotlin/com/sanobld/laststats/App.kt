package com.sanobld.laststats

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.sanobld.laststats.core.LocaleManager
import com.sanobld.laststats.ui.theme.LastStatsTheme

/**
 * Root of the app on every platform (phone, tablet, desktop, iPhone).
 * The whole tree is rebuilt when the language changes.
 */
@Composable
fun App() {
    val language by LocaleManager.code.collectAsState()
    key(language) {
        LastStatsTheme {
            Box(Modifier.fillMaxSize())
        }
    }
}
