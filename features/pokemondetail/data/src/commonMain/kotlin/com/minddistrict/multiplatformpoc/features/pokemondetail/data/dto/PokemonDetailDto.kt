package com.minddistrict.multiplatformpoc.features.pokemondetail.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonDetailDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("height") val height: Int,
    @SerialName("weight") val weight: Int,
    @SerialName("base_experience") val baseExperience: Int? = null,
    @SerialName("types") val types: List<TypeSlotDto>,
    @SerialName("stats") val stats: List<StatDto>,
    @SerialName("abilities") val abilities: List<AbilitySlotDto>,
    @SerialName("sprites") val sprites: SpritesDto
)

@Serializable
data class TypeSlotDto(
    @SerialName("slot") val slot: Int,
    @SerialName("type") val type: TypeDto
)

@Serializable
data class TypeDto(
    @SerialName("name") val name: String,
    @SerialName("url") val url: String
)

@Serializable
data class StatDto(
    @SerialName("base_stat") val baseStat: Int,
    @SerialName("effort") val effort: Int,
    @SerialName("stat") val stat: StatInfoDto
)

@Serializable
data class StatInfoDto(
    @SerialName("name") val name: String,
    @SerialName("url") val url: String
)

@Serializable
data class AbilitySlotDto(
    @SerialName("is_hidden") val isHidden: Boolean,
    @SerialName("slot") val slot: Int,
    @SerialName("ability") val ability: AbilityDto
)

@Serializable
data class AbilityDto(
    @SerialName("name") val name: String,
    @SerialName("url") val url: String
)

@Serializable
data class SpritesDto(
    @SerialName("front_default") val frontDefault: String? = null,
    @SerialName("front_shiny") val frontShiny: String? = null,
    @SerialName("back_default") val backDefault: String? = null,
    @SerialName("back_shiny") val backShiny: String? = null
)
