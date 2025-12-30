package com.minddistrict.multiplatformpoc.core.designsystem.unstyled.tokens

import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composeunstyled.theme.Theme
import com.minddistrict.multiplatformpoc.core.designsystem.core.components.BadgeTokens
import com.minddistrict.multiplatformpoc.core.designsystem.core.components.CardTokens
import com.minddistrict.multiplatformpoc.core.designsystem.core.components.ProgressBarTokens
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.*

/**
 * Unstyled (Compose Unstyled) component tokens.
 * 
 * Defines minimal styling for shared components:
 * - Cards: Flat with minimal elevation (1dp)
 * - Badges: Border-only outline style with no fill
 * - Progress bars: Thinner bars with linear motion
 * 
 * These implementations provide minimalist aesthetics while using the same
 * component code as Material theme.
 */
object UnstyledComponentTokens {
    /**
     * Unstyled card tokens with minimal elevation and subtle interaction.
     */
    val card: @Composable () -> CardTokens = {
        object : CardTokens {
            override val shape = Theme[shapes][shapeMedium]
            override val elevation = UnstyledTokens.elevation.level1  // Minimal (1dp)
            override val backgroundColor = Theme[colors][surface]
            override val contentColor = Theme[colors][onSurface]
            override val pressedScale = 0.98f  // More subtle than Material
        }
    }
    
    /**
     * Unstyled badge tokens with border-only outline style.
     */
    val badge: @Composable () -> BadgeTokens = {
        object : BadgeTokens {
            override val shape = Theme[shapes][shapeLarge]
            override val borderWidth = 2.dp  // Border only, no fill
            override val fillAlpha = 0f  // Transparent fill (outline style)
            override val textColor = Theme[colors][onSurface]
        }
    }
    
    /**
     * Unstyled progress bar tokens with minimal styling and linear motion.
     */
    val progressBar: @Composable () -> ProgressBarTokens = {
        object : ProgressBarTokens {
            override val height = 6.dp  // Thinner than Material (8dp)
            override val shape = Theme[shapes][shapeSmall]
            override val backgroundColor = Color.Gray.copy(alpha = 0.2f)
            override val foregroundColor = Theme[colors][primary]
            override val animationSpec = tween<Float>(
                durationMillis = UnstyledTokens.motion.durationMedium,
                easing = UnstyledTokens.motion.easingStandard  // Linear, not emphasized
            )
        }
    }
}
