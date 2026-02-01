---
name: kmp-mobile-expert
description: This skill should be used when working on shared KMP business logic including ViewModels, repositories, modules, and iOS bridging. Use for architecture decisions and shared layer implementation.
---

# KMP Mobile Expert Skill

Expert guidance for implementing shared Kotlin Multiplatform business logic across Android, iOS, Desktop, and Server. Focus on ViewModels, repositories, error handling, and iOS integration.

## When to Use

Use this skill when:
- Implementing or refactoring ViewModels
- Creating repositories with error handling
- Designing vertical slice module architecture
- Setting up iOS exports from KMP modules
- Writing Koin DI configuration
- Making decisions about shared vs platform-specific code
- Troubleshooting iOS-KMP integration issues
- Deciding between Direct Integration vs Wrapper pattern

## Mode Detection

| User Request | Mode | Context |
|--------------|------|---------|
| "Create a ViewModel for..." | VM_MODE | Implementing lifecycle-aware presentation layer |
| "Implement a repository for..." | REPO_MODE | Data layer with Either error handling |
| "How should I export this to iOS?" | IOS_MODE | iOS framework export decisions |
| "Design the module structure for..." | ARCH_MODE | Vertical slice architecture |
| "My ViewModel isn't working on iOS" | IOS_MODE | iOS troubleshooting |
| "Should I use Either or Result?" | REPO_MODE | Error handling patterns |
| "How do I persist state in ViewModel?" | VM_MODE | State persistence patterns |

---

## Repository Implementation (REPO_MODE)

### Core Pattern: Either Boundary

All repositories MUST return `Either<RepoError, T>` with proper error mapping.

```kotlin
// Define sealed error hierarchy in :api module
sealed interface RepoError {
    data class Network(val cause: Throwable) : RepoError
    data class Http(val code: Int, val message: String?) : RepoError
    data class Unknown(val cause: Throwable) : RepoError
}

// Repository interface (in :api module)
interface PokemonListRepository {
    suspend fun loadPage(limit: Int, offset: Int): Either<RepoError, PokemonPage>
}

// Implementation with factory function (in :data module)
internal class PokemonListRepositoryImpl(
    private val apiService: PokemonListApiService
) : PokemonListRepository {
    
    override suspend fun loadPage(limit: Int, offset: Int): Either<RepoError, PokemonPage> =
        withContext(Dispatchers.IO) {
            catch({
                val dto = apiService.getPokemonList(limit, offset)
                Either.Right(dto.toDomain())  // Map DTO → Domain
            }) { throwable ->
                Either.Left(throwable.toRepoError())
            }
        }
}

// Public factory function
fun PokemonListRepository(apiService: PokemonListApiService): PokemonListRepository =
    PokemonListRepositoryImpl(apiService)

// Error mapping extension
private fun Throwable.toRepoError(): RepoError = when (this) {
    is ClientRequestException -> RepoError.Http(response.status.value, message)
    is ServerResponseException -> RepoError.Http(response.status.value, message)
    is HttpRequestTimeoutException,
    is ConnectTimeoutException,
    is SocketTimeoutException -> RepoError.Network
    else -> RepoError.Unknown(this)
}
```

### Key Requirements

1. **Interface in `:api`**: Repository interfaces belong in `:api` module
2. **Implementation in `:data`**: `internal class <Interface>Impl` pattern
3. **Factory function**: Public function `fun <Interface>(...): <Interface>`
4. **Either return type**: Never `Result`, never nullable, never throwing
5. **DTO mapping**: Map DTOs to domain models at repository boundary
6. **Error mapping**: Use `catch { }` with `.mapLeft { it.toRepoError() }`
7. **Cancellation**: `Either.catch` automatically respects `CancellationException`

---

## ViewModel Implementation (VM_MODE)

### Core Pattern: Lifecycle-Aware with SavedStateHandle

All ViewModels MUST follow this canonical pattern:

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
) : ViewModel(viewModelScope),  // ← Pass scope to superclass
    DefaultLifecycleObserver,   // ← Lifecycle awareness
    UiStateHolder<PokemonListUiState, PokemonListUiEvent> {  // ← Optional interface
    
    // ✨ Automatic state persistence with delegate
    private var persistedState by savedStateHandle.saved { PokemonListPersistedState() }

    private val _uiState = MutableStateFlow<PokemonListUiState>(PokemonListUiState.Loading)
    val uiState: StateFlow<PokemonListUiState> = _uiState.asStateFlow()
    
    // ⚠️ NEVER perform work in init {}
    
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

### Key Requirements

1. **Extend ViewModel**: Pass `viewModelScope` to superclass constructor
2. **Implement DefaultLifecycleObserver**: For lifecycle-aware operations
3. **Inject SavedStateHandle**: Required for state persistence
4. **Use `by saved` delegate**: Automatic state serialization
5. **NO work in `init`**: Use `onStart(owner: LifecycleOwner)` instead
6. **Immutable UI state**: Use `kotlinx.collections.immutable` types
7. **Consume Either from repos**: Map to UI state sealed classes

### Koin Wiring Pattern

```kotlin
// In :wiring module
val pokemonListModule = module {
    factory { PokemonListApiService(httpClient = get()) }
    
    factory<PokemonListRepository> {
        PokemonListRepository(apiService = get())  // Uses factory function
    }
    
    viewModel {
        PokemonListViewModel(
            repository = get(),
            savedStateHandle = SavedStateHandle()
        )
    }
}
```

---

## iOS Export (IOS_MODE)

### What to Export

```kotlin
// In :shared/build.gradle.kts
kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    iosTarget.binaries.framework {
        baseName = "Shared"
        isStatic = true
        
        // ✅ Export public contracts
        export(projects.features.pokemonlist.api)
        
        // ✅ Export presentation (ViewModels + UI state)
        export(projects.features.pokemonlist.presentation)
        
        // ✅ Export core utilities
        export(projects.core.domain)
        export(projects.core.util)
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.features.pokemonlist.api)
            api(projects.features.pokemonlist.presentation)
        }
    }
}
```

### Export Rules

| Module Type | Export to iOS? | Reason |
|-------------|----------------|--------|
| `:api` | ✅ YES | Public contracts, domain models |
| `:presentation` | ✅ YES | ViewModels, UI state |
| `:data` | ❌ NO | Internal implementation |
| `:ui-material` | ❌ NO | Compose-specific |
| `:ui-unstyled` | ❌ NO | Compose-specific |
| `:wiring` | ❌ NO | Koin DI modules |
| `:core:designsystem` | ❌ NO | Compose components |

### iOS Integration Pattern (Direct Integration)

**SwiftUI side:**
```swift
// Get ViewModel from Koin via helper
private var viewModel = KoinIosKt.getPokemonListViewModel()

// Bridge StateFlow to SwiftUI
@State private var uiState: PokemonListUiState = PokemonListUiStateLoading()

var body: some View {
    content
        .onAppear {
            // Direct lifecycle calls (aligned with Android KMP ViewModel guide)
            viewModel.onStart(owner: DummyLifecycleOwner())
        }
        .onDisappear {
            viewModel.onStop(owner: DummyLifecycleOwner())
        }
        .task {
            // SKIE: StateFlow → AsyncSequence
            for await state in viewModel.uiState {
                self.uiState = state
            }
        }
}
```

**Kotlin helper (in `shared/src/iosMain`):**
```kotlin
fun getPokemonListViewModel(): PokemonListViewModel {
    return KoinPlatform.getKoin().get()
}
```

### Parametric ViewModels

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

**Swift usage:**
```swift
self.viewModel = KoinIosKt.getPokemonDetailViewModel(pokemonId: Int32(pokemonId))
```

---

## Architecture (ARCH_MODE)

### Vertical Slice Pattern

```
:features:<feature>:api              → Public contracts (interfaces, domain models)
:features:<feature>:data             → Network, DTOs, repositories
:features:<feature>:presentation     → ViewModels, UI state (shared with iOS)
:features:<feature>:ui-material      → Material Design 3 UI (Android + JVM + iOS Compose)
:features:<feature>:ui-unstyled      → Compose Unstyled UI
:features:<feature>:wiring           → Business DI (Koin modules)
:features:<feature>:wiring-ui-material   → Material navigation wiring
:features:<feature>:wiring-ui-unstyled   → Unstyled navigation wiring
```

### Module Independence Rules

1. **No feature → feature impl dependencies**
   - ✅ `:profile:impl` → `:auth:api`
   - ❌ `:profile:impl` → `:auth:impl`

2. **Each feature owns its network layer**
   - Each feature defines its own API service
   - Each feature defines its own DTOs
   - Even if calling same endpoint, define separate DTOs

3. **Domain models in :api only if shared**
   - If model used by multiple features → `:api`
   - If internal to feature → `:data/domain/`

### When to Create `:domain` Module

Optional module for complex business logic:
```
:features:<feature>:domain       → Use cases, validators, business rules
```

**When to create**: Feature has use cases orchestrating 2+ repositories or enforcing complex business rules. Most features won't need this—call repositories directly from ViewModels.

---

## Critical Anti-Patterns

| Anti-Pattern | Correct Pattern | Why It Matters |
|--------------|-----------------|----------------|
| `suspend fun get(): T?` | `suspend fun get(): Either<RepoError, T>` | Type-safe error handling |
| `init { loadData() }` | `override fun onStart(owner) { loadData() }` | Lifecycle-aware |
| `private val scope = CoroutineScope(...)` | Pass `viewModelScope` to constructor | Prevents leaks |
| `return Result.success(data)` | `Either.Right(data)` | Consistent error boundary |
| `class RepositoryImpl` (public) | `internal class RepositoryImpl` | Gradle compilation avoidance |
| No factory function | `fun Repository(...): Repository = RepositoryImpl(...)` | Simplifies DI wiring |
| Export `:data` to iOS | Export only `:api` and `:presentation` | iOS boundary violation |
| Use `@State` for ViewModel | Use `@StateObject` wrapper (if complex) | SwiftUI lifecycle |
| Store scope as field | Pass to constructor, not stored | ViewModel pattern violation |
| Swallow `CancellationException` | Use `Either.catch` (auto-respects cancellation) | Coroutine cancellation |

---

## Quick Reference

### Key Files by Pattern

| Pattern | File | Purpose |
|---------|------|---------|
| ViewModel | `PokemonListViewModel.kt` | Lifecycle-aware pattern |
| Repository | `PokemonListRepository.kt` | Either boundary interface |
| Repository Impl | `PokemonListRepositoryImpl.kt` | Impl + Factory pattern |
| Koin Wiring | `PokemonListModule.kt` | DI configuration |
| iOS Integration | `ios_integration.md` | Complete iOS guide |
| Architecture | `conventions.md` | Master architecture reference |

### Common Imports

```kotlin
// ViewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewModelScope

// Either
import arrow.core.Either
import arrow.core.raise.catch

// Coroutines
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Immutable collections
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

// DI
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
```

---

## Cross-References

### Documentation Links

| Topic | Document | Location |
|-------|----------|----------|
| Complete architecture | `conventions.md` | docs/tech/conventions.md |
| All 6 core patterns | `critical_patterns_quick_ref.md` | docs/tech/critical_patterns_quick_ref.md |
| iOS integration details | `ios_integration.md` | docs/tech/ios_integration.md |
| Testing strategy | `testing_strategy.md` | docs/tech/testing_strategy.md |
| Koin DI patterns | `dependency_injection.md` | docs/tech/dependency_injection.md |

### Implementation Examples

| Feature | Pattern | Key Files |
|---------|---------|-----------|
| pokemonlist | Standard list | `features/pokemonlist/` |
| pokemondetail | Parametric ViewModel | `features/pokemondetail/` |

### Anti-Pattern Quick Check

Before writing code, verify:

- [ ] Repository returns `Either<RepoError, T>` (not Result or nullable)
- [ ] ViewModel implements `DefaultLifecycleObserver`
- [ ] ViewModel has `SavedStateHandle` injected
- [ ] ViewModel uses `by saved` delegate for state
- [ ] NO work in ViewModel `init` block
- [ ] Repository uses `internal class <Name>Impl` pattern
- [ ] Repository has public factory function
- [ ] Koin uses factory function, not constructor directly
- [ ] iOS exports only `:api` and `:presentation`
- [ ] StateFlow uses `kotlinx.collections.immutable` types

---

## Command Reference

```bash
# Primary validation (Android build + all tests)
./gradlew :composeApp:assembleDebug test --continue

# Run specific feature tests
./gradlew :features:pokemonlist:presentation:testDebugUnitTest
./gradlew :features:pokemonlist:data:testDebugUnitTest

# Check dependency updates
./gradlew dependencyUpdates
```
