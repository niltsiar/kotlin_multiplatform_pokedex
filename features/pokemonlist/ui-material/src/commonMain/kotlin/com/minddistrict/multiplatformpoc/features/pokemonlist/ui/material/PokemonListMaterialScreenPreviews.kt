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
                    name = "Pikachu",
                    detailUrl = "https://pokeapi.co/api/v2/pokemon/25/"
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
                    name = "Bulbasaur with long name",
                    detailUrl = "https://pokeapi.co/api/v2/pokemon/1/"
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
                            name = "Bulbasaur",
                            detailUrl = "https://pokeapi.co/api/v2/pokemon/1/"
                        ),
                        Pokemon(
                            name = "Charmander",
                            detailUrl = "https://pokeapi.co/api/v2/pokemon/4/"
                        ),
                        Pokemon(
                            name = "Squirtle",
                            detailUrl = "https://pokeapi.co/api/v2/pokemon/7/"
                        ),
                        Pokemon(
                            name = "Pikachu",
                            detailUrl = "https://pokeapi.co/api/v2/pokemon/25/"
                        ),
                        Pokemon(
                            name = "Eevee",
                            detailUrl = "https://pokeapi.co/api/v2/pokemon/133/"
                        ),
                        Pokemon(
                            name = "Mewtwo",
                            detailUrl = "https://pokeapi.co/api/v2/pokemon/150/"
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
                            name = "Bulbasaur",
                            detailUrl = "https://pokeapi.co/api/v2/pokemon/1/"
                        ),
                        Pokemon(
                            name = "Charmander",
                            detailUrl = "https://pokeapi.co/api/v2/pokemon/4/"
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
