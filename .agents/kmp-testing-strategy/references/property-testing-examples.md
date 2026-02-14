# Property-Based Testing with Kotest

## When to Use Property-Based Tests

Use property-based tests to verify **invariants** and **transformation rules** that must hold across thousands of inputs.

**Required Coverage:**
- Mappers (DTO→Domain): **100% property test coverage** (MANDATORY)
- Repositories: **40-50%** property coverage (error mapping, pagination)
- ViewModels: **30-40%** property coverage (state transitions, random inputs)

**Project Target:** 40% property tests, 60% concrete tests (34 property tests = ~34,000 scenarios per run)

## Property Test Workflow

1. **Identify invariants** (data preservation, transformation rules, mathematical properties)
2. **Use appropriate `Arb` generators**: 
   - `Arb.int(range)` - Integer values within a range
   - `Arb.string(length)` - String values with specified length
   - `Arb.list()` - List collections
   - `.orNull()` - Nullable values
3. **Write property tests** with `checkAll()` for assertions or `forAll()` for boolean returns
4. **Default iterations = 1000** (do NOT override unless necessary)
5. **Use descriptive names** starting with "property:"
6. **Balance with concrete tests** (30-40% property, 60-70% concrete)
7. **Remove redundant concrete tests** covered by property tests
8. **Target: 100% property test coverage for mappers**

## Example: Mapper Property Tests

```kotlin
class PokemonMapperSpec : StringSpec({
    "property: mapper preserves all fields" {
        checkAll(Arb.pokemonDto()) { dto ->
            val domain = dto.toDomain()

            domain.id shouldBe dto.id
            domain.name shouldBe dto.name.lowercase()
            domain.imageUrl shouldContain dto.id.toString()
        }
    }
})
```

**Pattern:** Use custom `Arb` generators for domain DTOs. Check all fields are preserved and transformed correctly.

## Example: Repository Error Handling Property Tests

```kotlin
class PokemonListRepositoryTest : StringSpec({
    "property: HTTP error codes always produce Error state" {
        checkAll(Arb.int(400..599)) { errorCode ->
            coEvery { mockApi.getPokemonList(any(), any()) } throws
                ClientRequestException(
                    HttpResponse(
                        status = HttpStatusCode.fromValue(errorCode),
                        requestTime = null
                    ),
                    "HTTP error"
                )

            val result = repository.loadPage(20, 0)
            val error = result.shouldBeLeft()
            error shouldBeInstanceOf<RepoError.Http>()
            (error as RepoError.Http).code shouldBe errorCode
        }
    }
})
```

**Pattern:** Test error mapping across all possible HTTP error codes (400-599). Verifies the repository correctly maps exceptions to domain errors.

## Custom Arb Generators

Create custom generators for your DTOs to ensure realistic test data:

```kotlin
fun Arb.Companion.pokemonDto() = arbitrary {
    PokemonDto(
        id = Arb.int(1..1000).bind(),
        name = Arb.string(3..15).bind(),
        sprites = SpritesDto(
            frontDefault = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${Arb.int(1..1000).bind()}.png"
        ),
        types = Arb.list(Arb.pokemonTypeDto(), 1..3).bind()
    )
}
```

## Property Tests vs Concrete Tests

| Property Tests | Concrete Tests |
|----------------|----------------|
| Verify invariants across 1000+ inputs | Verify specific scenarios |
| Mapper data preservation | Repository success paths |
| Error code ranges | Known edge cases |
| State transition rules | User interaction flows |
| **Use when:** Rules that must hold universally | **Use when:** Specific business logic |

## Common Property Test Patterns

### Data Preservation (Mappers)
```kotlin
"property: all fields are mapped correctly" {
    checkAll(Arb.dto()) { dto ->
        val domain = dto.toDomain()
        // Assert all fields preserved/transformed
    }
}
```

### Error Mapping (Repositories)
```kotlin
"property: all HTTP errors map to RepoError.Http" {
    checkAll(Arb.int(400..599)) { code ->
        // Throw HTTP exception with code
        // Assert maps to RepoError.Http
    }
}
```

### State Transitions (ViewModels)
```kotlin
"property: any error during load transitions to Error state" {
    checkAll(Arb.repoError()) { error ->
        // Mock repository to return error
        // Assert ViewModel transitions to Error state
    }
}
```

## Guardrails

- **NEVER override default 1000 iterations** unless you have a specific reason
- **Use property tests in `androidUnitTest/`** for JVM + MockK support
- **Balance property tests with concrete tests** (30-40% property, 60-70% concrete)
- **Remove redundant concrete tests** after adding property test coverage
- **Name property tests with "property:" prefix** for easy identification
- **Mappers MUST have 100% property test coverage** (data integrity critical)
