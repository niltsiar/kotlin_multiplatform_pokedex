package com.minddistrict.multiplatformpoc.features.pokemondetail.ui.material

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.minddistrict.multiplatformpoc.core.designsystem.material.theme.PokemonTheme
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.Ability
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.PokemonDetail
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.Stat
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.TypeOfPokemon
import com.minddistrict.multiplatformpoc.features.pokemondetail.presentation.PokemonDetailUiState
import kotlinx.collections.immutable.persistentListOf

/**
 * Previews for PokemonDetailMaterialScreen.
 * 
 * Demonstrates all UI states (Loading, Content, Error) with Material theming.
 */

/**
 * Preview of loading state with centered progress indicator.
 */
@Preview(name = "Loading State")
@Composable
private fun PokemonDetailMaterialScreenLoadingPreview() {
    PokemonTheme {
        Surface {
            PokemonDetailMaterialContent(
                uiState = PokemonDetailUiState.Loading,
                onBackClick = {},
                onRetry = {},
                onScrollPositionChanged = { _, _ -> }
            )
        }
    }
}

/**
 * Preview of content state with Pikachu details.
 */
@Preview(name = "Content State - Pikachu")
@Composable
private fun PokemonDetailMaterialScreenContentPreview() {
    PokemonTheme {
        Surface {
            PokemonDetailMaterialContent(
                uiState = PokemonDetailUiState.Content(
                    pokemon = PokemonDetail(
                        id = 25,
                        name = "Pikachu",
                        height = 4,
                        weight = 60,
                        baseExperience = 112,
                        types = persistentListOf(
                            TypeOfPokemon(name = "electric", slot = 1)
                        ),
                        stats = persistentListOf(
                            Stat(name = "hp", baseStat = 35, effort = 0),
                            Stat(name = "attack", baseStat = 55, effort = 0),
                            Stat(name = "defense", baseStat = 40, effort = 0),
                            Stat(name = "special-attack", baseStat = 50, effort = 0),
                            Stat(name = "special-defense", baseStat = 50, effort = 0),
                            Stat(name = "speed", baseStat = 90, effort = 0)
                        ),
                        abilities = persistentListOf(
                            Ability(name = "static", isHidden = false, slot = 1),
                            Ability(name = "lightning-rod", isHidden = true, slot = 3)
                        ),
                        imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png"
                    )
                ),
                onBackClick = {},
                onRetry = {},
                onScrollPositionChanged = { _, _ -> }
            )
        }
    }
}

/**
 * Preview of error state with retry button.
 */
@Preview(name = "Error State")
@Composable
private fun PokemonDetailMaterialScreenErrorPreview() {
    PokemonTheme {
        Surface {
            PokemonDetailMaterialContent(
                uiState = PokemonDetailUiState.Error("Network error. Please check your connection."),
                onBackClick = {},
                onRetry = {},
                onScrollPositionChanged = { _, _ -> }
            )
        }
    }
}
