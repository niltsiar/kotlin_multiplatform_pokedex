package com.minddistrict.multiplatformpoc.core.di

import com.minddistrict.multiplatformpoc.features.pokemonlist.presentation.PokemonListViewModel
import com.minddistrict.multiplatformpoc.navigation.EntryProviderInstaller
import com.minddistrict.multiplatformpoc.navigation.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides

/**
 * Root dependency injection graph for the application.
 * 
 * Metro will generate an implementation of this interface at compile time
 * with all @Provides functions from modules annotated with @ContributesTo(AppScope::class).
 */
@DependencyGraph(AppScope::class)
interface AppGraph {
    /**
     * Provides access to the PokemonList ViewModel.
     */
    val pokemonListViewModel: PokemonListViewModel
    
    /**
     * The Navigator manages the navigation back stack.
     * Follows Android nav3-recipes modular architecture pattern.
     */
    val navigator: Navigator
    
    /**
     * All navigation entry provider installers contributed via @IntoSet
     * from feature wiring modules. Each installer registers a route and its
     * corresponding composable screen.
     */
    val entryProviderInstallers: Set<EntryProviderInstaller>
    
    /**
     * Factory for creating the AppGraph with runtime dependencies.
     */
    @DependencyGraph.Factory
    fun interface Factory {
        /**
         * Creates the AppGraph instance.
         * 
         * @param baseUrl The base URL for API calls (e.g., "https://pokeapi.co/api/v2")
         */
        fun create(@Provides baseUrl: String): AppGraph
    }
}

