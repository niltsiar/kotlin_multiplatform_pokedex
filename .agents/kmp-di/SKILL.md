---
name: kmp-di
description: "Koin dependency injection patterns for Kotlin Multiplatform. Use when: (1) Creating Koin modules, (2) Configuring dependency injection in wiring modules, (3) Setting up parametric injection with parametersOf, (4) Wiring ViewModels to Koin, (5) Resolving dependencies in Compose with koinInject"
---

# Koin DI Skill

Kotlin Multiplatform dependency injection using Koin 4.x with type-safe DSL and vertical-slice feature modules.

## When to Use This Skill

**MANDATORY**: Load this skill when:
- Creating or modifying Koin modules in `:features:<feature>:wiring`
- Configuring dependency injection in `:core:di`
- Implementing parametric injection with `parametersOf`
- Wiring ViewModels to Koin dependency graph
- Resolving dependencies in Compose with `koinInject()`
- Setting up iOS helper functions for Koin resolution

**Do NOT use for**: ViewModel implementation → use @kmp-presentation, Repository implementation → use @kmp-data-layer, Navigation setup → use @kmp-navigation

## Core Principle

**Keep production classes free of DI annotations. Use Koin's `module {}` DSL in separate wiring modules.**

```kotlin
// ✅ DO: Pure Kotlin class (no annotations)
class ProfileRepository(private val api: ProfileApiService)

// ❌ DON'T: Coupled to DI framework
class ProfileRepository {
    @Inject
    lateinit var api: ProfileApiService
}
```

## Reference Loading Guide

| Task | Reference | Load When |
|------|-----------|-----------|
| Module definition patterns | [koin-modules.md](references/koin-modules.md) | Creating Koin modules |
| Impl + Factory pattern | [factory-functions.md](references/factory-functions.md) | Wiring repositories |
| Parametric injection | [parametric-injection.md](references/parametric-injection.md) | ViewModels with parameters |
| ViewModel wiring | [viewmodel-wiring.md](references/viewmodel-wiring.md) | Injecting ViewModels |

## Essential Workflows

### Workflow 1: Configure Koin Module for Feature

To set up dependency injection for a new feature module:

1. Create a `wiring` module if it doesn't exist: `:features:<feature>:wiring`.
2. Define the Koin module in `commonMain`:
   ```kotlin
   val <feature>Module = module {
       // 1. API Service (factory)
       factory { <Feature>ApiService(httpClient = get(), baseUrl = get(named("baseUrl"))) }
       
       // 2. Repository implementation via factory function (NOT constructor)
       factory<<Feature>Repository> { <Feature>Repository(apiService = get()) }
       
       // 3. ViewModel with SavedStateHandle
       viewModel { <Feature>ViewModel(repository = get(), savedStateHandle = get()) }
   }
   ```
3. Register the module in `composeApp/src/commonMain/kotlin/.../App.kt` by adding it to the `modules()` list in `KoinApplication`.

### Workflow 2: Register Parametric ViewModels

To handle ViewModels that require runtime arguments (like an ID):

1. Define the ViewModel constructor with parameters:
   ```kotlin
   class <Feature>DetailViewModel(
       private val id: String,
       private val repository: <Feature>Repository,
       savedStateHandle: SavedStateHandle
   ) : ViewModel()
   ```
2. Register in Koin using the `params` lambda:
   ```kotlin
   viewModel { params -> 
       <Feature>DetailViewModel(
           id = params.get(), // Extract by type
           repository = get(), 
           savedStateHandle = get()
       ) 
   }
   ```
3. Resolve in Compose using `koinViewModel` with `parametersOf`:
   ```kotlin
   val viewModel: <Feature>DetailViewModel = koinViewModel { parametersOf(id) }
   ```

### Workflow 3: Resolve Dependencies with parametersOf

To resolve factory dependencies that need dynamic runtime data:

1. Define a factory that accepts parameters:
   ```kotlin
   factory { params -> <Feature>Service(config = params.get()) }
   ```
2. Resolve inside another Koin definition or Composable:
   ```kotlin
   // Inside another Koin module definition
   factory { get { parametersOf("some-config") } }
   
   // In a Composable screen
   val service: <Feature>Service = koinInject { parametersOf(configValue) }
   ```

## Key Patterns

### Vertical Slice Structure

```
:features:<feature>:wiring/src/commonMain/.../
├── FeatureModule.kt          // Repos, ViewModels
└── androidMain/jvmMain/.../
    └── FeatureNavigationModule.kt  // Navigation
```

### Module Composition

```kotlin
fun App() {
    KoinApplication(
        application = {
            modules(
                coreModule(baseUrl = "...") +
                featureModule +
                featureNavigationModule
            )
        }
    ) { ... }
}
```

## Critical Guardrails

1. NEVER call RepositoryImpl constructor directly → ALWAYS use the public factory function defined in the `:data` module. This maintains implementation hiding and enables Gradle compilation avoidance.

2. NEVER use `single` for ViewModels → ViewModels MUST use the `viewModel` DSL to ensure they are cleared correctly by the system lifecycle.

3. NEVER forget SavedStateHandle in ViewModel registration → ViewModels should usually receive a `SavedStateHandle`. Register it using `get()` which Koin resolves automatically.

4. NEVER skip `factory` for repositories → Repositories should generally not be singletons. Use `factory` so each consumer gets its own instance, preventing accidental state sharing across features.

5. NEVER place Koin modules in `:core` → Each feature MUST own its own wiring module (`:features:<feature>:wiring`) to maintain vertical slice architecture and module isolation.

6. NEVER export `:wiring` modules to iOS → Only `:api` and `:presentation` should be exported to the iOS framework. iOS implements its own DI/Wrapper pattern for consuming ViewModels.

7. NEVER mix Navigation 3 scope markers → Ensure `MaterialScope` is only used in `:wiring-ui-material` and `UnstyledScope` in `:wiring-ui-unstyled` navigation registration.

## Quick Reference

```kotlin
// Define module
val profileModule = module {
    single<HttpClient> { createHttpClient() }              // Singleton
    factory<ProfileRepository> { ProfileRepository(get()) }  // New instance
    single(qualifier = named("baseUrl")) { "https://api.example.com" }  // Named
}

// Resolve in Compose
@Composable
fun ProfileScreen() {
    val viewModel: ProfileViewModel = koinInject()
}

// Initialize Koin
KoinApplication(
    application = { modules(coreModule + featureModule) }
) { ... }
```

## Quick Reference

# Koin DI Quick Reference

Last Updated: February 2, 2026

**Purpose**: Quick reference for Koin dependency injection patterns in Kotlin Multiplatform projects.

**Official Docs**: [insert-koin.io](https://insert-koin.io)  
**KMP Guide**: [insert-koin.io/docs/reference/koin-mp/kmp](https://insert-koin.io/docs/reference/koin-mp/kmp/)

---

## 🎯 Core Concepts

### What is Koin?

Koin is a **runtime dependency injection** framework for Kotlin Multiplatform with a pure Kotlin DSL.

**Key Features**:
- ✅ Pure Kotlin DSL (no annotations on production code)
- ✅ Runtime resolution with lazy injection
- ✅ Full multiplatform support (Android, iOS, JVM, Native, JS, WASM)
- ✅ Lightweight (no code generation or reflection)
- ✅ Easy testing with module overrides

**Version**: Koin 4.2.0-beta2

---

## 📦 Setup

### Version Catalog (`gradle/libs.versions.toml`)

```toml
[versions]
koin = "4.2.0-beta2"

[libraries]
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }
koin-android = { module = "io.insert-koin:koin-android", version.ref = "koin" }
koin-test = { module = "io.insert-koin:koin-test", version.ref = "koin" }
```

### Gradle Configuration

**Core DI Module** (`core/di/build.gradle.kts`):
```kotlin
plugins {
    id("convention.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.koin.core)
            api(projects.core.navigation)
        }
    }
}
```

**App Module** (`composeApp/build.gradle.kts`):
```kotlin
plugins {
    id("convention.kmp.android.app")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.di)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }
        
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
    }
}
```

**Feature Wiring Module** (`features/pokemonlist/wiring/build.gradle.kts`):
```kotlin
plugins {
    id("convention.feature.wiring")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core)
        }
        
        androidMain.dependencies {
            implementation(libs.koin.compose)
        }
        
        jvmMain.dependencies {
            implementation(libs.koin.compose)
        }
    }
}
```

---

## 🏗️ Module Architecture

### Dependency Flow

```
┌─────────────────────────────────────────────────┐
│                   composeApp                     │
│  - Initializes: KoinApplication                 │
│  - Depends on: core:di, feature wiring modules │
└─────────────────┬───────────────────────────────┘
                  │ implementation
                  ↓
┌─────────────────────────────────────────────────┐
│                    core:di                       │
│  - Contains: AppGraph.create()                  │
│  - Provides: Navigator, baseUrl                 │
└─────────────────┬───────────────────────────────┘
                  │ api
                  ↓
┌─────────────────────────────────────────────────┐
│          features:*:wiring modules               │
│  - Define: Koin modules for each feature        │
│  - Provide: Repos, ViewModels, Navigation       │
│  - Platform-specific source sets                │
└─────────────────────────────────────────────────┘
```

### Feature Module Structure

```
:features:pokemonlist:api           → Public contracts
:features:pokemonlist:data          → Network + data layer
:features:pokemonlist:presentation  → ViewModels (shared with iOS)
:features:pokemonlist:ui            → Compose UI (Android/JVM/iOS Compose)
:features:pokemonlist:wiring        → Koin modules
  ├── commonMain                    → Repos, ViewModels
  ├── androidMain                   → Navigation, UI
  ├── jvmMain                       → Navigation, UI
  └── iosMain                       → Navigation, UI (for iosAppCompose)
```

---

## 🔧 Basic Patterns

### Defining a Module

```kotlin
// Shared module (core) exposes singleton HttpClient
val httpClientModule = module {
    single<HttpClient> { createHttpClient() }
}

// Feature module focuses on feature-specific bindings
val myModule = module {
    // Factory - new instance on each request
    factory<MyRepository> {
        MyRepositoryImpl(api = get())
    }
    
    // Named dependency
    single(qualifier = named("apiKey")) { "secret-key" }
    
    // Get named dependency
    factory<ApiService> {
        ApiService(apiKey = get(named("apiKey")), client = get())
    }
}
```

### Impl + Factory Pattern

**Keep DI-agnostic classes:**

```kotlin
// api/ProfileRepository.kt
interface ProfileRepository {
    suspend fun getProfile(): Either<Error, Profile>
}

// data/ProfileRepositoryImpl.kt
internal class ProfileRepositoryImpl(
    private val api: ProfileApiService
) : ProfileRepository {
    override suspend fun getProfile(): Either<Error, Profile> = ...
}

// data/ProfileRepositoryFactory.kt
fun ProfileRepository(api: ProfileApiService): ProfileRepository =
    ProfileRepositoryImpl(api)

// wiring/ProfileModule.kt
val profileModule = module {
    factory<ProfileRepository> {
        ProfileRepository(api = get())  // Call factory function
    }
}
```

**Benefits:**
- Classes remain DI-agnostic
- Implementations are internal/private
- Easy to test without DI framework
- Clear factory function signatures

---

## 🎨 Common Patterns

### AppGraph Pattern

**Centralized module aggregation:**

```kotlin
// core/di/AppGraph.kt
object AppGraph {
    fun create(baseUrl: String, featureModules: List<Module>): List<Module> {
        val coreModule = module {
            single { Navigator(startDestination = HomeRoute) }
            single(qualifier = named("baseUrl")) { baseUrl }
        }
        
        return listOf(coreModule) + featureModules
    }
}
```

**Usage in app:**

```kotlin
@Composable
fun App() {
    KoinApplication(
        application = {
            modules(
                AppGraph.create(
                    baseUrl = "https://api.example.com",
                    featureModules = listOf(
                        profileModule,
                        profileNavigationModule,
                        settingsModule
                    )
                )
            )
        }
    ) {
        // App content with koinInject()
        MainScreen()
    }
}
```

### Feature Module Pattern

**Common module (all platforms):**

```kotlin
// features/pokemonlist/wiring/src/commonMain/.../PokemonListModule.kt
val pokemonListModule = module {
    factory<PokemonListApiService> {
        PokemonListApiService(
            client = get(),
            baseUrl = get(named("baseUrl"))
        )
    }
    
    factory<PokemonListRepository> {
        PokemonListRepository(apiService = get())
    }
    
    factory<PokemonListViewModel> {
        PokemonListViewModel(repository = get())
    }
}
```

**Platform-specific navigation (Android/JVM):**

```kotlin
// features/pokemonlist/wiring/src/androidMain/.../PokemonListNavigationProviders.kt
val pokemonListNavigationModule = module {
    single<Set<EntryProviderInstaller>> {
        setOf(
            {
                entry<PokemonList> {
                    PokemonListScreen(
                        viewModel = koinInject(),
                        onPokemonClick = { 
                            koinInject<Navigator>().goTo(PokemonDetail(it.id))
                        }
                    )
                }
            }
        )
    }
}
```

### ViewModel Pattern

**ViewModel with dependencies:**

```kotlin
class ProfileViewModel(
    private val repository: ProfileRepository,
    viewModelScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )
) : ViewModel(viewModelScope) {
    private val _state = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val state: StateFlow<ProfileUiState> = _state
    
    fun load() {
        viewModelScope.launch {
            repository.getProfile().fold(
                ifLeft = { _state.value = ProfileUiState.Error(it) },
                ifRight = { _state.value = ProfileUiState.Success(it) }
            )
        }
    }
}

// Koin module
val profileModule = module {
    factory<ProfileViewModel> {
        ProfileViewModel(repository = get())
    }
}
```

**Usage in Compose:**

```kotlin
@Composable
fun ProfileScreen() {
    val viewModel: ProfileViewModel = koinInject()
    val state by viewModel.state.collectAsState()
    
    when (state) {
        is ProfileUiState.Loading -> LoadingView()
        is ProfileUiState.Success -> ProfileContent(state.profile)
        is ProfileUiState.Error -> ErrorView(state.error)
    }
}
```

---

## 🧪 Testing

### Unit Tests with Koin

```kotlin
class ProfileRepositoryTest : StringSpec({
    "should fetch profile successfully" {
        val mockApi = mockk<ProfileApiService>()
        coEvery { mockApi.getProfile() } returns ProfileDto(...)
        
        val koin = koinApplication {
            modules(module {
                single<ProfileApiService> { mockApi }
                factory<ProfileRepository> { ProfileRepository(get()) }
            })
        }
        
        val repository = koin.koin.get<ProfileRepository>()
        val result = repository.getProfile()
        
        result.shouldBeRight { profile ->
            profile.name shouldBe "John Doe"
        }
    }
})
```

### Module Validation

```kotlin
class ModuleCheckTest : StringSpec({
    "verify Koin configuration" {
        koinApplication {
            modules(
                AppGraph.create(
                    baseUrl = "https://test.com",
                    featureModules = listOf(
                        profileModule,
                        settingsModule
                    )
                )
            )
        }.checkModules()  // Validates all definitions
    }
})
```

### Overriding Modules in Tests

```kotlin
class ProfileViewModelTest : StringSpec({
    "should handle error state" {
        val mockRepo = mockk<ProfileRepository>()
        coEvery { mockRepo.getProfile() } returns Either.Left(RepoError.Network)
        
        val koin = koinApplication {
            modules(
                profileModule,
                module {
                    factory<ProfileRepository>(override = true) { mockRepo }
                }
            )
        }
        
        val viewModel = koin.koin.get<ProfileViewModel>()
        viewModel.load()
        
        viewModel.state.value shouldBe ProfileUiState.Error(...)
    }
})
```

---

## 🚨 Troubleshooting

### "No definition found for type 'X'"

**Problem**: Koin can't find a definition for the requested type.

**Solutions**:
1. Add definition to module
   ```kotlin
   val module = module {
       factory<MyService> { MyServiceImpl() }
   }
   ```

2. Ensure module is included in `KoinApplication`
   ```kotlin
   KoinApplication(
       application = { modules(myModule) }
   )
   ```

3. Check type matches exactly (including generics)
   ```kotlin
   // Define
   single<List<String>> { listOf("a", "b") }
   
   // Get
   val list: List<String> = koinInject()  // Must match type exactly
   ```

### "Cyclic dependency detected"

**Problem**: Two dependencies depend on each other (A → B, B → A).

**Solution**: Refactor architecture to break cycle:

```kotlin
// ❌ BAD: Circular dependency
class ServiceA(val b: ServiceB)
class ServiceB(val a: ServiceA)

// ✅ GOOD: Extract common dependency
class ServiceA(val common: CommonService)
class ServiceB(val common: CommonService)
class CommonService()
```

### "Cannot resolve parameter 'baseUrl' in ApiService"

**Problem**: Named dependency not provided or wrong qualifier used.

**Solution**:
```kotlin
// Provide
single(qualifier = named("baseUrl")) { "https://api.com" }

// Resolve
factory<ApiService> {
    ApiService(baseUrl = get(named("baseUrl")))  // Use named()
}
```

### "koinInject() can only be called from @Composable"

**Problem**: Trying to use `koinInject()` outside composable context.

**Solutions**:

1. **In ViewModels**: Use constructor injection
   ```kotlin
   class MyViewModel(private val repo: MyRepository) : ViewModel()
   
   val module = module {
       factory<MyViewModel> { MyViewModel(get()) }
   }
   ```

2. **In regular Kotlin code**: Get Koin instance directly
   ```kotlin
   val koin = GlobalContext.get()
   val service: MyService = koin.get()
   ```

3. **In tests**: Use `koinApplication { }.koin.get()`
   ```kotlin
   val koin = koinApplication { modules(myModule) }
   val service = koin.koin.get<MyService>()
   ```

### "More than one dependency found for type 'HttpClient'"

**Problem**: Multiple definitions for the same type.

**Solution**: Use named qualifiers
```kotlin
// Define
single(qualifier = named("pokemonClient")) { createPokemonHttpClient() }
single(qualifier = named("userClient")) { createUserHttpClient() }

// Resolve
factory<PokemonService> {
    PokemonService(client = get(named("pokemonClient")))
}
```

### "Definition for 'Set<EntryProviderInstaller>' not found"

**Problem**: Navigation module not included.

**Solution**: Add navigation module to `KoinApplication`
```kotlin
KoinApplication(
    application = {
        modules(
            AppGraph.create(
                baseUrl = "...",
                featureModules = listOf(
                    pokemonListModule,
                    pokemonListNavigationModule  // ← Add this
                )
            )
        )
    }
)
```

---

## 🎯 Best Practices

### 1. Keep Classes DI-Agnostic

**❌ Don't do this:**
```kotlin
class MyRepository {
    private val api: ApiService by inject()  // Coupled to Koin
}
```

**✅ Do this:**
```kotlin
class MyRepository(private val api: ApiService)  // Pure Kotlin
```

### 2. Use Factory for Stateless Services

```kotlin
val networkModule = module {
    // Singleton for stateful/expensive resources
    single<HttpClient> { createHttpClient() }
}

val featureModule = module {
    // Factory for stateless services (new instance each time)
    factory<ProfileRepository> { ProfileRepository(get()) }
    factory<ProfileViewModel> { ProfileViewModel(get()) }
}
```

### 3. Platform-Specific Source Sets

```kotlin
// commonMain - All platforms
val commonModule = module {
    factory<ProfileRepository> { ProfileRepository(get()) }
}

// androidMain - Android only
val androidNavigationModule = module {
    single<Set<EntryProviderInstaller>> {
        setOf({ entry<ProfileRoute> { ProfileScreen() } })
    }
}
```

### 4. Named Dependencies for Disambiguation

```kotlin
val module = module {
    single(named("dev")) { "https://dev.api.com" }
    single(named("prod")) { "https://api.com" }
    
    factory<ApiService> {
        ApiService(baseUrl = get(named("prod")))
    }
}
```

### 5. Validate Modules in Tests

```kotlin
class ModuleValidationTest : StringSpec({
    "all modules resolve" {
        koinApplication {
            modules(allModules)
        }.checkModules()  // Fails fast if definition missing
    }
})
```

---

## 🔄 Migration from Metro

### Key Differences

| Metro | Koin |
|-------|------|
| Compile-time validation | Runtime resolution (use `checkModules()` in tests) |
| `@DependencyGraph` interface | `AppGraph.create()` function |
| `@Provides` functions in companion object | `module { }` DSL |
| `@ContributesTo(AppScope::class)` | Add to `featureModules` list |
| `createGraphFactory<T>().create()` | `KoinApplication { modules(...) }` |
| `graph.pokemonListViewModel` | `koinInject<PokemonListViewModel>()` |
| Requires Metro plugin | No plugins needed (pure library) |

### Migration Pattern

**Before (Metro):**
```kotlin
@BindingContainer
@ContributesTo(AppScope::class)
interface PokemonListProviders {
    companion object {
        @Provides
        fun provideRepository(api: ApiService): Repository = Repository(api)
    }
}
```

**After (Koin):**
```kotlin
val pokemonListModule = module {
    factory<Repository> {
        Repository(api = get())
    }
}
```

---

## 📚 Additional Resources

- **Official Documentation**: [insert-koin.io](https://insert-koin.io)
- **Compose Integration**: [insert-koin.io/docs/reference/koin-compose](https://insert-koin.io/docs/reference/koin-compose/compose/)
- **Testing**: [insert-koin.io/docs/reference/koin-test/testing](https://insert-koin.io/docs/reference/koin-test/testing)
- **Full Guide**: [dependency_injection.md](dependency_injection.md)

---

## 🎓 Quick Cheat Sheet

### Definition
```kotlin
val module = module {
    single { /* singleton */ }
    factory { /* new instance */ }
    single(named("key")) { /* named */ }
}
```

### Resolution
```kotlin
// In Composable
val vm: MyViewModel = koinInject()

// In ViewModel
class MyViewModel(private val repo: MyRepository)

// Module
factory<MyViewModel> { MyViewModel(get()) }

// Named
get(named("key"))
```

### Initialization
```kotlin
KoinApplication(
    application = { modules(myModule) }
) {
    // Content
}
```

### Testing
```kotlin
koinApplication {
    modules(myModule)
}.checkModules()
```

---

**Remember**: Keep it simple. Koin's power is in its simplicity and flexibility. Use it to wire dependencies cleanly while keeping your business logic DI-agnostic.

## Cross-References

### Skills (by Category)

**Architecture**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-architecture | Feature wiring module structure | [SKILL.md](../kmp-architecture/SKILL.md) |
| @kmp-critical-patterns | DI patterns in core patterns | [SKILL.md](../kmp-critical-patterns/SKILL.md) |

**Layer Implementation**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-presentation | ViewModel registration with Koin | [SKILL.md](../kmp-presentation/SKILL.md) |
| @kmp-data-layer | Repository factory functions for Koin | [SKILL.md](../kmp-data-layer/SKILL.md) |
| @kmp-mobile-expert | Complete feature wiring examples | [SKILL.md](../kmp-mobile-expert/SKILL.md) |

**Platform & UI**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-navigation | Navigation 3 scoped DI integration | [SKILL.md](../kmp-navigation/SKILL.md) |
| @compose-screen | koinViewModel injection in Compose | [SKILL.md](../compose-screen/SKILL.md) |

### Documents

| Document | Purpose | Link |
| --- | --- | --- |
| Dependency injection | Complete Koin patterns and troubleshooting | [dependency_injection.md](See @kmp-di skill) |
| Architecture conventions | Wiring module patterns | [conventions.md](See @kmp-architecture skill for architecture patterns) |

