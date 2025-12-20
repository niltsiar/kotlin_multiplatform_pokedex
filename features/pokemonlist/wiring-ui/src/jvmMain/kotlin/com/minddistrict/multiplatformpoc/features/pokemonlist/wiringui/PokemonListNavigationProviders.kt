package com.minddistrict.multiplatformpoc.features.pokemonlist.wiringui

import com.minddistrict.multiplatformpoc.core.navigation.EntryProviderInstaller
import com.minddistrict.multiplatformpoc.core.navigation.Navigator
import com.minddistrict.multiplatformpoc.features.pokemondetail.navigation.PokemonDetail
import com.minddistrict.multiplatformpoc.features.pokemonlist.navigation.PokemonList
import com.minddistrict.multiplatformpoc.features.pokemonlist.presentation.PokemonListViewModel
import com.minddistrict.multiplatformpoc.features.pokemonlist.ui.PokemonListScreen
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Desktop-specific Koin DI module for Pokemon List navigation.
 */
val pokemonListNavigationModule = module {
    single<Set<EntryProviderInstaller>>(named(PokemonListNavigationInstallersQualifier)) {
        setOf(
            {
                entry<PokemonList> {
                    val navigator: Navigator = koinInject()
                    val viewModel: PokemonListViewModel = koinInject()

                    PokemonListScreen(
                        viewModel = viewModel,
                        onPokemonClick = { pokemon ->
                            navigator.goTo(PokemonDetail(pokemon.id))
                        },
                    )
                }
            },
        )
    }
}
