# Koin DI Quick Reference

Last Updated: November 26, 2025

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
val myModule = module {
    // Singleton - one instance shared across app
    single<HttpClient> {
        createHttpClient()
    }
    
    // Factory - new instance on each request
    factory<MyRepository> {
        MyRepositoryImpl(api = get())
    }
    
    // Named dependency
    single(qualifier = named("apiKey")) { "secret-key" }
    
    // Get named dependency
    factory<ApiService> {
        ApiService(apiKey = get(named("apiKey")))
    }
}
```

### Impl + Factory Pattern

**Keep DI-agnostic classes:**

```kotlin
// api/ProfileRepository.kt
interface ProfileRepository {
    suspend fun getProfile(): Either<RepoError, Profile>
}

// data/ProfileRepositoryImpl.kt
internal class ProfileRepositoryImpl(
    private val api: ProfileApiService
) : ProfileRepository {
    override suspend fun getProfile(): Either<RepoError, Profile> = ...
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
    single<HttpClient> {
        createHttpClient()
    }
    
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
val module = module {
    // Singleton for stateful/expensive resources
    single<HttpClient> { createHttpClient() }
    
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

- **Official Documentation**: [insert-koin.io/docs](https://insert-koin.io/docs)
- **Compose Integration**: [insert-koin.io/docs/reference/koin-compose](https://insert-koin.io/docs/reference/koin-compose/compose/)
- **Testing**: [insert-koin.io/docs/reference/koin-test](https://insert-koin.io/docs/reference/koin-test/test/)
- **Full Guide**: [dependency_injection.md](dependency_injection.md)
- **Migration Guide**: [../../metro_to_koin_migration.md](../../metro_to_koin_migration.md)

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
