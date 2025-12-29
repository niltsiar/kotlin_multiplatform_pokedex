package com.minddistrict.multiplatformpoc.features.pokemonlist.ui.material

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import com.minddistrict.multiplatformpoc.core.designsystem.core.gridColumns
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.collectLatest
import com.minddistrict.multiplatformpoc.core.designsystem.theme.PokemonTheme
import com.minddistrict.multiplatformpoc.features.pokemonlist.presentation.PokemonListViewModel
import com.minddistrict.multiplatformpoc.features.pokemonlist.presentation.PokemonListUiState
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon

@Composable
fun PokemonListScreenMaterial(
    viewModel: PokemonListViewModel,
    modifier: Modifier = Modifier,
    onPokemonClick: (Pokemon) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    PokemonListContent(
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
private fun PokemonListContent(
    uiState: PokemonListUiState,
    restoredScrollIndex: Int,
    restoredScrollOffset: Int,
    onLoadMore: () -> Unit,
    onPokemonClick: (Pokemon) -> Unit = {},
    onScrollPositionChanged: (firstVisibleItemIndex: Int, firstVisibleItemScrollOffset: Int) -> Unit,
    modifier: Modifier = Modifier
) {
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
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        
        is PokemonListUiState.Content -> {
            val windowAdaptiveInfo = currentWindowAdaptiveInfo()
            val columns = gridColumns(windowAdaptiveInfo)
            
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
                columns = GridCells.Fixed(columns),
                state = gridState,
                contentPadding = WindowInsets.safeDrawing
                    .add(WindowInsets(left = 16.dp, top = 16.dp, right = 16.dp, bottom = 16.dp))
                    .asPaddingValues(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = modifier.fillMaxSize()
            ) {
                itemsIndexed(
                    items = uiState.pokemons,
                    key = { _, pokemon -> pokemon.id }
                ) { index, pokemon ->
                    PokemonCard(
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
                                .padding(16.dp),
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

@Composable
private fun PokemonCard(
    pokemon: Pokemon,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = pokemon.imageUrl,
                contentDescription = pokemon.name,
                modifier = Modifier
                    .size(96.dp)
                    .padding(8.dp)
            )
            
            Text(
                text = "#${pokemon.id}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = pokemon.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// Previews

@Preview
@Composable
private fun PokemonCardPreview() {
    PokemonTheme {
        Surface {
            PokemonCard(
                pokemon = Pokemon(
                    id = 25,
                    name = "Pikachu",
                    imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png"
                )
            )
        }
    }
}

@Preview
@Composable
private fun PokemonCardLongNamePreview() {
    PokemonTheme {
        Surface {
            PokemonCard(
                pokemon = Pokemon(
                    id = 1,
                    name = "Bulbasaur with long name",
                    imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png"
                )
            )
        }
    }
}

@Preview
@Composable
private fun PokemonListLoadingPreview() {
    PokemonTheme {
        Surface {
            PokemonListContent(
                uiState = PokemonListUiState.Loading,
                restoredScrollIndex = 0,
                restoredScrollOffset = 0,
                onLoadMore = {}
                ,
                onScrollPositionChanged = { _, _ -> }
            )
        }
    }
}

@Preview
@Composable
private fun PokemonListErrorPreview() {
    PokemonTheme {
        Surface {
            PokemonListContent(
                uiState = PokemonListUiState.Error("Network error. Please check your connection."),
                restoredScrollIndex = 0,
                restoredScrollOffset = 0,
                onLoadMore = {}
                ,
                onScrollPositionChanged = { _, _ -> }
            )
        }
    }
}

@Preview
@Composable
private fun PokemonListContentPreview() {
    PokemonTheme {
        Surface {
            PokemonListContent(
                uiState = PokemonListUiState.Content(
                    pokemons = persistentListOf(
                        Pokemon(
                            id = 1,
                            name = "Bulbasaur",
                            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png"
                        ),
                        Pokemon(
                            id = 4,
                            name = "Charmander",
                            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/4.png"
                        ),
                        Pokemon(
                            id = 7,
                            name = "Squirtle",
                            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/7.png"
                        ),
                        Pokemon(
                            id = 25,
                            name = "Pikachu",
                            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png"
                        ),
                        Pokemon(
                            id = 133,
                            name = "Eevee",
                            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/133.png"
                        ),
                        Pokemon(
                            id = 150,
                            name = "Mewtwo",
                            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/150.png"
                        )
                    ),
                    isLoadingMore = false,
                    hasMore = true
                ),
                restoredScrollIndex = 0,
                restoredScrollOffset = 0,
                onLoadMore = {},
                onScrollPositionChanged = { _, _ -> }
            )
        }
    }
}

@Preview
@Composable
private fun PokemonListContentLoadingMorePreview() {
    PokemonTheme {
        Surface {
            PokemonListContent(
                uiState = PokemonListUiState.Content(
                    pokemons = persistentListOf(
                        Pokemon(
                            id = 1,
                            name = "Bulbasaur",
                            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png"
                        ),
                        Pokemon(
                            id = 4,
                            name = "Charmander",
                            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/4.png"
                        )
                    ),
                    isLoadingMore = true,
                    hasMore = true
                ),
                restoredScrollIndex = 0,
                restoredScrollOffset = 0,
                onLoadMore = {},
                onScrollPositionChanged = { _, _ -> }
            )
        }
    }
}
