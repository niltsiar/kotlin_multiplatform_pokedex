# Design Token Architecture

**Last Updated:** December 30, 2025  
**Status:** Complete - CompositionLocal pattern for both design + component tokens

## Architecture Overview

Two-layer token system using CompositionLocal pattern:

1. **Design Tokens** - Foundation (spacing, shapes, elevation, motion)
2. **Component Tokens** - Component-specific styling (cards, badges, progress bars)

## Design Token Pattern

```kotlin
MaterialTheme.tokens → LocalMaterialTokens → MaterialDesignTokens → DefaultMaterialTokens
```

**Implementation:**
- Interface: `MaterialDesignTokens` ([MaterialTokens.kt](../../core/designsystem-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/core/designsystem/material/tokens/MaterialTokens.kt))
- CompositionLocal: `LocalMaterialTokens`
- Default: `DefaultMaterialTokens` (internal class)
- Extension: `MaterialTheme.tokens` ([MaterialThemeTokens.kt](../../core/designsystem-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/core/designsystem/material/tokens/MaterialThemeTokens.kt))

**Access:**
```kotlin
MaterialTheme.tokens.spacing.medium   // 16.dp
MaterialTheme.tokens.shapes.large     // 24.dp corner
MaterialTheme.tokens.motion.durationMedium  // 300ms
```

## Component Token Pattern

```kotlin
MaterialTheme.componentTokens → LocalMaterialComponentTokens → MaterialComponentTokens → DefaultMaterialComponentTokens
```

**Implementation:**
- Interface: `MaterialComponentTokens` ([MaterialComponentTokens.kt](../../core/designsystem-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/core/designsystem/material/tokens/MaterialComponentTokens.kt))
- CompositionLocal: `LocalMaterialComponentTokens`
- Default: `DefaultMaterialComponentTokens` (internal class)
- Extension: `MaterialTheme.componentTokens` ([MaterialThemeTokens.kt](../../core/designsystem-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/core/designsystem/material/tokens/MaterialThemeTokens.kt))

**Access:**
```kotlin
Card(tokens = MaterialTheme.componentTokens.card())
TypeBadge(tokens = MaterialTheme.componentTokens.badge())
AnimatedStatBar(tokens = MaterialTheme.componentTokens.progressBar())
```

## Theme Integration

Both token systems provided in PokemonTheme ([Theme.kt](../../core/designsystem-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/core/designsystem/material/theme/Theme.kt)):

```kotlin
@Composable
fun PokemonTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalMaterialTokens provides DefaultMaterialTokens(),
        LocalMaterialComponentTokens provides DefaultMaterialComponentTokens()
    ) {
        MaterialTheme(content = content)
    }
}
```

## Critical Pattern: LaunchedEffect Access

**Problem:** `MaterialTheme.tokens` requires `@Composable` context, but `LaunchedEffect` lambda is suspend.

**Solution:** Capture tokens BEFORE LaunchedEffect:

```kotlin
// ✅ CORRECT
val motionTokens = MaterialTheme.tokens.motion
LaunchedEffect(Unit) {
    animateTo(animationSpec = tween(
        durationMillis = motionTokens.durationMedium,
        easing = motionTokens.easingEmphasizedDecelerate
    ))
}

// ❌ WRONG
LaunchedEffect(Unit) {
    animateTo(animationSpec = tween(
        durationMillis = MaterialTheme.tokens.motion.durationMedium  // Error!
    ))
}
```

**Examples:** [PokemonListGrid.kt](../../features/pokemonlist/ui-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/ui/material/components/PokemonListGrid.kt#L83), [TypeBadgeRow.kt](../../features/pokemondetail/ui-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/ui/material/components/TypeBadgeRow.kt#L52), [BaseStatsSection.kt](../../features/pokemondetail/ui-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/ui/material/components/BaseStatsSection.kt#L96)

## Customization Example

Full guide: [component_token_customization_example.md](component_token_customization_example.md)

**Quick example:**
```kotlin
class MyCustomTokens : MaterialComponentTokens {
    override val card = { object : CardTokens {
        override val shape = RoundedCornerShape(16.dp)  // Custom
        override val elevation = 1.dp
    } }
}

@Composable
fun MyTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalMaterialComponentTokens provides MyCustomTokens()
    ) { MaterialTheme(content = content) }
}
```

## Token Categories

**Design Tokens** (MaterialTheme.tokens):
- `spacing` - 8dp grid (xxs: 2dp → xxxl: 64dp)
- `shapes` - Corner radii (small: 8dp → extraLarge: 28dp)
- `elevation` - Shadow depths (level1: 1dp → level5: 8dp)
- `motion` - Durations (short: 200ms, medium: 300ms, long: 400ms) + easing curves

**Component Tokens** (MaterialTheme.componentTokens):
- `card()` - Shape, elevation, colors, pressedScale
- `badge()` - Shape, borderWidth, fillAlpha, textColor
- `progressBar()` - Height, shape, colors, animationSpec

## Benefits

✅ **Customizable** - Override tokens without forking components  
✅ **Type-safe** - Interface contracts enforce completeness  
✅ **Theme-scoped** - Different themes for different app sections  
✅ **Consistent** - Unified pattern for all tokens  
✅ **Future-proof** - Easy to add new token categories

## See Also

- [critical_patterns_quick_ref.md](critical_patterns_quick_ref.md) - Core architectural patterns
- [component_token_customization_example.md](component_token_customization_example.md) - Detailed customization guide
- [conventions.md](conventions.md) - Complete project architecture
