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

## Coroutine Patterns

Guidelines to ensure coroutine usage is testable, predictable, and aligned with platform lifecycles.

### Scopes

- **viewModelScope**: All ViewModels must pass `viewModelScope` to the `ViewModel` superclass constructor.
  ```kotlin
  class MyViewModel(
      viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
  ) : ViewModel(viewModelScope) {
      fun doSomething() = viewModelScope.launch { /* ... */ }
  }
  ```
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

## Essential Workflows

### Workflow 1: Create Lifecycle-Aware ViewModel

1. **Define class**: Extend `ViewModel(viewModelScope)` and implement `DefaultLifecycleObserver`.
2. **Inject dependencies**: Pass `SavedStateHandle` and `viewModelScope` (with default value) to constructor.
3. **Handle initialization**: Override `onStart()` for data loading. NEVER use `init` block for work.
4. **Expose state**: Use `MutableStateFlow` (internal) and `asStateFlow()` (public).

```kotlin
class HomeViewModel(
    private val repository: HomeRepository,
    private val savedStateHandle: SavedStateHandle,
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) : ViewModel(viewModelScope), DefaultLifecycleObserver, UiStateHolder<HomeUiState, HomeUiEvent> {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    override val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getData().fold(
                ifLeft = { _uiState.value = HomeUiState.Error(it.message) },
                ifRight = { _uiState.value = HomeUiState.Content(it.toImmutableList()) }
            )
        }
    }
}
```
*Cross-reference: @kmp-mobile-expert for mobile-specific patterns.*

### Workflow 2: Implement SavedStateHandle Persistence

1. **Annotate state**: Ensure state data classes are marked with `@Serializable`.
2. **Inject handle**: Pass `SavedStateHandle` to ViewModel constructor.
3. **Use delegate**: Declare state properties using the `by saved` delegate for automatic persistence.

```kotlin
@Serializable
data class HomeState(val query: String = "", val filter: String = "All")

class HomeViewModel(
    private val savedStateHandle: SavedStateHandle,
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob())
) : ViewModel(viewModelScope) {
    // State is automatically saved on every property write
    private var state by savedStateHandle.saved { HomeState() }

    fun updateQuery(newQuery: String) {
        state = state.copy(query = newQuery)
    }
}
```
*Cross-reference: @kmp-desktop for JVM-specific SavedStateHandle setup.*

### Workflow 3: Handle One-Time Events with EventChannel

1. **Define events**: Use a sealed interface for navigation, snackbars, or toasts.
2. **Create channel**: Use `Channel<E>(Channel.BUFFERED)` for emission.
3. **Expose flow**: Use `receiveAsFlow()` for consumption in UI.
4. **Consume in UI**: Use `LaunchedEffect` to collect events.

```kotlin
sealed interface HomeOneTimeEvent {
    data class NavigateToDetail(val id: Int) : HomeOneTimeEvent
}

class HomeViewModel(...) : ViewModel(...), OneTimeEventEmitter<HomeOneTimeEvent> {
    private val eventChannel = Channel<HomeOneTimeEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    fun onUserClick(id: Int) {
        viewModelScope.launch { eventChannel.send(HomeOneTimeEvent.NavigateToDetail(id)) }
    }
}

// In UI
LaunchedEffect(viewModel) {
    viewModel.events.collect { event -> /* handle event */ }
}
```
*Cross-reference: @kmp-navigation for navigation event patterns.*

### Workflow 4: Configure Parametric ViewModels with Koin

1. **Inject parameters**: Add parameters (e.g., `id`) to ViewModel constructor before repositories.
2. **Register in Koin**: Use `viewModel { params -> ... }` and `params.get()`.
3. **Inject in Compose**: Use `koinViewModel { parametersOf(id) }`.

```kotlin
// Wiring module
val detailModule = module {
    viewModel { params -> DetailViewModel(id = params.get(), repository = get()) }
}

// Compose Screen
val viewModel = koinViewModel<DetailViewModel> { parametersOf(route.id) }
```
*Cross-reference: @kmp-di for advanced Koin patterns.*

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
