package com.minddistrict.multiplatformpoc.core.designsystem.unstyled.tokens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    
    /**
     * Typography tokens - uses platform default fonts (not custom).
     * Unstyled theme relies on native platform fonts for optimal integration.
     * 
     * Follows Material 3 scale for consistency but with simpler styling:
     * - No extreme weights (max FontWeight.Medium)
     * - Tighter letter spacing
     * - Platform fonts provide the character
     */
    object typography {
        // Display styles - largest, for hero moments
        val displayLarge = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 57.sp,
            lineHeight = 64.sp,
            letterSpacing = 0.sp
        )
        val displayMedium = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 45.sp,
            lineHeight = 52.sp,
            letterSpacing = 0.sp
        )
        val displaySmall = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 36.sp,
            lineHeight = 44.sp,
            letterSpacing = 0.sp
        )
        
        // Headline styles - for section headings
        val headlineLarge = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp
        )
        val headlineMedium = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp
        )
        val headlineSmall = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp
        )
        
        // Title styles - for card titles, list headers
        val titleLarge = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        )
        val titleMedium = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp
        )
        val titleSmall = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.sp
        )
        
        // Body styles - for main content
        val bodyLarge = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp
        )
        val bodyMedium = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.sp
        )
        val bodySmall = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.sp
        )
        
        // Label styles - for buttons, tabs
        val labelLarge = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.sp
        )
        val labelMedium = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.sp
        )
        val labelSmall = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.sp
        )
    }
}
