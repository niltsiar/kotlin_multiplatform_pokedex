package com.minddistrict.multiplatformpoc.features.pokemondetail.wiring

import com.minddistrict.multiplatformpoc.features.pokemondetail.navigation.PokemonDetail
import com.minddistrict.multiplatformpoc.features.pokemondetail.ui.PokemonDetailScreen
import com.minddistrict.multiplatformpoc.navigation.EntryProviderInstaller
import com.minddistrict.multiplatformpoc.navigation.Navigator
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Android-specific Koin DI module for Pokemon Detail navigation.
 * 
 * Provides the EntryProviderInstaller that registers the PokemonDetailScreen composable
 * with the navigation graph.
 */
val pokemonDetailNavigationModule = module {
    /**
     * Contributes the Pokemon Detail navigation entry to the app's navigation graph.
     * Returns a Set containing a single EntryProviderInstaller.
     */
    single<Set<EntryProviderInstaller>>(named("pokemonDetailNavigationInstallers")) {
        setOf(
            {
                entry<PokemonDetail> { key ->
                    val navigator: Navigator = koinInject()
                    
                    PokemonDetailScreen(
                        pokemonId = key.id,
                        onBackClick = { navigator.goBack() }
                    )
                }
            }
        )
    }
}
