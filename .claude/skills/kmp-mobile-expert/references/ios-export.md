# iOS Export Guide

Detailed iOS export and integration patterns for Kotlin Multiplatform.

## What to Export

Only export public contracts and presentation layers to iOS:

```kotlin
// In :shared/build.gradle.kts
kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    iosTarget.binaries.framework {
        baseName = "Shared"
        isStatic = true
        
        // Export public contracts
        export(projects.features.pokemonlist.api)
        
        // Export presentation (ViewModels + UI state)
        export(projects.features.pokemonlist.presentation)
        
        // Export core utilities
        export(projects.core.domain)
        export(projects.core.util)
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.features.pokemonlist.api)
            api(projects.features.pokemonlist.presentation)
        }
    }
}
```

## Export Rules

| Module Type | Export to iOS? | Reason |
|-------------|----------------|--------|
| `:api` | ✅ YES | Public contracts, domain models |
| `:presentation` | ✅ YES | ViewModels, UI state |
| `:data` | ❌ NO | Internal implementation |
| `:ui-material` | ❌ NO | Compose-specific |
| `:ui-unstyled` | ❌ NO | Compose-specific |
| `:wiring` | ❌ NO | Koin DI modules |
| `:core:designsystem` | ❌ NO | Compose components |

## iOS Integration Pattern (Direct Integration)

### SwiftUI Side

```swift
import SwiftUI
import Shared

struct PokemonListView: View {
    // Get ViewModel from Koin via helper
    private var viewModel = KoinIosKt.getPokemonListViewModel()

    // Bridge StateFlow to SwiftUI
    @State private var uiState: PokemonListUiState = PokemonListUiStateLoading()

    var body: some View {
        content
            .onAppear {
                viewModel.onStart(owner: DummyLifecycleOwner())
            }
            .onDisappear {
                viewModel.onStop(owner: DummyLifecycleOwner())
            }
            .task {
                // SKIE: StateFlow → AsyncSequence
                for await state in viewModel.uiState {
                    self.uiState = state
                }
            }
    }
}
```

### Kotlin Helper (in shared/src/iosMain)

```kotlin
fun getPokemonListViewModel(): PokemonListViewModel {
    return KoinPlatform.getKoin().get()
}

fun getPokemonDetailViewModel(pokemonId: Int): PokemonDetailViewModel {
    return KoinPlatform.getKoin().get { parametersOf(pokemonId) }
}
```

## Parametric ViewModels

For ViewModels requiring parameters (e.g., pokemonId):

**Kotlin:**
```kotlin
val pokemonDetailModule = module {
    factory { params ->
        PokemonDetailViewModel(
            pokemonId = params.get(),
            repository = get()
        )
    }
}

fun getPokemonDetailViewModel(pokemonId: Int): PokemonDetailViewModel {
    return KoinPlatform.getKoin().get { parametersOf(pokemonId) }
}
```

**Swift:**
```swift
struct PokemonDetailView: View {
    let pokemonId: Int
    private var viewModel: PokemonDetailViewModel

    init(pokemonId: Int) {
        self.pokemonId = pokemonId
        // Cast Swift Int to Kotlin Int32
        viewModel = KoinIosKt.getPokemonDetailViewModel(pokemonId: Int32(pokemonId))
    }
}
```

## Type Conversion Reference

| Kotlin Type | Swift Type | Conversion |
|-------------|------------|------------|
| `Int` | `Int32` | `Int(pokemon.id)` |
| `String` | `String` | Direct |
| `Double` | `Double` | Direct |
| `Boolean` | `Bool` | Direct |
| `List<T>` | `KotlinArray<T>` | `Array(kotlinArray)` |
| `StateFlow<T>` | `AsyncSequence<T>` | Use `.task { for await ... }` |

## SKIE Renamed Types

Kotlin classes named after Swift keywords get `_` suffix:

| Kotlin Class | Swift Name |
|--------------|------------|
| `Type` | `Type_` |
| `Error` | `Error_` |
| `Result` | `Result_` |

## Decision Guide: Direct vs Wrapper Pattern

| Requirement | Direct Integration | Wrapper Pattern |
|-------------|-------------------|-----------------|
| Simple linear navigation | ✅ Recommended | ⚠️ Overkill |
| Tab-based navigation | ❌ State loss | ✅ Required |
| Sheet/modal presentation | ⚠️ May lose state | ✅ Preserves state |
| Testing in Previews | ❌ Hard to mock | ✅ Easy to mock |

See [ios_integration.md](../../../docs/tech/ios_integration.md) for complete comparison.
