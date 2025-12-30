package com.minddistrict.multiplatformpoc.features.pokemonlist.ui.material.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.min
import kotlinx.coroutines.launch
import com.minddistrict.multiplatformpoc.core.designsystem.core.gridColumns
import com.minddistrict.multiplatformpoc.core.designsystem.material.tokens.tokens
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon
import kotlinx.collections.immutable.ImmutableList

/**
 * Material Design 3 adaptive grid for Pokémon list.
 *
 * Displays Pokémon in a responsive grid (2/3/4 columns based on window size)
 * with token-based spacing and staggered entrance animations.
 *
 * Features:
 * - Adaptive columns using WindowSizeClass
 * - Safe area insets + token-based spacing
 * - Staggered fade + slide animations per item
 *
 * @param pokemons List of Pokémon to display
 * @param gridState Scroll state for the grid
 * @param onPokemonClick Callback when a Pokémon is clicked
 * @param onLoadMore Callback when user scrolls near end (pagination)
 * @param isLoadingMore Whether more items are being loaded
 * @param hasMore Whether there are more items to load
 * @param modifier Modifier for the grid container
 */
@Composable
fun PokemonListGrid(
    pokemons: ImmutableList<Pokemon>,
    gridState: LazyGridState,
    onPokemonClick: (Pokemon) -> Unit,
    onLoadMore: () -> Unit,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    modifier: Modifier = Modifier
) {
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val columns = gridColumns(windowAdaptiveInfo)

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        state = gridState,
        contentPadding = WindowInsets.safeDrawing
            .add(
                WindowInsets(
                    left = MaterialTheme.tokens.spacing.medium,
                    top = MaterialTheme.tokens.spacing.medium,
                    right = MaterialTheme.tokens.spacing.medium,
                    bottom = MaterialTheme.tokens.spacing.medium,
                ),
            )
            .asPaddingValues(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.tokens.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.tokens.spacing.medium),
        modifier = modifier.fillMaxSize(),
    ) {
        itemsIndexed(
            items = pokemons,
            key = { _, pokemon -> pokemon.id },
        ) { index, pokemon ->
            val alpha = remember { Animatable(0f) }
            val scale = remember { Animatable(0.8f) }  // Start smaller for smoother effect
            val motionTokens = MaterialTheme.tokens.motion

            LaunchedEffect(Unit) {
                // Only stagger first 8 items (visible viewport)
                // Items beyond viewport appear instantly when scrolled into view
                val visibleIndex = min(index, 8)
                val delay = visibleIndex * 40L  // 40ms stagger for better visual rhythm
                
                // Animate both properties simultaneously for smooth coordinated motion
                launch {
                    alpha.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = 400,  // Longer duration for smoother fade
                            delayMillis = delay.toInt(),
                            easing = motionTokens.easingEmphasizedDecelerate
                        ),
                    )
                }
                launch {
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = 400,  // Match alpha duration
                            delayMillis = delay.toInt(),
                            easing = motionTokens.easingEmphasizedDecelerate
                        ),
                    )
                }
            }

            PokemonListCard(
                pokemon = pokemon,
                onClick = { onPokemonClick(pokemon) },
                modifier = Modifier.graphicsLayer {
                    this.alpha = alpha.value
                    scaleX = scale.value
                    scaleY = scale.value
                },
            )

            // Load more when near end
            LaunchedEffect(index, pokemons.size, isLoadingMore, hasMore) {
                if (index >= pokemons.size - 4 && !isLoadingMore && hasMore) {
                    onLoadMore()
                }
            }
        }
    }
}
