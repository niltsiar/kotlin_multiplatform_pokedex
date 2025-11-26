# Dependency Injection Guidelines

Purpose: Establish consistent DI patterns using Koin for Kotlin Multiplatform with runtime injection, type-safe DSL, and vertical-slice feature modules.

References
- Koin docs: [insert-koin.io](https://insert-koin.io) (runtime DI with type-safe DSL, multiplatform)
- Koin KMP: [insert-koin.io/docs/reference/koin-mp](https://insert-koin.io/docs/reference/koin-mp/kmp/) (multiplatform support)

## Framework Choice
- We use Koin 4.0.1 across all platforms (Android, JVM Desktop, iOS). It's Kotlin Multiplatform-ready with a clean DSL for defining modules.
- Keep production classes free of DI annotations. Use Koin's `module {}` DSL in separate wiring modules to assemble dependencies.
- Use Koin's scopes and module system to wire feature modules cleanly.

## Architecture: Vertical Slices with api/data/presentation/ui/wiring
- Each feature lives in its own set of modules using the pattern:
  - `:features:<feature>:api` — public contracts (interfaces, models), navigation contracts
  - `:features:<feature>:data` — network layer, data layer (API services, DTOs, repositories, mappers)
  - `:features:<feature>:presentation` — ViewModels and UI state (shared with iOS)
  - `:features:<feature>:ui` — Compose UI screens (Android + JVM only, NOT exported to iOS)
  - `:features:<feature>:wiring` — Koin module definitions for the feature
- Only `api` modules are visible to other features; all other modules remain internal

## Location and Structure
- App-wide DI configuration lives in `:core:di/src/commonMain/.../AppGraph.kt`
- Feature-specific Koin modules live in `:features:<feature>:wiring/src/commonMain/...`
- Platform-specific DI (navigation, UI) uses platform source sets (`androidMain`, `jvmMain`)
- iOS uses only `commonMain` (no UI dependencies)

## Defining App Modules

```kotlin
// core/di/src/commonMain/.../AppGraph.kt
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import com.minddistrict.multiplatformpoc.core.navigation.Navigator
import com.minddistrict.multiplatformpoc.features.pokemonlist.api.PokemonList

object AppGraph {
    /**
     * Creates the complete list of Koin modules for the application.
     * 
     * @param baseUrl Base URL for API services (runtime parameter)
     * @param featureModules List of feature-specific Koin modules
     * @return Complete list of modules to initialize Koin
     */
    fun create(baseUrl: String, featureModules: List<Module>): List<Module> {
        val coreModule = module {
            // Provide Navigator singleton with start destination
            single { Navigator(startDestination = PokemonList) }
            
            // Provide runtime base URL as named dependency
            single(qualifier = named("baseUrl")) { baseUrl }
        }
        
        return listOf(coreModule) + featureModules
    }
}
```

**Key Points**:
- `AppGraph` is a simple object with a `create()` function returning a list of Koin modules
- Runtime parameters (like `baseUrl`) are provided as named dependencies: `single(qualifier = named("key")) { value }`
- Feature modules are passed in and aggregated with core modules
- No code generation required - plain Kotlin code

## Providing and Binding Dependencies (no annotations on classes)

Use Koin's DSL in wiring modules. Classes remain DI-agnostic (no annotations).

```kotlin
// Example: Providing dependencies in a wiring module
val featureModule = module {
    // Singleton: Same instance shared across app
    single<HttpClient> {
        createHttpClient()
    }
    
    // Factory: New instance each time
    factory<ApiService> {
        ApiService(client = get(), baseUrl = get(named("baseUrl")))
    }
    
    // Impl + Factory pattern: Call factory function
    factory<UserRepository> {
        UserRepository(
            api = get()
        )
    }
    
    // ViewModel with dependencies
    factory<ProfileViewModel> {
        ProfileViewModel(
            repository = get()
        )
    }
}
```

**Koin DSL Basics**:
- `single { }` - Singleton (one instance shared)
- `factory { }` - New instance on each request
- `get()` - Resolve dependency
- `get(named("key"))` - Resolve named dependency
- `get<Type>()` - Resolve by explicit type

## Contributing Bindings from Feature Modules

Feature modules define Koin modules in their wiring layer:

```kotlin
// features/pokemonlist/wiring/src/commonMain/.../PokemonListModule.kt
package com.minddistrict.multiplatformpoc.features.pokemonlist.wiring

import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import com.minddistrict.multiplatformpoc.features.pokemonlist.api.PokemonListRepository
import com.minddistrict.multiplatformpoc.features.pokemonlist.data.PokemonListApiService
import com.minddistrict.multiplatformpoc.features.pokemonlist.data.PokemonListRepository
import com.minddistrict.multiplatformpoc.features.pokemonlist.presentation.PokemonListViewModel

val pokemonListModule: Module = module {
    // HttpClient singleton for this feature
    single<HttpClient> {
        createHttpClient()
    }
    
    // API service factory
    factory<PokemonListApiService> {
        PokemonListApiService(
            client = get(),
            baseUrl = get(named("baseUrl"))
        )
    }
    
    // Repository factory (calls factory function - Impl + Factory pattern)
    factory<PokemonListRepository> {
        PokemonListRepository(
            apiService = get()
        )
    }
    
    // ViewModel factory
    factory<PokemonListViewModel> {
        PokemonListViewModel(
            repository = get()
        )
    }
}
```

**Key Points**:
- Define module as `val moduleName: Module = module { }`
- Use `factory<Interface>` for repositories and ViewModels (new instance per request)
- Use `single<Type>` for shared resources (HttpClient, Navigator)
- Call factory functions, not constructors (Impl + Factory pattern)
- **Critical**: No circular dependencies - wiring modules MUST NOT depend on `:core:di`

**See**: Working example in `features/pokemonlist/wiring/src/commonMain/kotlin/.../PokemonListModule.kt`

## Feature Modules and DI

### In `:features:<name>:api`
- Expose only contracts needed cross-feature (e.g., `UserRepository`, navigation routes, domain interfaces)
- Avoid leaking implementation details

### In `:features:<name>:data`
- Implement network layer: API services, DTOs, HTTP clients
- Implement data layer: repository contracts, mappers
- Keep classes DI-agnostic
- Implementations should be private/internal and named `<InterfaceName>Impl`

### In `:features:<name>:presentation`
- Implement ViewModels and UI state
- **Shared across all platforms** (Android, Desktop, iOS)
- ViewModels extend `androidx.lifecycle.ViewModel` (KMP)
- Keep DI-agnostic

### In `:features:<name>:ui`
- Implement Compose UI screens (@Composable functions)
- **Android + JVM only** (no iOS targets)
- Keep DI-agnostic

### In `:features:<name>:wiring`
- Define Koin modules that bind implementations to interfaces
- Use **platform-specific source sets** for UI dependencies:
  - `commonMain` → Provides repositories, ViewModels (all platforms)
  - `androidMain`/`jvmMain` → Provides UI entry points, navigation (Android/JVM only)
  - iOS targets use only `commonMain` (no UI dependencies)

```kotlin
// features/profile/api/src/commonMain/.../ProfileRepository.kt
interface ProfileRepository { 
    suspend fun load(): Either<RepoError, Profile> 
}

// features/profile/data/src/commonMain/.../ProfileRepositoryImpl.kt
internal class ProfileRepositoryImpl(
    private val api: ProfileApiService
) : ProfileRepository

// features/profile/data/src/commonMain/.../ProfileRepositoryFactory.kt
fun ProfileRepository(api: ProfileApiService): ProfileRepository = 
    ProfileRepositoryImpl(api)

// features/profile/wiring/src/commonMain/.../ProfileModule.kt
val profileModule = module {
    factory<ProfileRepository> {
        ProfileRepository(api = get())  // Calls factory function
    }
    
    factory<ProfileViewModel> {
        ProfileViewModel(repository = get())
    }
}
```

### Wiring/Aggregation Modules
- Wiring modules assemble and aggregate dependencies for a feature
- Keep implementation classes free of DI annotations and private to their modules
- Naming: `:features:<feature>:wiring` (feature-local)
- Responsibilities:
  - Define Koin modules that wire implementations to interfaces
  - Aggregate multibindings (e.g., sets of navigation entries)
  - Provide feature-scoped dependencies

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
            implementation(libs.koin.core)
        }
        
        // Android: UI dependencies
        val androidMain by getting {
            dependencies {
                implementation(projects.features.profile.ui)
                implementation(libs.koin.compose)
            }
        }
        
        // JVM Desktop: UI dependencies
        val jvmMain by getting {
            dependencies {
                implementation(projects.features.profile.ui)
                implementation(libs.koin.compose)
            }
        }
        
        // iOS: Uses only commonMain (no :ui module)
        // iOS gets ViewModels from :presentation via :shared framework
    }
}
```

**Why this works**:
- Koin is multiplatform-compatible
- Wiring modules can be consumed by iOS via `:shared` export
- iOS targets only compile `commonMain` dependencies (repos + ViewModels)
- Android/JVM targets compile `commonMain` + platform-specific (repos + ViewModels + UI)

## Platform-Specific Navigation Modules

For UI navigation (Android/Desktop only), use platform-specific source sets:

```kotlin
// features/pokemonlist/wiring/src/androidMain/.../PokemonListNavigationProviders.kt
package com.minddistrict.multiplatformpoc.features.pokemonlist.wiring

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject
import org.koin.core.module.Module
import org.koin.dsl.module
import com.minddistrict.multiplatformpoc.core.navigation.EntryProviderInstaller
import com.minddistrict.multiplatformpoc.features.pokemonlist.api.PokemonList
import com.minddistrict.multiplatformpoc.features.pokemonlist.ui.PokemonListScreen

val pokemonListNavigationModule: Module = module {
    single<Set<EntryProviderInstaller>> {
        setOf(
            {
                entry<PokemonList> {
                    PokemonListScreen(
                        viewModel = koinInject(),
                        onPokemonClick = koinInject<Navigator>().goTo(PokemonDetail(it.id))
                    )
                }
            }
        )
    }
}
```

**Key Points**:
- Navigation modules live in `androidMain`/`jvmMain` (platform-specific)
- Provide `Set<EntryProviderInstaller>` for navigation system
- Use `koinInject<T>()` to resolve dependencies in composable context
- Each feature provides its own navigation module

**See**: Working examples in `features/pokemonlist/wiring/src/androidMain/.../PokemonListNavigationProviders.kt`

## Initialization

Initialize Koin at app startup using `KoinApplication`:

```kotlin
// composeApp/src/commonMain/.../App.kt
import androidx.compose.runtime.Composable
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import com.minddistrict.multiplatformpoc.core.di.AppGraph
import com.minddistrict.multiplatformpoc.features.pokemonlist.wiring.pokemonListModule
import com.minddistrict.multiplatformpoc.features.pokemonlist.wiring.pokemonListNavigationModule
import com.minddistrict.multiplatformpoc.features.pokemondetail.wiring.pokemonDetailNavigationModule

@Composable
fun App() {
    KoinApplication(
        application = {
            modules(
                AppGraph.create(
                    baseUrl = "https://pokeapi.co/api/v2",
                    featureModules = listOf(
                        pokemonListModule,
                        pokemonListNavigationModule,
                        pokemonDetailNavigationModule
                    )
                )
            )
        }
    ) {
        // Access dependencies using koinInject()
        val navigator: Navigator = koinInject()
        val entryProviderInstallers: Set<EntryProviderInstaller> = koinInject()
        
        // Use in UI
        NavDisplay(
            backStack = navigator.backStack,
            onBack = { navigator.goBack() },
            entryProvider = entryProvider {
                entryProviderInstallers.forEach { this.it() }
            }
        )
    }
}
```

**Key Points**:
- `KoinApplication { }` - Initialize Koin with modules
- `modules(...)` - Pass list of Koin modules
- `koinInject<T>()` - Resolve dependencies in composable context
- Runtime parameters passed via `AppGraph.create()`
- No code generation, pure Kotlin

**See**: Working example in `composeApp/src/commonMain/kotlin/.../App.kt`

## Dependency Resolution Patterns

### In Composables (koinInject)
```kotlin
@Composable
fun ProfileScreen() {
    val viewModel: ProfileViewModel = koinInject()
    val navigator: Navigator = koinInject()
    
    // Use dependencies...
}
```

### In ViewModels (constructor injection)
```kotlin
class ProfileViewModel(
    private val repository: ProfileRepository,
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob())
) : ViewModel(viewModelScope) {
    // ViewModel implementation
}

// Koin module
val module = module {
    factory<ProfileViewModel> {
        ProfileViewModel(repository = get())
    }
}
```

### Named Dependencies
```kotlin
// Define named dependency
single(qualifier = named("apiKey")) { "secret-key" }

// Resolve named dependency
factory<ApiService> {
    ApiService(apiKey = get(named("apiKey")))
}
```

## Platform-Specific Dependencies
- Use expect/actual when you must bind platform-specific services
- Contribute the actuals in platform source sets and expose their contracts in commonMain

```kotlin
// commonMain
expect fun platformHttpClient(): HttpClient

val module = module {
    single { platformHttpClient() }
}

// androidMain
actual fun platformHttpClient(): HttpClient = HttpClient(OkHttp)

// iosMain
actual fun platformHttpClient(): HttpClient = HttpClient(Darwin)
```

## Best Practices
- Keep DI setup in commonMain where feasible; isolate platform specifics to source sets
- Use `single` for shared resources (HttpClient, Navigator, repositories with caching)
- Use `factory` for stateless services and ViewModels (new instance per request)
- Validate module visibility: only `api` modules are visible to other features
- Keep wiring modules simple - just module definitions, no business logic
- Use named qualifiers for multiple instances of same type
- Avoid circular dependencies between modules

### Build Performance: Compilation Avoidance
- Wiring modules improve build speed by leveraging Gradle Compilation Avoidance
- App modules depend on wiring; wiring depends on `api` + `data` + `presentation`
- Other features depend only on `api`
- See: [Pragmatic Modularization](https://proandroiddev.com/pragmatic-modularization-the-case-for-wiring-modules-c936d3af3611)

## iOS Umbrella (shared module)
- The `:shared` module is an umbrella framework for the iOS app
- It exports all required feature `api` modules and `presentation` modules (ViewModels)
- Keeps data layer and UI implementations internal

Ensure Gradle `export` entries include:
- `:features:<feature>:api` modules (repository interfaces, domain models, navigation)
- `:features:<feature>:presentation` modules (ViewModels, UI state - shared with iOS)
- `:core:*` modules (shared utilities, domain types)

**Do NOT export**:
- `:features:<feature>:data` modules (internal data layer)
- `:features:<feature>:ui` modules (Compose UI - Android/JVM only)
- `:features:<feature>:wiring` modules (can be exported if needed for iOS Koin setup)
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

#### 1. "No definition found for type 'X'"
**Cause**: Dependency not defined in any Koin module

**Fix**: Add definition to appropriate module
```kotlin
val module = module {
    factory<MyService> { MyServiceImpl() }
}
```

#### 2. "Cyclic dependency detected"
**Cause**: Two dependencies depend on each other

**Fix**: Refactor to break cycle, use lazy injection, or rethink architecture
```kotlin
// Before: A → B, B → A (cyclic)
// After: A → B, B → C, A → C (no cycle)
```

#### 3. "Cannot resolve parameter 'baseUrl' in XApiService"
**Cause**: Named dependency not provided

**Fix**: Provide named dependency and resolve with qualifier
```kotlin
// Provide
single(qualifier = named("baseUrl")) { "https://api.example.com" }

// Resolve
factory<ApiService> {
    ApiService(baseUrl = get(named("baseUrl")))
}
```

#### 4. "Definition for 'Set<EntryProviderInstaller>' not found"
**Cause**: Navigation module not included in initialization

**Fix**: Add navigation module to `KoinApplication`
```kotlin
KoinApplication(
    application = {
        modules(
            AppGraph.create(
                baseUrl = "...",
                featureModules = listOf(
                    pokemonListNavigationModule  // Add this
                )
            )
        )
    }
)
```

#### 5. "koinInject() can only be called from a @Composable function"
**Cause**: Trying to use `koinInject()` outside composable context

**Fix**: Use constructor injection in ViewModels, or get Koin instance directly
```kotlin
// In ViewModel (constructor injection)
class MyViewModel(private val repo: MyRepository) : ViewModel()

// In non-composable Kotlin code
val koin = GlobalContext.get()
val myService: MyService = koin.get()
```

#### 6. "More than one dependency found for type 'HttpClient'"
**Cause**: Multiple definitions for same type without qualifiers

**Fix**: Use named qualifiers to distinguish
```kotlin
single(qualifier = named("pokemonClient")) { createHttpClient() }
single(qualifier = named("userClient")) { createHttpClient() }

factory<PokemonService> {
    PokemonService(client = get(named("pokemonClient")))
}
```

**Quick Reference**: See [koin_di_quick_ref.md](koin_di_quick_ref.md) for more patterns and solutions.

## Testing Considerations
- For tests, use `koinApplication { }` to create isolated Koin instances
- Override modules in tests with test doubles
- Use `checkModules()` to validate module definitions at compile time

```kotlin
// Unit test with Koin
class MyRepositoryTest : StringSpec({
    "should fetch data" {
        val koin = koinApplication {
            modules(module {
                single<ApiService> { mockk() }
                factory<MyRepository> { MyRepositoryImpl(get()) }
            })
        }
        
        val repository = koin.koin.get<MyRepository>()
        // Test repository...
    }
})

// Module validation (runs at test time)
class ModuleCheckTest : StringSpec({
    "verify Koin configuration" {
        koinApplication {
            modules(
                AppGraph.create(
                    baseUrl = "https://test.com",
                    featureModules = listOf(pokemonListModule)
                )
            )
        }.checkModules()  // Validates all definitions can be resolved
    }
})
```

## Migration from Metro

If you're migrating from Metro to Koin, refer to:
- Migration Guide: [.junie/metro_to_koin_migration.md](../../metro_to_koin_migration.md)
- Pattern comparison and step-by-step migration instructions included

## Notes
- Koin performs dependency resolution at runtime (unlike Metro's compile-time validation)
- Use `checkModules()` in tests to validate modules before runtime
- Koin is lighter-weight and more flexible than Metro, with excellent multiplatform support
- Quick Reference: [koin_di_quick_ref.md](koin_di_quick_ref.md)
