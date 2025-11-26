package com.minddistrict.multiplatformpoc.core.di

import com.minddistrict.multiplatformpoc.features.pokemonlist.navigation.PokemonList
import com.minddistrict.multiplatformpoc.navigation.EntryProviderInstaller
import com.minddistrict.multiplatformpoc.navigation.Navigator
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Root dependency injection configuration for the application.
 * 
 * Koin modules are collected from feature wiring modules and combined
 * into the complete application graph.
 */
object AppGraph {
    /**
     * Creates the complete Koin module configuration for the application.
     * 
     * @param baseUrl The base URL for API calls (e.g., "https://pokeapi.co/api/v2")
     * @param featureModules All feature wiring modules to include
     * @return List of Koin modules to load
     */
    fun create(
        baseUrl: String,
        featureModules: List<Module>
    ): List<Module> {
        val coreModule = module {
            // Provide baseUrl as a named dependency
            single(qualifier = named("baseUrl")) { baseUrl }
            
            // Provide Navigator singleton with PokemonList as start destination
            single { Navigator(PokemonList) }
        }
        
        // Aggregation module to collect all navigation installers from feature modules
        val navigationAggregationModule = module {
            single<Set<EntryProviderInstaller>> {
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
        
        return listOf(coreModule) + featureModules + navigationAggregationModule
    }
}

