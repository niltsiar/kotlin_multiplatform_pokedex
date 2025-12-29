package com.minddistrict.multiplatformpoc.features.pokemondetail.presentation

import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.Ability
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.PokemonDetail
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.Stat
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.TypeOfPokemon
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable

@Serializable
internal data class PokemonDetailPersistedState(
    val pokemonId: Int,
    val lastErrorMessage: String? = null,
    val pokemon: PokemonDetailSnapshot? = null,
    val scrollPosition: Int = 0,
    val scrollOffset: Int = 0,
)

@Serializable
internal data class PokemonDetailSnapshot(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val baseExperience: Int,
    val types: List<TypeSnapshot>,
    val stats: List<StatSnapshot>,
    val abilities: List<AbilitySnapshot>,
    val imageUrl: String,
)

@Serializable
internal data class TypeSnapshot(
    val name: String,
    val slot: Int,
)

@Serializable
internal data class StatSnapshot(
    val name: String,
    val baseStat: Int,
    val effort: Int,
)

@Serializable
internal data class AbilitySnapshot(
    val name: String,
    val isHidden: Boolean,
    val slot: Int,
)

internal fun PokemonDetailSnapshot.asDomain(): PokemonDetail = PokemonDetail(
    id = id,
    name = name,
    height = height,
    weight = weight,
    baseExperience = baseExperience,
    types = types.map { TypeOfPokemon(name = it.name, slot = it.slot) }.toImmutableList(),
    stats = stats.map { Stat(name = it.name, baseStat = it.baseStat, effort = it.effort) }.toImmutableList(),
    abilities = abilities.map { Ability(name = it.name, isHidden = it.isHidden, slot = it.slot) }.toImmutableList(),
    imageUrl = imageUrl,
)

internal fun PokemonDetail.asSnapshot(): PokemonDetailSnapshot = PokemonDetailSnapshot(
    id = id,
    name = name,
    height = height,
    weight = weight,
    baseExperience = baseExperience,
    types = types.map { TypeSnapshot(name = it.name, slot = it.slot) },
    stats = stats.map { StatSnapshot(name = it.name, baseStat = it.baseStat, effort = it.effort) },
    abilities = abilities.map { AbilitySnapshot(name = it.name, isHidden = it.isHidden, slot = it.slot) },
    imageUrl = imageUrl,
)
