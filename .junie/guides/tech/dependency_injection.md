# Dependency Injection Guidelines

Purpose: Establish consistent DI patterns using Metro for Kotlin Multiplatform with compile-time graph validation and vertical-slice feature modules.

References
- Metro docs and API: zacsweers.github.io/metro (compile-time DI, K2, KSP, multiplatform)
- Examples in this guide are adapted from Metro reference docs such as dependency-graphs and aggregation pages.

## Framework Choice
- We use Metro across all platforms. It’s Kotlin Multiplatform-ready and performs full binding graph validation at compile time.
- Keep production classes free of DI annotations where possible. Prefer explicit `@Provides` functions in DI/wiring modules to assemble dependencies.
- Use Metro’s scopes, graph factories, and contribution annotations to wire feature modules cleanly.

## Architecture: Vertical Slices with api/impl
- Each feature lives in its own set of modules using the pattern:
  - :features:<feature-name>:api — public contracts (interfaces, models that need to be shared), navigation contracts, and DI entry points to be consumed by other features.
  - :features:<feature-name>:impl — private implementations, internal mappers, data sources, and DI contributions using Metro annotations.
- Only the `api` modules are visible to other features; `impl` remains private. `impl` contributes bindings to the application graph.

## Location and Structure
- DI code that declares the app/root graph lives in the app composition module’s `src/commonMain/.../di`.
- Feature-specific DI contributions live in each feature’s `wiring` module under `:features:<feature>:wiring/src/commonMain/.../di` (providers/aggregators), while implementations remain in `:features:<feature>:impl`.
- Platform-specific DI (if needed) is placed in platform source sets. Prefer commonMain where possible.

## Defining the App Graph

```kotlin
// Define an application scope marker
interface AppScope

// Root graph defines app-wide dependencies
@DependencyGraph
interface AppGraph {
  val loggerSet: Set<Logger> // example multibinding

  @DependencyGraph.Factory
  fun interface Factory {
    // Factory can accept runtime inputs using @Provides
    fun create(@Provides baseUrl: String): AppGraph
  }
}
```

Notes
- Use `@DependencyGraph` for the root. Metro generates an `$$MetroGraph` implementation.
- Use a `Factory` to pass runtime parameters (e.g., base URL, configs) using `@Provides`.

## Providing and Binding Dependencies (no annotations on classes)

```kotlin
// Prefer explicit provider functions in a wiring/DI module.
// Classes remain DI-agnostic (no @Inject on constructors).

// Example provider for a client
@Provides
fun provideHttpClient(json: Json, engine: HttpClientEngine): HttpClient = buildHttpClient(json, engine)

// Impl + Factory: bind interface to implementation by calling a public factory function
@Provides
fun provideUserRepository(
  api: UserApiService,
  storage: UserStorage
): UserRepository = UserRepository(api, storage) // factory returns interface, hiding *Impl
```

Multibinding (sets/maps)
```kotlin
// If you want to avoid class annotations, provide sets directly
@Provides
fun provideLoggers(
  console: ConsoleLogger,
  crash: CrashLogger,
): Set<Logger> = setOf(console, crash)

// Or if convenient, you may still use Metro multibinding annotations in wiring modules only,
// keeping production classes clean.
```

## Feature Modules and DI

In :features:<name>:api
- Expose only the contracts needed cross-feature (e.g., `UserRepository`, navigation contracts, and any domain/use case interfaces that other features must call). Avoid leaking implementation details.

In :features:<name>:data
- Implement repository contracts, API services, DTOs, mappers. Keep classes DI-agnostic. Implementations should be private/internal and named `<InterfaceName>Impl`.

In :features:<name>:presentation
- Implement ViewModels and UI state. **Shared across all platforms** (Android, Desktop, iOS). ViewModels extend `androidx.lifecycle.ViewModel` (KMP). Keep DI-agnostic.

In :features:<name>:ui
- Implement Compose UI screens (@Composable functions). **Android + JVM only** (no iOS targets). Keep DI-agnostic.

In :features:<name>:wiring
- Provide bindings via `@Provides` that return the interface type by invoking the top‑level factory function named after the interface (Impl + Factory pattern).
- Use **platform-specific source sets** for UI dependencies:
  - `commonMain` → Provides repositories, ViewModels (all platforms)
  - `androidMain`/`jvmMain` → Provides UI entry points, navigation (Android/JVM only)
  - iOS targets use only `commonMain` (no UI dependencies)
- Provide feature-scoped factories when needed using Metro graph extensions.

```kotlin
// features/profile/api/src/commonMain/.../ProfileRepository.kt
interface ProfileRepository { suspend fun load(): Either<RepoError, Profile> }

// features/profile/data/src/commonMain/.../ProfileRepositoryImpl.kt
internal class ProfileRepositoryImpl(
  private val api: ProfileApiService
) : ProfileRepository

// features/profile/data/src/commonMain/.../ProfileRepositoryFactory.kt
fun ProfileRepository(api: ProfileApiService): ProfileRepository = ProfileRepositoryImpl(api)

// features/profile/wiring/src/commonMain/.../ProfileWiring.kt
@Provides
fun provideProfileRepository(api: ProfileApiService): ProfileRepository = ProfileRepository(api) // calls factory
```

### Wiring/Aggregation Modules
- We use wiring modules to assemble and aggregate dependencies for a feature or a group of features, keeping implementation classes free of DI annotations and private to their modules.
- Naming: `:features:<feature>:wiring` (feature-local) or `:wiring:<area>` (cross-feature aggregation).
- Responsibilities:
  - Provide `@Provides` functions that wire implementations to interfaces
  - Aggregate multibindings (e.g., sets of `FeatureEntry` for navigation)
  - Optionally expose graph extensions/factories for contextual scopes

### Platform-Specific Source Sets in Wiring Modules

Wiring modules support all KMP targets but use platform-specific source sets for UI dependencies:

```kotlin
// :features:profile:wiring/build.gradle.kts
kotlin {
    sourceSets {
        // Common: Repos, ViewModels, domain - all platforms
        commonMain.dependencies {
            implementation(projects.features.profile.api)
            implementation(projects.features.profile.data)
            implementation(projects.features.profile.presentation)
        }
        
        // Android: UI dependencies
        val androidMain by getting {
            dependencies {
                implementation(projects.features.profile.ui)
            }
        }
        
        // JVM Desktop: UI dependencies
        val jvmMain by getting {
            dependencies {
                implementation(projects.features.profile.ui)
            }
        }
        
        // iOS: Uses only commonMain (no :ui module)
        // iOS gets ViewModels from :presentation via :shared framework
    }
}
```

**Why this works**:
- Metro DI is multiplatform-compatible
- Wiring modules can be consumed by iOS via `:shared` export
- iOS targets only compile `commonMain` dependencies (repos + ViewModels)
- Android/JVM targets compile `commonMain` + platform-specific (repos + ViewModels + UI)

## Graph Extensions for Subgraphs
Use graph extensions for lifecycle or contextual scopes (e.g., logged-in user):

```kotlin
@ContributesGraphExtension(LoggedInScope::class)
interface LoggedInGraph {
  val sessionUser: SessionUser

  @ContributesGraphExtension.Factory(AppScope::class)
  interface Factory {
    fun createLoggedInGraph(@Provides userId: String): LoggedInGraph
  }
}
```

## Initialization
- Construct the app graph at startup via the generated factory. Provide runtime parameters via `@Provides` args.

```kotlin
// Pseudo-code: exact API is generated by Metro for your graph.
// Common options include a companion `invoke` or a generated Factory.

// Example options (one of these will exist after compilation):
// val appGraph: AppGraph = AppGraph(/* @Provides args like baseUrl */)
// val appGraph: AppGraph = AppGraph.Factory.create(/* baseUrl */)

// Keep a singleton reference you can pass where needed
object DI {
  lateinit var appGraph: AppGraph
    private set

  fun init(baseUrl: String) {
    // initialize appGraph using the generated factory for AppGraph
    // e.g., appGraph = AppGraph.Factory.create(baseUrl)
  }
}
```

Tip: The exact invocation depends on the generated API for your graph (see Metro docs). Generally there is a factory or `invoke` operator on the companion.

## Platform-Specific Dependencies
- Use expect/actual when you must bind platform-specific services. Contribute the actuals in platform source sets and expose their contracts in commonMain.

## Best Practices
- Keep DI setup in commonMain where feasible; isolate platform specifics.
- Prefer small graphs and wiring modules over large monoliths.
- Use scoping as needed for repositories, network clients, and stateful services; use unscoped/`factory`-like patterns for ephemeral objects.
- Validate module visibility: only `api` modules are visible to other features; `impl` and `wiring` should not be exported.

### Build performance: Compilation Avoidance
- Wiring modules exist to improve build speed by leveraging Gradle Compilation Avoidance. App modules depend on wiring; wiring depends on `api` + `impl`; other features depend only on `api`. See Gradle’s write‑up: https://blog.gradle.org/our-approach-to-faster-compilation and the Aggregation Module pattern: https://proandroiddev.com/pragmatic-modularization-the-case-for-wiring-modules-c936d3af3611

## iOS Umbrella (shared module)
- The `shared` module is an umbrella framework for the iOS app. It exports all required feature `api` modules and `presentation` modules (ViewModels) while keeping data layer and UI implementations internal.
- Ensure Gradle `export` entries include:
  - `:features:<feature>:api` modules (repository interfaces, domain models, navigation)
  - `:features:<feature>:presentation` modules (ViewModels, UI state - shared with iOS)
  - `:core:*` modules (shared utilities, domain types)
- **Do NOT export**:
  - `:features:<feature>:data` modules (internal data layer)
  - `:features:<feature>:ui` modules (Compose UI - Android/JVM only)
  - `:features:<feature>:wiring` modules (though iOS can consume via commonMain)
  - `:composeApp` (Compose application)

**Example:**
```kotlin
// shared/build.gradle.kts
iosTarget.binaries.framework {
    baseName = "Shared"
    isStatic = true
    export(projects.features.profile.api)
    export(projects.features.profile.presentation)  // ViewModels shared with iOS SwiftUI
}

sourceSets {
    commonMain.dependencies {
        api(projects.features.profile.api)
        api(projects.features.profile.presentation)
    }
}
```

**iOS SwiftUI Integration**: iOS views consume KMP ViewModels from `:presentation` modules, call repositories from `:api` modules, all accessed via `shared.framework`.

## Testing Considerations
- For tests, construct small test graphs or directly instantiate classes with fakes/mocks. Metro validates graphs at compile time; prefer unit tests with explicit constructors over heavy DI bootstrapping in tests.

Notes from Metro docs
- Metro performs full binding graph validation and supports K2/KSP. See the official docs for `@DependencyGraph`, `@Provides`, `@Binds`, `@ContributesBinding`, `@ContributesIntoSet`, and graph extension patterns.
