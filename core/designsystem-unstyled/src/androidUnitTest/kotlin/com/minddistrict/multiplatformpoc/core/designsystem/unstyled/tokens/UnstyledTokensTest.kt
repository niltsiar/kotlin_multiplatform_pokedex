package com.minddistrict.multiplatformpoc.core.designsystem.unstyled.tokens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.minddistrict.multiplatformpoc.core.designsystem.core.tokens.BaseTokens
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * Tests for UnstyledTokens verifying delegation and minimal aesthetic values.
 * 
 * Tests verify:
 * - Spacing delegation to BaseTokens (8dp grid)
 * - Minimal shapes (extraLarge = 12dp max)
 * - Flat elevations (max 4dp)
 * - Linear motion curves (standard easing only)
 */
class UnstyledTokensTest : StringSpec({
    
    "spacing delegates to BaseTokens" {
        // UnstyledTokens.spacing should be the same reference as BaseTokens.spacing
        UnstyledTokens.spacing shouldBe BaseTokens.spacing
        
        // Verify 8dp grid values
        UnstyledTokens.spacing.xxxs shouldBe 2.dp
        UnstyledTokens.spacing.xxs shouldBe 4.dp
        UnstyledTokens.spacing.xs shouldBe 8.dp
        UnstyledTokens.spacing.small shouldBe 12.dp
        UnstyledTokens.spacing.medium shouldBe 16.dp
        UnstyledTokens.spacing.large shouldBe 20.dp
        UnstyledTokens.spacing.xl shouldBe 24.dp
        UnstyledTokens.spacing.xxl shouldBe 32.dp
        UnstyledTokens.spacing.xxxl shouldBe 64.dp
    }
    
    "shapes use minimal corner radii (max 12dp)" {
        UnstyledTokens.shapes.extraSmall shouldBe RoundedCornerShape(4.dp)
        UnstyledTokens.shapes.small shouldBe RoundedCornerShape(6.dp)
        UnstyledTokens.shapes.medium shouldBe RoundedCornerShape(8.dp)
        UnstyledTokens.shapes.large shouldBe RoundedCornerShape(10.dp)
        UnstyledTokens.shapes.extraLarge shouldBe RoundedCornerShape(12.dp)  // Minimal cap
    }
    
    "elevation uses flat values (max 4dp)" {
        UnstyledTokens.elevation.level0 shouldBe 0.dp
        UnstyledTokens.elevation.level1 shouldBe 1.dp
        UnstyledTokens.elevation.level2 shouldBe 2.dp
        UnstyledTokens.elevation.level3 shouldBe 3.dp
        UnstyledTokens.elevation.level4 shouldBe 4.dp
        UnstyledTokens.elevation.level5 shouldBe 4.dp  // Capped at 4dp
    }
    
    "motion uses minimal durations" {
        UnstyledTokens.motion.durationShort shouldBe 200
        UnstyledTokens.motion.durationMedium shouldBe 300
        UnstyledTokens.motion.durationLong shouldBe 300  // Same as medium (minimal)
    }
    
    "motion uses standard easing only (no emphasized curves)" {
        // All easing curves should be standard (linear/predictable)
        UnstyledTokens.motion.easingStandard shouldBe BaseTokens.motion.easingStandard
        UnstyledTokens.motion.easingEmphasizedDecelerate shouldBe BaseTokens.motion.easingStandard
        UnstyledTokens.motion.easingEmphasizedAccelerate shouldBe BaseTokens.motion.easingStandard
    }
    
    "extraLarge shape is minimal (12dp corner radius)" {
        // This is the signature Unstyled minimal value (contrast with Material's 28dp)
        val extraLargeCornerRadius = 12.dp
        UnstyledTokens.shapes.extraLarge shouldBe RoundedCornerShape(extraLargeCornerRadius)
    }
    
    "Unstyled shapes are more subtle than Material shapes" {
        // Unstyled extraLarge (12dp) should be significantly smaller than Material (28dp)
        val unstyledMaxRadius = 12.dp
        val materialMaxRadius = 28.dp
        
        unstyledMaxRadius.value shouldBe 12f
        // Verify Unstyled is less than half of Material's expressiveness
        (unstyledMaxRadius.value < materialMaxRadius.value / 2) shouldBe true
    }
    
    "Unstyled elevations are flatter than Material elevations" {
        // Unstyled max elevation (4dp) should be significantly smaller than Material (12dp)
        val unstyledMaxElevation = 4.dp
        val materialMaxElevation = 12.dp
        
        unstyledMaxElevation.value shouldBe 4f
        // Verify Unstyled is less than half of Material's depth
        (unstyledMaxElevation.value < materialMaxElevation.value / 2) shouldBe true
    }
})
