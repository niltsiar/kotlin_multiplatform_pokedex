package com.minddistrict.multiplatformpoc

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.minddistrict.multiplatformpoc.core.designsystem.theme.PokemonTheme
import com.minddistrict.multiplatformpoc.core.di.AppGraph
import com.minddistrict.multiplatformpoc.features.pokemonlist.wiring.pokemonListModule
import com.minddistrict.multiplatformpoc.navigation.EntryProviderInstaller
import com.minddistrict.multiplatformpoc.navigation.Navigator
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.module.Module

// Platform-specific navigation modules (defined in androidMain/jvmMain)
expect fun getPlatformNavigationModules(): List<Module>

@Composable
@Preview
fun App() {
    // Initialize Koin with all feature modules
    KoinApplication(
        application = {
            modules(
                AppGraph.create(
                    baseUrl = "https://pokeapi.co/api/v2",
                    featureModules = listOf(pokemonListModule) + getPlatformNavigationModules()
                )
            )
        }
    ) {
        // Get dependencies from Koin
        val navigator: Navigator = koinInject()
        val entryProviderInstallers: Set<EntryProviderInstaller> = koinInject()
        
        PokemonTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                NavDisplay(
                    backStack = navigator.backStack,
                    onBack = { navigator.goBack() },
                    entryProvider = entryProvider {
                        entryProviderInstallers.forEach { installer ->
                            this.installer()
                        }
                    }
                )
            }
        }
    }
}

