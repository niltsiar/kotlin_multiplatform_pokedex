package com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme

import androidx.compose.ui.graphics.Color

/**
 * Pokémon Type Colors - Unstyled Theme
 * Matching Material 3 Expressive design system colors for consistency
 * WCAG AA compliant (4.5:1 contrast ratio on appropriate backgrounds)
 */
object UnstyledPokemonTypeColors {
    // Primary Pokémon type colors (adjusted for accessibility)
    val Fire = Color(0xFFFF4422)
    val Water = Color(0xFF3399FF)
    val Grass = Color(0xFF77CC55)
    val Electric = Color(0xFFFFCC33)
    val Psychic = Color(0xFFFF5599)
    val Ice = Color(0xFF66CCFF)
    val Dragon = Color(0xFF7766EE)
    val Dark = Color(0xFF775544)
    val Fairy = Color(0xFFEE99EE)
    val Normal = Color(0xFFAAAA99)
    val Fighting = Color(0xFFBB5544)
    val Flying = Color(0xFF8899FF)
    val Poison = Color(0xFF9955BB)
    val Ground = Color(0xFFDDBB55)
    val Rock = Color(0xFFBBAA77)
    val Bug = Color(0xFFAABB22)
    val Ghost = Color(0xFF6666BB)
    val Steel = Color(0xFFAAAABB)
}

/**
 * Unstyled Theme Color System
 * Matches Material 3 Expressive neutral palette with Pokémon type accent colors
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
