package com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme

import androidx.compose.ui.graphics.Color
import com.minddistrict.multiplatformpoc.core.designsystem.core.PokemonTypeColors

/**
 * Type Colors helper for Unstyled design system.
 * Provides background and content colors for Pokémon types.
 */
object TypeColors {
    /**
     * Get background color for a Pokémon type.
     * @param type The Pokémon type name (e.g., "fire", "water")
     * @param isDark Whether dark mode is active
     * @return Color for the type background
     */
    fun getBackground(type: String, isDark: Boolean): Color {
        val normalizedType = type.lowercase()
        // Use the base colors from PokemonTypeColors
        // In dark mode, lighten slightly; in light mode, darken slightly
        val baseColor = when (normalizedType) {
            "normal" -> PokemonTypeColors.Normal
            "fire" -> PokemonTypeColors.Fire
            "water" -> PokemonTypeColors.Water
            "electric" -> PokemonTypeColors.Electric
            "grass" -> PokemonTypeColors.Grass
            "ice" -> PokemonTypeColors.Ice
            "fighting" -> PokemonTypeColors.Fighting
            "poison" -> PokemonTypeColors.Poison
            "ground" -> PokemonTypeColors.Ground
            "flying" -> PokemonTypeColors.Flying
            "psychic" -> PokemonTypeColors.Psychic
            "bug" -> PokemonTypeColors.Bug
            "rock" -> PokemonTypeColors.Rock
            "ghost" -> PokemonTypeColors.Ghost
            "dragon" -> PokemonTypeColors.Dragon
            "dark" -> PokemonTypeColors.Dark
            "steel" -> PokemonTypeColors.Steel
            "fairy" -> PokemonTypeColors.Fairy
            else -> PokemonTypeColors.Normal
        }
        
        // Adjust brightness for dark mode
        return if (isDark) {
            // Lighten for dark mode
            baseColor.copy(
                red = (baseColor.red * 1.2f).coerceIn(0f, 1f),
                green = (baseColor.green * 1.2f).coerceIn(0f, 1f),
                blue = (baseColor.blue * 1.2f).coerceIn(0f, 1f)
            )
        } else {
            // Slightly darken for light mode
            baseColor.copy(
                red = (baseColor.red * 0.9f).coerceIn(0f, 1f),
                green = (baseColor.green * 0.9f).coerceIn(0f, 1f),
                blue = (baseColor.blue * 0.9f).coerceIn(0f, 1f)
            )
        }
    }
    
    /**
     * Get content (text) color that contrasts well with the type background.
     * @param type The Pokémon type name
     * @param isDark Whether dark mode is active
     * @return Color for text on the type background
     */
    fun getContent(type: String, isDark: Boolean): Color {
        // Most type colors work well with white text
        return Color.White
    }
}
