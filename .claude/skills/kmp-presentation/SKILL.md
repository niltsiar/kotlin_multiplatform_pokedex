---
name: kmp-presentation
description: "Kotlin Multiplatform presentation layer patterns for ViewModels, UI state management, SavedStateHandle, and coroutine integration. Use when: (1) Creating ViewModels with proper lifecycle management, (2) Implementing UiStateHolder pattern, (3) Managing SavedStateHandle for state persistence, (4) Handling one-time events with EventChannel, (5) Configuring viewModelScope and coroutine patterns"
---

# KMP Presentation Layer Skill

Patterns for implementing ViewModels and UI state management in Kotlin Multiplatform with lifecycle awareness and proper coroutine handling.

## When to Use This Skill

**MANDATORY**: Load this skill when working on:
- Creating or modifying ViewModels in `:features:<feature>:presentation`
- Implementing UiStateHolder pattern for UI state management
- Using SavedStateHandle for configuration change persistence
- Handling one-time events (snackbars, navigation, toasts)
- Configuring viewModelScope and coroutine patterns

**Do NOT use for**: Repository implementation → use @kmp-data-layer, DI configuration → use @kmp-di, Navigation setup → use @kmp-navigation

## Critical Patterns (Read First)

### ViewModel Core Pattern

**NEVER do work in `init` block. Always use `onStart()` for lifecycle-aware initialization.**

```kotlin
class HomeViewModel(
    private val repository: HomeRepository,
    private val savedStateHandle: SavedStateHandle,
    viewModelScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    ),
) : ViewModel(viewModelScope),
    DefaultLifecycleObserver,
    UiStateHolder<HomeUiState, HomeUiEvent> {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    override val uiState: StateFlow<HomeUiState> = _uiState

    // ✅ CORRECT: Lifecycle-aware initialization
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.loadItems().fold(
                ifLeft = { _uiState.value = HomeUiState.Error(it.message) },
                ifRight = { _uiState.value = HomeUiState.Content(it.toImmutableList()) }
            )
        }
    }
}
```

**Key Requirements**:
1. Pass `viewModelScope` to constructor (NOT stored as field)
2. Implement `DefaultLifecycleObserver` for lifecycle awareness
3. **NO work in `init`** — use `onStart()` instead
4. Use `kotlinx.collections.immutable` for collections in state

### UiStateHolder Pattern

```kotlin
// Generic interface all ViewModels should implement
interface UiStateHolder<S, E> {
    val uiState: StateFlow<S>
    fun onUiEvent(event: E)
}

// UI State as sealed interface
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Error(val message: String) : HomeUiState
    data class Content(val items: ImmutableList<Item>) : HomeUiState
}

// UI Events as sealed interface
sealed interface HomeUiEvent {
    data object Refresh : HomeUiEvent
    data class ItemClicked(val id: String) : HomeUiEvent
}
```

## Reference Loading Guide

| Task | Reference | Load When |
|------|-----------|-----------|
| ViewModel patterns & examples | [viewmodel-patterns.md](references/viewmodel-patterns.md) | Creating new ViewModels |
| SavedStateHandle usage | [savedstatehandle.md](references/savedstatehandle.md) | Adding state persistence |
| Coroutine scopes & patterns | [coroutines.md](references/coroutines.md) | Configuring coroutines |
| One-time events | [onetime-events.md](references/onetime-events.md) | Handling navigation/snackbars |

## Architecture Overview

### Module Location

```
:features:<feature>:presentation/src/commonMain/kotlin/...
├── ViewModel.kt              # UiStateHolder implementation
├── UiState.kt                # State sealed interface
├── UiEvent.kt                # Event sealed interface
└── OneTimeEvent.kt           # Navigation/events (optional)
```

**Shared across ALL platforms**: Android, iOS (via `:shared`), Desktop

### Key Interfaces

| Interface | Purpose |
|-----------|---------|
| `UiStateHolder<S, E>` | Contract for UI state and event handling |
| `OneTimeEventEmitter<E>` | Emits one-time events (navigation, toasts) |
| `DefaultLifecycleObserver` | Lifecycle-aware initialization |

## Parametric ViewModels (With Parameters)

For ViewModels requiring constructor parameters (e.g., ID for detail screens):

```kotlin
// ViewModel
class PokemonDetailViewModel(
    private val pokemonId: Int,  // Parameter passed via Koin
    private val repository: PokemonDetailRepository,
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob())
) : ViewModel(viewModelScope), UiStateHolder<...> {
    // Load data automatically
    init { loadPokemon() }
}

// Koin wiring
factory { params ->
    PokemonDetailViewModel(
        pokemonId = params.get(),
        repository = get()
    )
}

// Compose injection
val viewModel = koinViewModel { parametersOf(pokemonId) }
```

**MANDATORY**: Read [viewmodel-patterns.md](references/viewmodel-patterns.md) for complete parametric ViewModel patterns including Navigation 3 key handling.

## Related Skills

| Skill | Use For |
|-------|---------|
| @kmp-architecture | Module structure and vertical slicing |
| @kmp-di | Koin configuration and wiring |
| @kmp-data-layer | Repository patterns with Either |
| @compose-screen | Compose UI implementation |
| @swiftui-screen | SwiftUI consuming KMP ViewModels |

## Documentation Sources

| Document | Purpose | Tokens |
|----------|---------|--------|
| [presentation_layer.md](../../../docs/tech/presentation_layer.md) | Complete presentation guide | ~4000 |
| [coroutines.md](../../../docs/tech/coroutines.md) | Scopes and dispatchers | ~600 |
| [viewmodel_patterns.md](../../../docs/patterns/viewmodel_patterns.md) | Extended examples | ~3500 |

## Quick Reference

### ViewModel Checklist

- [ ] Pass `viewModelScope` to constructor (not stored as field)
- [ ] Implement `DefaultLifecycleObserver` for lifecycle awareness
- [ ] **NO work in `init`** — use `onStart()` instead
- [ ] Implement `UiStateHolder<S, E>` interface
- [ ] Use `ImmutableList` for collections in state
- [ ] Handle repository `Either` results with `fold()`
- [ ] For one-time events, delegate to `EventChannel<E>`

### Anti-Patterns to Avoid

| ❌ DON'T | ✅ DO |
|----------|-------|
| Store `CoroutineScope` as field | Pass to `ViewModel()` constructor |
| Work in `init` block | Use `onStart()` for initialization |
| `List<T>` in UI state | `ImmutableList<T>` |
| Direct repository calls in Composable | Use ViewModel with lifecycle |

### Validation Commands

```bash
# Build and test
./gradlew :composeApp:assembleDebug test --continue

# Run specific ViewModel tests
./gradlew :features:<feature>:presentation:testDebugUnitTest
```

### Reference Implementations

- `features/pokemonlist/presentation/PokemonListViewModel.kt` — Pagination
- `features/pokemondetail/presentation/PokemonDetailViewModel.kt` — Parametric
