package com.minddistrict.multiplatformpoc.features.pokemonlist.ui.material.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.minddistrict.multiplatformpoc.core.designsystem.core.components.PokemonCard
import com.minddistrict.multiplatformpoc.core.designsystem.material.tokens.MaterialComponentTokens
import com.minddistrict.multiplatformpoc.core.designsystem.material.tokens.tokens
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon

/**
 * Material Design 3 Pokémon list card component.
 * 
 * Displays a Pokémon with image, ID, and name in an elevated card with
 * expressive corner radii and pressed state animation.
 * 
 * Uses shared PokemonCard with Material tokens for consistent styling.
 * 
 * @param pokemon Pokémon data to display
 * @param onClick Callback when card is clicked
 * @param modifier Modifier for the card container
 */
@Composable
fun PokemonListCard(
    pokemon: Pokemon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PokemonCard(
        tokens = MaterialComponentTokens.card(),
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(MaterialTheme.tokens.spacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = pokemon.imageUrl,
                contentDescription = pokemon.name,
                modifier = Modifier
                    .size(96.dp)
                    .padding(MaterialTheme.tokens.spacing.xs)
            )
            
            Text(
                text = "#${pokemon.id}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = pokemon.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
