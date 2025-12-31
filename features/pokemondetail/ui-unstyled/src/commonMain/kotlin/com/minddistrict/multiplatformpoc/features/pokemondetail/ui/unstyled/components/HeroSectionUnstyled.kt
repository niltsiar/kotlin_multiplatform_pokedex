package com.minddistrict.multiplatformpoc.features.pokemondetail.ui.unstyled.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.composeunstyled.Text
import com.composeunstyled.theme.Theme
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.*
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.tokens.UnstyledTokens

/**
 * Unstyled hero section component for Pokémon detail.
 * 
 * Displays Pokémon image (256dp), ID, and name with NO gradient background.
 * Uses flat background color and minimal spacing (20dp between elements).
 * 
 * Philosophy: Clean, minimal presentation without decorative gradients.
 * 
 * @param imageUrl Pokémon artwork URL
 * @param id Pokémon ID number
 * @param name Pokémon name
 * @param modifier Modifier for the container
 */
@Composable
fun HeroSectionUnstyled(
    imageUrl: String,
    id: Int,
    name: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Theme[colors][surface])  // Flat background, NO gradient
            .padding(Theme[spacing][spacingLg]),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingLg])  // 20dp
        ) {
            // Pokémon image (256dp)
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier.size(256.dp)
            )
            
            // ID
            Text(
                text = "#${id.toString().padStart(3, '0')}",
                style = Theme[typography][labelLarge],
                color = Theme[colors][onSurface].copy(alpha = 0.6f)
            )
            
            // Name
            Text(
                text = name.uppercase(),
                style = Theme[typography][displaySmall].copy(fontWeight = FontWeight.Bold),
                color = Theme[colors][onSurface],
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
private fun HeroSectionUnstyledPreview() {
    UnstyledTheme {
        HeroSectionUnstyled(
            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png",
            id = 1,
            name = "Bulbasaur"
        )
    }
}
