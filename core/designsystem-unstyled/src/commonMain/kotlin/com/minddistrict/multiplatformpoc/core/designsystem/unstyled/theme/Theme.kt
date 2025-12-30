package com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme

import androidx.compose.animation.core.Easing
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.composeunstyled.platformtheme.EmojiVariant
import com.composeunstyled.platformtheme.WebFontOptions
import com.composeunstyled.platformtheme.buildPlatformTheme
import com.composeunstyled.theme.ThemeProperty
import com.composeunstyled.theme.ThemeToken
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.tokens.UnstyledTokens

/**
 * Unstyled Theme System using Compose Unstyled Platform Theme
 * 
 * Architecture:
 * - Uses buildPlatformTheme for platform-native fonts, sizes, and touch feedback
 * - Pre-defined platform tokens: textStyles (text1-9, heading1-9), indications (bright/dimmed),
 *   shapes (rounded*), interactiveSizes (sizeDefault/sizeMinimum)
 * - Custom color palette defined via ThemeProperty/ThemeToken - Pokéball-inspired distinct from Material
 * - Custom spacing tokens defined via ThemeProperty/ThemeToken - consistent with core
 * 
 * Dynamic Theming:
 * - Automatically switches between light and dark color schemes based on system settings
 * - Light theme: Pokéball red (#EE1515) primary, light backgrounds (#FFFBFE)
 * - Dark theme: Pink (#FFB4AB) primary, dark backgrounds (#1C1B1F)
 * - Theme recomposes when system dark mode preference changes (no manual switching needed)
 * 
 * Platform-specific behavior:
 * - iOS: San Francisco font 12-34sp, 44dp touch targets, 0.25f alpha snap indication
 * - Android: Roboto font 11-57sp, 48dp touch targets, ripple indication
 * - Desktop: System fonts 10-68sp, 28dp touch targets
 * - Web: NotoSans font, 28dp touch targets, 0.08f alpha tween indication
 * 
 * Default properties:
 * - defaultContentColor: Automatically switches based on light/dark mode
 * - defaultTextStyle: 16sp with normal weight (platform fonts applied automatically)
 * - defaultIndication: Platform-native touch feedback (ripple on Android, etc.)
 * 
 * Usage:
 * ```
 * UnstyledTheme {
 *     // Platform tokens
 *     Text("Hello", style = Theme[textStyles][heading5])
 *     Box(modifier = Modifier
 *         .indication(Theme[indications][bright])
 *         .interactiveSize(Theme[interactiveSizes][sizeDefault])  // Accessibility-friendly touch targets
 *         .clip(Theme[shapes][roundedMedium])
 *     )
 *     
 *     // Custom colors & spacing (via Theme - automatically light/dark)
 *     Box(modifier = Modifier
 *         .background(Theme[colors][primary])
 *         .padding(Theme[spacing][spacingMd])
 *     )
 *     
 *     // Default content color and text style applied automatically
 *     Text("Uses defaultTextStyle")  // No explicit style needed
 * }
 * ```
 */

// Custom Theme Properties for Colors
val colors = ThemeProperty<Color>("colors")
val primary = ThemeToken<Color>("primary")
val background = ThemeToken<Color>("background")
val surface = ThemeToken<Color>("surface")
val error = ThemeToken<Color>("error")
val onPrimary = ThemeToken<Color>("onPrimary")
val onBackground = ThemeToken<Color>("onBackground")
val onSurface = ThemeToken<Color>("onSurface")
val onError = ThemeToken<Color>("onError")

// Custom Theme Properties for Spacing
val spacing = ThemeProperty<Dp>("spacing")
val spacingXxxs = ThemeToken<Dp>("xxxs")
val spacingXxs = ThemeToken<Dp>("xxs")
val spacingXs = ThemeToken<Dp>("xs")
val spacingSm = ThemeToken<Dp>("sm")
val spacingMd = ThemeToken<Dp>("md")
val spacingLg = ThemeToken<Dp>("lg")
val spacingXl = ThemeToken<Dp>("xl")
val spacingXxl = ThemeToken<Dp>("xxl")
val spacingXxxl = ThemeToken<Dp>("xxxl")

// Custom Theme Properties for Shapes
val shapes = ThemeProperty<Shape>("shapes")
val shapeExtraSmall = ThemeToken<Shape>("extraSmall")
val shapeSmall = ThemeToken<Shape>("small")
val shapeMedium = ThemeToken<Shape>("medium")
val shapeLarge = ThemeToken<Shape>("large")
val shapeExtraLarge = ThemeToken<Shape>("extraLarge")

// Custom Theme Properties for Elevation
val elevation = ThemeProperty<Dp>("elevation")
val elevationLevel0 = ThemeToken<Dp>("level0")
val elevationLevel1 = ThemeToken<Dp>("level1")
val elevationLevel2 = ThemeToken<Dp>("level2")
val elevationLevel3 = ThemeToken<Dp>("level3")
val elevationLevel4 = ThemeToken<Dp>("level4")
val elevationLevel5 = ThemeToken<Dp>("level5")

// Custom Theme Properties for Motion (durations)
val motionDuration = ThemeProperty<Int>("motionDuration")
val durationShort = ThemeToken<Int>("short")
val durationMedium = ThemeToken<Int>("medium")
val durationLong = ThemeToken<Int>("long")

// Custom Theme Properties for Motion (easing curves)
val motionEasing = ThemeProperty<Easing>("motionEasing")
val easingStandard = ThemeToken<Easing>("standard")
val easingEmphasizedDecelerate = ThemeToken<Easing>("emphasizedDecelerate")
val easingEmphasizedAccelerate = ThemeToken<Easing>("emphasizedAccelerate")

// Custom Theme Properties for Typography
val typography = ThemeProperty<TextStyle>("typography")
val displayLarge = ThemeToken<TextStyle>("displayLarge")
val displayMedium = ThemeToken<TextStyle>("displayMedium")
val displaySmall = ThemeToken<TextStyle>("displaySmall")
val headlineLarge = ThemeToken<TextStyle>("headlineLarge")
val headlineMedium = ThemeToken<TextStyle>("headlineMedium")
val headlineSmall = ThemeToken<TextStyle>("headlineSmall")
val titleLarge = ThemeToken<TextStyle>("titleLarge")
val titleMedium = ThemeToken<TextStyle>("titleMedium")
val titleSmall = ThemeToken<TextStyle>("titleSmall")
val bodyLarge = ThemeToken<TextStyle>("bodyLarge")
val bodyMedium = ThemeToken<TextStyle>("bodyMedium")
val bodySmall = ThemeToken<TextStyle>("bodySmall")
val labelLarge = ThemeToken<TextStyle>("labelLarge")
val labelMedium = ThemeToken<TextStyle>("labelMedium")
val labelSmall = ThemeToken<TextStyle>("labelSmall")

/**
 * Platform Theme with Native Tokens + Custom Properties + Dynamic Theming
 * 
 * **Pre-defined tokens** (automatically provided by buildPlatformTheme):
 * - **textStyles**: text1-9 (body), heading1-9 (headings) with platform fonts/sizes
 * - **indications**: bright (light bg), dimmed (dark bg) with platform touch feedback
 * - **shapes**: roundedNone, roundedSmall (4dp), roundedMedium (6dp), roundedLarge (8dp), roundedFull
 * - **interactiveSizes**: sizeDefault (platform touch target), sizeMinimum (compact target)
 * 
 * **Custom properties** (defined above via ThemeProperty/ThemeToken):
 * - **colors**: Pokéball-inspired color palette (primary, background, surface, error, on* variants)
 *   - Light theme: Pokéball red (#EE1515) primary, light backgrounds
 *   - Dark theme: Pink/blue tinted primary, dark backgrounds
 * - **spacing**: Shared spacing tokens (xxxs through xxxl)
 * 
 * **Default properties** (reduces repetitive styling):
 * - **defaultContentColor**: Switches between onSurface colors based on light/dark mode
 * - **defaultTextStyle**: 16sp font with normal weight, platform fonts applied automatically
 * - **Theme name**: "UnstyledTheme" for descriptive error messages during development
 * 
 * **Dynamic theming**:
 * - Automatically switches between light/dark color schemes based on system settings
 * - Uses isSystemInDarkTheme() to detect system preference
 * - Theme recomposes when system theme changes
 */
val UnstyledTheme = buildPlatformTheme(
    webFontOptions = WebFontOptions(
        emojiVariant = EmojiVariant.Colored
    )
) {
    // Theme name for better error messages during development
    name = "UnstyledTheme"
    
    // Dynamic theme switching based on system preference
    val isDark = isSystemInDarkTheme()
    
    // Default content color (automatically switches with light/dark mode)
    defaultContentColor = if (isDark) {
        UnstyledColors.Dark.onSurface
    } else {
        UnstyledColors.Light.onSurface
    }
    
    // Default text style (platform fonts applied automatically by buildPlatformTheme)
    defaultTextStyle = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal
    )
    
    // Custom color palette with dynamic light/dark mode
    properties[colors] = if (isDark) {
        mapOf(
            primary to UnstyledColors.Dark.primary,
            background to UnstyledColors.Dark.background,
            surface to UnstyledColors.Dark.surface,
            error to UnstyledColors.Dark.error,
            onPrimary to UnstyledColors.Dark.onPrimary,
            onBackground to UnstyledColors.Dark.onSurface,
            onSurface to UnstyledColors.Dark.onSurface,
            onError to UnstyledColors.Dark.onError
        )
    } else {
        mapOf(
            primary to UnstyledColors.Light.primary,
            background to UnstyledColors.Light.background,
            surface to UnstyledColors.Light.surface,
            error to UnstyledColors.Light.error,
            onPrimary to UnstyledColors.Light.onPrimary,
            onBackground to UnstyledColors.Light.onSurface,
            onSurface to UnstyledColors.Light.onSurface,
            onError to UnstyledColors.Light.onError
        )
    }
    
    // Custom spacing tokens (from UnstyledTokens → BaseTokens 8dp grid)
    properties[spacing] = mapOf(
        spacingXxxs to UnstyledTokens.spacing.xxxs,
        spacingXxs to UnstyledTokens.spacing.xxs,
        spacingXs to UnstyledTokens.spacing.xs,
        spacingSm to UnstyledTokens.spacing.small,
        spacingMd to UnstyledTokens.spacing.medium,
        spacingLg to UnstyledTokens.spacing.large,
        spacingXl to UnstyledTokens.spacing.xl,
        spacingXxl to UnstyledTokens.spacing.xxl,
        spacingXxxl to UnstyledTokens.spacing.xxxl
    )
    
    // Custom shape tokens (minimal corner radii from UnstyledTokens)
    properties[shapes] = mapOf(
        shapeExtraSmall to UnstyledTokens.shapes.extraSmall,
        shapeSmall to UnstyledTokens.shapes.small,
        shapeMedium to UnstyledTokens.shapes.medium,
        shapeLarge to UnstyledTokens.shapes.large,
        shapeExtraLarge to UnstyledTokens.shapes.extraLarge
    )
    
    // Custom elevation tokens (flat elevation from UnstyledTokens)
    properties[elevation] = mapOf(
        elevationLevel0 to UnstyledTokens.elevation.level0,
        elevationLevel1 to UnstyledTokens.elevation.level1,
        elevationLevel2 to UnstyledTokens.elevation.level2,
        elevationLevel3 to UnstyledTokens.elevation.level3,
        elevationLevel4 to UnstyledTokens.elevation.level4,
        elevationLevel5 to UnstyledTokens.elevation.level5
    )
    
    // Custom motion duration tokens (linear motion from UnstyledTokens)
    properties[motionDuration] = mapOf(
        durationShort to UnstyledTokens.motion.durationShort,
        durationMedium to UnstyledTokens.motion.durationMedium,
        durationLong to UnstyledTokens.motion.durationLong
    )
    
    // Custom motion easing tokens (linear/standard easing from UnstyledTokens)
    properties[motionEasing] = mapOf(
        easingStandard to UnstyledTokens.motion.easingStandard,
        easingEmphasizedDecelerate to UnstyledTokens.motion.easingEmphasizedDecelerate,
        easingEmphasizedAccelerate to UnstyledTokens.motion.easingEmphasizedAccelerate
    )
    
    // Custom typography tokens (platform fonts, minimal styling)
    properties[typography] = mapOf(
        displayLarge to UnstyledTokens.typography.displayLarge,
        displayMedium to UnstyledTokens.typography.displayMedium,
        displaySmall to UnstyledTokens.typography.displaySmall,
        headlineLarge to UnstyledTokens.typography.headlineLarge,
        headlineMedium to UnstyledTokens.typography.headlineMedium,
        headlineSmall to UnstyledTokens.typography.headlineSmall,
        titleLarge to UnstyledTokens.typography.titleLarge,
        titleMedium to UnstyledTokens.typography.titleMedium,
        titleSmall to UnstyledTokens.typography.titleSmall,
        bodyLarge to UnstyledTokens.typography.bodyLarge,
        bodyMedium to UnstyledTokens.typography.bodyMedium,
        bodySmall to UnstyledTokens.typography.bodySmall,
        labelLarge to UnstyledTokens.typography.labelLarge,
        labelMedium to UnstyledTokens.typography.labelMedium,
        labelSmall to UnstyledTokens.typography.labelSmall
    )
}
