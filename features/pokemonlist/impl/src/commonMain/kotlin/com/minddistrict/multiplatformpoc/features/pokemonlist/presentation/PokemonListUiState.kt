package com.minddistrict.multiplatformpoc.features.pokemonlist.presentation

import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

sealed interface PokemonListUiState {
    data object Loading : PokemonListUiState
    data class Content(
        val pokemons: ImmutableList<Pokemon>,
        val isLoadingMore: Boolean = false,
        val hasMore: Boolean = true
    ) : PokemonListUiState
    data class Error(val message: String) : PokemonListUiState
}
