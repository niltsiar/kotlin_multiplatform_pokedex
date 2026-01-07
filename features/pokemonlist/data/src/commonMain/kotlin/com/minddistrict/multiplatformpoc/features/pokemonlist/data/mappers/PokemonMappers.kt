package com.minddistrict.multiplatformpoc.features.pokemonlist.data.mappers

import com.minddistrict.multiplatformpoc.features.pokemonlist.data.dto.PokemonListDto
import com.minddistrict.multiplatformpoc.features.pokemonlist.data.dto.PokemonSummaryDto
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.PokemonPage

internal fun PokemonSummaryDto.toDomain(): Pokemon {
    return Pokemon(
        name = name.replaceFirstChar { it.uppercase() },
        detailUrl = url
    )
}

internal fun PokemonListDto.toDomain(): PokemonPage {
    return PokemonPage(
        pokemons = results.map { it.toDomain() },
        nextUrl = next,
        previousUrl = previous
    )
}
