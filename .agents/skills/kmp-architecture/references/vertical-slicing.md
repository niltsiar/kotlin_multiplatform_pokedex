# Vertical Slicing Principles

True vertical slicing means each feature contains ALL the layers it needs internally.

## What is Vertical Slicing?

Traditional horizontal layering organizes by technical concern:

```
Horizontal (Traditional)          Vertical (This Project)
┌─────────────────┐               ┌─────────────────┐
│  :core:network  │               │  :features:pokemonlist
├─────────────────┤               │    ├── :api     │
│  :core:data     │               │    ├── :data    │
├─────────────────┤               │    ├── :pres    │
│  :core:domain   │               │    └── :ui-*    │
├─────────────────┤               └─────────────────┘
│  :features:*    │               ┌─────────────────┐
└─────────────────┘               │  :features:pokemondetail
                                  │    ├── :api     │
                                  │    ├── :data    │
                                  │    ├── :pres    │
                                  │    └── :ui-*    │
                                  └─────────────────┘
```

**Each vertical slice contains**:
- Domain models specific to that feature
- Network/API services for that feature's endpoints
- Data/Repository layer for that feature's data access
- Presentation/UI for that feature's screens
- Navigation contracts for that feature's routes

## Why Vertical Slicing?

### 1. Compilation Avoidance

Changes to Pokemon Detail don't recompile Pokemon List:

```kotlin
// :features:pokemondetail:data - internal change
internal class PokemonDetailApiService {
    suspend fun getPokemonById(id: Int): PokemonDetailDto { 
        // Changed implementation
    }
}

// :features:pokemonlist is NOT recompiled because:
// - It only depends on :features:pokemondetail:api (not :data)
// - The :api module didn't change
```

### 2. Team Autonomy

Multiple teams can work on different features simultaneously without merge conflicts:

```
Team A: Working on Pokemon List
Team B: Working on Pokemon Detail
Team C: Working on Search

Each team owns their vertical slice completely.
```

### 3. Feature Independence

Features can be developed, tested, and deployed independently:

```kotlin
// Pokemon List can ship without Pokemon Detail being complete
// Each feature is self-contained
```

### 4. Clear Boundaries

All code for a feature lives in one place:

```
features/pokemonlist/
├── api/          ← Public contracts
├── data/         ← API calls, DTOs, repositories
├── presentation/ ← ViewModels, UI state
├── ui-material/  ← Material Design 3 screens
├── ui-unstyled/  ← Compose Unstyled screens
└── wiring*/      ← DI configuration

Everything about Pokemon List is here.
```

## Feature Independence Rules

### Rule 1: No Feature → Feature Implementation Dependencies

```kotlin
// ✅ CORRECT: Depend on public API only
:features:profile:data  →  :features:auth:api

// ❌ WRONG: Depend on implementation details
:features:profile:data  →  :features:auth:data
:features:profile:data  →  :features:auth:impl
```

**Why**: Implementation changes shouldn't force recompilation of dependent features.

### Rule 2: Each Feature Owns Its Network Layer

```kotlin
// :features:pokemonlist:data - Pokemon List's API service
internal class PokemonListApiService(private val httpClient: HttpClient) {
    suspend fun getPokemons(limit: Int, offset: Int): PokemonListDto { /* ... */ }
}

// :features:pokemondetail:data - Pokemon Detail's API service  
internal class PokemonDetailApiService(private val httpClient: HttpClient) {
    suspend fun getPokemonById(id: Int): PokemonDetailDto { /* ... */ }
}
```

**Both use shared HttpClient from `:core:di` but define their own services.**

### Rule 3: Each Feature Defines Its Own DTOs

```kotlin
// :features:pokemonlist:data/dto/PokemonListDto.kt
data class PokemonListDto(
    val results: List<PokemonListItemDto>
)

// :features:pokemondetail:data/dto/PokemonDetailDto.kt
data class PokemonDetailDto(
    val id: Int,
    val name: String,
    val stats: List<StatDto>
)
```

**Even if both call the same `/pokemon/{id}` endpoint, each defines its own DTO.**

**Why**: Features evolve independently. Shared DTOs create coupling.

### Rule 4: Domain Models in :api Only If Shared

```kotlin
// Pokemon model needed by multiple features → :api
// features/pokemonlist/api/domain/Pokemon.kt
data class Pokemon(
    val id: Int,
    val name: String,
    val imageUrl: String
)

// PokemonDetail only used internally → :data/domain/
// features/pokemondetail/data/domain/PokemonDetail.kt
internal data class PokemonDetail(
    val id: Int,
    val name: String,
    val stats: List<Stat>
)
```

## HttpClient Configuration

Shared HttpClient with feature-specific services:

```kotlin
// :core:di/AppModules.kt
fun httpClientModule() = module {
    single<HttpClient> { createHttpClient() }
}

// :features:pokemonlist:wiring
val pokemonListModule = module {
    factory { PokemonListApiService(httpClient = get()) }
    factory<PokemonListRepository> { PokemonListRepository(apiService = get()) }
}

// :features:pokemondetail:wiring
val pokemonDetailModule = module {
    factory { PokemonDetailApiService(httpClient = get()) }
    factory<PokemonDetailRepository> { PokemonDetailRepository(apiService = get()) }
}
```

**Pattern**: One shared HttpClient, many feature-specific services.

## Migration from Horizontal Layers

If you started with `:core:network`, `:core:data`, `:core:domain`:

1. **Move feature-specific code to feature modules**
   ```
   :core:network/PokemonApiService.kt  →  :features:pokemonlist:data/PokemonListApiService.kt
   :core:data/PokemonRepository.kt     →  :features:pokemonlist:data/PokemonListRepositoryImpl.kt
   ```

2. **Keep only truly generic utilities in :core**
   ```
   :core:httpclient/HttpClient.kt      (shared client configuration)
   :core:util/DateFormatters.kt        (generic utilities)
   ```

3. **Duplicate code across features if needed**
   
   **Coupling is worse than duplication.** If two features need similar DTOs, define them separately. When requirements diverge (and they will), you'll thank yourself.

## Anti-Patterns to Avoid

### ❌ Generic Repository Base Classes

```kotlin
// DON'T: Generic repository pattern
abstract class BaseRepository<T> {
    abstract suspend fun get(id: Int): T
    abstract suspend fun save(item: T)
}

// DO: Each feature defines its own
interface PokemonListRepository {
    suspend fun getPokemonPage(offset: Int): Either<RepoError, PokemonPage>
    fun stream(): Flow<List<Pokemon>>
}
```

### ❌ Generic Network Layer

```kotlin
// DON'T: Generic API service
class ApiService {
    suspend inline fun <reified T> get(url: String): T
}

// DO: Feature-specific service
internal class PokemonListApiService(private val httpClient: HttpClient) {
    suspend fun getPokemonList(limit: Int, offset: Int): PokemonListDto
}
```

### ❌ Shared DTOs Across Features

```kotlin
// DON'T: Shared DTO in :core
// :core:network/dto/PokemonDto.kt

data class PokemonDto(...) // Used by both features

// DO: Separate DTOs per feature
// :features:pokemonlist:data/dto/PokemonListItemDto.kt
// :features:pokemondetail:data/dto/PokemonDetailDto.kt
```

## Benefits Summary

| Benefit | Description |
|---------|-------------|
| **Compilation Avoidance** | Changes isolated to modified feature |
| **Team Autonomy** | Teams work independently on features |
| **Testability** | Features self-contained with clear boundaries |
| **Deployability** | Features can be feature-flagged independently |
| **Clarity** | All code for a feature in one place |
| **Flexibility** | Features evolve independently |

## Reference Implementation

Study these features for complete vertical slice examples:

### Pokemon List (`features/pokemonlist`)
- **Pattern**: Simple list with pagination (offset-based)
- **Navigation**: Simple route (`object PokemonList`)
- **Data**: List endpoint (`/pokemon?limit=20&offset=0`)
- **Key files**:
  - API: `PokemonListRepository.kt`
  - Data: `PokemonListApiService.kt`, `PokemonListRepositoryImpl.kt`, `PokemonMappers.kt`
  - Presentation: `PokemonListViewModel.kt`, `PokemonListUiState.kt`
  - UI: `PokemonListScreen.kt`, `PokemonListScreenUnstyled.kt`

### Pokemon Detail (`features/pokemondetail`)
- **Pattern**: Parametric ViewModel with ID parameter
- **Navigation**: Parameterized route (`data class PokemonDetail(val id: Int)`)
- **Data**: Detail endpoint with nested structures (`/pokemon/{id}`)
- **Key files**:
  - API: `PokemonDetailRepository.kt`, `PokemonDetail.kt` (navigation)
  - Data: `PokemonDetailApiService.kt`, `PokemonDetailRepositoryImpl.kt`
  - Presentation: `PokemonDetailViewModel.kt` (parametric)
  - UI: `PokemonDetailScreen.kt` (with Navigation 3 animations)

**Key Differences**:
- `pokemonlist`: No parameters, simple list state, infinite scroll
- `pokemondetail`: Parametric ViewModel, nested DTOs, retry mechanism, animations
