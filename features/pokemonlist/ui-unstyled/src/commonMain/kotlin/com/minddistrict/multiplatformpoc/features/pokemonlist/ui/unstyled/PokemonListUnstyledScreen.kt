package com.minddistrict.multiplatformpoc.features.pokemonlist.ui.unstyled

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeunstyled.theme.Theme
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.background
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.colors
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.spacing
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.spacingMd
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon
import com.minddistrict.multiplatformpoc.features.pokemonlist.presentation.PokemonListUiState
import com.minddistrict.multiplatformpoc.features.pokemonlist.presentation.PokemonListViewModel
import com.minddistrict.multiplatformpoc.features.pokemonlist.ui.unstyled.components.ErrorStateUnstyled
import com.minddistrict.multiplatformpoc.features.pokemonlist.ui.unstyled.components.LoadingStateUnstyled
import com.minddistrict.multiplatformpoc.features.pokemonlist.ui.unstyled.components.PokemonListGridUnstyled

/**
 * Unstyled Pokémon list screen.
 * 
 * Displays a grid of Pokémon with minimalist design using Unstyled components:
 * - Flat cards with border-only styling
 * - Minimal elevation (1dp)
 * - Linear motion
 * - Clean visual hierarchy
 * 
 * @param viewModel ViewModel providing Pokémon data
 * @param onPokemonClick Callback when a Pokémon is clicked
 * @param modifier Modifier for the screen container
 */
@Composable
fun PokemonListUnstyledScreen(
    viewModel: PokemonListViewModel,
    modifier: Modifier = Modifier,
    onPokemonClick: (Pokemon) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PokemonListContentUnstyled(
        uiState = uiState,
        restoredScrollIndex = viewModel.restoredScrollIndex,
        restoredScrollOffset = viewModel.restoredScrollOffset,
        onLoadMore = viewModel::loadNextPage,
        onPokemonClick = {
            viewModel.onPokemonSelected(it.id)
            onPokemonClick(it)
        },
        onScrollPositionChanged = viewModel::onScrollPositionChanged,
        modifier = modifier,
    )
}

@Composable
internal fun PokemonListContentUnstyled(
    uiState: PokemonListUiState,
    restoredScrollIndex: Int,
    restoredScrollOffset: Int,
    onLoadMore: () -> Unit,
    onPokemonClick: (Pokemon) -> Unit = {},
    onScrollPositionChanged: (firstVisibleItemIndex: Int, firstVisibleItemScrollOffset: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Theme[colors][background])
    ) {
        when (uiState) {
            is PokemonListUiState.Loading -> {
                LoadingStateUnstyled()
            }

            is PokemonListUiState.Error -> {
                ErrorStateUnstyled(
                    message = uiState.message,
                    onRetry = { /* TODO: Add retry */ }
                )
            }

            is PokemonListUiState.Content -> {
                PokemonListGridUnstyled(
                    pokemon = uiState.pokemons,
                    onPokemonClick = onPokemonClick,
                    onLoadMore = onLoadMore,
                    restoredScrollIndex = restoredScrollIndex,
                    restoredScrollOffset = restoredScrollOffset,
                    onScrollPositionChanged = onScrollPositionChanged
                )
            }
        }
    }
}
