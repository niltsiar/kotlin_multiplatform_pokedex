package com.minddistrict.multiplatformpoc.core.designsystem.core.tokens

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Base design tokens that serve as the foundation for all design system variants.
 * 
 * These tokens define:
 * - 8dp spacing grid (xxxs: 2dp → xxxl: 64dp)
 * - Standard corner radii (4dp → 20dp)
 * - Standard elevations (0dp → 12dp)
 * - Standard animation timing and easing
 * 
 * Design systems (Material, Unstyled) can:
 * 1. Delegate directly to these base tokens (e.g., spacing)
 * 2. Override with custom values (e.g., Material uses expressive shapes)
 * 
 * This pattern eliminates duplication while allowing customization.
 */
object BaseTokens {
    /**
     * Spacing tokens based on 8dp grid system.
     * All design systems delegate to these values for consistency.
     */
    object spacing : SpacingTokens {
        override val xxxs: Dp = 2.dp
        override val xxs: Dp = 4.dp
        override val xs: Dp = 8.dp
        override val small: Dp = 12.dp
        override val medium: Dp = 16.dp
        override val large: Dp = 20.dp
        override val xl: Dp = 24.dp
        override val xxl: Dp = 32.dp
        override val xxxl: Dp = 64.dp
    }
    
    /**
     * Standard shape tokens.
     * Design systems typically override these with their own personality.
     */
    object shapes : ShapeTokens {
        override val extraSmall: Shape = RoundedCornerShape(4.dp)
        override val small: Shape = RoundedCornerShape(8.dp)
        override val medium: Shape = RoundedCornerShape(12.dp)
        override val large: Shape = RoundedCornerShape(16.dp)
        override val extraLarge: Shape = RoundedCornerShape(20.dp)
    }
    
    /**
     * Standard elevation tokens.
     * Material 3 uses tonal elevation; minimal themes use flat shadows.
     */
    object elevation : ElevationTokens {
        override val level0: Dp = 0.dp
        override val level1: Dp = 1.dp
        override val level2: Dp = 3.dp
        override val level3: Dp = 6.dp
        override val level4: Dp = 8.dp
        override val level5: Dp = 12.dp
    }
    
    /**
     * Standard motion tokens with Material Design easing curves.
     */
    object motion : MotionTokens {
        override val durationShort: Int = 200
        override val durationMedium: Int = 300
        override val durationLong: Int = 400
        
        /** Standard easing (decelerate): Starts quickly, ends slowly */
        override val easingStandard: Easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
        
        /** Emphasized decelerate (Material 3 Expressive): More exaggerated deceleration */
        override val easingEmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
        
        /** Emphasized accelerate (Material 3 Expressive): Quick exit */
        override val easingEmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    }
}
