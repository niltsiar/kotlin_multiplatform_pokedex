package com.minddistrict.multiplatformpoc.features.pokemondetail.presentation

import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.PokemonDetail

sealed interface PokemonDetailUiState {
    data object Loading : PokemonDetailUiState
    data class Content(
        val pokemon: PokemonDetail
    ) : PokemonDetailUiState
    data class Error(val message: String) : PokemonDetailUiState
}
