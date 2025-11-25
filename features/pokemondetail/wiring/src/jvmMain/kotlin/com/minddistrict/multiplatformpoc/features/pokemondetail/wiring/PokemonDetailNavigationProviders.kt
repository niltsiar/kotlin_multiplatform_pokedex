package com.minddistrict.multiplatformpoc.features.pokemondetail.wiring

import com.minddistrict.multiplatformpoc.features.pokemondetail.navigation.PokemonDetail
import com.minddistrict.multiplatformpoc.features.pokemondetail.ui.PokemonDetailScreen
import com.minddistrict.multiplatformpoc.navigation.EntryProviderInstaller
import com.minddistrict.multiplatformpoc.navigation.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides

/**
 * JVM (Desktop)-specific Metro DI module for Pokemon Detail navigation.
 *
 * Provides the EntryProviderInstaller that registers the PokemonDetailScreen composable
 * with the navigation graph using non-inline syntax.
 */
@BindingContainer
@ContributesTo(AppScope::class)
interface PokemonDetailNavigationProviders {

    companion object {
        /**
         * Contributes the Pokemon Detail navigation entry to the app's navigation graph.
         * Using explicit NavEntry instead of inline entry<T> function.
         *
         * @param navigator The navigation controller for navigating between screens
         */
        @Provides
        @IntoSet
        fun providePokemonDetailNavigation(
            navigator: Navigator
        ): EntryProviderInstaller = {
            entry<PokemonDetail> { key ->
                PokemonDetailScreen(
                    pokemonId = key.id,
                    onBackClick = { navigator.goBack() }
                )
            }
        }
    }
}
