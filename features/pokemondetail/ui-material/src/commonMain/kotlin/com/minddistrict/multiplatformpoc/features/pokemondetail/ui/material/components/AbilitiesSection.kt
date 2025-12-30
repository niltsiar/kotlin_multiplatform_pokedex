package com.minddistrict.multiplatformpoc.features.pokemondetail.ui.material.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.minddistrict.multiplatformpoc.core.designsystem.material.tokens.tokens
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.Ability
import kotlinx.collections.immutable.ImmutableList

/**
 * Material Design 3 abilities section for Pokémon detail.
 * 
 * Displays Pokémon abilities in a card with "Hidden" chips for hidden abilities.
 * Uses Material surface variant colors and token-based styling.
 * 
 * Features:
 * - List of abilities with proper spacing
 * - "Hidden" chip badge for hidden abilities
 * - Token-based shapes and spacing
 * 
 * @param abilities List of Pokémon abilities
 * @param modifier Modifier for the section container
 */
@Composable
fun AbilitiesSection(
    abilities: ImmutableList<Ability>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.tokens.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.tokens.spacing.medium)
        ) {
            Text(
                text = "Abilities",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(MaterialTheme.tokens.spacing.small))
            abilities.forEach { ability ->
                AbilityRow(ability = ability)
                if (ability != abilities.last()) {
                    Spacer(modifier = Modifier.height(MaterialTheme.tokens.spacing.xs))
                }
            }
        }
    }
}

/**
 * Individual ability row with optional "Hidden" chip.
 */
@Composable
private fun AbilityRow(
    ability: Ability,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = ability.name.replace("-", " ").replaceFirstChar { it.titlecase() },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        if (ability.isHidden) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Text(
                    text = "Hidden",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = MaterialTheme.tokens.spacing.xs, vertical = 4.dp)
                )
            }
        }
    }
}
