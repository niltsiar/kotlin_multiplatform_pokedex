package com.minddistrict.multiplatformpoc.core.designsystem.material.tokens

import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.minddistrict.multiplatformpoc.core.designsystem.core.components.BadgeTokens
import com.minddistrict.multiplatformpoc.core.designsystem.core.components.CardTokens
import com.minddistrict.multiplatformpoc.core.designsystem.core.components.ProgressBarTokens

/**
 * CompositionLocal for accessing Material component tokens within the theme hierarchy.
 * 
 * Usage:
 * ```
 * @Composable
 * fun MyCard() {
 *     Card(
 *         tokens = MaterialTheme.componentTokens.card(),
 *         content = { }
 *     )
 * }
 * ```
 */
val LocalMaterialComponentTokens = staticCompositionLocalOf<MaterialComponentTokens> {
    error("No MaterialComponentTokens provided. Make sure PokemonTheme wraps your content.")
}

/**
 * Interface representing all Material component tokens.
 * Allows for theme variants or customization by implementing this interface.
 */
interface MaterialComponentTokens {
    val card: @Composable () -> CardTokens
    val badge: @Composable () -> BadgeTokens
    val progressBar: @Composable () -> ProgressBarTokens
}

/**
 * Default implementation of Material Design 3 Expressive component tokens.
 * 
 * Defines Material-specific styling for shared components:
 * - Cards: Elevated with expressive corner radii (28dp)
 * - Badges: Filled with type colors, no border
 * - Progress bars: Emphasized deceleration animation, primary color
 * 
 * These implementations provide Material 3 personality while using the same
 * component code as Unstyled theme.
 * 
 * CRITICAL: All values must come from MaterialTheme to allow theme customization.
 */
internal class DefaultMaterialComponentTokens : MaterialComponentTokens {
    /**
     * Material card tokens with expressive shapes and elevation.
     */
    override val card: @Composable () -> CardTokens = {
        object : CardTokens {
            override val shape = MaterialTheme.tokens.shapes.extraLarge
            override val elevation = MaterialTheme.tokens.elevation.level2
            override val backgroundColor = MaterialTheme.colorScheme.surface
            override val contentColor = MaterialTheme.colorScheme.onSurface
            override val pressedScale = 0.97f
        }
    }
    
    /**
     * Material badge tokens with filled backgrounds.
     */
    override val badge: @Composable () -> BadgeTokens = {
        object : BadgeTokens {
            override val shape = MaterialTheme.tokens.shapes.large  // Pill shape (24dp)
            override val borderWidth = 0.dp  // No border (filled style)
            override val fillAlpha = 1f  // Fully filled
            override val textColor = Color.White
        }
    }
    
    /**
     * Material progress bar tokens with emphasized motion.
     */
    override val progressBar: @Composable () -> ProgressBarTokens = {
        object : ProgressBarTokens {
            override val height = 8.dp
            override val shape = MaterialTheme.tokens.shapes.small
            override val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
            override val foregroundColor = MaterialTheme.colorScheme.primary
            override val animationSpec = tween<Float>(
                durationMillis = MaterialTheme.tokens.motion.durationLong,
                easing = MaterialTheme.tokens.motion.easingEmphasizedDecelerate
            )
        }
    }
}
