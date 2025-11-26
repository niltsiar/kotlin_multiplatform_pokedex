# Navigation Patterns (Navigation 3)

Last Updated: November 26, 2025

> **Canonical Reference**: See [Navigation 3 Pattern](../tech/critical_patterns_quick_ref.md#navigation-3-pattern) for core rules.

> Comprehensive code examples for Navigation 3 route objects, EntryProviderInstaller, metadata-based animations, and NavDisplay integration.

## Core Principle

**Route objects in `:api`, UI in `:ui`, wiring in platform-specific source sets (androidMain/jvmMain)**

## Route Objects

### Simple Routes

```kotlin
// :features:pokemonlist:api/PokemonList.kt
package com.example.features.pokemonlist.api

// Plain Kotlin object - no @Serializable needed
object PokemonList
```

### Parameterized Routes

```kotlin
// :features:pokemondetail:api/PokemonDetail.kt
package com.example.features.pokemondetail.api

// Data class with parameters - no @Serializable needed
data class PokemonDetail(val id: Int)

// :features:profile:api/ProfileRoute.kt
data class Profile(
    val userId: String,
    val tab: ProfileTab = ProfileTab.OVERVIEW
)

enum class ProfileTab {
    OVERVIEW, ACTIVITY, SETTINGS
}
```

## Navigator Class

```kotlin
// :core:navigation/Navigator.kt
package com.example.core.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class Navigator(startDestination: Any) {
    private val _backStack: SnapshotStateList<Any> = 
        mutableStateListOf(startDestination)
    
    val backStack: List<Any> = _backStack
    
    fun goTo(destination: Any) {
        _backStack.add(destination)
    }
    
    fun goBack() {
        if (_backStack.size > 1) {
            _backStack.removeAt(_backStack.lastIndex)
        }
    }
    
    fun popUpTo(destination: Any, inclusive: Boolean = false) {
        val index = _backStack.indexOfLast { it == destination }
        if (index != -1) {
            val targetIndex = if (inclusive) index else index + 1
            _backStack.removeRange(targetIndex, _backStack.size)
        }
    }
    
    fun replace(destination: Any) {
        if (_backStack.isNotEmpty()) {
            _backStack.removeAt(_backStack.lastIndex)
        }
        _backStack.add(destination)
    }
}
```

## EntryProviderInstaller Pattern

```kotlin
// :core:navigation/EntryProviderInstaller.kt
package com.example.core.navigation

import org.jetbrains.androidx.navigation.EntryProviderScope

// Typealias for navigation DSL
typealias EntryProviderInstaller = EntryProviderScope<Any>.() -> Unit
```

## Basic Navigation Wiring

### Simple Feature Navigation

```kotlin
// :features:pokemonlist:wiring/androidMain/PokemonListNavigationModule.kt
package com.example.features.pokemonlist.wiring

import com.example.core.navigation.EntryProviderInstaller
import com.example.core.navigation.Navigator
import com.example.features.pokemonlist.api.PokemonList
import com.example.features.pokemonlist.presentation.PokemonListViewModel
import com.example.features.pokemonlist.ui.PokemonListScreen
import com.example.features.pokemondetail.api.PokemonDetail
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val pokemonListNavigationModule = module {
    single<Set<EntryProviderInstaller>> {
        setOf(
            {
                entry<PokemonList> {
                    val navigator = koinInject<Navigator>()
                    val viewModel = koinInject<PokemonListViewModel> {
                        parametersOf(this@entry.lifecycle.coroutineScope)
                    }
                    
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
```

### Parameterized Navigation

```kotlin
// :features:pokemondetail:wiring/androidMain/PokemonDetailNavigationModule.kt
package com.example.features.pokemondetail.wiring

import com.example.core.navigation.EntryProviderInstaller
import com.example.core.navigation.Navigator
import com.example.features.pokemondetail.api.PokemonDetail
import com.example.features.pokemondetail.presentation.PokemonDetailViewModel
import com.example.features.pokemondetail.ui.PokemonDetailScreen
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val pokemonDetailNavigationModule = module {
    single<Set<EntryProviderInstaller>> {
        setOf(
            { 
                entry<PokemonDetail> { key ->
                    val navigator = koinInject<Navigator>()
                    val viewModel = koinInject<PokemonDetailViewModel> {
                        // Pass pokemon ID to ViewModel
                        parametersOf(key.id, this@entry.lifecycle.coroutineScope)
                    }
                    
                    PokemonDetailScreen(
                        pokemonId = key.id,
                        viewModel = viewModel,
                        onBack = { navigator.goBack() }
                    )
                }
            }
        )
    }
}
```

## Navigation 3 Animations (Metadata-Based)

### Basic Animations

```kotlin
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import org.jetbrains.androidx.navigation.NavDisplay

// :features:pokemondetail:wiring/androidMain/PokemonDetailNavigationProvider.kt
internal fun pokemonDetailNavigationProvider(
    navigator: Navigator
): EntryProviderInstaller = {
    entry<PokemonDetail>(
        // Enter animation
        metadata = NavDisplay.transitionSpec(
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300)
            ) + fadeIn(
                animationSpec = tween(durationMillis = 300)
            )
        ) + 
        // Exit animation (pop)
        NavDisplay.popTransitionSpec(
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300)
            ) + fadeOut(
                animationSpec = tween(durationMillis = 300)
            )
        )
    ) { key ->
        PokemonDetailScreen(
            pokemonId = key.id,
            viewModel = koinInject { parametersOf(key.id) },
            onBack = { navigator.goBack() }
        )
    }
}
```

### Custom Animation Patterns

```kotlin
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring

// Slide from bottom with spring
entry<AddItemRoute>(
    metadata = NavDisplay.transitionSpec(
        slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + fadeIn()
    ) + NavDisplay.popTransitionSpec(
        slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeOut()
    )
) { /* content */ }

// Fade only (modal-like)
entry<DialogRoute>(
    metadata = NavDisplay.transitionSpec(
        fadeIn(animationSpec = tween(150))
    ) + NavDisplay.popTransitionSpec(
        fadeOut(animationSpec = tween(150))
    )
) { /* content */ }

// Scale from center
entry<DetailRoute>(
    metadata = NavDisplay.transitionSpec(
        scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300))
    ) + NavDisplay.popTransitionSpec(
        scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300))
    )
) { /* content */ }
```

### Animation Constants

```kotlin
// :core:navigation/NavigationConstants.kt
package com.example.core.navigation

import androidx.compose.animation.core.tween

object NavigationConstants {
    const val ANIMATION_DURATION_MS = 300
    
    val defaultTweenSpec = tween<Float>(durationMillis = ANIMATION_DURATION_MS)
    val fastTweenSpec = tween<Float>(durationMillis = 150)
    val slowTweenSpec = tween<Float>(durationMillis = 500)
}
```

## NavDisplay Integration

### App-Level Setup

```kotlin
// :composeApp/commonMain/App.kt
package com.example

import androidx.compose.runtime.Composable
import com.example.core.navigation.EntryProviderInstaller
import com.example.core.navigation.Navigator
import com.example.features.pokemonlist.api.PokemonList
import org.jetbrains.androidx.navigation.NavDisplay
import org.jetbrains.androidx.navigation.entryProvider
import org.koin.compose.koinInject

@Composable
fun App() {
    val navigator = koinInject<Navigator>()
    val entryProviderInstallers = koinInject<List<Set<EntryProviderInstaller>>>()
    
    NavDisplay(
        backStack = navigator.backStack,
        onBack = { navigator.goBack() },
        entryProvider = entryProvider {
            // Install all feature navigation entries
            entryProviderInstallers.flatten().forEach { installer ->
                this.installer()
            }
        }
    )
}

// Koin module for Navigator and entry collection
val navigationModule = module {
    single<Navigator> { Navigator(startDestination = PokemonList) }
    
    // Collect all EntryProviderInstaller sets from features
    single<List<Set<EntryProviderInstaller>>> {
        getAll<Set<EntryProviderInstaller>>()
    }
}
```

## Deep Links & External Navigation

### Deep Link Handling

```kotlin
// :core:navigation/DeepLinkHandler.kt
package com.example.core.navigation

class DeepLinkHandler(private val navigator: Navigator) {
    fun handle(deepLink: String): Boolean {
        return when {
            deepLink.startsWith("app://pokemon/") -> {
                val id = deepLink.removePrefix("app://pokemon/").toIntOrNull()
                if (id != null) {
                    navigator.goTo(PokemonDetail(id))
                    true
                } else false
            }
            
            deepLink == "app://profile" -> {
                navigator.goTo(Profile)
                true
            }
            
            else -> false
        }
    }
}

// Usage in App
@Composable
fun App() {
    val navigator = koinInject<Navigator>()
    val deepLinkHandler = remember { DeepLinkHandler(navigator) }
    
    // Handle deep links from intent/URL
    LaunchedEffect(deepLinkUri) {
        deepLinkUri?.let { uri ->
            deepLinkHandler.handle(uri.toString())
        }
    }
    
    NavDisplay(/* ... */)
}
```

## Testing Navigation

### Navigator Testing

```kotlin
class NavigatorTest : StringSpec({
    lateinit var navigator: Navigator
    
    beforeTest {
        navigator = Navigator(startDestination = PokemonList)
    }
    
    "should start with initial destination" {
        navigator.backStack shouldHaveSize 1
        navigator.backStack.first() shouldBe PokemonList
    }
    
    "should navigate to new destination" {
        navigator.goTo(PokemonDetail(1))
        
        navigator.backStack shouldHaveSize 2
        navigator.backStack.last() shouldBe PokemonDetail(1)
    }
    
    "should go back" {
        navigator.goTo(PokemonDetail(1))
        navigator.goTo(PokemonDetail(2))
        
        navigator.goBack()
        
        navigator.backStack shouldHaveSize 2
        navigator.backStack.last() shouldBe PokemonDetail(1)
    }
    
    "should not go back when at root" {
        navigator.goBack()
        
        navigator.backStack shouldHaveSize 1
        navigator.backStack.first() shouldBe PokemonList
    }
    
    "should pop up to destination" {
        navigator.goTo(PokemonDetail(1))
        navigator.goTo(Profile)
        navigator.goTo(Settings)
        
        navigator.popUpTo(PokemonDetail(1))
        
        navigator.backStack shouldHaveSize 2
        navigator.backStack.last() shouldBe PokemonDetail(1)
    }
    
    "should replace current destination" {
        navigator.goTo(PokemonDetail(1))
        
        navigator.replace(PokemonDetail(2))
        
        navigator.backStack shouldHaveSize 2
        navigator.backStack.last() shouldBe PokemonDetail(2)
        navigator.backStack.first() shouldBe PokemonList
    }
})
```

## Anti-Patterns to Avoid

### ❌ DON'T: Use @Serializable on Route Objects

```kotlin
// ❌ WRONG - Not needed for Navigation 3
@Serializable
object PokemonList

// ✅ CORRECT - Plain Kotlin object
object PokemonList
```

### ❌ DON'T: Pass Direct Transition Parameters

```kotlin
// ❌ WRONG - entry<T>() does not accept transition parameters
entry<PokemonDetail>(
    enterTransition = slideInHorizontally(),  // DOESN'T COMPILE
    exitTransition = slideOutHorizontally()   // DOESN'T COMPILE
) { /* ... */ }

// ✅ CORRECT - Use metadata with NavDisplay.transitionSpec
entry<PokemonDetail>(
    metadata = NavDisplay.transitionSpec(
        slideInHorizontally() + fadeIn()
    ) + NavDisplay.popTransitionSpec(
        slideOutHorizontally() + fadeOut()
    )
) { /* ... */ }
```

### ❌ DON'T: Export Navigation Modules to iOS

```kotlin
// ❌ WRONG - iOS doesn't use Compose navigation
// :shared/build.gradle.kts
kotlin {
    iosTarget.binaries.framework {
        export(projects.core.navigation)  // WRONG
        export(projects.features.pokemonlist.wiring)  // WRONG
    }
}

// ✅ CORRECT - Only export api and presentation
kotlin {
    iosTarget.binaries.framework {
        export(projects.features.pokemonlist.api)
        export(projects.features.pokemonlist.presentation)
    }
}
```

### ❌ DON'T: Store Navigator in ViewModel

```kotlin
// ❌ WRONG - Navigator should be passed as event/callback
class MyViewModel(
    private val navigator: Navigator  // WRONG
) : ViewModel() {
    fun onItemClick() {
        navigator.goTo(Detail)
    }
}

// ✅ CORRECT - Navigation via callback
class MyViewModel(
    private val repo: Repository
) : ViewModel() {
    // Expose events, let UI handle navigation
}

@Composable
fun MyScreen(viewModel: MyViewModel, onNavigate: (Any) -> Unit) {
    val navigator = koinInject<Navigator>()
    
    ItemCard(onClick = {
        onNavigate(Detail)
        // or navigator.goTo(Detail)
    })
}
```

## See Also

- `.junie/guides/tech/navigation.md` — Complete navigation guide
- Official docs: https://developer.android.com/guide/navigation/navigation-3/animate-destinations
- `patterns/di_patterns.md` — Navigation DI patterns
- `patterns/viewmodel_patterns.md` — ViewModel with navigation
