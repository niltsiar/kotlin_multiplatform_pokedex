package com.minddistrict.multiplatformpoc.features.pokemonlist.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kotlinx.collections.immutable.persistentListOf

@Composable
fun PokemonListScreen(
    viewModel: PokemonListViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.loadInitialPage()
    }
    
    PokemonListContent(
        uiState = uiState,
        onLoadMore = viewModel::loadNextPage,
        modifier = modifier
    )
}

@Composable
private fun PokemonListContent(
    uiState: PokemonListUiState,
    onLoadMore: () -> Unit,
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
            val gridState = rememberLazyGridState()
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = modifier.fillMaxSize()
            ) {
                itemsIndexed(
                    items = uiState.pokemons,
                    key = { _, pokemon -> pokemon.id }
                ) { index, pokemon ->
                    PokemonCard(pokemon = pokemon)
                    
                    // Load more when near end
                    if (index >= uiState.pokemons.size - 4 && !uiState.isLoadingMore && uiState.hasMore) {
                        LaunchedEffect(Unit) {
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
    pokemon: com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon,
    modifier: Modifier = Modifier
) {
    Card(
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
    MaterialTheme {
        Surface {
            PokemonCard(
                pokemon = com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon(
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
    MaterialTheme {
        Surface {
            PokemonCard(
                pokemon = com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon(
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
    MaterialTheme {
        Surface {
            PokemonListContent(
                uiState = PokemonListUiState.Loading,
                onLoadMore = {}
            )
        }
    }
}

@Preview
@Composable
private fun PokemonListErrorPreview() {
    MaterialTheme {
        Surface {
            PokemonListContent(
                uiState = PokemonListUiState.Error("Network error. Please check your connection."),
                onLoadMore = {}
            )
        }
    }
}

@Preview
@Composable
private fun PokemonListContentPreview() {
    MaterialTheme {
        Surface {
            PokemonListContent(
                uiState = PokemonListUiState.Content(
                    pokemons = persistentListOf(
                        com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon(
                            id = 1,
                            name = "Bulbasaur",
                            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png"
                        ),
                        com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon(
                            id = 4,
                            name = "Charmander",
                            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/4.png"
                        ),
                        com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon(
                            id = 7,
                            name = "Squirtle",
                            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/7.png"
                        ),
                        com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon(
                            id = 25,
                            name = "Pikachu",
                            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png"
                        ),
                        com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon(
                            id = 133,
                            name = "Eevee",
                            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/133.png"
                        ),
                        com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon(
                            id = 150,
                            name = "Mewtwo",
                            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/150.png"
                        )
                    ),
                    isLoadingMore = false,
                    hasMore = true
                ),
                onLoadMore = {}
            )
        }
    }
}

@Preview
@Composable
private fun PokemonListContentLoadingMorePreview() {
    MaterialTheme {
        Surface {
            PokemonListContent(
                uiState = PokemonListUiState.Content(
                    pokemons = persistentListOf(
                        com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon(
                            id = 1,
                            name = "Bulbasaur",
                            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png"
                        ),
                        com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon(
                            id = 4,
                            name = "Charmander",
                            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/4.png"
                        )
                    ),
                    isLoadingMore = true,
                    hasMore = true
                ),
                onLoadMore = {}
            )
        }
    }
}
