# Component Library Guide

**Last Updated:** January 4, 2026

**Purpose:** Reference guide for theme-agnostic shared components built on the unified token system.

---

## Overview

The component library provides reusable UI building blocks that work across both Material Design 3 and Unstyled themes through a token-based abstraction layer. Components accept token interfaces, allowing different visual styling without code duplication.

**Key Principles:**
1. **Token-driven:** Components accept token interfaces (`CardTokens`, `BadgeTokens`, `ProgressBarTokens`)
2. **Theme-agnostic:** Same component code works in Material and Unstyled themes
3. **Override-friendly:** Per-instance token overrides for customization
4. **Accessibility-first:** Reduced motion support, WCAG AA contrast ratios

---

## PokemonCard

**Purpose:** Reusable card component for displaying Pokémon in grid layouts with press feedback.

**File:** [PokemonCard.kt](../../core/designsystem-core/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/core/designsystem/core/components/PokemonCard.kt)

### Token Parameters

| Token | Type | Material Value | Unstyled Value |
|-------|------|----------------|----------------|
| `shape` | `Shape` | 28dp corners (expressive) | 12dp corners (minimal) |
| `elevation` | `Dp` | 3dp shadow | 2dp shadow |
| `backgroundColor` | `Color` | Surface color | Surface color |
| `contentColor` | `Color` | OnSurface color | OnSurface color |
| `pressedScale` | `Float` | 0.95f | 0.98f |

### Usage Example

```kotlin
import com.minddistrict.multiplatformpoc.core.designsystem.material.MaterialTheme

@Composable
fun MyScreen() {
    PokemonCard(
        tokens = MaterialTheme.componentTokens.card(),
        onClick = { /* handle click */ }
    ) {
        // Card content
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Pikachu")
            Text("#025")
        }
    }
}
```

### Per-Instance Overrides

```kotlin
// Override shape for a special card
PokemonCard(
    tokens = MaterialTheme.componentTokens.card(),
    overrideShape = RoundedCornerShape(16.dp),  // Custom shape
    onClick = { /* ... */ }
) { /* content */ }

// Override elevation for emphasis
PokemonCard(
    tokens = MaterialTheme.componentTokens.card(),
    overrideElevation = 8.dp,  // Higher shadow
    onClick = { /* ... */ }
) { /* content */ }
```

### Material vs Unstyled Comparison

| Aspect | Material | Unstyled |
|--------|----------|----------|
| **Corner Radius** | 28dp (playful) | 12dp (subtle) |
| **Shadow** | 3dp elevation | 2dp flat shadow |
| **Pressed Scale** | 0.95f (bouncy) | 0.98f (minimal) |
| **Visual Feel** | Elevated, expressive | Flat, minimal |

### Preview References

- Material: [PokemonListMaterialScreen.kt](../../features/pokemonlist/ui-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/ui/material/PokemonListMaterialScreen.kt) — `@Preview` functions with realistic data
- Unstyled: [PokemonListUnstyledScreen.kt](../../features/pokemonlist/ui-unstyled/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/ui/unstyled/PokemonListUnstyledScreen.kt) — Minimal theme previews

---

## TypeBadge

**Purpose:** Display Pokémon type badges with official type colors and theme-specific styling.

**File:** [TypeBadge.kt](../../core/designsystem-core/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/core/designsystem/core/components/TypeBadge.kt)

### Token Parameters

| Token | Type | Material Value | Unstyled Value |
|-------|------|----------------|----------------|
| `shape` | `Shape` | 12dp corners | 8dp corners |
| `borderWidth` | `Dp` | 0dp (no border) | 2dp border |
| `fillAlpha` | `Float` | 1.0f (filled) | 0.0f (outline only) |
| `textColor` | `Color` | White | Type color |

### Color System Integration

**PokemonTypeColors** provides 18 official Pokémon type colors adjusted for WCAG AA accessibility:

```kotlin
object PokemonTypeColors {
    val Fire = Color(0xFFFF4422)
    val Water = Color(0xFF3399FF)
    val Grass = Color(0xFF77CC55)
    val Electric = Color(0xFFFFCC33)
    // ... 14 more types
    
    fun getBackground(type: String, isDark: Boolean): Color
    fun getContent(type: String, isDark: Boolean): Color  // Auto-contrast
}
```

### Usage Example

```kotlin
import com.minddistrict.multiplatformpoc.core.designsystem.material.MaterialTheme

@Composable
fun PokemonTypeRow(types: List<String>, isDark: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        types.forEach { type ->
            TypeBadge(
                type = type,
                isDark = isDark,
                tokens = MaterialTheme.componentTokens.badge()
            )
        }
    }
}
```

### Material vs Unstyled Comparison

| Aspect | Material | Unstyled |
|--------|----------|----------|
| **Fill Style** | Filled (fillAlpha = 1.0) | Outline (fillAlpha = 0.0) |
| **Border** | None (0dp) | 2dp border |
| **Text Color** | White (high contrast) | Type color (dynamic) |
| **Corner Radius** | 12dp (rounded) | 8dp (subtle) |
| **Visual Weight** | Bold, attention-grabbing | Minimal, text-focused |

### Override Example

```kotlin
// Custom border width for outline variant
TypeBadge(
    type = "fire",
    isDark = false,
    tokens = MaterialTheme.componentTokens.badge(),
    overrideBorderWidth = 3.dp  // Thicker border for emphasis
)
```

### Preview References

- Material: [PokemonDetailMaterialScreen.kt](../../features/pokemondetail/ui-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/ui/material/PokemonDetailMaterialScreen.kt) — Type badges with filled styling
- Unstyled: [PokemonDetailUnstyledScreen.kt](../../features/pokemondetail/ui-unstyled/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/ui/unstyled/PokemonDetailUnstyledScreen.kt) — Outline badges with type colors

---

## AnimatedStatBar

**Purpose:** Horizontal progress bar for Pokémon base stats (HP, Attack, Defense, etc.) with smooth animation.

**File:** [AnimatedStatBar.kt](../../core/designsystem-core/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/core/designsystem/core/components/AnimatedStatBar.kt)

### Token Parameters

| Token | Type | Material Value | Unstyled Value |
|-------|------|----------------|----------------|
| `height` | `Dp` | 8dp | 6dp |
| `shape` | `Shape` | 4dp corners | 3dp corners |
| `backgroundColor` | `Color` | SurfaceVariant | Border color |
| `foregroundColor` | `Color` | Primary | Foreground color |
| `animationSpec` | `AnimationSpec<Float>` | Emphasized decelerate (400ms) | Linear (300ms) |

### Animation Behavior

- **Normal Mode:** Smooth animation with theme-specific easing curves
- **Reduced Motion:** Instant progress change (snap animation)
- **Progress Clamping:** Values automatically clamped to 0.0-1.0 range
- **Max Value:** Default 255 (official Pokémon stat range), customizable

### Usage Example

```kotlin
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import com.minddistrict.multiplatformpoc.core.designsystem.material.MaterialTheme

@Composable
fun StatRow(statName: String, statValue: Int) {
    val reducedMotion = rememberReducedMotion()
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(statName)
        Text("$statValue")
    }
    
    AnimatedStatBar(
        value = statValue,
        maxValue = 255,  // Official stat range
        tokens = MaterialTheme.componentTokens.progressBar(),
        reducedMotion = reducedMotion,
        modifier = Modifier.fillMaxWidth()
    )
}
```

### Reduced Motion Support

```kotlin
@Composable
fun rememberReducedMotion(): Boolean {
    val windowInfo = currentWindowAdaptiveInfo()
    return windowInfo.windowSizeClass.widthSizeClass == WindowWidthSizeClass.COMPACT
        && /* user prefers reduced motion */
}
```

### Material vs Unstyled Comparison

| Aspect | Material | Unstyled |
|--------|----------|----------|
| **Height** | 8dp (chunky) | 6dp (minimal) |
| **Animation** | 400ms emphasized decelerate | 300ms linear |
| **Shape** | 4dp corners (rounded) | 3dp corners (subtle) |
| **Background** | SurfaceVariant (tonal) | Light border color |
| **Foreground** | Primary (bold) | Foreground color |
| **Visual Feel** | Playful, smooth | Direct, efficient |

### Custom Max Value

```kotlin
// Example: Use custom max value for percentage-based stats
AnimatedStatBar(
    value = 75,
    maxValue = 100,  // Percentage scale
    tokens = MaterialTheme.componentTokens.progressBar(),
    reducedMotion = false
)
```

### Preview References

- Material: [PokemonDetailMaterialScreen.kt](../../features/pokemondetail/ui-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/ui/material/PokemonDetailMaterialScreen.kt) — Animated stats with emphasized motion
- Unstyled: [PokemonDetailUnstyledScreen.kt](../../features/pokemondetail/ui-unstyled/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/ui/unstyled/PokemonDetailUnstyledScreen.kt) — Linear stat bars

---

## Component Token Interfaces

**File:** [ComponentTokens.kt](../../core/designsystem-core/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/core/designsystem/core/components/ComponentTokens.kt)

All component token interfaces live in `core/designsystem-core` to ensure components remain theme-agnostic:

```kotlin
interface CardTokens {
    val shape: Shape
    val elevation: Dp
    val backgroundColor: Color
    val contentColor: Color
    val pressedScale: Float
}

interface BadgeTokens {
    val shape: Shape
    val borderWidth: Dp
    val fillAlpha: Float  // 0f = outline, 1f = filled
    val textColor: Color
}

interface ProgressBarTokens {
    val height: Dp
    val shape: Shape
    val backgroundColor: Color
    val foregroundColor: Color
    val animationSpec: AnimationSpec<Float>
}
```

---

## Theme-Specific Token Implementations

### Material Design 3

**File:** [MaterialComponentTokens.kt](../../core/designsystem-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/core/designsystem/material/tokens/MaterialComponentTokens.kt)

```kotlin
object MaterialComponentTokens {
    fun card(): CardTokens = /* expressive shapes, elevated cards */
    fun badge(): BadgeTokens = /* filled badges with white text */
    fun progressBar(): ProgressBarTokens = /* emphasized motion, chunky bars */
}
```

### Unstyled Theme

**File:** [UnstyledComponentTokens.kt](../../core/designsystem-unstyled/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/core/designsystem/unstyled/tokens/UnstyledComponentTokens.kt)

```kotlin
object UnstyledComponentTokens {
    fun card(): CardTokens = /* minimal shapes, flat cards */
    fun badge(): BadgeTokens = /* outline badges with type colors */
    fun progressBar(): ProgressBarTokens = /* linear motion, slim bars */
}
```

---

## Adding New Components

### Step 1: Define Token Interface

```kotlin
// In core/designsystem-core/src/.../ComponentTokens.kt
interface MyComponentTokens {
    val property1: Type1
    val property2: Type2
}
```

### Step 2: Create Theme-Agnostic Component

```kotlin
// In core/designsystem-core/src/.../components/MyComponent.kt
@Composable
fun MyComponent(
    tokens: MyComponentTokens,
    modifier: Modifier = Modifier
) {
    // Use tokens for styling
    Box(modifier = modifier.background(tokens.property1)) {
        // Component content
    }
}
```

### Step 3: Implement Tokens in Each Theme

```kotlin
// In core/designsystem-material/...
fun MaterialComponentTokens.myComponent(): MyComponentTokens = object : MyComponentTokens {
    override val property1 = /* Material value */
    override val property2 = /* Material value */
}

// In core/designsystem-unstyled/...
fun UnstyledComponentTokens.myComponent(): MyComponentTokens = object : MyComponentTokens {
    override val property1 = /* Unstyled value */
    override val property2 = /* Unstyled value */
}
```

### Step 4: Use in Screens

```kotlin
@Composable
fun MyScreen() {
    MyComponent(
        tokens = MaterialTheme.componentTokens.myComponent()
    )
}
```

---

## Best Practices

### 1. Prefer Tokens Over Overrides

```kotlin
// ✅ GOOD: Use theme tokens
PokemonCard(tokens = MaterialTheme.componentTokens.card(), onClick = {})

// ⚠️ USE SPARINGLY: Override only for special cases
PokemonCard(
    tokens = MaterialTheme.componentTokens.card(),
    overrideElevation = 12.dp,  // Only if truly needed
    onClick = {}
)
```

### 2. Always Support Reduced Motion

```kotlin
@Composable
fun MyAnimatedComponent() {
    val reducedMotion = rememberReducedMotion()
    
    AnimatedStatBar(
        /* ... */,
        reducedMotion = reducedMotion  // ✅ Always pass this
    )
}
```

### 3. Use Official Type Colors

```kotlin
// ✅ GOOD: Use PokemonTypeColors
val typeColor = PokemonTypeColors.getBackground("fire", isDark)

// ❌ BAD: Hardcode colors
val typeColor = Color(0xFFFF0000)  // Don't do this
```

### 4. Test Components in Both Themes

```kotlin
// Always create previews for both Material and Unstyled
@Preview
@Composable
fun MyComponent_MaterialPreview() {
    MaterialTheme {
        MyComponent(tokens = MaterialTheme.componentTokens.card())
    }
}

@Preview
@Composable
fun MyComponent_UnstyledPreview() {
    UnstyledTheme {
        MyComponent(tokens = UnstyledTheme.componentTokens.card())
    }
}
```

---

## Related Documentation

- **Design Token System:** [design_tokens.md](design_tokens.md)
- **Material Design 3 Expressive:** [ui_ux.md](../project/ui_ux.md)
- **Testing Strategy:** [testing_strategy.md](testing_strategy.md)
- **Architecture Conventions:** [conventions.md](conventions.md)
