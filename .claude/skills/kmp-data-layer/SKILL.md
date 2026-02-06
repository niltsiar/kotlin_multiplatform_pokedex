---
name: kmp-data-layer
description: Data layer patterns for Kotlin Multiplatform - repository implementation with Arrow Either, error handling, and DTO mapping. Use when implementing repositories, defining RepoError hierarchies, mapping DTOs to domain, or testing data layers. Covers Either<RepoError,T> boundaries, sealed error classes, exception mapping, and repository testing patterns.
---

# KMP Data Layer Patterns

Repository implementation patterns with Arrow Either for type-safe error handling in Kotlin Multiplatform.

## Core Principle

**Repositories return `Either<RepoError, T>` at boundaries. NEVER throw, return null, or use `Result`.**

## Quick Reference

### 1. RepoError Hierarchy

```kotlin
// :features:<feature>:api - Public contract
sealed interface RepoError {
    data object Network : RepoError
    data class Http(val code: Int, val message: String?) : RepoError
    data object NotFound : RepoError
    data object Unauthorized : RepoError
    data class Unknown(val cause: Throwable) : RepoError
}
```

### 2. Repository Pattern (Impl + Factory)

```kotlin
// :features:jobs:data - Implementation
internal class JobRepositoryImpl(
    private val api: JobApiService
) : JobRepository {
    
    override suspend fun getJobs(): Either<RepoError, List<Job>> =
        Either.catch {
            api.getJobs().map { it.asDomain() }
        }.mapLeft { it.toRepoError() }
}

// Factory function (top-level, returns interface)
fun JobRepository(api: JobApiService): JobRepository = JobRepositoryImpl(api)
```

### 3. Exception Mapping

```kotlin
// Extension function for error mapping
fun Throwable.toRepoError(): RepoError = when (this) {
    is ClientRequestException -> when (response.status.value) {
        401 -> RepoError.Unauthorized
        404 -> RepoError.NotFound
        in 400..499 -> RepoError.Http(response.status.value, message)
        else -> RepoError.Unknown(this)
    }
    is ServerResponseException -> RepoError.Http(response.status.value, message)
    is IOException, is TimeoutCancellationException -> RepoError.Network
    else -> RepoError.Unknown(this)
}
```

### 4. DTO to Domain Mapping

```kotlin
// Extension functions in data layer
internal fun JobDto.asDomain(): Job = Job(
    id = id,
    title = title,
    description = description
)

internal fun JobEntity.asDomain(): Job = Job(
    id = id,
    title = title,
    description = description
)
```

### 5. Offline-First Pattern

```kotlin
interface JobRepository {
    fun stream(): Flow<List<Job>>  // Local DB as SSoT
    suspend fun refresh(): Either<RepoError, Unit>  // Network update
}

internal class JobRepositoryImpl(
    private val api: JobApiService,
    private val dao: JobDao
) : JobRepository {
    
    override fun stream(): Flow<List<Job>> = 
        dao.observeAll().map { list -> list.map(JobEntity::asDomain) }
    
    override suspend fun refresh(): Either<RepoError, Unit> = Either.catch {
        val response = api.getJobs()
        dao.replaceAll(response.map(JobEntity::from))
    }.mapLeft { it.toRepoError() }
}
```

### 6. Testing with Kotest

```kotlin
class JobRepositoryTest : StringSpec({
    "getJobs returns Right on success" {
        coEvery { mockApi.getJobs() } returns listOf(jobDto)
        
        val result = repository.getJobs()
        
        val jobs = result.shouldBeRight()
        jobs.size shouldBe 1
    }
    
    "getJobs returns Network error on timeout" {
        coEvery { mockApi.getJobs() } throws TimeoutCancellationException()
        
        val result = repository.getJobs()
        
        result.shouldBeLeft() shouldBe RepoError.Network
    }
    
    "getJobs returns Http error on 404" {
        coEvery { mockApi.getJobs() } throws 
            ClientRequestException(mockResponse { status = 404 })
        
        val result = repository.getJobs()
        
        val error = result.shouldBeLeft()
        error.shouldBeInstanceOf<RepoError.Http>()
        error.code shouldBe 404
    }
})
```

## Module Structure

```
:features:<feature>:api
├── JobRepository.kt          # Interface + RepoError
└── domain/Job.kt             # Domain model

:features:<feature>:data
├── JobRepositoryImpl.kt      # Implementation
├── JobRepository.kt          # Factory function
├── mappers/
│   └── JobMappers.kt         # DTO/Entity → Domain
├── dto/
│   └── JobDto.kt             # API DTOs
└── entity/
    └── JobEntity.kt          # DB entities
```

## When to Use References

- **Creating new repository**: Read [references/repository-pattern.md](references/repository-pattern.md)
- **Defining error hierarchies**: Read [references/error-handling.md](references/error-handling.md)
- **DTO mapping patterns**: Read [references/dto-mapping.md](references/dto-mapping.md)
- **Testing repositories**: Read [references/testing.md](references/testing.md)

## Critical Patterns

### NEVER Return Result
```kotlin
// ❌ WRONG - Using Kotlin Result
suspend fun getJobs(): Result<List<Job>>

// ✅ CORRECT - Using Arrow Either
suspend fun getJobs(): Either<RepoError, List<Job>>
```

### NEVER Swallow Cancellation
```kotlin
// ❌ WRONG - Either.catch respects cancellation
override suspend fun getJobs(): Either<RepoError, List<Job>> =
    Either.catch {
        api.getJobs().map { it.asDomain() }
    }.mapLeft { it.toRepoError() }
```

### NEVER Leak DTOs
```kotlin
// ❌ WRONG - Exposing DTO in interface
interface JobRepository {
    suspend fun getJobs(): Either<RepoError, List<JobDto>>
}

// ✅ CORRECT - Map to domain
interface JobRepository {
    suspend fun getJobs(): Either<RepoError, List<Job>>
}
```

### NEVER Expose Implementation Classes
```kotlin
// ❌ WRONG - Public implementation class
class JobRepositoryImpl : JobRepository

// ✅ CORRECT - Internal impl, factory function
internal class JobRepositoryImpl : JobRepository
fun JobRepository(api: JobApiService): JobRepository = JobRepositoryImpl(api)
```

## Related Skills

- **@kmp-architecture** - Module structure and vertical slicing
- **@kmp-domain** - Domain model design and contracts
- **@kmp-api-services** - API service implementation (Ktor, DTOs)

## API Service vs Repository

| Responsibility | API Service | Repository |
|---------------|-------------|------------|
| HTTP calls | ✅ | ❌ |
| Return type | DTOs | Domain models |
| Error handling | Throws exceptions | Returns `Either` |
| Error type | Exceptions | `RepoError` sealed class |
| Mapping | None | DTO → Domain |
| Local storage | ❌ | ✅ (optional) |

## Return Type Guidelines

- One-shot operations: `suspend fun op(): Either<RepoError, T>`
- Streams with errors: `Flow<Either<RepoError, T>>`
- Local-only streams: `Flow<T>` (stable)
- Offline-first: `Flow<T>` (SSoT) + `suspend fun refresh(): Either<RepoError, Unit>`

## Monad Comprehensions

For orchestrating multiple repository calls:

```kotlin
import arrow.core.raise.either

suspend fun submitAndCache(job: Job): Either<RepoError, JobId> = either {
    val saved: Unit = saveJob(job).bind()
    val refreshed: List<Job> = getJobs(page = 0, limit = 20).bind()
    refreshed.first { it.id == job.id }.id
}
```
