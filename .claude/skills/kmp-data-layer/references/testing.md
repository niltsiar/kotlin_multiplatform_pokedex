# Repository Testing

Comprehensive guide to testing repositories with Kotest, MockK, and Arrow Either.

## Table of Contents

1. [Test Setup](#test-setup)
2. [Success Path Testing](#success-path-testing)
3. [Error Path Testing](#error-path-testing)
4. [Property-Based Testing](#property-based-testing)
5. [Testing Patterns](#testing-patterns)
6. [Test Structure](#test-structure)

## Test Setup

### Dependencies

```kotlin
// build.gradle.kts
dependencies {
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.arrow)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
}
```

### Basic Test Structure

```kotlin
package com.example.features.jobs.data

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk

class JobRepositoryTest : StringSpec({
    
    lateinit var mockApi: JobApiService
    lateinit var repository: JobRepository
    
    beforeTest {
        mockApi = mockk()
        repository = JobRepository(mockApi)
    }
    
    afterTest {
        // Cleanup if needed
    }
})
```

## Success Path Testing

### Basic Success Test

```kotlin
"getJobs returns Right with jobs on success" {
    // Given
    val jobDto = JobDto(
        id = "1",
        title = "Engineer",
        company = "Acme"
    )
    coEvery { mockApi.getJobs() } returns listOf(jobDto)
    
    // When
    val result = repository.getJobs()
    
    // Then
    val jobs = result.shouldBeRight()
    jobs.size shouldBe 1
    jobs[0].id shouldBe "1"
    jobs[0].title shouldBe "Engineer"
}
```

### Testing Domain Mapping

```kotlin
"getPokemonById returns correctly mapped domain model" {
    // Given
    val dto = PokemonDetailDto(
        id = 25,
        name = "pikachu",
        height = 4,
        weight = 60,
        types = listOf(
            TypeSlotDto(1, NamedResourceDto("electric", "https://..."))
        ),
        stats = listOf(
            StatDto(35, NamedResourceDto("hp", "https://..."))
        ),
        abilities = listOf(
            AbilitySlotDto(false, NamedResourceDto("static", "https://..."))
        ),
        sprites = SpritesDto("https://.../25.png")
    )
    coEvery { mockApi.getPokemonById(25) } returns dto
    
    // When
    val result = repository.getPokemonById(25)
    
    // Then
    val pokemon = result.shouldBeRight()
    pokemon.name shouldBe "Pikachu"  // Capitalized
    pokemon.types.size shouldBe 1
    pokemon.types[0].name shouldBe "Electric"
    pokemon.imageUrl shouldBe "https://.../25.png"
}
```

### Testing Nested Structures

```kotlin
"getPokemonById maps nested structures correctly" {
    coEvery { mockApi.getPokemonById(any()) } returns PokemonDetailDto(
        id = 1,
        name = "bulbasaur",
        height = 7,
        weight = 69,
        types = listOf(
            TypeSlotDto(1, NamedResourceDto("grass", "...")),
            TypeSlotDto(2, NamedResourceDto("poison", "..."))
        ),
        stats = listOf(
            StatDto(45, NamedResourceDto("hp", "...")),
            StatDto(49, NamedResourceDto("attack", "...")),
            StatDto(49, NamedResourceDto("defense", "..."))
        ),
        abilities = listOf(
            AbilitySlotDto(false, NamedResourceDto("overgrow", "...")),
            AbilitySlotDto(true, NamedResourceDto("chlorophyll", "..."))
        ),
        sprites = SpritesDto(".../1.png")
    )
    
    val result = repository.getPokemonById(1)
    
    val pokemon = result.shouldBeRight()
    pokemon.types.map { it.name } shouldBe listOf("Grass", "Poison")
    pokemon.stats.size shouldBe 3
    pokemon.abilities.any { it.isHidden } shouldBe true
}
```

## Error Path Testing

### Network Error

```kotlin
"getJobs returns Network error on timeout" {
    // Given
    coEvery { mockApi.getJobs() } throws 
        TimeoutCancellationException("Connection timed out")
    
    // When
    val result = repository.getJobs()
    
    // Then
    result.shouldBeLeft() shouldBe RepoError.Network
}

"getJobs returns Network error on IOException" {
    coEvery { mockApi.getJobs() } throws 
        IOException("No connectivity")
    
    val result = repository.getJobs()
    
    result.shouldBeLeft() shouldBe RepoError.Network
}
```

### HTTP Error Testing

```kotlin
"getJobs returns Http error on 404" {
    // Given
    val mockResponse = mockk<HttpResponse> {
        coEvery { status } returns HttpStatusCode.NotFound
    }
    coEvery { mockApi.getJobs() } throws 
        ClientRequestException(mockResponse, "Not found")
    
    // When
    val result = repository.getJobs()
    
    // Then
    val error = result.shouldBeLeft()
    error.shouldBeInstanceOf<RepoError.Http>()
    error.code shouldBe 404
}

"getJobs returns Http error on 500" {
    val mockResponse = mockk<HttpResponse> {
        coEvery { status } returns HttpStatusCode.InternalServerError
    }
    coEvery { mockApi.getJobs() } throws 
        ServerResponseException(mockResponse, "Server error")
    
    val result = repository.getJobs()
    
    val error = result.shouldBeLeft()
    error.shouldBeInstanceOf<RepoError.Http>()
    error.code shouldBe 500
}
```

### Unauthorized Error

```kotlin
"getJobs returns Unauthorized on 401" {
    val mockResponse = mockk<HttpResponse> {
        coEvery { status } returns HttpStatusCode.Unauthorized
    }
    coEvery { mockApi.getJobs() } throws 
        ClientRequestException(mockResponse, "Unauthorized")
    
    val result = repository.getJobs()
    
    result.shouldBeLeft() shouldBe RepoError.Unauthorized
}
```

### Unknown Error

```kotlin
"getJobs returns Unknown error on unexpected exception" {
    coEvery { mockApi.getJobs() } throws 
        IllegalStateException("Unexpected error")
    
    val result = repository.getJobs()
    
    val error = result.shouldBeLeft()
    error.shouldBeInstanceOf<RepoError.Unknown>()
    error.cause.shouldBeInstanceOf<IllegalStateException>()
}
```

## Property-Based Testing

### HTTP Error Mapping

```kotlin
"should map all 4xx codes to Http error (except 401, 404)" {
    checkAll(Arb.int(400..499)) { code ->
        // Given
        val exception = ClientRequestException(
            mockk { every { status.value } returns code }
        )
        coEvery { mockApi.getJobs() } throws exception
        
        // When
        val result = repository.getJobs()
        
        // Then
        when (code) {
            401 -> result.shouldBeLeft() shouldBe RepoError.Unauthorized
            404 -> result.shouldBeLeft() shouldBe RepoError.NotFound
            else -> {
                val error = result.shouldBeLeft()
                error.shouldBeInstanceOf<RepoError.Http>()
                error.code shouldBe code
            }
        }
    }
}

"should map all 5xx codes to Http error" {
    checkAll(Arb.int(500..599)) { code ->
        val exception = ServerResponseException(
            mockk { every { status.value } returns code }
        )
        coEvery { mockApi.getJobs() } throws exception
        
        val result = repository.getJobs()
        
        val error = result.shouldBeLeft()
        error.shouldBeInstanceOf<RepoError.Http>()
        error.code shouldBe code
    }
}
```

### DTO Mapping Invariants

```kotlin
"mapping preserves id invariant" {
    checkAll(Arb.int(1..1000)) { id ->
        coEvery { mockApi.getPokemonById(id) } returns PokemonDetailDto(
            id = id,
            name = "pokemon$id",
            height = 10,
            weight = 100,
            types = emptyList(),
            stats = emptyList(),
            abilities = emptyList(),
            sprites = SpritesDto(null)
        )
        
        val result = repository.getPokemonById(id)
        
        val pokemon = result.shouldBeRight()
        pokemon.id shouldBe id
    }
}

"mapping preserves name length invariant" {
    checkAll(Arb.string(1..50, Codepoint.az())) { name ->
        coEvery { mockApi.getPokemonById(any()) } returns PokemonDetailDto(
            id = 1,
            name = name,
            height = 10,
            weight = 100,
            types = emptyList(),
            stats = emptyList(),
            abilities = emptyList(),
            sprites = SpritesDto(null)
        )
        
        val result = repository.getPokemonById(1)
        
        val pokemon = result.shouldBeRight()
        pokemon.name.length shouldBe name.length  // Capitalization preserves length
    }
}
```

## Testing Patterns

### Verify All Error Paths

```kotlin
class JobRepositoryErrorTest : StringSpec({
    val mockApi = mockk<JobApiService>()
    val repository = JobRepository(mockApi)
    
    "handles all error types" {
        val testCases = listOf(
            // Network errors
            TimeoutCancellationException("") to RepoError.Network,
            IOException("") to RepoError.Network,
            ConnectException("") to RepoError.Network,
            
            // HTTP errors
            createClientException(401) to RepoError.Unauthorized,
            createClientException(404) to RepoError.NotFound,
            createClientException(400) to RepoError.Http::class,
            createServerException(500) to RepoError.Http::class,
            
            // Unknown
            RuntimeException("") to RepoError.Unknown::class
        )
        
        testCases.forEach { (exception, expectedError) ->
            coEvery { mockApi.getJobs() } throws exception
            
            val result = repository.getJobs()
            
            when (expectedError) {
                is RepoError -> result.shouldBeLeft() shouldBe expectedError
                is KClass<*> -> result.shouldBeLeft().shouldBeInstanceOf(expectedError)
            }
        }
    }
})

private fun createClientException(code: Int): ClientRequestException {
    return ClientRequestException(
        mockk { every { status.value } returns code }
    )
}

private fun createServerException(code: Int): ServerResponseException {
    return ServerResponseException(
        mockk { every { status.value } returns code }
    )
}
```

### Testing Offline-First Repositories

```kotlin
class OfflineFirstRepositoryTest : StringSpec({
    val mockApi = mockk<JobApiService>()
    val mockDao = mockk<JobDao>()
    val repository = JobRepository(mockApi, mockDao)
    
    "stream emits from local database" {
        // Given
        val entities = listOf(
            JobEntity(id = "1", title = "Job 1"),
            JobEntity(id = "2", title = "Job 2")
        )
        coEvery { mockDao.observeAll() } returns flowOf(entities)
        
        // When
        val jobs = repository.stream().first()
        
        // Then
        jobs.size shouldBe 2
        jobs[0].title shouldBe "Job 1"
    }
    
    "refresh updates local database" {
        // Given
        val remoteJobs = listOf(JobDto(id = "1", title = "Remote Job"))
        coEvery { mockApi.getJobs() } returns remoteJobs
        coEvery { mockDao.replaceAll(any()) } just Runs
        
        // When
        val result = repository.refresh()
        
        // Then
        result.shouldBeRight()
        coVerify { mockDao.replaceAll(any()) }
    }
    
    "refresh returns error when API fails" {
        coEvery { mockApi.getJobs() } throws TimeoutCancellationException()
        
        val result = repository.refresh()
        
        result.shouldBeLeft() shouldBe RepoError.Network
    }
})
```

## Test Structure

### Given-When-Then Pattern

```kotlin
"descriptive test name explaining behavior" {
    // Given - setup
    val input = ...
    coEvery { mockApi.call() } returns ...
    
    // When - action
    val result = repository.method(input)
    
    // Then - assertions
    result.shouldBeRight()
    coVerify { mockApi.call() }
}
```

### Test Naming Conventions

| Pattern | Example |
|---------|---------|
| `method_returns_state_when_condition` | `getJobs_returnsRight_onSuccess` |
| `method_returns_errorType_on_errorCondition` | `getJobs_returnsNetwork_onTimeout` |
| `method_correctly_maps_specificField` | `getPokemonById_capitalizesName` |

### Test File Organization

```kotlin
class PokemonRepositoryTest : StringSpec({
    
    // === Setup ===
    lateinit var mockApi: PokemonApiService
    lateinit var repository: PokemonRepository
    
    beforeTest { ... }
    
    // === Success Path ===
    "getPokemonById returns Right on success" { ... }
    "getPokemonById correctly maps domain model" { ... }
    "getPokemonById maps nested structures" { ... }
    
    // === Error Path ===
    "getPokemonById returns Network on timeout" { ... }
    "getPokemonById returns Http on 404" { ... }
    "getPokemonById returns Unauthorized on 401" { ... }
    
    // === Property-Based ===
    "mapping preserves id invariant" { ... }
})
```

## Kotest Either Assertions

### Available Assertions

```kotlin
// Assert Right value
result.shouldBeRight()

// Assert Right and extract value
val jobs = result.shouldBeRight()

// Assert Right with lambda
result.shouldBeRight { jobs ->
    jobs.size shouldBe 1
}

// Assert Left value
result.shouldBeLeft()

// Assert Left and extract value
val error = result.shouldBeLeft()

// Assert Left with lambda
result.shouldBeLeft { error ->
    error.shouldBeInstanceOf<RepoError.Network>()
}

// Smart casting with shouldBeInstanceOf
val error = result.shouldBeLeft()
error.shouldBeInstanceOf<RepoError.Http>()
error.code shouldBe 404  // Smart cast works!
```

### Smart Casting Example

```kotlin
val result = repository.getJobs()

val error = result.shouldBeLeft()
error.shouldBeInstanceOf<RepoError.Http>()

// No manual cast needed - smart cast works!
error.code shouldBe 404      // Compiles!
error.message shouldBe "..." // Compiles!
```
