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
    val id: Int,
    val name: String,
    val imageUrl: String,
)

internal fun PokemonSnapshot.asDomain(): Pokemon = Pokemon(
    id = id,
    name = name,
    imageUrl = imageUrl,
)

internal fun Pokemon.asSnapshot(): PokemonSnapshot = PokemonSnapshot(
    id = id,
    name = name,
    imageUrl = imageUrl,
)
