# Property-Based Testing

Last Updated: February 6, 2026

Complete guide for property-based testing with Kotest in Kotlin Multiplatform.

## Quick Summary

Property-based testing verifies that code holds for ALL valid inputs, not just a few examples. Use `checkAll` and `Arb` to generate random test data and catch edge cases.

---

## Core Concept

**Example-Based Testing vs Property-Based Testing**

```kotlin
// ❌ Example-based - Tests specific values
"should format name correctly" {
    formatName("john") shouldBe "John"
    formatName("jane") shouldBe "Jane"
}

// ✅ Property-based - Tests ALL valid inputs
"should capitalize first letter for any name" {
    checkAll(Arb.string(1..50).filter { it.isNotBlank() }) { name ->
        val formatted = formatName(name)
        formatted.first().isUpperCase() shouldBe true
        formatted.substring(1) shouldBe name.substring(1).lowercase()
    }
}
```

---

## Basic Property Tests

### Single Parameter

```kotlin
"dto to domain preserves ID" {
    checkAll(Arb.int(1..1000)) { id ->
        val dto = PokemonSummaryDto("name", "url/$id/")
        val domain = dto.toDomain()

        domain.id shouldBe id
    }
}
```

### Multiple Parameters

```kotlin
"dto to domain preserves all fields" {
    checkAll(
        Arb.int(1..1000),
        Arb.string(1..50).filter { it.isNotBlank() },
        Arb.string(10..200)
    ) { id, name, imageUrl ->
        val dto = PokemonSummaryDto(name, imageUrl)
        val domain = dto.toDomain()

        domain.id shouldBe id
        domain.name shouldBe name
        domain.imageUrl shouldBe imageUrl
    }
}
```

### Using checkAll with Assertions

```kotlin
"round-trip preserves data" {
    checkAll(Arb.pokemon()) { original ->
        val dto = original.toDto()
        val roundTripped = dto.toDomain()

        roundTripped.id shouldBe original.id
        roundTripped.name shouldBe original.name
        roundTripped.imageUrl shouldBe original.imageUrl
    }
}
```

---

## Arbitraries (Arb)

### Built-in Arbitraries

```kotlin
// Integers
Arb.int(1..1000)              // Range
Arb.int()                      // Any int
Arb.positiveInt()              // Positive ints
Arb.negativeInt()              // Negative ints

// Strings
Arb.string(1..50)              // Length range
Arb.stringPattern("a{3}")      // Regex pattern
Arb.alphanumeric()              // Letters and numbers
Arb.alpha()                     // Letters only
Arb.numeric()                   // Numbers only
Arb.uuid()                      // UUID strings
Arb.email()                     // Email addresses
Arb.uri()                       // URIs

// Booleans
Arb.bool()                      // true or false

// Doubles/Floats
Arb.double(0.0..1.0)            // Range
Arb.positiveDouble()            // Positive doubles

// Lists
Arb.list(Arb.int(1..100), 0..10)  // 0-10 elements
Arb.list(Arb.int(1..100))          // Any size (bounded)
Arb.set(Arb.int(1..100), 0..10)    // Set (unique elements)

// Pairs/Triples
Arb.pair(Arb.string(), Arb.int())
Arb.triple(Arb.int(), Arb.string(), Arb.bool())

// Enums
Arb.enum<PokemonType>()

// Nullables
Arb nullable (Arb.int(1..100))    // Int?
```

### Filtering Arbitraries

```kotlin
// Filter out blank strings
Arb.string(1..50).filter { it.isNotBlank() }

// Filter even numbers
Arb.int(1..1000).filter { it % 2 == 0 }

// Filter valid IDs
Arb.int().filter { it > 0 }
```

### Mapping Arbitraries

```kotlin
// Transform arbitrary
Arb.int(1..1000).map { id ->
    "https://api.example.com/items/$id"
}

// Chain mapping
Arb.int(1..100)
    .map { it * 2 }
    .map { it.toString() }
    .map { "Value: $it" }
```

### Flat Mapping

```kotlin
// Create dependent arbitraries
Arb.int(1..10).flatMap { count ->
    Arb.list(Arb.int(1..100), count..count)
}

// Example: Generate list of exact size
Arb.int(0..10).flatMap { size ->
    Arb.list(Arb.string(1..20), size..size)
}
```

### Combining Arbitraries

```kotlin
// Use bind() to extract values
fun Arb.Companion.pokemon(): Arb<Pokemon> = arbitrary {
    val id = Arb.int(1..1000).bind()
    val name = Arb.string(1..20).bind()
    val imageUrl = Arb.stringPattern("https://example.com/{}.png").bind()

    Pokemon(id, name, imageUrl)
}
```

### Custom Arbitraries

```kotlin
// Simple custom arbitrary
fun Arb.Companion.customId(): Arb<Int> = arbitrary {
    Arb.int(1..10000).filter { it % 7 == 0 }.bind()
}

// Complex custom arbitrary
fun Arb.Companion.pokemonPage(): Arb<PokemonPage> = arbitrary {
    val count = Arb.int(20..50).bind()
    val pokemons = Arb.list(Arb.pokemon(), 0..20).bind()
    val hasMore = Arb.bool().bind()

    PokemonPage(pokemons, hasMore, count)
}

// Arbitrary with edge cases
fun Arb.Companion.pokemonWithTypes(): Arb<Pokemon> = arbitrary {
    val types = Arb.set(Arb.enum<PokemonType>(), 1..2).bind()
    val id = Arb.int(1..1000).bind()
    val name = Arb.string(1..20).bind()

    Pokemon(id, name, types)
}
```

---

## Property-Based Testing Patterns

### Mapper Testing (100% Property Tests)

```kotlin
class PokemonMappersTest : StringSpec({

    "dto to domain preserves all fields" {
        checkAll(
            Arb.int(1..1000),
            Arb.string(1..50).filter { it.isNotBlank() },
            Arb.string(10..200)
        ) { id, name, imageUrl ->
            val dto = PokemonSummaryDto(name, imageUrl)
            val domain = dto.toDomain()

            domain.id shouldBe id
            domain.name shouldBe name
            domain.imageUrl shouldBe imageUrl
        }
    }

    "should extract ID from any valid URL" {
        checkAll(Arb.int(1..10000)) { id ->
            val url = "https://pokeapi.co/api/v2/pokemon/$id/"
            val extractedId = extractIdFromUrl(url)

            extractedId shouldBe id
        }
    }

    "dto list to domain list preserves size" {
        checkAll(Arb.list(Arb.pokemonSummaryDto(), 0..100)) { dtos ->
            val domains = dtos.map { it.toDomain() }
            domains.size shouldBe dtos.size
        }
    }

    "round-trip mapping preserves data" {
        checkAll(Arb.pokemon()) { originalPokemon ->
            val dto = originalPokemon.toDto()
            val roundTripped = dto.toDomain()

            roundTripped.id shouldBe originalPokemon.id
            roundTripped.name shouldBe originalPokemon.name
            roundTripped.imageUrl shouldBe originalPokemon.imageUrl
        }
    }
})

// Custom arbitraries
fun Arb.Companion.pokemonSummaryDto(): Arb<PokemonSummaryDto> = arbitrary {
    PokemonSummaryDto(
        name = Arb.string(1..20).bind().lowercase(),
        url = "https://pokeapi.co/api/v2/pokemon/${Arb.int(1..1000).bind()}/"
    )
}

fun Arb.Companion.pokemon(): Arb<Pokemon> = arbitrary {
    Pokemon(
        id = Arb.int(1..1000).bind(),
        name = Arb.string(1..20).bind(),
        imageUrl = "https://example.com/${Arb.int(1..1000).bind()}.png"
    )
}
```

### Repository Property Tests (40-50% Coverage)

```kotlin
"should map all 4xx codes to Http error" {
    checkAll(Arb.int(400..499)) { code ->
        coEvery { mockApi.getPokemonList(any(), any()) } throws
            ClientRequestException(mockk { every { status.value } returns code })

        val result = repository.loadPage()

        result.shouldBeLeft { error ->
            when (code) {
                401 -> error shouldBe RepoError.Unauthorized
                404 -> error shouldBe RepoError.NotFound
                else -> {
                    error.shouldBeInstanceOf<RepoError.Http>()
                    error.code shouldBe code
                }
            }
        }
    }
}

"should preserve pokemon count through pagination" {
    checkAll(Arb.int(1..5), Arb.int(10..50)) { pages, pageSize ->
        val allPokemons = mutableListOf<Pokemon>()
        var offset = 0

        repeat(pages) { pageIndex ->
            val isLastPage = pageIndex == pages - 1
            coEvery { mockApi.getPokemonList(pageSize, offset) } returns
                PokemonListDto(
                    count = pages * pageSize,
                    next = if (isLastPage) null else "next",
                    previous = if (offset > 0) "prev" else null,
                    results = List(pageSize) { i ->
                        PokemonSummaryDto(
                            name = "pokemon${offset + i}",
                            url = "https://pokeapi.co/api/v2/pokemon/${offset + i + 1}/"
                        )
                    }
                )

            val result = repository.loadPage(offset = offset)
            result.shouldBeRight { page ->
                allPokemons.addAll(page.pokemons)
            }

            offset += pageSize
        }

        allPokemons.size shouldBe pages * pageSize
        allPokemons.distinctBy { it.id }.size shouldBe allPokemons.size
    }
}
```

### ViewModel Property Tests (30-40% Coverage)

```kotlin
"should transition Loading -> Content with any valid page" {
    checkAll(
        Arb.int(1..100),
        Arb.list(Arb.pokemon(), 1..50)
    ) { count, pokemons ->
        val testScope = TestScope()
        val mockRepo = mockk<PokemonListRepository>()
        val viewModel = PokemonListViewModel(mockRepo, testScope)

        coEvery { mockRepo.loadPage() } returns Either.Right(
            PokemonPage(
                pokemons = pokemons.toImmutableList(),
                hasMore = count > pokemons.size
            )
        )

        viewModel.uiState.test {
            awaitItem() shouldBe PokemonListUiState.Loading

            viewModel.onStart(TestLifecycleOwner())
            testScope.advanceUntilIdle()

            awaitItem().shouldBeInstanceOf<PokemonListUiState.Content> { state ->
                state.pokemons.size shouldBe pokemons.size
                state.hasMore shouldBe (count > pokemons.size)
            }

            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

---

## Property-Based Coverage Targets

| Code Type | Coverage Target | Example Tests |
|-----------|----------------|---------------|
| Mappers | 100% | Data preservation, field mapping, round-trip |
| Repositories | 40-50% | HTTP error ranges, ID extraction, pagination |
| ViewModels | 30-40% | State transitions, random data flows |
| Validators | 60-80% | Input validation, boundary conditions |

---

## Redundant Test Elimination

Property tests often make concrete tests obsolete. **Remove concrete tests fully covered by property tests.**

```kotlin
// ❌ REDUNDANT - Property test covers all 4xx codes
"should return Http error for 400" { /* specific test */ }
"should return Http error for 404" { /* specific test */ }
"should return Http error for 429" { /* specific test */ }

// ✅ KEEP - One property test replaces all three
"should map all 4xx codes to Http error" {
    checkAll(Arb.int(400..499)) { code ->
        // Tests ALL 4xx codes (100 scenarios)
    }
}
```

### Decision Matrix for Removing Tests

1. Does a property test cover this scenario? → **Remove concrete test**
2. Is this an edge case not covered by properties? → **Keep concrete test**
3. Does this test document important behavior? → **Keep but add comment**
4. Is this test redundant with another concrete test? → **Merge or remove**

---

## Property-Based Testing Anti-Patterns

### ❌ Don't Test Trivial Properties

```kotlin
// ❌ BAD - Tests obvious property
"a + b == b + a" {
    checkAll(Arb.int(), Arb.int()) { a, b ->
        (a + b) shouldBe (b + a)
    }
}

// ✅ GOOD - Tests meaningful property
"round-trip preserves data" {
    checkAll(Arb.pokemon()) { original ->
        val dto = original.toDto()
        val roundTripped = dto.toDomain()
        roundTripped.id shouldBe original.id
    }
}
```

### ❌ Don't Use Too Many Generations

```kotlin
// ❌ BAD - Too slow, not more value
checkAll(iterations = 10000) { id -> ... }

// ✅ GOOD - Default is 100, enough for most cases
checkAll { id -> ... }
```

### ❌ Don't Mix Concerns

```kotlin
// ❌ BAD - Tests multiple unrelated properties
"should preserve all data and format correctly" {
    checkAll(Arb.pokemon()) { pokemon ->
        // Test mapping
        pokemon.toDto().toDomain() shouldBe pokemon

        // Test formatting (unrelated!)
        formatPokemon(pokemon).length > 0 shouldBe true
    }
}

// ✅ GOOD - Separate tests
"round-trip preserves data" { ... }
"format produces valid output" { ... }
```

---

## Best Practices

### 1. Choose Meaningful Properties

Test properties that are:
- **Invariant**: Always true regardless of input
- **Round-trip**: Transformation + reverse = original
- **Idempotent**: f(f(x)) = f(x)
- **Preservation**: Key attributes maintained

### 2. Use Descriptive Property Names

```kotlin
// ✅ GOOD - Describes the property being tested
"dto to domain preserves all fields"
"should extract ID from any valid URL"

// ❌ BAD - Vague
"should work correctly"
"should be correct"
```

### 3. Provide Edge Cases in Generators

```kotlin
// Include boundary cases
Arb.int(1..1000)  // Includes 1 and 1000
Arb.list(Arb.int(), 0..10)  // Includes empty list

// Filter to exclude invalid cases
Arb.string(1..50).filter { it.isNotBlank() }
Arb.int().filter { it > 0 }
```

### 4. Combine Property and Example Tests

```kotlin
// Property test for general case
"should format any valid name" {
    checkAll(Arb.string(1..50).filter { it.isNotBlank() }) { name ->
        formatName(name).first().isUpperCase() shouldBe true
    }
}

// Example test for specific edge case
"should handle unicode characters" {
    formatName("josé") shouldBe "José"
    formatName("jörg") shouldBe "Jörg"
}
```

### 5. Use bind() Correctly

```kotlin
// ✅ GOOD - Extract values from arbitraries
fun Arb.Companion.complex(): Arb<Complex> = arbitrary {
    val id = Arb.int(1..1000).bind()
    val name = Arb.string(1..20).bind()
    val tags = Arb.set(Arb.string(1..10), 0..5).bind()

    Complex(id, name, tags)
}

// ❌ BAD - Not using bind()
fun Arb.Companion.complex(): Arb<Complex> = arbitrary {
    Complex(Arb.int(1..1000), ...).bind()
}
```

---

## Reference Implementations

- `features/pokemonlist/data/src/androidUnitTest/kotlin/.../PokemonMappersTest.kt` - 100% property tests
- `features/pokemonlist/data/src/androidUnitTest/kotlin/.../PokemonListRepositoryTest.kt` - 40% property tests
- `features/pokemonlist/presentation/src/androidUnitTest/kotlin/.../PokemonListViewModelTest.kt` - 30% property tests
