---
name: compose-screen
description: This skill should be used when implementing Compose screens from specifications. Use for Android and Desktop UI implementation with @Preview requirements.
---

## When to Use

Use this skill when implementing Compose UI screens for Android and Desktop from specifications:

- Creating new screens in `:features:<feature>:ui-material` and `:features:<feature>:ui-unstyled`
- Implementing component-based UI with Material Design 3 and Compose Unstyled
- Adding @Preview annotations for all composables
- Following dual-theme design system patterns
- Building responsive layouts with adaptive components

Do NOT use this skill for:
- Product/PRD decisions → switch to Product Design Mode
- Visual design/animations → switch to UI/UX Design Mode
- Shared ViewModel implementation → switch to KMP Mobile Expert Mode
- SwiftUI iOS screens → switch to SwiftUI Screen Implementation Mode
- Koin navigation wiring → switch to KMP Mobile Expert Mode

## Mode Detection

### FROM_SPEC Mode (Default)

Use when implementing screens from explicit specifications (requirements, mocks, wireframes):

1. Identify required UI states (Loading, Error, Content, Empty)
2. Map specification to component structure
3. Create mock data matching spec requirements
4. Implement with token-based styling
5. Add @Preview for all states

### DESIGN_FIRST Mode

Use when no explicit spec exists and you need to design the UI:

1. Reference existing similar screens for patterns
2. Use design tokens from `MaterialTheme.tokens` and `MaterialTheme.componentTokens`
3. Apply Material 3 Expressive guidelines
4. Implement dual-theme support (Material + Unstyled)
5. Validate with @Preview for all states

## Core Requirements

### @Preview Mandatory

**Every @Composable function MUST have @Preview annotation:**

```kotlin
@Composable
fun MyComponent(modifier: Modifier = Modifier) {
    // UI implementation
}

// ✅ CORRECT - Has @Preview
@Preview(name = "Default")
@Composable
private fun MyComponentPreview() {
    MyTheme {
        MyComponent()
    }
}
```

**Preview requirements:**
- Private preview composables for IDE/Studio rendering
- Named previews with `name` parameter for clarity
- Mock ViewModel for screen-level previews
- Use `Surface` wrapper for isolated component previews
- Preview all UI states: Loading, Error, Content, Edge cases

### Dual-Theme Check

All features require BOTH Material Design 3 and Compose Unstyled implementations:

```
:features:<feature>:ui-material/      → Material Design 3 UI
:features:<feature>:ui-unstyled/      → Compose Unstyled UI
```

**Dual-theme implementation pattern:**

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

### Mock ViewModel for Previews

Create mock ViewModels for screen-level previews:

```kotlin
// PokemonListScreenPreviews.kt
@Composable
private fun PokemonListContentPreview() {
    PokemonTheme {
        Surface {
            PokemonListMaterialContent(
                uiState = PokemonListUiState.Content(
                    pokemons = persistentListOf(
                        Pokemon(name = "Bulbasaur", detailUrl = "..."),
                        Pokemon(name = "Charmander", detailUrl = "...")
                    ),
                    isLoadingMore = false,
                    hasMore = true
                ),
                restoredScrollIndex = 0,
                restoredScrollOffset = 0,
                onLoadMore = {},
                onPokemonClick = {},
                onScrollPositionChanged = { _, _ -> }
            )
        }
    }
}
```

## Implementation Workflow

### 1. Create Feature UI Structure

Create module directories following split-by-layer pattern:

```bash
features/<feature>/ui-material/src/commonMain/kotlin/<pkg>/
└── <Feature>MaterialScreen.kt          # Main screen
└── <Feature>MaterialScreenPreviews.kt  # Preview composables
└── components/                          # Reusable components
    ├── <Feature>Card.kt
    ├── <Feature>Grid.kt
    ├── LoadingState.kt
    ├── ErrorState.kt
    └── EmptyState.kt

features/<feature>/ui-unstyled/src/commonMain/kotlin/<pkg>/
└── <Feature>UnstyledScreen.kt          # Unstyled variant
└── <Feature>UnstyledScreenPreviews.kt  # Preview composables
└── components/                          # Unstyled components
```

### 2. Define Screen Contract

Identify required parameters and callbacks:

```kotlin
// Parameters: ViewModel + modifier + navigation callbacks
@Composable
fun <Feature>MaterialScreen(
    viewModel: <Feature>ViewModel,
    modifier: Modifier = Modifier,
    onItemClick: (Item) -> Unit = {},
    onBackClick: () -> Unit = {}
)
```

### 3. Create UI State Handler

Handle all UI states in main content composable:

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

### 4. Implement Reusable Components

Create isolated components with @Preview:

```kotlin
@Composable
fun <Feature>Card(
    item: Item,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(
            defaultElevation = MaterialTheme.tokens.elevation.level2
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.tokens.spacing.medium)
        ) {
            // Content
        }
    }
}

@Preview(name = "Default")
@Composable
private fun <Feature>CardPreview() {
    PokemonTheme {
        Surface {
            <Feature>Card(
                item = Item(name = "Sample"),
                onClick = {}
            )
        }
    }
}
```

### 5. Implement Main Screen

Wire ViewModel state to content:

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

### 6. Add All @Preview Variations

Preview all states and edge cases:

```kotlin
@Preview(name = "Loading")
@Composable
private fun <Feature>LoadingPreview() {
    PokemonTheme {
        Surface {
            <Feature>MaterialContent(
                uiState = <Feature>UiState.Loading,
                onLoadMore = {},
                onItemClick = {},
                onScrollPositionChanged = { _, _ -> }
            )
        }
    }
}

@Preview(name = "Error")
@Composable
private fun <Feature>ErrorPreview() {
    PokemonTheme {
        Surface {
            <Feature>MaterialContent(
                uiState = <Feature>UiState.Error("Network error"),
                onLoadMore = {},
                onItemClick = {},
                onScrollPositionChanged = { _, _ -> }
            )
        }
    }
}

@Preview(name = "Content")
@Composable
private fun <Feature>ContentPreview() {
    PokemonTheme {
        Surface {
            <Feature>MaterialContent(
                uiState = <Feature>UiState.Content(
                    items = persistentListOf(Item("A"), Item("B"), Item("C")),
                    isLoadingMore = false,
                    hasMore = true
                ),
                onLoadMore = {},
                onItemClick = {},
                onScrollPositionChanged = { _, _ -> }
            )
        }
    }
}
```

### 7. Validate Implementation

Run primary validation command:

```bash
./gradlew :composeApp:assembleDebug test --continue
```

## Critical Anti-Patterns

1. **NEVER skip @Preview annotations** - Every @Composable function must have preview for IDE validation and visual testing

2. **NEVER use hardcoded dp values** - Always use `MaterialTheme.tokens.spacing`, `MaterialTheme.tokens.elevation`, `MaterialTheme.tokens.shapes`

3. **NEVER create screens without dual-theme support** - Both Material and Unstyled implementations are required

4. **NEVER use star imports** - Always use explicit imports (enforced by .editorconfig)

5. **NEVER access ViewModel.state directly** - Use `collectAsStateWithLifecycle()` for lifecycle-aware state consumption

6. **NEVER put navigation callbacks in UI state** - Pass callbacks as parameters to composable functions

## Quick Reference

### Validation Commands

| Command | Purpose | When to Run |
| --- | --- | --- |
| `./gradlew :composeApp:assembleDebug test --continue` | Primary validation (Android build + all tests) | Always, before committing |
| `./gradlew :composeApp:run` | Run desktop app | Local UI development |
| `./gradlew :features:<feature>:ui-material:testDebugUnitTest` | Run UI tests for Material variant | After adding UI tests |

### Token Reference

| Token Category | Access | Example |
| --- | --- | --- |
| Spacing | `MaterialTheme.tokens.spacing.medium` | 16.dp |
| Elevation | `MaterialTheme.tokens.elevation.level2` | 3.dp |
| Shapes | `MaterialTheme.tokens.shapes.large` | 24.dp corner |
| Motion | `MaterialTheme.tokens.motion.durationMedium` | 300ms |

### Preview Example Template

```kotlin
@Preview(name = "<State/Component Name>")
@Composable
private fun <Feature><State>Preview() {
    PokemonTheme {
        Surface {
            // Component or Content composable
        }
    }
}
```

### Component Token Usage

```kotlin
Card(tokens = MaterialTheme.componentTokens.card())
TypeBadge(tokens = MaterialTheme.componentTokens.badge())
AnimatedStatBar(tokens = MaterialTheme.componentTokens.progressBar())
```

## Cross-References

| Document | Purpose | Link |
| --- | --- | --- |
| Architecture + conventions | Master reference for modules, DI, vertical slicing | [conventions.md](../../../docs/tech/conventions.md) |
| Design tokens | Token system (spacing, elevation, shapes, motion) | [design_tokens.md](../../../docs/tech/design_tokens.md) |
| Critical patterns | 6 core patterns (ViewModel, Either, Impl+Factory) | [critical_patterns_quick_ref.md](../../../docs/tech/critical_patterns_quick_ref.md) |
| Navigation 3 | Modular navigation architecture | [navigation.md](../../../docs/tech/navigation.md) |
| Testing strategy | @Preview requirements and UI testing | [testing_strategy.md](../../../docs/tech/testing_strategy.md) |
| Animation guides | Creative UI animations and motion | [ui-ux-designer skill](../../ui-ux-designer/SKILL.md) |
| Product requirements | Feature acceptance criteria | [prd.md](../../../docs/project/prd.md) |

### Reference Implementations

**Pokemon List (Material):**
- [PokemonListMaterialScreen.kt](features/pokemonlist/ui-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/ui/material/PokemonListMaterialScreen.kt)
- [PokemonListMaterialScreenPreviews.kt](features/pokemonlist/ui-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/ui/material/PokemonListMaterialScreenPreviews.kt)

**Pokemon List (Unstyled):**
- [PokemonListUnstyledScreen.kt](features/pokemonlist/ui-unstyled/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/ui/unstyled/PokemonListUnstyledScreen.kt)
- [PokemonListUnstyledScreenPreviews.kt](features/pokemonlist/ui-unstyled/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/ui/unstyled/PokemonListUnstyledScreenPreviews.kt)

**Pokemon Detail (Material with animations):**
- [PokemonDetailMaterialScreen.kt](features/pokemondetail/ui-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/ui/material/PokemonDetailMaterialScreen.kt)
- [PokemonDetailMaterialScreenPreviews.kt](features/pokemondetail/ui-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/ui/material/PokemonDetailMaterialScreenPreviews.kt)
