# Troubleshooting: Design System Issues

Common theme token, color API, and resource access problems.

---

## Theme[property][token] Not Found

**Symptom:**
```kotlin
Theme[shapes][shapeLarge]
// Error: Unresolved reference
```

**Cause:** Wrong imports from platform theme instead of custom theme.

**Solution:**
```kotlin
// ❌ WRONG
import com.composeunstyled.platformtheme.shapes

// ✅ CORRECT
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.shapes
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.shapeLarge
```

**Supported properties in Unstyled:**
- `Theme[spacing][spacingSm/Md/Lg/Xl/...]`
- `Theme[shapes][shapeLarge/Medium/Small]`
- `Theme[typography][labelMedium/bodyLarge/...]`
- `Theme[colors][primary/onSurface/background/...]`
- `Theme[elevation][elevationLevel1/2/3]`
- `Theme[motionDuration][durationShort/Medium/Long]`
- `Theme[motionEasing][easingStandard]`

**Key insight:** Unstyled theme DOES support full `Theme[property][token]` syntax despite minimal aesthetic.

---

## PokemonTypeColors API Changes

**Symptom:**
```kotlin
PokemonTypeColors.getColorForType(type.name)
// Error: Unresolved reference 'getColorForType'
```

**Cause:** API method name changed.

**Solution:**
```kotlin
// ✅ CORRECT
val color = PokemonTypeColors.getBackground(type.name, isDark = false)
```

**Why:** Centralized color system with light/dark mode support.

---

## gridColumns Extension vs Function

**Symptom:**
```kotlin
windowInfo.gridColumns()
// Error: Unresolved reference 'gridColumns'
```

**Cause:** `gridColumns` is a top-level function, not an extension.

**Solution:**
```kotlin
// ✅ CORRECT
import com.minddistrict.multiplatformpoc.core.designsystem.core.gridColumns

val windowInfo = currentWindowAdaptiveInfo()
val columns = gridColumns(windowInfo)  // Function call
```

---

## Before Debugging Import Errors

1. **Check actual file:** Verify imports are present
2. **Try clean build first:** Often resolves stale cache
3. **Check package names:** Verify generated resource packages
