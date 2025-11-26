package com.minddistrict.multiplatformpoc.features.pokemondetail.data

import com.minddistrict.multiplatformpoc.features.pokemondetail.data.dto.AbilitySlotDto
import com.minddistrict.multiplatformpoc.features.pokemondetail.data.dto.PokemonDetailDto
import com.minddistrict.multiplatformpoc.features.pokemondetail.data.dto.StatDto
import com.minddistrict.multiplatformpoc.features.pokemondetail.data.dto.TypeSlotDto
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.Ability
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.PokemonDetail
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.Stat
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.Type
import kotlinx.collections.immutable.toImmutableList

/**
 * Maps PokemonDetailDto to PokemonDetail domain model.
 */
fun PokemonDetailDto.asDomain(): PokemonDetail {
    return PokemonDetail(
        id = id,
        name = name.replaceFirstChar { it.titlecase() },
        height = height,
        weight = weight,
        baseExperience = baseExperience ?: 0,
        types = types.sortedBy { it.slot }.map { it.asDomain() }.toImmutableList(),
        stats = stats.map { it.asDomain() }.toImmutableList(),
        abilities = abilities.sortedBy { it.slot }.map { it.asDomain() }.toImmutableList(),
        imageUrl = sprites.frontDefault ?: ""
    )
}

/**
 * Maps TypeSlotDto to Type domain model.
 */
fun TypeSlotDto.asDomain(): Type {
    return Type(
        name = type.name,
        slot = slot
    )
}

/**
 * Maps StatDto to Stat domain model.
 */
fun StatDto.asDomain(): Stat {
    return Stat(
        name = stat.name,
        baseStat = baseStat,
        effort = effort
    )
}

/**
 * Maps AbilitySlotDto to Ability domain model.
 */
fun AbilitySlotDto.asDomain(): Ability {
    return Ability(
        name = ability.name,
        isHidden = isHidden,
        slot = slot
    )
}
