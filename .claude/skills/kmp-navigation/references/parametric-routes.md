# Parametric Routes

Type-safe navigation with parameters, ViewModel keying, and lifecycle-aware DI wiring.

## Route Definition

### Parameterized Route Object

```kotlin
// :features:pokemondetail:api/src/commonMain/.../navigation/PokemonDetailEntry.kt
package com.example.features.pokemondetail.api

// Data class with parameters - no @Serializable needed
data class PokemonDetail(
    val id: Int
)
```

**Characteristics**:
- Plain Kotlin `data class`
- Parameters as constructor properties
- No `@Serializable` needed (Navigation 3 uses routes as in-memory keys)
- Exported to iOS via `:shared` (for reference in shared code)

### Multiple Parameters

```kotlin
// :features:profile:api/ProfileRoute.kt
data class Profile(
    val userId: String,
    val tab: ProfileTab = ProfileTab.OVERVIEW
)

enum class ProfileTab {
    OVERVIEW, ACTIVITY, SETTINGS
}
```

**Default parameters**: Supported and recommended for optional navigation state.

### Complex Types

```kotlin
// :features:search:api/SearchRoute.kt
data class Search(
    val query: String,
    val filters: Set<FilterType> = emptySet(),
    val sortBy: SortOption = SortOption.RELEVANCE
)

enum class FilterType {
    TYPE, REGION, GENERATION
}

enum class SortOption {
    RELEVANCE, NAME, NUMBER
}
```

## Navigation Wiring

### Basic Parametric Navigation

```kotlin
// :features:pokemondetail:wiring-ui/src/commonMain/.../PokemonDetailNavigationProviders.kt
package com.example.features.pokemondetail.wiringui

import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.core.navigation.EntryProviderInstaller
import com.example.core.navigation.Navigator
import com.example.features.pokemondetail.api.PokemonDetail
import com.example.features.pokemondetail.presentation.PokemonDetailViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

const val PokemonDetailNavigationInstallersQualifier = "pokemonDetailNavigationInstallers"

val pokemonDetailNavigationModule = module {
    single<Set<EntryProviderInstaller>>(named(PokemonDetailNavigationInstallersQualifier)) {
        setOf({
            entry<PokemonDetail> { route ->
                val navigator: Navigator = koinInject()

                // CRITICAL: Key ViewModel by route parameter to ensure new instance per ID
                // Without key, Navigation 3 will reuse same ViewModel for all PokemonDetail routes
                val viewModel: PokemonDetailViewModel = koinViewModel(
                    key = "pokemon_detail_${route.id}",  // ← Essential for parametric routes
                    parameters = { parametersOf(route.id) }
                )

                val lifecycleOwner = LocalLifecycleOwner.current

                // Register ViewModel with lifecycle (implements DefaultLifecycleObserver)
                // Key by route.id to properly dispose when navigating to different Pokemon
                DisposableEffect(route.id) {
                    lifecycleOwner.lifecycle.addObserver(viewModel)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(viewModel)
                    }
                }

                PokemonDetailScreen(
                    viewModel = viewModel,
                    onBackClick = { navigator.goBack() }
                )
            }
        })
    }
}
```

### ViewModel Constructor

```kotlin
// :features:pokemondetail:presentation/src/commonMain/.../PokemonDetailViewModel.kt
package com.example.features.pokemondetail.presentation

import androidx.lifecycle.ViewModel
import com.example.features.pokemondetail.domain.PokemonDetailRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class PokemonDetailViewModel(
    private val pokemonId: Int,  // Parameter passed via Koin
    private val repository: PokemonDetailRepository,
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob())
) : ViewModel(viewModelScope) {

    init {
        loadPokemon()
    }

    private fun loadPokemon() {
        viewModelScope.launch {
            repository.getPokemonDetail(pokemonId).fold(
                ifLeft = { /* handle error */ },
                ifRight = { _uiState.value = PokemonDetailUiState.Content(it) }
            )
        }
    }
}
```

### Koin DI Wiring

```kotlin
// :features:pokemondetail:wiring/src/commonMain/.../PokemonDetailModule.kt
package com.example.features.pokemondetail.wiring

import com.example.features.pokemondetail.domain.PokemonDetailRepository
import com.example.features.pokemondetail.presentation.PokemonDetailViewModel
import org.koin.dsl.module

val pokemonDetailModule = module {
    // Provide repository
    factory<PokemonDetailRepository> {
        PokemonDetailRepository(api = get())
    }

    // Provide ViewModel with parameters
    viewModel { params ->
        PokemonDetailViewModel(
            pokemonId = params.get(),  // Parameter from navigation
            repository = get()
        )
    }
}
```

## ViewModel Keying (Critical)

### Problem: Without Key

```kotlin
// ❌ WRONG - Reuses same ViewModel for all instances
entry<PokemonDetail> { route ->
    val viewModel: PokemonDetailViewModel = koinViewModel(
        parameters = { parametersOf(route.id) }  // Missing key!
    )

    PokemonDetailScreen(viewModel = viewModel)
}
```

**Result**: When navigating from Pokemon 1 to Pokemon 2, Navigation 3 reuses the same ViewModel instance, showing stale data.

### Solution: With Key

```kotlin
// ✅ CORRECT - Creates new ViewModel instance per route parameter
entry<PokemonDetail> { route ->
    val viewModel: PokemonDetailViewModel = koinViewModel(
        key = "pokemon_detail_${route.id}",  // ← Unique key per route
        parameters = { parametersOf(route.id) }
    )

    PokemonDetailScreen(viewModel = viewModel)
}
```

**Result**: Each Pokemon ID gets its own ViewModel instance with fresh data.

### Key Patterns

```kotlin
// Simple parameter key
key = "pokemon_detail_${route.id}"

// Multiple parameters key
key = "profile_${route.userId}_${route.tab}"

// Complex object key (use unique identifier)
key = "search_${route.query.hashCode()}_${route.filters.hashCode()}"

// Enum key
key = "profile_tab_${route.tab.name}"
```

## Navigation Actions

### From List to Detail

```kotlin
// In PokemonListScreen
PokemonListScreen(
    viewModel = viewModel,
    onPokemonClick = { pokemon ->
        navigator.goTo(PokemonDetail(pokemon.id))  // Pass parameter
    }
)
```

### Multiple Parameters

```kotlin
// From search results
SearchResultsScreen(
    onProfileClick = { userId, tab ->
        navigator.goTo(Profile(userId, tab))
    }
)
```

### Default Parameters

```kotlin
// Navigate with default tab
navigator.goTo(Profile(userId = "123"))  // Uses ProfileTab.OVERVIEW

// Navigate with specific tab
navigator.goTo(Profile(userId = "123", tab = ProfileTab.SETTINGS))
```

## Deep Links

### Deep Link Parsing

```kotlin
// :core:navigation/DeepLinkHandler.kt
package com.example.core.navigation

class DeepLinkHandler(private val navigator: Navigator) {
    fun handle(deepLink: String): Boolean {
        return when {
            // app://pokemon/25
            deepLink.startsWith("app://pokemon/") -> {
                val id = deepLink.removePrefix("app://pokemon/").toIntOrNull()
                if (id != null) {
                    navigator.goTo(PokemonDetail(id))
                    true
                } else false
            }

            // app://profile/123/settings
            deepLink.startsWith("app://profile/") -> {
                val parts = deepLink.removePrefix("app://profile/").split("/")
                if (parts.size >= 2) {
                    val userId = parts[0]
                    val tab = when (parts.getOrNull(1)) {
                        "settings" -> ProfileTab.SETTINGS
                        "activity" -> ProfileTab.ACTIVITY
                        else -> ProfileTab.OVERVIEW
                    }
                    navigator.goTo(Profile(userId, tab))
                    true
                } else false
            }

            else -> false
        }
    }
}
```

### Usage in App

```kotlin
@Composable
fun App() {
    val navigator = koinInject<Navigator>()
    val deepLinkHandler = remember { DeepLinkHandler(navigator) }
    val deepLinkUri = remember { System.getenv("DEEP_LINK_URI") }

    // Handle deep links on startup
    LaunchedEffect(deepLinkUri) {
        deepLinkUri?.let { uri ->
            deepLinkHandler.handle(uri.toString())
        }
    }

    NavDisplay(/* ... */)
}
```

## Testing

### Navigator Tests with Parameters

```kotlin
class NavigatorTest : StringSpec({
    lateinit var navigator: Navigator

    beforeTest {
        navigator = Navigator(startDestination = PokemonList)
    }

    "should navigate to parametric route" {
        navigator.goTo(PokemonDetail(25))

        navigator.backStack shouldHaveSize 2
        navigator.backStack.last() shouldBe PokemonDetail(25)
    }

    "should navigate to different parametric routes" {
        navigator.goTo(PokemonDetail(1))
        navigator.goTo(PokemonDetail(2))
        navigator.goTo(PokemonDetail(3))

        navigator.backStack shouldHaveSize 4
        navigator.backStack.last() shouldBe PokemonDetail(3)
    }

    "should go back from parametric route" {
        navigator.goTo(PokemonDetail(25))
        navigator.goBack()

        navigator.backStack shouldHaveSize 1
        navigator.backStack.first() shouldBe PokemonList
    }

    "should pop up to specific parametric route" {
        navigator.goTo(PokemonDetail(1))
        navigator.goTo(PokemonDetail(2))
        navigator.goTo(PokemonDetail(3))

        navigator.popUpTo(PokemonDetail(1))

        navigator.backStack shouldHaveSize 2
        navigator.backStack.last() shouldBe PokemonDetail(1)
    }
})
```

### Navigation Integration Tests

```kotlin
@RunWith(AndroidJUnit4::class)
class PokemonDetailNavigationTest {

    @Test
    fun clicking_pokemon_navigates_to_detail_with_correct_id() {
        val navigator = Navigator(startDestination = PokemonList)

        composeRule.setContent {
            PokemonListScreen(
                viewModel = viewModel,
                onPokemonClick = { pokemon ->
                    navigator.goTo(PokemonDetail(pokemon.id))
                }
            )
        }

        composeRule.onNodeWithText("Pikachu").performClick()

        navigator.backStack.last() shouldBe PokemonDetail(25)
    }
}
```

## Anti-Patterns to Avoid

### ❌ DON'T: Use @Serializable on Routes

```kotlin
// ❌ WRONG - Not needed for Navigation 3
@Serializable
data class PokemonDetail(val id: Int)

// ✅ CORRECT - Plain Kotlin data class
data class PokemonDetail(val id: Int)
```

### ❌ DON'T: Skip ViewModel Key

```kotlin
// ❌ WRONG - Reuses ViewModel for all instances
entry<PokemonDetail> { route ->
    val viewModel = koinViewModel(parameters = { parametersOf(route.id) })
    PokemonDetailScreen(viewModel = viewModel)
}

// ✅ CORRECT - Unique ViewModel per route parameter
entry<PokemonDetail> { route ->
    val viewModel = koinViewModel(
        key = "pokemon_detail_${route.id}",
        parameters = { parametersOf(route.id) }
    )
    PokemonDetailScreen(viewModel = viewModel)
}
```

### ❌ DON'T: Store Route in ViewModel

```kotlin
// ❌ WRONG - ViewModel should not know about navigation
class PokemonDetailViewModel(
    private val route: PokemonDetail  // WRONG
) : ViewModel() {
    fun onBack() {
        // viewModel.navigator.goBack()  // WRONG
    }
}

// ✅ CORRECT - Navigation handled in UI layer
class PokemonDetailViewModel(
    private val pokemonId: Int
) : ViewModel()

PokemonDetailScreen(
    viewModel = viewModel,
    onBackClick = { navigator.goBack() }  // Navigation callback
)
```

## Reference Implementations

- `features/pokemondetail/api/PokemonDetail.kt` — Route object definition
- `features/pokemondetail/wiring-ui/PokemonDetailNavigationProviders.kt` — Parametric wiring
- `features/pokemondetail/presentation/PokemonDetailViewModel.kt` — Parameterized ViewModel
- `core/navigation/src/androidUnitTest/kotlin/NavigatorTest.kt` — Navigator tests
