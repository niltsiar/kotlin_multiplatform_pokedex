package com.minddistrict.multiplatformpoc

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.minddistrict.multiplatformpoc.features.pokemonlist.ui.PokemonListScreen
import com.minddistrict.multiplatformpoc.features.pokemonlist.wiring.PokemonListModule

@Composable
@Preview
fun App() {
    MaterialTheme {
        Surface {
            val viewModel = remember { PokemonListModule.providePokemonListViewModel() }
            PokemonListScreen(viewModel = viewModel)
        }
    }
}
