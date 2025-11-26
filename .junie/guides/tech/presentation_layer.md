# Presentation Layer Guidelines

Purpose: Establish consistent patterns for UI architecture, state management, and component design in Compose Multiplatform projects.

## Location and Structure
- Feature‑local presentation code lives in `:features:<feature>:presentation/src/commonMain/kotlin/com/<org>/<app>/...`
- **ViewModels and UI state are KMP and shared across ALL platforms** (Android, Desktop, iOS)
- Organize by feature: ViewModels, UI state, event definitions
- **Compose UI screens live separately** in `:features:<feature>:ui` modules (Android + JVM + iOS Compose)
- Screen‑level route contracts live in `:features:<feature>:api` (plain data classes/objects for Navigation 3)
- Shared Compose components: prefer a shared module (e.g., `:core:designsystem`) or feature‑local components in `:ui` module

**Module Separation**:
```
:features:<feature>:presentation/  → ViewModels, UiState (KMP - exported to native iOS via :shared)
:features:<feature>:ui/            → Compose UI, @Composable functions (Android + JVM + iOS Compose)
```

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
- Do NOT store a `CoroutineScope` field. Instead, pass a `viewModelScope` parameter to the `ViewModel` superclass constructor with a default value of `CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)` and use `viewModelScope` internally.
- Use `SavedStateHandle` when necessary to persist screen state/inputs across configuration changes or process death (Android fully supported).
- Do not expose mutable collections; prefer `kotlinx.collections.immutable` types (`ImmutableList`, `ImmutableMap`, etc.).
- For one‑time events, implement `OneTimeEventEmitter<E>` by delegation to `EventChannel<E>`.

Example ViewModel with lifecycle start and SavedStateHandle
```kotlin
class HomeViewModel(
    private val repository: Repository,
    private val savedStateHandle: SavedStateHandle,
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) : ViewModel(viewModelScope),
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
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) : ViewModel(viewModelScope), UiStateHolder<ProfileUiState, ProfileUiEvent> {
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

### Parametric ViewModels

**Pattern**: ViewModels that need constructor parameters (e.g., `pokemonId`, `userId`) for initialization.

#### Why Parametric ViewModels?

Some screens require context to load data:
- **Detail screens** need an ID to fetch specific item
- **Edit screens** need initial data to populate form
- **Filtered lists** need filter criteria

**Anti-pattern**: Loading data from navigation state in Compose
```kotlin
// ❌ DON'T: Passing data through navigation and loading in Composable
@Composable
fun PokemonDetailScreen(pokemonId: Int) {
    val viewModel: PokemonDetailViewModel = koinInject()
    LaunchedEffect(pokemonId) {
        viewModel.loadPokemon(pokemonId)  // Loads on every recomposition
    }
}
```

**Correct pattern**: Pass parameter to ViewModel constructor
```kotlin
// ✅ DO: ViewModel initialized with parameter
class PokemonDetailViewModel(
    private val pokemonId: Int,
    private val repository: PokemonDetailRepository,
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) : ViewModel(viewModelScope), UiStateHolder<PokemonDetailUiState, PokemonDetailUiEvent> {
    
    private val _uiState = MutableStateFlow<PokemonDetailUiState>(PokemonDetailUiState.Loading)
    override val uiState: StateFlow<PokemonDetailUiState> = _uiState
    
    // Load on first collection (lifecycle-aware)
    init {
        loadPokemon()
    }
    
    private fun loadPokemon() {
        viewModelScope.launch {
            _uiState.value = PokemonDetailUiState.Loading
            repository.getPokemonById(pokemonId).fold(
                ifLeft = { error -> 
                    _uiState.value = PokemonDetailUiState.Error(error.message)
                },
                ifRight = { pokemon -> 
                    _uiState.value = PokemonDetailUiState.Content(pokemon)
                }
            )
        }
    }
    
    fun retry() {
        loadPokemon()
    }
    
    override fun onUiEvent(event: PokemonDetailUiEvent) {
        when (event) {
            is PokemonDetailUiEvent.Retry -> retry()
        }
    }
}
```

#### Koin DI with parametersOf

**Wiring module** (`:features:pokemondetail:wiring`):
```kotlin
val pokemonDetailModule = module {
    factory { params ->
        PokemonDetailViewModel(
            pokemonId = params.get(),  // Extract Int parameter
            repository = get()         // Resolve from Koin
        )
    }
}
```

**Injection in Compose** (`:features:pokemondetail:ui`):
```kotlin
@Composable
fun PokemonDetailScreen(
    pokemonId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Inject with parametersOf
    val viewModel: PokemonDetailViewModel = koinInject { parametersOf(pokemonId) }
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    PokemonDetailContent(
        uiState = uiState,
        onEvent = viewModel::onUiEvent,
        onBack = onBack,
        modifier = modifier
    )
}
```

**Navigation registration** (`:features:pokemondetail:wiring/androidMain`):
```kotlin
internal fun pokemonDetailNavigationProvider(
    navigator: Navigator
): EntryProviderInstaller = {
    entry<PokemonDetail>(
        metadata = NavDisplay.transitionSpec(
            slideInHorizontally() + fadeIn()
        )
    ) { key ->
        PokemonDetailScreen(
            pokemonId = key.id,  // Extract from route
            onBack = { navigator.goBack() }
        )
    }
}
```

**Complete Flow**:
1. Navigation passes route: `PokemonDetail(id = 25)`
2. `entry<PokemonDetail> { key -> }` extracts `key.id`
3. Screen receives `pokemonId` parameter
4. `koinInject { parametersOf(pokemonId) }` passes to Koin
5. Koin factory receives: `params.get()` extracts Int
6. ViewModel initialized with `pokemonId` and `repository`
7. ViewModel loads data automatically in `init`

#### iOS Integration

> **Current Pattern**: Direct Integration (see `.junie/guides/tech/ios_integration.md` for complete guide)
> **Alternative**: Wrapper Pattern available for complex apps with state preservation needs

**Kotlin helper function** (`shared/src/iosMain/kotlin/KoinIos.kt`):
```kotlin
fun getPokemonDetailViewModel(pokemonId: Int): PokemonDetailViewModel {
    return KoinPlatform.getKoin().get { parametersOf(pokemonId) }
}
```

**SwiftUI view - Direct Integration** (`iosApp/Views/PokemonDetailView.swift`):
```swift
struct PokemonDetailView: View {
    let pokemonId: Int
    private var viewModel: PokemonDetailViewModel
    @State private var uiState: PokemonDetailUiState = PokemonDetailUiStateLoading()
    
    init(pokemonId: Int) {
        self.pokemonId = pokemonId
        // Get ViewModel directly from Koin with parameter
        viewModel = KoinIosKt.getPokemonDetailViewModel(pokemonId: Int32(pokemonId))
    }
    
    var body: some View {
        content
            .task {
                // Observe StateFlow via SKIE AsyncSequence
                for await state in viewModel.uiState {
                    self.uiState = state
                }
            }
    }
    
    @ViewBuilder
    private var content: some View {
        switch uiState {
        case is PokemonDetailUiStateLoading:
            ProgressView("Loading...")
        case let content as PokemonDetailUiStateContent:
            DetailContent(pokemon: content.pokemon)
        case let error as PokemonDetailUiStateError:
            ErrorView(message: error.message, onRetry: { wrapper.retry() })
        default:
            EmptyView()
        }
    }
    .task {
        await wrapper.observeState()
    }
}
```

#### Key Patterns

**When to use parametric ViewModels**:
- ✅ Detail screens (ID parameter)
- ✅ Edit screens (initial data)
- ✅ Search/filter screens (query parameter)
- ✅ Parameterized lists (category, user ID)

**When NOT to use**:
- ❌ List screens with no filter (use simple ViewModel)
- ❌ Static content screens (no ViewModel needed)
- ❌ Settings screens (load on demand)

**Best Practices**:
1. Load data in `init` for immediate feedback
2. Provide `retry()` method for error recovery
3. Use `parametersOf` in Koin for type-safe injection
4. Extract parameters from route objects in navigation
5. Cast Swift `Int` to Kotlin `Int32` in iOS views
6. iOS: Direct Integration (private var + @State) for simple apps, Wrapper pattern for complex apps

**See Working Examples**:
- `features/pokemondetail/presentation/PokemonDetailViewModel.kt`
- `features/pokemondetail/wiring/src/commonMain/.../PokemonDetailModule.kt`
- `features/pokemondetail/wiring/src/androidMain/.../PokemonDetailNavigationProviders.kt`
- `shared/src/iosMain/kotlin/KoinIos.kt` (iOS helper)
- `iosApp/Views/PokemonDetailView.swift` (iOS Direct Integration)

**iOS Integration**: See `.junie/guides/tech/ios_integration.md` for complete patterns

---

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

### Compose Previews (MANDATORY)

**Every @Composable function MUST have a @Preview:**

```kotlin
@Composable
fun PokemonCard(
    pokemon: Pokemon,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column {
            AsyncImage(model = pokemon.imageUrl, contentDescription = pokemon.name)
            Text(text = "#${pokemon.id}")
            Text(text = pokemon.name)
        }
    }
}

// ✅ REQUIRED: Preview with realistic data
@Preview
@Composable
private fun PokemonCardPreview() {
    MaterialTheme {
        Surface {
            PokemonCard(
                pokemon = Pokemon(
                    id = 25,
                    name = "Pikachu",
                    imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png"
                )
            )
        }
    }
}

// ✅ RECOMMENDED: Multiple previews for different states
@Preview
@Composable
private fun PokemonCardLongNamePreview() {
    MaterialTheme {
        Surface {
            PokemonCard(
                pokemon = Pokemon(
                    id = 1,
                    name = "Bulbasaur with very long name",
                    imageUrl = "url"
                )
            )
        }
    }
}
```

**Preview Requirements:**
- ✅ Use `@Preview` from `org.jetbrains.compose.ui.tooling.preview.Preview`
- ✅ Preview function must be `private`
- ✅ Name pattern: `<ComponentName>Preview`
- ✅ Wrap in `MaterialTheme` and `Surface`
- ✅ Provide realistic data (not empty/null)
- ✅ Complex components need multiple previews (loading, error, content states)

**For SwiftUI Views:**
```swift
struct PokemonCard: View {
    let pokemon: Pokemon
    
    var body: some View {
        VStack {
            AsyncImage(url: URL(string: pokemon.imageUrl))
            Text("#\(pokemon.id)")
            Text(pokemon.name)
        }
    }
}

// ✅ REQUIRED: Swift Preview
#Preview {
    PokemonCard(pokemon: Pokemon(
        id: 25,
        name: "Pikachu",
        imageUrl: "https://..."
    ))
}
```

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
  viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) : ViewModel(viewModelScope), UiStateHolder<ProfileUiState, ProfileUiEvent> {
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
