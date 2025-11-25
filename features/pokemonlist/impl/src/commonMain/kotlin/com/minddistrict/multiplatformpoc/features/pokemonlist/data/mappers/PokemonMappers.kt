package com.minddistrict.multiplatformpoc.features.pokemonlist.data.mappers

import com.minddistrict.multiplatformpoc.features.pokemonlist.data.dto.PokemonListDto
import com.minddistrict.multiplatformpoc.features.pokemonlist.data.dto.PokemonSummaryDto
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.PokemonPage

/**
 * Extract Pokemon ID from URL.
 * Example: "https://pokeapi.co/api/v2/pokemon/25/" -> 25
 */
internal fun extractIdFromUrl(url: String): Int {
    return url.trimEnd('/').split('/').lastOrNull()?.toIntOrNull()
        ?: throw IllegalArgumentException("Invalid Pokemon URL: $url")
}

internal fun PokemonSummaryDto.toDomain(): Pokemon {
    val id = extractIdFromUrl(url)
    val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"
    
    return Pokemon(
        id = id,
        name = name.replaceFirstChar { it.uppercase() },
        imageUrl = imageUrl
    )
}

internal fun PokemonListDto.toDomain(): PokemonPage {
    return PokemonPage(
        pokemons = results.map { it.toDomain() },
        hasMore = next != null
    )
}
