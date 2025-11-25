# Project Conventions (Architecture, Modules, DI, Errors, Testing)

Purpose: Encode the cross-cutting rules we follow across modules and features. These conventions complement topic-specific guides in `.junie/guides/tech`.

## Architecture: True Vertical Slicing

**Core Principle**: Each feature is a complete vertical slice owning ALL its layers end-to-end. Features should be self-contained and minimize shared infrastructure.

### What is Vertical Slicing?

Vertical slicing means each feature contains ALL the layers it needs internally:
- **Domain models** specific to that feature
- **Network/API services** for that feature's endpoints
- **Data/Repository layer** for that feature's data access
- **Presentation/UI** for that feature's screens
- **Navigation contracts** for that feature's routes

### Module Structure (Primary Pattern: Split-by-Layer)

**All features use split-by-layer architecture** for clear separation of concerns and platform-specific UI:

```
:features:<feature>:api          → Public contracts (interfaces, domain models, navigation)
:features:<feature>:data         → Network, DTOs, repositories (KMP - all targets)
:features:<feature>:presentation → ViewModels, UI state (KMP - all targets, shared with iOS)
:features:<feature>:ui           → Compose UI screens (Android + JVM only, NOT iOS)
:features:<feature>:wiring       → DI assembly (KMP with platform-specific source sets)
```

**Example for Pokemon List feature:**
```
:features:pokemonlist:api/
  └── src/commonMain/kotlin/
      ├── PokemonListRepository.kt        (interface - public)
      ├── domain/Pokemon.kt               (domain model - public if shared)
      └── navigation/PokemonListEntry.kt  (navigation contract - plain data class)

:features:pokemonlist:data/
  └── src/commonMain/kotlin/
      ├── PokemonListApiService.kt        (Ktor HTTP client - all targets)
      ├── dto/PokemonListDto.kt           (DTOs for this feature's API)
      ├── mappers/PokemonMappers.kt       (DTO → Domain mappers)
      └── PokemonListRepositoryImpl.kt    (Repository implementation)

:features:pokemonlist:presentation/
  └── src/commonMain/kotlin/
      ├── PokemonListViewModel.kt         (ViewModel - shared across all platforms)
      └── PokemonListUiState.kt           (UI state - immutable collections)

:features:pokemonlist:ui/
  └── src/commonMain/kotlin/            (Android + JVM only - no iOS targets)
      └── PokemonListScreen.kt            (Compose UI - NOT exported to iOS)

:features:pokemonlist:wiring/
  ├── src/commonMain/kotlin/
  │   └── PokemonListModule.kt          (Provides repos, ViewModels - Metro DI)
  ├── src/androidMain/kotlin/           (Android-specific UI wiring)
  └── src/jvmMain/kotlin/               (JVM Desktop-specific UI wiring)
```

### When to Share Infrastructure (:core modules)

**ONLY create :core modules for:**
1. **Truly generic utilities** used by 3+ features (e.g., date formatters, string utils)
2. **Design system** (reusable UI components, theme, tokens)
3. **Cross-cutting domain models** (e.g., `User`, `Error` types used everywhere)
4. **Platform abstractions** (expect/actual for platform APIs)

**DO NOT create :core modules for:**
- ❌ Generic network layer (each feature has its own HttpClient configuration)
- ❌ Generic repository base classes (each feature implements its own patterns)
- ❌ Generic database layer (each feature manages its own data)
- ❌ Generic API service interfaces (each feature defines its own)

### Feature Independence Rules

1. **Features MUST NOT depend on other features' `:impl` modules**
   - ✅ `features:profile:impl` → `features:auth:api` (public interface)
   - ❌ `features:profile:impl` → `features:auth:impl` (implementation)

2. **Each feature owns its network layer**
   ```kotlin
   // :features:pokemonlist:data - Pokemon List's API service
   internal class PokemonListApiService(private val httpClient: HttpClient) {
       suspend fun getPokemons(limit: Int, offset: Int): PokemonListDto { ... }
   }
   
   // :features:pokemondetail:data - Pokemon Detail's API service  
   internal class PokemonDetailApiService(private val httpClient: HttpClient) {
       suspend fun getPokemonById(id: Int): PokemonDetailDto { ... }
   }
   ```

3. **Each feature defines its own DTOs**
   - Even if features call the same backend endpoint, they define their own DTOs
   - Why: Features evolve independently; shared DTOs create coupling

4. **Domain models in :api only if shared**
   - If `Pokemon` model is needed by multiple features → `features:pokemonlist:api`
   - If `PokemonDetail` model is only used internally → `features:pokemondetail:data/domain/`

### Optional: Domain Module

For features with complex business logic orchestrating multiple repositories:

```
:features:<feature>:domain       → Use cases, validators, business rules (KMP - all targets)
```

**When to create**: Feature has use cases that orchestrate 2+ repositories or enforce complex business rules. Most features won't need this—call repositories directly from ViewModels.

### Platform-Specific UI

**:ui modules target Android + JVM Desktop only** (no iOS targets):
- Android and Desktop share Compose Multiplatform UI
- iOS uses native SwiftUI, accessing ViewModels via `:shared` framework
- :ui modules are **NEVER exported** to iOS

### HttpClient Configuration

Each feature CAN share a root HttpClient instance (configured in app or a thin :core:httpclient module), but API services are feature-specific:

```kotlin
// Optional: :core:httpclient - ONLY provides HttpClient instance
fun createHttpClient(): HttpClient = HttpClient {
    install(ContentNegotiation) { json() }
    install(Logging)
}

// :features:pokemonlist:wiring
@Provides
fun providePokemonListApiService(httpClient: HttpClient): PokemonListApiService =
    PokemonListApiService(httpClient)

// :features:pokemondetail:wiring
@Provides  
fun providePokemonDetailApiService(httpClient: HttpClient): PokemonDetailApiService =
    PokemonDetailApiService(httpClient)
```

### Benefits of True Vertical Slicing

1. **Compilation Avoidance**: Changing Pokemon Detail doesn't recompile Pokemon List
2. **Team Autonomy**: Teams can work on features independently
3. **Testability**: Features are self-contained with clear boundaries
4. **Deployability**: Features can be feature-flagged or modularized independently
5. **Clarity**: All code for a feature lives in one place

### Migration from Horizontal Layers

If you started with `:core:network`, `:core:data`, `:core:domain`:
1. Move feature-specific code to feature modules
2. Keep only truly generic utilities in :core
3. Duplicate code across features if needed (coupling is worse than duplication)

---

## Architecture (Original Content Preserved)

### Interfaces: Impl + Factory Function pattern (required)
- When you define an interface (e.g., `SomeRepository` or `SomeService`) implement it with a private/internal class named `<InterfaceName>Impl` and expose a public top‑level factory function named exactly like the interface that returns the interface type.
- Rationale: keeps implementations hidden, simplifies DI wiring, and improves Gradle compilation avoidance.
- Example:
```kotlin
// :features:jobs:api/src/commonMain/.../JobRepository.kt
interface JobRepository {
  suspend fun refresh(page: Int): Either<RepoError, Unit>
  fun stream(): Flow<List<Job>>
}

// :features:jobs:impl/src/commonMain/.../JobRepositoryImpl.kt (internal)
internal class JobRepositoryImpl(
  private val api: JobApiService,
  private val cache: JobCache
) : JobRepository { /* ... */ }

// Public factory (same module as Impl or in api if you want discoverability)
fun JobRepository(api: JobApiService, cache: JobCache): JobRepository = JobRepositoryImpl(api, cache)

// :features:jobs:wiring/... calls the factory
@Provides fun provideJobRepository(api: JobApiService, cache: JobCache): JobRepository = JobRepository(api, cache)
```

## Dependency Injection (Metro)
- Use Metro for DI across all platforms.
- Keep production classes free of DI annotations. Prefer wiring modules with `@Provides` functions that return interface types.
- Root graph: define an `AppGraph` with `@DependencyGraph` in commonMain and a marker scope `AppScope`.
- Use wiring/aggregation modules to bind implementations of `api` contracts into the graph (multibinding supported).
- Graph extensions: use `@ContributesGraphExtension` for contextual/lifecycle scopes (e.g., logged-in).

### Wiring modules and Gradle Compilation Avoidance
- Wiring modules are created specifically to improve build speed by leveraging Gradle's Compilation Avoidance. See:
  - Gradle blog: https://blog.gradle.org/our-approach-to-faster-compilation
  - Aggregation Module pattern: https://proandroiddev.com/pragmatic-modularization-the-case-for-wiring-modules-c936d3af3611
- Dependency edges:
  - App modules depend on wiring modules.
  - Wiring depends on feature layer modules (`:data`, `:presentation`, `:ui`) and `:api`.
  - Other features depend only on feature `:api` (not layer modules), minimizing recompilation when implementations change.

### Wiring Platform-Specific Source Sets

Wiring modules support all KMP targets but use **platform-specific source sets** for UI dependencies:

```kotlin
// :features:pokemonlist:wiring/build.gradle.kts
kotlin {
    sourceSets {
        // Common: Repos, ViewModels, domain logic
        commonMain.dependencies {
            implementation(projects.features.pokemonlist.api)
            implementation(projects.features.pokemonlist.data)
            implementation(projects.features.pokemonlist.presentation)
        }
        
        // Android + JVM: Can depend on :ui module
        val androidMain by getting {
            dependencies {
                implementation(projects.features.pokemonlist.ui)
            }
        }
        
        val jvmMain by getting {
            dependencies {
                implementation(projects.features.pokemonlist.ui)
            }
        }
        
        // iOS: Uses only commonMain (no :ui dependency)
        // iOS accesses ViewModels from :presentation via :shared framework
    }
}
```

**Key principle**: iOS can consume wiring modules via `:shared` export because iOS targets only use `commonMain` dependencies (repos + ViewModels), never `:ui` module.

## Repository Boundary and Error Handling
- Repositories return Arrow `Either<RepoError, T>` and use `Either.catch { ... }.mapLeft { it.toRepoError() }` to map failures.
- API services throw exceptions and expose DTOs; repositories map DTOs to domain models.
- Avoid returning Kotlin `Result` and avoid null for error signaling at repository boundaries.

## Coroutines & Dispatchers
- Inject dispatchers (IO/Default) rather than hardcoding. Use structured concurrency and cancellation-aware IO.
- Long-lived jobs use an ApplicationScope provided via DI; UI logic uses screen/viewmodel scopes.

## Presentation Layer
- Consume `Either` from repositories and map to UI state (e.g., sealed `UiState` with Loading/Error/Content).
- Do not create pass-through/empty use cases. Call repositories directly from viewmodels when no domain orchestration is needed. Introduce use cases only when they add value.
- Define `UiStateHolder<S, E>` as an interface; have viewmodels implement it. One-time events should be emitted via a `OneTimeEventEmitter<E>` backed by a Channel and include `suspend fun emit(event: E)` so ViewModels can delegate to a reusable `EventChannel<E>`.
- Keep navigation contracts in feature `api` (plain data classes/objects for Navigation 3); implementations in feature modules; aggregate via wiring where needed.
- **ViewModels are KMP and shared across all platforms** (Android, Desktop, iOS): defined in `:features:<feature>:presentation` modules, exported to iOS via `:shared` framework.
- ViewModels: all ViewModels must extend `androidx.lifecycle.ViewModel` (KMP). Do not perform work in `init`. Be lifecycle-aware (load on lifecycle callbacks). Do NOT store a `CoroutineScope` field; instead pass a `viewModelScope` parameter to the `ViewModel` superclass constructor with a default value of `CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)` and use `viewModelScope` internally. Use `SavedStateHandle` when needed to restore state/inputs. Do not expose mutable collections; prefer `kotlinx.collections.immutable` types. Implement `OneTimeEventEmitter<E>` by delegation to `EventChannel<E>` located in `:core:util`.
- **Compose UI is platform-specific**: Lives in `:features:<feature>:ui` modules (Android + JVM only). iOS uses native SwiftUI consuming shared ViewModels.

## iOS Shared Umbrella
- The `shared` module produces a single umbrella framework for iOS.
- **Export to iOS**:
  - `:features:<feature>:api` → Repository interfaces, domain models, navigation contracts
  - `:features:<feature>:presentation` → ViewModels, UI state (shared ViewModels across platforms)
  - `:core:*` modules → Shared utilities, domain types
- **NEVER export to iOS**:
  - `:features:<feature>:data` → Internal data layer
  - `:features:<feature>:ui` → Compose UI (Android/JVM only)
  - `:features:<feature>:wiring` → DI assembly (though iOS can consume via commonMain)
  - `:composeApp` → Compose application module

**Example shared/build.gradle.kts:**
```kotlin
iosTarget.binaries.framework {
    baseName = "Shared"
    isStatic = true
    export(projects.features.pokemonlist.api)
    export(projects.features.pokemonlist.presentation)  // ViewModels shared with iOS
}

sourceSets {
    commonMain.dependencies {
        api(projects.features.pokemonlist.api)
        api(projects.features.pokemonlist.presentation)
    }
}
```

**iOS Strategy**: iOS SwiftUI views consume shared KMP ViewModels from `:presentation` modules, not Compose UI from `:ui` modules.

## Testing Stack (Mobile-First)

### ⚠️ MANDATORY: Test Enforcement

**NO CODE WITHOUT TESTS** - See `.junie/test-enforcement-agent.md`

Every production file requires a test file. This is not optional.

**Quick Reference:**
- Repository → androidTest/ (Kotest + MockK) ✅ MANDATORY
- ViewModel → androidTest/ (Kotest + MockK) ✅ MANDATORY
- Mapper → androidTest/ (Property tests) ✅ MANDATORY
- @Composable → @Preview in same file ✅ MANDATORY
- Utility → commonTest/ (kotlin-test) ✅ MANDATORY

**Minimum Coverage:**
- Success path + All error paths
- Initial state + All state transitions
- Data preservation (property-based)

### Primary: androidTest/ for Business Logic
- **Kotest** - Full framework (StringSpec, BehaviorSpec, FunSpec) with property-based testing
- **MockK** - Powerful mocking (JVM/Android only, not available for Native)
- **Location**: Place ALL business logic tests in module-specific `androidTest/` source sets:
  - Repository tests → `:features:<feature>:data/src/androidTest`
  - ViewModel tests → `:features:<feature>:presentation/src/androidTest`
  - Mapper tests → `:features:<feature>:data/src/androidTest`
- **Rationale**: Android is primary mobile target; iOS shares same Kotlin code (type safety guarantees)

### Minimal: commonTest/ for Simple Utilities
- **kotlin-test** - Basic assertions only
- **Use for**: Pure functions with NO dependencies
- **Rule**: If it needs mocking or Kotest features, put it in androidTest/

### Framework Limitations
- ❌ Kotest does NOT support iOS/Native targets
- ❌ MockK does NOT support iOS/Native targets
- ✅ Both fully support Android/JVM

### Testing Philosophy
1. Write tests in `androidTest/` for repositories, ViewModels, mappers
2. Android tests validate ALL shared business logic  
3. iOS compiles same Kotlin code (type system ensures compatibility)
4. Fast feedback: JVM tests run in seconds vs iOS builds in minutes
5. Mobile-first: Android primary target, Desktop convenience feature

### Smart Casting in Tests
**Never manually cast after type-checking matchers** - Kotest matchers provide smart casting through Kotlin compiler contracts.

See [kotest-smart-casting-quick-ref.md](./kotest-smart-casting-quick-ref.md) for complete guide.

### Screenshot Testing
- **Roborazzi** - Android/JVM Compose UI screenshot tests (Robolectric-based)
- Store baselines under `composeApp/src/test/snapshots`
- Record: `./gradlew recordRoborazziDebug`
- Verify: `./gradlew verifyRoborazziDebug`

### Property-Based Testing
- Use Kotest `checkAll`/`forAll` in **androidTest/** for:
  - Mappers (DTO ↔ Domain invariants)
  - Parsers (round-trip tests)
  - Value objects (laws and constraints)

### JSON round‑trip tests
- For modules dealing with JSON, favor round‑trip tests in **androidTest/**:
  - json → object → json (equality or semantic equivalence)
  - object → json → object (structural equality)
- Use Kotlinx Serialization and Kotest matchers to assert

### Gradle Example (Mobile-First)
```kotlin
kotlin {
  sourceSets {
    // Common: Basic utilities only
    commonTest.dependencies {
      implementation(kotlin("test"))
      implementation(libs.kotlinx.coroutines.test)
    }
    
    // Android: PRIMARY - All business logic
    androidTest.dependencies {
      implementation(libs.kotest.assertions)
      implementation(libs.kotest.framework)
      implementation(libs.kotest.property)
      implementation(libs.mockk)
    }
    
    // JVM: Full testing + screenshots
    jvmTest.dependencies {
      implementation(libs.kotest.assertions)
      implementation(libs.kotest.framework)
      implementation(libs.kotest.property)
      implementation(libs.mockk)
      implementation(libs.roborazzi)
    }
  }
}
```

## Naming
- Modules: `:features:<feature>:api`, `:features:<feature>:impl`.
- Files: use clear, purpose-revealing names. Avoid `Utils`, `Helper`.
- Tests: mirror the feature and layer names, suffix with `Spec` or `Test`.

## Alignment with Product Docs
- When behavior is product-driven, reference `.junie/guides/project/prd.md` and `.junie/guides/project/user_flow.md`. Resolve conflicts by preferring PRD for data rules and user_flow for sequence/UX.

## Desktop JVM App and Design System
- Desktop JVM target is a full-featured app, not just a design showcase.
- Maintain a reusable `designsystem` module for shared Compose components and tokens. Both Android and Desktop apps consume it.

## Aggregation/Wiring Modules
- Use wiring modules alongside feature `api` and implementation modules to assemble dependencies and registries without leaking implementation details.
- Naming: `:features:<feature>:wiring` (feature-local) or `:wiring:<area>` (cross-feature).
- Responsibilities: provide `@Provides` bindings, aggregate multibindings (e.g., `Set<FeatureEntry>`), and keep the app module thin.

## Gradle Convention Plugins
- Standardize build configuration using Convention Plugins in a `build-logic` module.
- Apply plugins such as `convention.kmp.library`, `convention.android.app`, and feature-specific ones like `convention.feature.api`, `convention.feature.data`, `convention.feature.presentation`, `convention.feature.wiring`.
- Centralize common configuration (Compose MPP, Kotlin options, Kotest/MockK, Roborazzi, lint). See `.junie/guides/tech/gradle_convention_plugins.md` for details.

## Dependency Management
- **Version Catalog**: All dependency versions centralized in `gradle/libs.versions.toml`
- **Ben Manes Versions Plugin**: Automated dependency update checking configured in root `build.gradle.kts`
- **Check for updates**: `./gradlew dependencyUpdates`
- **View report**: `build/dependencyUpdates/report.html`
- **Stability rules**:
  - Stable versions (e.g., `2.8.4`) will NOT upgrade to unstable versions (e.g., `2.9.0-alpha01`)
  - Unstable versions (e.g., `2.9.0-alpha01`) WILL upgrade within same major.minor version only:
    - `2.9.0-alpha01` → `2.9.0-alpha03` ✅ (same major.minor)
    - `2.9.0-alpha01` → `2.9.0-beta01` ✅ (same major.minor)
    - `2.9.0-rc02` → `2.9.0` ✅ (same major.minor)
    - `2.9.0-alpha01` → `2.10.0-alpha01` ❌ (different minor)
    - `2.9.0-alpha01` → `3.0.0-alpha01` ❌ (different major)
    - `2.9.0-alpha01` → `3.9.0-alpha01` ❌ (different major)
  - Unstable versions WILL upgrade to ANY stable version:
    - `2.9.0-alpha02` → `3.1.1` ✅ (stable release)
    - `1.0.0-rc02` → `1.0.0` ✅ (stable release)
  - Gradle wrapper updates also checked
- **Workflow**: Check for updates before adding new dependencies to ensure you use the latest compatible version

## No Empty Use Cases
- Avoid pass-through use cases that simply call a repository. Call repositories directly from presentation when no orchestration/business rule is needed. Create a use case only when it adds value (aggregation, policy, cross-cutting concerns).

## Navigation
- We standardize on Navigation 3 for Compose Multiplatform (supported since CMP 1.10.0‑beta02).
- Artifacts to include via version catalog:
  - `org.jetbrains.androidx.navigation3:navigation3-ui`
  - `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3`
  - Optional: `org.jetbrains.compose.material3.adaptive:adaptive-navigation3`

## Code Quality
- Lint/formatting: ktlint.
- Static analysis: detekt.
- Configure both via convention plugins in `build-logic` and run them in CI.
