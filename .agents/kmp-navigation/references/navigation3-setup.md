# Navigation 3 Setup

Complete setup guide for Navigation 3 modular architecture in Kotlin Multiplatform.

## Core Module Setup

### :core:navigation Module

```kotlin
// core/navigation/build.gradle.kts
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

### Navigator Implementation

```kotlin
// core/navigation/src/commonMain/kotlin/.../Navigator.kt
package com.example.core.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class Navigator(startDestination: Any) {
    private val _backStack: SnapshotStateList<Any> = mutableStateListOf(startDestination)
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

### EntryProviderInstaller Type Alias

```kotlin
// core/navigation/src/commonMain/kotlin/.../EntryProviderInstaller.kt
package com.example.core.navigation

import org.jetbrains.androidx.navigation.EntryProviderScope

typealias EntryProviderInstaller = EntryProviderScope<Any>.() -> Unit
```

**Purpose**: Enables Koin DI named qualifier collection for dynamic navigation graph assembly.

## Koin DI Configuration

### Core Navigation Module

```kotlin
// core/di/src/commonMain/kotlin/.../AppModules.kt
package com.example.core.di

import com.example.core.navigation.Navigator
import org.koin.dsl.module

val navigationUiModule = module {
    // Provide Navigator singleton with start destination
    single<Navigator> { Navigator(startDestination = PokemonList) }
}
```

### Feature Navigation Module

```kotlin
// features/pokemonlist/wiring-ui/src/commonMain/.../PokemonListNavigationProviders.kt
package com.example.features.pokemonlist.wiringui

import com.example.core.navigation.EntryProviderInstaller
import com.example.core.navigation.Navigator
import com.example.features.pokemonlist.api.PokemonList
import com.example.features.pokemonlist.presentation.PokemonListViewModel
import com.example.features.pokemondetail.api.PokemonDetail
import org.koin.compose.koinInject
import org.koin.dsl.module

// Public const qualifier for direct collection in App.kt
const val PokemonListNavigationInstallersQualifier = "pokemonListNavigationInstallers"

val pokemonListNavigationModule = module {
    single<Set<EntryProviderInstaller>>(named(PokemonListNavigationInstallersQualifier)) {
        setOf({
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
        })
    }
}
```

**Key points**:
- Public const qualifier enables direct collection in App.kt
- Returns `Set<EntryProviderInstaller>` with named qualifier
- `koinInject()` resolves dependencies from Compose context
- `entry<RouteType>` registers composable for route

## App.kt Integration

### KoinApplication Setup

```kotlin
// composeApp/src/commonMain/kotlin/.../App.kt
package com.example

import androidx.compose.runtime.Composable
import com.example.core.di.navigationUiModule
import com.example.core.navigation.EntryProviderInstaller
import com.example.core.navigation.Navigator
import com.example.features.pokemonlist.api.PokemonList
import com.example.features.pokemonlist.wiringui.PokemonListNavigationInstallersQualifier
import com.example.features.pokemondetail.wiringui.PokemonDetailNavigationInstallersQualifier
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
                pokemonListNavigationModule +
                pokemonDetailNavigationModule
            )
        }
    ) {
        val navigator: Navigator = koinInject()

        // Collect navigation installers from all feature modules
        val pokemonListInstallers: Set<EntryProviderInstaller> =
            koinInject(qualifier = named(PokemonListNavigationInstallersQualifier))
        val pokemonDetailInstallers: Set<EntryProviderInstaller> =
            koinInject(qualifier = named(PokemonDetailNavigationInstallersQualifier))

        val allInstallers = pokemonListInstallers + pokemonDetailInstallers

        PokemonTheme {
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
```

**Flow**:
1. `koinInject()` resolves dependencies from Koin context
2. Navigation installers collected directly using public const qualifiers
3. NavDisplay observes navigator.backStack (SnapshotStateList)
4. entryProvider installs all EntryProviderInstallers from features
5. Back navigation triggers navigator.goBack()

## Platform-Specific Source Sets

### Module Structure

```
:features:pokemonlist:wiring-ui/
├── build.gradle.kts
└── src/
    ├── commonMain/kotlin/             # Shared navigation registration (Android, Desktop, iOS Compose)
    ├── androidMain/kotlin/            # Android-specific customizations (optional)
    ├── jvmMain/kotlin/                # Desktop-specific customizations (optional)
    └── iosMain/kotlin/                # iOS Compose-specific customizations (optional)
```

### Common Main (Compose Multiplatform)

```kotlin
// :features:pokemonlist:wiring-ui/src/commonMain/.../PokemonListNavigationProviders.kt
val pokemonListNavigationModule = module {
    single<Set<EntryProviderInstaller>>(named(PokemonListNavigationInstallersQualifier)) {
        setOf({
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
        })
    }
}
```

**Pattern**: Identical pattern for all Compose Multiplatform targets (Android, Desktop, iOS Compose). Can diverge for platform-specific UI.

### Platform-Specific Customizations

```kotlin
// :features:pokemonlist:wiring-ui/src/androidMain/.../PokemonListNavigationProviders.kt
internal actual fun Module.providePlatformNavigationProviders() {
    // Android-specific navigation providers
    // Use actual keyword in commonMain expect declaration
}

// :features:pokemonlist:wiring-ui/src/jvmMain/.../PokemonListNavigationProviders.kt
internal actual fun Module.providePlatformNavigationProviders() {
    // Desktop-specific navigation providers
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

### Feature :wiring-ui Dependencies

```kotlin
plugins {
    id("convention.feature.wiring")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.navigation)
            implementation(projects.features.pokemonlist.api)
            implementation(projects.features.pokemonlist.presentation)
            implementation(projects.features.pokemonlist.ui)
            implementation(libs.koin.compose)
        }
    }
}
```

**Key points**:
- :api, :presentation in commonMain (all platforms)
- :ui in commonMain (Compose Multiplatform - Android, Desktop, iOS Compose)
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
- ❌ :wiring-ui modules (Compose navigation registration)
- ❌ EntryProviderInstaller (Compose-specific type)

**iOS navigation**: SwiftUI app implements own navigation, calls shared ViewModels.

### Verify iOS Exports

```bash
# Check that navigation modules are NOT exported
./gradlew :shared:dependencies --configuration iosMain
```

Should NOT include:
- `:core:navigation`
- `:features:*:wiring-ui`
- `:features:*:ui`

Should include:
- `:features:*:api`
- `:features:*:presentation`
- `:features:*:data`
