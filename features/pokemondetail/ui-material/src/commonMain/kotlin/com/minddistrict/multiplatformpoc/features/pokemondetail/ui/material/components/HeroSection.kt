package com.minddistrict.multiplatformpoc.features.pokemondetail.ui.material.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.minddistrict.multiplatformpoc.core.designsystem.material.tokens.tokens

/**
 * Material Design 3 hero section for Pokémon detail.
 * 
 * Displays large Pokémon image (256dp) with parallax-ready layout,
 * Pokémon name, and ID number on a gradient background.
 * 
 * Features:
 * - 256dp hero image size
 * - Type-based gradient background
 * - Display typography for name
 * - Token-based spacing
 * 
 * @param name Pokémon name
 * @param id Pokémon ID number
 * @param imageUrl URL of Pokémon sprite
 * @param typeColor Primary type color for gradient
 * @param modifier Modifier for the hero container
 */
@Composable
fun HeroSection(
    name: String,
    id: Int,
    imageUrl: String,
    typeColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        typeColor.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(MaterialTheme.tokens.spacing.xl)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier.size(256.dp)
            )
            Spacer(modifier = Modifier.height(MaterialTheme.tokens.spacing.medium))
            Text(
                text = name,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "#${id.toString().padStart(3, '0')}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
