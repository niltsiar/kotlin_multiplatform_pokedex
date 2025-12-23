# Dependency Injection Patterns (Koin)

Last Updated: November 26, 2025

> **Canonical Reference**: See [Impl+Factory Pattern](../tech/critical_patterns_quick_ref.md#implfactory-pattern) for core rules.

> Comprehensive code examples for Koin DI with Impl+Factory pattern, wiring modules, and platform-specific source sets.

## Core Principle

**Classes stay DI-agnostic. Wire via Koin `module { }` DSL in wiring modules with platform-specific source sets.**

## Impl + Factory Pattern (MANDATORY)

Every interface MUST follow this pattern:

```kotlin
// :features:jobs:api - Public contract
interface JobRepository {
    suspend fun getJobs(): Either<RepoError, List<Job>>
    suspend fun getById(id: String): Either<RepoError, Job>
}

// :features:jobs:data - Internal implementation
internal class JobRepositoryImpl(
    private val api: JobApiService
) : JobRepository {
    override suspend fun getJobs(): Either<RepoError, List<Job>> =
        Either.catch {
            api.getJobs().jobs.map { it.asDomain() }
        }.mapLeft { it.toRepoError() }
    
    override suspend fun getById(id: String): Either<RepoError, Job> =
        Either.catch {
            api.getJob(id).asDomain()
        }.mapLeft { it.toRepoError() }
}

// Public factory function (Impl + Factory pattern)
fun JobRepository(api: JobApiService): JobRepository = 
    JobRepositoryImpl(api)
```

**Why this pattern:**
- ✅ Enables Gradle compilation avoidance
- ✅ Hides implementation details
- ✅ Simplifies testing (no mocking framework needed for interfaces)
- ✅ Clean boundary between public API and internal implementation

## SavedStateHandle in ViewModels

**Rule:** All ViewModels MUST inject `SavedStateHandle` for state persistence.

**Why:** Preserves state across:
- Android configuration changes (rotation, language switch)
- Android process death (low memory)
- Desktop window state restoration
- iOS SwiftUI view rebuilds (via shared ViewModels)

**Platform Support:**
- ✅ Android: Full native support via `viewModel { }` DSL
- ✅ Desktop/JVM: Manual creation (`SavedStateHandle()`)
- ✅ iOS: App state restoration when ViewModel recreated

**Reference Implementations:**
- [PokemonListViewModel.kt](../../features/pokemonlist/presentation/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/presentation/PokemonListViewModel.kt) - Delegate pattern for scroll position + pagination
- [PokemonDetailViewModel.kt](../../features/pokemondetail/presentation/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/presentation/PokemonDetailViewModel.kt) - Delegate pattern for detail state
- [PokemonListModule.kt](../../features/pokemonlist/wiring/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/wiring/PokemonListModule.kt) - Koin wiring example

**Current Pattern (Delegate - SavedState 1.4.0+):**
```kotlin
import androidx.lifecycle.serialization.saved

class MyViewModel(
    private val repository: MyRepository,
    private val savedStateHandle: SavedStateHandle,  // ← Always inject
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) : ViewModel(viewModelScope), DefaultLifecycleObserver {
    
    // ✨ Single line - automatic persistence (93% code reduction)
    private var persistedState by savedStateHandle.saved { MyPersistedState() }
    
    // State automatically persisted on every write
    fun updateData() {
        persistedState = persistedState.copy(...)
        // No manual persistState() call needed!
    }
}
```

**Requirements:**
- State type must be `@Serializable`
- Import: `androidx.lifecycle.serialization.saved` (NOT `androidx.savedstate.serialization.saved`)
- Dependencies: AndroidX Lifecycle 2.10.0-alpha07+, SavedState 1.4.0+

**⚠️ Property Name Stability:**
Renaming the property breaks state restoration for existing users (delegate uses property name as internal key).

See [ViewModel Patterns Guide](../patterns/viewmodel_patterns.md#savedstatehandle-pattern) for complete examples.

## Wiring Modules with Platform-Specific Source Sets

### commonMain: Business Logic Wiring

```kotlin
// :features:jobs:wiring/commonMain/kotlin/JobsModule.kt
package com.example.features.jobs.wiring

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val jobsDataModule = module {
    // API service
    single<JobApiService> {
        JobApiService(httpClient = get())
    }
    
    // Repository using factory function
    factory<JobRepository> {
        JobRepository(api = get())
    }
}

val jobsPresentationModule = module {
    // ViewModel with injected scope
    factory<JobsViewModel> { params ->
        JobsViewModel(
            repository = get(),
            viewModelScope = params.get()
        )
    }
}
```

### androidMain/jvmMain: UI Wiring (Compose)

```kotlin
// :features:jobs:wiring/androidMain/kotlin/JobsNavigationModule.kt
package com.example.features.jobs.wiring

import com.example.core.navigation.EntryProviderInstaller
import org.koin.dsl.module

val jobsNavigationModule = module {
    single<Set<EntryProviderInstaller>> {
        setOf(
            { 
                entry<JobsRoute> { 
                    JobsScreen(
                        viewModel = koinInject(),
                        onNavigate = { destination ->
                            koinInject<Navigator>().goTo(destination)
                        }
                    )
                }
            }
        )
    }
}

// Aggregate all modules
val jobsModules = listOf(
    jobsDataModule,
    jobsPresentationModule,
    jobsNavigationModule
)
```

### iosMain: iOS-Specific Wiring (if needed)

```kotlin
// :features:jobs:wiring/iosMain/kotlin/JobsModuleIos.kt
package com.example.features.jobs.wiring

import org.koin.dsl.module

// iOS-specific DI if needed (rare)
val jobsIosModule = module {
    // iOS-specific dependencies
    single<JobsPlatformService> {
        IosJobsPlatformService()
    }
}
```

## Complete Feature Wiring Example

### pokemonlist Feature (Production Code)

```kotlin
// :features:pokemonlist:wiring/commonMain/PokemonListModule.kt
package com.example.features.pokemonlist.wiring

import com.example.features.pokemonlist.api.PokemonListRepository
import com.example.features.pokemonlist.data.PokemonListApiService
import com.example.features.pokemonlist.data.PokemonListRepository
import com.example.features.pokemonlist.presentation.PokemonListViewModel
import org.koin.dsl.module

val pokemonListDataModule = module {
    single<PokemonListApiService> {
        PokemonListApiService(httpClient = get())
    }
    
    factory<PokemonListRepository> {
        PokemonListRepository(api = get())
    }
}

val pokemonListPresentationModule = module {
    factory<PokemonListViewModel> { params ->
        PokemonListViewModel(
            repository = get(),
            viewModelScope = params.get()
        )
    }
}

// :features:pokemonlist:wiring/androidMain/PokemonListNavigationModule.kt
package com.example.features.pokemonlist.wiring

import com.example.core.navigation.EntryProviderInstaller
import com.example.core.navigation.Navigator
import com.example.features.pokemonlist.api.PokemonList
import com.example.features.pokemonlist.presentation.PokemonListViewModel
import com.example.features.pokemonlist.ui.PokemonListScreen
import com.example.features.pokemondetail.api.PokemonDetail
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val pokemonListNavigationModule = module {
    single<Set<EntryProviderInstaller>> {
        setOf(
            {
                entry<PokemonList> {
                    val navigator = koinInject<Navigator>()
                    val viewModel = koinInject<PokemonListViewModel> {
                        parametersOf(this@entry.lifecycle.coroutineScope)
                    }
                    
                    PokemonListScreen(
                        viewModel = viewModel,
                        onPokemonClick = { pokemon ->
                            navigator.goTo(PokemonDetail(pokemon.id))
                        }
                    )
                }
            }
        )
    }
}

// Aggregate for easy installation
val pokemonListModules = listOf(
    pokemonListDataModule,
    pokemonListPresentationModule,
    pokemonListNavigationModule
)
```

## Module Composition Patterns

### Direct Composition (Recommended)

```kotlin
// App.kt or main()
fun Application.module() {
    install(Koin) {
        modules(
            coreModules +
            pokemonListModules +
            pokemonDetailModules +
            profileModules
        )
    }
}
```

### Feature-Grouped Modules

```kotlin
// :features:pokemonlist:wiring/PokemonListModules.kt
val pokemonListModules = listOf(
    pokemonListDataModule,
    pokemonListPresentationModule,
    pokemonListNavigationModule
)

// :features:pokemondetail:wiring/PokemonDetailModules.kt
val pokemonDetailModules = listOf(
    pokemonDetailDataModule,
    pokemonDetailPresentationModule,
    pokemonDetailNavigationModule
)

// App-level aggregation
val allFeatureModules = 
    pokemonListModules + 
    pokemonDetailModules + 
    profileModules
```

## Testing with Koin

### Test Module Setup

```kotlin
// PokemonListViewModelTest.kt
class PokemonListViewModelTest : StringSpec({
    lateinit var mockRepository: PokemonListRepository
    lateinit var testScope: TestScope
    lateinit var viewModel: PokemonListViewModel
    
    beforeTest {
        mockRepository = mockk()
        testScope = TestScope()
        
        // Direct instantiation (no Koin in tests)
        viewModel = PokemonListViewModel(
            repository = mockRepository,
            viewModelScope = testScope
        )
    }
    
    "should load pokemons on start" {
        coEvery { mockRepository.loadPage() } returns Either.Right(
            PokemonPage(
                pokemons = persistentListOf(
                    Pokemon(1, "Bulbasaur", "...")
                ),
                hasMore = true
            )
        )
        
        viewModel.uiState.test {
            awaitItem() shouldBe PokemonListUiState.Loading
            
            viewModel.onStart(TestLifecycleOwner())
            testScope.advanceUntilIdle()
            
            awaitItem().shouldBeInstanceOf<PokemonListUiState.Content>().let { state ->
                state.pokemons shouldHaveSize 1
                state.pokemons.first().name shouldBe "Bulbasaur"
            }
            
            cancelAndIgnoreRemainingEvents()
        }
    }
})
```

### Test Module with Koin (Integration Tests)

```kotlin
class PokemonListIntegrationTest : StringSpec({
    lateinit var koin: Koin
    
    beforeTest {
        koin = startKoin {
            modules(
                module {
                    single<PokemonListApiService> { mockk() }
                    factory<PokemonListRepository> {
                        PokemonListRepository(api = get())
                    }
                    factory<PokemonListViewModel> { params ->
                        PokemonListViewModel(
                            repository = get(),
                            viewModelScope = params.get()
                        )
                    }
                }
            )
        }.koin
    }
    
    afterTest {
        stopKoin()
    }
    
    "should integrate repository and viewmodel" {
        val mockApi = koin.get<PokemonListApiService>()
        val viewModel = koin.get<PokemonListViewModel> {
            parametersOf(TestScope())
        }
        
        coEvery { mockApi.getPokemonList(any(), any()) } returns PokemonListDto(
            count = 1292,
            next = null,
            previous = null,
            results = listOf(
                PokemonSummaryDto("bulbasaur", "https://.../1/")
            )
        )
        
        viewModel.uiState.test {
            awaitItem() shouldBe PokemonListUiState.Loading
            viewModel.onStart(TestLifecycleOwner())
            
            awaitItem().shouldBeInstanceOf<PokemonListUiState.Content>()
            cancelAndIgnoreRemainingEvents()
        }
    }
})
```

## Anti-Patterns to Avoid

### ❌ DON'T: Add DI Annotations to Production Classes

```kotlin
// ❌ WRONG - Production class with DI annotations
@Single
class JobRepositoryImpl @Inject constructor(
    private val api: JobApiService
) : JobRepository {
    // ...
}
```

### ❌ DON'T: Use Constructor Injection in ViewModels

```kotlin
// ❌ WRONG - Constructor injection in ViewModel
class HomeViewModel @Inject constructor(
    private val repo: JobRepository
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob())  // ALSO WRONG
}
```

### ❌ DON'T: Create Wrapper Modules

```kotlin
// ❌ WRONG - Wrapper module pattern
val allModules = module {
    includes(
        pokemonListDataModule,
        pokemonListPresentationModule,
        pokemonListNavigationModule
    )
}

// ✅ CORRECT - Direct composition
val allModules = listOf(
    pokemonListDataModule,
    pokemonListPresentationModule,
    pokemonListNavigationModule
)
```

### ✅ DO: Keep Classes DI-Agnostic

```kotlin
// ✅ CORRECT - Production class without annotations
internal class JobRepositoryImpl(
    private val api: JobApiService
) : JobRepository {
    // Clean, framework-agnostic implementation
}

// Factory function for wiring
fun JobRepository(api: JobApiService): JobRepository =
    JobRepositoryImpl(api)

// Koin wiring happens separately
val jobsModule = module {
    factory<JobRepository> {
        JobRepository(api = get())
    }
}
```

## Platform-Specific DI Strategies

### Shared Business Logic (commonMain)

```kotlin
// All platforms share the same business logic wiring
val sharedModule = module {
    single<HttpClient> { createHttpClient() }
    factory<PokemonRepository> { PokemonRepository(api = get()) }
    factory<UserRepository> { UserRepository(api = get()) }
}
```

### Android/JVM UI (androidMain/jvmMain)

```kotlin
// Compose UI wiring with Navigation 3
val androidUiModule = module {
    single<Navigator> { Navigator(startDestination = PokemonList) }
    
    single<Set<EntryProviderInstaller>> {
        // Collect all feature navigation modules
        getAll<Set<EntryProviderInstaller>>().flatten().toSet()
    }
}
```

### iOS SwiftUI (iosMain)

```kotlin
// iOS typically doesn't need UI wiring (SwiftUI views create ViewModels directly)
// But can provide helpers for iOS consumption

val iosModule = module {
    // Helper to create ViewModel from Swift
    factory<PokemonListViewModelHelper> {
        PokemonListViewModelHelper(
            repository = get()
        )
    }
}

// Swift side
class PokemonListView: View {
    @State private var viewModel: PokemonListViewModel
    
    init() {
        let helper = KoinKt.koin.get(objCClass: PokemonListViewModelHelper.self)
        _viewModel = State(initialValue: helper.createViewModel())
    }
}
```

## See Also

- [dependency_injection.md](../tech/dependency_injection.md) — Complete DI strategy guide
- [koin_di_quick_ref.md](../tech/koin_di_quick_ref.md) — Koin quick reference and troubleshooting
- [architecture_patterns.md](architecture_patterns.md) — Module structure and conventions
- [viewmodel_patterns.md](viewmodel_patterns.md) — ViewModel DI patterns
