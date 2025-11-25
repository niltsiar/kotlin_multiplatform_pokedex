package com.minddistrict.multiplatformpoc.features.pokemonlist.wiring

import androidx.compose.runtime.Composable
import com.minddistrict.multiplatformpoc.features.pokemondetail.navigation.PokemonDetail
import com.minddistrict.multiplatformpoc.features.pokemonlist.navigation.PokemonList
import com.minddistrict.multiplatformpoc.features.pokemonlist.presentation.PokemonListViewModel
import com.minddistrict.multiplatformpoc.features.pokemonlist.ui.PokemonListScreen
import com.minddistrict.multiplatformpoc.navigation.EntryProviderInstaller
import com.minddistrict.multiplatformpoc.navigation.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides

/**
 * Android-specific Metro DI module for Pokemon List navigation.
 *
 * Provides the EntryProviderInstaller that registers the PokemonListScreen composable
 * with the navigation graph using non-inline syntax.
 */
@BindingContainer
@ContributesTo(AppScope::class)
interface PokemonListNavigationProviders {

    companion object {
        /**
         * Contributes the Pokemon List navigation entry to the app's navigation graph.
         * Using explicit NavEntry instead of inline entry<T> function.
         *
         * @param navigator The navigation controller for navigating between screens
         * @param viewModel The ViewModel for the Pokemon List screen
         */
        @Provides
        @IntoSet
        fun providePokemonListNavigation(
            navigator: Navigator,
            viewModel: PokemonListViewModel
        ): EntryProviderInstaller = {
            entry<PokemonList> {
                PokemonListScreen(
                    viewModel = viewModel,
                    onPokemonClick = { pokemon ->
                        navigator.goTo(PokemonDetail(pokemon.id))
                    },
                )
            }
        }
    }
}
