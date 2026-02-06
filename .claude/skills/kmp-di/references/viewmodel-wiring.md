# ViewModel Wiring

## Basic ViewModel Injection

```kotlin
class ProfileViewModel(
    private val repository: ProfileRepository,
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob())
) : ViewModel(viewModelScope) { ... }

// Koin module
val profileModule = module {
    factory<ProfileViewModel> {
        ProfileViewModel(repository = get())
    }
}
```

## ViewModel with SavedStateHandle

```kotlin
class DetailViewModel(
    private val id: String,
    private val repository: DetailRepository,
    savedStateHandle: SavedStateHandle,
    viewModelScope: CoroutineScope = ...
) : ViewModel(viewModelScope) { ... }

// Koin module
val detailModule = module {
    factory { params ->
        DetailViewModel(
            id = params.get(),
            repository = get(),
            savedStateHandle = SavedStateHandle()
        )
    }
}
```

## Compose Usage

```kotlin
@Composable
fun ProfileScreen() {
    val viewModel: ProfileViewModel = koinInject()
    val state by viewModel.state.collectAsState()
    
    when (state) {
        is ProfileUiState.Loading -> LoadingView()
        is ProfileUiState.Success -> ProfileContent(state.profile)
        is ProfileUiState.Error -> ErrorView(state.error)
    }
}
```

## Navigation Integration

```kotlin
// features/pokemonlist/wiring/src/androidMain/.../PokemonListNavigationProviders.kt
val pokemonListNavigationModule = module {
    single<Set<EntryProviderInstaller>> {
        setOf(
            {
                entry<PokemonList> {
                    PokemonListScreen(
                        viewModel = koinInject(),
                        onPokemonClick = {
                            koinInject<Navigator>().goTo(PokemonDetail(it.id))
                        }
                    )
                }
            }
        )
    }
}
```

## ViewModel Pattern Checklist

- [ ] Pass `viewModelScope` to constructor
- [ ] Use `factory` in Koin module (new instance per request)
- [ ] For parameters, use `factory { params -> ... }`
- [ ] Inject dependencies with `get()`
- [ ] Use `koinInject()` in Compose for resolution
