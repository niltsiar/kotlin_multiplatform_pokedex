package com.minddistrict.multiplatformpoc.features.pokemonlist.ui.unstyled.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composeunstyled.ProgressBar
import com.composeunstyled.ProgressIndicator
import com.composeunstyled.theme.Theme
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.*
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.tokens.UnstyledTokens

/**
 * Unstyled loading state component.
 * 
 * Displays a minimal linear progress indicator with clean animation.
 * Uses linear easing (no emphasized curves) and subtle styling.
 * 
 * @param modifier Modifier for the container
 */
@Composable
fun LoadingStateUnstyled(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        var progress by remember { mutableStateOf(0f) }
        LaunchedEffect(Unit) {
            while (true) {
                animate(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                ) { value, _ ->
                    progress = value
                }
            }
        }
        ProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(6.dp),  // Minimal height
            shape = RoundedCornerShape(3.dp),
            backgroundColor = Theme[colors][surface],
            contentColor = Theme[colors][onSurface],
        ) {
            ProgressBar()
        }
    }
}

@Preview
@Composable
private fun LoadingStateUnstyledPreview() {
    UnstyledTheme {
        LoadingStateUnstyled()
    }
}
