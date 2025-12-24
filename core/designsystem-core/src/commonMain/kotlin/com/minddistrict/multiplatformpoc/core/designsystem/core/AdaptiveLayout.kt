package com.minddistrict.multiplatformpoc.core.designsystem.core

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Adaptive Layout Utilities (Shared across Material & Unstyled)
 * 
 * Provides responsive design utilities following Material 3 Adaptive guidelines.
 * Uses Compose Multiplatform WindowAdaptiveInfo API (works on Android, iOS, Desktop, Web).
 * 
 * **API Pattern:**
 * - Uses `WindowAdaptiveInfo.windowSizeClass.widthSizeClass` (enum-based API)
 * - Returns `WindowWidthSizeClass.Compact`, `Medium`, or `Expanded`
 * - NOT the deprecated dp-based `windowWidthDp` property
 * 
 * **Breakpoints (Material 3 Standard):**
 * - **Compact**: 0-599dp (phone portrait, optimize for single-column)
 * - **Medium**: 600-839dp (phone landscape, small tablet, two-column layouts)
 * - **Expanded**: 840dp+ (large tablet, desktop, multi-column layouts)
 * 
 * **Official Resources:**
 * - [CMP Adaptive Layouts](https://kotlinlang.org/docs/multiplatform/compose-adaptive-layouts.html)
 * - [Material 3 Adaptive](https://m3.material.io/foundations/layout/applying-layout/window-size-classes)
 * - [Compose Material3 Adaptive](https://developer.android.com/develop/ui/compose/layouts/adaptive)
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
    return when (windowAdaptiveInfo.windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 2
        WindowWidthSizeClass.Medium -> 3
        WindowWidthSizeClass.Expanded -> 4
        else -> 2
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
    return when (windowAdaptiveInfo.windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 8.dp
        WindowWidthSizeClass.Medium -> 16.dp
        WindowWidthSizeClass.Expanded -> 24.dp
        else -> 8.dp
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
    return when (windowAdaptiveInfo.windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 12.dp
        WindowWidthSizeClass.Medium -> 16.dp
        WindowWidthSizeClass.Expanded -> 20.dp
        else -> 12.dp
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
    return when (windowAdaptiveInfo.windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> NavigationSuiteType.BOTTOM_BAR
        WindowWidthSizeClass.Medium -> NavigationSuiteType.RAIL
        WindowWidthSizeClass.Expanded -> NavigationSuiteType.DRAWER
        else -> NavigationSuiteType.BOTTOM_BAR
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
    return windowAdaptiveInfo.windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
}

/**
 * Checks if the current window is Medium (600-839dp width).
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun isMediumWindow(): Boolean {
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    return windowAdaptiveInfo.windowSizeClass.widthSizeClass == WindowWidthSizeClass.Medium
}

/**
 * Checks if the current window is Expanded (≥840dp width).
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun isExpandedWindow(): Boolean {
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    return windowAdaptiveInfo.windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
}
