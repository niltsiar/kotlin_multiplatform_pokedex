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
