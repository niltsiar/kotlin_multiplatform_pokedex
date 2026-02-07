# ViewModel Patterns - Complete Guide

Reference implementations and patterns for ViewModels in KMP presentation layer.

## Core Pattern

### Required Structure

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
                ifLeft = { error ->
                    _uiState.value = HomeUiState.Error(error.toUiMessage())
                },
                ifRight = { items ->
                    _uiState.value = HomeUiState.Content(items.toImmutableList())
                }
            )
        }
    }

    override fun onUiEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.Refresh -> loadData()
            is HomeUiEvent.ItemClicked -> handleItemClick(event.id)
        }
    }
}
```

### UI State Definition

```kotlin
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Error(val message: String) : HomeUiState
    data class Content(
        val items: ImmutableList<ItemUiState>
    ) : HomeUiState
}

data class ItemUiState(
    val id: String,
    val title: String,
    val imageUrl: String?
)
```

### UI Events Definition

```kotlin
sealed interface HomeUiEvent {
    data object Refresh : HomeUiEvent
    data class ItemClicked(val id: String) : HomeUiEvent
}
```

## Parametric ViewModels (With ID/Parameters)

**CRITICAL**: Navigation 3 requires explicit ViewModel scoping for parametric routes.

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

    // ✅ Load data automatically with parameter
    init {
        loadPokemon()
    }

    private fun loadPokemon() {
        viewModelScope.launch {
            _uiState.value = PokemonDetailUiState.Loading
            repository.getById(pokemonId).fold(
                ifLeft = { error ->
                    _uiState.value = PokemonDetailUiState.Error(
                        message = error.toUiMessage()
                    )
                },
                ifRight = { pokemon ->
                    _uiState.value = PokemonDetailUiState.Content(pokemon = pokemon)
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
            is PokemonDetailUiEvent.Favorite -> toggleFavorite()
        }
    }

    private fun toggleFavorite() { /* ... */ }
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

### Koin Wiring (Common)

```kotlin
// :features:pokemondetail:wiring/src/commonMain/.../PokemonDetailModule.kt
val pokemonDetailModule = module {
    factory { params ->
        PokemonDetailViewModel(
            pokemonId = params.get(),  // Extract Int parameter
            repository = get(),
            savedStateHandle = get()
        )
    }
}
```

### Navigation 3 Integration (Android)

```kotlin
// :features:pokemondetail:wiring-ui/src/commonMain/.../PokemonDetailNavigationProviders.kt
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

entry<PokemonDetail> { route ->
    val navigator: Navigator = koinInject()

    // ✅ REQUIRED: Key ViewModel by route parameter
    val viewModel: PokemonDetailViewModel = koinViewModel(
        key = "pokemon_detail_${route.id}",  // Essential for parametric routes
        parameters = { parametersOf(route.id) }
    )

    val lifecycleOwner = LocalLifecycleOwner.current

    // ✅ REQUIRED: Key DisposableEffect by route parameter
    DisposableEffect(route.id) {
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

### iOS Helper Function

```kotlin
// shared/src/iosMain/kotlin/KoinIos.kt
fun getPokemonDetailViewModel(pokemonId: Int): PokemonDetailViewModel {
    return KoinPlatform.getKoin().get { parametersOf(pokemonId) }
}
```

### iOS SwiftUI Usage

```swift
struct PokemonDetailView: View {
    let pokemonId: Int
    private var viewModel: PokemonDetailViewModel
    @State private var uiState: PokemonDetailUiState = PokemonDetailUiStateLoading()

    init(pokemonId: Int) {
        self.pokemonId = pokemonId
        viewModel = KoinIosKt.getPokemonDetailViewModel(pokemonId: Int32(pokemonId))
    }

    var body: some View {
        content
            .task {
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
            ErrorView(message: error.message, onRetry: { viewModel.retry() })
        default:
            EmptyView()
        }
    }
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
                    pokemons = page.pokemons.toImmutableList(),
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
                        loadMoreError = error.toUiMessage()
                    )
                },
                ifRight = { page ->
                    _uiState.value = currentState.copy(
                        pokemons = (currentState.pokemons + page.pokemons)
                            .toImmutableList(),
                        hasMore = page.hasMore,
                        isLoadingMore = false,
                        loadMoreError = null
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
        val loadMoreError: String? = null
    ) : PokemonListUiState

    data class Error(val message: String) : PokemonListUiState
}

sealed interface PokemonListUiEvent {
    data object LoadMore : PokemonListUiEvent
    data object Retry : PokemonListUiEvent
}
```

## Anti-Patterns

### ❌ DON'T: Store CoroutineScope as Field

```kotlin
// ❌ WRONG
class MyViewModel : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob())

    init {
        scope.launch { /* Memory leak */ }
    }
}

// ✅ CORRECT
class MyViewModel(
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob())
) : ViewModel(viewModelScope) {
    fun loadData() {
        viewModelScope.launch { /* Auto-cancelled on onCleared */ }
    }
}
```

### ❌ DON'T: Work in init Block

```kotlin
// ❌ WRONG
class MyViewModel(...) : ViewModel(...) {
    init {
        loadData()  // Not lifecycle-aware
    }
}

// ✅ CORRECT
class MyViewModel(...) : ViewModel(...), DefaultLifecycleObserver {
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        loadData()  // Lifecycle-aware
    }
}
```

### ❌ DON'T: Use Mutable Collections

```kotlin
// ❌ WRONG
data class HomeUiState(
    val items: List<Item>  // Mutable
)

// ✅ CORRECT
data class HomeUiState(
    val items: ImmutableList<Item>  // Immutable
)
```

### ❌ DON'T: Constructor Injection without Default

```kotlin
// ❌ WRONG - Hard to test
class MyViewModel @Inject constructor(
    private val repo: MyRepository
) : ViewModel()

// ✅ CORRECT - Easy to test
class MyViewModel(
    private val repo: MyRepository,
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob())
) : ViewModel(viewModelScope)
```

## Reference Implementations

- `features/pokemonlist/presentation/PokemonListViewModel.kt` — Pagination
- `features/pokemondetail/presentation/PokemonDetailViewModel.kt` — Parametric
- `features/pokemonlist/presentation/PokemonListViewModelTest.kt` — Tests
- `features/pokemondetail/presentation/PokemonDetailViewModelTest.kt` — Tests
