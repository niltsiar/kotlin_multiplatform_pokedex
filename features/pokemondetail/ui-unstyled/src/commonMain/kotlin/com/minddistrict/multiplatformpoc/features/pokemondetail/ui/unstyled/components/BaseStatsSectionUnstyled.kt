package com.minddistrict.multiplatformpoc.features.pokemondetail.ui.unstyled.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import com.composeunstyled.platformtheme.shapes
import com.composeunstyled.theme.Theme
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.*
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.tokens.UnstyledComponentTokens
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.tokens.UnstyledTokens
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.Stat
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Unstyled base stats section component.
 * 
 * Displays Pokémon stats with monochrome stat bars.
 * Uses UnstyledComponentTokens.progressBar for minimal styling:
 * - Thin bars (6dp)
 * - Monochrome color (no stat-specific colors)
 * - Linear motion (no emphasized easing)
 * 
 * Philosophy: Clean, readable stats presentation without color decoration.
 * 
 * @param stats List of Pokémon base stats
 * @param modifier Modifier for the container
 */
@Composable
fun BaseStatsSectionUnstyled(
    stats: ImmutableList<Stat>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingMd])
    ) {
        Text(
            text = "Base Stats",
            style = Theme[typography][titleLarge].copy(fontWeight = FontWeight.Bold),
            color = Theme[colors][onSurface]
        )
        
        stats.forEach { stat ->
            StatRow(
                name = stat.name,
                value = stat.baseStat,
                maxValue = 255  // Max stat value
            )
        }
    }
}

@Composable
private fun StatRow(
    name: String,
    value: Int,
    maxValue: Int,
    modifier: Modifier = Modifier
) {
    val displayName = name.replace("-", " ").uppercase()
    
    var animatedProgress by remember { mutableFloatStateOf(0f) }
    
    LaunchedEffect(value) {
        animatedProgress = (value.toFloat() / maxValue).coerceIn(0f, 1f)
    }
    
    val progress by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(
            durationMillis = UnstyledTokens.motion.durationMedium,
            easing = UnstyledTokens.motion.easingStandard  // Linear
        ),
        label = "statProgress"
    )
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Theme[spacing][spacingMd]),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Stat name
        Text(
            text = displayName,
            style = Theme[typography][labelMedium],
            color = Theme[colors][onSurface].copy(alpha = 0.7f),
            modifier = Modifier.weight(0.3f)
        )
        
        // Stat bar (monochrome)
        Box(
            modifier = Modifier
                .weight(0.5f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .border(
                    width = 1.dp,
                    color = Theme[colors][onSurface].copy(alpha = 0.2f),
                    shape = RoundedCornerShape(3.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(6.dp)
                    .background(Theme[colors][onSurface])  // Monochrome
            )
        }
        
        // Stat value
        Text(
            text = value.toString(),
            style = Theme[typography][labelMedium].copy(fontWeight = FontWeight.Medium),
            color = Theme[colors][onSurface],
            modifier = Modifier.weight(0.2f)
        )
    }
}

@Preview
@Composable
private fun BaseStatsSectionUnstyledPreview() {
    UnstyledTheme {
        BaseStatsSectionUnstyled(
            stats = persistentListOf(
                Stat(name = "hp", baseStat = 45, effort = 0),
                Stat(name = "attack", baseStat = 49, effort = 0),
                Stat(name = "defense", baseStat = 49, effort = 0),
                Stat(name = "special-attack", baseStat = 65, effort = 0),
                Stat(name = "special-defense", baseStat = 65, effort = 0),
                Stat(name = "speed", baseStat = 45, effort = 0)
            )
        )
    }
}
