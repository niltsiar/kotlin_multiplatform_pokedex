---
name: kmp-presentation
description: "Kotlin Multiplatform presentation layer patterns for ViewModels, UI state management, SavedStateHandle, and coroutine integration. Use when: (1) Creating ViewModels with proper lifecycle management, (2) Implementing UiStateHolder pattern, (3) Managing SavedStateHandle for state persistence, (4) Handling one-time events with EventChannel, (5) Configuring viewModelScope and coroutine patterns"
---

# KMP Presentation Layer Skill

Patterns for implementing ViewModels and UI state management in Kotlin Multiplatform with lifecycle awareness and proper coroutine handling.

## Mode Detection

| User Request | Reference File | Load When |
|--------------|----------------|-----------|
| "Create ViewModel" / "Implement ViewModel" | [viewmodel-patterns.md](references/viewmodel-patterns.md) | MANDATORY - Read before implementing |
| "SavedStateHandle" / "State persistence" | [savedstatehandle.md](references/savedstatehandle.md) | MANDATORY - Read before implementing |
| "Configure coroutines" / "viewModelScope" | [coroutines.md](references/coroutines.md) | MANDATORY - Read before implementing |
| "One-time events" / "Navigation events" | [onetime-events.md](references/onetime-events.md) | MANDATORY - Read before implementing |

**MANDATORY - READ ENTIRE FILE**: Before implementing ViewModels, you MUST read [viewmodel-patterns.md](references/viewmodel-patterns.md) (~437 lines) for complete lifecycle-aware and parametric patterns.

**MANDATORY - READ ENTIRE FILE**: Before implementing state persistence, you MUST read [savedstatehandle.md](references/savedstatehandle.md) (~407 lines) for SavedStateHandle delegate patterns.

**MANDATORY - READ ENTIRE FILE**: Before configuring coroutines, you MUST read [coroutines.md](references/coroutines.md) (~448 lines) for scope and dispatcher patterns.

**Do NOT load** `onetime-events.md` unless handling navigation or one-time UI events.

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

## Coroutine Patterns

**Key principles**:
- Pass `viewModelScope` to `ViewModel()` constructor (NOT stored as field)
- Use `SupervisorJob()` so one failure doesn't cancel siblings
- Use `Dispatchers.Main.immediate` for immediate UI updates
- NEVER use `GlobalScope` — always use structured scopes
- NEVER catch `CancellationException` — use `Either.catch { }` which respects cancellation

For detailed coroutine patterns (scopes, dispatchers, testing, Arrow integration), see [coroutines.md](references/coroutines.md).
- **backgroundScope**: Use for repository/data work. Inject the `ioDispatcher` for testability.
- **ApplicationScope**: For jobs that must outlive screens (e.g., caches, analytics). Provide via DI.

### Dispatchers

- **Inject Dispatchers**: Depend on abstractions (e.g., `DispatchersProvider`) to improve testability.
- **Dispatchers.IO**: Use for blocking IO or network-bound work.
- **Dispatchers.Default**: Confine CPU-heavy work here.

### Repositories

- Expose `suspend` functions and `Flow`s. Perform IO using `backgroundScope`.
- Use `withContext(ioDispatcher)` around discrete IO when a new scope is not required.
- For long-running operations that should continue across screens, delegate to `ApplicationScope`.

### Structured Concurrency

- Prefer structured concurrency; **NEVER** use `GlobalScope`.
- Use `SupervisorJob` for scopes handling independent child coroutines so one failure doesn’t cancel siblings.

### Cancellation & Timeouts

- **NEVER** catch and swallow `CancellationException`.
- Use Arrow `Either.catch { ... }` which respects coroutine cancellation.
- Propagate `coroutineContext` to Ktor/SQL drivers to make calls cancellable.

### Testing

- Inject dispatchers and scopes; in unit tests, use `StandardTestDispatcher` and `TestScope`.
- Avoid real delays; use `TestCoroutineScheduler` to advance time.

### Arrow Patterns in Suspend Code

- At repository boundaries, wrap throwing blocks with `Either.catch { ... }` and map exceptions.
- Inside repositories or use cases, prefer Arrow monad comprehensions:
  ```kotlin
  val result: Either<Error, Domain> = either {
      val a = repo.stepA().bind()
      val b = repo.stepB(a).bind()
      combine(a, b)
  }
  ```

## Decision Framework

Before implementing ViewModels, ask yourself:

1. **What state needs management?**
   - Loading state → `UiState.Loading` sealed class
   - Success state → `UiState.Content(data)` with ImmutableList
   - Error state → `UiState.Error(message)`
   - One-time events (navigation, snackbar) → `Channel` + `receiveAsFlow()`

2. **What needs to survive process death?**
   - User selections, form input → `by saved` delegate with SavedStateHandle
   - Transient UI state (loading spinners) → Regular MutableStateFlow
   - Navigation state → Already handled by Navigation 3

3. **When should initialization happen?**
   - Data loading on screen appear → `onStart()` lifecycle callback
   - NEVER in `init` block → Screen not attached yet
   - Background sync → Use `ApplicationScope` via DI

## Essential Workflows

For detailed ViewModel implementation patterns, see reference files:
- **Lifecycle-aware ViewModels**: [viewmodel-patterns.md](references/viewmodel-patterns.md) — Complete examples with `onStart()`, state management, and lifecycle integration
- **SavedStateHandle persistence**: [savedstatehandle.md](references/savedstatehandle.md) — Delegate patterns and serialization
- **One-time events**: [onetime-events.md](references/onetime-events.md) — EventChannel patterns for navigation and snackbars
- **Coroutine configuration**: [coroutines.md](references/coroutines.md) — Scopes, dispatchers, testing

**Quick workflow summary**:
1. Extend `ViewModel(viewModelScope)` + implement `DefaultLifecycleObserver`
2. Pass `SavedStateHandle` and `viewModelScope` to constructor
3. Override `onStart()` for initialization (NEVER use `init` block)
4. Use `by saved` delegate for state persistence
5. Use `Channel` + `receiveAsFlow()` for one-time events
6. Register with Koin using `viewModel { params -> ... }` for parametric ViewModels

## Critical Guardrails

1. NEVER do work in `init` block → override `onStart(owner: LifecycleOwner)` instead (lifecycle-aware initialization).
2. NEVER store `CoroutineScope` as field → pass `viewModelScope` to constructor with default value (prevents leaks).
3. NEVER use nullable UI state (`T?`) → use sealed class hierarchy with Loading/Content/Error states.
4. NEVER directly expose `MutableStateFlow` → expose as `StateFlow` via `.asStateFlow()`.
5. NEVER swallow `CancellationException` → respect coroutine cancellation (Either.catch does this automatically).
6. NEVER use `GlobalScope` or `CoroutineScope(Dispatchers.Main)` → always use `viewModelScope` parameter.
7. NEVER skip `by saved` delegate for restorable state → always use SavedStateHandle for process death survival.
8. NEVER emit events to `StateFlow` → use `Channel` + `receiveAsFlow()` for one-time events (navigation, snackbars).

## Cross-References

### Skills (by Category)

**Layer Implementation**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-mobile-expert | Mobile ViewModels, repositories, iOS integration | [SKILL.md](../kmp-mobile-expert/SKILL.md) |
| @kmp-data-layer | Repository patterns feeding ViewModels | [SKILL.md](../kmp-data-layer/SKILL.md) |
| @kmp-domain | Domain models used in UI state | [SKILL.md](../kmp-domain/SKILL.md) |
| @kmp-di | Koin ViewModel registration and parametric injection | [SKILL.md](../kmp-di/SKILL.md) |

**Platform**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-desktop | Desktop (JVM) SavedStateHandle setup | [SKILL.md](../kmp-desktop/SKILL.md) |
| @kmp-ios | iOS SwiftUI consuming KMP ViewModels | [SKILL.md](../kmp-ios/SKILL.md) |

**Navigation & Testing**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-navigation | Navigation events and ViewModel navigation patterns | [SKILL.md](../kmp-navigation/SKILL.md) |
| @kmp-testing-patterns | ViewModel testing with Turbine and TestScope | [SKILL.md](../kmp-testing-patterns/SKILL.md) |

### Documents

| Document | Purpose | Link |
| --- | --- | --- |
| ViewModel architecture | Master reference for ViewModel patterns | [@kmp-architecture](../kmp-architecture/SKILL.md) |
| Critical patterns | 6 core patterns including ViewModel lifecycle | [@kmp-critical-patterns](../kmp-critical-patterns/SKILL.md) |
| Dependency injection | Koin ViewModel registration patterns | [@kmp-di](../kmp-di/SKILL.md) |

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
