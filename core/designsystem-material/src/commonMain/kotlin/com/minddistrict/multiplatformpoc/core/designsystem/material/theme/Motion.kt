package com.minddistrict.multiplatformpoc.core.designsystem.material.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

/**
 * Material 3 Expressive motion system with emphasized easing curves.
 * These easing curves create more personality and expressiveness compared to standard Material motion.
 */
object ExpressiveMotion {
    
    /**
     * Emphasized decelerate easing for enter animations.
     * Creates a sense of anticipation with slower start and quick finish.
     * Use for: Elements entering the screen, expanding animations.
     */
    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    
    /**
     * Emphasized accelerate easing for exit animations.
     * Creates decisive motion that quickly picks up speed.
     * Use for: Elements leaving the screen, collapsing animations.
     */
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    
    /**
     * Standard easing for mid-transition states.
     * Balanced acceleration and deceleration.
     */
    val Standard: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
}

/**
 * Duration constants for Material 3 Expressive animations.
 * Longer durations compared to standard Material for more personality.
 */
object ExpressiveDurations {
    
    /**
     * Short duration for quick micro-interactions.
     * Examples: Button press, icon changes, small state changes.
     */
    const val Short = 200
    
    /**
     * Medium duration for standard UI element changes.
     * Examples: Card expansion, list item entrance, dialog appearance.
     */
    const val Medium = 300
    
    /**
     * Long duration for major screen transitions and complex choreography.
     * Examples: Screen transitions, hero animations, complex sequences.
     */
    const val Long = 400
    
    /**
     * Extra long duration for emphasized moments.
     * Examples: Onboarding, first-time experiences, celebration moments.
     */
    const val ExtraLong = 600
    
    /**
     * Stagger delay for sequential animations.
     * Applied between items in lists, cards, etc.
     */
    const val StaggerDelay = 50
    
    /**
     * Stat bar stagger delay (specific to progress bar animations).
     */
    const val StatBarStagger = 200
}
