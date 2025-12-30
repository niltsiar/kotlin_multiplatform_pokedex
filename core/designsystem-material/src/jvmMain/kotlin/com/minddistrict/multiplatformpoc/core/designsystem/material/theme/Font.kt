package com.minddistrict.multiplatformpoc.core.designsystem.material.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import multiplatformpoc.core.designsystem_material.generated.resources.GoogleSans_Bold
import multiplatformpoc.core.designsystem_material.generated.resources.GoogleSans_Medium
import multiplatformpoc.core.designsystem_material.generated.resources.GoogleSans_Regular
import multiplatformpoc.core.designsystem_material.generated.resources.Res

/**
 * Desktop (JVM) implementation using Google Sans font family.
 * Loads font files from composeResources/font/ using org.jetbrains.compose.resources.Font.
 */
@Composable
actual fun pokemonFontFamily(): FontFamily = FontFamily(
    Font(Res.font.GoogleSans_Regular, FontWeight.Normal),
    Font(Res.font.GoogleSans_Medium, FontWeight.Medium),
    Font(Res.font.GoogleSans_Bold, FontWeight.Bold)
)
