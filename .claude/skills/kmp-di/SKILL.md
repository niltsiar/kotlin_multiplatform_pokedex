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

## Reference Loading Guide

| Task | Reference | Load When |
|------|-----------|-----------|
| Module definition patterns | [koin-modules.md](references/koin-modules.md) | Creating Koin modules |
| Impl + Factory pattern | [factory-functions.md](references/factory-functions.md) | Wiring repositories |
| Parametric injection | [parametric-injection.md](references/parametric-injection.md) | ViewModels with parameters |
| ViewModel wiring | [viewmodel-wiring.md](references/viewmodel-wiring.md) | Injecting ViewModels |

## Related Skills

| Skill | Use For |
|-------|---------|
| @kmp-presentation | ViewModel lifecycle and state |
| @kmp-data-layer | Repository patterns with Either |
| @kmp-architecture | Module structure and vertical slicing |
| @compose-screen | Compose UI implementation |
| @kmp-navigation | Navigation 3 setup |

## Documentation Sources

| Document | Purpose | Tokens |
|----------|---------|--------|
| [dependency_injection.md](../../../docs/tech/dependency_injection.md) | Complete DI guide | ~700 |
| [koin_di_quick_ref.md](../../../docs/tech/koin_di_quick_ref.md) | Quick reference | ~725 |

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
