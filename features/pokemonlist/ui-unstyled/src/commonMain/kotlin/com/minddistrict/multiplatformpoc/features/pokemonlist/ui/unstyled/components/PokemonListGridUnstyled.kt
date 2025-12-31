package com.minddistrict.multiplatformpoc.features.pokemonlist.ui.unstyled.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composeunstyled.theme.Theme
import com.minddistrict.multiplatformpoc.core.designsystem.core.gridColumns
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.UnstyledTheme
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.spacing
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.spacingLg
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.spacingMd
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.collectLatest

/**
 * Unstyled Pokémon grid component.
 * 
 * Displays a grid of Pokémon cards with adaptive columns (2/3/4) based on window size.
 * Uses UnstyledTokens.spacing.large (20dp) for clean spacing.
 * 
 * Features:
 * - Adaptive grid layout (2-4 columns)
 * - Infinite scroll with load more callback
 * - Scroll position restoration
 * - Minimal spacing for clean aesthetic
 * 
 * @param pokemon List of Pokémon to display
 * @param onPokemonClick Callback when a Pokémon card is clicked
 * @param onLoadMore Callback when user scrolls near the end
 * @param modifier Modifier for the grid container
 * @param restoredScrollIndex Initial scroll position (index)
 * @param restoredScrollOffset Initial scroll position (offset)
 * @param onScrollPositionChanged Callback for saving scroll position
 */
@Composable
fun PokemonListGridUnstyled(
    pokemon: ImmutableList<Pokemon>,
    onPokemonClick: (Pokemon) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    restoredScrollIndex: Int = 0,
    restoredScrollOffset: Int = 0,
    onScrollPositionChanged: (firstVisibleItemIndex: Int, firstVisibleItemScrollOffset: Int) -> Unit = { _, _ -> }
) {
    val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = restoredScrollIndex,
        initialFirstVisibleItemScrollOffset = restoredScrollOffset
    )
    
    val windowInfo = currentWindowAdaptiveInfo()
    val columns = gridColumns(windowInfo)
    
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset }
            .collectLatest { (index, offset) ->
                onScrollPositionChanged(index, offset)
            }
    }
    
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collectLatest { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= pokemon.size - 6) {
                    onLoadMore()
                }
            }
    }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        state = gridState,
        contentPadding = PaddingValues(Theme[spacing][spacingLg]),  // 20dp for clean spacing
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(Theme[spacing][spacingLg]),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(Theme[spacing][spacingLg]),
        modifier = modifier.fillMaxSize()
    ) {
        itemsIndexed(
            items = pokemon,
            key = { _, item -> item.id }
        ) { _, item ->
            PokemonListCardUnstyled(
                pokemon = item,
                onClick = { onPokemonClick(item) }
            )
        }
    }
}

@Preview
@Composable
private fun PokemonListGridUnstyledPreview() {
    UnstyledTheme {
        PokemonListGridUnstyled(
            pokemon = persistentListOf(
                Pokemon(1, "Bulbasaur", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png"),
                Pokemon(4, "Charmander", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/4.png"),
                Pokemon(7, "Squirtle", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/7.png"),
                Pokemon(25, "Pikachu", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png")
            ),
            onPokemonClick = {},
            onLoadMore = {}
        )
    }
}
