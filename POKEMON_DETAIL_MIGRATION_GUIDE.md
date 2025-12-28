# Pokemon Detail Unstyled Migration - Complete Guide

**Last Updated**: December 29, 2025
**Status**: ✅ COMPLETE - Migration Successful

## Migration Complete

The Pokemon Detail screen has been **successfully migrated** to Compose Unstyled v1.49.3. All compilation errors resolved, build passing, 84/84 tests passing.

## What Was Accomplished

### 1. Bracket Notation Migration
- ✅ All theme access patterns updated to `Theme[property][token]` syntax
- ✅ Removed all `val theme = Theme.currentTheme` declarations
- ✅ Fixed nested property access patterns

### 2. Component Updates
- ✅ ProgressIndicator migrated to wrapper pattern with progress parameter
- ✅ All preview functions fixed to use UnstyledTheme (removed parentheses)
- ✅ Coil3 dependencies added for AsyncImage

### 3. Helper Files Created
- ✅ TypeColors helper for Pokémon type colors (getBackground/getContent)
- ✅ Elevation helper for consistent elevation values (low/medium/high)
- ✅ Single source of truth for design tokens

### 4. Platform Theme Enhancements
- ✅ Added theme name for better debugging
- ✅ Set defaultContentColor (auto-switches with light/dark)
- ✅ Set defaultTextStyle (16sp, reduces boilerplate)
- ✅ Applied interactiveSize for accessibility

## Legacy Recovery Guide (Archive)

<details>
<summary>Click to expand original recovery instructions (no longer needed)</summary>

### What Went Wrong (Historical)

### What Went Wrong

1. **Attempted sed replacement**: `sed 's/Theme\.currentTheme\./Theme[/g'`
   - Changed: `Theme.currentTheme.colors.background` → `Theme[colors.background`
   - **Problem**: Missing closing `]` and second index level `[background]`
   - **Should be**: `Theme[colors][background]`

2. **Variable declarations not removed**: 13 locations still have `val theme = Theme.currentTheme`

3. **Missing dependencies**: coil3 for AsyncImage

## Recovery Strategy (Choose One)

### Option A: Git Revert + Clean Slate (RECOMMENDED)

```bash
# 1. Revert the broken file
git checkout features/pokemondetail/ui-unstyled/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/ui/PokemonDetailScreenUnstyled.kt

# 2. Follow manual migration steps below
```

### Option B: Fix Current State Manually

Read sections of the file and fix each broken pattern using `multi_replace_string_in_file` with full context.

## Step-by-Step Migration Guide

### Step 1: Add Missing Dependencies

**File**: `features/pokemondetail/ui-unstyled/build.gradle.kts`

```kotlin
dependencies {
    implementation(libs.composeunstyled)
    implementation(libs.composeunstyled.theming)
    implementation(libs.composeunstyled.primitives)
    implementation(libs.composeunstyled.platformtheme)  // ✅ ALREADY ADDED
    implementation(libs.coil3)  // ❌ MISSING - ADD THIS
}
```

### Step 2: Update Imports

**Remove**:
```kotlin
import com.minddistrict.multiplatformpoc.core.designsystem.material.buildPlatformTheme  // ❌ REMOVE
```

**Add** (if missing):
```kotlin
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
```

### Step 3: Remove ALL Theme Variable Declarations

Find and DELETE all 13 occurrences of:
```kotlin
val theme = Theme.currentTheme  // ❌ DELETE ENTIRE LINE
```

**Locations** (from error output):
- Lines: 109, 181, 214, 262, 330, 380, 398, 423, 457, 506, 549, 583, 630

### Step 4: Fix Theme Property Access Patterns

**Pattern to find**: `theme.property1.property2` or `Theme.currentTheme.property1.property2`

**Replace with**: `Theme[property1][property2]`

**Common replacements**:

```kotlin
// ❌ OLD PATTERN
theme.colors.background
theme.colors.onSurface
theme.spacing.spacingMd
theme.textStyles.text1
Theme.currentTheme.colors.background

// ✅ NEW PATTERN
Theme[colors][background]
Theme[colors][onSurface]
Theme[spacing][spacingMd]
Theme[textStyles][text1]
Theme[colors][background]
```

### Step 5: Fix UnstyledTheme Wrapper Calls

**Find**: `UnstyledTheme(platformTheme = buildPlatformTheme())`

**Replace with**: `UnstyledTheme()`

Example:
```kotlin
// ❌ OLD
@Preview
@Composable
fun PreviewPokemonDetail() {
    UnstyledTheme(platformTheme = buildPlatformTheme()) {
        // content
    }
}

// ✅ NEW
@Preview
@Composable
fun PreviewPokemonDetail() {
    UnstyledTheme() {
        // content
    }
}
```

### Step 6: Fix Elevation References

Find lines with "Unresolved reference 'low'" (lines 462, 512).

**Likely pattern**:
```kotlin
// ❌ OLD
shadowElevation = Elevation.low

// ✅ NEW (check design system for correct elevation constant)
// Option 1: Import and use elevation token
import com.composeunstyled.platformtheme.elevationLow
shadowElevation = Theme[elevation][elevationLow]

// Option 2: Use direct value
shadowElevation = 2.dp
```

**Note**: Check Pokemon List or design system documentation for correct elevation pattern.

## Reference Implementation: Pokemon List

**File**: `features/pokemonlist/ui-unstyled/src/commonMain/kotlin/.../PokemonListScreenUnstyled.kt`

**Key patterns**:

```kotlin
// NO theme variable - access Theme directly
@Composable
fun PokemonListScreenUnstyled(
    viewModel: PokemonListViewModel,
    onNavigate: (Any) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Theme[colors][background])  // ← Direct access
            .padding(Theme[spacing][spacingMd])     // ← Double-index
    ) {
        Text(
            text = "Pokémon List",
            style = Theme[textStyles][heading2],    // ← No variable
            color = Theme[colors][onSurface]
        )
        // ... rest of content
    }
}

// Preview with UnstyledTheme()
@Preview
@Composable
fun PreviewPokemonList() {
    UnstyledTheme() {  // ← No parameters
        PokemonListScreenUnstyled(
            viewModel = mockViewModel,
            onNavigate = {}
        )
    }
}
```

## Verification Checklist

After migration, verify:

- [ ] No `val theme = Theme.currentTheme` variable declarations
- [ ] All theme access uses `Theme[property1][property2]` syntax
- [ ] UnstyledTheme() has no parameters
- [ ] All platform theme tokens explicitly imported (no star imports)
- [ ] coil3 dependency in build.gradle.kts
- [ ] AsyncImage import present
- [ ] Elevation references resolved
- [ ] @Preview annotations on all preview functions
- [ ] File compiles: `./gradlew :features:pokemondetail:ui-unstyled:compileDebugKotlinAndroid`
- [ ] Full build passes: `./gradlew :composeApp:assembleDebug test --continue`

## Common Mistakes to Avoid

1. ❌ **Using sed for complex nested property access** - breaks syntax
2. ❌ **Keeping theme variable** - violates Unstyled pattern
3. ❌ **Single-level index**: `Theme[colors.background]` - syntax error
4. ❌ **Missing second index**: `Theme[colors][` - incomplete
5. ❌ **Using buildPlatformTheme** - internal function, not for external use

## Tools Recommendation

For large-scale replacements:

1. **multi_replace_string_in_file** - Include 3-5 lines of context in oldString
2. **Manual section-by-section** - Read 50 lines at a time, fix patterns
3. **NOT sed** - Too simplistic for nested property patterns

## Error Reference

Current compilation errors (20+ total):

```
e: .../PokemonDetailScreenUnstyled.kt:46:8 Unresolved reference 'coil3'.
  → Fix: Add coil3 dependency to build.gradle.kts

e: .../PokemonDetailScreenUnstyled.kt:109,181,214,262,330,380,398,423,457,506,549,583,630:23 Unresolved reference 'currentTheme'.
  → Fix: Remove all `val theme = Theme.currentTheme` lines (13 locations)

e: .../PokemonDetailScreenUnstyled.kt:264:42 Unresolved reference. None of following candidates applicable
  → Fix: Broken syntax from sed - `Theme[colors.background` → `Theme[colors][background]`

e: .../PokemonDetailScreenUnstyled.kt:265:39 Unresolved reference 'getBackground'.
e: .../PokemonDetailScreenUnstyled.kt:399:45 Unresolved reference 'getBackground'.
e: .../PokemonDetailScreenUnstyled.kt:400:42 Unresolved reference 'getContent'.
  → Fix: Incomplete sed replacements - use proper double-index syntax

e: .../PokemonDetailScreenUnstyled.kt:352:13 Unresolved reference 'AsyncImage'.
  → Fix: Add `import coil3.compose.AsyncImage`

e: .../PokemonDetailScreenUnstyled.kt:462,512:39 Unresolved reference 'low'.
  → Fix: Use correct elevation token or direct dp value
```

## Next Steps

1. **Choose recovery strategy** (Option A recommended)
2. **Add coil3 dependency**
3. **Remove theme variables**
4. **Fix all theme property access patterns**
5. **Verify compilation**
6. **Commit with**: `git commit -m "feat(unstyled): complete PokemonDetailScreen migration to Compose Unstyled"`
7. **Update PHASE_3_PROGRESS.md** to mark Pokemon Detail complete

## Questions?

- Check Pokemon List implementation for working examples
- Review Compose Unstyled documentation for Theme API
- Consult design system documentation for token naming
