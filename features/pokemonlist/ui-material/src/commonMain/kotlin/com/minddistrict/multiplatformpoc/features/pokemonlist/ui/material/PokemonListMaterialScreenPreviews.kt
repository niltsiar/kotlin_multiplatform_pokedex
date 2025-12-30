package com.minddistrict.multiplatformpoc.features.pokemonlist.ui.material

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.minddistrict.multiplatformpoc.core.designsystem.material.theme.PokemonTheme
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon
import com.minddistrict.multiplatformpoc.features.pokemonlist.presentation.PokemonListUiState
import com.minddistrict.multiplatformpoc.features.pokemonlist.ui.material.components.PokemonListCard
import kotlinx.collections.immutable.persistentListOf

/**
 * Material Design 3 Pokémon list screen previews.
 * 
 * Demonstrates different states with realistic sample data.
 */

@Preview(name = "Single Card")
@Composable
private fun PokemonListCardPreview() {
    PokemonTheme {
        Surface {
            PokemonListCard(
                pokemon = Pokemon(
                    id = 25,
                    name = "Pikachu",
                    imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png"
                ),
                onClick = {}
            )
        }
    }
}

@Preview(name = "Card - Long Name")
@Composable
private fun PokemonListCardLongNamePreview() {
    PokemonTheme {
        Surface {
            PokemonListCard(
                pokemon = Pokemon(
                    id = 1,
                    name = "Bulbasaur with long name",
                    imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png"
                ),
                onClick = {}
            )
        }
    }
}

@Preview(name = "Loading State")
@Composable
private fun PokemonListLoadingPreview() {
    PokemonTheme {
        Surface {
            PokemonListMaterialContent(
                uiState = PokemonListUiState.Loading,
                restoredScrollIndex = 0,
                restoredScrollOffset = 0,
                onLoadMore = {},
                onPokemonClick = {},
                onScrollPositionChanged = { _, _ -> }
            )
        }
    }
}

@Preview(name = "Error State")
@Composable
private fun PokemonListErrorPreview() {
    PokemonTheme {
        Surface {
            PokemonListMaterialContent(
                uiState = PokemonListUiState.Error("Network error. Please check your connection."),
                restoredScrollIndex = 0,
                restoredScrollOffset = 0,
                onLoadMore = {},
                onPokemonClick = {},
                onScrollPositionChanged = { _, _ -> }
            )
        }
    }
}

@Preview(name = "Content State")
@Composable
private fun PokemonListContentPreview() {
    PokemonTheme {
        Surface {
            PokemonListMaterialContent(
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
                onPokemonClick = {},
                onScrollPositionChanged = { _, _ -> }
            )
        }
    }
}

@Preview(name = "Content with Loading More")
@Composable
private fun PokemonListContentLoadingMorePreview() {
    PokemonTheme {
        Surface {
            PokemonListMaterialContent(
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
                onPokemonClick = {},
                onScrollPositionChanged = { _, _ -> }
            )
        }
    }
}
