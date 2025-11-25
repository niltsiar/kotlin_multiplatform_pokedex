package com.minddistrict.multiplatformpoc.features.pokemondetail.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.minddistrict.multiplatformpoc.core.designsystem.theme.PokemonTheme

/**
 * Placeholder Pokemon Detail screen.
 * Shows the pokemon ID and a back button.
 * 
 * TODO: Implement actual Pokemon detail with data, images, stats, etc.
 * 
 * @param pokemonId The ID of the pokemon to display
 * @param onBackClick Callback when the back button is clicked
 */
@Composable
fun PokemonDetailScreen(
    pokemonId: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Pokémon Detail",
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "ID: $pokemonId",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBackClick) {
                Text("Go Back")
            }
            Text(
                text = "TODO: Implement actual detail screen",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Preview
@Composable
private fun PokemonDetailScreenPreview() {
    PokemonTheme {
        Surface {
            PokemonDetailScreen(
                pokemonId = 25,
                onBackClick = {}
            )
        }
    }
}
