---
name: kmp-design-systems
description: Design tokens, components, and icon strategy for dual-theme system
version: 1.0.0
tags: [design, tokens, components, icons, material, unstyled]
---

# KMP Design Systems

Design system architecture for organizing design tokens, reusable components, and icon strategies in Kotlin Multiplatform.

## When to Use This Skill

Use this skill when:
- **Working on design tokens** (spacing, shapes, elevation, motion)
- **Creating or customizing components** (PokemonCard, TypeBadge, AnimatedStatBar)
- **Implementing icons** (Vector Drawable XML, Material Symbols)
- **Integrating themes** (Material Design 3 vs Compose Unstyled)

## Design System Architecture

The design system uses a two-layer token system provided via `CompositionLocal`:

1.  **Design Tokens** ([design_tokens.md](references/design_tokens.md)): Foundation tokens (spacing, shapes, elevation, motion).
2.  **Component Tokens** ([component_library.md](references/component_library.md)): Component-specific styling via interfaces (`CardTokens`, `BadgeTokens`).

### Key Patterns

#### Token Access Pattern
```kotlin
MaterialTheme.tokens.spacing.medium   // Design Token
MaterialTheme.componentTokens.card()  // Component Token
```

#### LaunchedEffect Token Capture (CRITICAL)
Always capture tokens BEFORE `LaunchedEffect` since the lambda is a suspend context and cannot access `@Composable` providers:

```kotlin
// ✅ CORRECT
val motionTokens = MaterialTheme.tokens.motion
LaunchedEffect(Unit) {
    animateTo(durationMillis = motionTokens.durationMedium)
}
```

## Component Library

Shared components live in `:core:designsystem-core` and are theme-agnostic. They accept token interfaces to allow different visual styling.

- **PokemonCard**: Responsive card with press feedback.
- **TypeBadge**: Accessible Pokémon type indicators.
- **AnimatedStatBar**: Smoothly animated progress bars with reduced motion support.

See [component_library.md](references/component_library.md) for full specs and usage.

## Material Icons Strategy

We use **Material Symbols Rounded Filled** as Vector Drawable XML files.

- **Source**: [fonts.google.com/icons](https://fonts.google.com/icons) (Android tab).
- **Storage**: `composeResources/drawable/ic_*.xml` in `:core:designsystem-core`.
- **Usage**: `painterResource(Res.drawable.ic_*)` with `tint` and `contentDescription`.

See [material_icons_strategy.md](references/material_icons_strategy.md) for configuration and inventory.

## Related Skills
- @kmp-architecture - Module structure and layer placement
- @kmp-compose-unstyled - Headless component patterns
- @compose-screen - Using design system in screens
- @ui-ux-designer - Visual design guidelines

## Essential Workflows

### Workflow 1: Creating Design Tokens for a New Theme

1.  **Define design token interfaces** in `:core:designsystem-material` (e.g., `MaterialDesignTokens`).
2.  **Implement the interface** with specific values for spacing, shapes, elevation, and motion.
3.  **Provide the tokens** using a `CompositionLocalProvider` in your theme Composable.

```kotlin
// features/<feature>/ui-material/src/commonMain/kotlin/.../CustomTheme.kt
internal class CustomDesignTokens : MaterialDesignTokens {
    override val spacing = object : SpacingTokens {
        override val medium = 16.dp
        // ... other tokens
    }
    // ... implementations for shapes, elevation, motion
}

@Composable
fun CustomTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalMaterialTokens provides CustomDesignTokens()
    ) {
        MaterialTheme(content = content)
    }
}
```

### Workflow 2: Building a Themed Component with Token Access

1.  **Define a Composable component** that accepts a token interface as an optional parameter.
2.  **Default to the theme's tokens** using the `MaterialTheme` extension property.
3.  **Apply tokens** to internal Material 3 components to maintain visual consistency.

```kotlin
@Composable
fun PokemonCard(
    pokemon: Pokemon,
    modifier: Modifier = Modifier,
    tokens: CardTokens = MaterialTheme.componentTokens.card(),
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.scale(if (pressed) tokens.pressedScale else 1f),
        shape = tokens.shape,
        elevation = CardDefaults.cardElevation(defaultElevation = tokens.elevation),
        colors = CardDefaults.cardColors(containerColor = tokens.containerColor),
        onClick = onClick
    ) {
        // Component content using spacing tokens
        Column(modifier = Modifier.padding(MaterialTheme.tokens.spacing.medium)) {
            Text(text = pokemon.name)
        }
    }
}
```

### Workflow 3: Adding Material Icons (Vector Drawable XML)

1.  **Download Material Symbol** from [fonts.google.com/icons](https://fonts.google.com/icons) in Android XML format.
2.  **Save to `:core:designsystem-core`** under `src/commonMain/composeResources/drawable/ic_<name>.xml`.
3.  **Remove platform-specific references** (like `@android:color`) and use hex values or `currentColor`.
4.  **Use in UI** with `painterResource` and apply type-safe colors.

```kotlin
Icon(
    painter = painterResource(Res.drawable.ic_pokemon_type_fire),
    contentDescription = "Fire Type",
    tint = PokemonTypeColors.getBackground("fire", isDark = isSystemInDarkTheme())
)
```

### Workflow 4: Implementing LaunchedEffect Token Capture Pattern

1.  **Capture the token object** in a local variable outside the `LaunchedEffect` lambda.
2.  **Access the captured variable** inside the suspend context.

```kotlin
@Composable
fun AnimatedStatBar(value: Float) {
    // 1. Capture BEFORE LaunchedEffect
    val motion = MaterialTheme.tokens.motion
    val animValue = remember { Animatable(0f) }

    LaunchedEffect(value) {
        // 2. Use captured token in suspend context
        animValue.animateTo(
            targetValue = value,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = motion.stiffnessMedium
            )
        )
    }
}
```

## Critical Guardrails

1.  **NEVER hardcode dp values** → always use `MaterialTheme.tokens.spacing` or `shapes` (reason: ensures consistency across all components and themes).
2.  **NEVER skip LaunchedEffect token capture pattern** → always capture tokens in a local variable before the lambda (reason: `@Composable` providers cannot be accessed from suspend contexts).
3.  **NEVER use @android:color references in icons** → use hardcoded hex values or `android:fillColor="currentColor"` (reason: platform-specific references break multiplatform compilation).
4.  **NEVER forget contentDescription for accessibility** → always provide a meaningful description or `null` for decorative elements (reason: ensures screen reader compatibility).
5.  **NEVER use material-icons-extended library** → always use Vector Drawable XML files in `composeResources/drawable` (reason: minimizes application binary size by including only used icons).
6.  **NEVER hardcode colors for Pokémon types** → always use the `PokemonTypeColors` utility (reason: ensures WCAG AA accessibility compliance across the 18 types).
7.  **NEVER apply hardcoded elevation values** → use `MaterialTheme.tokens.elevation` levels (reason: maintains consistent visual hierarchy and depth across themes).
8.  **NEVER use platform-specific font names** → use the design system's `Typography` tokens (reason: ensures cross-platform font consistency).

## Quick Reference

| Pattern | Purpose | Example |
| :--- | :--- | :--- |
| `MaterialTheme.tokens.spacing.*` | Access spacing/padding tokens | `padding(MaterialTheme.tokens.spacing.medium)` |
| `MaterialTheme.tokens.shapes.*` | Access corner radius tokens | `clip(MaterialTheme.tokens.shapes.large)` |
| `MaterialTheme.tokens.elevation.*` | Access elevation level tokens | `elevation = MaterialTheme.tokens.elevation.level2` |
| `MaterialTheme.tokens.motion.*` | Access motion/duration tokens | `animateTo(durationMillis = motion.durationLong)` |
| `MaterialTheme.componentTokens.*` | Access component-specific styles | `MaterialTheme.componentTokens.card()` |
| `LaunchedEffect` Capture | Access tokens in suspend context | `val motion = MaterialTheme.tokens.motion; LaunchedEffect { ... }` |
| `painterResource` | Load icon from resources | `painterResource(Res.drawable.ic_search)` |

## Troubleshooting Common Design System Issues

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

---

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

## Cross-References

### Skills (by Category)

**Design & UI**
| Skill | Purpose | Link |
| --- | --- | --- |
| @compose-screen | Using design system in screens | [SKILL.md](../compose-screen/SKILL.md) |
| @kmp-compose-unstyled | Headless component patterns | [SKILL.md](../kmp-compose-unstyled/SKILL.md) |
| @ui-ux-designer | Visual design guidelines | [SKILL.md](../ui-ux-designer/SKILL.md) |
| @swiftui-screen | iOS design integration | [SKILL.md](../swiftui-screen/SKILL.md) |

**Architecture & Development**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-architecture | Module structure and layer placement | [SKILL.md](../kmp-architecture/SKILL.md) |
| @kmp-developer | General development patterns | [SKILL.md](../kmp-developer/SKILL.md) |
| @kmp-testing-patterns | Component testing patterns | [SKILL.md](../kmp-testing-patterns/SKILL.md) |
| @kmp-critical-patterns | Quick patterns reference | [SKILL.md](../kmp-critical-patterns/SKILL.md) |

### Documents

| Document | Purpose | Link |
| --- | --- | --- |
| design_tokens.md | Token system architecture | [design_tokens.md](../../../docs/tech/design_tokens.md) |
| component_library.md | Component specifications | [component_library.md](../../../docs/tech/component_library.md) |
| material_icons_strategy.md | Icon usage guide | [material_icons_strategy.md](../../../docs/tech/material_icons_strategy.md) |
| conventions.md | Master architecture reference | [conventions.md](../../../docs/tech/conventions.md) |
