package com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Unstyled Theme System
 * Provides color tokens matching Material 3 Expressive but without Material components
 */
data class UnstyledColorScheme(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val outlineVariant: Color,
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val error: Color,
    val onError: Color,
)

val LocalUnstyledColorScheme = staticCompositionLocalOf<UnstyledColorScheme> {
    error("No UnstyledColorScheme provided")
}

@Composable
fun UnstyledTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        UnstyledColorScheme(
            background = UnstyledColors.Dark.background,
            surface = UnstyledColors.Dark.surface,
            surfaceVariant = UnstyledColors.Dark.surfaceVariant,
            onSurface = UnstyledColors.Dark.onSurface,
            onSurfaceVariant = UnstyledColors.Dark.onSurfaceVariant,
            outline = UnstyledColors.Dark.outline,
            outlineVariant = UnstyledColors.Dark.outlineVariant,
            primary = UnstyledColors.Dark.primary,
            onPrimary = UnstyledColors.Dark.onPrimary,
            secondary = UnstyledColors.Dark.secondary,
            onSecondary = UnstyledColors.Dark.onSecondary,
            error = UnstyledColors.Dark.error,
            onError = UnstyledColors.Dark.onError,
        )
    } else {
        UnstyledColorScheme(
            background = UnstyledColors.Light.background,
            surface = UnstyledColors.Light.surface,
            surfaceVariant = UnstyledColors.Light.surfaceVariant,
            onSurface = UnstyledColors.Light.onSurface,
            onSurfaceVariant = UnstyledColors.Light.onSurfaceVariant,
            outline = UnstyledColors.Light.outline,
            outlineVariant = UnstyledColors.Light.outlineVariant,
            primary = UnstyledColors.Light.primary,
            onPrimary = UnstyledColors.Light.onPrimary,
            secondary = UnstyledColors.Light.secondary,
            onSecondary = UnstyledColors.Light.onSecondary,
            error = UnstyledColors.Light.error,
            onError = UnstyledColors.Light.onError,
        )
    }

    CompositionLocalProvider(
        LocalUnstyledColorScheme provides colorScheme,
        content = content
    )
}

/**
 * Access current Unstyled color scheme
 */
object UnstyledTheme {
    val colorScheme: UnstyledColorScheme
        @Composable
        get() = LocalUnstyledColorScheme.current
}
