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

### Workflow 2: Map Exceptions to RepoError
1. Define `sealed interface RepoError` in `:api`.
2. Create `Throwable.toRepoError()` extension in `:data`.
3. Map Ktor/Coroutine exceptions to `RepoError` cases.
4. Apply `.mapLeft { it.toRepoError() }` at all repository boundaries.

```kotlin
fun Throwable.toRepoError(): RepoError = when (this) {
    is ClientRequestException -> when (response.status.value) {
        404 -> RepoError.NotFound
        else -> RepoError.Http(response.status.value, message)
    }
    is IOException -> RepoError.Network
    else -> RepoError.Unknown(this)
}
```

### Workflow 3: Test Repository with Property-Based Tests
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

### Workflow 4: Integrate Repository with ViewModel
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
| Repository patterns | Either boundary implementation | [conventions.md](See @kmp-architecture skill for architecture patterns) |
| Error handling | RepoError hierarchy and mapping | [conventions.md](See @kmp-architecture skill for architecture patterns) |

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
