package com.minddistrict.multiplatformpoc.features.pokemonlist.navigation

import com.minddistrict.multiplatformpoc.core.designsystem.navigation.AppDestination

/**
 * Navigation destination for Pokémon List feature.
 */
data object PokemonListDestination : AppDestination {
    override val route: String = "pokemonList"
    override val label: String = "Pokémon"
    override val showInNavigation: Boolean = true
}

/**
 * Navigation entry point for Pokémon List feature.
 * Provides route building for navigation.
 */
interface PokemonListEntry {
    val destination: AppDestination
    fun buildRoute(): String
}
