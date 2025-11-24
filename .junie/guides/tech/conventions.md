# Project Conventions (Architecture, Modules, DI, Errors, Testing)

Purpose: Encode the cross-cutting rules we follow across modules and features. These conventions complement topic-specific guides in `.junie/guides/tech`.

## Architecture
- Clean Architecture with vertical slices. Each feature owns its code end-to-end.
- Feature modularization patterns (choose one and keep consistent):
  - api/impl (+ wiring):
    - `:features:<feature>:api` — public contracts to be shared (interfaces, navigation contracts, domain models required by others).
    - `:features:<feature>:impl` — private implementations (repositories, data sources, mappers, UI implementations). Keep classes DI-agnostic. Add `:features:<feature>:wiring` to assemble DI providers and registries.
  - Split-by-layer (+ api + wiring):
    - `:features:<feature>:api` — public contracts (repository interfaces, navigation contracts, domain types shared across features).
    - `:features:<feature>:data`, `:features:<feature>:presentation`, (optional `:features:<feature>:domain` if domain logic grows) — private implementations per layer.
    - `:features:<feature>:wiring` — aggregates and wires implementations for the app graph.
  - Only `api` modules are exposed to other features and the iOS umbrella. Do not export `impl`, `data`, `presentation`, or `wiring` modules.
  - Shared/core modules are allowed (e.g., `:core:domain:api`, `:core:network:api`, `:core:designsystem`) but must remain small and focused.

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
- Wiring modules are created specifically to improve build speed by leveraging Gradle’s Compilation Avoidance. See:
  - Gradle blog: https://blog.gradle.org/our-approach-to-faster-compilation
  - Aggregation Module pattern: https://proandroiddev.com/pragmatic-modularization-the-case-for-wiring-modules-c936d3af3611
- Dependency edges:
  - App modules depend on wiring modules.
  - Wiring depends on feature `impl` and `api`.
  - Other features depend only on feature `api` (not `impl`), minimizing recompilation when `impl` changes.

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
- Define `UiStateHolder<S, E>` as an interface; have viewmodels implement it. One-time events should be emitted via a `OneTimeEventEmitter<E>` backed by a Channel.
- Keep navigation contracts in feature `api`; implementations in feature modules; aggregate via wiring where needed.
 - ViewModels: do not perform work in `init`. Be lifecycle-aware (load on lifecycle callbacks) and accept a `CoroutineScope` (your `viewModelScope`) via constructor for easy testing. Use `SavedStateHandle` when needed to restore state/inputs. Do not expose mutable collections; prefer `kotlinx.collections.immutable` types.

## iOS Shared Umbrella
- The `shared` module produces a single umbrella framework for iOS. Export only required `api` modules and shared contracts. Keep `impl`, layer-specific modules, and `wiring` internal.

## Testing Stack
- Use Kotest as the primary test framework (unit + property-based).
- Use MockK for mocking in multiplatform (JVM/Android supported; use fakes for Native if needed).
- Prefer property-based testing (Kotest `checkAll`/`forAll`) where appropriate (parsers, mappers, invariants).
 - Screenshot testing: Use Roborazzi for Android/JVM Compose UI screenshot tests. Store baselines under `composeApp/src/test/snapshots` (or a central `snapshots/`) and verify in PRs. Record/update only behind a flag in CI.

### JSON round‑trip tests
- For modules dealing with JSON, favor round‑trip tests:
  - json → object → json (equality or semantic equivalence)
  - object → json → object (structural equality)
- Use Kotlinx Serialization and AssertK/Kotest matchers to assert.

### Gradle (Multiplatform test deps example)
```kotlin
kotlin {
  sourceSets {
    val commonTest by getting {
      dependencies {
        implementation("io.kotest:kotest-assertions-core:<version>")
        implementation("io.kotest:kotest-framework-engine:<version>")
        implementation("io.kotest:kotest-property:<version>")
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation("io.mockk:mockk:<version>")
      }
    }
    // Add platform-specific MockK or fakes as needed
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
