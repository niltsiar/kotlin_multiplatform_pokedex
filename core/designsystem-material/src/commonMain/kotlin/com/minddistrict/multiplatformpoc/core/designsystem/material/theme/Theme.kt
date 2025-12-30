package com.minddistrict.multiplatformpoc.core.designsystem.material.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.minddistrict.multiplatformpoc.core.designsystem.material.tokens.DefaultMaterialTokens
import com.minddistrict.multiplatformpoc.core.designsystem.material.tokens.LocalMaterialTokens

/**
 * Pokemon theme wrapper that applies Material 3 Expressive design system.
 * Automatically switches between light and dark color schemes based on system settings.
 * 
 * Features:
 * - Material 3 Expressive color schemes with Pokémon-inspired accents
 * - Typography using Google Sans Flex variable font (Android/Desktop) or San Francisco (iOS)
 * - Emphasized motion system with expressive easing curves
 * - Support for both light and dark modes with proper WCAG AA contrast
 * 
 * @param darkTheme Whether to use dark theme. Defaults to system setting.
 * @param content The composable content to apply the theme to.
 */
@Composable
fun PokemonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }
    
    val typography = rememberPokemonTypography()
    
    // Provide Material design tokens through CompositionLocal
    CompositionLocalProvider(LocalMaterialTokens provides DefaultMaterialTokens()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}
