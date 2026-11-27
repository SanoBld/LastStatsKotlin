package com.sanobld.laststats

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/** Entry point called by the Swift host app. */
fun MainViewController(): UIViewController {
    AppBootstrap.start()
    return ComposeUIViewController { App() }
}
