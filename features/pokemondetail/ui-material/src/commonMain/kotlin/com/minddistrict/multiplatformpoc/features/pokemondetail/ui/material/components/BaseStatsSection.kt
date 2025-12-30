package com.minddistrict.multiplatformpoc.features.pokemondetail.ui.material.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.minddistrict.multiplatformpoc.core.designsystem.core.components.AnimatedStatBar
import com.minddistrict.multiplatformpoc.core.designsystem.core.motion.rememberReducedMotion
import com.minddistrict.multiplatformpoc.core.designsystem.material.tokens.componentTokens
import com.minddistrict.multiplatformpoc.core.designsystem.material.tokens.tokens
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.Stat
import kotlinx.collections.immutable.ImmutableList

/**
 * Material Design 3 base stats section for Pokémon detail.
 * 
 * Displays Pokémon base stats (HP, Attack, Defense, etc.) using the shared
 * AnimatedStatBar component with staggered entrance animations.
 * 
 * Features:
 * - Card container with Material styling
 * - Staggered animations with 50ms delay per stat
 * - Stat name, progress bar, and value display
 * - Reduced motion support
 * - Token-based spacing and shapes
 * 
 * @param stats List of Pokémon base stats
 * @param modifier Modifier for the section container
 */
@Composable
fun BaseStatsSection(
    stats: ImmutableList<Stat>,
    modifier: Modifier = Modifier
) {
    val reducedMotion = rememberReducedMotion()
    
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
                text = "Base Stats",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(MaterialTheme.tokens.spacing.medium))
            stats.forEachIndexed { index, stat ->
                StatRow(
                    stat = stat,
                    animationDelay = index * 50,
                    reducedMotion = reducedMotion
                )
                if (stat != stats.last()) {
                    Spacer(modifier = Modifier.height(MaterialTheme.tokens.spacing.small))
                }
            }
        }
    }
}

/**
 * Individual stat row with animated progress bar.
 */
@Composable
private fun StatRow(
    stat: Stat,
    animationDelay: Int,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val alpha = remember { Animatable(if (reducedMotion) 1f else 0f) }
    val motionTokens = MaterialTheme.tokens.motion
    
    LaunchedEffect(Unit) {
        if (!reducedMotion) {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = motionTokens.durationMedium,
                    delayMillis = animationDelay,
                    easing = motionTokens.easingEmphasizedDecelerate
                )
            )
        }
    }
    
    val statName = stat.name.replace("-", " ")
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar { char -> char.titlecase() } }
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = statName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(100.dp)
            )
            AnimatedStatBar(
                value = stat.baseStat,
                maxValue = 255,
                tokens = MaterialTheme.componentTokens.progressBar(),
                reducedMotion = reducedMotion,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = MaterialTheme.tokens.spacing.xs)
            )
            Text(
                text = stat.baseStat.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.End
            )
        }
    }
}
