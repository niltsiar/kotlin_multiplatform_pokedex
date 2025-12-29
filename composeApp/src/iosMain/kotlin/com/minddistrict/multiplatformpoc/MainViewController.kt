package com.minddistrict.multiplatformpoc

import androidx.compose.ui.window.ComposeUIViewController
import com.minddistrict.multiplatformpoc.core.designsystem.material.theme.PokemonTheme
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    return ComposeUIViewController {
        PokemonTheme {
            App()
        }
    }
}
