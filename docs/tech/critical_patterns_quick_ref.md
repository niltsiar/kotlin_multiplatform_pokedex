# Critical Patterns Quick Reference

**Last Updated:** December 22, 2025

> **Purpose**: Canonical source of truth for the 6 core architectural patterns in this Kotlin Multiplatform project. All other documentation should link to this file rather than duplicating these definitions.

---

## ViewModel Pattern

**Rule**: All ViewModels MUST implement `DefaultLifecycleObserver` for lifecycle-aware operations.

### Required Elements

1. **Extend `androidx.lifecycle.ViewModel`**
2. **Implement `DefaultLifecycleObserver`** for lifecycle awareness
3. **Pass `viewModelScope` as constructor parameter** with default value
4. **Pass scope to superclass constructor** (NOT stored as field)
5. **Inject `SavedStateHandle` and use `by saved` delegate** for state persistence
6. **NO work in `init` block**
7. **Override `onStart(owner: LifecycleOwner)`** for initialization logic
8. **Implement `UiStateHolder<S, E>`**
9. **Use `kotlinx.collections.immutable` types** in UI state

### Canonical Example

```kotlin
import androidx.lifecycle.serialization.saved

class PokemonListViewModel(
    private val repository: PokemonListRepository,
    private val savedStateHandle: SavedStateHandle,
    viewModelScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )
) : ViewModel(viewModelScope),  // ← Pass to superclass constructor
    DefaultLifecycleObserver,   // ← Lifecycle awareness
    UiStateHolder<PokemonListUiState, PokemonListUiEvent> {
    
    // ✨ Automatic state persistence with delegate
    private var persistedState by savedStateHandle.saved { PokemonListPersistedState() }
    
    private val _uiState = MutableStateFlow<PokemonListUiState>(PokemonListUiState.Loading)
    override val uiState: StateFlow<PokemonListUiState> = _uiState
    
    // ⚠️ NEVER perform work in init {}
    
    // Lifecycle-aware initialization (replaces repeatOnLifecycle pattern)
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
                    persistedState = persistedState.copy(
                        pokemons = page.pokemons  // ← Automatically persisted
                    )
                    _uiState.value = PokemonListUiState.Content(
                        pokemons = page.pokemons.toImmutableList(),
                        hasMore = page.hasMore
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
}
```

**Note**: ViewModels implementing `DefaultLifecycleObserver` are automatically registered with the lifecycle in Compose (via `LocalLifecycleOwner`) and in iOS (via custom `LifecycleViewModelStoreOwner`).

### Common Violations

| ❌ Anti-Pattern | ✅ Correct Pattern |
|----------------|-------------------|
| `class MyViewModel : ViewModel()` | `class MyViewModel(...) : ViewModel(...), DefaultLifecycleObserver` |
| `private val scope = CoroutineScope(...)` | Pass `viewModelScope` to constructor |
| No `SavedStateHandle` parameter | Inject `SavedStateHandle` and use `by saved` delegate |
| `init { loadData() }` | `override fun onStart(owner: LifecycleOwner) { loadData() }` |
| `fun start(lifecycle: Lifecycle) { repeatOnLifecycle... }` | `override fun onStart(owner: LifecycleOwner) { ... }` |
| `_state: MutableStateFlow<List<T>>` | `_state: MutableStateFlow<ImmutableList<T>>` |
| Work in constructor | Work in `onStart()`lifecycle callback |

---

## Either Boundary Pattern

**Rule**: Repositories MUST return `Either<RepoError, T>`. NEVER throw exceptions, return null, or use `Result`.

### Required Elements

1. **Return type**: `Either<RepoError, T>`
2. **Error handling**: `Either.catch { }` wraps throwing code
3. **Error mapping**: `.mapLeft { it.toRepoError() }`
4. **Sealed error hierarchy** per feature
5. **DTO → Domain mapping** at boundary
6. **Respect cancellation**: `Either.catch` automatically handles `CancellationException`

### Canonical Example

```kotlin
// Define sealed error hierarchy
sealed interface RepoError {
    data class Network(val cause: Throwable) : RepoError
    data class Http(val code: Int, val message: String) : RepoError
    data class Unknown(val cause: Throwable) : RepoError
}

// Extension to map exceptions
fun Throwable.toRepoError(): RepoError = when (this) {
    is IOException -> RepoError.Network(this)
    is ClientRequestException -> RepoError.Http(
        code = response.status.value,
        message = message ?: "HTTP error"
    )
    else -> RepoError.Unknown(this)
}

// Repository implementation
interface JobRepository {
    suspend fun getJobs(): Either<RepoError, List<Job>>
}

internal class JobRepositoryImpl(
    private val api: JobApiService
) : JobRepository {
    override suspend fun getJobs(): Either<RepoError, List<Job>> =
        Either.catch {
            api.getJobs().jobs.map { it.toDomain() }
        }.mapLeft { it.toRepoError() }
}
```

### Common Violations

| ❌ Anti-Pattern | ✅ Correct Pattern |
|----------------|-------------------|
| `suspend fun get(): T?` | `suspend fun get(): Either<RepoError, T>` |
| `suspend fun get(): Result<T>` | `suspend fun get(): Either<RepoError, T>` |
| `suspend fun get(): T` (throws) | `Either.catch { ... }.mapLeft { ... }` |
| `try { ... } catch { null }` | `Either.catch { ... }.mapLeft { it.toRepoError() }` |
| Swallow `CancellationException` | `Either.catch` respects cancellation automatically |

---

## Impl+Factory Pattern

**Rule**: All interfaces MUST be implemented by internal `*Impl` classes and exposed via public factory functions.

### Required Elements

1. **Interface**: Public, defines contract
2. **Implementation**: `internal class <Interface>Impl` (never public)
3. **Factory function**: `fun <Interface>(...): <Interface> = <Interface>Impl(...)`
4. **Factory is public**, implementation is internal
5. **Koin wiring**: Uses factory function in `module { }`

### Canonical Example

```kotlin
// Interface (public contract)
interface JobRepository {
    suspend fun getJobs(): Either<RepoError, List<Job>>
}

// Implementation (internal)
internal class JobRepositoryImpl(
    private val api: JobApiService,
    private val dispatcher: CoroutineDispatcher
) : JobRepository {
    override suspend fun getJobs(): Either<RepoError, List<Job>> =
        withContext(dispatcher) {
            Either.catch {
                api.getJobs().jobs.map { it.toDomain() }
            }.mapLeft { it.toRepoError() }
        }
}

// Factory function (public)
fun JobRepository(
    api: JobApiService,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): JobRepository = JobRepositoryImpl(api, dispatcher)

// Koin wiring (uses factory function)
val jobsDataModule = module {
    single<JobApiService> { JobApiService(get()) }
    factory<JobRepository> { JobRepository(get(), Dispatchers.IO) }
}
```

### Common Violations

| ❌ Anti-Pattern | ✅ Correct Pattern |
|----------------|-------------------|
| `class JobRepositoryImpl : JobRepository` (public) | `internal class JobRepositoryImpl : JobRepository` |
| No factory function | `fun JobRepository(...): JobRepository = JobRepositoryImpl(...)` |
| `single { JobRepositoryImpl(...) }` | `single<JobRepository> { JobRepository(...) }` |
| DI annotations on class (`@Singleton`) | Keep classes DI-agnostic, wire in modules |
| Exposing implementation type | Factory returns interface type only |

---

## Navigation 3 Pattern

**Rule**: Use Koin Navigation 3 DSL with `navigation<Route>` in modules and `koinEntryProvider()` in app.

### Required Elements

1. **Route objects**: Plain Kotlin objects/data classes (NO `@Serializable`)
2. **Navigator**: Manages explicit back stack (`SnapshotStateList`)
3. **Koin Navigation 3 DSL**: Use `navigation<Route>` in wiring modules (commonMain)
4. **Imports**: `org.koin.dsl.navigation3.navigation` and `org.koin.compose.navigation3.koinEntryProvider`
5. **Metadata-based animations**: `NavDisplay.transitionSpec()` for enter, `NavDisplay.popTransitionSpec()` for exit
6. **Automatic aggregation**: `koinEntryProvider()` collects all `navigation<T>` entries automatically
7. **Parametric ViewModel scoping**: For routes with parameters, MUST use `koinViewModel(key = "screen_${route.param}", ...)`
8. **Lifecycle registration**: Use `DisposableEffect(route.param)` to scope lifecycle observer registration to route instance

### Canonical Example

```kotlin
// Route object in :api module (plain Kotlin)
data class PokemonDetail(val id: Int)

// Koin wiring module in :wiring-ui/commonMain
import org.koin.dsl.navigation3.navigation
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.module

val pokemonDetailNavigationModule = module {
    // Declare navigation entry using Koin's Navigation 3 DSL
    navigation<PokemonDetail>(
        metadata = NavDisplay.transitionSpec(
            slideInHorizontally(initialOffsetX = { it }) +
            fadeIn(animationSpec = tween(300))
        ) + NavDisplay.popTransitionSpec(
            slideOutHorizontally(targetOffsetX = { it }) +
            fadeOut(animationSpec = tween(300))
        )
    ) { route ->
        val navigator: Navigator = koinInject()
        // CRITICAL: Use unique key for parametric routes
        val viewModel = koinViewModel<PokemonDetailViewModel>(
            key = "pokemon_detail_${route.id}"
        ) { parametersOf(route.id) }
        val lifecycleOwner = LocalLifecycleOwner.current
        
        // Register ViewModel with lifecycle (implements DefaultLifecycleObserver)
        // Key by route.id to properly dispose when navigating to different Pokemon
        DisposableEffect(route.id) {
            lifecycleOwner.lifecycle.addObserver(viewModel)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(viewModel)
            }
        }
        
        PokemonDetailScreen(
            pokemonId = route.id,
            viewModel = viewModel,
            onBack = { navigator.goBack() }
        )
    }
}

// App.kt - Automatic aggregation
import org.koin.compose.navigation3.koinEntryProvider

@Composable
fun App() {
    KoinApplication(/* modules */) {
        val navigator: Navigator = koinInject()
        val entryProvider = koinEntryProvider()  // Auto-collects all navigation<T>
        
        NavDisplay(
            backStack = navigator.backStack,
            onBack = { navigator.goBack() },
            entryProvider = entryProvider
        )
    }
}
```

### Common Violations

| ❌ Anti-Pattern | ✅ Correct Pattern |
|----------------|-------------------|
| `@Serializable` on route objects | Plain Kotlin objects/data classes |
| Wrong import: `org.koin.compose.module.navigation` | `org.koin.dsl.navigation3.navigation` |
| Wrong import: `org.koin.dsl.navigation3.koinEntryProvider` | `org.koin.compose.navigation3.koinEntryProvider` |
| Manual `Set<EntryProviderInstaller>` collection | Use `koinEntryProvider()` auto-aggregation |
| No `key` for parametric ViewModels | `koinViewModel(key = "screen_${route.id}")` |
| Direct transition parameters to `entry<T>()` | Use `metadata = NavDisplay.transitionSpec(...)` |
| UI screens in `:api` module | UI in `:ui`, routes in `:api` |
| Export navigation to iOS | Navigation is Compose-specific, NOT exported |
| String-based routes | Type-safe route objects |
| No lifecycle registration for ViewModels | Use `DisposableEffect(route.param)` pattern |

---

## Testing Pattern

**Rule**: Use Kotest + MockK in `androidUnitTest/`, prioritize property-based tests, use Turbine for flows.

### Required Elements

1. **Primary location**: `androidUnitTest/` for ALL business logic tests
2. **Framework**: Kotest (assertions, property tests, framework)
3. **Mocking**: MockK (JVM/Android only)
4. **Property tests**: 30-40% overall coverage (100% for mappers)
5. **Flow testing**: Turbine (NEVER `Thread.sleep()` or `delay()`)
6. **TestScope injection**: Pass `testScope` to ViewModel constructor
7. **Test types**: Unit (repositories, ViewModels), property (mappers, invariants), screenshot (Roborazzi)

### Canonical Example

```kotlin
// Property-based test (mapper)
class PokemonMapperSpec : FreeSpec({
    "DTO to domain preserves all properties" - {
        checkAll(Arb.pokemonDto()) { dto ->
            val domain = dto.asDomain()
            
            domain.id shouldBe dto.id
            domain.name shouldBe dto.name.lowercase()
            domain.imageUrl shouldContain dto.id.toString()
        }
    }
})

// ViewModel test with Turbine
class PokemonListViewModelSpec : FreeSpec({
    lateinit var repository: PokemonListRepository
    lateinit var testScope: TestScope
    lateinit var viewModel: PokemonListViewModel
    
    beforeTest {
        repository = mockk()
        testScope = TestScope()
        viewModel = PokemonListViewModel(repository, testScope)
    }
    
    "should load pokemon successfully" {
        val pokemons = listOf(Pokemon(1, "Bulbasaur", "url"))
        coEvery { repository.getPokemons(0, 20) } returns pokemons.right()
        
        viewModel.uiState.test {
            awaitItem() shouldBe PokemonListUiState.Loading
            
            viewModel.loadInitialPage()  // Call public methods directly
            testScope.advanceUntilIdle()
            
            val content = awaitItem().shouldBeInstanceOf<PokemonListUiState.Content>()
            content.pokemons shouldHaveSize 1
            content.pokemons.first().name shouldBe "Bulbasaur"
            
            cancelAndIgnoreRemainingEvents()
        }
    }
})
```

### Common Violations

| ❌ Anti-Pattern | ✅ Correct Pattern |
|----------------|-------------------|
| `Thread.sleep(100)` in tests | Use Turbine + `testScope.advanceUntilIdle()` |
| `delay(100)` in tests | Use Turbine for deterministic flow testing |
| Tests in `commonTest/` | Use `androidUnitTest/` for business logic |
| No property tests for mappers | 100% property test coverage for mappers |
| Manual cast after `shouldBeInstanceOf` | Use smart cast directly (Kotest provides it) |
| `Dispatchers.setMain()` with testScope | Not needed—testScope injection is sufficient |

---

## Convention Plugins Pattern

**Rule**: Use shared configuration utilities and base plugin composition following Now in Android patterns.

### Required Elements

1. **Shared configuration utilities** in `build-logic/convention/src/main/kotlin/com/minddistrict/multiplatformpoc/`:
   - `KotlinMultiplatform.kt` - `configureKmpTargets()` centralizes target configuration
   - `TestConfiguration.kt` - `configureTests()` standardizes test setup
   - `ComposeConfiguration.kt` - `configureComposeMultiplatform()` for Compose dependencies
   - `ProjectExtensions.kt` - `libs` property and `getVersion()`/`getLibrary()` extensions
2. **Base plugin composition**: `convention.feature.base` provides KMP targets, tests, common dependencies
3. **Layer plugins compose base**: `convention.feature.api`/`data`/`presentation`/`wiring` extend base
4. **UI plugin explicit targets**: Android + JVM + iOS (not composing base due to target differences)
5. **Single source of truth** for dependency versions and target configurations

### Canonical Example

```kotlin
// Shared configuration utility
// build-logic/convention/src/main/kotlin/.../KotlinMultiplatform.kt
internal fun Project.configureKmpTargets() {
    extensions.configure<KotlinMultiplatformExtension> {
        jvm()
        androidTarget {
            @OptIn(ExperimentalKotlinGradlePluginApi::class)
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }
        
        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = "shared"
                isStatic = true
            }
        }
    }
}

// Base plugin composition
// build-logic/convention/src/main/kotlin/.../FeatureBaseConventionPlugin.kt
class FeatureBaseConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
            }
            
            configureKmpTargets()
            configureTests()
            
            // Common dependencies for all features
            dependencies {
                commonMain {
                    implementation(libs.getLibrary("arrow-core"))
                    implementation(libs.getLibrary("kotlinx-coroutines-core"))
                    implementation(libs.getLibrary("kotlinx-collections-immutable"))
                }
            }
        }
    }
}

// Layer plugin composes base
// build-logic/convention/build.gradle.kts
gradlePlugin {
    plugins {
        register("convention.feature.data") {
            id = "convention.feature.data"
            implementationClass = "FeatureDataConventionPlugin"
        }
    }
}

// build-logic/convention/src/main/kotlin/.../FeatureDataConventionPlugin.kt
class FeatureDataConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("convention.feature.base")  // Compose base plugin
                apply("org.jetbrains.kotlin.plugin.serialization")
            }
            
            dependencies {
                commonMain {
                    implementation(libs.getLibrary("ktor-client-core"))
                }
            }
        }
    }
}
```

### Common Violations

| ❌ Anti-Pattern | ✅ Correct Pattern |
|----------------|-------------------|
| Duplicate target configuration | Use `configureKmpTargets()` utility |
| Hardcoded dependency versions | Use `libs.getLibrary()` with version catalog |
| Copy-paste plugin logic | Extract to shared utilities |
| Layer plugins duplicate base config | Compose `convention.feature.base` |
| Inconsistent test setup | Use `configureTests()` utility |

---

## Usage Guidelines

### For Documentation Authors

When writing guides, use anchored links to this file:

```markdown
See [ViewModel Pattern](critical_patterns_quick_ref.md#viewmodel-pattern) for complete rules.
```

### For LLM Agents

When implementing code:
1. Read this file first for canonical pattern definitions
2. Verify your implementation matches ALL required elements
3. Check against common violations table
4. Link to specific pattern sections in code comments if complex

### For Documentation Agent

When auditing consistency:
1. Extract key rules from each pattern section
2. Compare semantically against references in other documents
3. Propose consolidation: replace detailed explanations with anchored links to this file
4. Verify all links resolve and content matches semantically

---

## Maintenance

**This file is the single source of truth**. When patterns evolve:
1. Update this file first
2. Run Documentation Agent audit to find all references
3. Update references to maintain consistency
4. Never duplicate pattern definitions in other files

**Last Review:** November 26, 2025
