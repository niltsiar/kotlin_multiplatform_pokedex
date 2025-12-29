package com.minddistrict.multiplatformpoc

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.minddistrict.multiplatformpoc.core.designsystem.material.theme.PokemonTheme

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "MultiplatformPOC",
    ) {
        PokemonTheme {
            App()
        }
    }
}
