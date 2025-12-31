package com.minddistrict.multiplatformpoc.features.pokemondetail.wiring.ui.unstyled

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
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.tokens.UnstyledTokens
import com.minddistrict.multiplatformpoc.core.navigation.Navigator
import com.minddistrict.multiplatformpoc.features.pokemondetail.navigation.PokemonDetail
import com.minddistrict.multiplatformpoc.features.pokemondetail.presentation.PokemonDetailViewModel
import com.minddistrict.multiplatformpoc.features.pokemondetail.ui.unstyled.PokemonDetailUnstyledScreen
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
 * Uses Unstyled minimal motion with standard timing curves (no scale effects).
 */
val pokemonDetailNavigationUnstyledModule = module {
    scope<UnstyledScope> {
        navigation<PokemonDetail>(
            metadata = NavDisplay.transitionSpec {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(
                        durationMillis = UnstyledTokens.motion.durationMedium,
                        easing = UnstyledTokens.motion.easingStandard,
                    ),
                ) + fadeIn(
                    animationSpec = tween(durationMillis = UnstyledTokens.motion.durationMedium),
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { -it / 4 },
                    animationSpec = tween(durationMillis = UnstyledTokens.motion.durationMedium),
                ) + fadeOut(
                    animationSpec = tween(durationMillis = UnstyledTokens.motion.durationMedium),
                )
            } + NavDisplay.popTransitionSpec {
                slideInHorizontally(
                    initialOffsetX = { -it / 4 },
                    animationSpec = tween(durationMillis = UnstyledTokens.motion.durationShort),
                ) + fadeIn(
                    animationSpec = tween(durationMillis = UnstyledTokens.motion.durationShort),
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(
                        durationMillis = UnstyledTokens.motion.durationShort,
                        easing = UnstyledTokens.motion.easingStandard,
                    ),
                ) + fadeOut(
                    animationSpec = tween(durationMillis = UnstyledTokens.motion.durationShort),
                )
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

            UnstyledTheme {
                PokemonDetailUnstyledScreen(
                    viewModel = viewModel,
                    onBackClick = { navigator.goBack() },
                )
            }
        }
    }
}
