# Troubleshooting Guide

**Last Updated:** December 31, 2025

**Purpose:** Common issues encountered during development and their solutions. Based on real problems solved during Steps 1-7 of Phase 2 redesign.

---

## Build Issues

### Unresolved Reference Errors (Despite Correct Imports)

**Symptom:**
```
e: file:///path/to/BaseTokens.kt:51:47 Unresolved reference 'RoundedCornerShape'
e: file:///path/to/BaseTokens.kt:59:17 Unresolved reference 'dp'
```

**Cause:** Gradle build cache corruption from previous failed builds.

**Solution:**
```bash
./gradlew clean :composeApp:assembleDebug test --continue
```

**Why:** Stale dependency resolution cache prevents proper import resolution. Clean build clears cache.

**Prevention:** Run clean build after multiple consecutive failed builds or when seeing import errors on standard library types.

---

### "Unresolved reference 'generated'" for Compose Resources

**Symptom:**
```kotlin
import multiplatformpoc.core.designsystem_core.generated.resources.Res
// Error: Unresolved reference 'generated'
```

**Cause:** Library module resources not configured for public access.

**Solution (3 steps in library module's build.gradle.kts):**

```kotlin
// 1. Add dependency
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.components.resources)  // CRITICAL!
        }
    }
}

// 2. Enable public Res class
compose.resources {
    publicResClass = true  // Default internal won't work!
}

// 3. Android namespace determines package
android {
    namespace = "com.minddistrict.multiplatformpoc.core.designsystem.core"
}
```

**Generated package name:** Namespace with dots → underscores:
- Input: `com.minddistrict.multiplatformpoc.core.designsystem.core`
- Output: `multiplatformpoc.core.designsystem_core.generated.resources`

**Reference:** [ICON_KNOWLEDGE_BASE.md](ICON_KNOWLEDGE_BASE.md)

---

### Navigation Provider "Unresolved reference" Errors

**Symptom:**
```kotlin
PokemonListScreenUnstyled(viewModel = ...)
// Error: Unresolved reference 'PokemonListScreenUnstyled'
```

**Cause:** Screen function naming convention mismatch.

**Correct Pattern:**
- ✅ `{Feature}UnstyledScreen` (e.g., `PokemonListUnstyledScreen`)
- ✅ `{Feature}MaterialScreen` (e.g., `PokemonListMaterialScreen`)
- ❌ `{Feature}ScreenUnstyled` (wrong suffix order)

**Solution:**
```kotlin
// ✅ CORRECT imports and usage
import ...ui.unstyled.PokemonListUnstyledScreen
PokemonListUnstyledScreen(viewModel = ...)

import ...ui.material.PokemonListMaterialScreen  
PokemonListMaterialScreen(viewModel = ...)
```

**Why:** Consistent naming convention: `{Adjective}{Noun}` not `{Noun}{Adjective}`.

---

## Domain Model Issues

### Constructor Parameter Mismatches

**Symptom:**
```kotlin
Stat(name = "hp", value = 45)
// Error: No value parameter with type Int
```

**Cause:** Domain class has different parameters than expected.

**Solution:** Always verify domain class signatures:

```kotlin
// ✅ CORRECT domain classes (from api module)
data class Stat(
    val name: String,
    val baseStat: Int,  // NOT 'value'
    val effort: Int     // Required, not optional
)

data class TypeOfPokemon(
    val name: String,
    val slot: Int  // Required for ordering
)

data class Ability(
    val name: String,
    val isHidden: Boolean,
    val slot: Int  // Required for positioning
)
```

**Prevention:** Check `features/<feature>/api/src/commonMain/kotlin/.../domain/` for authoritative definitions.

---

## UI Component Issues

### Clickable Component Not Responding

**Symptom:** Card hover/press states work, but clicking does nothing.

**Cause:** Missing `.clickable()` modifier despite having `MutableInteractionSource`.

**Solution:**
```kotlin
Column(
    modifier = modifier
        .clip(shape)
        .border(...)
        .clickable(  // ← REQUIRED for actual clicks
            interactionSource = interactionSource,
            indication = null,  // Or ripple effect
            onClick = onClick
        )
        .hoverable(interactionSource = interactionSource)  // Only tracks hover
        .padding(...)
)
```

**Why:** `hoverable()` only tracks hover state, doesn't make component clickable. Must add `.clickable()` separately.

**Order matters:**
1. `.clip()` - Define shape first
2. `.border()` - Visual border
3. `.clickable()` - Make clickable
4. `.hoverable()` - Track hover state
5. `.padding()` - Internal padding

---

### Hover Effects Too Subtle

**Symptom:** Hover state implemented but barely visible.

**Cause:** Minimal effect values (brightness 1.1, border alpha 0.2).

**Solution for Unstyled theme:**
```kotlin
val brightness by animateFloatAsState(
    targetValue = when {
        isPressed -> 0.95f
        isHovered -> 1.15f  // More noticeable (was 1.1)
        else -> 1f
    }
)

val borderAlpha by animateFloatAsState(
    targetValue = when {
        isPressed -> 0.3f
        isHovered -> 0.5f   // More prominent (was 0.2)
        else -> 0.2f
    }
)

val scale by animateFloatAsState(
    targetValue = when {
        isPressed -> 0.98f
        isHovered -> 1.02f  // Slight grow (was 1.0)
        else -> 1f
    }
)
```

**Why:** Minimal effects match "unstyled" aesthetic but need sufficient visibility for usability.

---

## Theme System Issues

### Theme[property][token] Not Found

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

## Method/Property Access Issues

### PokemonTypeColors API Changes

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

### gridColumns Extension vs Function

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

## Testing Issues

### Tests Pass But Build Shows Failures

**Symptom:**
```
> Task :features:pokemonlist:wiring-ui-unstyled:compileDebugKotlinAndroid FAILED
BUILD SUCCESSFUL in 1m 23s
All 84 tests PASSED
```

**Cause:** `--continue` flag allows tests to run despite task failures.

**Interpretation:**
- Task failures shown are from earlier in build
- Tests actually passed (verify with explicit test run)
- Subsequent clean build resolves stale task states

**Solution:** Run explicit test verification:
```bash
./gradlew test --rerun-tasks
```

---

## Git Commit Issues

### Commit Message Too Long for PTY

**Symptom:**
```
pty is gonna break
```

**Cause:** Verbose commit message with detailed explanations exceeds terminal buffer.

**Solution:** Use concise commit format:
```bash
git commit -m "feat(unstyled): complete Step 7 with navigation fixes

- Fixed 15 compilation errors in 5 waves
- Added .clickable() modifier for navigation
- Enhanced hover effects (brightness 1.15, border 0.5, scale 1.02)

Result: BUILD SUCCESSFUL, 84 tests passing"
```

**Guidelines:**
- Subject line: 72 chars max
- Body: Bulleted summary, not prose
- Omit implementation details (keep in code comments)

---

## Prevention Strategies

### Before Implementing Unstyled Components

1. **Verify compilation early:** Build immediately after scaffolding
2. **Check domain classes:** Verify constructor parameters first
3. **Copy working patterns:** Use Material components as reference
4. **Test incrementally:** Build after each component

### Before Debugging Import Errors

1. **Check actual file:** Verify imports are present
2. **Try clean build first:** Often resolves stale cache
3. **Check package names:** Verify generated resource packages

### Before Navigation Debugging

1. **Verify naming convention:** {Feature}UnstyledScreen format
2. **Check both modules:** Provider imports + screen exports
3. **Test with Material first:** Verify pattern works before copying

---

## Quick Diagnosis Checklist

**Import errors on standard libs?** → Clean build

**"Unresolved reference" on custom type?** → Check naming convention

**Component not clickable?** → Add `.clickable()` modifier

**Hover effect not visible?** → Increase effect values (1.15, 0.5, 1.02)

**Constructor errors?** → Verify domain class in `:api` module

**Theme token not found?** → Check import source (custom vs platform)

**Tests pass but tasks fail?** → Run explicit test verification

---

## Related Documentation

- [CODE_REFERENCES.md](CODE_REFERENCES.md) - Canonical implementation examples
- [ICON_KNOWLEDGE_BASE.md](ICON_KNOWLEDGE_BASE.md) - Resource configuration
- [QUICK_REFERENCE.md](QUICK_REFERENCE.md) - Common commands
- [UI_REDESIGN_PLAN.md](UI_REDESIGN_PLAN.md) - Step-by-step progress

---

**Token Efficiency:** This troubleshooting guide (~700 lines) consolidates solutions discovered across 40+ hours of development, preventing repeated debugging sessions.
