package com.minddistrict.multiplatformpoc.core.designsystem.material.tokens

import androidx.compose.runtime.staticCompositionLocalOf
import com.minddistrict.multiplatformpoc.core.designsystem.core.tokens.ElevationTokens
import com.minddistrict.multiplatformpoc.core.designsystem.core.tokens.MotionTokens
import com.minddistrict.multiplatformpoc.core.designsystem.core.tokens.ShapeTokens
import com.minddistrict.multiplatformpoc.core.designsystem.core.tokens.SpacingTokens

/**
 * CompositionLocal for accessing Material design tokens within the theme hierarchy.
 * 
 * Usage:
 * ```
 * @Composable
 * fun MyComponent() {
 *     val tokens = LocalMaterialTokens.current
 *     Box(modifier = Modifier.padding(tokens.spacing.medium))
 * }
 * ```
 * 
 * Or use the extension property:
 * ```
 * MaterialTheme.tokens.spacing.medium
 * ```
 */
val LocalMaterialTokens = staticCompositionLocalOf<MaterialDesignTokens> {
    error("No MaterialTokens provided. Make sure PokemonTheme wraps your content.")
}

/**
 * Interface representing all Material design tokens.
 * Allows for future theme variants or customization.
 */
interface MaterialDesignTokens {
    val spacing: SpacingTokens
    val shapes: ShapeTokens
    val elevation: ElevationTokens
    val motion: MotionTokens
}

/**
 * Default implementation of Material design tokens.
 * Uses values from MaterialTokens object.
 */
internal class DefaultMaterialTokens : MaterialDesignTokens {
    override val spacing: SpacingTokens = MaterialTokens.spacing
    override val shapes: ShapeTokens = MaterialTokens.shapes
    override val elevation: ElevationTokens = MaterialTokens.elevation
    override val motion: MotionTokens = MaterialTokens.motion
}
