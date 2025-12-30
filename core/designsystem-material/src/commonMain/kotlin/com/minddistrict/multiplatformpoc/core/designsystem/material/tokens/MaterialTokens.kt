package com.minddistrict.multiplatformpoc.core.designsystem.material.tokens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.minddistrict.multiplatformpoc.core.designsystem.core.tokens.BaseTokens
import com.minddistrict.multiplatformpoc.core.designsystem.core.tokens.ElevationTokens
import com.minddistrict.multiplatformpoc.core.designsystem.core.tokens.MotionTokens
import com.minddistrict.multiplatformpoc.core.designsystem.core.tokens.ShapeTokens
import com.minddistrict.multiplatformpoc.core.designsystem.core.tokens.SpacingTokens

/**
 * Material Design 3 Expressive tokens.
 * 
 * Expresses personality through:
 * - Playful corner radii (extraLarge: 28dp)
 * - Tonal elevation for depth
 * - Emphasized motion curves for liveliness
 * - Delegates spacing to BaseTokens (8dp grid)
 * 
 * See: https://m3.material.io/styles/motion/easing-and-duration/applying-easing-and-duration
 */
object MaterialTokens {
    /**
     * Spacing tokens - delegate directly to BaseTokens.
     * All design systems share the same 8dp grid for consistency.
     */
    val spacing: SpacingTokens = BaseTokens.spacing
    
    /**
     * Material 3 Expressive shapes with playful corner radii.
     * ExtraLarge uses 28dp for a distinctive personality.
     */
    object shapes : ShapeTokens {
        override val extraSmall: Shape = RoundedCornerShape(8.dp)
        override val small: Shape = RoundedCornerShape(12.dp)
        override val medium: Shape = RoundedCornerShape(16.dp)
        override val large: Shape = RoundedCornerShape(24.dp)
        override val extraLarge: Shape = RoundedCornerShape(28.dp)  // Expressive!
    }
    
    /**
     * Material 3 tonal elevation tokens.
     * Uses elevation to create visual hierarchy without heavy shadows.
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
     * Material 3 Expressive motion tokens.
     * Uses emphasized easing curves for liveliness.
     */
    object motion : MotionTokens {
        override val durationShort: Int = 200
        override val durationMedium: Int = 300
        override val durationLong: Int = 400
        
        // Delegate to base motion curves (already Material 3 spec)
        override val easingStandard = BaseTokens.motion.easingStandard
        override val easingEmphasizedDecelerate = BaseTokens.motion.easingEmphasizedDecelerate
        override val easingEmphasizedAccelerate = BaseTokens.motion.easingEmphasizedAccelerate
    }
}
