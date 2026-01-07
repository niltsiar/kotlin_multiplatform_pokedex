package com.minddistrict.multiplatformpoc.features.pokemonlist.presentation

import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon
import kotlinx.serialization.Serializable

@Serializable
internal data class PokemonListPersistedState(
    val offset: Int = 0,
    val pageSize: Int = 20,
    val hasMore: Boolean = true,
    val pokemons: List<PokemonSnapshot> = emptyList(),

    // UX state
    val scrollIndex: Int = 0,
    val scrollOffset: Int = 0,
    val scrollAnchorPokemonId: Int? = null,
    val lastSelectedPokemonId: Int? = null,

    // Last known UI state
    val lastErrorMessage: String? = null,
)

@Serializable
internal data class PokemonSnapshot(
    val name: String,
    val detailUrl: String,
)

internal fun PokemonSnapshot.asDomain(): Pokemon = Pokemon(
    name = name,
    detailUrl = detailUrl,
)

internal fun Pokemon.asSnapshot(): PokemonSnapshot = PokemonSnapshot(
    name = name,
    detailUrl = detailUrl,
)
