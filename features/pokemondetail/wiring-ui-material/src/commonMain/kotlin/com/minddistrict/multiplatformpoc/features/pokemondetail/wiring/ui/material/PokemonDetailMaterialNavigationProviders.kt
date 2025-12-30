package com.minddistrict.multiplatformpoc.features.pokemondetail.wiring.ui.material

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.ui.NavDisplay
import com.minddistrict.multiplatformpoc.core.designsystem.material.MaterialScope
import com.minddistrict.multiplatformpoc.core.designsystem.material.tokens.MaterialTokens
import com.minddistrict.multiplatformpoc.core.navigation.Navigator
import com.minddistrict.multiplatformpoc.features.pokemondetail.navigation.PokemonDetail
import com.minddistrict.multiplatformpoc.features.pokemondetail.presentation.PokemonDetailViewModel
import com.minddistrict.multiplatformpoc.features.pokemondetail.ui.material.PokemonDetailMaterialScreen
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
 * Uses Material 3 Expressive motion with emphasized timing curves.
 */
val pokemonDetailNavigationModule = module {
    scope<MaterialScope> {
        navigation<PokemonDetail>(
            metadata = NavDisplay.transitionSpec {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(
                        durationMillis = MaterialTokens.motion.durationLong,
                        easing = MaterialTokens.motion.easingEmphasizedDecelerate,
                    ),
                ) + fadeIn(
                    animationSpec = tween(durationMillis = MaterialTokens.motion.durationLong),
                ) + scaleIn(
                    initialScale = 0.9f,
                    animationSpec = tween(durationMillis = MaterialTokens.motion.durationLong),
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { -it / 4 },
                    animationSpec = tween(durationMillis = MaterialTokens.motion.durationLong),
                ) + fadeOut(
                    animationSpec = tween(durationMillis = MaterialTokens.motion.durationLong),
                ) + scaleOut(
                    targetScale = 0.9f,
                    animationSpec = tween(durationMillis = MaterialTokens.motion.durationLong),
                )
            } + NavDisplay.popTransitionSpec {
                slideInHorizontally(
                    initialOffsetX = { -it / 4 },
                    animationSpec = tween(durationMillis = MaterialTokens.motion.durationShort),
                ) + fadeIn(
                    animationSpec = tween(durationMillis = MaterialTokens.motion.durationShort),
                ) + scaleIn(
                    initialScale = 0.9f,
                    animationSpec = tween(durationMillis = MaterialTokens.motion.durationShort),
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(
                        durationMillis = MaterialTokens.motion.durationShort,
                        easing = MaterialTokens.motion.easingEmphasizedAccelerate,
                    ),
                ) + fadeOut(
                    animationSpec = tween(durationMillis = MaterialTokens.motion.durationShort),
                ) + scaleOut(
                    targetScale = 0.9f,
                    animationSpec = tween(durationMillis = MaterialTokens.motion.durationShort),
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

            PokemonDetailMaterialScreen(
                viewModel = viewModel,
                onBackClick = { navigator.goBack() },
            )
        }
    }
}
