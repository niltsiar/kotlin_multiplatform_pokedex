package com.minddistrict.multiplatformpoc.features.pokemondetail.ui.unstyled.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import com.composeunstyled.theme.Theme
import com.minddistrict.multiplatformpoc.core.designsystem.core.PokemonTypeColors
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.UnstyledTheme
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.labelMedium
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.shapeLarge
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.shapes
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.spacing
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.spacingMd
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.spacingSm
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.spacingXs
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.typography
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.TypeOfPokemon
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Unstyled type badge row component.
 * 
 * Displays Pokémon types as border-only badges (no fill).
 * Uses UnstyledComponentTokens.badge for minimal outline style.
 * 
 * Philosophy: Clean, readable type indicators without color fills.
 * 
 * @param types List of Pokémon types
 * @param modifier Modifier for the container
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TypeBadgeRowUnstyled(
    types: ImmutableList<TypeOfPokemon>,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Theme[spacing][spacingSm]),
        verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingSm])
    ) {
        types.forEach { type ->
            val typeColor = PokemonTypeColors.getBackground(type.name, false)
            
            // Border-only badge (no fill)
            Text(
                text = type.name.uppercase(),
                style = Theme[typography][labelMedium].copy(fontWeight = FontWeight.Medium),
                color = typeColor,
                modifier = Modifier
                    .clip(Theme[shapes][shapeLarge])
                    .border(
                        width = 2.dp,
                        color = typeColor,
                        shape = Theme[shapes][shapeLarge]
                    )
                    .padding(
                        horizontal = Theme[spacing][spacingMd],
                        vertical = Theme[spacing][spacingXs]
                    )
            )
        }
    }
}

@Preview
@Composable
private fun TypeBadgeRowUnstyledPreview() {
    UnstyledTheme {
        TypeBadgeRowUnstyled(
            types = persistentListOf(
                TypeOfPokemon(name = "grass", slot = 1),
                TypeOfPokemon(name = "poison", slot = 2)
            )
        )
    }
}
