package com.minddistrict.multiplatformpoc.features.pokemonlist.wiring.ui.unstyled

import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.UnstyledScope
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.UnstyledTheme
import com.minddistrict.multiplatformpoc.core.navigation.Navigator
import com.minddistrict.multiplatformpoc.features.pokemondetail.navigation.PokemonDetail
import com.minddistrict.multiplatformpoc.features.pokemonlist.navigation.PokemonList
import com.minddistrict.multiplatformpoc.features.pokemonlist.presentation.PokemonListViewModel
import com.minddistrict.multiplatformpoc.features.pokemonlist.ui.unstyled.PokemonListScreenUnstyled
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

/**
 * Koin DI module for Pokemon List navigation (Unstyled theme).
 *
 * Scoped to UnstyledScope to separate from Material entries.
 */
val pokemonListNavigationUnstyledModule = module {
    scope<UnstyledScope> {
        navigation<PokemonList> { route ->
            val navigator: Navigator = koinInject()
            val viewModel = koinViewModel<PokemonListViewModel>()
            val lifecycleOwner = LocalLifecycleOwner.current

            // Register ViewModel with lifecycle (implements DefaultLifecycleObserver)
            DisposableEffect(viewModel) {
                lifecycleOwner.lifecycle.addObserver(viewModel)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(viewModel)
                }
            }

            UnstyledTheme {
                PokemonListScreenUnstyled(
                    viewModel = viewModel,
                    onPokemonClick = { pokemon ->
                        // Navigate to detail screen in unstyled world
                        navigator.goTo(PokemonDetail(id = pokemon.id))
                    },
                )
            }
        }
    }
}
