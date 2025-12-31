package com.minddistrict.multiplatformpoc.features.pokemonlist.ui.unstyled.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.composeunstyled.Text
import com.composeunstyled.platformtheme.shapes
import com.composeunstyled.platformtheme.indications
import com.composeunstyled.platformtheme.sizeDefault
import com.composeunstyled.platformtheme.interactiveSizes
import com.composeunstyled.theme.Theme
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.*
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.tokens.UnstyledComponentTokens
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon

/**
 * Unstyled Pokémon list card component.
 * 
 * Displays a Pokémon with image, ID, and name in a minimal flat card with
 * border-only styling and subtle hover effect (brightness 1.1f).
 * 
 * Uses Unstyled tokens for clean, minimalist aesthetic:
 * - Flat elevation (1dp)
 * - Border-only outline (no fill)
 * - Subtle hover state
 * 
 * @param pokemon Pokémon data to display
 * @param onClick Callback when card is clicked
 * @param modifier Modifier for the card container
 */
@Composable
fun PokemonListCardUnstyled(
    pokemon: Pokemon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val brightness by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.95f
            isHovered -> 1.15f  // More noticeable brightness increase on hover
            else -> 1f
        },
        animationSpec = tween(durationMillis = com.minddistrict.multiplatformpoc.core.designsystem.unstyled.tokens.UnstyledTokens.motion.durationShort),
        label = "cardBrightness"
    )
    
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.98f
            isHovered -> 1.02f  // Slight grow on hover
            else -> 1f
        },
        animationSpec = tween(durationMillis = com.minddistrict.multiplatformpoc.core.designsystem.unstyled.tokens.UnstyledTokens.motion.durationShort),
        label = "cardScale"
    )
    
    val borderAlpha by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.3f
            isHovered -> 0.5f  // More prominent border on hover
            else -> 0.2f
        },
        animationSpec = tween(durationMillis = com.minddistrict.multiplatformpoc.core.designsystem.unstyled.tokens.UnstyledTokens.motion.durationShort),
        label = "borderAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(Theme[shapes][shapeMedium])
            .border(
                width = 1.dp,
                color = Theme[colors][onSurface].copy(alpha = borderAlpha),
                shape = Theme[shapes][shapeMedium]
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .hoverable(interactionSource = interactionSource)
            .padding(Theme[spacing][spacingSm]),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AsyncImage(
            model = pokemon.imageUrl,
            contentDescription = pokemon.name,
            modifier = Modifier
                .size(96.dp)
                .graphicsLayer {
                    val matrix = ColorMatrix().apply {
                        setToScale(brightness, brightness, brightness, 1f)
                    }
                    colorFilter = ColorFilter.colorMatrix(matrix)
                },
        )
        
        Text(
            text = "#${pokemon.id.toString().padStart(3, '0')}",
            style = Theme[typography][labelSmall],
            color = Theme[colors][onSurface].copy(alpha = 0.6f),
            modifier = Modifier.padding(top = Theme[spacing][spacingXs])
        )
        
        Text(
            text = pokemon.name,
            style = Theme[typography][bodyMedium].copy(fontWeight = FontWeight.Medium),
            color = Theme[colors][onSurface],
        )
    }
}

@Preview
@Composable
private fun PokemonListCardUnstyledPreview() {
    UnstyledTheme {
        PokemonListCardUnstyled(
            pokemon = Pokemon(
                id = 1,
                name = "Bulbasaur",
                imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png"
            ),
            onClick = {}
        )
    }
}
