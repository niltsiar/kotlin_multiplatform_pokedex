package com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Elevation helper for Unstyled design system.
 * Provides convenient elevation level constants.
 */
object Elevation {
    /**
     * No elevation (0dp)
     */
    val none: Dp = 0.dp
    
    /**
     * Low elevation (2dp) - for cards, buttons
     */
    val low: Dp = 2.dp
    
    /**
     * Medium elevation (4dp) - for raised components
     */
    val medium: Dp = 4.dp
    
    /**
     * High elevation (8dp) - for dialogs, modals
     */
    val high: Dp = 8.dp
}
