# Material Icons Strategy

**Last Updated:** December 30, 2025  
**Status:** Active - Migrating to Vector Drawable XML

## Overview

This document defines the app's icon strategy following Material 3 v1.4.0's deprecation of the `material-icons-extended` library.

## Material 3 v1.4.0 Icon Library Changes

**Official Announcement (September 24, 2025):**

> The androidx.compose.material.icons library is no longer recommended for displaying Material Icons in Compose, as Material Symbols are the new way forward. We have stopped publishing updates to this library and it has been removed from the latest Material 3 library release.
>
> Instead, we recommend downloading a Vector Drawable XML file from the Android tab of https://fonts.google.com/icons to get access to the latest styled icons: Material Symbols.
>
> **Why?** The icons library ("Material Icons") have been superseded by the newer look of Material Symbols and we've seen that the library can increase the build time of your apps significantly as it includes all the various icons that may not be needed.

**Source:** [Compose Material 3 Release Notes](https://developer.android.com/jetpack/androidx/releases/compose-material3#1.4.0)

## App Icon Strategy

### Style Choice: Rounded Filled

**Family:** Material Symbols - Rounded  
**Weight:** Filled (default)  
**Rationale:** Provides friendly, approachable aesthetic with solid filled shapes matching Pokémon brand personality and modern Material 3 design.

### Icon Source

**Primary:** [Google Fonts Material Symbols](https://fonts.google.com/icons)
- Download Vector Drawable XML from "Android" tab
- Select "Rounded" family + "Filled" weight (default)
- Save to `composeApp/src/commonMain/composeResources/drawable/`

**Alternative:** [Composables.com Material Symbols](https://composables.com/icons/icon-libraries/material-symbols)
- Browse searchable icon catalog (select Rounded Filled style)
- Copy Vector Drawable XML code
- Save with `ic_` prefix (e.g., `ic_arrow_back.xml`)

## Icon Inventory

### Critical Icons (Implemented - Step 5)

| Icon Name | File Name | Usage | Size | Tint Token | Status |
|-----------|-----------|-------|------|------------|--------|
| arrow_back | ic_arrow_back.xml | Back navigation button | 24dp | onSurface | ✅ Implemented |
| error_outline | ic_error_outline.xml | Error state indicator | 64dp | error | ✅ Implemented |
| refresh | ic_refresh.xml | Retry button (optional) | 24dp | primary | ✅ Implemented |
| settings | ic_settings.xml | Material theme nav item | 24dp | onSurfaceVariant | ✅ Implemented |
| info | ic_info.xml | Unstyled theme nav item | 24dp | onSurfaceVariant | ✅ Implemented |

### Deferred Icons (Step 6 - UI Redesign)

| Icon Name | File Name | Current Workaround | Usage | Size | Tint Token | Status |
|-----------|-----------|-------------------|-------|------|------------|--------|
| straighten or height | ic_straighten.xml | 📏 emoji | Height indicator in stat card | 48dp | onSurfaceVariant | ⏳ Deferred |
| balance | ic_balance.xml | ⚖️ emoji | Weight indicator in stat card | 48dp | onSurfaceVariant | ⏳ Deferred |
| star | ic_star.xml | ⭐ emoji | Base XP indicator in stat card | 48dp | onSurfaceVariant | ⏳ Deferred |

## Implementation Pattern

### Vector Drawable XML Structure

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:autoMirrored="true">  <!-- RTL support for directional icons -->
    <path
        android:fillColor="@android:color/white"
        android:pathData="M20,11H7.83l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13H20v-2z"/>
</vector>
```

**Key Attributes:**
- `android:autoMirrored="true"` - Mirrors icon in RTL layouts (arrow_back, navigation icons)
- `android:fillColor="@android:color/white"` - Default color (overridden by tint parameter)
- `android:viewportWidth/Height` - Typically 24x24 for Material Symbols

### Compose Usage Pattern

```kotlin
import androidx.compose.material3.Icon
import org.jetbrains.compose.resources.painterResource
import pokedex.composeapp.generated.resources.Res
import pokedex.composeapp.generated.resources.ic_arrow_back

@Composable
fun MyComponent() {
    Icon(
        painter = painterResource(Res.drawable.ic_arrow_back),
        contentDescription = "Back",
        tint = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.size(24.dp)
    )
}
```

**Theming Pattern:**
- Navigation icons: `tint = MaterialTheme.colorScheme.onSurface`
- Error icons: `tint = MaterialTheme.colorScheme.error`
- Interactive icons: `tint = MaterialTheme.colorScheme.primary`
- Secondary icons: `tint = MaterialTheme.colorScheme.onSurfaceVariant`

### Accessibility Requirements

All icons MUST include descriptive `contentDescription`:

```kotlin
// ✅ GOOD: Descriptive for screen readers
Icon(..., contentDescription = "Navigate back")
Icon(..., contentDescription = "Error occurred")
Icon(..., contentDescription = "Retry loading")

// ❌ BAD: Generic or missing
Icon(..., contentDescription = "Icon")
Icon(..., contentDescription = null)
```

## Migration Phases

### Phase 1: Critical Icons (Complete - December 30, 2025) ✅

**Scope:** Navigation, error states, theme selection  
**Icons:** arrow_back, error_outline, refresh, settings, info  
**Locations:**
- PokemonDetailMaterialScreen.kt (back button)
- ErrorState.kt (Material + Unstyled variants)
- App.kt (navigation bar)

**Status:** ✅ All critical icons replaced with Vector Drawable XML

### Phase 2: Stat Card Icons (Deferred to Step 6 UI Redesign) ⏳

**Scope:** Physical attributes display  
**Icons:** straighten/height, balance, star_outline  
**Locations:**
- PhysicalAttributesCard.kt (Material variant)
- PokemonDetailScreenUnstyled.kt (Unstyled variant)

**Current Workaround:** Emoji icons (📏⚖️⭐)  
**Reason for Deferral:** UI/UX Design agent will handle holistic screen redesign with proper icon integration  
**Estimated:** Step 6 implementation (post-January 2026)

## Testing Checklist

### Visual Consistency
- [ ] All icons use Rounded Filled style
- [ ] Icon sizes appropriate for context (24dp standard, 48-64dp emphasis)
- [ ] Icons align with text baselines correctly

### Theming
- [ ] Icons tint correctly in light mode
- [ ] Icons tint correctly in dark mode
- [ ] Icon tints use proper Material 3 color tokens

### Accessibility
- [ ] All icons have descriptive contentDescription
- [ ] Icons visible at minimum WCAG AA contrast (3:1 for graphics)
- [ ] Icons scale properly with system font size settings

### RTL Support
- [ ] Directional icons (arrow_back) mirror in RTL locales
- [ ] Test with Arabic/Hebrew language settings
- [ ] Non-directional icons remain unchanged

## References

- [Material 3 Release Notes v1.4.0](https://developer.android.com/jetpack/androidx/releases/compose-material3#1.4.0)
- [Material Symbols (Google Fonts)](https://fonts.google.com/icons)
- [Material Symbols (Composables.com)](https://composables.com/icons/icon-libraries/material-symbols)
- [Compose Resources Documentation](https://developer.android.com/develop/ui/compose/graphics/images/material)
- [WCAG 2.1 Non-Text Contrast](https://www.w3.org/WAI/WCAG21/Understanding/non-text-contrast.html)

## Future Considerations

### Performance Monitoring
- Monitor app build times with Vector Drawable XMLs vs previous approach
- Target: Sub-60 second clean builds on CI
- Metric: Icon loading performance in Compose recomposition

### Icon Library Expansion
When adding new icons:
1. Search [fonts.google.com/icons](https://fonts.google.com/icons) first
2. Select Rounded + Filled style (default weight)
3. Download Vector Drawable XML
4. Save to `composeApp/src/commonMain/composeResources/drawable/ic_*.xml`
5. Use `painterResource(Res.drawable.ic_*)` in Compose
6. Add `contentDescription` and `tint` parameters

### Alternative Icon Sources
If Material Symbols don't provide needed icon:
1. Check [Phosphor Icons](https://phosphoricons.com/) for alternatives
2. Design custom icon matching Rounded Filled style
3. Export as Vector Drawable XML with 24x24 viewBox
4. Follow same naming and theming patterns
