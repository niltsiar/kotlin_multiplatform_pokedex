package com.minddistrict.multiplatformpoc.features.pokemonlist.ui.material

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minddistrict.multiplatformpoc.core.designsystem.material.tokens.tokens
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon
import com.minddistrict.multiplatformpoc.features.pokemonlist.presentation.PokemonListUiState
import com.minddistrict.multiplatformpoc.features.pokemonlist.presentation.PokemonListViewModel
import com.minddistrict.multiplatformpoc.features.pokemonlist.ui.material.components.ErrorState
import com.minddistrict.multiplatformpoc.features.pokemonlist.ui.material.components.LoadingState
import com.minddistrict.multiplatformpoc.features.pokemonlist.ui.material.components.PokemonListGrid
import kotlinx.coroutines.flow.collectLatest

/**
 * Material Design 3 Pokémon list screen.
 * 
 * Main entry point for the Pokémon list feature with Material styling.
 * Coordinates ViewModel state with composable UI components.
 * 
 * @param viewModel ViewModel managing list state and pagination
 * @param onPokemonClick Callback when a Pokémon is clicked
 * @param modifier Modifier for the screen container
 */
@Composable
fun PokemonListMaterialScreen(
    viewModel: PokemonListViewModel,
    modifier: Modifier = Modifier,
    onPokemonClick: (Pokemon) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    PokemonListMaterialContent(
        uiState = uiState,
        restoredScrollIndex = viewModel.restoredScrollIndex,
        restoredScrollOffset = viewModel.restoredScrollOffset,
        onLoadMore = viewModel::loadNextPage,
        onPokemonClick = {
            viewModel.onPokemonSelected(it.id)
            onPokemonClick(it)
        },
        onScrollPositionChanged = viewModel::onScrollPositionChanged,
        modifier = modifier
    )
}

/**
 * Material Design 3 Pokémon list content.
 * 
 * Renders different states (Loading, Error, Content) using focused components.
 * 
 * @param uiState Current UI state (Loading, Error, or Content)
 * @param restoredScrollIndex Restored scroll position index
 * @param restoredScrollOffset Restored scroll position offset
 * @param onLoadMore Callback for pagination
 * @param onPokemonClick Callback when Pokémon is clicked
 * @param onScrollPositionChanged Callback to save scroll position
 * @param modifier Modifier for the content container
 */
@Composable
internal fun PokemonListMaterialContent(
    uiState: PokemonListUiState,
    restoredScrollIndex: Int,
    restoredScrollOffset: Int,
    onLoadMore: () -> Unit,
    onPokemonClick: (Pokemon) -> Unit,
    onScrollPositionChanged: (firstVisibleItemIndex: Int, firstVisibleItemScrollOffset: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is PokemonListUiState.Loading -> {
            LoadingState(modifier = modifier)
        }
        
        is PokemonListUiState.Error -> {
            ErrorState(
                message = uiState.message,
                onRetry = onLoadMore,
                modifier = modifier
            )
        }
        
        is PokemonListUiState.Content -> {
            val gridState = rememberLazyGridState(
                initialFirstVisibleItemIndex = restoredScrollIndex,
                initialFirstVisibleItemScrollOffset = restoredScrollOffset,
            )

            LaunchedEffect(gridState) {
                snapshotFlow {
                    gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset
                }.collectLatest { (index, offset) ->
                    onScrollPositionChanged(index, offset)
                }
            }
            
            Box(modifier = modifier.fillMaxSize()) {
                PokemonListGrid(
                    pokemons = uiState.pokemons,
                    gridState = gridState,
                    onPokemonClick = onPokemonClick,
                    onLoadMore = onLoadMore,
                    isLoadingMore = uiState.isLoadingMore,
                    hasMore = uiState.hasMore
                )
                
                if (uiState.isLoadingMore) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(MaterialTheme.tokens.spacing.medium)
                            .align(Alignment.BottomCenter),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}
