package com.minddistrict.multiplatformpoc.features.pokemonlist.wiring.ui.material

import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.minddistrict.multiplatformpoc.core.designsystem.material.MaterialScope
import com.minddistrict.multiplatformpoc.core.navigation.Navigator
import com.minddistrict.multiplatformpoc.features.pokemondetail.navigation.PokemonDetail
import com.minddistrict.multiplatformpoc.features.pokemonlist.navigation.PokemonList
import com.minddistrict.multiplatformpoc.features.pokemonlist.presentation.PokemonListViewModel
import com.minddistrict.multiplatformpoc.features.pokemonlist.ui.material.PokemonListMaterialScreen
import org.koin.compose.koinInject
import org.koin.dsl.navigation3.navigation
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.module

/**
 * Koin DI module for Pokemon List navigation (Compose platforms: Android, Desktop, iOS Compose).
 *
 * Uses Koin's Navigation 3 integration to declaratively register the PokemonListScreen.
 * Scoped to MaterialScope to separate from Unstyled entries.
 */
val pokemonListNavigationModule = module {
    scope<MaterialScope> {
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

            PokemonListMaterialScreen(
                viewModel = viewModel,
                onPokemonClick = { pokemon ->
                    navigator.goTo(PokemonDetail(pokemon.id))
                },
            )
        }
    }
}
