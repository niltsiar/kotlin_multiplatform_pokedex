package com.minddistrict.multiplatformpoc

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.minddistrict.multiplatformpoc.core.designsystem.theme.PokemonTheme
import com.minddistrict.multiplatformpoc.core.di.coreModule
import com.minddistrict.multiplatformpoc.core.di.navigationAggregationModule
import com.minddistrict.multiplatformpoc.features.pokemonlist.wiring.pokemonListModule
import com.minddistrict.multiplatformpoc.navigation.EntryProviderInstaller
import com.minddistrict.multiplatformpoc.navigation.Navigator
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.module.Module
import org.koin.core.qualifier.named

// Platform-specific navigation modules (defined in androidMain/jvmMain)
expect fun getPlatformNavigationModules(): List<Module>

@Composable
@Preview
fun App() {
    // Initialize Koin with all modules directly (idiomatic Koin pattern)
    KoinApplication(
        application = {
            modules(
                coreModule(baseUrl = "https://pokeapi.co/api/v2") +
                pokemonListModule +
                getPlatformNavigationModules() +
                navigationAggregationModule
            )
        }
    ) {
        // Get dependencies from Koin with explicit qualifier
        val navigator: Navigator = koinInject()
        val entryProviderInstallers: Set<EntryProviderInstaller> = 
            koinInject(qualifier = named("allNavigationInstallers"))
        
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

