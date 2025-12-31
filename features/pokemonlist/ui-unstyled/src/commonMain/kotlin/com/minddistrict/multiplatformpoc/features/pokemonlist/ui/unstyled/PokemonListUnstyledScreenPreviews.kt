package com.minddistrict.multiplatformpoc.features.pokemonlist.ui.unstyled

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.UnstyledTheme
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon
import com.minddistrict.multiplatformpoc.features.pokemonlist.presentation.PokemonListUiState
import kotlinx.collections.immutable.persistentListOf

/**
 * Unstyled Pokémon list screen previews.
 * 
 * Demonstrates:
 * - Loading state with minimal progress indicator
 * - Error state with clean retry button
 * - Content state with grid of Pokémon
 * - Loading more state
 */

@Preview(name = "Loading State")
@Composable
private fun PokemonListUnstyledScreenLoadingPreview() {
    UnstyledTheme {
        PokemonListContentUnstyled(
            uiState = PokemonListUiState.Loading,
            restoredScrollIndex = 0,
            restoredScrollOffset = 0,
            onLoadMore = {},
            onPokemonClick = {},
            onScrollPositionChanged = { _, _ -> }
        )
    }
}

@Preview(name = "Error State")
@Composable
private fun PokemonListUnstyledScreenErrorPreview() {
    UnstyledTheme {
        PokemonListContentUnstyled(
            uiState = PokemonListUiState.Error("Failed to load Pokémon data"),
            restoredScrollIndex = 0,
            restoredScrollOffset = 0,
            onLoadMore = {},
            onPokemonClick = {},
            onScrollPositionChanged = { _, _ -> }
        )
    }
}

@Preview(name = "Content State")
@Composable
private fun PokemonListUnstyledScreenContentPreview() {
    UnstyledTheme {
        PokemonListContentUnstyled(
            uiState = PokemonListUiState.Content(
                pokemons = persistentListOf(
                    Pokemon(1, "Bulbasaur", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png"),
                    Pokemon(4, "Charmander", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/4.png"),
                    Pokemon(7, "Squirtle", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/7.png"),
                    Pokemon(25, "Pikachu", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png")
                ),
                hasMore = true,
                isLoadingMore = false
            ),
            restoredScrollIndex = 0,
            restoredScrollOffset = 0,
            onLoadMore = {},
            onPokemonClick = {},
            onScrollPositionChanged = { _, _ -> }
        )
    }
}

@Preview(name = "Loading More State")
@Composable
private fun PokemonListUnstyledScreenLoadingMorePreview() {
    UnstyledTheme {
        PokemonListContentUnstyled(
            uiState = PokemonListUiState.Content(
                pokemons = persistentListOf(
                    Pokemon(1, "Bulbasaur", ""),
                    Pokemon(4, "Charmander", "")
                ),
                hasMore = true,
                isLoadingMore = true
            ),
            restoredScrollIndex = 0,
            restoredScrollOffset = 0,
            onLoadMore = {},
            onPokemonClick = {},
            onScrollPositionChanged = { _, _ -> }
        )
    }
}
