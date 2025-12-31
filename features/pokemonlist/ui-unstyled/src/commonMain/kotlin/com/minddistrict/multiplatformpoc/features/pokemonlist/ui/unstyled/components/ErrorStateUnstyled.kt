package com.minddistrict.multiplatformpoc.features.pokemonlist.ui.unstyled.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import com.composeunstyled.platformtheme.shapes
import com.composeunstyled.platformtheme.indications
import com.composeunstyled.platformtheme.sizeDefault
import com.composeunstyled.platformtheme.interactiveSizes
import com.composeunstyled.theme.Theme
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.*

/**
 * Unstyled error state component.
 * 
 * Displays an error message with a clean retry button (no FilledTonal).
 * Uses minimal styling with border-only button.
 * 
 * @param message Error message to display
 * @param onRetry Callback when retry button is clicked
 * @param modifier Modifier for the container
 */
@Composable
fun ErrorStateUnstyled(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingMd]),
            modifier = Modifier.padding(Theme[spacing][spacingLg])
        ) {
            Text(
                text = message,
                style = Theme[typography][bodyMedium],
                color = Theme[colors][error],
                textAlign = TextAlign.Center
            )
            
            // Clean border-only button
            Box(
                modifier = Modifier
                    .clip(Theme[shapes][shapeMedium])
                    .border(
                        width = 1.dp,
                        color = Theme[colors][onSurface].copy(alpha = 0.5f),
                        shape = Theme[shapes][shapeMedium]
                    )
                    .clickable(onClick = onRetry)
                    .padding(
                        horizontal = Theme[spacing][spacingLg],
                        vertical = Theme[spacing][spacingSm]
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Retry",
                    style = Theme[typography][labelLarge],
                    color = Theme[colors][onSurface]
                )
            }
        }
    }
}

@Preview
@Composable
private fun ErrorStateUnstyledPreview() {
    UnstyledTheme {
        ErrorStateUnstyled(
            message = "Failed to load Pokémon",
            onRetry = {}
        )
    }
}
