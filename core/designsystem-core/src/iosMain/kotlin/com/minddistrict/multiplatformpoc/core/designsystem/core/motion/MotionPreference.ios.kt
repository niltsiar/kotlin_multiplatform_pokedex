package com.minddistrict.multiplatformpoc.core.designsystem.core.motion

import androidx.compose.runtime.Composable
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled

/**
 * iOS implementation of motion preference detection.
 * 
 * Checks UIAccessibilityIsReduceMotionEnabled() to respect the "Reduce Motion"
 * accessibility setting in iOS Settings > Accessibility > Motion.
 * 
 * @return true if Reduce Motion is enabled, false otherwise
 */
@Composable
actual fun isReduceMotionEnabled(): Boolean {
    return UIAccessibilityIsReduceMotionEnabled()
}
