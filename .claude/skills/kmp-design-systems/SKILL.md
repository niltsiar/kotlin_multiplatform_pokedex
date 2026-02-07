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

## NEVER
- **Do NOT** hardcode dp values - always use `MaterialTheme.tokens`
- **Do NOT** skip `LaunchedEffect` token capture pattern
- **Do NOT** use `@android:color` references in icons - use hex or tint
- **Do NOT** forget `contentDescription` for accessibility
- **Do NOT** use `material-icons-extended` library - use Vector XML
- **Do NOT** hardcode colors - use `PokemonTypeColors` for Pokémon types
