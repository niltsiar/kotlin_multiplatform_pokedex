package com.minddistrict.multiplatformpoc.core.designsystem.core

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Elevation System (Shared Design Tokens)
 * 
 * Elevation levels for shadows and depth hierarchy.
 * Follows Material Design elevation principles.
 * 
 * Scale: level0/none (0dp) → level5 (12dp, highest)
 */
object Elevation {
    val level0: Dp = 0.dp
    val level1: Dp = 1.dp
    val level2: Dp = 3.dp
    val level3: Dp = 6.dp
    val level4: Dp = 8.dp
    val level5: Dp = 12.dp
    
    // Convenience aliases
    val none: Dp = level0
    val low: Dp = level1
    val medium: Dp = level3
    val high: Dp = level4
}
