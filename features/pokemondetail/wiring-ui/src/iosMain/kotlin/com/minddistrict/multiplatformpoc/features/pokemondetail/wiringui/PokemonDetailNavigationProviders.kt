package com.minddistrict.multiplatformpoc.features.pokemondetail.wiringui

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
 * iOS-specific Koin DI module for Pokemon Detail navigation.
 */
val pokemonDetailNavigationModule = module {
    single<Set<EntryProviderInstaller>>(named(PokemonDetailNavigationInstallersQualifier)) {
        setOf(
            {
                entry<PokemonDetail> { key ->
                    val navigator: Navigator = koinInject()
                    val viewModel: PokemonDetailViewModel = koinInject { parametersOf(key.id) }

                    PokemonDetailScreen(
                        viewModel = viewModel,
                        onBackClick = { navigator.goBack() },
                    )
                }
            },
        )
    }
}
