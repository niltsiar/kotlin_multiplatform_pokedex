package com.minddistrict.multiplatformpoc.core.designsystem.core.tokens

import androidx.compose.animation.core.Easing
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

/**
 * Spacing tokens define the vertical and horizontal space between UI elements.
 * Based on an 8dp grid system for consistent spacing across the design.
 */
interface SpacingTokens {
    val xxxs: Dp
    val xxs: Dp
    val xs: Dp
    val small: Dp
    val medium: Dp
    val large: Dp
    val xl: Dp
    val xxl: Dp
    val xxxl: Dp
}

/**
 * Shape tokens define the corner radii for UI elements.
 * Different design systems use different corner radii to express their personality.
 */
interface ShapeTokens {
    val extraSmall: Shape
    val small: Shape
    val medium: Shape
    val large: Shape
    val extraLarge: Shape
}

/**
 * Elevation tokens define the depth/shadow of UI elements.
 * Used for visual hierarchy and layering.
 */
interface ElevationTokens {
    val level0: Dp
    val level1: Dp
    val level2: Dp
    val level3: Dp
    val level4: Dp
    val level5: Dp
}

/**
 * Motion tokens define animation durations and easing curves.
 * Controls the personality of UI animations.
 */
interface MotionTokens {
    /** Short duration in milliseconds (e.g., 200ms) */
    val durationShort: Int
    
    /** Medium duration in milliseconds (e.g., 300ms) */
    val durationMedium: Int
    
    /** Long duration in milliseconds (e.g., 400ms) */
    val durationLong: Int
    
    /** Standard easing curve for most animations */
    val easingStandard: Easing
    
    /** Emphasized deceleration curve (Material 3 Expressive) */
    val easingEmphasizedDecelerate: Easing
    
    /** Emphasized acceleration curve (Material 3 Expressive) */
    val easingEmphasizedAccelerate: Easing
}
