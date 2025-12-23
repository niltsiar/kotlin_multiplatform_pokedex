package com.minddistrict.multiplatformpoc.core.diui

import com.minddistrict.multiplatformpoc.core.navigation.Navigator
import com.minddistrict.multiplatformpoc.features.pokemonlist.navigation.PokemonList
import org.koin.dsl.module

/**
 * Compose-only navigation module.
 *
 * This MUST NOT be included in `Shared.framework` (native SwiftUI iOS app).
 * 
 * Provides only the Navigator singleton with PokemonList as start destination.
 * Navigation entry providers are registered via feature navigation modules in getPlatformNavigationModules().
 */
val navigationUiModule = module {
    single { Navigator(PokemonList) }
}
