package com.minddistrict.multiplatformpoc.core.designsystem.core.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

/**
 * Component-level design tokens that define the appearance of UI components.
 * 
 * These token interfaces allow theme-agnostic components to be styled differently
 * in Material and Unstyled themes without code duplication.
 * 
 * Pattern:
 * 1. Define token interface (e.g., CardTokens)
 * 2. Create theme-agnostic component accepting token interface
 * 3. Each design system provides its own implementation
 * 4. Components accept optional overrides for customization
 */

/**
 * Tokens for card-based components (PokemonCard).
 */
interface CardTokens {
    val shape: Shape
    val elevation: Dp
    val backgroundColor: Color
    val contentColor: Color
    val pressedScale: Float
}

/**
 * Tokens for type badge components.
 * Material uses filled badges, Unstyled uses border-only.
 */
interface BadgeTokens {
    val shape: Shape
    val borderWidth: Dp
    val fillAlpha: Float  // 0f = outline only, 1f = filled
    val textColor: Color
}

/**
 * Tokens for progress bar components (stat bars).
 */
interface ProgressBarTokens {
    val height: Dp
    val shape: Shape
    val backgroundColor: Color
    val foregroundColor: Color
    val animationSpec: AnimationSpec<Float>
}
