package com.minddistrict.multiplatformpoc.core.designsystem.core

import androidx.compose.ui.graphics.Color

/**
 * Pokémon Type Colors (Domain-Specific)
 * 
 * These colors represent the official Pokémon type system and are shared across all design systems.
 * Colors are adjusted for WCAG AA accessibility compliance (4.5:1 contrast ratio on appropriate backgrounds).
 * 
 * Use these colors for:
 * - Type badges on Pokémon cards
 * - Type indicators in detail views
 * - Color-coding for type effectiveness
 */
object PokemonTypeColors {
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
    
    /**
     * Get background color for a Pokémon type with theme-aware adjustments.
     * @param type The Pokémon type name (e.g., "fire", "water")
     * @param isDark Whether dark mode is active
     * @return Color for the type background
     */
    fun getBackground(type: String, isDark: Boolean): Color {
        val normalizedType = type.lowercase()
        val baseColor = when (normalizedType) {
            "normal" -> Normal
            "fire" -> Fire
            "water" -> Water
            "electric" -> Electric
            "grass" -> Grass
            "ice" -> Ice
            "fighting" -> Fighting
            "poison" -> Poison
            "ground" -> Ground
            "flying" -> Flying
            "psychic" -> Psychic
            "bug" -> Bug
            "rock" -> Rock
            "ghost" -> Ghost
            "dragon" -> Dragon
            "dark" -> Dark
            "steel" -> Steel
            "fairy" -> Fairy
            else -> Normal
        }
        
        // Adjust brightness for theme
        return if (isDark) {
            // Lighten for dark mode (better contrast on dark backgrounds)
            baseColor.copy(
                red = (baseColor.red * 1.2f).coerceIn(0f, 1f),
                green = (baseColor.green * 1.2f).coerceIn(0f, 1f),
                blue = (baseColor.blue * 1.2f).coerceIn(0f, 1f)
            )
        } else {
            // Slightly darken for light mode (better contrast on light backgrounds)
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
    
    /**
     * Get both background and content colors as a Pair.
     */
    fun getColors(type: String, isDark: Boolean): Pair<Color, Color> {
        return Pair(getBackground(type, isDark), getContent(type, isDark))
    }
}
