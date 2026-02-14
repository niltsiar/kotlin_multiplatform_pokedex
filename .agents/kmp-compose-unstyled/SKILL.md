---
name: kmp-compose-unstyled
description: "Headless component library with platform-native theming and accessibility. Use when: (1) Building Unstyled UI screens in :ui-unstyled modules, (2) Configuring themes with buildPlatformTheme DSL, (3) Implementing headless components (Button, Text, ProgressIndicator), (4) Ensuring platform-native accessibility, (5) Creating custom-styled components. Keywords: Compose Unstyled, headless components, platform theming, accessibility"
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

## Critical Guardrails

1. **NEVER include Material Design 3 patterns or components in Unstyled modules** → keep modules strictly separated (reason: maintains clean architectural boundaries and prevents visual leakage).
2. **NEVER hardcode platform-specific sizes** → always use `Theme[interactiveSizes][sizeDefault]` (reason: ensures accessibility compliance across Android/iOS/Desktop/Web automatically).
3. **NEVER manual configure `fontFamily` in `buildPlatformTheme`** → let it be automatic unless using custom fonts (reason: ensures native platform look-and-feel without extra configuration).
4. **NEVER store `Theme.currentTheme` in variables** → always use direct bracket notation `Theme[prop][token]` (reason: breaks reactivity and prevents state atomicity issues).
5. **NEVER use `Modifier.clickable` for interactive elements** → use the `Button` component instead (reason: `Button` provides built-in ARIA support and keyboard interaction logic).
6. **NEVER forget the inner fill Box for `ProgressIndicator`** → always implement the rendering block (reason: unlike Material, Unstyled ProgressIndicator renders no UI by default).
7. **NEVER skip `@Preview` annotations** → every UI component needs a preview with its theme (reason: essential for visual verification and developer efficiency).
8. **NEVER access tokens via `MaterialTheme.tokens` in unstyled modules** → use the Unstyled `Theme` object (reason: prevents tight coupling and breaks dual-theme encapsulation).

## Decision Framework

Before implementing Unstyled components, ask yourself:

1. **What level of customization is needed?**
   - Platform-native defaults → Use `buildPlatformTheme` (automatic fonts, sizes, indications)
   - Custom styling → Use `buildTheme` with explicit token values
   - Component-specific → Apply Modifier chains to headless components

2. **Which headless components should I use?**
   - Interactive elements → `Button`, `IconButton` (handle touch targets, accessibility)
   - Text display → `Text` (platform-native typography)
   - Loading states → `ProgressIndicator` (platform-specific animations)
   - Custom components → Build with `Modifier.interactiveSize()` for accessibility

3. **How do I ensure accessibility?**
   - Always use `Modifier.interactiveSize()` for touch targets (48dp minimum)
   - Use `Modifier.indication()` for platform-native feedback
   - Add `@Preview` with theme applied for visual verification
   - Test on both Android and iOS for platform consistency

## Essential Workflows

### Workflow 1: Creating an Unstyled Screen with buildPlatformTheme

To implement a screen with platform-native theming:

1. Define the theme using `buildPlatformTheme` in your design system module:
   ```kotlin
   val UnstyledTheme = buildPlatformTheme(name = "UnstyledTheme") {
       defaultContentColor = Color(0xFF1C1B1F)
       defaultTextStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal)
       // Platform fonts (Roboto, SF Pro) applied automatically
   }
   ```
2. Wrap your screen content with the theme object:
   ```kotlin
   @Composable
   fun PokemonListUnstyledScreen(viewModel: PokemonListViewModel) {
       val uiState by viewModel.uiState.collectAsStateWithLifecycle()
       UnstyledTheme {
           PokemonListContent(uiState = uiState)
       }
   }
   ```
3. Access tokens using bracket notation:
   ```kotlin
   Box(modifier = Modifier.background(Theme[colors][background])) {
       Text("Pokédex", style = Theme[typography][headlineLarge])
   }
   ```

Related skills: @kmp-design-systems, @compose-screen

### Workflow 2: Building Custom Headless Components

To create reusable components using Unstyled primitives:

1. Use the `Button` component for any interactive element:
   ```kotlin
   @Composable
   fun PokemonCard(pokemon: Pokemon, onClick: () -> Unit) {
       Button(
           onClick = onClick,
           backgroundColor = Theme[colors][surface],
           contentColor = Theme[colors][onSurface],
           shape = Theme[shapes][cardShape],
           modifier = Modifier.interactiveSize(Theme[interactiveSizes][sizeDefault])
       ) {
           Column(horizontalAlignment = Alignment.CenterHorizontally) {
               AsyncImage(model = pokemon.imageUrl, contentDescription = null)
               Text(text = pokemon.name, style = Theme[typography][bodyLarge])
           }
       }
   }
   ```
2. Apply `interactiveSize` to ensure accessibility standards are met.

Related skills: @kmp-design-systems, @ui-ux-designer

### Workflow 3: Implementing Interactive Size for Accessibility

To ensure your UI meets platform-specific touch target requirements:

1. Use `sizeDefault` (48dp Android, 44dp iOS) for primary actions:
   ```kotlin
   Button(
       onClick = {},
       modifier = Modifier.interactiveSize(Theme[interactiveSizes][sizeDefault])
   ) { Text("Accessible Button") }
   ```
2. Use `sizeMinimum` (32dp Android, 28dp iOS) for dense toolbars:
   ```kotlin
   IconButton(
       onClick = {},
       modifier = Modifier.interactiveSize(Theme[interactiveSizes][sizeMinimum])
   ) { Icon(Lucide.Settings, contentDescription = "Settings") }
   ```

Related skills: @kmp-architecture, @ui-ux-designer

### Workflow 4: Using ProgressIndicator with Custom Styling

To implement a progress bar that requires manual fill rendering:

1. Provide the rendering block inside the `ProgressIndicator` call:
   ```kotlin
   ProgressIndicator(
       progress = progress,
       modifier = Modifier.fillMaxWidth().height(8.dp),
       shape = RoundedCornerShape(8.dp),
       backgroundColor = Theme[colors][surface],
       contentColor = Theme[colors][primary]
   ) {
       // Manual rendering of the progress fill
       Box(
           Modifier
               .fillMaxWidth(progress)
               .fillMaxSize()
               .background(contentColor, shape)
       )
   }
   ```

Related skills: @kmp-developer, @compose-screen

## Quick Reference

| Pattern | Purpose | Example |
|---------|---------|---------|
| `buildPlatformTheme` | Platform-native fonts/sizes | `buildPlatformTheme(name = "App") { ... }` |
| `Theme[colors][primary]` | Direct theme access | `val primary = Theme[colors][primary]` |
| `interactiveSize` | Accessibility compliance | `Modifier.interactiveSize(Theme[interactiveSizes][sizeDefault])` |
| `ProgressIndicator` | Custom fill rendering | `ProgressIndicator(progress) { Box(...) }` |
| `ProvideTextStyle` | Cascading text styles | `ProvideTextStyle(Theme[typography][body]) { ... }` |

## Cross-References

### Skills (by Category)

**Design & UI**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-design-systems | Token system and component architecture | [SKILL.md](../kmp-design-systems/SKILL.md) |
| @ui-ux-designer | Visual design and animation guidelines | [SKILL.md](../ui-ux-designer/SKILL.md) |
| @compose-screen | General patterns for implementing Compose screens | [SKILL.md](../compose-screen/SKILL.md) |

**Implementation**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-architecture | Module structure and vertical slicing | [SKILL.md](../kmp-architecture/SKILL.md) |
| @kmp-developer | General development and feature implementation | [SKILL.md](../kmp-developer/SKILL.md) |
| @kmp-critical-patterns | 6 core patterns quick reference | [SKILL.md](../kmp-critical-patterns/SKILL.md) |

**Platform & Navigation**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-navigation | Navigation 3 modular architecture | [SKILL.md](../kmp-navigation/SKILL.md) |
| @kmp-testing-patterns | UI and screenshot testing with Roborazzi | [SKILL.md](../kmp-testing-patterns/SKILL.md) |

### Documents

| Document | Purpose | Link |
| --- | --- | --- |
| compose_unstyled_patterns.md | Detailed patterns catalog | See @kmp-compose-unstyled skill |
| compose_unstyled_reference.md | Catalog and implementation reference | [compose_unstyled_reference.md](references/compose_unstyled_reference.md) |
| component_token_customization_example.md | Customization via CompositionLocal | [component_token_customization_example.md](references/component_token_customization_example.md) |
| troubleshooting.md | Common UI component issues and solutions | [troubleshooting.md](references/troubleshooting.md) |
| [@kmp-architecture](../kmp-architecture/SKILL.md) | Architecture and development conventions | Architecture skill |

## Troubleshooting Common Unstyled Component Issues

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
