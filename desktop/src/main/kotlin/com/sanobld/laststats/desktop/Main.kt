package com.sanobld.laststats.desktop

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.graphics.painter.BitmapPainter
import com.sanobld.laststats.App
import com.sanobld.laststats.AppBootstrap

fun main() {
    AppBootstrap.start()
    application {
        val icon = BitmapPainter(useResource("icon.png", ::loadImageBitmap))
        Window(
            onCloseRequest = ::exitApplication,
            title = "LastStats",
            icon = icon,
            state = rememberWindowState(size = DpSize(1180.dp, 800.dp)),
        ) {
            window.minimumSize = java.awt.Dimension(420, 640)
            App()
        }
    }
}
