package com.minddistrict.multiplatformpoc.features.pokemondetail.wiringui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.ui.NavDisplay
import com.minddistrict.multiplatformpoc.core.designsystem.material.MaterialScope
import com.minddistrict.multiplatformpoc.core.navigation.Navigator
import com.minddistrict.multiplatformpoc.features.pokemondetail.navigation.PokemonDetail
import com.minddistrict.multiplatformpoc.features.pokemondetail.presentation.PokemonDetailViewModel
import com.minddistrict.multiplatformpoc.features.pokemondetail.ui.PokemonDetailScreen
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

/**
 * Koin DI module for Pokemon Detail navigation (Compose platforms: Android, Desktop, iOS Compose).
 *
 * Uses Koin's Navigation 3 integration to declaratively register the PokemonDetailScreen.
 * Scoped to MaterialScope to separate from Unstyled entries.
 */
val pokemonDetailNavigationModule = module {
    scope<MaterialScope> {
        navigation<PokemonDetail>(
            metadata = NavDisplay.transitionSpec {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300),
                ) + fadeIn(animationSpec = tween(300)) togetherWith
                    slideOutHorizontally(
                        targetOffsetX = { -it / 3 },
                        animationSpec = tween(300),
                    ) + fadeOut(animationSpec = tween(300))
            } + NavDisplay.popTransitionSpec {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(300),
                ) + fadeIn(animationSpec = tween(300)) togetherWith
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300),
                    ) + fadeOut(animationSpec = tween(300))
            },
        ) { route ->
            val navigator: Navigator = koinInject()
            // Key ViewModel by route.id to ensure new instance per Pokemon
            val viewModel: PokemonDetailViewModel = koinViewModel(
                key = "pokemon_detail_${route.id}",
                parameters = { parametersOf(route.id) },
            )
            val lifecycleOwner = LocalLifecycleOwner.current
            
            // Register ViewModel with lifecycle (implements DefaultLifecycleObserver)
            // Key by route.id to properly dispose when navigating to different Pokemon
            DisposableEffect(route.id) {
                lifecycleOwner.lifecycle.addObserver(viewModel)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(viewModel)
                }
            }

            PokemonDetailScreen(
                viewModel = viewModel,
                onBackClick = { navigator.goBack() },
            )
        }
    }
}
