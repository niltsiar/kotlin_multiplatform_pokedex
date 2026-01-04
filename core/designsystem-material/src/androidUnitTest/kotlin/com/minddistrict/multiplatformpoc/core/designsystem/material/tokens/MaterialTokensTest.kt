package com.minddistrict.multiplatformpoc.core.designsystem.material.tokens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.minddistrict.multiplatformpoc.core.designsystem.core.tokens.BaseTokens
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * Tests for MaterialTokens verifying delegation and Material 3 Expressive values.
 * 
 * Tests verify:
 * - Spacing delegation to BaseTokens (8dp grid)
 * - Expressive shapes (extraLarge = 28dp)
 * - Tonal elevation values
 * - Emphasized motion curves
 */
class MaterialTokensTest : StringSpec({
    
    "spacing delegates to BaseTokens" {
        // MaterialTokens.spacing should be the same reference as BaseTokens.spacing
        MaterialTokens.spacing shouldBe BaseTokens.spacing
        
        // Verify 8dp grid values
        MaterialTokens.spacing.xxxs shouldBe 2.dp
        MaterialTokens.spacing.xxs shouldBe 4.dp
        MaterialTokens.spacing.xs shouldBe 8.dp
        MaterialTokens.spacing.small shouldBe 12.dp
        MaterialTokens.spacing.medium shouldBe 16.dp
        MaterialTokens.spacing.large shouldBe 20.dp
        MaterialTokens.spacing.xl shouldBe 24.dp
        MaterialTokens.spacing.xxl shouldBe 32.dp
        MaterialTokens.spacing.xxxl shouldBe 64.dp
    }
    
    "shapes use Material 3 Expressive corner radii" {
        MaterialTokens.shapes.extraSmall shouldBe RoundedCornerShape(8.dp)
        MaterialTokens.shapes.small shouldBe RoundedCornerShape(12.dp)
        MaterialTokens.shapes.medium shouldBe RoundedCornerShape(16.dp)
        MaterialTokens.shapes.large shouldBe RoundedCornerShape(24.dp)
        MaterialTokens.shapes.extraLarge shouldBe RoundedCornerShape(28.dp)  // Expressive!
    }
    
    "elevation uses Material 3 tonal elevation values" {
        MaterialTokens.elevation.level0 shouldBe 0.dp
        MaterialTokens.elevation.level1 shouldBe 1.dp
        MaterialTokens.elevation.level2 shouldBe 3.dp
        MaterialTokens.elevation.level3 shouldBe 6.dp
        MaterialTokens.elevation.level4 shouldBe 8.dp
        MaterialTokens.elevation.level5 shouldBe 12.dp
    }
    
    "motion uses Material 3 Expressive durations" {
        MaterialTokens.motion.durationShort shouldBe 200
        MaterialTokens.motion.durationMedium shouldBe 300
        MaterialTokens.motion.durationLong shouldBe 400
    }
    
    "motion curves delegate to BaseTokens" {
        // Emphasized curves should be the same as BaseTokens
        MaterialTokens.motion.easingStandard shouldBe BaseTokens.motion.easingStandard
        MaterialTokens.motion.easingEmphasizedDecelerate shouldBe BaseTokens.motion.easingEmphasizedDecelerate
        MaterialTokens.motion.easingEmphasizedAccelerate shouldBe BaseTokens.motion.easingEmphasizedAccelerate
    }
    
    "extraLarge shape is distinctive (28dp corner radius)" {
        // This is the signature Material 3 Expressive value
        val extraLargeCornerRadius = 28.dp
        MaterialTokens.shapes.extraLarge shouldBe RoundedCornerShape(extraLargeCornerRadius)
    }
})
