# ViewModel Patterns - Extended Examples

Last Updated: December 20, 2025

> **Core Rules**: See [ViewModel Pattern](../tech/critical_patterns_quick_ref.md#viewmodel-pattern) for canonical pattern definition.

This guide provides extended examples and edge cases for the ViewModel pattern.

## Reference Implementations

**Production ViewModels:**
- [PokemonListViewModel.kt](../../features/pokemonlist/presentation/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/presentation/PokemonListViewModel.kt) - List with pagination
- [PokemonDetailViewModel.kt](../../features/pokemondetail/presentation/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/presentation/PokemonDetailViewModel.kt) - Parametric ViewModel

**Tests:**
- [PokemonListViewModelTest.kt](../../features/pokemonlist/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/presentation/PokemonListViewModelTest.kt)
- [PokemonDetailViewModelTest.kt](../../features/pokemondetail/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/presentation/PokemonDetailViewModelTest.kt)

## Core Pattern Summary

**ALL ViewModels MUST follow this pattern** (see [canonical rules](../tech/critical_patterns_quick_ref.md#viewmodel-pattern)):
1. Extend `androidx.lifecycle.ViewModel`
2. Implement `DefaultLifecycleObserver` for lifecycle awareness
3. Pass `viewModelScope` as constructor parameter to superclass (NOT stored as field)
4. NEVER perform work in `init` block
5. Override `onStart(owner: LifecycleOwner)` for initialization logic
6. Use `kotlinx.collections.immutable` types in UI state

## Basic ViewModel Pattern

```kotlin
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PokemonListViewModel(
    private val repository: PokemonListRepository,
    private val savedStateHandle: SavedStateHandle,
    viewModelScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )
) : ViewModel(viewModelScope),  // ← Pass to superclass constructor
    DefaultLifecycleObserver,   // ← Lifecycle awareness
    UiStateHolder<PokemonListUiState, PokemonListUiEvent> {
    
    private val _uiState = MutableStateFlow<PokemonListUiState>(PokemonListUiState.Loading)
    override val uiState: StateFlow<PokemonListUiState> = _uiState
    
    // ⚠️ NEVER perform work in init {}
    
    // Lifecycle-aware initialization replaces repeatOnLifecycle pattern
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        loadInitialPage()
    }
    
    private fun loadInitialPage() {
        viewModelScope.launch {
            _uiState.value = PokemonListUiState.Loading
            repository.loadPage(limit = 20, offset = 0).fold(
                ifLeft = { error ->
                    _uiState.value = PokemonListUiState.Error(error.toUiMessage())
                },
                ifRight = { page ->
                    _uiState.value = PokemonListUiState.Content(
                        pokemons = page.pokemons,
                        hasMore = page.hasMore,
                        isLoadingMore = false
                    )
                }
            )
        }
    }
    
    override fun onUiEvent(event: PokemonListUiEvent) {
        when (event) {
            is PokemonListUiEvent.LoadMore -> loadNextPage()
            is PokemonListUiEvent.Retry -> loadInitialPage()
        }
    }
    
    private fun loadNextPage() {
        // Pagination logic
    }
}

// UI State with immutable collections
sealed interface PokemonListUiState {
    data object Loading : PokemonListUiState
    data class Content(
        val pokemons: ImmutableList<Pokemon>,
        val hasMore: Boolean,
        val isLoadingMore: Boolean
    ) : PokemonListUiState
    data class Error(val message: String) : PokemonListUiState
}

// UI Events
sealed interface HomeUiEvent {
    data object Refresh : HomeUiEvent
    data class ItemClicked(val id: String) : HomeUiEvent
}

// UiStateHolder interface
interface UiStateHolder<S, E> {
    val uiState: StateFlow<S>
    fun onUiEvent(event: E)
}
```

## Parametric ViewModel (With ID/Parameters)

**CRITICAL: Navigation 3 requires explicit ViewModel scoping for parametric routes**

When using parametric routes (e.g., `PokemonDetail(id: Int)`), you MUST provide a unique `key` to `koinViewModel()` that includes the parameter. Without this, Navigation 3 will reuse the same ViewModel instance across different parameter values, causing stale data bugs.

### Navigation Wiring (Required Pattern)

```kotlin
// :features:pokemondetail:wiring-ui/commonMain/PokemonDetailNavigationProviders.kt
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

entry<PokemonDetail> { route ->
    val navigator: Navigator = koinInject()
    
    // ✅ REQUIRED: Key ViewModel by route parameter
    val viewModel: PokemonDetailViewModel = koinViewModel(
        key = "pokemon_detail_${route.id}",  // ← Essential for parametric routes
        parameters = { parametersOf(route.id) }
    )
    
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // ✅ REQUIRED: Key DisposableEffect by route parameter
    DisposableEffect(route.id) {  // ← Not viewModel!
        lifecycleOwner.lifecycle.addObserver(viewModel)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(viewModel)
        }
    }
    
    PokemonDetailScreen(
        viewModel = viewModel,
        onBackClick = { navigator.goBack() }
    )
}
```

**Why This Matters:**
- Without `key`, navigating from `PokemonDetail(1)` → `PokemonDetail(2)` reuses ViewModel with ID=1
- Without `DisposableEffect(route.id)`, lifecycle observers aren't properly cleaned up
- Navigation 3 entry scope is based on route **type**, not parameter values

### ViewModel Implementation

```kotlin
class PokemonDetailViewModel(
    private val pokemonId: Int,
    private val repository: PokemonDetailRepository,
    private val savedStateHandle: SavedStateHandle,
    viewModelScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )
) : ViewModel(viewModelScope),
    DefaultLifecycleObserver,
    UiStateHolder<PokemonDetailUiState, PokemonDetailUiEvent> {
    
    private val _uiState = MutableStateFlow<PokemonDetailUiState>(
        PokemonDetailUiState.Loading
    )
    override val uiState: StateFlow<PokemonDetailUiState> = _uiState
    
    // Lifecycle-aware initialization
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        loadPokemon()
    }
    
    private fun loadPokemon() {
        viewModelScope.launch {
            repository.getById(pokemonId).fold(
                ifLeft = { error ->
                    _uiState.value = PokemonDetailUiState.Error(
                        message = error.toUiMessage()
                    )
                },
                ifRight = { pokemon ->
                    _uiState.value = PokemonDetailUiState.Content(
                        pokemon = pokemon
                    )
                }
            )
        }
    }
    
    override fun onUiEvent(event: PokemonDetailUiEvent) {
        when (event) {
            is PokemonDetailUiEvent.Retry -> loadPokemon()
            is PokemonDetailUiEvent.Favorite -> toggleFavorite()
        }
    }
    
    private fun toggleFavorite() {
        // Toggle favorite state
    }
}

sealed interface PokemonDetailUiState {
    data object Loading : PokemonDetailUiState
    data class Content(val pokemon: Pokemon) : PokemonDetailUiState
    data class Error(val message: String) : PokemonDetailUiState
}

sealed interface PokemonDetailUiEvent {
    data object Retry : PokemonDetailUiEvent
    data object Favorite : PokemonDetailUiEvent
}
```

## Pagination ViewModel

```kotlin
class PokemonListViewModel(
    private val repository: PokemonListRepository,
    viewModelScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )
) : ViewModel(viewModelScope),
    DefaultLifecycleObserver,
    UiStateHolder<PokemonListUiState, PokemonListUiEvent> {
    
    private val _uiState = MutableStateFlow<PokemonListUiState>(
        PokemonListUiState.Loading
    )
    override val uiState: StateFlow<PokemonListUiState> = _uiState
    
    private var currentOffset = 0
    
    // Lifecycle-aware initialization
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        loadInitialPage()
    }
    
    private fun loadInitialPage() {
        viewModelScope.launch {
            loadPage()
        }
    }
    
    override fun onUiEvent(event: PokemonListUiEvent) {
        when (event) {
            is PokemonListUiEvent.LoadMore -> loadMore()
            is PokemonListUiEvent.Retry -> loadInitialPage()
        }
    }
    
    private suspend fun loadPage() {
        repository.loadPage(offset = currentOffset).fold(
            ifLeft = { error ->
                _uiState.value = PokemonListUiState.Error(
                    message = error.toUiMessage()
                )
            },
            ifRight = { page ->
                _uiState.value = PokemonListUiState.Content(
                    pokemons = page.pokemons,
                    hasMore = page.hasMore,
                    isLoadingMore = false
                )
                currentOffset += page.pokemons.size
            }
        )
    }
    
    private fun loadMore() {
        val currentState = _uiState.value
        if (currentState !is PokemonListUiState.Content || 
            currentState.isLoadingMore ||
            !currentState.hasMore) {
            return
        }
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoadingMore = true)
            
            repository.loadPage(offset = currentOffset).fold(
                ifLeft = { error ->
                    _uiState.value = currentState.copy(
                        isLoadingMore = false,
                        error = error.toUiMessage()
                    )
                },
                ifRight = { page ->
                    _uiState.value = currentState.copy(
                        pokemons = (currentState.pokemons + page.pokemons)
                            .toImmutableList(),
                        hasMore = page.hasMore,
                        isLoadingMore = false,
                        error = null
                    )
                    currentOffset += page.pokemons.size
                }
            )
        }
    }
    
    private fun retry() {
        viewModelScope.launch {
            currentOffset = 0
            _uiState.value = PokemonListUiState.Loading
            loadPage()
        }
    }
}

sealed interface PokemonListUiState {
    data object Loading : PokemonListUiState
    
    data class Content(
        val pokemons: ImmutableList<Pokemon>,
        val hasMore: Boolean,
        val isLoadingMore: Boolean,
        val error: String? = null
    ) : PokemonListUiState
    
    data class Error(val message: String) : PokemonListUiState
}

sealed interface PokemonListUiEvent {
    data object LoadMore : PokemonListUiEvent
    data object Retry : PokemonListUiEvent
}
```

## One-Time Events Pattern

```kotlin
import arrow.core.Either
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

// One-time event emitter interface
interface OneTimeEventEmitter<E> {
    val events: Flow<E>
    suspend fun emit(event: E)
}

// Implementation via delegation
class EventChannel<E> : OneTimeEventEmitter<E> {
    private val _events = Channel<E>(Channel.BUFFERED)
    override val events: Flow<E> = _events.receiveAsFlow()
    
    override suspend fun emit(event: E) {
        _events.send(event)
    }
}

// ViewModel with one-time events
class LoginViewModel(
    private val authRepository: AuthRepository,
    viewModelScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )
) : ViewModel(viewModelScope),
    UiStateHolder<LoginUiState, LoginUiEvent>,
    OneTimeEventEmitter<LoginNavEvent> by EventChannel() {
    
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    override val uiState: StateFlow<LoginUiState> = _uiState
    
    override fun onUiEvent(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.Login -> login(event.email, event.password)
        }
    }
    
    private fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            
            authRepository.login(email, password).fold(
                ifLeft = { error ->
                    _uiState.value = LoginUiState.Error(error.toUiMessage())
                },
                ifRight = { user ->
                    _uiState.value = LoginUiState.Idle
                    emit(LoginNavEvent.NavigateToHome)  // One-time event
                }
            )
        }
    }
}

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Error(val message: String) : LoginUiState
}

sealed interface LoginUiEvent {
    data class Login(val email: String, val password: String) : LoginUiEvent
}

sealed interface LoginNavEvent {
    data object NavigateToHome : LoginNavEvent
    data object NavigateToSignup : LoginNavEvent
}

// Usage in Compose
@Composable
fun LoginScreen(viewModel: LoginViewModel, onNavigateToHome: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LoginNavEvent.NavigateToHome -> onNavigateToHome()
                is LoginNavEvent.NavigateToSignup -> { /* navigate */ }
            }
        }
    }
    
    // UI content
}
```

## SavedStateHandle Pattern

**Current Pattern (Delegate - SavedState 1.4.0+):**

Use `by saved` delegate for automatic state persistence across configuration changes and process death.

```kotlin
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.serialization.saved
import kotlinx.serialization.Serializable

@Serializable
data class SearchPersistedState(
    val query: String = "",
    val lastResults: List<SearchResult> = emptyList()
)

class SearchViewModel(
    private val repository: SearchRepository,
    private val savedStateHandle: SavedStateHandle,
    viewModelScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )
) : ViewModel(viewModelScope),
    UiStateHolder<SearchUiState, SearchUiEvent> {
    
    // ✨ Single line - automatic persistence (no manual calls needed)
    private var persistedState by savedStateHandle.saved { SearchPersistedState() }
    
    private val _uiState = MutableStateFlow<SearchUiState>(
        restoreUiState()
    )
    override val uiState: StateFlow<SearchUiState> = _uiState
    
    override fun onUiEvent(event: SearchUiEvent) {
        when (event) {
            is SearchUiEvent.QueryChanged -> updateQuery(event.query)
            is SearchUiEvent.Search -> search()
        }
    }
    
    private fun updateQuery(query: String) {
        persistedState = persistedState.copy(query = query)  // ← Automatically persisted
        _uiState.value = SearchUiState.Idle(query)
    }
    
    private fun search() {
        val query = persistedState.query
        if (query.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading(query)
            
            repository.search(query).fold(
                ifLeft = { error ->
                    _uiState.value = SearchUiState.Error(
                        query = query,
                        message = error.toUiMessage()
                    )
                },
                ifRight = { results ->
                    persistedState = persistedState.copy(
                        lastResults = results  // ← Automatically persisted
                    )
                    _uiState.value = SearchUiState.Results(
                        query = query,
                        results = results.toImmutableList()
                    )
                }
            )
        }
    }
    
    private fun restoreUiState(): SearchUiState {
        return if (persistedState.lastResults.isNotEmpty()) {
            SearchUiState.Results(
                query = persistedState.query,
                results = persistedState.lastResults.toImmutableList()
            )
        } else {
            SearchUiState.Idle(persistedState.query)
        }
    }
}
```

**Key Benefits:**
- ✅ **93% code reduction** - No manual JSON encoding/decoding
- ✅ **Automatic persistence** - State saved on every property write
- ✅ **Type-safe** - Uses kotlinx.serialization internally
- ✅ **No manual calls** - No `persistState()` functions needed
- ✅ **KMP compatible** - Works in `commonMain` source set

**Critical Requirements:**
1. Import: `androidx.lifecycle.serialization.saved` (NOT `androidx.savedstate.serialization.saved`)
2. State type must be `@Serializable`
3. Property name stability (renaming breaks restoration for existing users)

**Reference Implementations:**
- [PokemonListViewModel.kt](../../features/pokemonlist/presentation/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/presentation/PokemonListViewModel.kt)
- [PokemonDetailViewModel.kt](../../features/pokemondetail/presentation/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/presentation/PokemonDetailViewModel.kt)

sealed interface SearchUiState {
    val query: String
    
    data class Idle(override val query: String) : SearchUiState
    data class Loading(override val query: String) : SearchUiState
    data class Results(
        override val query: String,
        val results: ImmutableList<SearchResult>
    ) : SearchUiState
    data class Error(
        override val query: String,
        val message: String
    ) : SearchUiState
}

sealed interface SearchUiEvent {
    data class QueryChanged(val query: String) : SearchUiEvent
    data object Search : SearchUiEvent
}
```

## Anti-Patterns to Avoid

### ❌ DON'T: Store CoroutineScope as Field

```kotlin
// ❌ WRONG
class MyViewModel : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob())  // WRONG
    
    init {
        scope.launch {  // Memory leak - not tied to ViewModel lifecycle
            // ...
        }
    }
}

// ✅ CORRECT
class MyViewModel(
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob())
) : ViewModel(viewModelScope) {
    
    fun loadData() {
        viewModelScope.launch {  // Automatically cancelled on onCleared
            // ...
        }
    }
}
```

### ❌ DON'T: Work in init Block

```kotlin
// ❌ WRONG
class MyViewModel(...) : ViewModel(...) {
    init {
        loadData()  // Executes immediately, not lifecycle-aware
    }
}

// ✅ CORRECT  
class MyViewModel(...) : ViewModel(...), DefaultLifecycleObserver {
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        loadData()  // Lifecycle-aware, called when UI becomes visible
    }
}
```

### ❌ DON'T: Use Mutable Collections

```kotlin
// ❌ WRONG
data class HomeUiState(
    val items: List<Item>  // Mutable List
)

// ✅ CORRECT
data class HomeUiState(
    val items: ImmutableList<Item>  // Immutable
)
```

### ❌ DON'T: Constructor Injection without Default

```kotlin
// ❌ WRONG - Hard to test, couples to DI framework
class MyViewModel @Inject constructor(
    private val repo: MyRepository
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob())
}

// ✅ CORRECT - Easy to test, DI-agnostic
class MyViewModel(
    private val repo: MyRepository,
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob())
) : ViewModel(viewModelScope)
```

## See Also

- [presentation_layer.md](../tech/presentation_layer.md) — Complete presentation layer guide
- [coroutines.md](../tech/coroutines.md) — Scopes, dispatchers, lifecycle
- `patterns/di_patterns.md` — ViewModel DI patterns
- `patterns/error_handling_patterns.md` — Using Either in ViewModels
- `patterns/testing_patterns.md` — ViewModel testing with Turbine
