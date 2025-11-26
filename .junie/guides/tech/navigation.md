# Navigation Guidelines

## Overview

This guide covers Navigation 3 modular architecture for Kotlin Multiplatform with Compose. Navigation follows split-by-layer pattern: route contracts in `:api`, UI in `:ui`, and wiring in platform-specific source sets (androidMain/jvmMain).

## Architecture Principles

- **Feature-local navigation**: Each feature owns its routes and UI registration
- **Route objects in :api**: Plain Kotlin objects/data classes as navigation keys
- **UI in platform-specific wiring**: androidMain/jvmMain provide EntryProviderInstallers
- **No iOS exports**: Navigation is Compose-only (not exported via :shared)
- **Explicit back stack**: Navigator class manages navigation state

## Core Components

### Navigator (`:core:navigation`)

Back stack manager with explicit navigation control:

```kotlin
// core/navigation/src/commonMain/kotlin/.../Navigator.kt
class Navigator(startDestination: Any) {
    private val _backStack = mutableStateListOf(startDestination)
    val backStack: List<Any> = _backStack
    
    fun goTo(destination: Any) {
        _backStack.add(destination)
    }
    
    fun goBack() {
        if (_backStack.size > 1) {
            _backStack.removeAt(_backStack.lastIndex)
        }
    }
}
```

**Key features**:
- Uses `SnapshotStateList` for Compose reactivity
- Accepts `Any` type for destination keys
- Maintains minimum one destination (start destination)

### EntryProviderInstaller (`:core:navigation`)

Type alias for feature navigation contributions:

```kotlin
// core/navigation/src/commonMain/kotlin/.../EntryProviderInstaller.kt
typealias EntryProviderInstaller = EntryProviderScope<Any>.() -> Unit
```

**Purpose**: Enables Koin DI named qualifier collection for dynamic navigation graph assembly

## Route Definition Pattern

### Simple Route (No Parameters)

```kotlin
// :features:pokemonlist:api/src/commonMain/.../navigation/PokemonListEntry.kt
object PokemonList
```

**Characteristics**:
- Plain Kotlin `object` (singleton)
- No properties or methods
- Acts as navigation key
- No @Serializable needed
- Exported to iOS via :shared (for reference in shared code)

### Parameterized Route

```kotlin
// :features:pokemondetail:api/src/commonMain/.../navigation/PokemonDetailEntry.kt
data class PokemonDetail(val id: Int)
```

**Characteristics**:
- Plain Kotlin `data class`
- Parameters as constructor properties
- No @Serializable needed
- Acts as navigation key with state
- Exported to iOS via :shared (for reference in shared code)

**Why no @Serializable?**: Navigation 3 uses routes as in-memory keys, not for URL serialization

## Wiring Pattern (Platform-Specific)

### Module Structure

```
:features:pokemonlist:wiring/
├── build.gradle.kts                    # Depends on :core:navigation, :ui, :presentation
└── src/
    ├── commonMain/kotlin/              # Provides ViewModels, repositories via Koin
    ├── androidMain/kotlin/             # Provides EntryProviderInstaller for Android
    └── jvmMain/kotlin/                 # Provides EntryProviderInstaller for Desktop
```

### Common Main (Data Layer)

```kotlin
// :features:pokemonlist:wiring/src/commonMain/.../PokemonListModule.kt
import org.koin.dsl.module

val pokemonListModule = module {
    // Provide repository
    factory<PokemonListRepository> {
        PokemonListRepository(api = get())
    }
    
    // Provide ViewModel
    factory {
        PokemonListViewModel(repository = get())
    }
}
```

**Key points**:
- Uses Koin `module { }` DSL
- `factory` for new instances per injection
- Dependencies resolved via `get()`
- No annotations on production classes

### Android Main (UI Navigation)

```kotlin
// :features:pokemonlist:wiring/src/androidMain/.../PokemonListNavigationProviders.kt
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import org.koin.dsl.module

val pokemonListNavigationModule = module {
    single<Set<EntryProviderInstaller>>(named("pokemonListNavigationInstallers")) {
        setOf(
            {
                entry<PokemonList> {
                    val navigator: Navigator = koinInject()
                    val viewModel: PokemonListViewModel = koinInject()
                    
                    PokemonListScreen(
                        viewModel = viewModel,
                        onPokemonClick = { pokemon ->
                            navigator.goTo(PokemonDetail(pokemon.id))
                        },
                    )
                }
            }
        )
    }
}
```

**Key points**:
- Returns `Set<EntryProviderInstaller>` with named qualifier
- Named qualifier enables collection in `navigationAggregationModule`
- `koinInject()` resolves dependencies from Compose context
- `entry<RouteType>` registers composable for route

### JVM Main (Desktop UI Navigation)

```kotlin
// :features:pokemonlist:wiring/src/jvmMain/.../PokemonListNavigationProviders.kt
val pokemonListNavigationModule = module {
    single<Set<EntryProviderInstaller>>(named("pokemonListNavigationInstallers")) {
        setOf(
            {
                entry<PokemonList> {
                    val navigator: Navigator = koinInject()
                    val viewModel: PokemonListViewModel = koinInject()
                    
                    PokemonListScreen(
                        viewModel = viewModel,
                        onPokemonClick = { pokemon ->
                            navigator.goTo(PokemonDetail(pokemon.id))
                        },
                    )
                }
            }
        )
    }
}
```

**Pattern**: Identical to androidMain for simple cases, can diverge for platform-specific UI

### Parameterized Routes

```kotlin
// :features:pokemondetail:wiring/src/androidMain/.../PokemonDetailNavigationProviders.kt
import org.koin.core.parameter.parametersOf

val pokemonDetailNavigationModule = module {
    single<Set<EntryProviderInstaller>>(named("pokemonDetailNavigationInstallers")) {
        setOf(
            {
                entry<PokemonDetail> { route ->
                    val navigator: Navigator = koinInject()
                    // Use parametersOf to inject route parameter into ViewModel
                    val viewModel: PokemonDetailViewModel = koinInject(
                        parameters = { parametersOf(route.id) }
                    )
                    
                    PokemonDetailScreen(
                        viewModel = viewModel,
                        onBackClick = { navigator.goBack() }
                    )
                }
            }
        )
    }
}
```

**Key differences**:
- `entry<T> { route -> }` receives typed route object for parameter extraction
- `koinInject(parameters = { parametersOf(route.id) })` passes parameters to ViewModel
- ViewModel must accept parameter in constructor

## Application Integration

### Core DI Module (`:core:di`)

```kotlin
// core/di/src/commonMain/.../AppModules.kt
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun coreModule(baseUrl: String) = module {
    // Provide baseUrl as named dependency
    single(qualifier = named("baseUrl")) { baseUrl }
    
    // Provide Navigator singleton with start destination
    single { Navigator(PokemonList) }
}

val navigationAggregationModule = module {
    single<Set<EntryProviderInstaller>>(qualifier = named("allNavigationInstallers")) {
        val allInstallers = mutableSetOf<EntryProviderInstaller>()
        
        // Collect all named navigation installer sets from features
        runCatching {
            getOrNull<Set<EntryProviderInstaller>>(named("pokemonListNavigationInstallers"))
        }.getOrNull()?.let { allInstallers.addAll(it) }
        
        runCatching {
            getOrNull<Set<EntryProviderInstaller>>(named("pokemonDetailNavigationInstallers"))
        }.getOrNull()?.let { allInstallers.addAll(it) }
        
        allInstallers
    }
}
```

**Components**:
- `coreModule(baseUrl)`: Function taking runtime parameters
- `navigationAggregationModule`: Collects all feature navigation installers by named qualifier
- `runCatching`: Safely handles platform differences (some modules may not exist on all platforms)
- Returns aggregated `Set<EntryProviderInstaller>`

### App.kt Integration

```kotlin
// composeApp/src/commonMain/kotlin/.../App.kt
@Composable
fun App() {
    val navigator: Navigator = koinInject()
    val entryProviderInstallers: Set<EntryProviderInstaller> = koinInject(
        qualifier = named("allNavigationInstallers")
    )
    
    PokemonTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavDisplay(
                backStack = navigator.backStack,
                onBack = { navigator.goBack() },
                entryProvider = entryProvider {
                    entryProviderInstallers.forEach { this.it() }
                }
            )
        }
    }
}
```

**Flow**:
1. `koinInject()` resolves dependencies from Koin context
2. `navigationAggregationModule` provides aggregated installers
3. NavDisplay observes navigator.backStack (SnapshotStateList)
4. entryProvider installs all EntryProviderInstallers from features
5. Back navigation triggers navigator.goBack()

**Koin Initialization** (in main()):
```kotlin
// Android: Application.onCreate()
// Desktop: main() function
startKoin {
    modules(
        coreModule(baseUrl = "https://pokeapi.co/api/v2"),
        httpClientModule,
        pokemonListModule,
        pokemonDetailModule,
        pokemonListNavigationModule,  // Platform-specific (androidMain/jvmMain)
        pokemonDetailNavigationModule, // Platform-specific
        navigationAggregationModule   // Must be AFTER feature modules
    )
}
```

**Critical**: `navigationAggregationModule` must be loaded AFTER all feature navigation modules

## Navigation Operations

### Forward Navigation

```kotlin
// In feature screen
onPokemonClick = { pokemon ->
    navigator.goTo(PokemonDetail(pokemon.id))
}
```

**Cross-feature navigation**: Navigator injected via wiring, route object from :api module

### Back Navigation

```kotlin
// In feature screen
onBackClick = {
    navigator.goBack()
}
```

**Safety**: Navigator ensures at least one destination remains (start destination)

### Conditional Navigation

```kotlin
onSaveClick = {
    viewModelScope.launch {
        repository.save().fold(
            ifLeft = { /* show error */ },
            ifRight = { 
                navigator.goBack()  // Navigate on success
            }
        )
    }
}
```

## Module Dependencies

### :core:navigation Dependencies

```kotlin
plugins {
    id("convention.kmp.library")
    id("convention.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            api(libs.androidx.navigation3.ui)  // Exposes Navigation 3 types
        }
    }
}
```

### Feature :wiring Dependencies

```kotlin
plugins {
    id("convention.feature.wiring")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.navigation)
            implementation(projects.features.pokemonlist.api)
            implementation(projects.features.pokemonlist.data)
            implementation(projects.features.pokemonlist.presentation)
        }
        
        androidMain.dependencies {
            implementation(projects.features.pokemonlist.ui)
        }
        
        jvmMain.dependencies {
            implementation(projects.features.pokemonlist.ui)
        }
    }
}
```

**Key points**:
- :api, :data, :presentation in commonMain (all platforms)
- :ui only in androidMain/jvmMain (Compose-specific)
- :core:navigation in commonMain (Navigator type needed everywhere)

## iOS Considerations

### What's Exported

Via `:shared` umbrella module:
- ✅ Route objects from :api modules (for reference in shared code)
- ✅ ViewModels from :presentation modules
- ✅ Repositories from :data modules

### What's NOT Exported

- ❌ :core:navigation module (Compose-specific)
- ❌ :ui modules (Compose screens)
- ❌ :wiring modules (Compose navigation registration)
- ❌ EntryProviderInstaller (Compose-specific type)

**iOS navigation**: SwiftUI app implements own navigation, calls shared ViewModels

## Testing Strategy

### Navigator Tests

```kotlin
// core/navigation/src/androidUnitTest/kotlin/.../NavigatorTest.kt
class NavigatorTest : StringSpec({
    
    "goTo adds destination to back stack" {
        val navigator = Navigator(startDestination = "start")
        
        navigator.goTo("screen1")
        
        navigator.backStack shouldContainExactly listOf("start", "screen1")
    }
    
    "goBack removes last destination" {
        val navigator = Navigator(startDestination = "start")
        navigator.goTo("screen1")
        
        navigator.goBack()
        
        navigator.backStack shouldContainExactly listOf("start")
    }
    
    "goBack preserves start destination" {
        val navigator = Navigator(startDestination = "start")
        
        navigator.goBack()  // Should not remove start
        
        navigator.backStack shouldContainExactly listOf("start")
    }
})
```

### Navigation Integration Tests

```kotlin
// features/pokemonlist/ui/src/androidUnitTest/kotlin/.../PokemonListNavigationTest.kt
@RunWith(AndroidJUnit4::class)
class PokemonListNavigationTest {
    
    @Test
    fun clicking_pokemon_navigates_to_detail() {
        val navigator = Navigator(startDestination = PokemonList)
        val mockViewModel = mockk<PokemonListViewModel>(relaxed = true)
        
        val composeRule = createComposeRule()
        composeRule.setContent {
            PokemonListScreen(
                viewModel = mockViewModel,
                onPokemonClick = { navigator.goTo(PokemonDetail(it.id)) }
            )
        }
        
        composeRule.onNodeWithText("Pikachu").performClick()
        
        navigator.backStack.last() shouldBe PokemonDetail(25)
    }
}
```

## Troubleshooting

### EntryProviderInstaller Not Found

**Symptom**: Empty screen, no routes registered

**Causes**:
1. Missing `@IntoSet` on provider function
2. Missing platform-specific source set (androidMain/jvmMain)
3. Wiring module not included in :core:di dependencies

**Fix**:
```kotlin
// Verify in Koin modules
println(getKoin().getAll<Set<EntryProviderInstaller>>().flatten().size)  // Should be > 0

// Verify in wiring module
val featureNavigationModule = module {
    single<Set<EntryProviderInstaller>>(named("featureName")) {
        setOf(
            { /* EntryProviderInstaller lambda */ }
        )
    }
}
```

### Navigator Not Navigating

**Symptom**: Navigator.goTo() called but screen doesn't change

**Causes**:
1. Back stack not observed by NavDisplay
2. Route not registered in any EntryProviderInstaller
3. Wrong route type passed to goTo()

**Fix**:
```kotlin
// Verify NavDisplay observes backStack
NavDisplay(
    backStack = graph.navigator.backStack,  // ← Must be from navigator
    ...
)

// Verify route registered
entry<PokemonDetail> { ... }  // ← Route type must match exactly
navigator.goTo(PokemonDetail(1))  // ← Type must match entry<T>
```

### iOS Build Errors

**Symptom**: iOS framework export fails with navigation types

**Cause**: Trying to export Compose-specific modules (:core:navigation, :ui, :wiring)

**Fix**:
```kotlin
// shared/build.gradle.kts - DO NOT export these
export(projects.core.navigation)  // ❌ Compose-only
export(projects.features.pokemonlist.ui)  // ❌ Compose-only
export(projects.features.pokemonlist.wiring)  // ❌ Compose-only

// Only export these
export(projects.features.pokemonlist.api)  // ✅ Route objects
export(projects.features.pokemonlist.presentation)  // ✅ ViewModels
```

## Examples from Codebase

### Simple Navigation (List to Detail)

```kotlin
// Route objects
object PokemonList  // No parameters
data class PokemonDetail(val id: Int)  // With parameter

// Wiring (androidMain)
val pokemonListNavigationModule = module {
    single<Set<EntryProviderInstaller>>(named("pokemonListNavigationInstallers")) {
        setOf(
            {
                entry<PokemonList> {
                    val navigator: Navigator = koinInject()
                    val viewModel: PokemonListViewModel = koinInject()
                    PokemonListScreen(
                        viewModel = viewModel,
                        onPokemonClick = { pokemon ->
                            navigator.goTo(PokemonDetail(pokemon.id))
                        }
                    )
                }
            }
        )
    }
}

val pokemonDetailNavigationModule = module {
    single<Set<EntryProviderInstaller>>(named("pokemonDetailNavigationInstallers")) {
        setOf(
            {
                entry<PokemonDetail> { key ->
                    val navigator: Navigator = koinInject()
                    PokemonDetailScreen(
                        pokemonId = key.id,
                        onBackClick = { navigator.goBack() }
                    )
                }
            }
        )
    }
}
```

## Navigation Animations with Navigation 3

### Overview

Navigation 3 supports custom animations through **metadata parameters** on `NavDisplay`. The `entry<T>()` function does NOT accept direct transition parameters—instead, use `NavDisplay.transitionSpec` and `NavDisplay.popTransitionSpec` helper functions to provide metadata-based animations.

### Metadata-Based Animation Pattern

**Key Point**: Navigation 3 animations work through metadata, not direct parameters on `entry<T>()`.

**Official Documentation**: https://developer.android.com/guide/navigation/navigation-3/animate-destinations

### NavDisplay.transitionSpec Helper

Provides metadata for enter animations (forward navigation):

```kotlin
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.fadeIn
import androidx.navigation3.ui.NavDisplay

NavDisplay(
    backStack = navigator.backStack,
    onBack = { navigator.goBack() },
    entryProvider = entryProvider {
        entryProviderInstallers.forEach { this.it() }
    },
    metadata = NavDisplay.transitionSpec(
        slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }) +
        fadeIn(animationSpec = tween(durationMillis = 300))
    )
)
```

**Properties**:
- `slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth })` - Slides from right
- `fadeIn(animationSpec = tween(durationMillis = 300))` - Fades in over 300ms
- `+` operator combines animations (using `togetherWith` internally)

### NavDisplay.popTransitionSpec Helper

Provides metadata for exit animations (back navigation):

```kotlin
NavDisplay(
    backStack = navigator.backStack,
    onBack = { navigator.goBack() },
    entryProvider = entryProvider {
        entryProviderInstallers.forEach { this.it() }
    },
    metadata = NavDisplay.popTransitionSpec(
        slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }) +
        fadeOut(animationSpec = tween(durationMillis = 300))
    )
)
```

**Properties**:
- `slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth })` - Slides to right
- `fadeOut(animationSpec = tween(durationMillis = 300))` - Fades out over 300ms

### Complete Animation Example

From `features/pokemondetail/wiring/src/androidMain`:

```kotlin
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation3.ui.NavDisplay
import com.minddistrict.multiplatformpoc.core.navigation.EntryProviderInstaller
import com.minddistrict.multiplatformpoc.features.pokemondetail.api.navigation.PokemonDetail
import com.minddistrict.multiplatformpoc.features.pokemondetail.ui.PokemonDetailScreen
import org.koin.compose.koinInject
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.parameter.parametersOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual fun Module.providePlatformNavigationProviders() {
    factoryOf(::pokemonDetailNavigationProvider) bind EntryProviderInstaller::class
}

private fun pokemonDetailNavigationProvider(
    navigator: Navigator
): EntryProviderInstaller = {
    entry<PokemonDetail>(
        metadata = NavDisplay.transitionSpec(
            slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }) +
            fadeIn(animationSpec = tween(durationMillis = 300))
        ) + NavDisplay.popTransitionSpec(
            slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }) +
            fadeOut(animationSpec = tween(durationMillis = 300))
        )
    ) { key ->
        PokemonDetailScreen(
            viewModel = koinInject { parametersOf(key.id) },
            onBack = { navigator.goBack() }
        )
    }
}
```

**Key Patterns**:
1. ✅ `metadata = NavDisplay.transitionSpec(...)` for enter animations
2. ✅ `+ NavDisplay.popTransitionSpec(...)` for exit animations
3. ✅ Combine metadata with `+` operator
4. ✅ Use `tween(durationMillis = 300)` for animation timing
5. ✅ `slideInHorizontally + fadeIn` creates smooth combined animations
6. ✅ `initialOffsetX = { fullWidth -> fullWidth }` slides from right edge
7. ✅ `targetOffsetX = { fullWidth -> fullWidth }` slides to right edge

### Common Animation Combinations

#### Slide + Fade (Standard Material)
```kotlin
entry<MyRoute>(
    metadata = NavDisplay.transitionSpec(
        slideInHorizontally(initialOffsetX = { it }) + fadeIn(tween(300))
    ) + NavDisplay.popTransitionSpec(
        slideOutHorizontally(targetOffsetX = { it }) + fadeOut(tween(300))
    )
)
```

#### Slide Vertical + Fade
```kotlin
entry<MyRoute>(
    metadata = NavDisplay.transitionSpec(
        slideInVertically(initialOffsetY = { it }) + fadeIn(tween(300))
    ) + NavDisplay.popTransitionSpec(
        slideOutVertically(targetOffsetY = { it }) + fadeOut(tween(300))
    )
)
```

#### Scale + Fade (Modal)
```kotlin
entry<MyRoute>(
    metadata = NavDisplay.transitionSpec(
        scaleIn(initialScale = 0.9f) + fadeIn(tween(300))
    ) + NavDisplay.popTransitionSpec(
        scaleOut(targetScale = 0.9f) + fadeOut(tween(300))
    )
)
```

### Animation Properties

**Duration**: Use `tween(durationMillis = Int)` to control timing
```kotlin
fadeIn(animationSpec = tween(durationMillis = 300))  // Standard
fadeIn(animationSpec = tween(durationMillis = 150))  // Fast
fadeIn(animationSpec = tween(durationMillis = 500))  // Slow
```

**Easing**: Use `tween(easing = Easing)` for custom curves
```kotlin
import androidx.compose.animation.core.FastOutSlowInEasing
fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing))
```

**Offsets**: Use lambdas for dynamic positioning
```kotlin
slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth })  // From right
slideInHorizontally(initialOffsetX = { fullWidth -> -fullWidth }) // From left
slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth / 2 }) // Half screen
```

### Migration Notes

**Old API (DOES NOT WORK)**:
```kotlin
// ❌ This does NOT work in Navigation 3
entry<MyRoute>(
    enterTransition = { slideInHorizontally() }  // No such parameter
)
```

**New API (CORRECT)**:
```kotlin
// ✅ Use metadata parameter with NavDisplay.transitionSpec
entry<MyRoute>(
    metadata = NavDisplay.transitionSpec(slideInHorizontally() + fadeIn())
)
```

### Testing Animations

Animations can be tested in UI tests by checking state transitions:

```kotlin
@Test
fun navigation_animates_correctly() = runComposeUiTest {
    val navigator = Navigator(startDestination = PokemonList)
    
    setContent {
        NavDisplay(
            backStack = navigator.backStack,
            metadata = NavDisplay.transitionSpec(slideInHorizontally() + fadeIn())
        )
    }
    
    navigator.goTo(PokemonDetail(1))
    
    // Wait for animation to complete
    waitForIdle()
    onNodeWithText("Bulbasaur").assertIsDisplayed()
}
```

### Platform Differences

**Android & Desktop**: Full animation support via Compose Multiplatform

**iOS**: Not applicable (iOS uses native SwiftUI navigation with its own animation system)

---

## Best Practices

1. **Route objects are keys**: Keep them simple, no business logic
2. **Platform-specific UI**: androidMain/jvmMain for EntryProviderInstallers
3. **Navigator is singleton**: One instance per app, injected everywhere
4. **Koin named qualifiers for collection**: Use Koin named qualifiers to collect all navigation entries from feature modules
5. **No iOS exports**: Navigation is Compose-specific, iOS uses SwiftUI
6. **Test navigation**: Use Kotest for Navigator logic, Compose Test for UI navigation
7. **Explicit back stack**: Navigator.backStack is observable state, debug-friendly
8. **Animations via metadata**: Use `NavDisplay.transitionSpec` and `NavDisplay.popTransitionSpec` helpers
9. **Combine animations**: Use `+` operator to combine enter/exit animations
10. **Standard duration**: 300ms for most animations (Material Design guideline)
}
```

Result passing example (pop with result)
```kotlin
Button(onClick = {
  resultStore.setResult("updated", resultKey = "profile_action")
  backStack.removeLastOrNull()
}) { Text("Done") }
```

## Deep Links
- Define deep link parsing and mapping to feature routes in a single place; forward parameters to feature screens via the contract methods.
- Keep parsing logic independent of UI for testability.

## Testing
- Unit-test contract behavior (route building, parameter encoding/decoding) with Kotest, using property-based testing for round-trip invariants when applicable.
- UI navigation tests (Android) live under `:features:<feature>:presentation/src/commonTest/screentest` when implemented.
 - ViewModels used by destinations must extend `androidx.lifecycle.ViewModel` and can use `viewModelScope`; prefer constructing them via your DI/wiring or using factory methods aligned with Navigation 3 patterns.

## Notes
- Keep navigation state ephemeral and derived from the backstack where possible; avoid duplicating route state in view models.
- Align destination availability and restrictions with PRD and flows in `.junie/guides/project/user_flow.md`.
