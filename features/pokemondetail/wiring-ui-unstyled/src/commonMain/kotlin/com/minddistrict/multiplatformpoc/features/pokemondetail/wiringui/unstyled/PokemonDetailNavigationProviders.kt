package com.minddistrict.multiplatformpoc.features.pokemondetail.wiringui.unstyled

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.ui.NavDisplay
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.UnstyledScope
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.UnstyledTheme
import com.minddistrict.multiplatformpoc.core.navigation.Navigator
import com.minddistrict.multiplatformpoc.features.pokemondetail.navigation.PokemonDetail
import com.minddistrict.multiplatformpoc.features.pokemondetail.presentation.PokemonDetailViewModel
import com.minddistrict.multiplatformpoc.features.pokemondetail.ui.unstyled.PokemonDetailScreenUnstyled
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

/**
 * Koin DI module for Pokemon Detail navigation (Compose Unstyled UI).
 *
 * Uses Koin's Navigation 3 integration to declaratively register the PokemonDetailScreenUnstyled.
 * Scoped to UnstyledScope to separate from Material entries.
 */
val pokemonDetailNavigationUnstyledModule = module {
    scope<UnstyledScope> {
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
            // Key ViewModel by route.id to ensure new instance per Pokemon (unstyled variant)
            val viewModel: PokemonDetailViewModel = koinViewModel(
                key = "pokemon_detail_unstyled_${route.id}",
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

            UnstyledTheme {
                PokemonDetailScreenUnstyled(
                    viewModel = viewModel,
                    onBackClick = { navigator.goBack() },
                )
            }
        }
    }
}
