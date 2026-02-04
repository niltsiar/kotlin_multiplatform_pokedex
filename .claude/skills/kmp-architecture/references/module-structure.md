# Module Structure Reference

Complete breakdown of the 8-module split-by-layer architecture.

## Module Overview

Each feature consists of exactly these 8 modules:

### 1. `:api` - Public Contracts

**Purpose**: Define public interfaces, domain models, and navigation contracts.

**Contains**:
- Repository interfaces (`<Feature>Repository.kt`)
- Domain models (if shared across features)
- Navigation route definitions (Navigation 3 routes)
- Public constants/enums

**Example**:
```kotlin
// features/pokemonlist/api/PokemonListRepository.kt
interface PokemonListRepository {
    suspend fun getPokemonPage(offset: Int): Either<RepoError, PokemonPage>
    fun stream(): Flow<List<Pokemon>>
}

// features/pokemonlist/api/navigation/PokemonList.kt
@Parcelize
@Serializable
object PokemonList : Screen
```

**Gradle**:
```kotlin
plugins {
    id("convention.feature.api")
}
```

### 2. `:data` - Network and Data Layer

**Purpose**: API services, DTOs, repository implementations, mappers.

**Contains**:
- API service classes (`<Feature>ApiService.kt`)
- DTO classes (`dto/<Feature>Dto.kt`)
- Repository implementations (internal class)
- Mapper functions (`mappers/<Feature>Mappers.kt`)
- Factory functions for repositories

**Example**:
```kotlin
// features/pokemonlist/data/PokemonListApiService.kt
internal class PokemonListApiService(
    private val httpClient: HttpClient
) {
    suspend fun getPokemonList(limit: Int, offset: Int): PokemonListDto { /* ... */ }
}

// features/pokemonlist/data/PokemonListRepositoryImpl.kt
internal class PokemonListRepositoryImpl(
    private val api: PokemonListApiService
) : PokemonListRepository { /* ... */ }

// Public factory
fun PokemonListRepository(api: PokemonListApiService): PokemonListRepository =
    PokemonListRepositoryImpl(api)
```

**Gradle**:
```kotlin
plugins {
    id("convention.feature.data")
}

dependencies {
    implementation(projects.features.pokemonlist.api)
    implementation(projects.core.httpclient)
}
```

### 3. `:presentation` - ViewModels and UI State

**Purpose**: ViewModels, UI state classes, one-time events. Shared with iOS.

**Contains**:
- ViewModel classes (`<Feature>ViewModel.kt`)
- UI state classes (sealed interface hierarchy)
- One-time event classes
- UiStateHolder implementations

**Example**:
```kotlin
// features/pokemonlist/presentation/PokemonListViewModel.kt
class PokemonListViewModel(
    private val repository: PokemonListRepository,
    private val savedStateHandle: SavedStateHandle,
    viewModelScope: CoroutineScope
) : ViewModel(viewModelScope), DefaultLifecycleObserver,
    UiStateHolder<PokemonListUiState, PokemonListUiEvent> {
    // Implementation
}

// features/pokemonlist/presentation/PokemonListUiState.kt
sealed interface PokemonListUiState {
    data object Loading : PokemonListUiState
    data class Content(val pokemon: ImmutableList<Pokemon>) : PokemonListUiState
    data class Error(val message: String) : PokemonListUiState
}
```

**Gradle**:
```kotlin
plugins {
    id("convention.feature.presentation")
}

dependencies {
    implementation(projects.features.pokemonlist.api)
}
```

### 4. `:ui-material` - Material Design 3 UI

**Purpose**: Compose UI screens using Material Design 3 components.

**Contains**:
- Screen composables (`<Feature>Screen.kt`)
- State-specific sub-composables
- Preview functions for all states

**Example**:
```kotlin
// features/pokemonlist/ui-material/PokemonListScreen.kt
@Composable
fun PokemonListScreen(
    viewModel: PokemonListViewModel,
    onPokemonClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) { /* ... */ }

@Preview
@Composable
private fun PokemonListScreenPreview() { /* ... */ }
```

**Gradle**:
```kotlin
plugins {
    id("convention.feature.ui")
}

dependencies {
    implementation(projects.features.pokemonlist.presentation)
    implementation(projects.core.designsystemMaterial)
}
```

### 5. `:ui-unstyled` - Compose Unstyled UI

**Purpose**: Compose UI screens using headless Compose Unstyled components.

**Contains**:
- Screen composables (`<Feature>ScreenUnstyled.kt`)
- Styling wrappers and theme integration
- Preview functions

**Example**:
```kotlin
// features/pokemonlist/ui-unstyled/PokemonListScreenUnstyled.kt
@Composable
fun PokemonListScreenUnstyled(
    viewModel: PokemonListViewModel,
    onPokemonClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) { /* ... */ }
```

**Gradle**:
```kotlin
plugins {
    id("convention.feature.ui")
}

dependencies {
    implementation(projects.features.pokemonlist.presentation)
    implementation(projects.core.designsystemUnstyled)
}
```

### 6. `:wiring` - Business DI Assembly

**Purpose**: Koin modules for repositories and ViewModels.

**Contains**:
- Koin module definitions (`<Feature>Module.kt`)
- Factory registrations for repositories
- ViewModel factory registrations

**Example**:
```kotlin
// features/pokemonlist/wiring/PokemonListModule.kt
val pokemonListModule = module {
    factory { PokemonListApiService(httpClient = get()) }
    factory<PokemonListRepository> { PokemonListRepository(api = get()) }
    factory<PokemonListViewModel> { 
        PokemonListViewModel(repository = get(), savedStateHandle = get()) 
    }
}
```

**Key Rule**: `wiring` MUST NOT depend on `core:di` (circular dependency).

**Gradle**:
```kotlin
plugins {
    id("convention.feature.wiring")
}

dependencies {
    implementation(projects.features.pokemonlist.api)
    implementation(projects.features.pokemonlist.data)
    implementation(projects.features.pokemonlist.presentation)
}
```

### 7. `:wiring-ui-material` - Material Navigation

**Purpose**: Navigation 3 entry providers for Material Design 3 UI.

**Contains**:
- Scoped navigation module (`<Feature>MaterialNavigationProviders.kt`)
- Navigation entry registrations within `MaterialScope`

**Example**:
```kotlin
// features/pokemonlist/wiring-ui-material/PokemonListMaterialNavigationProviders.kt
val pokemonListMaterialNavigationModule = module {
    scope<MaterialScope> {
        navigation<PokemonList> { route ->
            PokemonListScreen(
                viewModel = koinViewModel(),
                onPokemonClick = { id -> /* navigation */ }
            )
        }
    }
}
```

**Gradle**:
```kotlin
plugins {
    id("convention.feature.wiring")
}

dependencies {
    implementation(projects.core.designsystemMaterial) // For MaterialScope
    implementation(projects.features.pokemonlist.uiMaterial)
}
```

### 8. `:wiring-ui-unstyled` - Unstyled Navigation

**Purpose**: Navigation 3 entry providers for Compose Unstyled UI.

**Contains**:
- Scoped navigation module (`<Feature>UnstyledNavigationProviders.kt`)
- Navigation entry registrations within `UnstyledScope`
- Theme wrapper integration

**Example**:
```kotlin
// features/pokemonlist/wiring-ui-unstyled/PokemonListUnstyledNavigationProviders.kt
val pokemonListUnstyledNavigationModule = module {
    scope<UnstyledScope> {
        navigation<PokemonList> { route ->
            UnstyledTheme {
                PokemonListScreenUnstyled(
                    viewModel = koinViewModel(),
                    onPokemonClick = { id -> /* navigation */ }
                )
            }
        }
    }
}
```

**Gradle**:
```kotlin
plugins {
    id("convention.feature.wiring")
}

dependencies {
    implementation(projects.core.designsystemUnstyled) // For UnstyledScope
    implementation(projects.features.pokemonlist.uiUnstyled)
}
```

## Platform-Specific Source Sets in Wiring

Wiring modules use platform-specific source sets for UI dependencies:

```kotlin
kotlin {
    sourceSets {
        // Common: Repos, ViewModels (exported to iOS)
        commonMain.dependencies {
            implementation(projects.features.pokemonlist.api)
            implementation(projects.features.pokemonlist.data)
            implementation(projects.features.pokemonlist.presentation)
        }
        
        // Android + JVM: Can depend on :ui modules
        val androidMain by getting {
            dependencies {
                implementation(projects.features.pokemonlist.uiMaterial)
            }
        }
        
        val jvmMain by getting {
            dependencies {
                implementation(projects.features.pokemonlist.uiMaterial)
            }
        }
        
        // iOS: Uses only commonMain (no :ui dependency)
        // iOS accesses ViewModels from :presentation via :shared framework
    }
}
```

## Optional: `:domain` Module

For features with complex business logic orchestrating multiple repositories:

```
:features:<feature>:domain       → Use cases, validators, business rules
```

**When to create**: Feature has use cases that orchestrate 2+ repositories or enforce complex business rules. Most features won't need this—call repositories directly from ViewModels.

## Module Dependencies Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                        :composeApp                          │
│  (Android app / Desktop app / iOS Compose app)              │
└─────────────────────┬───────────────────────────────────────┘
                      │ depends on
┌─────────────────────▼───────────────────────────────────────┐
│  :features:*:wiring-ui-material                              │
│  :features:*:wiring-ui-unstyled                              │
└─────────────────────┬───────────────────────────────────────┘
                      │ depends on
┌─────────────────────▼───────────────────────────────────────┐
│  :features:*:wiring                                          │
└─────────────────────┬───────────────────────────────────────┘
                      │ depends on
    ┌─────────────────┼─────────────────┐
    │                 │                 │
┌───▼────┐    ┌──────▼──────┐    ┌──────▼──────┐
│ :data  │    │ :presentation│    │  :api       │
└────────┘    └──────────────┘    └─────────────┘
    │                 │
    │         ┌───────┘
    │         │
    └────┬────┘
         │ implements
    ┌────▼────┐
    │  :api   │
    └─────────┘
```

## File Naming Conventions

| Module | File Naming Pattern | Example |
|--------|---------------------|---------|
| `:api` | `<Feature><Concept>.kt` | `PokemonListRepository.kt` |
| `:data` | `<Feature><Concept>Impl.kt` | `PokemonListRepositoryImpl.kt` |
| `:data` | `<Feature>ApiService.kt` | `PokemonListApiService.kt` |
| `:data` | `<Feature>Mappers.kt` | `PokemonMappers.kt` |
| `:presentation` | `<Feature>ViewModel.kt` | `PokemonListViewModel.kt` |
| `:presentation` | `<Feature>UiState.kt` | `PokemonListUiState.kt` |
| `:ui-*` | `<Feature>Screen.kt` | `PokemonListScreen.kt` |
| `:wiring` | `<Feature>Module.kt` | `PokemonListModule.kt` |
| `:wiring-ui-*` | `<Feature><Variant>NavigationProviders.kt` | `PokemonListMaterialNavigationProviders.kt` |

**NavigationProviders naming**: Include variant suffix to avoid IDE navigation collisions:
- Material: `<Feature>MaterialNavigationProviders.kt`
- Unstyled: `<Feature>UnstyledNavigationProviders.kt`
