package com.minddistrict.multiplatformpoc.features.pokemondetail.ui.unstyled.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import com.composeunstyled.platformtheme.shapes
import com.composeunstyled.theme.Theme
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.*
import multiplatformpoc.core.designsystem_core.generated.resources.Res
import multiplatformpoc.core.designsystem_core.generated.resources.ic_height
import multiplatformpoc.core.designsystem_core.generated.resources.ic_star
import multiplatformpoc.core.designsystem_core.generated.resources.ic_weight
import org.jetbrains.compose.resources.painterResource

/**
 * Unstyled physical attributes card component.
 * 
 * Displays height, weight, and base experience with Material Symbols icons
 * (ic_height, ic_weight, ic_star) in flat cards with minimal elevation.
 * 
 * Philosophy: Clean presentation with border-only cards and consistent iconography.
 * 
 * @param height Pokémon height in decimeters
 * @param weight Pokémon weight in hectograms
 * @param baseExperience Base experience value
 * @param modifier Modifier for the container
 */
@Composable
fun PhysicalAttributesCardUnstyled(
    height: Int,
    weight: Int,
    baseExperience: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Theme[spacing][spacingSm])
    ) {
        // Height card
        AttributeCard(
            icon = {
                Image(
                    painter = painterResource(Res.drawable.ic_height),
                    contentDescription = "Height",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Theme[colors][onSurface])
                )
            },
            label = "Height",
            value = "${height / 10f} m",
            modifier = Modifier.weight(1f)
        )
        
        // Weight card
        AttributeCard(
            icon = {
                Image(
                    painter = painterResource(Res.drawable.ic_weight),
                    contentDescription = "Weight",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Theme[colors][onSurface])
                )
            },
            label = "Weight",
            value = "${weight / 10f} kg",
            modifier = Modifier.weight(1f)
        )
        
        // Base Experience card
        AttributeCard(
            icon = {
                Image(
                    painter = painterResource(Res.drawable.ic_star),
                    contentDescription = "Base Experience",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Theme[colors][onSurface])
                )
            },
            label = "Base Exp",
            value = baseExperience.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AttributeCard(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(Theme[shapes][shapeMedium])
            .border(
                width = 1.dp,
                color = Theme[colors][onSurface].copy(alpha = 0.2f),
                shape = Theme[shapes][shapeMedium]
            )
            .padding(Theme[spacing][spacingMd]),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingXs])
    ) {
        icon()
        
        Text(
            text = label,
            style = Theme[typography][labelSmall],
            color = Theme[colors][onSurface].copy(alpha = 0.6f)
        )
        
        Text(
            text = value,
            style = Theme[typography][bodyMedium].copy(fontWeight = FontWeight.Medium),
            color = Theme[colors][onSurface]
        )
    }
}

@Preview
@Composable
private fun PhysicalAttributesCardUnstyledPreview() {
    UnstyledTheme {
        PhysicalAttributesCardUnstyled(
            height = 7,
            weight = 69,
            baseExperience = 64
        )
    }
}
