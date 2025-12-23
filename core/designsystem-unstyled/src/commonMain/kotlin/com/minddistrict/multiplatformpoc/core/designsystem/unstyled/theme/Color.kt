package com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme

import androidx.compose.ui.graphics.Color
import com.minddistrict.multiplatformpoc.core.designsystem.core.PokemonTypeColors

/**
 * Unstyled Theme Color System
 * Matches Material 3 Expressive neutral palette with Pokémon type accent colors
 * 
 * Note: Pokémon type colors are imported from designsystem-core for consistency
 */
object UnstyledColors {
    // Light Theme Colors
    object Light {
        val background = Color(0xFFFFFBFE)
        val surface = Color(0xFFFFFBFE)
        val surfaceVariant = Color(0xFFE7E0EC)
        val onSurface = Color(0xFF1C1B1F)
        val onSurfaceVariant = Color(0xFF49454F)
        val outline = Color(0xFF79747E)
        val outlineVariant = Color(0xFFCAC4D0)
        
        // Pokémon-specific
        val primary = Color(0xFFEE1515) // Pokéball red
        val onPrimary = Color(0xFFFFFFFF)
        val secondary = Color(0xFF0075BE) // Pokéball blue
        val onSecondary = Color(0xFFFFFFFF)
        val error = Color(0xFFBA1A1A)
        val onError = Color(0xFFFFFFFF)
    }
    
    // Dark Theme Colors
    object Dark {
        val background = Color(0xFF1C1B1F)
        val surface = Color(0xFF1C1B1F)
        val surfaceVariant = Color(0xFF49454F)
        val onSurface = Color(0xFFE6E1E5)
        val onSurfaceVariant = Color(0xFFCAC4D0)
        val outline = Color(0xFF938F99)
        val outlineVariant = Color(0xFF49454F)
        
        // Pokémon-specific
        val primary = Color(0xFFFFB4AB)
        val onPrimary = Color(0xFF690005)
        val secondary = Color(0xFF4FC3F7)
        val onSecondary = Color(0xFF003544)
        val error = Color(0xFFFFB4AB)
        val onError = Color(0xFF690005)
    }
}
