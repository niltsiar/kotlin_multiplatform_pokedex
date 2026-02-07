# Core Module Guidelines

When to create `:core` modules vs keeping code in `:features` modules.

## The Golden Rule

> **Duplication is better than premature abstraction.**
>
> Only create `:core` modules for truly cross-cutting concerns used by 3+ features.

## When to Create :core Modules

### ✅ DO Create for Cross-Cutting Concerns

| Category | Examples | Threshold |
|----------|----------|-----------|
| **Generic utilities** | Date formatters, string utils, math helpers | 3+ features |
| **Design system** | Theme, reusable UI components, design tokens | Always |
| **Cross-cutting domain** | User, Error types, common enums | 3+ features |
| **Platform abstractions** | expect/actual for platform APIs | 2+ features |
| **Shared infrastructure** | HttpClient configuration | Always |

### Examples of Valid :core Modules

```
:core:designsystem-material       → Material 3 Expressive theme
:core:designsystem-unstyled       → Compose Unstyled theme
:core:designsystem-core           → Shared design tokens
:core:navigation                  → Navigation 3 utilities
:core:di                          → Koin configuration, HttpClient
:core:httpclient                  → Ktor client setup
:core:domain                      → Common error types (RepoError, etc.)
:core:util                        → Generic utilities
```

## When NOT to Create :core Modules

### ❌ DON'T Create for Feature-Specific Logic

| Anti-Pattern | Why | Do Instead |
|--------------|-----|------------|
| `:core:network` | Features own their network layer | Each feature has its own API service in `:data` |
| `:core:repository` | Features own their repositories | Each feature defines its own repository interface + impl |
| `:core:data` | Features own their data access | Each feature manages its own DTOs, mappers |
| `:core:api` | Generic API services | Each feature defines its own service interfaces |
| `:core:models` | Shared domain models | Keep models in the feature that owns them; export via `:api` if needed |

### Common Mistakes

#### Mistake 1: Generic Network Layer

```kotlin
// ❌ DON'T: Generic network module
:core:network/
├── ApiService.kt          // Generic service
├── BaseDto.kt             // Base DTO class
└── NetworkClient.kt       // Generic client

// ✅ DO: Feature-specific services
:features:pokemonlist:data/
├── PokemonListApiService.kt
├── dto/
│   └── PokemonListDto.kt
└── PokemonListRepositoryImpl.kt

:features:pokemondetail:data/
├── PokemonDetailApiService.kt
├── dto/
│   └── PokemonDetailDto.kt
└── PokemonDetailRepositoryImpl.kt
```

#### Mistake 2: Generic Repository Base

```kotlin
// ❌ DON'T: Generic repository base
// :core:data/BaseRepository.kt
abstract class BaseRepository<T> {
    abstract suspend fun get(id: Int): T
    abstract suspend fun save(item: T)
}

// ✅ DO: Feature-specific repositories
// :features:pokemonlist/api/PokemonListRepository.kt
interface PokemonListRepository {
    suspend fun getPokemonPage(offset: Int): Either<RepoError, PokemonPage>
    fun stream(): Flow<List<Pokemon>>
}

// :features:pokemonlist/data/PokemonListRepositoryImpl.kt
internal class PokemonListRepositoryImpl(
    private val api: PokemonListApiService
) : PokemonListRepository {
    override suspend fun getPokemonPage(offset: Int): Either<RepoError, PokemonPage> = 
        Either.catch { /* ... */ }.mapLeft { it.toRepoError() }
    
    override fun stream(): Flow<List<Pokemon>> = /* ... */
}
```

#### Mistake 3: Shared DTOs

```kotlin
// ❌ DON'T: Shared DTOs
// :core:network/dto/PokemonDto.kt
data class PokemonDto(
    val id: Int,
    val name: String,
    val url: String
)

// Used by pokemonlist, pokemondetail, search...
// Problem: When one feature needs a new field, all are affected

// ✅ DO: Separate DTOs per feature
// :features:pokemonlist:data/dto/PokemonListItemDto.kt
data class PokemonListItemDto(
    val name: String,
    val url: String
)

// :features:pokemondetail:data/dto/PokemonDetailDto.kt
data class PokemonDetailDto(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val stats: List<StatDto>
)
```

## Decision Flowchart

```
Need to share code between features?
        │
        ▼
   ┌────┴────┐
   │         │
   No       Yes
   │         │
   ▼         ▼
Keep in  Used by 2 features?
feature     │
         ┌──┴──┐
         │     │
        No    Yes
         │     │
         ▼     ▼
    Keep in  Used by 3+ features?
    one feature  │
              ┌──┴──┐
              │     │
             No    Yes
              │     │
              ▼     ▼
         Consider  Create :core
         exporting  module
         via :api
```

## The 3-Feature Rule

Before creating a `:core` module, verify:

1. **Is it used by 3+ features?** If not, keep it in the feature.
2. **Is it truly generic?** If feature-specific, keep it in the feature.
3. **Will it change together?** If features evolve independently, don't share.

### Examples

| Code | Location | Rationale |
|------|----------|-----------|
| `RepoError` sealed class | `:core:domain` | Used by ALL repositories (5+ features) |
| `DateFormatter` | `:core:util` | Used by 4+ features |
| `Pokemon` domain model | `:features:pokemonlist:api` | Exported for other features to use |
| `PokemonDetail` domain model | `:features:pokemondetail:data` | Internal to this feature only |
| `Theme` object | `:core:designsystem-material` | Used by ALL UI screens |
| `PokemonListApiService` | `:features:pokemonlist:data` | Specific to this feature |
| `HttpClient` config | `:core:di` | Shared infrastructure |

## Existing :core Modules Reference

### :core:designsystem-material
**Purpose**: Material 3 Expressive theme implementation
**Contains**: Color schemes, typography, shapes, motion, component tokens
**Used by**: All `:features:*:ui-material` modules

### :core:designsystem-unstyled
**Purpose**: Compose Unstyled theme implementation
**Contains**: Minimal theme, base tokens for headless components
**Used by**: All `:features:*:ui-unstyled` modules

### :core:designsystem-core
**Purpose**: Shared design tokens
**Contains**: BaseTokens (spacing, elevations, motion curves)
**Used by**: `:core:designsystem-material`, `:core:designsystem-unstyled`

### :core:navigation
**Purpose**: Navigation 3 utilities
**Contains**: Navigator interface, navigation extensions
**Used by**: All feature `:wiring-ui-*` modules

### :core:di
**Purpose**: Koin DI configuration
**Contains**: HttpClient setup, root module composition
**Used by**: `:composeApp`, `:shared`

### :core:httpclient
**Purpose**: Ktor HttpClient configuration
**Contains**: Client setup, interceptors, default request config
**Used by**: All feature `:data` modules

### :core:domain
**Purpose**: Cross-cutting domain types
**Contains**: `RepoError`, `Either` extensions
**Used by**: All `:features:*:api` and `:features:*:data` modules

## Migration: Extracting to :core

If you discover code that should be in `:core`:

1. **Create the :core module** following existing patterns
2. **Move the code** from feature to :core
3. **Update imports** in affected features
4. **Export if needed** via `:shared` for iOS
5. **Document** in module README

### Example: Extracting DateFormatter

```kotlin
// Before: In :features:pokemonlist:data
internal object DateFormatter {
    fun format(date: Instant): String = /* ... */
}

// Also in :features:pokemondetail:data
internal object DateFormatter {
    fun format(date: Instant): String = /* ... */
}

// After: Extract to :core:util
// :core:util/src/commonMain/kotlin/DateFormatter.kt
object DateFormatter {
    fun format(date: Instant): String = /* ... */
}

// Features now depend on :core:util
// :features:pokemonlist:data/build.gradle.kts
dependencies {
    implementation(projects.core.util)
}
```

## Testing :core Modules

Core modules should have comprehensive tests since they affect multiple features:

```kotlin
// :core:util/src/androidUnitTest/kotlin/DateFormatterTest.kt
class DateFormatterSpec : FreeSpec({
    "format returns correct string for valid date" {
        val date = Instant.parse("2024-01-15T10:30:00Z")
        DateFormatter.format(date) shouldBe "Jan 15, 2024"
    }
    
    "format handles edge cases" {
        // Property-based tests for various inputs
    }
})
```

## Summary

| Decision | Action |
|----------|--------|
| Code used by 1 feature | Keep in feature module |
| Code used by 2 features | Keep in one feature, other depends on `:api` |
| Code used by 3+ features | Consider `:core` module |
| Generic utilities | `:core:util` |
| Design system | `:core:designsystem-*` |
| Platform abstractions | `:core:platform` or relevant |
| Feature-specific logic | NEVER in `:core` |

**Remember**: Start with duplication. Extract to `:core` only when a clear pattern emerges across 3+ features.
