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
| Dependency injection | Complete Koin patterns and troubleshooting | [dependency_injection.md](../../../docs/tech/dependency_injection.md) |
| Architecture conventions | Wiring module patterns | [conventions.md](../../../docs/tech/conventions.md) |

