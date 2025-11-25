package com.minddistrict.multiplatformpoc

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.minddistrict.multiplatformpoc.core.designsystem.theme.PokemonTheme
import com.minddistrict.multiplatformpoc.core.di.AppGraph
import com.minddistrict.multiplatformpoc.features.pokemonlist.ui.PokemonListScreen
import dev.zacsweers.metro.createGraphFactory

@Composable
@Preview
fun App() {
    val graph: AppGraph = remember { 
        createGraphFactory<AppGraph.Factory>().create(baseUrl = "https://pokeapi.co/api/v2")
    }
    
    PokemonTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val viewModel = graph.pokemonListViewModel
            PokemonListScreen(viewModel = viewModel)
        }
    }
}
