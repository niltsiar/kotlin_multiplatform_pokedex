# Icon Implementation Status

**Date:** December 30, 2025  
**Issue:** Compose Multiplatform resources from library modules  
**Status:** Documentation committed, code implementation pending

## What We Have

### ✅ Completed
1. **Documentation:**
   - `docs/tech/material_icons_strategy.md` - Comprehensive strategy
   - `docs/UI_REDESIGN_PLAN.md` - Updated with Step 6 TODO
   - `docs/QUICK_REFERENCE.md` - Added strategy link
   - `DOWNLOAD_ICONS.md` - Simple download instructions

2. **Icons Created:**
   - `core/designsystem-core/src/commonMain/composeResources/drawable/ic_arrow_back.xml`
   - `core/designsystem-core/src/commonMain/composeResources/drawable/ic_error_outline.xml`
   - `core/designsystem-core/src/commonMain/composeResources/drawable/ic_refresh.xml`
   - `core/designsystem-core/src/commonMain/composeResources/drawable/ic_settings.xml`
   - `core/designsystem-core/src/commonMain/composeResources/drawable/ic_info.xml`

3. **Commit:**
   - Committed: `2532016` - "docs(icons): add Material Icons strategy and Vector Drawable icons"

## Current Challenge

**Problem:** Compose resources in library modules (like `core:designsystem-core`) are NOT automatically exposed to consuming modules.

**Expected usage pattern (doesn't work yet):**
```kotlin
// In PokemonDetailMaterialScreen.kt
Icon(
    painter = painterResource(Res.drawable.ic_arrow_back),
    contentDescription = "Back"
)
```

**Error:** "Unresolved reference 'generated'" because library resources don't generate a `Res` class by default.

## Solution Options

### Option 1: Library Module Resources (SOLUTION FOUND! ✅)
**Approach:** Configure library to properly export Compose resources.

**Implementation:**
1. Add `publicResClass = true` to `core/designsystem-core/build.gradle.kts`:
   ```kotlin
   compose.resources {
       publicResClass = true
   }
   ```

2. Consuming modules already have `compose.components.resources` via transitive dependency from `designsystem-material`/`designsystem-unstyled` (which use `api(projects.core.designsystemCore)`).

3. Import pattern in consuming code:
   ```kotlin
   import com.minddistrict.multiplatformpoc.core.designsystem.core.generated.resources.Res
   import com.minddistrict.multiplatformpoc.core.designsystem.core.generated.resources.ic_arrow_back
   import org.jetbrains.compose.resources.painterResource
   ```

**Reference:** [Compose Multiplatform Resources Documentation](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html#customizing-accessor-class-generation)

> **Key Quote:** "`publicResClass` set to `true` makes the generated `Res` class public. By default, the generated class is internal."

**Pros:**
- Icons centralized in design system (architectural goal) ✅
- Single source of truth ✅
- Reusable across all feature modules ✅
- Matches project architecture patterns ✅

**Cons:**
- Requires one-line configuration (minimal effort)

### Option 2: Move Icons to App Module
**Approach:** Move icons to `composeApp/src/commonMain/composeResources/drawable/`

**Implementation:**
```bash
mv core/designsystem-core/src/commonMain/composeResources/drawable/*.xml \
   composeApp/src/commonMain/composeResources/drawable/
```

**Pros:**
- Simpler - app resources work out of the box
- Immediate solution

**Cons:**
- Icons in app module, not design system (architectural compromise)
- Can't be reused by `:server` or other non-app modules
- Violates "centralized in core" goal

### Option 3: Wrapper Functions in Design System
**Approach:** Create wrapper functions in `designsystem-core` that expose icons.

**Example:**
```kotlin
// In core/designsystem-core
@Composable
fun rememberArrowBackIcon(): Painter = 
    painterResource(Res.drawable.ic_arrow_back)
```

**Pros:**
- Encapsulates resource access
- Type-safe API

**Cons:**
- More boilerplate (one function per icon)
- Still requires library resources to work

## Next Steps

### Step 1: Enable Public Res Class ✅
Add configuration to `core/designsystem-core/build.gradle.kts`:

```kotlin
android {
    namespace = "com.minddistrict.multiplatformpoc.core.designsystem.core"
}

compose.resources {
    publicResClass = true
}
```

### Step 2: Clean Build
```bash
./gradlew clean :core:designsystem-core:build
```

### Step 3: Replace Emoji Icons
Once build succeeds, replace icons in 3 files:

**PokemonDetailMaterialScreen.kt:**
```kotlin
import com.minddistrict.multiplatformpoc.core.designsystem.core.generated.resources.Res
import com.minddistrict.multiplatformpoc.core.designsystem.core.generated.resources.ic_arrow_back
import org.jetbrains.compose.resources.painterResource

// Replace back button
Icon(
    painter = painterResource(Res.drawable.ic_arrow_back),
    contentDescription = "Back",
    tint = MaterialTheme.colorScheme.onSurface
)
```

**ErrorState.kt:**
```kotlin
import com.minddistrict.multiplatformpoc.core.designsystem.core.generated.resources.Res
import com.minddistrict.multiplatformpoc.core.designsystem.core.generated.resources.ic_error_outline
import org.jetbrains.compose.resources.painterResource

// Replace error emoji
Icon(
    painter = painterResource(Res.drawable.ic_error_outline),
    contentDescription = "Error",
    tint = MaterialTheme.colorScheme.error,
    modifier = Modifier.size(64.dp)
)
```

**App.kt:**
```kotlin
import com.minddistrict.multiplatformpoc.core.designsystem.core.generated.resources.Res
import com.minddistrict.multiplatformpoc.core.designsystem.core.generated.resources.ic_settings
import com.minddistrict.multiplatformpoc.core.designsystem.core.generated.resources.ic_info
import org.jetbrains.compose.resources.painterResource

// Replace navigation icons
Icon(painterResource(Res.drawable.ic_settings), contentDescription = "Material")
Icon(painterResource(Res.drawable.ic_info), contentDescription = "Unstyled")
```

### Step 4: Delete Old Icon Files
```bash
rm composeApp/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/icons/Settings.kt
rm composeApp/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/icons/Info.kt
```

### Step 5: Test Build
```bash
./gradlew :composeApp:assembleDebug
```

### Step 6: Verify Rendering
- Run app on Android/Desktop
- Test light/dark mode
- Verify RTL mirroring of back arrow (Arabic/Hebrew locale)

## References

- Compose Multiplatform Resources: https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-images-resources.html
- Material 3 deprecation: https://developer.android.com/jetpack/androidx/releases/compose-material3#1.4.0
- Current icon strategy: `docs/tech/material_icons_strategy.md`
