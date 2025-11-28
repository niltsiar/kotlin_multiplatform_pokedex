package com.minddistrict.multiplatformpoc.features.pokemonlist.wiring

import com.minddistrict.multiplatformpoc.core.navigation.EntryProviderInstaller
import com.minddistrict.multiplatformpoc.core.navigation.Navigator
import com.minddistrict.multiplatformpoc.features.pokemondetail.navigation.PokemonDetail
import com.minddistrict.multiplatformpoc.features.pokemonlist.navigation.PokemonList
import com.minddistrict.multiplatformpoc.features.pokemonlist.presentation.PokemonListViewModel
import com.minddistrict.multiplatformpoc.features.pokemonlist.ui.PokemonListScreen
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * iOS-specific Koin DI module for Pokemon List navigation.
 *
 * Provides the EntryProviderInstaller that registers the PokemonListScreen composable
 * with the navigation graph for Compose Multiplatform iOS.
 */
val pokemonListNavigationModule = module {
    /**
     * Contributes the Pokemon List navigation entry to the app's navigation graph.
     * Returns a Set containing a single EntryProviderInstaller.
     */
    single<Set<EntryProviderInstaller>>(named("pokemonListNavigationInstallers")) {
        setOf(
            {
                entry<PokemonList> {
                    val navigator: Navigator = koinInject()
                    val viewModel: PokemonListViewModel = koinInject()
                    
                    PokemonListScreen(
                        viewModel = viewModel,
                        onPokemonClick = { pokemon ->
                            navigator.goTo(PokemonDetail(pokemon.id))
                        },
                    )
                }
            }
        )
    }
}
