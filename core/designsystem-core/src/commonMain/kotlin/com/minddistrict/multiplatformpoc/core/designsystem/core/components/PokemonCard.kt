package com.minddistrict.multiplatformpoc.core.designsystem.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp

/**
 * Theme-agnostic Pokémon card component.
 * 
 * This component accepts [CardTokens] to define its appearance, allowing it to be
 * styled differently in Material (elevated, rounded) and Unstyled (flat, minimal) themes.
 * 
 * Features:
 * - Pressed state scaling
 * - Configurable shape and elevation
 * - Optional token overrides for customization
 * 
 * @param tokens Card styling tokens from design system
 * @param onClick Callback when card is clicked
 * @param modifier Modifier for the card container
 * @param overrideShape Optional custom shape (overrides tokens.shape)
 * @param overrideElevation Optional custom elevation (overrides tokens.elevation)
 * @param content Card content (typically image + text)
 */
@Composable
fun PokemonCard(
    tokens: CardTokens,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    overrideShape: Shape? = null,
    overrideElevation: Dp? = null,
    content: @Composable () -> Unit
) {
    val shape = overrideShape ?: tokens.shape
    val elevation = overrideElevation ?: tokens.elevation
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale = if (isPressed) tokens.pressedScale else 1f
    
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation = elevation, shape = shape)
            .clip(shape)
            .background(tokens.backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        content()
    }
}
