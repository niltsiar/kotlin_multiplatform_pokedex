package com.minddistrict.multiplatformpoc.features.pokemondetail.wiringui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.navigation3.ui.NavDisplay
import com.minddistrict.multiplatformpoc.core.navigation.EntryProviderInstaller
import com.minddistrict.multiplatformpoc.core.navigation.Navigator
import com.minddistrict.multiplatformpoc.features.pokemondetail.navigation.PokemonDetail
import com.minddistrict.multiplatformpoc.features.pokemondetail.presentation.PokemonDetailViewModel
import com.minddistrict.multiplatformpoc.features.pokemondetail.ui.PokemonDetailScreen
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Desktop-specific Koin DI module for Pokemon Detail navigation.
 */
val pokemonDetailNavigationModule = module {
    single<Set<EntryProviderInstaller>>(named(PokemonDetailNavigationInstallersQualifier)) {
        setOf(
            {
                entry<PokemonDetail>(
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
                    val viewModel: PokemonDetailViewModel = koinInject(
                        parameters = { parametersOf(route.id) },
                    )

                    PokemonDetailScreen(
                        viewModel = viewModel,
                        onBackClick = { navigator.goBack() },
                    )
                }
            },
        )
    }
}
