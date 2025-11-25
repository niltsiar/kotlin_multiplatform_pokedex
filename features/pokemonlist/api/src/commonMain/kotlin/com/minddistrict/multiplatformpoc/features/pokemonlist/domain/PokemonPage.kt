package com.minddistrict.multiplatformpoc.features.pokemonlist.domain

data class PokemonPage(
    val pokemons: List<Pokemon>,
    val hasMore: Boolean
)
