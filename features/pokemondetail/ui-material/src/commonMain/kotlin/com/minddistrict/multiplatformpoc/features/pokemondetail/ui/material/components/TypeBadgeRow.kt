package com.minddistrict.multiplatformpoc.features.pokemondetail.ui.material.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.minddistrict.multiplatformpoc.core.designsystem.core.components.TypeBadge
import com.minddistrict.multiplatformpoc.core.designsystem.material.tokens.MaterialComponentTokens
import com.minddistrict.multiplatformpoc.core.designsystem.material.tokens.tokens
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.TypeOfPokemon
import kotlinx.collections.immutable.ImmutableList

/**
 * Material Design 3 type badge row for Pokémon detail.
 * 
 * Displays Pokémon types as filled badges with staggered entrance animations.
 * Uses shared TypeBadge component with Material tokens.
 * 
 * Features:
 * - FlowRow layout for wrapping
 * - Staggered fade + slide animations (50ms delay per badge)
 * - Token-based spacing
 * 
 * @param types List of Pokémon types
 * @param modifier Modifier for the row container
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TypeBadgeRow(
    types: ImmutableList<TypeOfPokemon>,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    
    val componentTokens = MaterialTheme.tokens
    
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(componentTokens.spacing.xs),
        modifier = modifier
    ) {
        types.forEachIndexed { index, type ->
            val alpha = remember { Animatable(0f) }
            val offsetY = remember { Animatable(20f) }
            
            LaunchedEffect(Unit) {
                val delay = index * 25L  // 25ms stagger (was 50ms)
                alpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 200,  // 200ms duration (was 300ms)
                        delayMillis = delay.toInt(),
                        easing = EaseOutCubic
                    )
                )
                offsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = 200,
                        delayMillis = delay.toInt(),
                        easing = EaseOutCubic
                    )
                )
            }
            
            TypeBadge(
                type = type.name,
                isDark = isDark,
                tokens = MaterialComponentTokens.badge(),
                modifier = Modifier.graphicsLayer {
                    this.alpha = alpha.value
                    translationY = offsetY.value
                }
            )
        }
    }
}
