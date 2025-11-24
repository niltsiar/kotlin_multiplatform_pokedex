# Presentation Layer Guidelines

Purpose: Establish consistent patterns for UI architecture, state management, and component design in Compose Multiplatform projects.

## Location and Structure
- Feature‑local presentation code lives under `:features:<feature>:presentation/src/commonMain/kotlin/com/<org>/<app>/presentation/<feature>/...`
- Organize by feature: `presentation/screens/<feature>/` (e.g., `home/`, `paywall/`, `onboarding/`)
- Shared components: prefer a shared module (e.g., `:core:designsystem`) or feature‑local `presentation/components/`
- Screen‑level route contracts live in `:features:<feature>:api`; screen implementations/composables live in `:features:<feature>:presentation`

## Screen Architecture Pattern

### UiStateHolder Pattern (as an interface)
Define a small interface that viewmodels implement. Repositories return Arrow `Either`, so handle both Left and Right paths near the boundary and map to UI state.

Recommended interfaces
```kotlin
// Generic UI state holder interface
interface UiStateHolder<S, E> {
    val uiState: StateFlow<S>
    fun onUiEvent(event: E)
}

// One-time events (snackbars, toasts, navigations). Backed by a Channel.
// Includes a suspend emit(event) to enable clean delegation.
interface OneTimeEventEmitter<E> {
    val events: Flow<E>
    suspend fun emit(event: E)
}

// Simple helper you can reuse per feature (optional)
class EventChannel<E> : OneTimeEventEmitter<E> {
    private val channel = Channel<E>(capacity = Channel.BUFFERED)
    override val events: Flow<E> = channel.receiveAsFlow()
    override suspend fun emit(event: E) = channel.send(event)
}
```

ViewModels rules (required)
- All ViewModels must extend `androidx.lifecycle.ViewModel` (KMP).
- Do not perform work in `init` blocks. Be lifecycle‑aware and start work in lifecycle callbacks (or Compose effects) instead.
- Do NOT store a `CoroutineScope` field. Instead, pass a custom scope to the `ViewModel` superclass constructor (e.g., `CloseableCoroutineScope`) and use `viewModelScope` internally.
- Use `SavedStateHandle` when necessary to persist screen state/inputs across configuration changes or process death (Android fully supported).
- Do not expose mutable collections; prefer `kotlinx.collections.immutable` types (`ImmutableList`, `ImmutableMap`, etc.).
- For one‑time events, implement `OneTimeEventEmitter<E>` by delegation to `EventChannel<E>`.

Canonical helpers (place in `:core:util` commonMain)
```kotlin
class CloseableCoroutineScope(
  context: CoroutineContext = SupervisorJob() + Dispatchers.Main.immediate
) : Closeable, CoroutineScope {
  override val coroutineContext: CoroutineContext = context
  override fun close() { coroutineContext.cancel() }
}
```

Example ViewModel with lifecycle start and SavedStateHandle
```kotlin
class HomeViewModel(
    private val repository: Repository,
    private val savedStateHandle: SavedStateHandle,
    customScope: CloseableCoroutineScope = CloseableCoroutineScope(),
) : ViewModel(customScope),
    UiStateHolder<HomeUiState, HomeUiEvent>,
    OneTimeEventEmitter<HomeOneShotEvent> by EventChannel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    override val uiState: StateFlow<HomeUiState> = _uiState

    override fun onUiEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.Refresh -> refresh()
            is HomeUiEvent.ItemClicked -> viewModelScope.launch {
                emit(HomeOneShotEvent.NavigateToDetail(event.id))
            }
        }
    }

    fun start(lifecycle: Lifecycle) {
        viewModelScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                _uiState.value = HomeUiState.Loading
                repository.loadItems().fold(
                    ifLeft = { _uiState.value = HomeUiState.Error(mapError(it)) },
                    ifRight = { items -> _uiState.value = HomeUiState.Content(items.map(::toUi).toImmutableList()) }
                )
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            repository.loadItems().fold(
                ifLeft = { _uiState.value = HomeUiState.Error(mapError(it)) },
                ifRight = { _uiState.value = HomeUiState.Content(it.map(::toUi).toImmutableList()) }
            )
        }
    }
}
```

### No empty use cases
- Avoid overengineering. If a screen only needs to call a single repository method and apply simple mapping to UI state, call the repository directly from the `UiStateHolder`. Do not introduce a pass‑through use case.

Minimal example without a use case
```kotlin
class ProfileViewModel(
    private val userRepository: UserRepository,
    customScope: CloseableCoroutineScope = CloseableCoroutineScope(),
) : ViewModel(customScope), UiStateHolder<ProfileUiState, ProfileUiEvent> {
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    override val uiState: StateFlow<ProfileUiState> = _uiState

    fun load(userId: String) = viewModelScope.launch {
        _uiState.value = ProfileUiState.Loading
        userRepository.getUser(userId).fold(
            ifLeft = { _uiState.value = ProfileUiState.Error(mapError(it)) },
            ifRight = { user -> _uiState.value = ProfileUiState.Content(user.toUi()) }
        )
    }
    override fun onUiEvent(event: ProfileUiEvent) { /* handle */ }
}
```

### Screen Composables
Follow function overloading pattern for flexibility and collect one‑time events:

```kotlin
// Main entry point with StateHolder interface
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiStateHolder: UiStateHolder<HomeUiState, HomeUiEvent>,
    onNavigate: (destination: Any) -> Unit
) {
    val uiState by uiStateHolder.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Collect one-time events if the holder also implements OneTimeEventEmitter
    (uiStateHolder as? OneTimeEventEmitter<HomeOneShotEvent>)?.let { emitter ->
        LaunchedEffect(emitter) {
            emitter.events.collect { event ->
                when (event) {
                    is HomeOneShotEvent.NavigateToDetail -> onNavigate(event.id)
                    is HomeOneShotEvent.ShowMessage -> {/* show snackbar */}
                }
            }
        }
    }

    HomeScreen(
        modifier = modifier,
        uiState = uiState,
        onUiEvent = uiStateHolder::onUiEvent,
        onNavigate = onNavigate
    )
}

// Pure composable for testing and previews
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onUiEvent: (HomeUiEvent) -> Unit,
    onNavigate: (destination: Any) -> Unit
) {
    when (uiState) {
        is HomeUiState.Loading -> Loading()
        is HomeUiState.Error -> ErrorView(message = uiState.message) { onUiEvent(HomeUiState.DefaultRetryEvent) }
        is HomeUiState.Content -> ContentList(uiState.items, onUiEvent)
    }
}
```

## State Management

### UI State Classes
- Use immutable data classes for UI state
- Provide meaningful defaults for easy construction
- Use immutable collections from `kotlinx.collections.immutable`

```kotlin
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Error(val message: String) : HomeUiState
    data class Content(val items: ImmutableList<ItemUiState>) : HomeUiState

    companion object {
        val DefaultRetryEvent = HomeUiEvent.Refresh
    }
}
```

### UI Events
Use sealed classes/interfaces for user interactions:

```kotlin
sealed interface HomeUiEvent {
    data object Refresh : HomeUiEvent
    data class ItemClicked(val id: String) : HomeUiEvent
    data class FilterChanged(val filter: FilterType) : HomeUiEvent
}
```

## Component Design

### Reusable Components
- Keep components small and focused on single responsibility
- Use `modifier: Modifier = Modifier` parameter for all custom composables
- Provide meaningful parameter names, avoid generic names like `onClick`

```kotlin
@Composable
fun VideoCardItem(
    title: String,
    thumbnailUrl: String?,
    onPlayClick: () -> Unit,
    onMoreOptionsClick: () -> Unit,
    modifier: Modifier = Modifier
) { /* ... */ }
```

### Component Organization
- Group related components in dedicated packages (feature‑local):
  - `presentation/components/video/` – video related
  - `presentation/components/premium/` – premium/subscription
  - `presentation/components/ads/` – ads

### Design System Integration
- Use components from the `designsystem` module when available.
- Keep presentation components generic and parameterized.

## Performance Considerations

### State Management
- Use `remember` for expensive calculations.
- Provide stable keys for Lazy lists.
- Avoid unnecessary recomposition with stable parameters and immutable state.

## Navigation 3 integration (Compose Multiplatform)
- We standardize on Navigation 3. Feature routes/entries live in `:features:<feature>:api`; composables live in `:features:<feature>:presentation`.
- Use a back stack model (e.g., `rememberNavBackStack(startKey)`) and drive UI by adding/removing keys.
- `SavedStateHandle` can be used to save input state and restore across configuration/process death.

Example (receiving nav args via SavedStateHandle)
```kotlin
class ProfileViewModel(
  private val repo: UserRepository,
  private val savedStateHandle: SavedStateHandle,
  customScope: CloseableCoroutineScope = CloseableCoroutineScope(),
) : ViewModel(customScope), UiStateHolder<ProfileUiState, ProfileUiEvent> {
  // assume "userId" is provided by the entry/route
  private val userId: String = checkNotNull(savedStateHandle.get<String>("userId"))
  private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
  override val uiState: StateFlow<ProfileUiState> = _uiState

  override fun onUiEvent(event: ProfileUiEvent) { /* ... */ }

  fun start(lifecycle: Lifecycle) {
    viewModelScope.launch {
      lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
        repo.getUser(userId).fold(
          ifLeft = { _uiState.value = ProfileUiState.Error(mapError(it)) },
          ifRight = { user -> _uiState.value = ProfileUiState.Content(user.toUi()) }
        )
      }
    }
  }
}
```

## Alignment with Product Docs
- Map UI states to user flows from `.junie/guides/project/user_flow.md`
- Implement copy and content from `.junie/guides/project/prd.md`
- Follow UX specifications from `.junie/guides/project/ui_ux.md`
- Handle premium states according to `.junie/guides/project/paywall.md`
