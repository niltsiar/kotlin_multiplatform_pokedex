# Metro DI Quick Reference

**Purpose**: Quick reference for Metro dependency injection patterns in Kotlin Multiplatform projects.

**Official Docs**: [zacsweers.github.io/metro](https://zacsweers.github.io/metro)

---

## 🎯 Core Concepts

### What is Metro?

Metro is a **compile-time dependency injection** framework for Kotlin Multiplatform that uses the **Kotlin compiler plugin** (NOT KSP) for code generation.

**Key Features**:
- ✅ Compile-time graph validation (catch DI errors at build time)
- ✅ Kotlin compiler plugin (K2) for fast code generation
- ✅ Multiplatform support (Android, iOS, JVM, Native)
- ✅ Contribution-based architecture (features contribute bindings to central graph)
- ✅ No runtime reflection or annotation processing

**Important**: Metro does NOT use KSP. It integrates directly with the Kotlin K2 compiler.

---

## 📦 Setup

### Version Catalog (`gradle/libs.versions.toml`)

```toml
[versions]
metro = "0.7.7"

[libraries]
metro-runtime = { module = "dev.zacsweers.metro:runtime", version.ref = "metro" }

[plugins]
metro = { id = "dev.zacsweers.metro", version.ref = "metro" }
```

### Gradle Configuration

**1. Graph Definition Module** (e.g., `core/di/build.gradle.kts`):
```kotlin
plugins {
    id("convention.kmp.library")
    alias(libs.plugins.metro)  // Metro plugin for graph generation
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Metro runtime is added automatically by the plugin
            
            // Add wiring modules as api dependencies
            // (Metro needs to discover @ContributesTo annotations)
            api(projects.features.pokemonlist.wiring)
        }
    }
}
```

**2. Consumption Module** (e.g., `composeApp/build.gradle.kts`):
```kotlin
plugins {
    id("convention.kmp.android.app")
    alias(libs.plugins.metro)  // Needed for createGraphFactory() extension
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.di)  // Contains AppGraph
        }
    }
}
```

**Why Dual Plugin?**
- Graph definition module: Generates `$$MetroGraph` implementation
- Consumption module: Provides `createGraphFactory<T>()` extension function

---

## 🏗️ Module Architecture

### Dependency Flow

```
┌─────────────────────────────────────────────────┐
│                   composeApp                     │
│  (Metro plugin for createGraphFactory())        │
│  - Depends on: core:di                          │
└─────────────────┬───────────────────────────────┘
                  │ implementation
                  ↓
┌─────────────────────────────────────────────────┐
│                    core:di                       │
│  (Metro plugin for graph generation)            │
│  - Contains: AppGraph interface                 │
│  - Depends on: wiring modules (api)             │
└─────────────────┬───────────────────────────────┘
                  │ api (for Metro discovery)
                  ↓
┌─────────────────────────────────────────────────┐
│        features:pokemonlist:wiring               │
│  (Metro plugin via convention.feature.wiring)   │
│  - @ContributesTo(AppScope::class)              │
│  - Depends on: api, data, presentation          │
│  - MUST NOT depend on: core:di                  │
└─────────────────┬───────────────────────────────┘
                  │
                  ↓
        api / data / presentation modules
```

**Critical**: Wiring modules MUST NOT depend on `core:di` to avoid circular dependencies.

---

## 🎨 Built-in Scopes

Metro provides standard scopes out of the box. Use them instead of defining custom scopes.

### AppScope (Application-Level)

```kotlin
import dev.zacsweers.metro.AppScope  // Built-in abstract class

@DependencyGraph(AppScope::class)  // Use built-in scope
interface AppGraph {
    // ...
}
```

**Do NOT create custom scope markers** like:
```kotlin
// ❌ WRONG - Don't create custom AppScope
interface AppScope
```

Metro's `AppScope` is an abstract class in the runtime library. Just import and use it.

---

## 📝 Defining the Dependency Graph

### AppGraph Interface

```kotlin
package com.minddistrict.multiplatformpoc.core.di

import com.minddistrict.multiplatformpoc.features.pokemonlist.presentation.PokemonListViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides

/**
 * Root dependency injection graph for the application.
 * Metro generates an implementation at compile time.
 */
@DependencyGraph(AppScope::class)  // ⚠️ Explicit scope is REQUIRED
interface AppGraph {
    /**
     * ViewModels and services exposed to the app.
     */
    val pokemonListViewModel: PokemonListViewModel
    
    /**
     * Factory for creating the graph with runtime dependencies.
     */
    @DependencyGraph.Factory
    fun interface Factory {
        /**
         * @param baseUrl Injected at runtime via @Provides
         */
        fun create(@Provides baseUrl: String): AppGraph
    }
}
```

**Key Points**:
- ✅ `@DependencyGraph(AppScope::class)` - Explicit scope parameter is REQUIRED
- ✅ Expose properties for dependencies needed by app (ViewModels, services)
- ✅ Use `@DependencyGraph.Factory` for runtime parameters
- ✅ Mark runtime params with `@Provides` in factory

**Common Mistake**:
```kotlin
// ❌ WRONG - Missing scope parameter
@DependencyGraph
interface AppGraph { ... }
```

---

## 🔌 Contributing Bindings

### @ContributesTo Pattern

Feature modules contribute bindings to the central graph using `@BindingContainer` + `@ContributesTo`:

```kotlin
package com.minddistrict.multiplatformpoc.features.pokemonlist.wiring

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
@ContributesTo(AppScope::class)  // Contribute to AppGraph
interface PokemonListProviders {
    
    companion object {
        /**
         * Singleton HttpClient shared across API services.
         */
        @Provides
        @SingleIn(AppScope::class)  // Scope for singleton
        fun provideHttpClient(): HttpClient {
            return createHttpClient()
        }
        
        /**
         * API service with injected dependencies.
         */
        @Provides
        fun providePokemonListApiService(
            httpClient: HttpClient,
            @Provides baseUrl: String  // Runtime parameter from factory
        ): PokemonListApiService {
            return PokemonListApiService(httpClient)
        }
        
        /**
         * Repository using Impl + Factory pattern.
         */
        @Provides
        fun providePokemonListRepository(
            apiService: PokemonListApiService
        ): PokemonListRepository {
            // Call public factory function (not constructor)
            return createPokemonListRepository(apiService)
        }
        
        /**
         * ViewModel exposed in AppGraph.
         */
        @Provides
        fun providePokemonListViewModel(
            repository: PokemonListRepository
        ): PokemonListViewModel {
            return PokemonListViewModel(repository)
        }
    }
}
```

**Key Points**:
- ✅ `@BindingContainer` + `@ContributesTo(AppScope::class)` make bindings discoverable
- ✅ Put `@Provides` functions in `companion object`
- ✅ Use `@SingleIn(AppScope::class)` for singletons
- ✅ Runtime params marked with `@Provides` (e.g., `baseUrl`)
- ✅ Call factory functions, not constructors (Impl + Factory pattern)

**Why companion object?**
Metro requires `@Provides` functions to be static/top-level or in companion objects.

---

## 🚀 Graph Initialization

### createGraphFactory Pattern

```kotlin
package com.minddistrict.multiplatformpoc

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.minddistrict.multiplatformpoc.core.di.AppGraph
import dev.zacsweers.metro.createGraphFactory

@Composable
fun App() {
    val graph: AppGraph = remember { 
        createGraphFactory<AppGraph.Factory>().create(
            baseUrl = "https://pokeapi.co/api/v2"
        )
    }
    
    // Access dependencies from graph
    val viewModel = graph.pokemonListViewModel
    
    // Use viewModel in UI...
}
```

**Key Points**:
- ✅ `createGraphFactory<AppGraph.Factory>()` - Type-safe factory creation
- ✅ `.create(...)` - Pass runtime parameters
- ✅ `remember { }` - Cache graph instance in Compose
- ✅ Access dependencies via graph properties

**Where does `createGraphFactory` come from?**
Generated by Metro plugin in the consumption module (`composeApp`).

---

## 🎭 Convention Plugin for Wiring

### `convention.feature.wiring`

Wiring modules automatically get Metro configured via convention plugin:

```kotlin
// features/pokemonlist/wiring/build.gradle.kts
plugins {
    id("convention.feature.wiring")  // Applies Metro + base config
}

// No manual Metro setup needed!
```

**What it does**:
- Applies `dev.zacsweers.metro` plugin
- Configures KMP targets (Android, iOS, JVM)
- Adds base dependencies (Arrow, Coroutines, etc.)
- Sets up test dependencies

**Convention Plugin Location**: 
`build-logic/convention/src/main/kotlin/com/minddistrict/multiplatformpoc/ConventionFeatureWiringPlugin.kt`

See: [convention_plugins_quick_ref.md](convention_plugins_quick_ref.md) for details.

---

## ⚠️ Common Pitfalls & Solutions

### 1. Forgetting Explicit Scope in @DependencyGraph

**Error**:
```
@DependencyGraph requires a scope parameter
```

**Fix**:
```kotlin
// ❌ WRONG
@DependencyGraph
interface AppGraph { ... }

// ✅ CORRECT
@DependencyGraph(AppScope::class)
interface AppGraph { ... }
```

---

### 2. Circular Dependency: wiring → core:di

**Symptom**: Build fails with circular dependency error

**Cause**: Wiring module depends on `core:di` (often accidentally via convention plugin or for custom scope marker)

**Fix**: 
- ✅ Use Metro's built-in `dev.zacsweers.metro.AppScope` (not custom scope in core:di)
- ✅ Remove any `implementation(project(":core:di"))` from wiring modules
- ✅ Ensure convention plugin doesn't add core:di dependency

**Correct Architecture**:
```
core:di → (api) → wiring
wiring ✗ core:di  // MUST NOT depend back
```

---

### 3. Missing Metro Plugin in Consumption Module

**Error**:
```
Unresolved reference: createGraphFactory
```

**Cause**: Metro plugin not applied to module using `createGraphFactory()`

**Fix**: Add Metro plugin to consumption module
```kotlin
// composeApp/build.gradle.kts
plugins {
    alias(libs.plugins.metro)  // Add this
}
```

---

### 4. Trying to Use KSP with Metro

**Symptom**: Build errors about KSP configuration or missing symbol processors

**Cause**: Attempting to use KSP for Metro code generation

**Fix**: Remove KSP configuration - Metro uses Kotlin compiler plugin, NOT KSP
```kotlin
// ❌ WRONG - Don't add KSP plugin for Metro
plugins {
    id("com.google.devtools.ksp")  // Not needed!
}
```

Metro's Gradle plugin automatically configures the Kotlin compiler plugin for code generation.

---

### 5. Forgetting @Provides on Runtime Parameters

**Error**:
```
No binding found for parameter 'baseUrl'
```

**Fix**: Mark runtime parameters with `@Provides` in both factory and provider functions
```kotlin
// AppGraph.Factory
fun create(@Provides baseUrl: String): AppGraph

// Provider function
@Provides
fun provideApiService(
    @Provides baseUrl: String  // Must annotate here too
): ApiService
```

---

### 6. Using Constructor Instead of Factory Function

**Anti-pattern**:
```kotlin
// ❌ WRONG - Exposing constructor
@Provides
fun provideRepository(api: ApiService): Repository {
    return RepositoryImpl(api)  // Constructor call
}
```

**Correct (Impl + Factory Pattern)**:
```kotlin
// ✅ CORRECT - Call factory function
@Provides
fun provideRepository(api: ApiService): Repository {
    return createRepository(api)  // Factory function
}
```

See: [conventions.md](conventions.md#impl--factory-pattern) for details.

---

## 🧪 Testing with Metro

### Unit Tests (No DI Framework)

Prefer direct instantiation with fakes/mocks over DI in tests:

```kotlin
class PokemonRepositoryTest : StringSpec({
    lateinit var mockApi: PokemonListApiService
    lateinit var repository: PokemonListRepository
    
    beforeTest {
        mockApi = mockk()
        // Direct instantiation via factory function
        repository = createPokemonListRepository(mockApi)
    }
    
    "should return Right on success" {
        coEvery { mockApi.getPokemonList(any(), any()) } returns mockResponse
        
        val result = repository.loadPage()
        
        result.shouldBeRight()
    }
})
```

**Why?**
- ✅ Faster test execution (no DI graph initialization)
- ✅ Explicit dependencies (easier to understand)
- ✅ Better isolation (test one class at a time)

Metro validates graphs at compile time, so runtime DI in tests is unnecessary.

---

## 📊 Visual Architecture Diagram

```
┌──────────────────────────────────────────────────────────────┐
│                        composeApp                             │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ App.kt:                                                │  │
│  │   val graph = createGraphFactory<AppGraph.Factory>()  │  │
│  │                 .create(baseUrl = "...")               │  │
│  │   val vm = graph.pokemonListViewModel                  │  │
│  └────────────────────────────────────────────────────────┘  │
│  Plugins: alias(libs.plugins.metro)  // for createGraphFactory() │
└─────────────────────────┬────────────────────────────────────┘
                          │ implementation
                          ↓
┌──────────────────────────────────────────────────────────────┐
│                         core:di                               │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ @DependencyGraph(AppScope::class)                      │  │
│  │ interface AppGraph {                                   │  │
│  │   val pokemonListViewModel: PokemonListViewModel       │  │
│  │                                                         │  │
│  │   @DependencyGraph.Factory                             │  │
│  │   fun interface Factory {                              │  │
│  │     fun create(@Provides baseUrl: String): AppGraph    │  │
│  │   }                                                     │  │
│  │ }                                                       │  │
│  └────────────────────────────────────────────────────────┘  │
│  Plugins: alias(libs.plugins.metro)  // for graph generation    │
│  Dependencies: api(projects.features.pokemonlist.wiring)      │
└─────────────────────────┬────────────────────────────────────┘
                          │ api (for Metro to discover contributions)
                          ↓
┌──────────────────────────────────────────────────────────────┐
│              features:pokemonlist:wiring                      │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ @BindingContainer                                      │  │
│  │ @ContributesTo(AppScope::class)                        │  │
│  │ interface PokemonListProviders {                       │  │
│  │   companion object {                                   │  │
│  │     @Provides                                          │  │
│  │     fun provideHttpClient(): HttpClient { ... }        │  │
│  │                                                         │  │
│  │     @Provides                                          │  │
│  │     fun provideApiService(                             │  │
│  │       client: HttpClient,                              │  │
│  │       @Provides baseUrl: String                        │  │
│  │     ): ApiService { ... }                              │  │
│  │                                                         │  │
│  │     @Provides                                          │  │
│  │     fun provideRepository(...): Repository { ... }     │  │
│  │                                                         │  │
│  │     @Provides                                          │  │
│  │     fun provideViewModel(...): ViewModel { ... }       │  │
│  │   }                                                     │  │
│  │ }                                                       │  │
│  └────────────────────────────────────────────────────────┘  │
│  Plugins: convention.feature.wiring  // applies Metro            │
│  Dependencies: api, data, presentation  // NOT core:di!         │
└─────────────────────────┬────────────────────────────────────┘
                          │
                          ↓
              api / data / presentation modules
```

**Dependency Rules**:
- ✅ `core:di` depends on `wiring` (api) - for Metro discovery
- ✅ `wiring` depends on `api`, `data`, `presentation`
- ❌ `wiring` MUST NOT depend on `core:di` (circular dependency)
- ✅ `composeApp` depends on `core:di` (implementation)

---

## 🔗 Related Documentation

- [dependency_injection.md](dependency_injection.md) - Comprehensive DI guidelines
- [conventions.md](conventions.md) - Impl + Factory pattern, module structure
- [convention_plugins_quick_ref.md](convention_plugins_quick_ref.md) - Convention plugin usage
- [testing_strategy.md](testing_strategy.md) - Testing without DI framework

---

## 📚 External Resources

- **Metro Official Docs**: [zacsweers.github.io/metro](https://zacsweers.github.io/metro)
  - [Dependency Graphs](https://zacsweers.github.io/metro/dependency-graphs/)
  - [Aggregation](https://zacsweers.github.io/metro/aggregation/)
  - [Scopes](https://zacsweers.github.io/metro/scopes/)
  - [Runtime Parameters](https://zacsweers.github.io/metro/runtime-parameters/)

- **Gradle Compilation Avoidance**: [Our Approach to Faster Compilation](https://blog.gradle.org/our-approach-to-faster-compilation)

- **Wiring Modules Pattern**: [Pragmatic Modularization: The Case for Wiring Modules](https://proandroiddev.com/pragmatic-modularization-the-case-for-wiring-modules-c936d3af3611)

---

## ✅ Checklist for New Features

When adding a new feature with DI:

- [ ] Create `:features:<name>:wiring` module with `convention.feature.wiring` plugin
- [ ] Create `@BindingContainer` + `@ContributesTo(AppScope::class)` interface
- [ ] Add `@Provides` functions in companion object
- [ ] Use `@SingleIn(AppScope::class)` for singletons
- [ ] Call factory functions (not constructors) in providers
- [ ] Add wiring module as `api` dependency to `core:di`
- [ ] Add properties to `AppGraph` interface for exposed dependencies
- [ ] Verify wiring module does NOT depend on `core:di`
- [ ] Build and verify no circular dependency errors
- [ ] Test graph initialization with `createGraphFactory()`

---

**Last Updated**: November 25, 2025
