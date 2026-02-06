---
name: kmp-domain
description: "Domain layer patterns for Kotlin Multiplatform: immutable data classes, use case guidelines, domain exceptions, and pure business logic. Use when: (1) Designing domain models and data classes, (2) Deciding when to create use cases vs calling repositories directly, (3) Implementing domain exceptions (UnauthenticatedException, PurchaseRequiredException), (4) Defining repository interfaces, (5) Writing unit-testable domain logic with fakes"
---

# KMP Domain Layer Skill

Domain layer patterns for pure, immutable, platform-agnostic business logic in Kotlin Multiplatform.

## When to Load This Skill

**MANDATORY**: Load this skill when working on:
- Creating domain models and data classes
- Deciding between use cases vs direct repository calls
- Implementing domain exceptions for business rules
- Defining repository interfaces in `:api` modules
- Writing unit-testable domain logic

**Do NOT use for**: Repository implementation details → use @kmp-data-layer, ViewModel patterns → use @kmp-mobile-expert, UI implementation → use @compose-screen or @swiftui-screen, Architecture decisions → use @kmp-architecture

**Conditional Loading**:
| Task | Reference | Load When |
|------|-----------|-----------|
| Domain model design | [domain-models.md](references/domain-models.md) | Creating data classes |
| Use case guidelines | [use-cases.md](references/use-cases.md) | Deciding use case boundaries |
| Domain exceptions | [exceptions.md](references/exceptions.md) | Implementing business rule exceptions |

## Core Principles

### Pure Domain Models

Domain models are the source of truth inside the app:
- **Immutable**: `val` properties only, data classes
- **Pure**: No serialization annotations, no persistence concerns, no UI-specific fields
- **Platform-agnostic**: No Android/iOS classes, no Ktor, no SQL types
- **Testable**: Unit-testable without Android/iOS dependencies

```kotlin
// ✅ Pure domain model
data class Pokemon(
    val id: PokemonId,
    val name: String,
    val types: List<PokemonType> = emptyList(),
    val spriteUrl: String? = null,
)
```

### Module Placement

With vertical slicing, domain models live in feature modules:
- `:features:<feature>:api` → Public domain models other features need
- `:features:<feature>:data` → Private domain types
- `:core:domain:api` → Shared cross-feature domain types (if necessary)

### Repository Interfaces

Define repository interfaces in `:api` modules when cross-feature access is required:

```kotlin
// In :features:pokemon:api
interface PokemonRepository {
    suspend fun getPokemon(id: PokemonId): Either<RepoError, Pokemon>
    fun observePokemonList(): Flow<Either<RepoError, List<PokemonListItem>>>
}
```

## Use Cases: When to Create Them

**NEVER create pass-through use cases** that only call a single repository method.

**Create a use case ONLY when**:
1. Orchestrates 2+ repositories or data sources
2. Enforces business rules/authorization gates
3. Applies cross-cutting policies (rate limiting, retries, validation)
4. Transforms multiple inputs into a domain decision

```kotlin
// ❌ Empty pass-through - AVOID
class GetPokemonUseCase(private val repo: PokemonRepository) {
    suspend operator fun invoke(id: PokemonId) = repo.getPokemon(id)
}

// ✅ Call repository directly from ViewModel when no domain policy needed
class PokemonDetailViewModel(
    private val repo: PokemonRepository,
    scope: CoroutineScope,
) : ViewModel(scope) {
    fun load(id: PokemonId) = scope.launch {
        repo.getPokemon(id).fold(
            ifLeft = { /* handle error */ },
            ifRight = { /* update UI */ }
        )
    }
}

// ✅ Value-adding use case
class SubmitOrderUseCase(
    private val cartRepo: CartRepository,
    private val paymentRepo: PaymentRepository,
    private val inventoryRepo: InventoryRepository,
) {
    suspend operator fun invoke(): Either<RepoError, Receipt> = either {
        val cart = cartRepo.current().bind()
        ensure(cart.items.isNotEmpty()) { 
            RepoError.Unknown(IllegalStateException("Empty cart")) 
        }
        inventoryRepo.reserve(cart.items).bind()
        val receipt = paymentRepo.charge(cart.total).bind()
        receipt
    }
}
```

**Read full guidelines**: [use-cases.md](references/use-cases.md)

## Domain Exceptions

Use domain exceptions for control flow dictated by business rules:

```kotlin
// Thrown when operation requires signed-in user
class UnauthenticatedException : Exception("Authentication required")

// Thrown when operation requires active purchase/subscription
class PurchaseRequiredException : Exception("Purchase required")
```

Throw these when explicitly modeled by PRD/user flows. Reserve exceptions for truly exceptional or control-flow dictated cases.

**Read full details**: [exceptions.md](references/exceptions.md)

## Testing Domain Logic

Domain layer should be unit-testable with simple fakes:

```kotlin
class FakePokemonRepository : PokemonRepository {
    private val pokemon = mutableMapOf<PokemonId, Pokemon>()
    
    override suspend fun getPokemon(id: PokemonId): Either<RepoError, Pokemon> {
        return pokemon[id]?.right() ?: RepoError.NotFound.left()
    }
    
    fun addPokemon(p: Pokemon) { pokemon[p.id] = p }
}

class PokemonUseCaseTest : FunSpec({
    test("should reject empty cart") {
        val cartRepo = FakeCartRepository(emptyCart())
        val useCase = SubmitOrderUseCase(cartRepo, fakePayment(), fakeInventory())
        
        useCase().shouldBeLeft()
    }
})
```

## Related Skills

| Skill | Use For |
|-------|---------|
| @kmp-architecture | Module structure, vertical slicing, feature boundaries |
| @kmp-data-layer | Repository implementation, DTO mapping, error handling |
| @kmp-mobile-expert | ViewModel patterns, repository consumption |
| @kmp-presentation | UI state holders, presentation layer patterns |

## Documentation Sources

| Document | Purpose | Tokens |
|----------|---------|--------|
| [domain.md](../../../docs/tech/domain.md) | Original domain guidelines | ~1500 |
| [conventions.md](../../../docs/tech/conventions.md) | Architecture master reference | ~3000 |

**Internal references**:
- [domain-models.md](references/domain-models.md) - Model design patterns and examples
- [use-cases.md](references/use-cases.md) - When to create use cases (with anti-patterns)
- [exceptions.md](references/exceptions.md) - Domain exceptions reference

## Quick Reference

### Model Checklist

- [ ] `val` properties only (immutable)
- [ ] Sensible defaults for optional fields
- [ ] No `@Serializable`, no Room annotations
- [ ] No Android Context, no UI types
- [ ] Pure helper functions (side-effect-free)

### Use Case Decision Tree

```
Does the operation need business logic?
├── No → Call repository directly from ViewModel
└── Yes → Does it need 2+ repositories?
    ├── No → Can it be done in ViewModel?
    │   ├── Yes → Do it in ViewModel
    │   └── No → Create use case
    └── Yes → Create use case
```

### Error Handling

- **Repository boundary**: Convert transport/storage errors into `Either<RepoError, T>`
- **Domain layer**: Use sealed results for recoverable cases
- **Control flow**: Reserve exceptions for exceptional cases (auth, purchase gates)

### Validation Commands

```bash
# Run domain layer tests
./gradlew test --continue

# Check :api module structure
./gradlew :features:<feature>:api:dependencies --configuration commonMain
```

### Reference Implementation

Study `features/pokemonlist/api/Pokemon.kt` and `features/pokemonlist/data/PokemonRepository.kt` for domain model and repository interface patterns.
