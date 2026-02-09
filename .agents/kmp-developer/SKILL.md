---
name: kmp-developer
description: This skill should be used for general Kotlin Multiplatform development tasks including implementing features, fixing bugs, refactoring code, and adding functionality. It provides shared patterns for the KMP Pokedex project.
---

## When to Use

Use this skill when working on:

- Implementing new features following vertical slice architecture
- Fixing bugs in existing feature modules (api, data, presentation, ui, wiring)
- Refactoring code within feature boundaries
- Adding or modifying repositories, ViewModels, and data layers
- Creating or updating Compose UI screens
- Writing or updating tests (Kotest in androidUnitTest/)
- Adding new dependencies via version catalog
- Making changes to Koin DI wiring modules

Do NOT use this skill for:
- Product/PRD decisions → switch to Product Design Mode
- Visual design/animations → switch to UI/UX Design Mode
- SwiftUI iOS screens → switch to SwiftUI Screen Implementation Mode
- Test planning/strategy → switch to Testing Strategy Mode
- Ktor backend changes → switch to Backend Development Mode

## Essential Workflows

### Workflow 1: Implement New Feature (Vertical Slice)

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

### Workflow 2: Fix Bug in Existing Feature

To diagnose and fix bugs:

1. Identify the affected layer from error symptoms:
   - UI issues → check `:ui` module screens
   - Data/loading issues → check `:presentation` ViewModel
   - Network/parsing errors → check `:data` repository implementation
   - DI/runtime errors → check `:wiring` module configuration

2. Locate relevant test file in `androidUnitTest/`:
   - Repositories → `:data/src/androidUnitTest`
   - ViewModels → `:presentation/src/androidUnitTest`
   - Mappers → `:data/src/androidUnitTest`

3. Reproduce the failing test case

4. Fix the implementation following canonical patterns:
   - ViewModels: pass `viewModelScope` to constructor, use `onStart()` not `init`
   - Repositories: return `Either<RepoError, T>`, use `Either.catch { }.mapLeft { }`
   - Impl+Factory: internal implementation class, public factory function

5. Run tests to verify fix:
   ```bash
   ./gradlew :composeApp:assembleDebug test --continue
   ```

6. Add regression test for the bug fix

### Workflow 3: Add or Update Tests

To maintain test coverage requirements (100% for mappers, 30-40% property tests):

1. For mapper tests (REQUIRED - 100% property coverage):
   ```kotlin
   class <Feature>MapperSpec : FreeSpec({
       "dto to domain preserves all properties" {
           checkAll(Arb.<Feature>Dto()) { dto ->
               val domain = dto.toDomain()
               domain.id shouldBe dto.id
               // verify all properties
           }
       }
   })
   ```

2. For ViewModel tests (REQUIRED - Turbine for flows):
   ```kotlin
   class <Feature>ViewModelSpec : FreeSpec({
       lateinit var repository: <Feature>Repository
       lateinit var testScope: TestScope
       lateinit var viewModel: <Feature>ViewModel

       beforeTest {
           repository = mockk()
           testScope = TestScope()
           viewModel = <Feature>ViewModel(repository, testScope)
       }

       "state transitions correctly on success" {
           val data = listOf(<Feature>Data(...))
           coEvery { repository.getData() } returns data.right()

           viewModel.uiState.test {
               awaitItem() shouldBe <Feature>UiState.Loading
               viewModel.onStart(owner)
               testScope.advanceUntilIdle()
               val content = awaitItem().shouldBeInstanceOf<<Feature>UiState.Content>()
               content.data shouldHaveSize 1
           }
       }
   })
   ```

3. For repository tests (REQUIRED - all error paths):
   ```kotlin
   class <Feature>RepositorySpec : FreeSpec({
       "returns Network error on IOException" {
           coEvery { api.getData() } throws IOException("network error")
           repository.getData().fold(
               ifLeft = { error -> error shouldBeInstanceOf RepoError.Network::class },
               ifRight = { fail("Expected left") }
           )
       }
   })
   ```

4. Run tests:
   ```bash
   ./gradlew :composeApp:assembleDebug test --continue
   ```

## Critical Guardrails

1. NEVER do work in ViewModel `init` block → override `onStart(owner: LifecycleOwner)` instead (lifecycle-aware)

2. NEVER store `CoroutineScope` as field → pass `viewModelScope` to constructor with default value

3. NEVER return nullable or `Result` from repositories → return `Either<RepoError, T>` with `Either.catch { }.mapLeft { }`

4. NEVER export `:data`, `:ui`, `:wiring` to iOS → only `:api` and `:presentation` are exported via `:shared` framework

5. NEVER use star imports → always use explicit imports (enforced by .editorconfig)

6. NEVER skip tests when adding code → every production file requires a test file in `androidUnitTest/`

7. NEVER create empty use cases → call repositories directly from ViewModels when no orchestration needed

8. NEVER swallow `CancellationException` → `Either.catch` respects cancellation automatically

## Quick Reference

| Command | Purpose | When to Run |
| --- | --- | --- |
| `./gradlew :composeApp:assembleDebug test --continue` | Primary validation (Android build + all tests) | Always, before committing |
| `./gradlew :composeApp:run` | Run desktop app | Local development |
| `./gradlew dependencyUpdates` | Check for dependency updates | Periodically |
| `./gradlew recordRoborazziDebug` | Record screenshot baselines | When adding UI tests |
| `./gradlew verifyRoborazziDebug` | Verify screenshots match baselines | Running UI tests |
| `./gradlew :features:<feature>:<layer>:testDebugUnitTest` | Run specific module tests | Focused testing |

## Cross-References

### Skills (by Category)

**Architecture**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-architecture | Module structure, vertical slicing, feature boundaries | [SKILL.md](../kmp-architecture/SKILL.md) |
| @kmp-critical-patterns | 6 core patterns quick reference (ViewModel, Either, Impl+Factory, Navigation, Testing, Plugins) | [SKILL.md](../kmp-critical-patterns/SKILL.md) |

**Layer Implementation**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-presentation | ViewModels, lifecycle, SavedStateHandle, UI state management | [SKILL.md](../kmp-presentation/SKILL.md) |
| @kmp-data-layer | Repository patterns, Either<RepoError, T>, DTO mapping | [SKILL.md](../kmp-data-layer/SKILL.md) |
| @kmp-domain | Domain models, use cases, domain exceptions | [SKILL.md](../kmp-domain/SKILL.md) |
| @kmp-di | Koin dependency injection patterns and configuration | [SKILL.md](../kmp-di/SKILL.md) |

**Platform & Navigation**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-ios | SwiftUI + KMP ViewModels Direct Integration, iOS export | [SKILL.md](../kmp-ios/SKILL.md) |
| @kmp-navigation | Navigation 3 modular architecture, scoped routes | [SKILL.md](../kmp-navigation/SKILL.md) |
| @kmp-desktop | Desktop (JVM) SavedStateHandle, Koin, platform-specific patterns | [SKILL.md](../kmp-desktop/SKILL.md) |

**Design & Build**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-design-systems | Design tokens, component library, icon strategy | [SKILL.md](../kmp-design-systems/SKILL.md) |
| @kmp-compose-unstyled | Headless components, Compose Unstyled patterns | [SKILL.md](../kmp-compose-unstyled/SKILL.md) |
| @kmp-api-services | Ktor Client, DTOs, API service patterns | [SKILL.md](../kmp-api-services/SKILL.md) |
| @kmp-gradle | Convention plugins, Gradle build configuration | [SKILL.md](../kmp-gradle/SKILL.md) |
| @kmp-commands | CLI reference, build commands, validation scripts | [SKILL.md](../kmp-commands/SKILL.md) |

**Testing**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-testing-strategy | Testing philosophy, coverage guidelines, test planning | [SKILL.md](../kmp-testing-strategy/SKILL.md) |
| @kmp-testing-patterns | Kotest, MockK, Turbine, property-based testing patterns | [SKILL.md](../kmp-testing-patterns/SKILL.md) |

### Documents

| Document | Purpose | Link |
| --- | --- | --- |
| Architecture + conventions | Master reference for architecture, modules, DI | [@kmp-architecture](../kmp-architecture/SKILL.md) |
| iOS integration | SwiftUI + KMP ViewModels Direct Integration details | [@kmp-ios](../kmp-ios/SKILL.md) |
| Dependency injection | Koin patterns and troubleshooting | [@kmp-di](../kmp-di/SKILL.md) |

**Reference Implementation**: `pokemonlist` feature demonstrates all patterns:
- [API](../../features/pokemonlist/api/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/PokemonListRepository.kt) • [Data](../../features/pokemonlist/data/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/data/PokemonListRepositoryImpl.kt) • [Presentation](../../features/pokemonlist/presentation/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/presentation/PokemonListViewModel.kt) • [UI](../../features/pokemonlist/ui-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/ui/material/PokemonListMaterialScreen.kt) • [Wiring](../../features/pokemonlist/wiring/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/wiring/PokemonListModule.kt)
