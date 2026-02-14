# Pattern Examples - Detailed Code Samples

Complete code examples for the 6 critical KMP patterns. Load this file when implementing patterns for the first time or when you need detailed syntax reference.

---

## Pattern 1: Impl + Factory (Koin) - Detailed Example

**Complete Implementation:**

```kotlin
// In :data module - PokemonListRepositoryImpl.kt
internal class PokemonListRepositoryImpl(
    private val api: PokemonListApiService
) : PokemonListRepository {
    override suspend fun getPokemonList(): Either<RepoError, List<Pokemon>> =
        Either.catch { api.fetch().map { it.toDomain() } }
            .mapLeft { it.toRepoError() }
}

// Public factory in same file
fun PokemonListRepository(api: PokemonListApiService): PokemonListRepository =
    PokemonListRepositoryImpl(api)
```

**Koin Wiring:**

```kotlin
// In :wiring module
val pokemonListDataModule = module {
    single { PokemonListRepository(api = get()) }
}
```

**Why This Pattern:**
- `Impl` class is `internal` → encapsulation, no leaky abstractions
- Factory function is `public` → clean API surface
- Production code stays DI-agnostic → testable without framework
- Koin wiring is isolated to wiring modules → separation of concerns

---

## Pattern 2: Either Boundary - Detailed Example

**Repository Interface:**

```kotlin
interface PokemonListRepository {
    suspend fun getPokemonList(): Either<RepoError, List<Pokemon>>
}
```

**Implementation with Error Mapping:**

```kotlin
internal class PokemonListRepositoryImpl(
    private val api: PokemonListApiService
) : PokemonListRepository {
    override suspend fun getPokemonList(): Either<RepoError, List<Pokemon>> =
        Either.catch { 
            api.fetch().map { it.toDomain() } 
        }.mapLeft { throwable ->
            throwable.toRepoError()
        }
}

// Error mapper
private fun Throwable.toRepoError(): RepoError = when (this) {
    is IOException -> RepoError.Network(this)
    is HttpException -> RepoError.Http(code = this.code, message = this.message)
    else -> RepoError.Unknown(this)
}
```

**Error Types:**

```kotlin
sealed interface RepoError {
    data class Network(val cause: Throwable) : RepoError
    data class Http(val code: Int, val message: String?) : RepoError
    data class Unknown(val cause: Throwable) : RepoError
}
```

**ViewModel Consumption:**

```kotlin
class PokemonListViewModel(...) {
    fun loadPokemon() {
        viewModelScope.launch {
            repository.getPokemonList()
                .onRight { pokemon -> _state.value = UiState.Content(pokemon) }
                .onLeft { error -> _state.value = UiState.Error(error.toMessage()) }
        }
    }
}
```

---

## Pattern 3: ViewModel Pattern - Detailed Example

**Complete ViewModel:**

```kotlin
class PokemonListViewModel(
    private val repository: PokemonListRepository,
    private val savedStateHandle: SavedStateHandle,
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) : ViewModel(viewModelScope), DefaultLifecycleObserver,
    UiStateHolder<PokemonListUiState, PokemonListUiEvent> {

    private val _uiState = MutableStateFlow<PokemonListUiState>(PokemonListUiState.Loading)
    override val uiState: StateFlow<PokemonListUiState> = _uiState.asStateFlow()

    override fun onStart(owner: LifecycleOwner) {
        // Initialization here, NOT in init
        loadPokemon()
    }

    private fun loadPokemon() {
        viewModelScope.launch {
            repository.getPokemonList()
                .onRight { pokemon -> _uiState.value = PokemonListUiState.Content(pokemon.toImmutableList()) }
                .onLeft { error -> _uiState.value = PokemonListUiState.Error(error.toMessage()) }
        }
    }

    override fun onEvent(event: PokemonListUiEvent) {
        when (event) {
            is PokemonListUiEvent.Retry -> loadPokemon()
            is PokemonListUiEvent.SelectPokemon -> { /* navigate */ }
        }
    }
}
```

**Koin Wiring:**

```kotlin
val pokemonListPresentationModule = module {
    viewModel { params ->
        PokemonListViewModel(
            repository = get(),
            savedStateHandle = params.get(),
            viewModelScope = params.get()
        )
    }
}
```

**Compose Usage:**

```kotlin
@Composable
fun PokemonListScreen(
    viewModel: PokemonListViewModel = koinViewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(viewModel)
        onDispose { lifecycleOwner.lifecycle.removeObserver(viewModel) }
    }

    val uiState by viewModel.uiState.collectAsState()
    // UI rendering...
}
```

---

## Pattern 4: Navigation 3 Pattern - Detailed Example

**Route Definition (in :api module):**

```kotlin
@Serializable
object PokemonListRoute

@Serializable
data class PokemonDetailRoute(val id: Int)
```

**Navigation Provider (in :wiring-ui-material module):**

```kotlin
val pokemonListNavigationModule = module {
    scope<MaterialScope> {
        navigation<PokemonListRoute> { route ->
            PokemonListScreen(
                viewModel = koinViewModel(),
                onPokemonClick = { id ->
                    navigator.navigate(PokemonDetailRoute(id))
                },
                onBack = { navigator.goBack() }
            )
        }
    }
}
```

**Module Structure:**

```
:features:pokemonlist:
  :api/
    PokemonListRoute.kt          # Route objects
  :presentation/
    PokemonListViewModel.kt      # Shared ViewModel
  :ui-material/
    PokemonListScreen.kt         # Material UI
  :wiring-ui-material/
    PokemonListNavigationModule.kt  # Material navigation provider
```

**Navigation Usage in Compose:**

```kotlin
@Composable
fun PokemonListScreen(
    viewModel: PokemonListViewModel,
    onPokemonClick: (Int) -> Unit,
    onBack: () -> Unit
) {
    // Screen implementation
    LazyColumn {
        items(pokemon) { item ->
            PokemonCard(
                pokemon = item,
                onClick = { onPokemonClick(item.id) }
            )
        }
    }
}
```

---

## Pattern 5: Testing Pattern - Detailed Examples

### Mapper Test (Property-Based):

```kotlin
class PokemonDtoMapperTest : FunSpec({
    test("dto to domain preserves all properties") {
        checkAll(Arb.pokemonDto()) { dto ->
            val domain = dto.toDomain()
            domain.id shouldBe dto.id
            domain.name shouldBe dto.name
            domain.height shouldBe dto.height
            domain.weight shouldBe dto.weight
        }
    }

    test("domain to dto round-trip is identity") {
        checkAll(Arb.pokemon()) { original ->
            val roundTrip = original.toDto().toDomain()
            roundTrip shouldBe original
        }
    }
})
```

### ViewModel Test (Turbine + TestScope):

```kotlin
class PokemonListViewModelTest : FunSpec({
    test("loads pokemon on start") {
        val testScope = TestScope()
        val repository = mockk<PokemonListRepository>()
        coEvery { repository.getPokemonList() } returns Either.Right(listOf(mockPokemon))

        val viewModel = PokemonListViewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(),
            viewModelScope = testScope
        )

        viewModel.uiState.test {
            awaitItem() shouldBe PokemonListUiState.Loading
            viewModel.onStart(mockk(relaxed = true))
            testScope.advanceUntilIdle()
            val content = awaitItem()
            content shouldBeInstanceOf PokemonListUiState.Content::class
            content as PokemonListUiState.Content
            content.pokemon.size shouldBe 1
        }
    }

    test("handles repository error") {
        val testScope = TestScope()
        val repository = mockk<PokemonListRepository>()
        coEvery { repository.getPokemonList() } returns Either.Left(RepoError.Network(IOException()))

        val viewModel = PokemonListViewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(),
            viewModelScope = testScope
        )

        viewModel.uiState.test {
            awaitItem() shouldBe PokemonListUiState.Loading
            viewModel.onStart(mockk(relaxed = true))
            testScope.advanceUntilIdle()
            awaitItem() shouldBeInstanceOf PokemonListUiState.Error::class
        }
    }
})
```

### Repository Test (All Error Paths):

```kotlin
class PokemonListRepositoryTest : FunSpec({
    test("returns success when API succeeds") {
        val api = mockk<PokemonListApiService>()
        coEvery { api.fetch() } returns listOf(mockDto)
        val repository = PokemonListRepository(api)

        val result = repository.getPokemonList()
        result.shouldBeRight()
        result.getOrNull()?.size shouldBe 1
    }

    test("returns Network error on IOException") {
        val api = mockk<PokemonListApiService>()
        coEvery { api.fetch() } throws IOException("Network error")
        val repository = PokemonListRepository(api)

        val result = repository.getPokemonList()
        result.shouldBeLeft()
        result.leftOrNull() shouldBeInstanceOf RepoError.Network::class
    }

    test("returns Http error on HttpException") {
        val api = mockk<PokemonListApiService>()
        coEvery { api.fetch() } throws HttpException(404, "Not Found")
        val repository = PokemonListRepository(api)

        val result = repository.getPokemonList()
        result.shouldBeLeft()
        val error = result.leftOrNull()
        error shouldBeInstanceOf RepoError.Http::class
        (error as RepoError.Http).code shouldBe 404
    }

    test("returns Unknown error on unexpected exception") {
        val api = mockk<PokemonListApiService>()
        coEvery { api.fetch() } throws IllegalStateException("Unexpected")
        val repository = PokemonListRepository(api)

        val result = repository.getPokemonList()
        result.shouldBeLeft()
        result.leftOrNull() shouldBeInstanceOf RepoError.Unknown::class
    }
})
```

---

## Pattern 6: Convention Plugins - Detailed Example

**build.gradle.kts for :features:pokemonlist:data:**

```kotlin
plugins {
    id("convention.feature.data")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.features.pokemonlist.api)
            implementation(projects.core.httpclient)
        }
        commonTest.dependencies {
            implementation(libs.kotest.framework.engine)
            implementation(libs.kotest.assertions.core)
            implementation(libs.mockk)
        }
    }
}
```

**What `convention.feature.data` Provides:**

```kotlin
// From convention plugin (build-logic/convention/src/main/kotlin/FeatureDataConventionPlugin.kt)
- Kotlin Multiplatform setup (androidTarget, iosArm64, iosSimulatorArm64, jvm)
- Android library configuration
- Common dependencies:
  - Arrow Core (Either)
  - Kotlinx Coroutines
  - Kotlinx Serialization
  - Ktor Client
- Test dependencies:
  - Kotest
  - MockK
  - Turbine
```

**Plugin Matrix:**

| Plugin | Targets | Common Dependencies | Use For |
|--------|---------|---------------------|---------|
| `convention.feature.api` | All platforms | Kotlinx Serialization, Arrow Core | Contracts, models, routes |
| `convention.feature.data` | All platforms | Ktor, Arrow, Serialization | Repositories, API services |
| `convention.feature.presentation` | All platforms | Coroutines, Lifecycle, Arrow | ViewModels |
| `convention.feature.ui` | Android, iOS, JVM | Compose Multiplatform | Compose screens |
| `convention.feature.wiring` | All platforms | Koin | DI modules |

**When to Use Which Plugin:**

```
IF module defines contracts/models → convention.feature.api
IF module implements data layer    → convention.feature.data
IF module has ViewModels           → convention.feature.presentation
IF module has Compose UI           → convention.feature.ui
IF module wires dependencies       → convention.feature.wiring
```
