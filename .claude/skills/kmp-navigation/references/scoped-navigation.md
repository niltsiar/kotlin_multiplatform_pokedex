# Scoped Navigation

Scoped navigation patterns for feature modules with Koin DI collection and dual-theme support.

## Feature Module Structure

### Standard 8-Module Layout

```
:features:pokemonlist/
├── api/                             # Route objects, public contracts
│   └── src/commonMain/kotlin/
│       └── PokemonList.kt            # object PokemonList
├── data/                            # Repositories, API services
├── presentation/                     # ViewModels (shared with iOS)
├── ui-material/                      # Material Design 3 UI
├── ui-unstyled/                      # Compose Unstyled UI
├── wiring/                          # Business logic DI
│   └── src/commonMain/kotlin/
│       └── PokemonListModule.kt      # Koin module
├── wiring-ui-material/               # Material navigation registration
│   └── src/
│       ├── commonMain/kotlin/         # Shared providers
│       ├── androidMain/kotlin/        # Android-specific
│       ├── jvmMain/kotlin/           # Desktop-specific
│       └── iosMain/kotlin/           # iOS Compose-specific
└── wiring-ui-unstyled/               # Unstyled navigation registration
    └── src/
        ├── commonMain/kotlin/         # Shared providers
        ├── androidMain/kotlin/
        ├── jvmMain/kotlin/
        └── iosMain/kotlin/
```

**Key principle**: Each theme has its own wiring-ui module with scoped navigation.

## Dual-Theme Navigation

### Module Structure for Dual Themes

```kotlin
// :features:pokemonlist:wiring-ui-material/build.gradle.kts
plugins {
    id("convention.feature.wiring")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.navigation)
            implementation(projects.core.designsystemMaterial)  // Material theme
            implementation(projects.features.pokemonlist.api)
            implementation(projects.features.pokemonlist.presentation)
            implementation(projects.features.pokemonlist.uiMaterial)
        }
    }
}
```

```kotlin
// :features:pokemonlist:wiring-ui-unstyled/build.gradle.kts
plugins {
    id("convention.feature.wiring")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.navigation)
            implementation(projects.core.designsystemUnstyled)  // Unstyled theme
            implementation(projects.features.pokemonlist.api)
            implementation(projects.features.pokemonlist.presentation)
            implementation(projects.features.pokemonlist.uiUnstyled)
        }
    }
}
```

### Material Navigation Provider

```kotlin
// :features:pokemonlist:wiring-ui-material/src/commonMain/.../PokemonListMaterialNavigationProviders.kt
package com.example.features.pokemonlist.wiringui.material

import com.example.core.designsystem.material.MaterialScope
import com.example.core.navigation.EntryProviderInstaller
import com.example.core.navigation.Navigator
import com.example.features.pokemonlist.api.PokemonList
import com.example.features.pokemonlist.presentation.PokemonListViewModel
import com.example.features.pokemonlist.ui.material.PokemonListMaterialScreen
import org.koin.compose.koinInject
import org.koin.dsl.module

const val PokemonListMaterialNavigationInstallersQualifier = "pokemonListMaterialNavigationInstallers"

val pokemonListMaterialNavigationModule = module {
    single<Set<EntryProviderInstaller>>(named(PokemonListMaterialNavigationInstallersQualifier)) {
        setOf({
            entry<PokemonList> {
                val navigator: Navigator = koinInject()
                val viewModel: PokemonListViewModel = koinInject()

                PokemonListMaterialScreen(  // Material UI
                    viewModel = viewModel,
                    onPokemonClick = { pokemon ->
                        navigator.goTo(PokemonDetail(pokemon.id))
                    },
                )
            }
        })
    }
}
```

### Unstyled Navigation Provider

```kotlin
// :features:pokemonlist:wiring-ui-unstyled/src/commonMain/.../PokemonListUnstyledNavigationProviders.kt
package com.example.features.pokemonlist.wiringui.unstyled

import com.example.core.designsystem.unstyled.UnstyledScope
import com.example.core.navigation.EntryProviderInstaller
import com.example.core.navigation.Navigator
import com.example.features.pokemonlist.api.PokemonList
import com.example.features.pokemonlist.presentation.PokemonListViewModel
import com.example.features.pokemonlist.ui.unstyled.PokemonListUnstyledScreen
import org.koin.compose.koinInject
import org.koin.dsl.module

const val PokemonListUnstyledNavigationInstallersQualifier = "pokemonListUnstyledNavigationInstallers"

val pokemonListUnstyledNavigationModule = module {
    single<Set<EntryProviderInstaller>>(named(PokemonListUnstyledNavigationInstallersQualifier)) {
        setOf({
            entry<PokemonList> {
                val navigator: Navigator = koinInject()
                val viewModel: PokemonListViewModel = koinInject()

                PokemonListUnstyledScreen(  // Unstyled UI
                    viewModel = viewModel,
                    onPokemonClick = { pokemon ->
                        navigator.goTo(PokemonDetail(pokemon.id))
                    },
                )
            }
        })
    }
}
```

## Koin Collection Pattern

### Direct Collection in App.kt

```kotlin
// composeApp/src/commonMain/kotlin/.../App.kt
package com.example

import com.example.core.di.navigationUiModule
import com.example.core.navigation.EntryProviderInstaller
import com.example.core.navigation.Navigator
import com.example.features.pokemonlist.api.PokemonList
import com.example.features.pokemonlist.wiringui.material.PokemonListMaterialNavigationInstallersQualifier
import com.example.features.pokemonlist.wiringui.unstyled.PokemonListUnstyledNavigationInstallersQualifier
import com.example.features.pokemondetail.wiringui.material.PokemonDetailMaterialNavigationInstallersQualifier
import com.example.features.pokemondetail.wiringui.unstyled.PokemonDetailUnstyledNavigationInstallersQualifier
import org.jetbrains.androidx.navigation.NavDisplay
import org.jetbrains.androidx.navigation.entryProvider
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun App() {
    KoinApplication(
        application = {
            modules(
                navigationUiModule +
                pokemonListModule +
                pokemonDetailModule +
                pokemonListMaterialNavigationModule +
                pokemonListUnstyledNavigationModule +
                pokemonDetailMaterialNavigationModule +
                pokemonDetailUnstyledNavigationModule
            )
        }
    ) {
        val navigator: Navigator = koinInject()

        // Collect Material navigation installers
        val pokemonListMaterialInstallers: Set<EntryProviderInstaller> =
            koinInject(qualifier = named(PokemonListMaterialNavigationInstallersQualifier))
        val pokemonDetailMaterialInstallers: Set<EntryProviderInstaller> =
            koinInject(qualifier = named(PokemonDetailMaterialNavigationInstallersQualifier))

        // Collect Unstyled navigation installers
        val pokemonListUnstyledInstallers: Set<EntryProviderInstaller> =
            koinInject(qualifier = named(PokemonListUnstyledNavigationInstallersQualifier))
        val pokemonDetailUnstyledInstallers: Set<EntryProviderInstaller> =
            koinInject(qualifier = named(PokemonDetailUnstyledNavigationInstallersQualifier))

        // Theme selection state (managed by app)
        var selectedTheme by remember { mutableStateOf(Theme.MATERIAL) }

        // Select installers based on theme
        val allInstallers = when (selectedTheme) {
            Theme.MATERIAL ->
                pokemonListMaterialInstallers + pokemonDetailMaterialInstallers
            Theme.UNSTYLED ->
                pokemonListUnstyledInstallers + pokemonDetailUnstyledInstallers
        }

        when (selectedTheme) {
            Theme.MATERIAL -> {
                PokemonMaterialTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        NavDisplay(
                            backStack = navigator.backStack,
                            onBack = { navigator.goBack() },
                            entryProvider = entryProvider {
                                allInstallers.forEach { installer -> installer() }
                            }
                        )
                    }
                }
            }
            Theme.UNSTYLED -> {
                PokemonUnstyledTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        NavDisplay(
                            backStack = navigator.backStack,
                            onBack = { navigator.goBack() },
                            entryProvider = entryProvider {
                                allInstallers.forEach { installer -> installer() }
                            }
                        )
                    }
                }
            }
        }
    }
}

enum class Theme {
    MATERIAL, UNSTYLED
}
```

**Benefits**:
- Explicit collection using public const qualifiers
- Theme switching by selecting installers at runtime
- No aggregation module needed — simpler, more explicit
- Each theme has isolated navigation graph

### Alternative: Auto-Collection

```kotlin
// core/di/src/commonMain/.../AppModules.kt
package com.example.core.di

import org.koin.dsl.module

val navigationUiModule = module {
    single<Navigator> { Navigator(startDestination = PokemonList) }

    // Auto-collect all EntryProviderInstaller sets
    // Requires all navigation modules to be loaded first
    single<List<Set<EntryProviderInstaller>>> {
        getAll<Set<EntryProviderInstaller>>()
    }
}
```

```kotlin
// App.kt
val allInstallers = koinInject<List<Set<EntryProviderInstaller>>>()

NavDisplay(
    backStack = navigator.backStack,
    entryProvider = entryProvider {
        allInstallers.flatten().forEach { installer -> installer() }
    }
)
```

**Note**: Auto-collection simpler but less explicit than direct collection with named qualifiers.

## Scoped Navigation Patterns

### Feature-Local Navigation

```kotlin
// Each feature owns its routes and UI registration
// :features:pokemonlist:wiring-ui-material
val pokemonListMaterialNavigationModule = module {
    single<Set<EntryProviderInstaller>>(named(PokemonListMaterialNavigationInstallersQualifier)) {
        setOf({
            // PokemonList entry (feature's own route)
            entry<PokemonList> {
                PokemonListMaterialScreen(
                    // ... can navigate to other features' routes
                    onPokemonClick = { navigator.goTo(PokemonDetail(it.id)) }
                )
            }
        })
    }
}

// :features:pokemondetail:wiring-ui-material
val pokemonDetailMaterialNavigationModule = module {
    single<Set<EntryProviderInstaller>>(named(PokemonDetailMaterialNavigationInstallersQualifier)) {
        setOf({
            // PokemonDetail entry (feature's own route)
            entry<PokemonDetail> { route ->
                PokemonDetailMaterialScreen(
                    // ... can navigate back to other features' routes
                    onBackClick = { navigator.goBack() }
                )
            }
        })
    }
}
```

**Key principle**: Features are self-contained but can reference routes from other features' `:api` modules.

### Dependency Rules

```
:features:pokemonlist:wiring-ui-material  →  :features:pokemondetail:api        ✅ OK (public route)
:features:pokemonlist:wiring-ui-material  →  :features:pokemondetail:ui-material   ❌ NEVER (implementation)
```

### Cross-Feature Navigation

```kotlin
// :features:pokemonlist:wiring-ui-material
val pokemonListMaterialNavigationModule = module {
    single<Set<EntryProviderInstaller>>(named(PokemonListMaterialNavigationInstallersQualifier)) {
        setOf({
            entry<PokemonList> {
                val navigator: Navigator = koinInject()
                val viewModel: PokemonListViewModel = koinInject()

                PokemonListMaterialScreen(
                    viewModel = viewModel,
                    onPokemonClick = { pokemon ->
                        // Navigate to other feature's route
                        navigator.goTo(PokemonDetail(pokemon.id))
                    },
                )
            }
        })
    }
}
```

**Imports**: Only import route objects from other features' `:api` modules.

## Module Dependencies

### Feature Wiring-UI Dependencies

```kotlin
// :features:pokemonlist:wiring-ui-material/build.gradle.kts
plugins {
    id("convention.feature.wiring")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Core navigation
            implementation(projects.core.navigation)
            implementation(projects.core.designsystemMaterial)  // Material theme

            // Feature modules
            implementation(projects.features.pokemonlist.api)
            implementation(projects.features.pokemonlist.presentation)
            implementation(projects.features.pokemonlist.uiMaterial)

            // Other features' API (for cross-feature navigation)
            implementation(projects.features.pokemondetail.api)

            // Koin
            implementation(libs.koin.compose)
        }
    }
}
```

## Platform-Specific Navigation

### Android-Specific Customizations

```kotlin
// :features:pokemonlist:wiring-ui-material/src/androidMain/.../PokemonListAndroidNavigationProviders.kt
package com.example.features.pokemonlist.wiringui.material

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import com.example.core.navigation.EntryProviderInstaller
import com.example.features.pokemonlist.api.PokemonList
import org.koin.compose.koinInject
import org.koin.dsl.module

internal actual fun Module.providePlatformNavigationProviders() {
    single<Set<EntryProviderInstaller>>(named(PokemonListMaterialNavigationInstallersQualifier)) {
        setOf({
            entry<PokemonList> {
                val navigator: Navigator = koinInject()
                val viewModel: PokemonListViewModel = koinInject()
                val snackbarHostState = SnackbarHostState()

                PokemonListMaterialScreen(
                    viewModel = viewModel,
                    onPokemonClick = { navigator.goTo(PokemonDetail(it.id)) },
                    onShowSnackbar = { message ->
                        scope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    }  // Android-specific feature
                )
            }
        })
    }
}
```

### Desktop-Specific Customizations

```kotlin
// :features:pokemonlist:wiring-ui-material/src/jvmMain/.../PokemonListDesktopNavigationProviders.kt
package com.example.features.pokemonlist.wiringui.material

import com.example.core.navigation.EntryProviderInstaller
import com.example.features.pokemonlist.api.PokemonList
import org.koin.compose.koinInject
import org.koin.dsl.module

internal actual fun Module.providePlatformNavigationProviders() {
    single<Set<EntryProviderInstaller>>(named(PokemonListMaterialNavigationInstallersQualifier)) {
        setOf({
            entry<PokemonList> {
                val navigator: Navigator = koinInject()
                val viewModel: PokemonListViewModel = koinInject()

                PokemonListMaterialScreen(
                    viewModel = viewModel,
                    onPokemonClick = { navigator.goTo(PokemonDetail(it.id)) },
                    onShowSnackbar = { /* Desktop: show tray notification */ }  // Desktop-specific
                )
            }
        })
    }
}
```

### Expect/Actual Pattern

```kotlin
// commonMain
internal expect fun Module.providePlatformNavigationProviders()

val pokemonListMaterialNavigationModule = module {
    providePlatformNavigationProviders()
}

// androidMain
internal actual fun Module.providePlatformNavigationProviders() { /* ... */ }

// jvmMain
internal actual fun Module.providePlatformNavigationProviders() { /* ... */ }
```

## Validation Commands

```bash
# Build and test
./gradlew :composeApp:assembleDebug test --continue

# Check navigation module dependencies
./gradlew :features:pokemonlist:wiring-ui-material:dependencies --configuration commonMain

# Verify iOS export (should NOT include navigation modules)
./gradlew :shared:dependencies --configuration iosMain
```

## Reference Implementations

- `features/pokemonlist/wiring-ui-material/PokemonListMaterialNavigationProviders.kt` — Material scoped navigation
- `features/pokemonlist/wiring-ui-unstyled/PokemonListUnstyledNavigationProviders.kt` — Unstyled scoped navigation
- `composeApp/src/commonMain/kotlin/App.kt` — Dual-theme navigation integration
- `core/di/src/commonMain/kotlin/AppModules.kt` — Koin module configuration
