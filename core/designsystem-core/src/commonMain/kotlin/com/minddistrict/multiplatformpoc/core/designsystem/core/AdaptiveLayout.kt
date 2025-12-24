package com.minddistrict.multiplatformpoc.core.designsystem.core

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Adaptive Layout Utilities (Shared across Material & Unstyled)
 * 
 * Provides responsive design utilities following Material 3 Adaptive guidelines.
 * Uses Compose Multiplatform WindowAdaptiveInfo (works on Android, iOS, Desktop, Web).
 * 
 * **Breakpoints (Material 3 Standard):**
 * - **Compact**: 0-599dp (phone portrait, optimize for single-column)
 * - **Medium**: 600-839dp (phone landscape, small tablet, two-column layouts)
 * - **Expanded**: 840dp+ (large tablet, desktop, multi-column layouts)
 * 
 * **Resources:**
 * - [CMP Adaptive Layouts](https://kotlinlang.org/docs/multiplatform/compose-adaptive-layouts.html)
 * - [Material 3 Adaptive](https://m3.material.io/foundations/layout/applying-layout/window-size-classes)
 * - [Touchlab Guide](https://touchlab.co/adaptive-layouts-cmp)
 * 
 * **Usage:**
 * ```
 * @Composable
 * fun MyScreen() {
 *     val windowAdaptiveInfo = currentWindowAdaptiveInfo()
 *     val columns = gridColumns(windowAdaptiveInfo)
 *     
 *     LazyVerticalGrid(
 *         columns = GridCells.Fixed(columns),
 *         contentPadding = PaddingValues(adaptiveSpacing(windowAdaptiveInfo))
 *     ) { ... }
 * }
 * ```
 */

// Breakpoint Constants (Material 3 Standard - in dp)
private const val WIDTH_COMPACT_UPPER_BOUND = 600
private const val WIDTH_MEDIUM_UPPER_BOUND = 840

/**
 * Determines the number of grid columns based on window width.
 * 
 * Returns:
 * - **2 columns** for Compact (<600dp)
 * - **3 columns** for Medium (600-839dp)
 * - **4 columns** for Expanded (≥840dp)
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun gridColumns(windowAdaptiveInfo: WindowAdaptiveInfo): Int {
    val widthDp = windowAdaptiveInfo.windowSizeClass.windowWidthDp
    return when {
        widthDp < WIDTH_COMPACT_UPPER_BOUND -> 2
        widthDp < WIDTH_MEDIUM_UPPER_BOUND -> 3
        else -> 4 // EXPANDED
    }
}

/**
 * Returns adaptive spacing based on window width.
 * 
 * Returns:
 * - **8dp** (Spacing.xs) for Compact
 * - **16dp** (Spacing.md) for Medium
 * - **24dp** (Spacing.lg) for Expanded
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun adaptiveSpacing(windowAdaptiveInfo: WindowAdaptiveInfo): Dp {
    val widthDp = windowAdaptiveInfo.windowSizeClass.windowWidthDp
    return when {
        widthDp < WIDTH_COMPACT_UPPER_BOUND -> 8.dp
        widthDp < WIDTH_MEDIUM_UPPER_BOUND -> 16.dp
        else -> 24.dp // EXPANDED
    }
}

/**
 * Returns adaptive item spacing for grids/lists.
 * 
 * Returns:
 * - **12dp** (Spacing.sm) for Compact
 * - **16dp** (Spacing.md) for Medium
 * - **20dp** for Expanded
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun adaptiveItemSpacing(windowAdaptiveInfo: WindowAdaptiveInfo): Dp {
    val widthDp = windowAdaptiveInfo.windowSizeClass.windowWidthDp
    return when {
        widthDp < WIDTH_COMPACT_UPPER_BOUND -> 12.dp
        widthDp < WIDTH_MEDIUM_UPPER_BOUND -> 16.dp
        else -> 20.dp // EXPANDED
    }
}

/**
 * Navigation Type for Adaptive Navigation UI.
 * 
 * Determines the appropriate navigation pattern based on screen size:
 * - **BOTTOM_BAR**: Compact screens (phones)
 * - **RAIL**: Medium screens (tablets in portrait)
 * - **DRAWER**: Expanded screens (tablets in landscape, desktops)
 */
enum class NavigationSuiteType {
    BOTTOM_BAR, RAIL, DRAWER
}

/**
 * Returns the recommended navigation type based on window width.
 * 
 * Returns:
 * - **BOTTOM_BAR** for Compact
 * - **RAIL** for Medium
 * - **DRAWER** for Expanded
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun adaptiveNavigationType(windowAdaptiveInfo: WindowAdaptiveInfo): NavigationSuiteType {
    val widthDp = windowAdaptiveInfo.windowSizeClass.windowWidthDp
    return when {
        widthDp < WIDTH_COMPACT_UPPER_BOUND -> NavigationSuiteType.BOTTOM_BAR
        widthDp < WIDTH_MEDIUM_UPPER_BOUND -> NavigationSuiteType.RAIL
        else -> NavigationSuiteType.DRAWER // EXPANDED
    }
}

// Composable Helper Functions for Convenience

/**
 * Checks if the current window is Compact (<600dp width).
 * 
 * Usage:
 * ```
 * if (isCompactWindow()) {
 *     // Single column layout
 * }
 * ```
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun isCompactWindow(): Boolean {
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    return windowAdaptiveInfo.windowSizeClass.windowWidthDp < WIDTH_COMPACT_UPPER_BOUND
}

/**
 * Checks if the current window is Medium (600-839dp width).
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun isMediumWindow(): Boolean {
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val widthDp = windowAdaptiveInfo.windowSizeClass.windowWidthDp
    return widthDp >= WIDTH_COMPACT_UPPER_BOUND && widthDp < WIDTH_MEDIUM_UPPER_BOUND
}

/**
 * Checks if the current window is Expanded (≥840dp width).
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun isExpandedWindow(): Boolean {
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    return windowAdaptiveInfo.windowSizeClass.windowWidthDp >= WIDTH_MEDIUM_UPPER_BOUND
}
