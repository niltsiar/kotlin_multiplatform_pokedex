# Domain Model Design Patterns

Guidelines for creating pure, immutable, platform-agnostic domain models.

## Core Principles

### Immutability

Always use `val` properties. Domain models should never change after creation.

```kotlin
// ✅ Correct: Immutable
data class Pokemon(
    val id: PokemonId,
    val name: String,
    val types: List<PokemonType>,
)

// ❌ Wrong: Mutable
data class Pokemon(
    var id: PokemonId,
    var name: String,
)
```

### Purity

Domain models contain only business data - no infrastructure concerns:

```kotlin
// ✅ Pure domain
@JvmInline
value class PokemonId(val value: Int)

data class Pokemon(
    val id: PokemonId,
    val name: String,
    val types: List<PokemonType> = emptyList(),
)

// ❌ Wrong: Contains infrastructure concerns
data class Pokemon(
    val id: Int,
    @SerializedName("name")  // No!
    val name: String,
    @ColumnInfo("types")      // No!
    val types: List<PokemonType>,
)
```

### Default Values

Provide sensible defaults for optional fields to simplify construction and testing.

```kotlin
data class User(
    val id: UserId,
    val name: String = "",                    // Optional with default
    val avatarUrl: String? = null,            // Nullable with null default
    val preferences: UserPrefs = UserPrefs(), // Complex default
)
```

**Caution**: Do not add defaults that hide required invariants. Required fields should have no default.

```kotlin
// ❌ Wrong: Hides required field
data class Order(
    val items: List<Item> = emptyList(),  // Should be required!
    val total: Money,
)

// ✅ Correct: Required field has no default
data class Order(
    val items: List<Item>,
    val total: Money,
)
```

## Value Objects

Use inline value classes for type safety:

```kotlin
@JvmInline
value class PokemonId(val value: Int)

@JvmInline
value class UserId(val value: String)

@JvmInline
value class Money(val cents: Int) {
    val dollars: Double get() = cents / 100.0
}
```

## Derived Properties

Use pure, side-effect-free helpers via extension functions:

```kotlin
// Extension on domain model
data class Pokemon(
    val id: PokemonId,
    val name: String,
    val types: List<PokemonType>,
    val stats: Map<StatType, Int>,
)

// Pure derivation
fun Pokemon.totalStats(): Int = stats.values.sum()

fun Pokemon.isLegendary(): Boolean =
    totalStats() > 500 || types.contains(PokemonType.DRAGON)

fun Pokemon.displayName(): String =
    name.replaceFirstChar { it.uppercase() }
```

## Sealed Classes for States

Use sealed classes to model domain states:

```kotlin
sealed interface OrderStatus {
    data object Pending : OrderStatus
    data class Processing(val startedAt: Instant) : OrderStatus
    data class Completed(val completedAt: Instant, val receipt: Receipt) : OrderStatus
    data class Failed(val reason: String, val canRetry: Boolean) : OrderStatus
}
```

## Domain Collections

Prefer immutable collections:

```kotlin
data class Cart(
    val items: List<CartItem> = emptyList(),  // Immutable List
) {
    val total: Money
        get() = items.fold(Money(0)) { acc, item ->
            acc + item.price
        }
    
    fun addItem(item: CartItem): Cart =
        copy(items = items + item)
}
```

## Module Placement

### Feature-Local Domain

Most domain types should be feature-local:

```
:features:pokemonlist:api/
  └── PokemonListItem.kt     # Public contract
  └── PokemonRepository.kt   # Interface

:features:pokemonlist:data/
  └── PokemonMapper.kt       # DTO → Domain mapping
  └── PokemonEntity.kt       # Private DB type
```

### Shared Domain

Only create `:core:domain:api` for truly cross-feature types:

```kotlin
// In :core:domain:api
sealed interface RepoError {
    data class Network(val cause: Throwable) : RepoError
    data class Http(val code: Int, val message: String) : RepoError
    data class NotFound(val id: String) : RepoError
    data class Unknown(val cause: Throwable) : RepoError
}
```

## Mapping at Boundaries

Map from DTOs/entities at the data layer boundary:

```kotlin
// In :features:pokemon:data
internal fun PokemonDto.toDomain(): Pokemon =
    Pokemon(
        id = PokemonId(id),
        name = name,
        types = types.map { it.toDomain() },
    )

internal fun PokemonEntity.toDomain(): Pokemon =
    Pokemon(
        id = PokemonId(id),
        name = name,
        types = types.split(",").map { PokemonType.valueOf(it) },
    )
```

## Testing with Fakes

Domain models should be easy to construct in tests:

```kotlin
// Easy construction with defaults
val testPokemon = Pokemon(
    id = PokemonId(25),
    name = "Pikachu",
    // types defaults to emptyList()
)

// Fake repository for testing
class FakePokemonRepository : PokemonRepository {
    private val pokemon = mutableMapOf<PokemonId, Pokemon>()
    
    override suspend fun getPokemon(id: PokemonId): Either<RepoError, Pokemon> {
        return pokemon[id]?.right() ?: RepoError.NotFound(id.toString()).left()
    }
    
    fun addPokemon(p: Pokemon) { pokemon[p.id] = p }
    fun clear() { pokemon.clear() }
}
```

## Anti-Patterns

### ❌ Mutable State in Models

```kotlin
// Wrong - allows external modification
class Pokemon {
    lateinit var name: String
}
```

### ❌ Infrastructure Coupling

```kotlin
// Wrong - ties domain to persistence
data class User(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)
```

### ❌ UI Logic in Domain

```kotlin
// Wrong - UI concerns don't belong in domain
data class Pokemon(
    val id: PokemonId,
    val name: String,
    val isLoading: Boolean = false,  // UI state!
    val errorMessage: String? = null, // UI state!
)
```

### ❌ Android Types in Domain

```kotlin
// Wrong - platform-specific types
import android.graphics.Bitmap

data class Pokemon(
    val id: PokemonId,
    val sprite: Bitmap,  // Android type!
)
```
