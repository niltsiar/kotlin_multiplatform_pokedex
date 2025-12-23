package com.minddistrict.multiplatformpoc.features.pokemonlist.ui.unstyled

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.UnstyledTheme
import com.minddistrict.multiplatformpoc.core.designsystem.core.PokemonTypeColors
import com.minddistrict.multiplatformpoc.core.designsystem.core.Spacing
import com.minddistrict.multiplatformpoc.core.designsystem.core.Corners
import com.minddistrict.multiplatformpoc.core.designsystem.core.Elevation
import com.minddistrict.multiplatformpoc.features.pokemonlist.presentation.PokemonListViewModel
import com.minddistrict.multiplatformpoc.features.pokemonlist.presentation.PokemonListUiState
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon

@Composable
fun PokemonListScreenUnstyled(
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
        modifier = modifier
    )
}

@Composable
private fun PokemonListContentUnstyled(
    uiState: PokemonListUiState,
    restoredScrollIndex: Int,
    restoredScrollOffset: Int,
    onLoadMore: () -> Unit,
    onPokemonClick: (Pokemon) -> Unit = {},
    onScrollPositionChanged: (firstVisibleItemIndex: Int, firstVisibleItemScrollOffset: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    UnstyledTheme {
        when (uiState) {
            is PokemonListUiState.Loading -> {
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is PokemonListUiState.Error -> {
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(Spacing.md),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        Text(
                            text = uiState.message,
                            color = UnstyledTheme.colorScheme.error
                        )
                    }
                }
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
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    contentPadding = PaddingValues(Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    modifier = modifier
                        .fillMaxSize()
                        .background(UnstyledTheme.colorScheme.background)
                ) {
                    itemsIndexed(
                        items = uiState.pokemons,
                        key = { _, pokemon -> pokemon.id }
                    ) { index, pokemon ->
                        PokemonCardUnstyled(
                            pokemon = pokemon,
                            onClick = { onPokemonClick(pokemon) }
                        )
                        
                        // Load more when near end
                        LaunchedEffect(index, uiState.pokemons.size, uiState.isLoadingMore, uiState.hasMore) {
                            if (index >= uiState.pokemons.size - 4 && !uiState.isLoadingMore && uiState.hasMore) {
                                onLoadMore()
                            }
                        }
                    }
                    
                    if (uiState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.md),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PokemonCardUnstyled(
    pokemon: Pokemon,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(
                elevation = Elevation.level2,
                shape = RoundedCornerShape(Corners.md)
            )
            .clip(RoundedCornerShape(Corners.md))
            .background(UnstyledTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = UnstyledTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                shape = RoundedCornerShape(Corners.md)
            )
            .clickable(onClick = onClick)
            .padding(Spacing.sm)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            // Pokemon Image
            AsyncImage(
                model = pokemon.imageUrl,
                contentDescription = pokemon.name,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            )
            
            // Pokemon Number
            Text(
                text = "#${pokemon.id.toString().padStart(3, '0')}",
                fontSize = 12.sp,
                color = UnstyledTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            // Pokemon Name
            Text(
                text = pokemon.name.replaceFirstChar { it.uppercase() },
                fontWeight = FontWeight.Medium,
                color = UnstyledTheme.colorScheme.onSurface,
                fontSize = 14.sp
            )
        }
    }
}
