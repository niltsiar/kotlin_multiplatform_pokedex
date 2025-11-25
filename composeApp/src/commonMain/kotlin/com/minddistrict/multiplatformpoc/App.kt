package com.minddistrict.multiplatformpoc

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.minddistrict.multiplatformpoc.core.designsystem.theme.PokemonTheme
import com.minddistrict.multiplatformpoc.core.di.AppGraph
import dev.zacsweers.metro.createGraphFactory

@Composable
@Preview
fun App() {
    val graph: AppGraph = remember { 
        createGraphFactory<AppGraph.Factory>().create(baseUrl = "https://pokeapi.co/api/v2")
    }
    
    PokemonTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavDisplay(
                backStack = graph.navigator.backStack,
                onBack = { graph.navigator.goBack() },
                entryProvider = entryProvider {
                    graph.entryProviderInstallers.forEach { installer ->
                        this.installer()
                    }
                }
            )
        }
    }
}

