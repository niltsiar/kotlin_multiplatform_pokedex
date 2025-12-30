package com.minddistrict.multiplatformpoc.features.pokemondetail.ui.material.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.minddistrict.multiplatformpoc.core.designsystem.material.tokens.tokens

/**
 * Material Design 3 physical attributes card for Pokémon detail.
 * 
 * Displays height, weight, and base experience in a row of info cards
 * with emoji indicators and token-based styling.
 * 
 * Features:
 * - Equal-width columns using weight(1f)
 * - Emoji + label + value layout
 * - Token-based spacing and shapes
 * 
 * @param height Pokémon height in decimeters
 * @param weight Pokémon weight in hectograms
 * @param baseExperience Base experience gained from defeating this Pokémon
 * @param modifier Modifier for the card container
 */
@Composable
fun PhysicalAttributesCard(
    height: Int,
    weight: Int,
    baseExperience: Int,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.tokens.spacing.xs),
        modifier = modifier.fillMaxWidth()
    ) {
        InfoCard(
            emoji = "📏",
            label = "Height",
            value = "${height / 10.0} m",
            modifier = Modifier.weight(1f)
        )
        InfoCard(
            emoji = "⚖️",
            label = "Weight",
            value = "${weight / 10.0} kg",
            modifier = Modifier.weight(1f)
        )
        InfoCard(
            emoji = "⭐",
            label = "Base XP",
            value = baseExperience.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Individual info card for physical attribute.
 */
@Composable
private fun InfoCard(
    emoji: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.tokens.shapes.large
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.tokens.spacing.medium)
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.displaySmall
            )
            Spacer(modifier = Modifier.height(MaterialTheme.tokens.spacing.xs))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
