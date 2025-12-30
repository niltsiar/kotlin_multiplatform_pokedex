package com.minddistrict.multiplatformpoc.core.designsystem.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minddistrict.multiplatformpoc.core.designsystem.core.PokemonTypeColors

/**
 * Theme-agnostic Pokémon type badge component.
 * 
 * Displays a Pokémon type (e.g., "Fire", "Water") with official type colors.
 * Material theme uses filled badges, Unstyled theme uses border-only badges.
 * 
 * Features:
 * - Official Pokémon type colors from PokemonTypeColors
 * - Configurable fill alpha (0f = outline, 1f = filled)
 * - Configurable border width
 * - Optional override for border width
 * 
 * @param type Pokémon type name (e.g., "fire", "water")
 * @param isDark Whether dark mode is active
 * @param tokens Badge styling tokens from design system
 * @param modifier Modifier for the badge container
 * @param overrideBorderWidth Optional custom border width (overrides tokens.borderWidth)
 */
@Composable
fun TypeBadge(
    type: String,
    isDark: Boolean,
    tokens: BadgeTokens,
    modifier: Modifier = Modifier,
    overrideBorderWidth: Dp? = null
) {
    val typeColor = PokemonTypeColors.getBackground(type, isDark)
    val borderWidth = overrideBorderWidth ?: tokens.borderWidth
    
    BasicText(
        text = type.lowercase().replaceFirstChar { it.uppercase() },
        style = TextStyle(color = tokens.textColor, fontSize = 14.sp),
        modifier = modifier
            .clip(tokens.shape)
            .background(typeColor.copy(alpha = tokens.fillAlpha))
            .then(
                if (borderWidth > 0.dp) {
                    Modifier.border(borderWidth, typeColor, tokens.shape)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
