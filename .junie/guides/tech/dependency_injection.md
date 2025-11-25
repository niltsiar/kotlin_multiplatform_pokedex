# Dependency Injection Guidelines

Purpose: Establish consistent DI patterns using Metro for Kotlin Multiplatform with compile-time graph validation and vertical-slice feature modules.

References
- Metro docs and API: [zacsweers.github.io/metro](https://zacsweers.github.io/metro) (compile-time DI using K2 compiler plugin, NOT KSP, multiplatform)
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
// Import Metro's built-in AppScope (abstract class from Metro runtime)
import dev.zacsweers.metro.AppScope

// Root graph defines app-wide dependencies
@DependencyGraph(AppScope::class)  // Explicit scope parameter REQUIRED
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
```

## Contributing Bindings from Feature Modules

Feature modules contribute bindings to the central AppGraph using `@ContributesTo`:

```kotlin
// features/pokemonlist/wiring/src/commonMain/.../PokemonListModule.kt
package com.minddistrict.multiplatformpoc.features.pokemonlist.wiring

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient

@BindingContainer
@ContributesTo(AppScope::class)  // Contributes to AppGraph
interface PokemonListProviders {
    companion object {
        @Provides
        @SingleIn(AppScope::class)
        fun provideHttpClient(): HttpClient = createHttpClient()
        
        @Provides
        fun providePokemonListRepository(
            apiService: PokemonListApiService
        ): PokemonListRepository {
            // Call factory function (Impl + Factory pattern)
            return createPokemonListRepository(apiService)
        }
        
        @Provides
        fun providePokemonListViewModel(
            repository: PokemonListRepository
        ): PokemonListViewModel = PokemonListViewModel(repository)
    }
}
```

**Key Points**:
- Use `@BindingContainer` + `@ContributesTo(AppScope::class)` to make bindings discoverable
- Put `@Provides` functions in `companion object` (Metro requirement)
- Use `@SingleIn(AppScope::class)` for singletons
- Call factory functions, not constructors
- Wiring module MUST be added as `api` dependency to `core:di` for Metro discovery
- **Critical**: Wiring modules MUST NOT depend on `core:di` (would create circular dependency)

**See**: Working example in `features/pokemonlist/wiring/src/commonMain/kotlin/.../PokemonListModule.kt`
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
// Metro generates createGraphFactory() extension function
import dev.zacsweers.metro.createGraphFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun App() {
    val graph: AppGraph = remember {
        createGraphFactory<AppGraph.Factory>().create(
            baseUrl = "https://pokeapi.co/api/v2"
        )
    }
    
    val viewModel = graph.pokemonListViewModel
    // Use viewModel in UI...
}
```

**Key Points**:
- `createGraphFactory<AppGraph.Factory>()` - Type-safe factory creation
- `.create(...)` - Pass runtime parameters marked with `@Provides`
- `remember { }` - Cache graph instance in Compose to avoid recreation
- Access dependencies via graph properties

**See**: Working example in `composeApp/src/commonMain/kotlin/.../App.kt`

### Dual Metro Plugin Requirement

Metro plugin must be applied to **both** graph definition and consumption modules:

**1. Graph Definition Module** (`core:di`):
```kotlin
plugins {
    alias(libs.plugins.metro)  // Generates $$MetroGraph implementation
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Wiring modules as api for Metro discovery
            api(projects.features.pokemonlist.wiring)
        }
    }
}
```

**2. Consumption Module** (`composeApp`):
```kotlin
plugins {
    alias(libs.plugins.metro)  // Provides createGraphFactory() extension
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.di)
        }
    }
}
```

**Why Both?**
- `core:di`: Processes `@DependencyGraph` and discovers `@ContributesTo` annotations
- `composeApp`: Generates `createGraphFactory<T>()` extension function for graph instantiation

**Reference**: See `core/di/build.gradle.kts` and `composeApp/build.gradle.kts`

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

## Troubleshooting

### Common Errors and Solutions

#### 1. "Unresolved reference: createGraphFactory"
**Cause**: Metro plugin not applied to consumption module

**Fix**: Add Metro plugin to module using `createGraphFactory()`
```kotlin
plugins {
    alias(libs.plugins.metro)
}
```

#### 2. "@DependencyGraph requires a scope parameter"
**Cause**: Missing explicit scope in @DependencyGraph annotation

**Fix**: Add scope parameter
```kotlin
@DependencyGraph(AppScope::class)  // Explicit scope required
interface AppGraph { ... }
```

#### 3. "Circular dependency detected"
**Symptom**: Build fails with circular dependency involving `core:di` and wiring modules

**Cause**: Wiring module depends on `core:di` (usually for custom scope marker)

**Fix**: 
- Use Metro's built-in `dev.zacsweers.metro.AppScope` instead of custom scope
- Remove any `implementation(project(":core:di"))` from wiring modules
- Verify convention plugin doesn't add `core:di` dependency

**Correct Architecture**:
```
core:di → (api) → wiring modules
wiring ✗ core:di  // MUST NOT depend back
```

#### 4. "No binding found for parameter 'baseUrl'"
**Cause**: Runtime parameter not annotated with `@Provides`

**Fix**: Add `@Provides` to parameter in both factory and provider functions
```kotlin
// AppGraph.Factory
fun create(@Provides baseUrl: String): AppGraph

// Provider function
@Provides
fun provideApiService(@Provides baseUrl: String): ApiService
```

#### 5. KSP configuration errors
**Cause**: Attempting to use KSP with Metro

**Fix**: Remove KSP configuration - Metro uses Kotlin compiler plugin, NOT KSP
```kotlin
// ❌ WRONG - Don't add KSP
plugins {
    id("com.google.devtools.ksp")  // Not needed!
}
```

Metro's Gradle plugin automatically configures the Kotlin compiler plugin.

#### 6. "Cannot access 'ViewModel' which is a supertype"
**Symptom**: Warning about accessing ViewModel supertype in wiring module

**Cause**: Missing lifecycle dependency in wiring module

**Fix**: Convention plugin should add this automatically. If manual, add:
```kotlin
commonMain.dependencies {
    implementation(libs.androidx.lifecycle.viewmodel)
}
```

**Quick Reference**: See [metro_di_quick_ref.md](metro_di_quick_ref.md) for more patterns and solutions.

## Testing Considerations
- For tests, construct small test graphs or directly instantiate classes with fakes/mocks. Metro validates graphs at compile time; prefer unit tests with explicit constructors over heavy DI bootstrapping in tests.

Notes from Metro docs
- Metro performs full binding graph validation using the K2 compiler plugin (NOT KSP). See the official docs for `@DependencyGraph`, `@Provides`, `@Binds`, `@ContributesBinding`, `@ContributesIntoSet`, and graph extension patterns.
- Quick Reference: [metro_di_quick_ref.md](metro_di_quick_ref.md)
