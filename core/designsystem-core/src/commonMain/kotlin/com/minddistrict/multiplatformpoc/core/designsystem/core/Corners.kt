package com.minddistrict.multiplatformpoc.core.designsystem.core

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Corner Radius System (Shared Design Tokens)
 * 
 * Shape system for consistent corner radii across all components.
 * Follows Material Design shape scale principles.
 * 
 * Scale: none (0dp) → full (9999dp for pill shapes)
 */
object Corners {
    val none: Dp = 0.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 28.dp
    val full: Dp = 9999.dp
}
