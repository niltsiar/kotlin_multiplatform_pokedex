package com.minddistrict.multiplatformpoc.core.di

import com.minddistrict.multiplatformpoc.features.pokemonlist.navigation.PokemonList
import com.minddistrict.multiplatformpoc.navigation.EntryProviderInstaller
import com.minddistrict.multiplatformpoc.navigation.Navigator
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Core application module providing base dependencies.
 * 
 * Idiomatic Koin pattern: modules are defined as top-level functions/properties.
 * 
 * @param baseUrl The base URL for API calls (e.g., "https://pokeapi.co/api/v2")
 */
fun coreModule(baseUrl: String) = module {
    // Provide baseUrl as a named dependency
    single(qualifier = named("baseUrl")) { baseUrl }
    
    // Provide Navigator singleton with PokemonList as start destination
    single { Navigator(PokemonList) }
}

/**
 * Navigation aggregation module that collects all EntryProviderInstallers
 * from feature modules.
 * 
 * This module must be loaded AFTER all feature navigation modules to ensure
 * all named installers are available.
 * 
 * Provides: Set<EntryProviderInstaller> with qualifier "allNavigationInstallers"
 */
val navigationAggregationModule = module {
    single<Set<EntryProviderInstaller>>(qualifier = named("allNavigationInstallers")) {
        // Collect all named navigation installer sets and merge them
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

