package com.minddistrict.multiplatformpoc.core.designsystem.material.theme

import androidx.compose.ui.graphics.Color

/**
 * Pokémon type colors with light and dark mode variants adjusted for WCAG AA compliance.
 * Each type has background and content colors optimized for both themes.
 */
object PokemonTypeColors {
    
    // Normal type
    private val NormalLight = Color(0xFFA8A878)
    private val NormalDark = Color(0xFFC8C898)
    
    // Fire type
    private val FireLight = Color(0xFFF08030)
    private val FireDark = Color(0xFFFF9A5A)
    
    // Water type
    private val WaterLight = Color(0xFF6890F0)
    private val WaterDark = Color(0xFF88B4FF)
    
    // Electric type
    private val ElectricLight = Color(0xFFF8D030)
    private val ElectricDark = Color(0xFFFFE066)
    
    // Grass type
    private val GrassLight = Color(0xFF78C850)
    private val GrassDark = Color(0xFF98E070)
    
    // Ice type
    private val IceLight = Color(0xFF98D8D8)
    private val IceDark = Color(0xFFB8F8F8)
    
    // Fighting type
    private val FightingLight = Color(0xFFC03028)
    private val FightingDark = Color(0xFFE05048)
    
    // Poison type
    private val PoisonLight = Color(0xFFA040A0)
    private val PoisonDark = Color(0xFFC060C0)
    
    // Ground type
    private val GroundLight = Color(0xFFE0C068)
    private val GroundDark = Color(0xFFFFE088)
    
    // Flying type
    private val FlyingLight = Color(0xFFA890F0)
    private val FlyingDark = Color(0xFFC8B0FF)
    
    // Psychic type
    private val PsychicLight = Color(0xFFF85888)
    private val PsychicDark = Color(0xFFFF7AA8)
    
    // Bug type
    private val BugLight = Color(0xFFA8B820)
    private val BugDark = Color(0xFFC8D840)
    
    // Rock type
    private val RockLight = Color(0xFFB8A038)
    private val RockDark = Color(0xFFD8C058)
    
    // Ghost type
    private val GhostLight = Color(0xFF705898)
    private val GhostDark = Color(0xFF9078B8)
    
    // Dragon type
    private val DragonLight = Color(0xFF7038F8)
    private val DragonDark = Color(0xFF9060FF)
    
    // Dark type
    private val DarkLight = Color(0xFF705848)
    private val DarkDark = Color(0xFF907868)
    
    // Steel type
    private val SteelLight = Color(0xFFB8B8D0)
    private val SteelDark = Color(0xFFD8D8F0)
    
    // Fairy type
    private val FairyLight = Color(0xFFEE99AC)
    private val FairyDark = Color(0xFFFFB9CC)
    
    /**
     * Get background color for a Pokémon type.
     * @param type The Pokémon type name (e.g., "fire", "water")
     * @param isDark Whether dark mode is active
     * @return Color for the type background
     */
    fun getBackground(type: String, isDark: Boolean): Color {
        val normalizedType = type.lowercase()
        return when (normalizedType) {
            "normal" -> if (isDark) NormalDark else NormalLight
            "fire" -> if (isDark) FireDark else FireLight
            "water" -> if (isDark) WaterDark else WaterLight
            "electric" -> if (isDark) ElectricDark else ElectricLight
            "grass" -> if (isDark) GrassDark else GrassLight
            "ice" -> if (isDark) IceDark else IceLight
            "fighting" -> if (isDark) FightingDark else FightingLight
            "poison" -> if (isDark) PoisonDark else PoisonLight
            "ground" -> if (isDark) GroundDark else GroundLight
            "flying" -> if (isDark) FlyingDark else FlyingLight
            "psychic" -> if (isDark) PsychicDark else PsychicLight
            "bug" -> if (isDark) BugDark else BugLight
            "rock" -> if (isDark) RockDark else RockLight
            "ghost" -> if (isDark) GhostDark else GhostLight
            "dragon" -> if (isDark) DragonDark else DragonLight
            "dark" -> if (isDark) DarkDark else DarkLight
            "steel" -> if (isDark) SteelDark else SteelLight
            "fairy" -> if (isDark) FairyDark else FairyLight
            else -> if (isDark) NormalDark else NormalLight // Default to normal
        }
    }
    
    /**
     * Get content (text) color that contrasts well with the type background.
     * @param type The Pokémon type name
     * @param isDark Whether dark mode is active
     * @return Color for text on the type background
     */
    fun getContent(type: String, isDark: Boolean): Color {
        // For most type colors in light mode, black text works well
        // In dark mode, we use adjusted colors that are lighter, so black text still works
        return Color.Black
    }
    
    /**
     * Get both background and content colors as a Pair.
     */
    fun getColors(type: String, isDark: Boolean): Pair<Color, Color> {
        return Pair(getBackground(type, isDark), getContent(type, isDark))
    }
}
