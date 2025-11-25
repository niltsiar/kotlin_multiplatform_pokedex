package com.minddistrict.multiplatformpoc

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.minddistrict.multiplatformpoc.core.di.AppGraph
import com.minddistrict.multiplatformpoc.features.pokemonlist.ui.PokemonListScreen
import dev.zacsweers.metro.createGraphFactory

@Composable
@Preview
fun App() {
    MaterialTheme {
        Surface {
            val graph: AppGraph = remember { 
                createGraphFactory<AppGraph.Factory>().create(baseUrl = "https://pokeapi.co/api/v2")
            }
            val viewModel = graph.pokemonListViewModel
            PokemonListScreen(viewModel = viewModel)
        }
    }
}
