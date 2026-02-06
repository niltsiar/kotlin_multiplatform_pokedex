---
name: kmp-navigation
description: "Navigation 3 modular architecture for Kotlin Multiplatform Compose with type-safe routes, scoped navigation, and Koin DI integration. Use when: (1) Setting up Navigation 3 with Navigator and EntryProviderInstaller, (2) Creating route objects in :api modules, (3) Wiring navigation in :wiring-ui modules with Koin, (4) Implementing parametric routes with type safety, (5) Adding metadata-based animations for transitions"
---

# KMP Navigation Skill

Navigation 3 modular architecture for Kotlin Multiplatform Compose with type-safe routes and scoped navigation patterns.

## When to Use This Skill

**MANDATORY**: Load this skill when working on:
- Setting up Navigation 3 with `Navigator` and `EntryProviderInstaller`
- Creating route objects in `:features:<feature>:api` modules
- Wiring navigation in `:features:<feature>:wiring-ui` modules with Koin
- Implementing parametric routes with type safety
- Adding metadata-based animations for screen transitions

**Do NOT use for**: ViewModel implementation → use @kmp-presentation, Repository patterns → use @kmp-data-layer, DI configuration → use @kmp-di

## Core Principle

**Route objects in `:api`, UI registration in `:wiring-ui`, Navigator in `:core:navigation`**

## Quick Reference

### Route Objects

```kotlin
// Simple route (no parameters)
object PokemonList

// Parameterized route
data class PokemonDetail(val id: Int)
```

**Characteristics**: Plain Kotlin objects, no `@Serializable` needed, exported to iOS via `:shared`

### Navigator Class

```kotlin
// In :core:navigation
class Navigator(startDestination: Any) {
    private val _backStack = mutableStateListOf(startDestination)
    val backStack: List<Any> = _backStack

    fun goTo(destination: Any) { _backStack.add(destination) }
    fun goBack() {
        if (_backStack.size > 1) _backStack.removeAt(_backStack.lastIndex)
    }
}
```

### Navigation Wiring

```kotlin
// :features:pokemonlist:wiring-ui
val pokemonListNavigationModule = module {
    single<Set<EntryProviderInstaller>>(named("pokemonListNavigationInstallers")) {
        setOf({
            entry<PokemonList> {
                val navigator: Navigator = koinInject()
                val viewModel: PokemonListViewModel = koinInject()

                PokemonListScreen(
                    viewModel = viewModel,
                    onPokemonClick = { navigator.goTo(PokemonDetail(it.id)) }
                )
            }
        })
    }
}
```

### Parametric Routes

```kotlin
entry<PokemonDetail> { route ->
    val viewModel: PokemonDetailViewModel = koinViewModel(
        key = "pokemon_detail_${route.id}",  // Essential for unique instances
        parameters = { parametersOf(route.id) }
    )

    PokemonDetailScreen(
        viewModel = viewModel,
        onBackClick = { navigator.goBack() }
    )
}
```

### Animations (Metadata-Based)

```kotlin
entry<PokemonDetail>(
    metadata = NavDisplay.transitionSpec(
        slideInHorizontally(initialOffsetX = { it }) + fadeIn(tween(300))
    ) + NavDisplay.popTransitionSpec(
        slideOutHorizontally(targetOffsetX = { it }) + fadeOut(tween(300))
    )
) { /* content */ }
```

## Reference Loading Guide

| Task | Reference | Load When |
|------|-----------|-----------|
| Navigation 3 setup & configuration | [navigation3-setup.md](references/navigation3-setup.md) | Setting up new navigation |
| Parametric routes with type safety | [parametric-routes.md](references/parametric-routes.md) | Creating routes with parameters |
| Scoped navigation for feature modules | [scoped-navigation.md](references/scoped-navigation.md) | Wiring feature navigation |

## Related Skills

| Skill | Use For |
|-------|---------|
| @kmp-architecture | Module structure and vertical slicing |
| @compose-screen | Compose UI implementation |
| @swiftui-screen | SwiftUI navigation (iOS) |
| @kmp-presentation | ViewModel integration with routes |

## Documentation Sources

| Document | Purpose | Tokens |
|----------|---------|--------|
| [navigation.md](../../../docs/tech/navigation.md) | Complete navigation guide | ~9000 |
| [navigation_patterns.md](../../../docs/patterns/navigation_patterns.md) | Code examples and patterns | ~5500 |

## Validation Commands

```bash
# Build and test
./gradlew :composeApp:assembleDebug test --continue

# Verify navigation module dependencies
./gradlew :features:<feature>:api:dependencies --configuration commonMain

# Verify iOS export configuration (should NOT include navigation)
./gradlew :shared:dependencies --configuration iosMain
```

## Anti-Patterns to Avoid

| ❌ DON'T | ✅ DO |
|----------|-------|
| Use `@Serializable` on routes | Plain Kotlin objects |
| Export navigation modules to iOS | Only export `:api` and `:presentation` |
| Store Navigator in ViewModel | Pass as callback or inject in Composable |
| Direct transition params on `entry<T>()` | Use metadata with `NavDisplay.transitionSpec` |

## Reference Implementations

- `core/navigation/src/commonMain/kotlin/Navigator.kt` — Back stack manager
- `features/pokemonlist/wiring-ui/PokemonListNavigationProviders.kt` — Simple navigation
- `features/pokemondetail/wiring-ui/PokemonDetailNavigationProviders.kt` — Parametric routes
