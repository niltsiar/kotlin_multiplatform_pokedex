package com.minddistrict.multiplatformpoc.core.designsystem.core

import androidx.compose.material3.adaptive.WindowSizeClass
import androidx.compose.material3.adaptive.WindowWidthSizeClass
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Adaptive Layout Utilities (Shared across Material & Unstyled)
 * 
 * Provides responsive design utilities following Material 3 Adaptive guidelines.
 * Uses Compose Multiplatform WindowSizeClass (works on Android, iOS, Desktop, Web).
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
 *     val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
 *     
 *     LazyVerticalGrid(
 *         columns = GridCells.Fixed(gridColumns(windowSizeClass)),
 *         contentPadding = PaddingValues(adaptiveSpacing(windowSizeClass))
 *     ) { ... }
 * }
 * ```
 */

/**
 * Calculate appropriate grid columns based on window size
 * 
 * Returns responsive column count following Material guidelines:
 * - Compact (< 600dp): 2 columns (phone portrait)
 * - Medium (600-839dp): 3 columns (phone landscape, small tablet)
 * - Expanded (≥ 840dp): 4 columns (large tablet, desktop)
 * 
 * **Use cases:**
 * - Product grids (Pokémon cards)
 * - Image galleries
 * - Any grid layout requiring responsive columns
 * 
 * @param windowSizeClass Current window size class from currentWindowAdaptiveInfo()
 * @return Number of columns appropriate for the window size
 */
fun gridColumns(windowSizeClass: WindowSizeClass): Int {
    return when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.COMPACT -> 2   // < 600dp
        WindowWidthSizeClass.MEDIUM -> 3    // 600-839dp
        WindowWidthSizeClass.EXPANDED -> 4  // >= 840dp
        else -> 2
    }
}

/**
 * Calculate adaptive spacing based on window size
 * 
 * Returns appropriate spacing following Material adaptive principles:
 * - Compact: 8dp (maximize content on small screens)
 * - Medium: 16dp (comfortable spacing for medium screens)
 * - Expanded: 24dp (generous spacing for large screens)
 * 
 * **Use cases:**
 * - Grid contentPadding
 * - Screen margins
 * - Component spacing
 * 
 * @param windowSizeClass Current window size class from currentWindowAdaptiveInfo()
 * @return Spacing value (Dp) appropriate for the window size
 */
fun adaptiveSpacing(windowSizeClass: WindowSizeClass): Dp {
    return when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.COMPACT -> Spacing.xs   // 8dp
        WindowWidthSizeClass.MEDIUM -> Spacing.md    // 16dp
        WindowWidthSizeClass.EXPANDED -> Spacing.lg  // 24dp
        else -> Spacing.xs
    }
}

/**
 * Calculate adaptive item spacing (gap between items) based on window size
 * 
 * Returns spacing for gaps between grid items:
 * - Compact: 12dp (comfortable gaps on phones)
 * - Medium: 16dp (standard gaps on tablets)
 * - Expanded: 20dp (generous gaps on large screens)
 * 
 * **Use cases:**
 * - Grid horizontalArrangement/verticalArrangement spacing
 * - Row/Column spacing between items
 * 
 * @param windowSizeClass Current window size class from currentWindowAdaptiveInfo()
 * @return Item spacing value (Dp)
 */
fun adaptiveItemSpacing(windowSizeClass: WindowSizeClass): Dp {
    return when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.COMPACT -> Spacing.sm   // 12dp
        WindowWidthSizeClass.MEDIUM -> Spacing.md    // 16dp
        WindowWidthSizeClass.EXPANDED -> 20.dp       // 20dp
        else -> Spacing.sm
    }
}

/**
 * Determine navigation type based on window size
 * 
 * Returns appropriate navigation pattern following Material 3 Adaptive guidelines:
 * - Compact: BOTTOM_BAR (traditional phone navigation)
 * - Medium: RAIL (vertical navigation for landscape)
 * - Expanded: DRAWER (permanent navigation drawer for large screens)
 * 
 * **Usage:**
 * ```
 * val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
 * val navType = adaptiveNavigationType(windowSizeClass)
 * when (navType) {
 *     NavigationSuiteType.BOTTOM_BAR -> BottomNavigation { ... }
 *     NavigationSuiteType.RAIL -> NavigationRail { ... }
 *     NavigationSuiteType.DRAWER -> NavigationDrawer { ... }
 * }
 * ```
 * 
 * @param windowSizeClass Current window size class from currentWindowAdaptiveInfo()
 * @return NavigationSuiteType enum value
 */
fun adaptiveNavigationType(windowSizeClass: WindowSizeClass): NavigationSuiteType {
    return when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.COMPACT -> NavigationSuiteType.BOTTOM_BAR
        WindowWidthSizeClass.MEDIUM -> NavigationSuiteType.RAIL
        WindowWidthSizeClass.EXPANDED -> NavigationSuiteType.DRAWER
        else -> NavigationSuiteType.BOTTOM_BAR
    }
}

/**
 * Navigation Suite Types
 * 
 * Enum representing different navigation patterns for adaptive layouts.
 * Maps to Material 3 NavigationSuiteScaffold layout types.
 */
enum class NavigationSuiteType {
    /**
     * Bottom navigation bar (Compact screens)
     * - Positioned at bottom of screen
     * - 3-5 top-level destinations
     * - Best for phone portrait
     */
    BOTTOM_BAR,
    
    /**
     * Navigation rail (Medium screens)
     * - Vertical rail on left/start edge
     * - Compact icons with optional labels
     * - Best for phone landscape, small tablets
     */
    RAIL,
    
    /**
     * Navigation drawer (Expanded screens)
     * - Permanent side drawer
     * - Full labels with icons
     * - Best for large tablets, desktops
     */
    DRAWER
}

/**
 * Check if current window is compact (phone portrait)
 * 
 * @return true if width < 600dp
 */
@Composable
fun isCompactWindow(): Boolean {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return windowSizeClass.widthSizeClass == WindowWidthSizeClass.COMPACT
}

/**
 * Check if current window is medium (phone landscape, small tablet)
 * 
 * @return true if width between 600-839dp
 */
@Composable
fun isMediumWindow(): Boolean {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return windowSizeClass.widthSizeClass == WindowWidthSizeClass.MEDIUM
}

/**
 * Check if current window is expanded (large tablet, desktop)
 * 
 * @return true if width >= 840dp
 */
@Composable
fun isExpandedWindow(): Boolean {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return windowSizeClass.widthSizeClass == WindowWidthSizeClass.EXPANDED
}
