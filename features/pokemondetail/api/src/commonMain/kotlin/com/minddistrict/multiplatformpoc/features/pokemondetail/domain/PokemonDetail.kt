package com.minddistrict.multiplatformpoc.features.pokemondetail.domain

import kotlinx.collections.immutable.ImmutableList

data class PokemonDetail(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val baseExperience: Int,
    val types: ImmutableList<TypeOfPokemon>,
    val stats: ImmutableList<Stat>,
    val abilities: ImmutableList<Ability>,
    val imageUrl: String
)

data class TypeOfPokemon(
    val name: String,
    val slot: Int
)

data class Stat(
    val name: String,
    val baseStat: Int,
    val effort: Int
)

data class Ability(
    val name: String,
    val isHidden: Boolean,
    val slot: Int
)
