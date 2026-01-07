package com.minddistrict.multiplatformpoc.features.pokemonlist.domain

data class Pokemon(
    val name: String,
    val detailUrl: String
) {
    val id: Int get() = detailUrl.trimEnd('/').substringAfterLast('/').toInt()
    val imageUrl: String get() = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"
}
