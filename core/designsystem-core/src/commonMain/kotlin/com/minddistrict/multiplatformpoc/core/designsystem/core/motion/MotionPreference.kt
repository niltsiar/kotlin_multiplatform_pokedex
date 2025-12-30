package com.minddistrict.multiplatformpoc.core.designsystem.core.motion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Platform-specific function to check if reduced motion is enabled in system accessibility settings.
 * 
 * Platform implementations:
 * - Android: Checks Settings.Global.TRANSITION_ANIMATION_SCALE via AccessibilityManager
 * - iOS: Checks UIAccessibility.isReduceMotionEnabled
 * - Desktop: Returns false (no standard API)
 * 
 * @return true if reduced motion is enabled, false otherwise
 */
@Composable
expect fun isReduceMotionEnabled(): Boolean

/**
 * Composable function that remembers whether reduced motion is enabled.
 * 
 * Use this to conditionally disable animations for accessibility:
 * ```kotlin
 * val reducedMotion = rememberReducedMotion()
 * val animationSpec = if (reducedMotion) snap() else tween(300)
 * ```
 * 
 * @return true if reduced motion is enabled, false otherwise
 */
@Composable
fun rememberReducedMotion(): Boolean = isReduceMotionEnabled()
