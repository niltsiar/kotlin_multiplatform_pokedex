package com.minddistrict.multiplatformpoc.features.pokemonlist.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonListDto(
    @SerialName("count") val count: Int,
    @SerialName("next") val next: String? = null,
    @SerialName("previous") val previous: String? = null,
    @SerialName("results") val results: List<PokemonSummaryDto>
)

@Serializable
data class PokemonSummaryDto(
    @SerialName("name") val name: String,
    @SerialName("url") val url: String
)
