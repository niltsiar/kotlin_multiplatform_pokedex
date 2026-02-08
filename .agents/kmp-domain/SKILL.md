---
name: kmp-domain
description: "Domain layer patterns for Kotlin Multiplatform: immutable data classes, use case guidelines, domain exceptions, and pure business logic. Use when: (1) Designing domain models and data classes, (2) Deciding when to create use cases vs calling repositories directly, (3) Implementing domain exceptions (UnauthenticatedException, PurchaseRequiredException), (4) Defining repository interfaces, (5) Writing unit-testable domain logic with fakes"
---

# KMP Domain Layer Skill

Domain layer patterns for pure, immutable, platform-agnostic business logic in Kotlin Multiplatform.

## When to Use

**MANDATORY**: Use this skill when:
- Designing pure domain models and immutable data classes.
- Deciding between creating use cases vs calling repositories directly.
- Implementing domain exceptions (e.g., `UnauthenticatedException`, `PurchaseRequiredException`).
- Defining repository interfaces in `:api` modules.
- Writing unit-testable domain logic with fakes.

**Do NOT use for**:
- Repository implementation details → use **@kmp-data-layer**
- ViewModel implementation → use **@kmp-presentation**
- UI screens or components → use **@compose-screen** or **@swiftui-screen**
- Architecture-level module planning → use **@kmp-architecture**

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

## Essential Workflows

### Workflow 1: Design Immutable Domain Models
To create stable and pure business logic models:
1. Define a `data class` with `val` properties for immutability.
2. Annotate with `@Serializable` if the model is used for navigation or transport.
3. Use `ImmutableList` or `ImmutableMap` from `kotlinx.collections.immutable`.
4. Keep models platform-agnostic (no Android/iOS types, no Ktor, no SQL concerns).
5. Add pure helper functions via extension functions for side-effect-free logic.

### Workflow 2: Decide When to Create Use Cases
Avoid overengineering by following the use case decision tree:
1. **Orchestration**: Does the operation need to coordinate 2+ repositories?
2. **Business Rules**: Does it enforce authorization gates or complex policies?
3. **Cross-Cutting**: Does it apply retry policies, transactions, or validation?
4. **Conclusion**: If none apply, call the repository directly from the ViewModel.

### Workflow 3: Define Domain Exceptions
Use exceptions for control flow dictated by business rules:
1. Identify scenarios explicitly modeled by PRD/user flows (e.g., `UnauthenticatedException`).
2. Define specific exception classes in the `:api` module.
3. Throw exceptions in repositories or use cases for exceptional control flow.
4. Catch and handle in the ViewModel to trigger UI navigation (e.g., show paywall).

## Critical Guardrails

1. **NEVER use mutable collections** → Use `ImmutableList` or `ImmutableMap`.
2. **NEVER put business logic in data classes** → Models are pure data; use pure extension functions.
3. **NEVER return Either from domain** → `Either<RepoError, T>` is for repositories; domain uses pure types or exceptions.
4. **NEVER create use cases for single repo calls** → Call repositories directly from ViewModels.
5. **NEVER leak serialization annotations to domain** → Domain models should stay pure unless required for navigation.
6. **NEVER use nullable domain models for optional states** → Use sealed classes for explicit domain states.
7. **NEVER place domain models in :core** unless used by 3+ independent features.

## Quick Reference

### Model Checklist

- [ ] `val` properties only (immutable)
- [ ] Sensible defaults for optional fields
- [ ] No `@Serializable` (unless for navigation), no Room annotations
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

## Troubleshooting Common Domain Model Issues

### Constructor Parameter Mismatches

**Symptom:**
```kotlin
Stat(name = "hp", value = 45)
// Error: No value parameter with type Int
```

**Cause:** Domain class has different parameters than expected.

**Solution:** Always verify domain class signatures:

```kotlin
// ✅ CORRECT domain classes (from api module)
data class Stat(
    val name: String,
    val baseStat: Int,  // NOT 'value'
    val effort: Int     // Required, not optional
)

data class TypeOfPokemon(
    val name: String,
    val slot: Int  // Required for ordering
)

data class Ability(
    val name: String,
    val isHidden: Boolean,
    val slot: Int  // Required for positioning
)
```

**Prevention:** Check `features/<feature>/api/src/commonMain/kotlin/.../domain/` for authoritative definitions.

---

## Cross-References

### Skills (by Category)

**Architecture**
| Skill | Purpose | Link |
|-------|---------|------|
| @kmp-architecture | Module structure and feature boundaries | [SKILL.md](../kmp-architecture/SKILL.md) |
| @kmp-critical-patterns | Quick reference for 6 core patterns | [SKILL.md](../kmp-critical-patterns/SKILL.md) |

**Layer Implementation**
| Skill | Purpose | Link |
|-------|---------|------|
| @kmp-data-layer | Repository implementation and DTO mapping | [SKILL.md](../kmp-data-layer/SKILL.md) |
| @kmp-presentation | ViewModels and UI state management | [SKILL.md](../kmp-presentation/SKILL.md) |
| @kmp-di | Koin patterns and DI wiring | [SKILL.md](../kmp-di/SKILL.md) |

**Testing**
| Skill | Purpose | Link |
|-------|---------|------|
| @kmp-testing-patterns | Kotest, MockK, and property testing | [SKILL.md](../kmp-testing-patterns/SKILL.md) |

### Documents
| Document | Purpose | Link |
|----------|---------|------|
| Architecture + conventions | Master architecture reference | [conventions.md](See @kmp-architecture skill for architecture patterns) |
| Domain layer guidelines | Domain layer guidelines | [domain.md](See @kmp-domain skill) |
| Product requirements | Feature acceptance criteria | [prd.md](../../docs/project/prd.md) |

**Internal references**:
- [domain-models.md](references/domain-models.md) - Model design patterns
- [use-cases.md](references/use-cases.md) - When to create use cases
- [exceptions.md](references/exceptions.md) - Domain exceptions reference
- [troubleshooting.md](references/troubleshooting.md) - Common domain model issues and solutions
