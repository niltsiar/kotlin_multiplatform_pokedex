package com.minddistrict.multiplatformpoc.core.designsystem.material.tokens

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Extension property to access Material design tokens from MaterialTheme.
 * 
 * Usage:
 * ```
 * @Composable
 * fun MyComponent() {
 *     Column(
 *         modifier = Modifier.padding(MaterialTheme.tokens.spacing.medium)
 *     ) {
 *         // Card with expressive corner radius
 *         Card(shape = MaterialTheme.tokens.shapes.extraLarge) { }
 *     }
 * }
 * ```
 */
val MaterialTheme.tokens: MaterialDesignTokens
    @Composable
    @ReadOnlyComposable
    get() = LocalMaterialTokens.current

/**
 * Extension property to access Material component tokens from MaterialTheme.
 * 
 * Provides customizable component tokens for:
 * - Cards (elevation, shapes, colors)
 * - Badges (shapes, colors, borders)
 * - Progress bars (motion, colors)
 * 
 * Usage:
 * ```
 * @Composable
 * fun MyCard() {
 *     Card(
 *         tokens = MaterialTheme.componentTokens.card(),
 *         content = { }
 *     )
 * }
 * ```
 */
val MaterialTheme.componentTokens: MaterialComponentTokens
    @Composable
    @ReadOnlyComposable
    get() = LocalMaterialComponentTokens.current
