package com.minddistrict.multiplatformpoc.core.designsystem.core.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

/**
 * Theme-agnostic animated stat bar component.
 * 
 * Displays a horizontal progress bar with smooth animation. Used for Pokémon base stats
 * (HP, Attack, Defense, etc.) with values typically 0-255.
 * 
 * Features:
 * - Smooth animation with configurable easing
 * - Reduced motion support (instant progress change)
 * - Configurable height, shape, and colors
 * - Progress clamping to 0-1 range
 * 
 * @param value Current stat value (e.g., 78)
 * @param maxValue Maximum possible value for normalization (default 255)
 * @param tokens Progress bar styling tokens from design system
 * @param reducedMotion Whether reduced motion is enabled (disables animation)
 * @param modifier Modifier for the progress bar container
 */
@Composable
fun AnimatedStatBar(
    value: Int,
    maxValue: Int = 255,
    tokens: ProgressBarTokens,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val progress = (value.toFloat() / maxValue).coerceIn(0f, 1f)
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = if (reducedMotion) snap() else tokens.animationSpec,
        label = "statBarProgress"
    )
    
    Box(
        modifier = modifier
            .height(tokens.height)
            .fillMaxWidth()
            .clip(tokens.shape)
            .background(tokens.backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .background(tokens.foregroundColor)
        )
    }
}
