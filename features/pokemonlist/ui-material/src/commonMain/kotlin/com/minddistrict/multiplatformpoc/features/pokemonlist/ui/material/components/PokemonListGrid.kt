package com.minddistrict.multiplatformpoc.features.pokemonlist.ui.material.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
                    bottom = MaterialTheme.tokens.spacing.medium
                )
            )
            .asPaddingValues(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.tokens.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.tokens.spacing.medium),
        modifier = modifier.fillMaxSize()
    ) {
        itemsIndexed(
            items = pokemons,
            key = { _, pokemon -> pokemon.id }
        ) { index, pokemon ->
            val alpha = remember { Animatable(0f) }
            val offsetY = remember { Animatable(20f) }
            
            LaunchedEffect(Unit) {
                val delay = index * 50L // 50ms stagger
                alpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 300, delayMillis = delay.toInt())
                )
                offsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 300, delayMillis = delay.toInt())
                )
            }
            
            PokemonListCard(
                pokemon = pokemon,
                onClick = { onPokemonClick(pokemon) },
                modifier = Modifier.graphicsLayer {
                    this.alpha = alpha.value
                    translationY = offsetY.value
                }
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
