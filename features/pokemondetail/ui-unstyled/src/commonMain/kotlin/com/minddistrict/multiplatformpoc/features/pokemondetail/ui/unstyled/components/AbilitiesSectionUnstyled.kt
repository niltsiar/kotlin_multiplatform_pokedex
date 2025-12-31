package com.minddistrict.multiplatformpoc.features.pokemondetail.ui.unstyled.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import com.composeunstyled.platformtheme.shapes
import com.composeunstyled.theme.Theme
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.*
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.Ability
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Unstyled abilities section component.
 * 
 * Displays Pokémon abilities as a simple list with minimal styling.
 * Uses border-only chip badges for clean presentation.
 * 
 * Philosophy: Clean, readable ability list without decorative elements.
 * 
 * @param abilities List of Pokémon abilities
 * @param modifier Modifier for the container
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AbilitiesSectionUnstyled(
    abilities: ImmutableList<Ability>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingSm])
    ) {
        Text(
            text = "Abilities",
            style = Theme[typography][titleLarge].copy(fontWeight = FontWeight.Bold),
            color = Theme[colors][onSurface]
        )
        
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Theme[spacing][spacingSm]),
            verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingSm])
        ) {
            abilities.forEach { ability ->
                Text(
                    text = ability.name.replace("-", " ").uppercase(),
                    style = Theme[typography][labelMedium],
                    color = Theme[colors][onSurface],
                    modifier = Modifier
                        .clip(Theme[shapes][shapeMedium])
                        .border(
                            width = 1.dp,
                            color = Theme[colors][onSurface].copy(alpha = 0.3f),
                            shape = Theme[shapes][shapeMedium]
                        )
                        .padding(
                            horizontal = Theme[spacing][spacingMd],
                            vertical = Theme[spacing][spacingXs]
                        )
                )
            }
        }
    }
}

@Preview
@Composable
private fun AbilitiesSectionUnstyledPreview() {
    UnstyledTheme {
        AbilitiesSectionUnstyled(
            abilities = persistentListOf(
                Ability(name = "overgrow", isHidden = false, slot = 1),
                Ability(name = "chlorophyll", isHidden = true, slot = 3)
            )
        )
    }
}
