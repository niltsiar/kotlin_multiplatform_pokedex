---
name: kmp-compose-unstyled
description: Headless component library with platform-native theming and accessibility. Use when building Unstyled UI screens, working with buildPlatformTheme, implementing headless components, or platform-native theming.
version: 1.0.0
tags: [unstyled, headless, theming, platform-native, accessibility]
---

# KMP Compose Unstyled

Headless component library implementation patterns for the Pokédex project. Unstyled components handle UX logic, state, and accessibility while rendering no visual UI by default.

## When to Use This Skill

- **Building Unstyled UI screens** in `:features:<feature>:ui-unstyled` modules.
- **Configuring Themes** using `buildTheme` or `buildPlatformTheme` DSLs.
- **Implementing Headless Components** (Button, Text, ProgressIndicator, etc.) with custom styling.
- **Ensuring Platform-Native Accessibility** using interactive size modifiers and platform-specific indications.

## Core Patterns

### 1. buildPlatformTheme DSL
Uses platform-specific fonts, sizes, and indications automatically.
```kotlin
val PlatformTheme = buildPlatformTheme(name = "MyAppTheme") {
    defaultContentColor = Color.Black
    defaultTextStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal)
    // Platform fonts (Roboto, SF Pro) applied automatically
}
```

### 2. Interactive Size Modifier
Ensures touch targets meet platform accessibility standards (Android 48dp, iOS 44dp).
```kotlin
Button(
    onClick = {},
    modifier = Modifier.interactiveSize(Theme[interactiveSizes][sizeDefault])
) { Text("Accessible Button") }
```

### 3. Theme Access Syntax
Always use direct bracket notation for fresh theme reads. Avoid storing theme references.
```kotlin
val primary = Theme[colors][primary] // ✅ Direct access
val body = Theme[typography][bodyMedium]
```

### 4. ProgressIndicator Wrapper
Unlike Material, Unstyled `ProgressIndicator` requires a wrapper to render the fill.
```kotlin
ProgressIndicator(progress = progress) {
    Box(Modifier.fillMaxWidth(progress).fillMaxSize().background(contentColor, shape))
}
```

## Related Skills
- **@kmp-design-systems** - Token system and core component architecture.
- **@kmp-architecture** - Module structure (Unstyled theme in `:core:designsystem-unstyled`).
- **@compose-screen** - General patterns for implementing Compose screens.
- **@ui-ux-designer** - Visual design and animation guidelines.

## NEVER Guidelines

- **NEVER** include Material Design 3 patterns or components in Unstyled modules.
- **NEVER** hardcode platform-specific sizes (use `Theme[interactiveSizes][sizeDefault]`).
- **NEVER** manual configure `fontFamily` in `buildPlatformTheme` unless using custom fonts (it's automatic).
- **NEVER** store `Theme.currentTheme` in variables (breaks reactivity/atomicity).

## Reference Documentation

- [Compose Unstyled Catalog & Patterns](references/compose_unstyled_reference.md)
- [Component Token Customization Patterns](references/component_token_customization_example.md)
