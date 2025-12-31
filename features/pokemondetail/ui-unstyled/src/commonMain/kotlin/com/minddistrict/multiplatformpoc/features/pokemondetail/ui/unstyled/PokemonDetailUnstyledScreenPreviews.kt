package com.minddistrict.multiplatformpoc.features.pokemondetail.ui.unstyled

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.UnstyledTheme
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.Ability
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.PokemonDetail
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.Stat
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.TypeOfPokemon
import com.minddistrict.multiplatformpoc.features.pokemondetail.presentation.PokemonDetailUiState
import kotlinx.collections.immutable.persistentListOf

/**
 * Unstyled Pokémon detail screen previews.
 * 
 * Demonstrates:
 * - Loading state with minimal text
 * - Error state with clean retry button
 * - Content state with all detail sections
 */

@Preview(name = "Loading State")
@Composable
private fun PokemonDetailUnstyledScreenLoadingPreview() {
    UnstyledTheme {
        PokemonDetailContentUnstyled(
            uiState = PokemonDetailUiState.Loading,
            restoredScrollIndex = 0,
            restoredScrollOffset = 0,
            onBackClick = {},
            onRetry = {},
            onScrollPositionChanged = { _, _ -> }
        )
    }
}

@Preview(name = "Error State")
@Composable
private fun PokemonDetailUnstyledScreenErrorPreview() {
    UnstyledTheme {
        PokemonDetailContentUnstyled(
            uiState = PokemonDetailUiState.Error("Failed to load Pokémon data"),
            restoredScrollIndex = 0,
            restoredScrollOffset = 0,
            onBackClick = {},
            onRetry = {},
            onScrollPositionChanged = { _, _ -> }
        )
    }
}

@Preview(name = "Content State - Bulbasaur")
@Composable
private fun PokemonDetailUnstyledScreenContentPreview() {
    UnstyledTheme {
        PokemonDetailContentUnstyled(
            uiState = PokemonDetailUiState.Content(
                pokemon = PokemonDetail(
                    id = 1,
                    name = "Bulbasaur",
                    imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png",
                    types = persistentListOf(
                        TypeOfPokemon(name = "grass", slot = 1),
                        TypeOfPokemon(name = "poison", slot = 2)
                    ),
                    height = 7,
                    weight = 69,
                    baseExperience = 64,
                    abilities = persistentListOf(
                        Ability(name = "overgrow", isHidden = false, slot = 1),
                        Ability(name = "chlorophyll", isHidden = true, slot = 3)
                    ),
                    stats = persistentListOf(
                        Stat(name = "hp", baseStat = 45, effort = 0),
                        Stat(name = "attack", baseStat = 49, effort = 0),
                        Stat(name = "defense", baseStat = 49, effort = 0),
                        Stat(name = "special-attack", baseStat = 65, effort = 0),
                        Stat(name = "special-defense", baseStat = 65, effort = 0),
                        Stat(name = "speed", baseStat = 45, effort = 0)
                    )
                )
            ),
            restoredScrollIndex = 0,
            restoredScrollOffset = 0,
            onBackClick = {},
            onRetry = {},
            onScrollPositionChanged = { _, _ -> }
        )
    }
}

@Preview(name = "Content State - Charizard")
@Composable
private fun PokemonDetailUnstyledScreenCharizardPreview() {
    UnstyledTheme {
        PokemonDetailContentUnstyled(
            uiState = PokemonDetailUiState.Content(
                pokemon = PokemonDetail(
                    id = 6,
                    name = "Charizard",
                    imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/6.png",
                    types = persistentListOf(
                        TypeOfPokemon(name = "fire", slot = 1),
                        TypeOfPokemon(name = "flying", slot = 2)
                    ),
                    height = 17,
                    weight = 905,
                    baseExperience = 267,
                    abilities = persistentListOf(
                        Ability(name = "blaze", isHidden = false, slot = 1),
                        Ability(name = "solar-power", isHidden = true, slot = 3)
                    ),
                    stats = persistentListOf(
                        Stat(name = "hp", baseStat = 78, effort = 0),
                        Stat(name = "attack", baseStat = 84, effort = 0),
                        Stat(name = "defense", baseStat = 78, effort = 0),
                        Stat(name = "special-attack", baseStat = 109, effort = 0),
                        Stat(name = "special-defense", baseStat = 85, effort = 0),
                        Stat(name = "speed", baseStat = 100, effort = 0)
                    )
                )
            ),
            restoredScrollIndex = 0,
            restoredScrollOffset = 0,
            onBackClick = {},
            onRetry = {},
            onScrollPositionChanged = { _, _ -> }
        )
    }
}
