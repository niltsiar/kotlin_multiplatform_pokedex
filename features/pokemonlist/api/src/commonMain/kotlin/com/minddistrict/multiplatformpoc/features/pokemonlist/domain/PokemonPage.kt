package com.minddistrict.multiplatformpoc.features.pokemonlist.domain

data class PokemonPage(
    val pokemons: List<Pokemon>,
    val nextUrl: String?,
    val previousUrl: String?
) {
    val hasMore: Boolean get() = nextUrl != null
    val hasPrevious: Boolean get() = previousUrl != null
}
