package com.minddistrict.multiplatformpoc

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.minddistrict.multiplatformpoc.features.pokemonlist.presentation.PokemonListScreen
import com.minddistrict.multiplatformpoc.features.pokemonlist.wiring.PokemonListModule
import org.jetbrains.compose.ui.tooling.preview.Preview

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
