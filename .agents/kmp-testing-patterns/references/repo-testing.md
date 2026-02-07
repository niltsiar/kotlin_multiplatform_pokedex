# Repository Testing

Last Updated: February 6, 2026

Complete guide for testing repositories with Kotest, MockK, and Either in Kotlin Multiplatform.

## Quick Summary

Repository tests verify data fetching, error handling, and DTO-to-domain mapping. Test success path + ALL error paths.

---

## Test Structure

### Basic Setup

```kotlin
class PokemonListRepositoryTest : StringSpec({
    lateinit var mockApi: PokemonListApiService
    lateinit var repository: PokemonListRepository

    beforeTest {
        mockApi = mockk()
        repository = PokemonListRepository(mockApi)
    }
})
```

**Key Points:**
- Mock all API services
- Repository is test subject
- Test returns `Either<RepoError, T>`
- Test success + ALL error paths

---

## Success Path Testing

### Basic Success

```kotlin
"should return Right with page on success" {
    coEvery { mockApi.getPokemonList(20, 0) } returns PokemonListDto(
        count = 1292,
        next = "https://pokeapi.co/api/v2/pokemon?offset=20&limit=20",
        previous = null,
        results = listOf(
            PokemonSummaryDto("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/"),
            PokemonSummaryDto("ivysaur", "https://pokeapi.co/api/v2/pokemon/2/")
        )
    )

    val result = repository.loadPage()

    result.shouldBeRight { page ->
        page.pokemons shouldHaveSize 2
        page.pokemons.first().name shouldBe "Bulbasaur"
        page.hasMore shouldBe true
    }
}
```

### Data Preservation

```kotlin
"should preserve pokemon data through mapping" {
    val dto = PokemonListDto(
        count = 2,
        next = "next",
        previous = "prev",
        results = listOf(
            PokemonSummaryDto("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/")
        )
    )

    coEvery { mockApi.getPokemonList(20, 0) } returns dto

    val result = repository.loadPage()

    result.shouldBeRight { page ->
        page.pokemons.first().id shouldBe 1
        page.pokemons.first().name shouldBe "Bulbasaur"
    }
}
```

---

## Error Path Testing

### Network Error

```kotlin
"should return Network error on timeout" {
    coEvery { mockApi.getPokemonList(any(), any()) } throws
        ConnectTimeoutException("Connection timed out")

    val result = repository.loadPage()

    result.shouldBeLeft { error ->
        error shouldBe RepoError.Network
    }
}
```

### IOException

```kotlin
"should return Network error on IOException" {
    coEvery { mockApi.getPokemonList(any(), any()) } throws
        IOException("Network error")

    val result = repository.loadPage()

    result.shouldBeLeft { error ->
        error shouldBe RepoError.Network
    }
}
```

### HTTP Errors

#### 401 Unauthorized

```kotlin
"should return Unauthorized on 401" {
    coEvery { mockApi.getPokemonList(any(), any()) } throws
        ClientRequestException(
            mockk { every { status.value } returns 401 }
        )

    val result = repository.loadPage()

    result.shouldBeLeft { error ->
        error shouldBe RepoError.Unauthorized
    }
}
```

#### 404 Not Found

```kotlin
"should return NotFound on 404" {
    coEvery { mockApi.getPokemonList(any(), any()) } throws
        ClientRequestException(
            mockk { every { status.value } returns 404 }
        )

    val result = repository.loadPage()

    result.shouldBeLeft { error ->
        error shouldBe RepoError.NotFound
    }
}
```

#### Generic HTTP Error

```kotlin
"should return Http error with code and message" {
    coEvery { mockApi.getPokemonList(any(), any()) } throws
        ClientRequestException(
            mockk {
                every { status.value } returns 429
                every { message } returns "Rate limit exceeded"
            }
        )

    val result = repository.loadPage()

    result.shouldBeLeft { error ->
        error.shouldBeInstanceOf<RepoError.Http>()
        error.code shouldBe 429
        error.message shouldBe "Rate limit exceeded"
    }
}
```

### Server Error

```kotlin
"should return Http error on 500" {
    coEvery { mockApi.getPokemonList(any(), any()) } throws
        ServerResponseException(
            mockk { every { status.value } returns 500 }
        )

    val result = repository.loadPage()

    result.shouldBeLeft { error ->
        error.shouldBeInstanceOf<RepoError.Http>()
        error.code shouldBe 500
    }
}
```

### Unknown Error

```kotlin
"should return Unknown error on unexpected exception" {
    coEvery { mockApi.getPokemonList(any(), any()) } throws
        IllegalStateException("Unexpected state")

    val result = repository.loadPage()

    result.shouldBeLeft { error ->
        error.shouldBeInstanceOf<RepoError.Unknown>()
    }
}
```

---

## Property-Based Repository Testing

### HTTP Error Range

```kotlin
"should map all 4xx codes to Http error" {
    checkAll(Arb.int(400..499)) { code ->
        coEvery { mockApi.getPokemonList(any(), any()) } throws
            ClientRequestException(
                mockk { every { status.value } returns code }
            )

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
```

### Pagination Preservation

```kotlin
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

### ID Extraction

```kotlin
"should extract ID from any valid URL" {
    checkAll(Arb.int(1..10000)) { id ->
        val url = "https://pokeapi.co/api/v2/pokemon/$id/"
        val extractedId = extractIdFromUrl(url)

        extractedId shouldBe id
    }
}
```

---

## Mocking API Responses

### Simple Response

```kotlin
coEvery { mockApi.getPokemonList(20, 0) } returns PokemonListDto(
    count = 1292,
    next = "next",
    previous = null,
    results = listOf(summaryDto)
)
```

### Multiple Responses

```kotlin
coEvery { mockApi.getPokemonList(any(), any()) } returnsMany listOf(
    page1,
    page2,
    page3
)
```

### Response With Delay

```kotlin
coEvery { mockApi.getPokemonList(any(), any()) } coAnswers {
    delay(100)
    mockPage
}
```

---

## Verification

### Verify API Calls

```kotlin
"should call API with correct parameters" {
    coEvery { mockApi.getPokemonList(20, 0) } returns mockDto

    repository.loadPage(limit = 20, offset = 0)

    coVerify { mockApi.getPokemonList(20, 0) }
}
```

### Verify Call Count

```kotlin
"should call API exactly once" {
    coEvery { mockApi.getPokemonList(any(), any()) } returns mockDto

    repository.loadPage()

    coVerify(exactly = 1) { mockApi.getPokemonList(any(), any()) }
}
```

### Verify Call Order

```kotlin
"should call APIs in correct order" {
    coEvery { mockApi.getPokemonList(20, 0) } returns page1
    coEvery { mockApi.getPokemonDetails(1) } returns details

    repository.loadPokemonWithDetails(1)

    verifyOrder {
        mockApi.getPokemonList(20, 0)
        mockApi.getPokemonDetails(1)
    }
}
```

---

## Cache Testing

### Cache Hit

```kotlin
"should return cached data on cache hit" {
    val cachedData = Pokemon(1, "Bulbasaur", "url")
    repository.cache[1] = cachedData

    val result = repository.getById(1)

    result.shouldBeRight { it shouldBe cachedData }
    coVerify(exactly = 0) { mockApi.getPokemonDetails(1) }
}
```

### Cache Miss

```kotlin
"should fetch from API on cache miss" {
    coEvery { mockApi.getPokemonDetails(1) } returns dto

    val result = repository.getById(1)

    result.shouldBeRight { it.id shouldBe 1 }
    coVerify { mockApi.getPokemonDetails(1) }
}
```

### Cache Update

```kotlin
"should update cache on fetch" {
    coEvery { mockApi.getPokemonDetails(1) } returns dto

    repository.getById(1)

    repository.cache[1] shouldBe domain
}
```

---

## Offline-First Repository Testing

### Stream Testing

```kotlin
"should stream pokemons from local database" {
    val pokemons = listOf(
        PokemonEntity(1, "Bulbasaur"),
        PokemonEntity(2, "Ivysaur")
    )
    every { mockDao.observeAll() } returns MutableStateFlow(pokemons)

    val flow = repository.stream()

    flow.test {
        val items = awaitItem()
        items.size shouldBe 2
        items.first().name shouldBe "Bulbasaur"

        cancelAndIgnoreRemainingEvents()
    }
}
```

### Refresh Testing

```kotlin
"should refresh data from API" {
    coEvery { mockApi.getPokemonList(any(), any()) } returns dto

    val result = repository.refresh()

    result.shouldBeRight()
    coVerify { mockDao.replaceAll(any()) }
}
```

---

## Mapper Testing Integration

### Repository Calls Mapper

```kotlin
"should map DTO to domain correctly" {
    val dto = PokemonListDto(
        count = 1,
        next = null,
        previous = null,
        results = listOf(
            PokemonSummaryDto("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/")
        )
    )

    coEvery { mockApi.getPokemonList(20, 0) } returns dto

    val result = repository.loadPage()

    result.shouldBeRight { page ->
        page.pokemons.first().id shouldBe 1
        page.pokemons.first().name shouldBe "Bulbasaur"
    }
}
```

---

## Best Practices

### 1. Test All Error Paths

```kotlin
// Success
"should return Right on success" { ... }

// Network errors
"should return Network error on timeout" { ... }
"should return Network error on IOException" { ... }

// HTTP errors
"should return Unauthorized on 401" { ... }
"should return NotFound on 404" { ... }
"should return Http error on 429" { ... }

// Unknown errors
"should return Unknown error on unexpected exception" { ... }
```

### 2. Use Property Tests for Ranges

```kotlin
// ❌ BAD - Tests individual codes
"should return error for 400" { ... }
"should return error for 401" { ... }
// ... 97 more tests

// ✅ GOOD - Tests entire range
"should map all 4xx codes to Http error" {
    checkAll(Arb.int(400..499)) { code -> ... }
}
```

### 3. Use Smart Casting

```kotlin
// ✅ GOOD
result.shouldBeLeft { error ->
    error shouldBe RepoError.Network
}

// ❌ BAD
val error = result.shouldBeLeft()
error shouldBe RepoError.Network
```

### 4. Verify API Calls

```kotlin
coVerify { mockApi.getPokemonList(20, 0) }
coVerify(exactly = 1) { mockApi.getPokemonList(any(), any()) }
```

### 5. Use Descriptive Test Names

```kotlin
// ✅ GOOD
"should return Network error on timeout"
"should map all 4xx codes to Http error"

// ❌ BAD
"test error"
"should work"
```

---

## Anti-Patterns

### ❌ Don't Test DTO Objects

```kotlin
// ❌ WRONG - DTO is not repository responsibility
"should create DTO correctly" {
    val dto = PokemonListDto(...)
    dto.count shouldBe 1
}

// ✅ CORRECT - Test repository behavior with DTOs
"should map DTO to domain" {
    coEvery { mockApi.getPokemonList(any(), any()) } returns dto
    val result = repository.loadPage()
    result.shouldBeRight { ... }
}
```

### ❌ Don't Ignore Return Values

```kotlin
// ❌ WRONG
result.shouldBeRight()
val page = result.getOrNull()

// ✅ CORRECT
val page = result.shouldBeRight()
```

### ❌ Don't Test Implementation Details

```kotlin
// ❌ WRONG - Tests internal mapping logic
"should call mapper" {
    coVerify { mapper.toDomain(any()) }
}

// ✅ CORRECT - Tests observable behavior
"should return correct domain data" {
    result.shouldBeRight { it.name shouldBe "Bulbasaur" }
}
```

---

## Reference Implementations

- `features/pokemonlist/data/src/androidUnitTest/kotlin/.../PokemonListRepositoryTest.kt`
- `features/pokemondetail/data/src/androidUnitTest/kotlin/.../PokemonDetailRepositoryTest.kt`
