package com.minddistrict.multiplatformpoc

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.ui.NavDisplay
import com.minddistrict.multiplatformpoc.core.designsystem.theme.PokemonTheme
import com.minddistrict.multiplatformpoc.core.di.coreModule
import com.minddistrict.multiplatformpoc.core.diui.navigationUiModule
import com.minddistrict.multiplatformpoc.core.navigation.Navigator
import com.minddistrict.multiplatformpoc.features.pokemondetail.wiring.pokemonDetailModule
import com.minddistrict.multiplatformpoc.features.pokemondetail.wiringui.pokemonDetailNavigationModule
import com.minddistrict.multiplatformpoc.features.pokemonlist.wiring.pokemonListModule
import com.minddistrict.multiplatformpoc.features.pokemonlist.wiring.ui.material.pokemonListNavigationModule
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.KoinApplication
import org.koin.dsl.koinConfiguration

/**
 * Root app composable with Material Design 3 theme.
 *
 * TODO Phase 5: Add theme switching between Material and Unstyled design systems.
 */
@Composable
@Preview
fun App() {
    KoinApplication(
        configuration = koinConfiguration(
            declaration = fun KoinApplication.() {
                modules(
                    coreModule(baseUrl = "https://pokeapi.co/api/v2") +
                        pokemonListModule +
                        pokemonDetailModule +
                        pokemonListNavigationModule +
                        pokemonDetailNavigationModule +
                        navigationUiModule,
                )
            },
        ),
    ) {
        val navigator: Navigator = koinInject()
        val entryProvider = koinEntryProvider()
        
        PokemonTheme {
            Scaffold { paddingValues ->
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    NavDisplay(
                        backStack = navigator.backStack,
                        onBack = { navigator.goBack() },
                        entryProvider = entryProvider,
                    )
                }
            }
        }
    }
}
