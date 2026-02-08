---
name: kmp-mobile-expert
description: "Implement shared Kotlin Multiplatform business logic with ViewModels, repositories, error handling (Either), and iOS integration. Use when: (1) Creating ViewModels with lifecycle awareness and SavedStateHandle, (2) Implementing repositories with Either<RepoError,T> patterns, (3) Setting up iOS exports from KMP modules, (4) Writing Koin DI configuration, (5) Troubleshooting iOS-KMP integration, (6) Deciding between Direct Integration vs Wrapper pattern"
---

# KMP Mobile Expert Skill

Expert guidance for shared Kotlin Multiplatform business logic across Android, iOS, Desktop, and Server.

## When to Use

- Creating ViewModels with lifecycle awareness and SavedStateHandle
- Implementing repositories with Either<RepoError, T> patterns
- Setting up iOS exports from KMP modules
- Writing Koin DI configuration
- Troubleshooting iOS-KMP integration issues
- Deciding between Direct Integration vs Wrapper pattern

## Mode Detection

| User Request | Reference File | Load When |
|--------------|----------------|-----------|
| "Create a ViewModel" | [viewmodel-patterns.md](references/viewmodel-patterns.md) | MANDATORY - Read before implementing |
| "Implement a repository" | [repository-patterns.md](references/repository-patterns.md) | MANDATORY - Read before implementing |
| "Export to iOS" / "iOS integration" | [ios-export.md](references/ios-export.md) | MANDATORY - Read before setting up exports |
| "Design module structure" | See Architecture section below | N/A |

**MANDATORY - READ ENTIRE FILE**: Before implementing repositories, you MUST read [repository-patterns.md](references/repository-patterns.md) (~100 lines) for complete Either boundary pattern.

**MANDATORY - READ ENTIRE FILE**: Before implementing ViewModels, you MUST read [viewmodel-patterns.md](references/viewmodel-patterns.md) (~120 lines) for lifecycle-aware pattern.

**Do NOT load** `repository-patterns.md` for ViewModel-only tasks.
**Do NOT load** `viewmodel-patterns.md` for repository-only tasks.

---

## Architecture Overview

### Vertical Slice Pattern

```
:features:<feature>:api              → Public contracts (interfaces, domain models)
:features:<feature>:data             → Network, DTOs, repositories
:features:<feature>:presentation     → ViewModels, UI state (shared with iOS)
:features:<feature>:ui-material      → Material Design 3 UI
:features:<feature>:ui-unstyled    → Compose Unstyled UI
:features:<feature>:wiring          → Business DI (Koin modules)
```

### Module Independence Rules

1. **No feature → feature impl dependencies**
   - ✅ `:profile:impl` → `:auth:api`
   - ❌ `:profile:impl` → `:auth:impl`

2. **Each feature owns its network layer**
   - Each feature defines its own API service
   - Each feature defines its own DTOs

3. **Export only `:api` and `:presentation` to iOS**
   - ❌ NEVER export `:data`, `:ui`, `:wiring`

---

## Essential Workflows

### Workflow 1: Create ViewModel with Lifecycle Awareness

All ViewModels in this project MUST be lifecycle-aware to support proper initialization and state preservation.

1. **Define dependencies**: Inject repository, `SavedStateHandle`, and `CoroutineScope` (with default value).
2. **Implement interfaces**: Extend `ViewModel(viewModelScope)` and implement `DefaultLifecycleObserver`.
3. **Handle state persistence**: Use `by saved` delegate for automatic state persistence.
4. **Lifecycle initialization**: Override `onStart(owner)` for initial data loading. **NEVER use `init` block for work.**

```kotlin
class PokemonListViewModel(
    private val repository: PokemonListRepository,
    private val savedStateHandle: SavedStateHandle,
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) : ViewModel(viewModelScope), DefaultLifecycleObserver {
    private val _uiState = MutableStateFlow<PokemonListUiState>(PokemonListUiState.Loading)
    val uiState: StateFlow<PokemonListUiState> = _uiState.asStateFlow()

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        viewModelScope.launch {
            repository.loadPage(20, 0).fold(
                { _uiState.value = PokemonListUiState.Error(it) },
                { _uiState.value = PokemonListUiState.Content(it) }
            )
        }
    }
}
```
*For complete ViewModel patterns, see @kmp-presentation.*

### Workflow 2: Implement Repository with Either Boundary

Repositories bridge data sources to domain logic, ensuring type-safe error handling without exceptions.

1. **Define interface**: Place in `:api` module, returning `Either<RepoError, T>`.
2. **Implement class**: Create `internal class <Name>Impl` in `:data` module.
3. **Map errors**: Use `catch { ... }` with `.mapLeft { it.toRepoError() }`.
4. **Expose factory**: Create a public factory function in `:data`.

```kotlin
// In :api
interface PokemonListRepository {
    suspend fun loadPage(limit: Int, offset: Int): Either<RepoError, PokemonPage>
}

// In :data
internal class PokemonListRepositoryImpl(private val api: PokemonListApiService) : PokemonListRepository {
    override suspend fun loadPage(limit: Int, offset: Int): Either<RepoError, PokemonPage> =
        catch({ Either.Right(api.getPokemonList(limit, offset).toDomain()) }) { it.toRepoError().left() }
}

fun PokemonListRepository(api: PokemonListApiService): PokemonListRepository = PokemonListRepositoryImpl(api)
```
*For error handling patterns, see @kmp-data-layer.*

### Workflow 3: Set Up iOS Exports for KMP Feature

Only specific layers should be visible to iOS to maintain a clean boundary.

1. **Identify modules**: Export only `:api` (contracts) and `:presentation` (ViewModels).
2. **Configure `:shared`**: Update `shared/build.gradle.kts` to `export` modules.
3. **Update dependencies**: Add modules as `api` dependencies in `commonMain`.

```kotlin
// shared/build.gradle.kts
iosTarget.binaries.framework {
    baseName = "Shared"
    export(projects.features.pokemonlist.api)
    export(projects.features.pokemonlist.presentation)
}
sourceSets {
    commonMain.dependencies {
        api(projects.features.pokemonlist.api)
        api(projects.features.pokemonlist.presentation)
    }
}
```
*For iOS integration details, see @kmp-ios.*

### Workflow 4: Configure Koin DI for Mobile Feature

Dependency injection connects all layers and supports platform-specific injection for iOS.

1. **Create module**: Define in `:wiring` module using the `module { ... }` DSL.
2. **Register components**: Use `factory` for repos and `viewModel` DSL for ViewModels.
3. **Handle parameters**: Use `parametersOf` for components requiring runtime data.

```kotlin
val pokemonListModule = module {
    factory { PokemonListApiService(get()) }
    factory<PokemonListRepository> { PokemonListRepository(get()) }
    viewModel { PokemonListViewModel(get(), get()) }
}

// For parametric ViewModels (e.g., in iOS helper)
fun getPokemonDetailViewModel(id: Int): PokemonDetailViewModel = 
    KoinPlatform.getKoin().get { parametersOf(id) }
```
*For DI patterns, see @kmp-di.*

---

## Critical Guardrails

| Anti-Pattern | Correct Pattern | Why It Matters |
|--------------|-----------------|----------------|
| `suspend fun get(): T?` | `suspend fun get(): Either<RepoError, T>` | Type-safe error handling |
| `init { loadData() }` | `override fun onStart(owner) { loadData() }` | Lifecycle-aware |
| `private val scope = CoroutineScope(...)` | Pass `viewModelScope` to constructor | Prevents leaks |
| `return Result.success(data)` | `Either.Right(data)` | Consistent error boundary |
| `class RepositoryImpl` (public) | `internal class RepositoryImpl` | Gradle compilation avoidance |
| No factory function | `fun Repository(...): Repository = RepositoryImpl(...)` | Simplifies DI wiring |
| Export `:data` to iOS | Export only `:api` and `:presentation` | iOS boundary violation |
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

// Immutable collections
import kotlinx.collections.immutable.toImmutableList

// DI
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
```

---

## Cross-References

### Architecture & Core Patterns
| Topic | Reference |
|-------|-----------|
| Complete architecture | See @kmp-architecture skill |
| All 6 core patterns | [@kmp-critical-patterns](../kmp-critical-patterns/SKILL.md) |
| Module structure & vertical slicing | [@kmp-architecture](../kmp-architecture/SKILL.md) |

### Layers & Patterns
| Topic | Reference |
|-------|-----------|
| ViewModel lifecycle & SavedStateHandle | [@kmp-presentation](../kmp-presentation/SKILL.md) (also [viewmodel-patterns.md](references/viewmodel-patterns.md)) |
| Repository Either<RepoError,T> patterns | [@kmp-data-layer](../kmp-data-layer/SKILL.md) (also [repository-patterns.md](references/repository-patterns.md)) |
| Domain models & use cases | [@kmp-domain](../kmp-domain/SKILL.md) |
| Koin DI configuration | [@kmp-di](../kmp-di/SKILL.md) |

### Platform & Navigation
| Topic | Reference |
|-------|-----------|
| iOS integration & exports | [@kmp-ios](../kmp-ios/SKILL.md) (also [ios-export.md](references/ios-export.md)) |
| Navigation 3 architecture | [@kmp-navigation](../kmp-navigation/SKILL.md) |

### Testing
| Topic | Reference |
|-------|-----------|
| Testing strategy & philosophy | [@kmp-testing-strategy](../kmp-testing-strategy/SKILL.md) |
| Testing patterns (Kotest, MockK, property tests) | [@kmp-testing-patterns](../kmp-testing-patterns/SKILL.md) |

---

## Command Reference

```bash
# Primary validation (Android build + all tests)
./gradlew :composeApp:assembleDebug test --continue

# Run specific feature tests
./gradlew :features:<feature>:presentation:testDebugUnitTest
./gradlew :features:<feature>:data:testDebugUnitTest

# Check dependency updates
./gradlew dependencyUpdates
```

---

## Anti-Pattern Quick Check

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
