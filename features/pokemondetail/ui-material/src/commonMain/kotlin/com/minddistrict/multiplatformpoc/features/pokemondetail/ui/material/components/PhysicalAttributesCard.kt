package com.minddistrict.multiplatformpoc.features.pokemondetail.ui.material.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.minddistrict.multiplatformpoc.core.designsystem.material.tokens.tokens
import multiplatformpoc.core.designsystem_core.generated.resources.Res
import multiplatformpoc.core.designsystem_core.generated.resources.ic_balance
import multiplatformpoc.core.designsystem_core.generated.resources.ic_star
import multiplatformpoc.core.designsystem_core.generated.resources.ic_straighten
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Material Design 3 physical attributes card for Pokémon detail.
 * 
 * Displays height, weight, and base experience in a row of info cards
 * with Material icon indicators and token-based styling.
 * 
 * Features:
 * - Equal-width columns using weight(1f)
 * - Material icon + label + value layout
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
            iconRes = Res.drawable.ic_straighten,
            label = "Height",
            value = "${height / 10.0} m",
            contentDescription = "Height",
            modifier = Modifier.weight(1f)
        )
        InfoCard(
            iconRes = Res.drawable.ic_balance,
            label = "Weight",
            value = "${weight / 10.0} kg",
            contentDescription = "Weight",
            modifier = Modifier.weight(1f)
        )
        InfoCard(
            iconRes = Res.drawable.ic_star,
            label = "Base XP",
            value = baseExperience.toString(),
            contentDescription = "Base experience",
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Individual info card for physical attribute.
 */
@Composable
private fun InfoCard(
    iconRes: DrawableResource,
    label: String,
    value: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = MaterialTheme.tokens.elevation.level2
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
            Icon(
                painter = painterResource(iconRes),
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(MaterialTheme.tokens.spacing.xs))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
