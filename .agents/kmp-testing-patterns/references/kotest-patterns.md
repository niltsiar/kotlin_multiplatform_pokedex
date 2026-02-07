# Kotest Patterns

Last Updated: February 6, 2026

Complete guide for Kotest testing patterns, assertions, and matchers in Kotlin Multiplatform.

## Quick Summary

Kotest is the primary testing framework for JVM-based tests in this project (androidUnitTest). Use StringSpec style for test organization and leverage matchers for assertions.

---

## Test Structure Patterns

### StringSpec Style (Primary)

```kotlin
class PokemonListRepositoryTest : StringSpec({
    "should return Right on success" {
        // Test code
    }

    "should return Network error on timeout" {
        // Test code
    }
})
```

**Why StringSpec?**
- Descriptive test names in natural language
- No boilerplate - just test strings
- Readable and self-documenting
- Standard across the project

### Test Lifecycle Hooks

```kotlin
class MyRepositoryTest : StringSpec({
    lateinit var mockApi: MyApiService
    lateinit var repository: MyRepository

    beforeTest {
        // Runs before each test
        mockApi = mockk()
        repository = MyRepository(mockApi)
    }

    afterTest {
        // Runs after each test
        // Cleanup if needed
    }

    beforeSpec {
        // Runs once before all tests in this spec
        // Expensive setup
    }

    afterSpec {
        // Runs once after all tests in this spec
        // Cleanup
    }
})
```

---

## Core Assertions

### shouldBe / shouldNotBe

```kotlin
"should assert equality" {
    val result = calculate(2, 3)
    result shouldBe 5
    result shouldNotBe 0
}

"should assert same instance" {
    val list = mutableListOf(1, 2, 3)
    val reference = list
    reference shouldBeSameInstanceAs list
}
```

### shouldBeTrue / shouldBeFalse

```kotlin
"should assert boolean" {
    val isValid = validate(input)
    isValid shouldBe true
    isValid shouldBeTrue
}
```

### shouldBeNull / shouldNotBeNull

```kotlin
"should assert nullability" {
    val result: String? = findUser(1)
    result.shouldBeNull()
}

"should assert non-null with smart cast" {
    val result: String? = findUser(1)
    result.shouldNotBeNull()
    // Smart cast: result is now String
    result.length shouldBe 10
}
```

### Collection Matchers

```kotlin
"should assert collection size" {
    val items = listOf(1, 2, 3)
    items shouldHaveSize 3
}

"should contain element" {
    val items = listOf("apple", "banana", "cherry")
    items shouldContain "banana"
}

"should contain all elements" {
    val items = listOf(1, 2, 3, 4, 5)
    items shouldContainAll listOf(2, 4)
}

"should be empty" {
    val items = emptyList<Int>()
    items shouldBeEmpty()
}
```

### Exception Testing

```kotlin
"should throw exception" {
    shouldThrow<IllegalArgumentException> {
        invalidOperation()
    }
}

"should throw exception with message" {
    val exception = shouldThrow<IllegalArgumentException> {
        invalidOperation()
    }
    exception.message shouldBe "Invalid argument"
}
```

---

## Type Assertions with Smart Casting

### shouldBeInstanceOf

```kotlin
"should assert type with smart cast" {
    val state: Any = PokemonListUiState.Content(listOf(pokemon))

    state.shouldBeInstanceOf<PokemonListUiState.Content>()
    // Smart cast: state is now PokemonListUiState.Content
    state.pokemons.size shouldBe 1
}
```

### shouldBeTypeOf

```kotlin
"should assert exact type" {
    val animal: Animal = Dog("Buddy")

    animal.shouldBeTypeOf<Dog>()
    // Smart cast: animal is now Dog
    animal.bark()
}
```

---

## Arrow Either Assertions

### shouldBeRight / shouldBeLeft

```kotlin
"should assert Right value" {
    val result: Either<RepoError, Pokemon> = repository.getById(1)

    val pokemon = result.shouldBeRight()
    // pokemon is extracted and typed as Pokemon
    pokemon.id shouldBe 1
    pokemon.name shouldBe "Bulbasaur"
}

"should assert Left value" {
    val result: Either<RepoError, Pokemon> = repository.getById(999)

    val error = result.shouldBeLeft()
    // error is extracted and typed as RepoError
    error shouldBe RepoError.NotFound
}
```

### shouldBeRight with lambda

```kotlin
"should assert Right with assertions in lambda" {
    val result = repository.loadPage()

    result.shouldBeRight { page ->
        page.pokemons shouldHaveSize 20
        page.hasMore shouldBe true
        page.pokemons.first().name shouldBe "Bulbasaur"
    }
}

"should assert Left with assertions in lambda" {
    val result = repository.loadPage()

    result.shouldBeLeft { error ->
        error shouldBe RepoError.Network
        error.message shouldBe "Connection timeout"
    }
}
```

---

## Soft Assertions

```kotlin
"should use soft assertions for multiple checks" {
    val pokemon = Pokemon(1, "Bulbasaur", "url")

    assertSoftly {
        pokemon.id shouldBe 1
        pokemon.name shouldBe "Bulbasaur"
        pokemon.imageUrl shouldContain "url"
    }
    // All assertions run even if one fails
    // Shows all failures at once
}
```

---

## Parameterized Tests with checkAll

### Basic Property Test

```kotlin
"should work for any valid ID" {
    checkAll(Arb.int(1..1000)) { id ->
        val result = repository.getById(id)
        result.shouldBeRight { it.id shouldBe id }
    }
}
```

### Multiple Parameters

```kotlin
"should preserve all fields through mapping" {
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

### Custom Arbitraries

```kotlin
"should use custom arbitrary" {
    checkAll(Arb.pokemon()) { pokemon ->
        val result = repository.save(pokemon)
        result.shouldBeRight()
    }
}

// Custom arbitrary definition
fun Arb.Companion.pokemon(): Arb<Pokemon> = arbitrary {
    Pokemon(
        id = Arb.int(1..1000).bind(),
        name = Arb.string(1..20).bind(),
        imageUrl = Arb.stringPattern("https://example.com/{}.png").bind()
    )
}
```

---

## Kotest Configuration

### Timeout

```kotlin
class LongRunningTest : StringSpec({
    // Configure timeout for this spec
    timeout = 5_000 // 5 seconds

    "long running operation" {
        // Test that might take time
    }
})
```

### Tags

```kotlin
class TaggedTest : StringSpec({
    tags(setOf(IntegrationTest))

    "integration test" {
        // Test that runs only with IntegrationTest tag
    }
})
```

---

## Test Scope

### IsolationMode

Kotest uses `InstancePerTest` by default - each test gets a new instance of the test class.

```kotlin
class IsolatedTest : StringSpec({
    val list = mutableListOf(1) // Fresh for each test

    "test 1" {
        list.add(2)
        list shouldBe listOf(1, 2)
    }

    "test 2" {
        // list is fresh - still just [1]
        list shouldBe listOf(1)
    }
})
```

---

## Best Practices

### 1. Descriptive Test Names

```kotlin
// ✅ GOOD
"should return Network error on timeout"

// ❌ BAD
"test1"
```

### 2. Arrange-Act-Assert

```kotlin
"should load pokemons correctly" {
    // Arrange
    val expectedPokemon = Pokemon(1, "Bulbasaur", "url")
    coEvery { mockApi.getPokemonList(20, 0) } returns mockDto

    // Act
    val result = repository.loadPage()

    // Assert
    result.shouldBeRight { page ->
        page.pokemons.first() shouldBe expectedPokemon
    }
}
```

### 3. Use Smart Casting

```kotlin
// ✅ GOOD - Use smart casting
state.shouldBeInstanceOf<Content>()
state.pokemons.size shouldBe 10

// ❌ BAD - Manual cast
state.shouldBeInstanceOf<Content>()
val content = state as Content
content.pokemons.size shouldBe 10
```

### 4. Use Either Assertions Return Values

```kotlin
// ✅ GOOD - Use return value
val pokemon = result.shouldBeRight()
pokemon.id shouldBe 1

// ❌ BAD - Ignore return value
result.shouldBeRight()
val pokemon = result.getOrNull()
pokemon!!.id shouldBe 1
```

---

## Common Pitfalls

### Don't Use Thread.sleep

```kotlin
// ❌ WRONG
viewModel.load()
Thread.sleep(100) // Flaky!
viewModel.uiState.value.shouldBeInstanceOf<Content>()

// ✅ CORRECT
viewModel.uiState.test {
    viewModel.load()
    testScope.advanceUntilIdle()
    awaitItem().shouldBeInstanceOf<Content>()
    cancelAndIgnoreRemainingEvents()
}
```

### Don't Mock Final Classes

```kotlin
// ❌ WRONG - Kotlin classes are final by default
val mock = mockk<MyService>() // Error!

// ✅ CORRECT - Open the class or use an interface
interface MyService { ... }
val mock = mockk<MyService>()
```

---

## Reference Implementations

- `features/pokemonlist/data/src/androidUnitTest/kotlin/.../PokemonListRepositoryTest.kt`
- `features/pokemonlist/presentation/src/androidUnitTest/kotlin/.../PokemonListViewModelTest.kt`
- `features/pokemonlist/data/src/androidUnitTest/kotlin/.../PokemonMappersTest.kt`
