# ViewModel Patterns

Detailed ViewModel implementation patterns for Kotlin Multiplatform.

## Lifecycle-Aware ViewModel with SavedStateHandle

All ViewModels MUST follow this canonical pattern:

### Complete Example

```kotlin
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PokemonListViewModel(
    private val repository: PokemonListRepository,
    private val savedStateHandle: SavedStateHandle,
    viewModelScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )
) : ViewModel(viewModelScope),
    DefaultLifecycleObserver,
    UiStateHolder<PokemonListUiState, PokemonListUiEvent> {
    
    // Automatic state persistence with delegate
    private var persistedState by savedStateHandle.saved { PokemonListPersistedState() }

    private val _uiState = MutableStateFlow<PokemonListUiState>(PokemonListUiState.Loading)
    val uiState: StateFlow<PokemonListUiState> = _uiState.asStateFlow()
    
    // NEVER perform work in init {}
    
    // Lifecycle-aware initialization
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        loadInitialPage()
    }
    
    fun loadInitialPage() {
        viewModelScope.launch {
            _uiState.value = PokemonListUiState.Loading
            repository.loadPage(limit = 20, offset = 0).fold(
                ifLeft = { error ->
                    _uiState.value = PokemonListUiState.Error(error.toUiMessage())
                },
                ifRight = { page ->
                    persistedState = persistedState.copy(pokemons = page.pokemons)
                    _uiState.value = PokemonListUiState.Content(
                        pokemons = page.pokemons.toImmutableList(),
                        hasMore = page.hasMore
                    )
                }
            )
        }
    }
}
```

## Key Requirements

1. **Extend ViewModel**: Pass `viewModelScope` to superclass constructor
2. **Implement DefaultLifecycleObserver**: For lifecycle-aware operations
3. **Inject SavedStateHandle**: Required for state persistence
4. **Use `by saved` delegate**: Automatic state serialization
5. **NO work in `init`**: Use `onStart(owner: LifecycleOwner)` instead
6. **Immutable UI state**: Use `kotlinx.collections.immutable` types
7. **Consume Either from repos**: Map to UI state sealed classes

## Koin Wiring Pattern

```kotlin
// In :wiring module
val pokemonListModule = module {
    factory { PokemonListApiService(httpClient = get()) }
    
    factory<PokemonListRepository> {
        PokemonListRepository(apiService = get())
    }
    
    viewModel {
        PokemonListViewModel(
            repository = get(),
            savedStateHandle = SavedStateHandle()
        )
    }
}
```

## Parametric ViewModels

For ViewModels requiring constructor parameters:

```kotlin
// Koin wiring with parametersOf
val pokemonDetailModule = module {
    factory { params ->
        PokemonDetailViewModel(
            pokemonId = params.get(),
            repository = get()
        )
    }
}

// Helper function for iOS
fun getPokemonDetailViewModel(pokemonId: Int): PokemonDetailViewModel {
    return KoinPlatform.getKoin().get { parametersOf(pokemonId) }
}
```

## Testing Pattern

```kotlin
class PokemonListViewModelSpec : StringSpec({
    lateinit var repository: PokemonListRepository
    lateinit var testScope: TestScope
    lateinit var viewModel: PokemonListViewModel

    beforeTest {
        repository = mockk(relaxed = true)
        testScope = TestScope()
        viewModel = PokemonListViewModel(repository, testScope)
    }

    "loads data on onStart" {
        coEvery { repository.loadPage(any(), any()) } returns Either.right(mockPage)
        
        viewModel.uiState.test {
            awaitItem() shouldBe PokemonListUiState.Loading
            viewModel.onStart(mockk())
            testScope.advanceUntilIdle()
            awaitItem().shouldBeInstanceOf<PokemonListUiState.Content>()
        }
    }
})
```

## Common Imports

```kotlin
// ViewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewModelScope

// Coroutines
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Immutable collections
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
```
