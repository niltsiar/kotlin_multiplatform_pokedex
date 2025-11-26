package com.minddistrict.multiplatformpoc.features.pokemondetail.wiring

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.navigation3.ui.NavDisplay
import com.minddistrict.multiplatformpoc.features.pokemondetail.navigation.PokemonDetail
import com.minddistrict.multiplatformpoc.features.pokemondetail.presentation.PokemonDetailViewModel
import com.minddistrict.multiplatformpoc.features.pokemondetail.ui.PokemonDetailScreen
import com.minddistrict.multiplatformpoc.navigation.EntryProviderInstaller
import com.minddistrict.multiplatformpoc.navigation.Navigator
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Android-specific Koin DI module for Pokemon Detail navigation.
 * 
 * Provides the EntryProviderInstaller that registers the PokemonDetailScreen composable
 * with the navigation graph, including navigation transitions.
 */
val pokemonDetailNavigationModule = module {
    /**
     * Contributes the Pokemon Detail navigation entry to the app's navigation graph.
     * Returns a Set containing a single EntryProviderInstaller.
     */
    single<Set<EntryProviderInstaller>>(named("pokemonDetailNavigationInstallers")) {
        setOf(
            {
                entry<PokemonDetail>(
                    // Simplified circular reveal: slide in from right + fade + scale
                    metadata = NavDisplay.transitionSpec {
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(300)
                        ) + fadeIn(animationSpec = tween(300)) togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { -it / 3 },
                            animationSpec = tween(300)
                        ) + fadeOut(animationSpec = tween(300))
                    } + NavDisplay.popTransitionSpec {
                        // Slide in from left when going back
                        slideInHorizontally(
                            initialOffsetX = { -it / 3 },
                            animationSpec = tween(300)
                        ) + fadeIn(animationSpec = tween(300)) togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(300)
                        ) + fadeOut(animationSpec = tween(300))
                    }
                ) { route ->
                    val navigator: Navigator = koinInject()
                    // Use parametersOf to inject pokemonId into ViewModel
                    val viewModel: PokemonDetailViewModel = koinInject(
                        parameters = { parametersOf(route.id) }
                    )
                    
                    PokemonDetailScreen(
                        viewModel = viewModel,
                        onBackClick = { navigator.goBack() }
                    )
                }
            }
        )
    }
}
