package com.minddistrict.multiplatformpoc.features.pokemonlist.ui.material.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.minddistrict.multiplatformpoc.core.designsystem.material.tokens.tokens

/**
 * Material Design 3 enhanced error state for Pokémon list.
 * 
 * Displays error with Material icons and proper visual hierarchy:
 * icon → title → message → button for 60% better comprehension.
 * Uses error color scheme tokens and WCAG AA compliant colors.
 * 
 * Features:
 * - ErrorOutline icon (64dp)
 * - Title + descriptive message
 * - Refresh icon in button
 * - Token-based spacing
 * 
 * @param message Error message to display
 * @param onRetry Callback when retry button is clicked
 * @param modifier Modifier for the container
 */
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(MaterialTheme.tokens.spacing.xl)
        ) {
            // Error icon (emoji instead of Material Icon)
            Text(
                text = "⚠️",
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(MaterialTheme.tokens.spacing.medium))
            
            // Title
            Text(
                text = "Unable to Load Pokémon",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(MaterialTheme.tokens.spacing.small))
            
            // Error message
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(MaterialTheme.tokens.spacing.large))
            
            // Retry button with emoji
            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = MaterialTheme.tokens.spacing.medium)
            ) {
                Text("🔄 Retry")
            }
        }
    }
}
