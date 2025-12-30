package com.minddistrict.multiplatformpoc.core.designsystem.core.motion

import androidx.compose.runtime.Composable

/**
 * JVM/Desktop implementation of motion preference detection.
 * 
 * Desktop platforms (Windows, macOS, Linux) don't have a standard API for detecting
 * reduced motion preference, so we default to false (animations enabled).
 * 
 * @return false (no standard API available on desktop)
 */
@Composable
actual fun isReduceMotionEnabled(): Boolean = false
