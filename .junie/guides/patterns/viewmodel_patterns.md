# ViewModel Patterns (androidx.lifecycle)

> Comprehensive code examples for lifecycle-aware ViewModels, viewModelScope injection, UiStateHolder, and immutable state.

## Core Principle

**ALL ViewModels MUST follow this pattern exactly:**
1. Extend `androidx.lifecycle.ViewModel`
2. Pass `viewModelScope` as constructor parameter (with default value)
3. NEVER store `CoroutineScope` as a field
4. NEVER perform work in `init` block
5. Use lifecycle-aware callbacks for data loading
6. Use `kotlinx.collections.immutable` types in UI state

## Basic ViewModel Pattern

```kotlin
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: JobRepository,
    viewModelScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )
) : ViewModel(viewModelScope),  // ← Pass to superclass constructor
    UiStateHolder<HomeUiState, HomeUiEvent> {
    
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    override val uiState: StateFlow<HomeUiState> = _uiState
    
    // ⚠️ NEVER perform work in init {}
    
    // Load data in lifecycle-aware callbacks
    fun start(lifecycle: Lifecycle) {
        viewModelScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.getJobs().fold(
                    ifLeft = { error ->
                        _uiState.value = HomeUiState.Error(error.toUiMessage())
                    },
                    ifRight = { jobs ->
                        _uiState.value = HomeUiState.Content(
                            jobs = jobs.toImmutableList()
                        )
                    }
                )
            }
        }
    }
    
    override fun onUiEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.Refresh -> refresh()
            is HomeUiEvent.ItemClicked -> handleClick(event.id)
        }
    }
    
    private fun refresh() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            repository.getJobs().fold(
                ifLeft = { error ->
                    _uiState.value = HomeUiState.Error(error.toUiMessage())
                },
                ifRight = { jobs ->
                    _uiState.value = HomeUiState.Content(
                        jobs = jobs.toImmutableList()
                    )
                }
            )
        }
    }
    
    private fun handleClick(id: String) {
        // Handle item click
    }
}

// UI State with immutable collections
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Content(
        val jobs: ImmutableList<Job>
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
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

```kotlin
class PokemonDetailViewModel(
    private val pokemonId: Int,
    private val repository: PokemonDetailRepository,
    viewModelScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )
) : ViewModel(viewModelScope),
    UiStateHolder<PokemonDetailUiState, PokemonDetailUiEvent> {
    
    private val _uiState = MutableStateFlow<PokemonDetailUiState>(
        PokemonDetailUiState.Loading
    )
    override val uiState: StateFlow<PokemonDetailUiState> = _uiState
    
    fun start(lifecycle: Lifecycle) {
        viewModelScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
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
    }
    
    override fun onUiEvent(event: PokemonDetailUiEvent) {
        when (event) {
            is PokemonDetailUiEvent.Retry -> retry()
            is PokemonDetailUiEvent.Favorite -> toggleFavorite()
        }
    }
    
    private fun retry() {
        viewModelScope.launch {
            _uiState.value = PokemonDetailUiState.Loading
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
    UiStateHolder<PokemonListUiState, PokemonListUiEvent> {
    
    private val _uiState = MutableStateFlow<PokemonListUiState>(
        PokemonListUiState.Loading
    )
    override val uiState: StateFlow<PokemonListUiState> = _uiState
    
    private var currentOffset = 0
    
    fun start(lifecycle: Lifecycle) {
        viewModelScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loadPage()
            }
        }
    }
    
    override fun onUiEvent(event: PokemonListUiEvent) {
        when (event) {
            is PokemonListUiEvent.LoadMore -> loadMore()
            is PokemonListUiEvent.Retry -> retry()
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

```kotlin
import androidx.lifecycle.SavedStateHandle

class SearchViewModel(
    private val repository: SearchRepository,
    private val savedStateHandle: SavedStateHandle,
    viewModelScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )
) : ViewModel(viewModelScope),
    UiStateHolder<SearchUiState, SearchUiEvent> {
    
    companion object {
        private const val KEY_QUERY = "search_query"
        private const val KEY_RESULTS = "search_results"
    }
    
    private val _uiState = MutableStateFlow<SearchUiState>(
        SearchUiState.Idle(
            query = savedStateHandle.get<String>(KEY_QUERY) ?: ""
        )
    )
    override val uiState: StateFlow<SearchUiState> = _uiState
    
    override fun onUiEvent(event: SearchUiEvent) {
        when (event) {
            is SearchUiEvent.QueryChanged -> updateQuery(event.query)
            is SearchUiEvent.Search -> search()
        }
    }
    
    private fun updateQuery(query: String) {
        savedStateHandle[KEY_QUERY] = query
        _uiState.value = SearchUiState.Idle(query)
    }
    
    private fun search() {
        val currentState = _uiState.value
        val query = when (currentState) {
            is SearchUiState.Idle -> currentState.query
            is SearchUiState.Results -> currentState.query
            else -> return
        }
        
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
                    val immutableResults = results.toImmutableList()
                    savedStateHandle[KEY_RESULTS] = results
                    _uiState.value = SearchUiState.Results(
                        query = query,
                        results = immutableResults
                    )
                }
            )
        }
    }
}

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
class MyViewModel(...) : ViewModel(...) {
    fun start(lifecycle: Lifecycle) {
        viewModelScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loadData()  // Lifecycle-aware
            }
        }
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

- `.junie/guides/tech/presentation_layer.md` — Complete presentation layer guide
- `.junie/guides/tech/coroutines.md` — Scopes, dispatchers, lifecycle
- `patterns/di_patterns.md` — ViewModel DI patterns
- `patterns/error_handling_patterns.md` — Using Either in ViewModels
- `patterns/testing_patterns.md` — ViewModel testing with Turbine
