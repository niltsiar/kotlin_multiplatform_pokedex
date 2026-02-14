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
@Serializable
object PokemonList

// Parameterized route
@Serializable
data class PokemonDetail(val id: Int)
```

**Characteristics**: Kotlin objects/data classes with `@Serializable` for state persistence, exported to iOS via `:shared`

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

## Essential Workflows

### Workflow 1: Define Route Object in :api

Create type-safe route objects in the `:api` module for public contract visibility.

1. Create a sealed class or object in `:features:<feature>:api/src/commonMain/.../navigation/`.
2. Annotate with `@Serializable` (required for Navigation 3 state restoration).
3. Export the `:api` module via `:shared` framework for iOS reference.

```kotlin
@Serializable
sealed class PokemonRoute {
    @Serializable
    object List : PokemonRoute()
    
    @Serializable
    data class Detail(val id: Int) : PokemonRoute()
}
```

### Workflow 2: Register Navigation with EntryProviderInstaller

Register screens in the `:wiring-ui` module using `EntryProviderInstaller`.

1. Define an `EntryProviderInstaller` lambda in `:wiring-ui`.
2. Use `entry<T> { }` to map routes to screens.
3. Inject `Navigator` and `ViewModel` using Koin.

```kotlin
val pokemonNavigationModule = module {
    single<Set<EntryProviderInstaller>>(named("pokemonInstallers")) {
        setOf({
            entry<PokemonRoute.List> {
                val navigator: Navigator = koinInject()
                PokemonListScreen(
                    viewModel = koinInject(),
                    onPokemonClick = { navigator.goTo(PokemonRoute.Detail(it.id)) }
                )
            }
        })
    }
}
```

### Workflow 3: Navigate with Type-Safe Routes

Use the `Navigator` singleton to trigger state changes.

1. Inject `Navigator` into your Composable or pass via callback.
2. Call `navigator.goTo(RouteObject)` (equivalent to `navigate`) for forward navigation.
3. Call `navigator.goBack()` for backward navigation.

```kotlin
onPokemonClick = { pokemon ->
    navigator.goTo(PokemonRoute.Detail(pokemon.id))
}
```

### Workflow 4: Handle Parametric Routes

Extract parameters from route objects in the navigation graph.

1. Define a `@Serializable` data class for the route.
2. Receive the typed `route` object in the `entry<T>` block.
3. Pass parameters to the ViewModel using `parametersOf()`.

```kotlin
entry<PokemonRoute.Detail> { route ->
    val viewModel: PokemonDetailViewModel = koinViewModel(
        parameters = { parametersOf(route.id) }
    )
    PokemonDetailScreen(viewModel = viewModel)
}
```

## Critical Guardrails

1. **NEVER put routes in `:wiring` modules** → routes belong in `:api` to serve as public navigation contracts.
2. **NEVER register navigation outside EntryProviderInstaller** → centralized registration in `:wiring-ui` ensures modularity.
3. **NEVER mix MaterialScope and UnstyledScope routes** → maintain strict scope separation for theme-specific navigation.
4. **NEVER bypass Navigator** → always use `Navigator.goTo()` to ensure back stack consistency and type safety.
5. **NEVER use string-based routes** → Navigation 3 in this project is strictly type-safe; use Kotlin objects/classes.
6. **NEVER skip @Serializable on route data classes** → required for state persistence and Navigation 3 compatibility.
7. **NEVER register the same route in multiple scopes** → each route should have exactly one UI implementation per theme.

## Cross-References

### Skills (by Category)

**Architecture**
| Skill | Purpose | Link |
|-------|---------|------|
| @kmp-architecture | Module structure and vertical slicing | [SKILL.md](../kmp-architecture/SKILL.md) |

**Layer Implementation**
| Skill | Purpose | Link |
|-------|---------|------|
| @kmp-presentation | ViewModel and UI state management | [SKILL.md](../kmp-presentation/SKILL.md) |
| @kmp-di | Koin dependency injection patterns | [SKILL.md](../kmp-di/SKILL.md) |

**Platform**
| Skill | Purpose | Link |
|-------|---------|------|
| @kmp-ios | SwiftUI + KMP integration | [SKILL.md](../kmp-ios/SKILL.md) |
| @compose-screen | Compose UI implementation | [SKILL.md](../compose-screen/SKILL.md) |
| @swiftui-screen | Native iOS UI with SwiftUI | [SKILL.md](../swiftui-screen/SKILL.md) |

### Documents

| Document | Purpose | Link |
|----------|---------|------|
| [@kmp-navigation](../kmp-navigation/SKILL.md) | Complete navigation architecture | Complete guide |
| [@kmp-architecture](../kmp-architecture/SKILL.md) | Master reference for architecture | Master ref |

## Predictive Back Navigation

**Last Updated:** December 30, 2025

### Status: DEFERRED

Predictive back gesture implementation has been deferred in favor of the official AndroidX solution.

### Background

Initial implementation used custom `PredictiveBackHandler` with platform-specific handlers (Android 13+ with `OnBackPressedCallback`, iOS/JVM no-ops) and Animatable-based transforms (10% scale reduction, 48dp translation).

### Deprecation Notice

**Custom PredictiveBackHandler is deprecated** in favor of:
- **NavigationBackHandler** from AndroidX Navigation Event library
- Reference: https://developer.android.com/jetpack/androidx/releases/navigationevent

### Future Implementation Path

When implementing predictive back gestures:

1. **Use AndroidX Navigation Event Library**
   - Dependency: `androidx.navigationevent:navigationevent` (or newer version)
   - Official support for predictive back patterns
   - Integrated with modular navigation (Nav 3)

2. **Benefits of Official Solution**
   - First-party support from Android team
   - Better integration with system gestures
   - Consistent behavior across Android versions
   - Automatic handling of edge cases

3. **Migration Plan** (when ready)
   - Add `androidx.navigationevent:navigationevent` dependency
   - Replace custom platform handlers with `NavigationBackHandler`
   - Update navigation metadata to support predictive animations
   - Test on Android 13+ devices

4. **Documentation Links**
   - [AndroidX Navigation Event Releases](https://developer.android.com/jetpack/androidx/releases/navigationevent)
   - [Predictive Back Design Guidelines](https://developer.android.com/guide/navigation/predictive-back-gesture)

### Current State

- ✅ Navigation transitions with motion tokens implemented
- ✅ Theme-based duration/easing curves working
- ❌ Predictive back gestures deferred until official library stable
- 📝 Custom PredictiveBackHandler files remain in codebase but unused

### Related Files (Not Currently Used)

- `core/navigation/src/commonMain/.../predictiveback/PredictiveBackHandler.kt`
- `core/navigation/src/androidMain/.../PlatformPredictiveBackHandler.android.kt`
- `core/navigation/src/iosMain/.../PlatformPredictiveBackHandler.ios.kt`
- `core/navigation/src/jvmMain/.../PlatformPredictiveBackHandler.jvm.kt`

These files can serve as reference for the transform calculations when migrating to the official solution.

## Troubleshooting Common Navigation Issues

### Navigation Provider "Unresolved reference" Errors

**Symptom:**
```kotlin
PokemonListScreenUnstyled(viewModel = ...)
// Error: Unresolved reference 'PokemonListScreenUnstyled'
```

**Cause:** Screen function naming convention mismatch.

**Correct Pattern:**
- ✅ `{Feature}UnstyledScreen` (e.g., `PokemonListUnstyledScreen`)
- ✅ `{Feature}MaterialScreen` (e.g., `PokemonListMaterialScreen`)
- ❌ `{Feature}ScreenUnstyled` (wrong suffix order)

**Solution:**
```kotlin
// ✅ CORRECT imports and usage
import ...ui.unstyled.PokemonListUnstyledScreen
PokemonListUnstyledScreen(viewModel = ...)

import ...ui.material.PokemonListMaterialScreen  
PokemonListMaterialScreen(viewModel = ...)
```

**Why:** Consistent naming convention: `{Adjective}{Noun}` not `{Noun}{Adjective}`.

---

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
| Skip `@Serializable` on routes | Use `@Serializable` for state restoration |
| Export navigation modules to iOS | Only export `:api` and `:presentation` |
| Store Navigator in ViewModel | Pass as callback or inject in Composable |
| Direct transition params on `entry<T>()` | Use metadata with `NavDisplay.transitionSpec` |

## Reference Implementations

- `core/navigation/src/commonMain/kotlin/Navigator.kt` — Back stack manager
- `features/pokemonlist/wiring-ui/PokemonListNavigationProviders.kt` — Simple navigation
- `features/pokemondetail/wiring-ui/PokemonDetailNavigationProviders.kt` — Parametric routes

## Troubleshooting

See [troubleshooting.md](references/troubleshooting.md) for common navigation issues and solutions.
