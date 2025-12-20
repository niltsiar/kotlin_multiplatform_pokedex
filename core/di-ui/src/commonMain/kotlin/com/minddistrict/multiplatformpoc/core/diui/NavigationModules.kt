package com.minddistrict.multiplatformpoc.core.diui

import com.minddistrict.multiplatformpoc.core.navigation.EntryProviderInstaller
import com.minddistrict.multiplatformpoc.core.navigation.Navigator
import com.minddistrict.multiplatformpoc.features.pokemonlist.navigation.PokemonList
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Compose-only navigation modules.
 *
 * These MUST NOT be included in `Shared.framework` (native SwiftUI iOS app).
 */
val navigationUiModule = module {
    // Provide Navigator singleton with PokemonList as start destination
    single { Navigator(PokemonList) }
}

/**
 * Navigation aggregation module that collects all EntryProviderInstallers from feature modules.
 */
val navigationAggregationModule = module {
    single<Set<EntryProviderInstaller>>(qualifier = named("allNavigationInstallers")) {
        val allInstallers = mutableSetOf<EntryProviderInstaller>()

        // Try to get each feature's navigation installers (may not exist on all platforms)
        runCatching {
            getOrNull<Set<EntryProviderInstaller>>(named("pokemonListNavigationInstallers"))
        }.getOrNull()?.let { allInstallers.addAll(it) }

        runCatching {
            getOrNull<Set<EntryProviderInstaller>>(named("pokemonDetailNavigationInstallers"))
        }.getOrNull()?.let { allInstallers.addAll(it) }

        allInstallers
    }
}
