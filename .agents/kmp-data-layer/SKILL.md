---
name: kmp-data-layer
description: Data layer patterns for Kotlin Multiplatform - repository implementation with Arrow Either, error handling, and DTO mapping. Use when implementing repositories, defining RepoError hierarchies, mapping DTOs to domain, or testing data layers. Covers Either<RepoError,T> boundaries, sealed error classes, exception mapping, and repository testing patterns.
---

# KMP Data Layer Patterns

Repository implementation patterns with Arrow Either for type-safe error handling in Kotlin Multiplatform.

## Core Principle

**Repositories return `Either<RepoError, T>` at boundaries. NEVER throw, return null, or use `Result`.**

## Quick Reference

### 1. Repository Pattern (Impl + Factory)

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

### 2. DTO to Domain Mapping

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

### 3. Offline-First Pattern

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

### 4. Testing with Kotest

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

## Error Handling Patterns

### RepoError Hierarchy

Define feature-specific errors in `:api`.

```kotlin
sealed interface RepoError {
    data object Network : RepoError
    data class Http(val code: Int, val message: String?) : RepoError
    data object Unauthorized : RepoError
    data object NotFound : RepoError
    data class Unknown(val cause: Throwable) : RepoError
}
```

### Exception Mapping

Map Ktor/Coroutine exceptions to `RepoError` in `:data`.

```kotlin
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

### Cancellation Handling

`Either.catch` respects cancellation automatically. Re-throw `CancellationException` if using manual `try-catch`.

```kotlin
// ✅ CORRECT - Either.catch respects cancellation
override suspend fun getJobs(): Either<RepoError, List<Job>> =
    Either.catch {
        api.getJobs().map { it.asDomain() }
    }.mapLeft { it.toRepoError() }

// ❌ WRONG - Swallowing CancellationException
try { ... } catch (e: Exception) { Either.Left(e.toRepoError()) } 
```

### UI Message Mapping

Provide user-friendly messages for errors (usually in `:api` or `:presentation`).

```kotlin
fun RepoError.toUiMessage(): String = when (this) {
    is RepoError.Network -> "No internet connection"
    is RepoError.Http -> "Server error: $message"
    is RepoError.Unauthorized -> "Please log in again"
    is RepoError.NotFound -> "Item not found"
    is RepoError.Unknown -> "Something went wrong"
}
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

## Decision Framework

Before implementing repositories, ask yourself:

1. **What error types can occur?**
   - Network failures (IOException) → `RepoError.Network`
   - HTTP errors (4xx, 5xx) → `RepoError.Http(code, message)`
   - Parsing/serialization errors → `RepoError.Unknown`
   - Map ALL exceptions to RepoError, NEVER throw

2. **What DTO mapping is needed?**
   - Remote DTO → Domain model: `dto.asDomain()` or `dto.toDomain()`
   - Test with property-based tests (100% coverage target for mappers)
   - Keep DTOs in `:data`, domain models in `:api`

3. **How should this be tested?**
   - Success path: Mock API returns data → verify domain mapping
   - Network error: Mock throws IOException → verify RepoError.Network
   - HTTP error: Mock returns error response → verify RepoError.Http
   - Unknown error: Mock throws RuntimeException → verify RepoError.Unknown

## Essential Workflows

### Workflow 1: Implement Repository with Either Boundary
1. Define repository interface in `:api` returning `Either<RepoError, T>`.
2. Create `internal class RepositoryImpl` in `:data`.
3. Inject `ApiService` and implement methods with `Either.catch { ... }.mapLeft { it.toRepoError() }`.
4. Create public factory function in `:data` returning the interface.

```kotlin
interface JobRepository { suspend fun getJobs(): Either<RepoError, List<Job>> }

internal class JobRepositoryImpl(private val api: JobApiService) : JobRepository {
    override suspend fun getJobs(): Either<RepoError, List<Job>> =
        Either.catch { api.getJobs().map { it.asDomain() } }.mapLeft { it.toRepoError() }
}

fun JobRepository(api: JobApiService): JobRepository = JobRepositoryImpl(api)
```

### Workflow 2: Test Repository with Property-Based Tests
1. Create test class in `androidUnitTest/` using Kotest.
2. Use `Arb` generators for diverse DTO scenarios.
3. Verify success paths return `Right` with mapped models.
4. Verify error paths return the correct `Left(RepoError)`.

```kotlin
"getJobs returns Right on success" {
    checkAll(Arb.list(Arb.jobDto())) { dtos ->
        val api = mockk<JobApiService> { coEvery { getJobs() } returns dtos }
        JobRepository(api).getJobs().shouldBeRight().size shouldBe dtos.size
    }
}
```

### Workflow 3: Integrate Repository with ViewModel
1. Inject repository into `ViewModel` and call methods in `viewModelScope`.
2. Use `.fold()` to handle success/failure.
3. Update `UiState` based on the result.

```kotlin
class JobViewModel(private val repository: JobRepository) : ViewModel() {
    override fun onStart(owner: LifecycleOwner) {
        viewModelScope.launch {
            repository.getJobs().fold(
                ifLeft = { _uiState.value = JobUiState.Error(it) },
                ifRight = { _uiState.value = JobUiState.Content(it) }
            )
        }
    }
}
```

## Critical Guardrails

1. NEVER return nullable (`T?`) or `Result<T>` → return `Either<RepoError, T>` to establish a type-safe error boundary.
2. NEVER let exceptions leak from repositories → use `Either.catch { }` to capture all exceptions at the boundary.
3. NEVER expose `RepositoryImpl` as public → use `internal class` with a public factory function to support Gradle compilation avoidance.
4. NEVER skip error mapping → always apply `.mapLeft { it.toRepoError() }` for consistent error types across the feature.
5. NEVER place repository implementations in `:core` → each feature owns its repository implementation for vertical slice independence.
6. NEVER share DTOs between features → each feature defines its own DTOs to maintain feature independence.
7. NEVER use repository directly in UI → always access repositories via ViewModels to maintain the presentation layer boundary.
8. NEVER swallow `CancellationException` → `Either.catch` respects cancellation automatically; ensure manual catch blocks do the same.

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

## Cross-References

### Skills (by Category)

| Category | Skill | Purpose | Link |
| --- | --- | --- | --- |
| Architecture | @kmp-architecture | Module structure, feature boundaries | [SKILL.md](../kmp-architecture/SKILL.md) |
| Architecture | @kmp-critical-patterns | Core patterns including Either boundary | [SKILL.md](../kmp-critical-patterns/SKILL.md) |
| Implementation | @kmp-mobile-expert | Repository implementation patterns | [SKILL.md](../kmp-mobile-expert/SKILL.md) |
| Implementation | @kmp-presentation | ViewModel consuming repositories | [SKILL.md](../kmp-presentation/SKILL.md) |
| Implementation | @kmp-domain | Domain models returned by repositories | [SKILL.md](../kmp-domain/SKILL.md) |
| Implementation | @kmp-api-services | API services consumed by repositories | [SKILL.md](../kmp-api-services/SKILL.md) |
| Implementation | @kmp-di | Koin repository registration | [SKILL.md](../kmp-di/SKILL.md) |
| Testing | @kmp-testing-patterns | Repository testing with Kotest | [SKILL.md](../kmp-testing-patterns/SKILL.md) |

### Documents
| Document | Purpose | Link |
| --- | --- | --- |
| Repository patterns | Either boundary implementation | [@kmp-architecture](../kmp-architecture/SKILL.md) |
| Error handling | RepoError hierarchy and mapping | [@kmp-architecture](../kmp-architecture/SKILL.md) |

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
