package com.minddistrict.multiplatformpoc.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Light theme colors - Material 3 Expressive with Pokémon vibes
private val PrimaryLight = Color(0xFFFF5E57) // Coral
private val OnPrimaryLight = Color(0xFFFFFFFF)
private val PrimaryContainerLight = Color(0xFFFFDAD6)
private val OnPrimaryContainerLight = Color(0xFF410002)

private val SecondaryLight = Color(0xFFFFCA3A) // Yellow
private val OnSecondaryLight = Color(0xFF000000)
private val SecondaryContainerLight = Color(0xFFFFEDB3)
private val OnSecondaryContainerLight = Color(0xFF261900)

private val TertiaryLight = Color(0xFF78C850) // Grass green
private val OnTertiaryLight = Color(0xFFFFFFFF)
private val TertiaryContainerLight = Color(0xFFC8E8A8)
private val OnTertiaryContainerLight = Color(0xFF1A3300)

private val ErrorLight = Color(0xFFBA1A1A)
private val OnErrorLight = Color(0xFFFFFFFF)
private val ErrorContainerLight = Color(0xFFFFDAD6)
private val OnErrorContainerLight = Color(0xFF410002)

private val BackgroundLight = Color(0xFFFFFBF0) // Warm white
private val OnBackgroundLight = Color(0xFF1A1A1A) // Near black
private val SurfaceLight = Color(0xFFFFFBF0)
private val OnSurfaceLight = Color(0xFF1A1A1A)
private val SurfaceVariantLight = Color(0xFFF4EFE5)
private val OnSurfaceVariantLight = Color(0xFF4A4541)

private val OutlineLight = Color(0xFF7B7571)
private val OutlineVariantLight = Color(0xFFCDC8C0)

private val SurfaceTint = PrimaryLight

// Dark theme colors - Material 3 Expressive with adjusted contrast
private val PrimaryDark = Color(0xFFFF9A5A) // Lighter coral for dark bg
private val OnPrimaryDark = Color(0xFF000000)
private val PrimaryContainerDark = Color(0xFF8C2F00)
private val OnPrimaryContainerDark = Color(0xFFFFDAD6)

private val SecondaryDark = Color(0xFFFFE066) // Brighter yellow
private val OnSecondaryDark = Color(0xFF000000)
private val SecondaryContainerDark = Color(0xFF5E4400)
private val OnSecondaryContainerDark = Color(0xFFFFEDB3)

private val TertiaryDark = Color(0xFF98E070) // Brighter grass green
private val OnTertiaryDark = Color(0xFF000000)
private val TertiaryContainerDark = Color(0xFF2D5000)
private val OnTertiaryContainerDark = Color(0xFFC8E8A8)

private val ErrorDark = Color(0xFFFFB4AB)
private val OnErrorDark = Color(0xFF690005)
private val ErrorContainerDark = Color(0xFF93000A)
private val OnErrorContainerDark = Color(0xFFFFDAD6)

private val BackgroundDark = Color(0xFF1A1A1A) // Near black
private val OnBackgroundDark = Color(0xFFF5F5F5) // Off white
private val SurfaceDark = Color(0xFF1A1A1A)
private val OnSurfaceDark = Color(0xFFF5F5F5)
private val SurfaceVariantDark = Color(0xFF2C2C2C)
private val OnSurfaceVariantDark = Color(0xFFD0D0D0)

private val OutlineDark = Color(0xFF959595)
private val OutlineVariantDark = Color(0xFF4A4541)

private val SurfaceTintDark = PrimaryDark

val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    surfaceTint = SurfaceTint
)

val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    surfaceTint = SurfaceTintDark
)
