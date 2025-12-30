package com.minddistrict.multiplatformpoc.core.designsystem.unstyled.tokens

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
 * Unstyled/Minimal design tokens.
 * 
 * Expresses minimalism through:
 * - Subtle corner radii (max 12dp)
 * - Flat elevations (max 4dp)
 * - Linear/standard motion curves
 * - Delegates spacing to BaseTokens (8dp grid)
 * 
 * Philosophy: Let content speak for itself with minimal visual interference.
 */
object UnstyledTokens {
    /**
     * Spacing tokens - delegate directly to BaseTokens.
     * All design systems share the same 8dp grid for consistency.
     */
    val spacing: SpacingTokens = BaseTokens.spacing
    
    /**
     * Minimal shapes with subtle corner radii (max 12dp).
     * Restraint in rounding creates a clean, minimal aesthetic.
     */
    object shapes : ShapeTokens {
        override val extraSmall: Shape = RoundedCornerShape(4.dp)
        override val small: Shape = RoundedCornerShape(6.dp)
        override val medium: Shape = RoundedCornerShape(8.dp)
        override val large: Shape = RoundedCornerShape(10.dp)
        override val extraLarge: Shape = RoundedCornerShape(12.dp)  // Minimal cap
    }
    
    /**
     * Flat elevation tokens (max 4dp).
     * Minimal shadows create subtle depth without visual distraction.
     */
    object elevation : ElevationTokens {
        override val level0: Dp = 0.dp
        override val level1: Dp = 1.dp
        override val level2: Dp = 2.dp
        override val level3: Dp = 3.dp
        override val level4: Dp = 4.dp
        override val level5: Dp = 4.dp  // Cap at 4dp for flat aesthetic
    }
    
    /**
     * Linear/standard motion tokens.
     * Uses only standard easing for predictable, non-distracting animations.
     */
    object motion : MotionTokens {
        override val durationShort: Int = 200
        override val durationMedium: Int = 300
        override val durationLong: Int = 300  // Same as medium (minimal)
        
        // Use standard easing for all animations (no emphasis)
        override val easingStandard = BaseTokens.motion.easingStandard
        override val easingEmphasizedDecelerate = BaseTokens.motion.easingStandard  // Linear
        override val easingEmphasizedAccelerate = BaseTokens.motion.easingStandard  // Linear
    }
}
