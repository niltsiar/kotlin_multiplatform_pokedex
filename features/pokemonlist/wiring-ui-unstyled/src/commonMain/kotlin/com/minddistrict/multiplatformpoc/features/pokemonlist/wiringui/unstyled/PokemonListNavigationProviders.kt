package com.minddistrict.multiplatformpoc.features.pokemonlist.wiringui.unstyled

import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.minddistrict.multiplatformpoc.features.pokemonlist.navigation.PokemonList
import com.minddistrict.multiplatformpoc.features.pokemonlist.presentation.PokemonListViewModel
import com.minddistrict.multiplatformpoc.features.pokemonlist.ui.unstyled.PokemonListScreenUnstyled
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

/**
 * Koin DI module for Pokemon List navigation (Compose Unstyled UI).
 *
 * Uses Koin's Navigation 3 integration to declaratively register the PokemonListScreenUnstyled.
 * The navigation<T> DSL automatically registers this entry, which is collected by koinEntryProvider().
 */
val pokemonListNavigationUnstyledModule = module {
    // Declare navigation entry using Koin's Navigation 3 DSL
    navigation<PokemonList> { route ->
        val viewModel = koinViewModel<PokemonListViewModel>()
        val lifecycleOwner = LocalLifecycleOwner.current
        
        // Register ViewModel with lifecycle (implements DefaultLifecycleObserver)
        DisposableEffect(viewModel) {
            lifecycleOwner.lifecycle.addObserver(viewModel)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(viewModel)
            }
        }

        PokemonListScreenUnstyled(
            viewModel = viewModel,
            onPokemonClick = { pokemon ->
            // TODO: Navigate to detail screen when unstyled detail is implemented
            println("Clicked Pokemon: ${pokemon.name}")
        },
        )
    }
}
