package com.minddistrict.multiplatformpoc.core.designsystem.material.tokens

import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.minddistrict.multiplatformpoc.core.designsystem.core.components.BadgeTokens
import com.minddistrict.multiplatformpoc.core.designsystem.core.components.CardTokens
import com.minddistrict.multiplatformpoc.core.designsystem.core.components.ProgressBarTokens

/**
 * Material Design 3 Expressive component tokens.
 * 
 * Defines Material-specific styling for shared components:
 * - Cards: Elevated with expressive corner radii (28dp)
 * - Badges: Filled with type colors, no border
 * - Progress bars: Emphasized deceleration animation, primary color
 * 
 * These implementations provide Material 3 personality while using the same
 * component code as Unstyled theme.
 */
object MaterialComponentTokens {
    /**
     * Material card tokens with expressive shapes and elevation.
     */
    val card: @Composable () -> CardTokens = {
        object : CardTokens {
            override val shape = MaterialTokens.shapes.extraLarge
            override val elevation = MaterialTokens.elevation.level2
            override val backgroundColor = MaterialTheme.colorScheme.surface
            override val contentColor = MaterialTheme.colorScheme.onSurface
            override val pressedScale = 0.97f
        }
    }
    
    /**
     * Material badge tokens with filled backgrounds.
     */
    val badge: @Composable () -> BadgeTokens = {
        object : BadgeTokens {
            override val shape = MaterialTokens.shapes.large  // Pill shape (24dp)
            override val borderWidth = 0.dp  // No border (filled style)
            override val fillAlpha = 1f  // Fully filled
            override val textColor = Color.White
        }
    }
    
    /**
     * Material progress bar tokens with emphasized motion.
     */
    val progressBar: @Composable () -> ProgressBarTokens = {
        object : ProgressBarTokens {
            override val height = 8.dp
            override val shape = MaterialTokens.shapes.small
            override val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
            override val foregroundColor = MaterialTheme.colorScheme.primary
            override val animationSpec = tween<Float>(
                durationMillis = MaterialTokens.motion.durationLong,
                easing = MaterialTokens.motion.easingEmphasizedDecelerate
            )
        }
    }
}
