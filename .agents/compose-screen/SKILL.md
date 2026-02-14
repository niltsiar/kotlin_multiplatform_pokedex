---
name: compose-screen
description: "Implement Compose UI screens for Android and Desktop from specifications. Use when: (1) Creating screens in :features:<feature>:ui-material and :ui-unstyled modules, (2) Building Material Design 3 or Compose Unstyled UI, (3) Adding @Preview annotations for composables, (4) Implementing dual-theme support, (5) Building responsive adaptive layouts"
---

# Compose Screen Skill

Implement Compose UI screens for Android and Desktop from specifications.

## When to Use

- Creating screens in `:features:<feature>:ui-material` and `:ui-unstyled`
- Building Material Design 3 or Compose Unstyled UI
- Adding @Preview annotations for composables
- Implementing dual-theme support
- Building responsive adaptive layouts

## Core Requirements

### @Preview Mandatory

Every @Composable function MUST have @Preview annotation.

**MANDATORY - READ ENTIRE FILE**: For complete preview examples including multi-state, multi-theme, and responsive previews, read [preview-examples.md](references/preview-examples.md) (~150 lines).

**Basic Preview Pattern:**
```kotlin
@Composable
fun MyComponent(modifier: Modifier = Modifier) { }

@Preview(name = "Default")
@Composable
private fun MyComponentPreview() {
    PokemonTheme {
        MyComponent()
    }
}
```

**Do NOT load** `preview-examples.md` if only adding simple component previews.

### Dual-Theme Check

All features require BOTH Material Design 3 and Compose Unstyled implementations:

```
:features:<feature>:ui-material/      → Material Design 3 UI
:features:<feature>:ui-unstyled/      → Compose Unstyled UI
```

**Pattern:**
```kotlin
// ui-material/PokemonListMaterialScreen.kt
@Composable
fun PokemonListMaterialScreen(
    viewModel: PokemonListViewModel,
    modifier: Modifier = Modifier,
    onPokemonClick: (Pokemon) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PokemonListMaterialContent(uiState = uiState, /* ... */)
}

// ui-unstyled/PokemonListUnstyledScreen.kt
@Composable
fun PokemonListUnstyledScreen(
    viewModel: PokemonListViewModel,
    modifier: Modifier = Modifier,
    onPokemonClick: (Pokemon) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PokemonListUnstyledContent(uiState = uiState, /* ... */)
}
```

### Token-Based Styling

**ALWAYS use design tokens, never hardcoded values:**

```kotlin
// ✅ CORRECT - Use tokens
Card(
    modifier = modifier.padding(MaterialTheme.tokens.spacing.medium),
    elevation = CardDefaults.cardElevation(
        defaultElevation = MaterialTheme.tokens.elevation.level2
    )
)

// ❌ WRONG - Hardcoded values
Card(
    modifier = modifier.padding(16.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
)
```

---

## Essential Workflows

### Workflow 1: Create Feature UI Structure

```bash
features/<feature>/ui-material/src/commonMain/kotlin/<pkg>/
└── <Feature>MaterialScreen.kt          # Main screen
└── <Feature>MaterialScreenPreviews.kt  # Preview composables
└── components/
    ├── <Feature>Card.kt
    ├── <Feature>Grid.kt
    ├── LoadingState.kt
    ├── ErrorState.kt
    └── EmptyState.kt

features/<feature>/ui-unstyled/src/commonMain/kotlin/<pkg>/
└── <Feature>UnstyledScreen.kt
└── <Feature>UnstyledScreenPreviews.kt
└── components/
```

### Workflow 2: Define Screen Contract

```kotlin
@Composable
fun <Feature>MaterialScreen(
    viewModel: <Feature>ViewModel,
    modifier: Modifier = Modifier,
    onItemClick: (Item) -> Unit = {},
    onBackClick: () -> Unit = {}
)
```

### Workflow 3: Create UI State Handler

```kotlin
@Composable
internal fun <Feature>MaterialContent(
    uiState: <Feature>UiState,
    onLoadMore: () -> Unit,
    onItemClick: (Item) -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is <Feature>UiState.Loading -> LoadingState(modifier)
        is <Feature>UiState.Error -> ErrorState(uiState.message, onRetry, modifier)
        is <Feature>UiState.Content -> <Feature>List(uiState.items, onItemClick, modifier)
    }
}
```

### Workflow 4: Implement Main Screen

```kotlin
@Composable
fun <Feature>MaterialScreen(
    viewModel: <Feature>ViewModel,
    modifier: Modifier = Modifier,
    onItemClick: (Item) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    <Feature>MaterialContent(
        uiState = uiState,
        onLoadMore = viewModel::loadNextPage,
        onItemClick = onItemClick,
        modifier = modifier
    )
}
```

### Workflow 5: Add All @Preview Variations

Preview all states: Loading, Error, Content, LoadingMore, EndOfList.

**MANDATORY - READ ENTIRE FILE**: For complete multi-state preview examples, read [preview-examples.md](references/preview-examples.md).

---

## Critical Guardrails

1. **NEVER skip @Preview annotations** - Every @Composable must have preview
2. **NEVER use hardcoded dp values** - Always use `MaterialTheme.tokens`
3. **NEVER create screens without dual-theme support** - Both Material and Unstyled required
4. **NEVER use star imports** - Always use explicit imports
5. **NEVER access ViewModel.state directly** - Use `collectAsStateWithLifecycle()`
6. **NEVER put navigation callbacks in UI state** - Pass as parameters

---

## Quick Reference

### Validation Commands

| Command | Purpose |
|---------|---------|
| `./gradlew :composeApp:assembleDebug test --continue` | Primary validation |
| `./gradlew :composeApp:run` | Run desktop app |

### Token Reference

| Token Category | Access | Example |
|----------------|--------|---------|
| Spacing | `MaterialTheme.tokens.spacing.medium` | 16.dp |
| Elevation | `MaterialTheme.tokens.elevation.level2` | 3.dp |
| Shapes | `MaterialTheme.tokens.shapes.large` | 24.dp corner |
| Motion | `MaterialTheme.tokens.motion.durationMedium` | 300ms |

### Preview Template

```kotlin
@Preview(name = "<State Name>")
@Composable
private fun <Feature><State>Preview() {
    PokemonTheme {
        Surface {
            <Feature>MaterialContent(
                uiState = <Feature>UiState.<State>(/* mock data */)
            )
        }
    }
}
```

---

## Troubleshooting Common UI Component Issues

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

## Related Skills

- @kmp-architecture - Module structure and vertical slice organization
- @kmp-presentation - ViewModels and UI state management for screens
- @kmp-navigation - Navigation 3 modular architecture
- @kmp-design-systems - Design tokens, components, and icon strategy
- @kmp-compose-unstyled - Headless component patterns for Unstyled screens
- @kmp-testing-patterns - Screenshot tests with Roborazzi
- @ui-ux-designer - Visual design, animations, and interaction patterns

---

## Cross-References

| Document | Purpose | Link |
|----------|---------|------|
| Architecture + conventions | Master reference | [@kmp-architecture](../kmp-architecture/SKILL.md) |
| Design tokens | Token system | [@kmp-design-systems](../kmp-design-systems/SKILL.md) |
| Critical patterns | 6 core patterns | [@kmp-critical-patterns](../kmp-critical-patterns/SKILL.md) |
| Module structure | Feature modules and UI layers | [kmp-architecture](../kmp-architecture/SKILL.md) |
| ViewModel patterns | UI state and lifecycle | [kmp-presentation](../kmp-presentation/SKILL.md) |
| Navigation 3 | Modular navigation | [@kmp-navigation](../kmp-navigation/SKILL.md) |
| Screenshot testing | Roborazzi tests | [kmp-testing-patterns](../kmp-testing-patterns/SKILL.md) |
| Animation guides | UI animations | [ui-ux-designer](../ui-ux-designer/SKILL.md) |
| Complete preview examples | Multi-state previews | [references/preview-examples.md](references/preview-examples.md) |

### Reference Implementations

**Pokemon List (Material):**
- [PokemonListMaterialScreen.kt](../../features/pokemonlist/ui-material/src/.../PokemonListMaterialScreen.kt)
- [PokemonListMaterialScreenPreviews.kt](../../features/pokemonlist/ui-material/src/.../PokemonListMaterialScreenPreviews.kt)

**Pokemon Detail (with animations):**
- [PokemonDetailMaterialScreen.kt](../../features/pokemondetail/ui-material/src/.../PokemonDetailMaterialScreen.kt)
