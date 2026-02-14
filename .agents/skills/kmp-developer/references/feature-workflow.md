# Feature Implementation Workflow

Complete step-by-step guide for implementing new features using vertical slice architecture.

## Workflow: Implement New Feature (Vertical Slice)

To add a complete feature module:

1. Create feature directory structure in `features/<feature>/` with layers:
   - `api/` - interfaces, domain models, navigation routes
   - `data/` - API services, DTOs, mappers, repository implementations
   - `presentation/` - ViewModels, UI state classes
   - `ui-material/` - Material Design 3 Compose screens
   - `ui-unstyled/` - Compose Unstyled screens
   - `wiring/` - Koin modules for repos and ViewModels
   - `wiring-ui-material/` - Material navigation registration
   - `wiring-ui-unstyled/` - Unstyled navigation registration

2. Define repository interface in `:api`:
   ```kotlin
   // features/<feature>/api/src/commonMain/.../<Feature>Repository.kt
   interface <Feature>Repository {
       suspend fun getData(): Either<RepoError, List<Data>>
   }
   ```

3. Implement repository in `:data` with Either boundary:
   ```kotlin
   // features/<feature>/data/src/commonMain/.../<Feature>RepositoryImpl.kt
   internal class <Feature>RepositoryImpl(
       private val api: <Feature>ApiService
   ) : <Feature>Repository {
       override suspend fun getData(): Either<RepoError, List<Data>> =
           Either.catch { api.getData().map { it.toDomain() } }
               .mapLeft { it.toRepoError() }
   }
   ```

4. Create factory function in `:data`:
   ```kotlin
   fun <Feature>Repository(api: <Feature>ApiService): <Feature>Repository =
       <Feature>RepositoryImpl(api)
   ```

5. Implement ViewModel with lifecycle awareness in `:presentation`:
   ```kotlin
   class <Feature>ViewModel(
       private val repository: <Feature>Repository,
       private val savedStateHandle: SavedStateHandle,
       viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
   ) : ViewModel(viewModelScope), DefaultLifecycleObserver,
      UiStateHolder<<Feature>UiState, <Feature>UiEvent> {
       // Use by saved delegate for state persistence
       // Override onStart() for initialization (NO init block work)
   }
   ```

6. Create Compose UI screens in `:ui-material` and `:ui-unstyled` with @Preview annotations

7. Wire dependencies in `:wiring` module:
   ```kotlin
   val <feature>Module = module {
       factory { <Feature>ApiService(httpClient = get()) }
       factory<<Feature>Repository> { <Feature>Repository(get()) }
       factory<<Feature>ViewModel> { <Feature>ViewModel(get(), get()) }
   }
   ```

8. Register navigation in `:wiring-ui-material` and `:wiring-ui-unstyled`:
   ```kotlin
   val <feature>NavigationModule = module {
       scope<MaterialScope> {
           navigation<<Feature>Route> { route ->
               <Feature>Screen(viewModel = koinViewModel(), onBack = { navigator.goBack() })
           }
       }
   }
   ```

9. Write tests in `androidUnitTest/`:
   - Repository tests with success + all error paths
   - ViewModel tests with state transitions using Turbine
   - Property-based tests for mappers (30-40% target)

10. Validate: `./gradlew :composeApp:assembleDebug test --continue`

## Reference Implementation

The `pokemonlist` feature demonstrates all patterns:
- [API](../../features/pokemonlist/api/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/PokemonListRepository.kt)
- [Data](../../features/pokemonlist/data/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/data/PokemonListRepositoryImpl.kt)
- [Presentation](../../features/pokemonlist/presentation/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/presentation/PokemonListViewModel.kt)
- [UI](../../features/pokemonlist/ui-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/ui/material/PokemonListMaterialScreen.kt)
- [Wiring](../../features/pokemonlist/wiring/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/wiring/PokemonListModule.kt)
