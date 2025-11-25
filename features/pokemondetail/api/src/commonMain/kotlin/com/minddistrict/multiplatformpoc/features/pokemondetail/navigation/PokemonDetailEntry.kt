package com.minddistrict.multiplatformpoc.features.pokemondetail.navigation

import com.minddistrict.multiplatformpoc.core.designsystem.navigation.AppDestination

/**
 * Navigation destination for Pokémon Detail feature.
 */
data object PokemonDetailDestination : AppDestination {
    override val route: String = "pokemonDetail/{pokemonId}"
    override val label: String = "Detail"
    // Detail screen should not appear in navigation suite
    override val showInNavigation: Boolean = false
}

/**
 * Navigation entry point for Pokémon Detail feature.
 * Provides route building with parameters.
 */
interface PokemonDetailEntry {
    val destination: AppDestination
    fun buildRoute(pokemonId: Int): String
}
