# Repository Guidelines

## Overview
Repositories coordinate data from remote APIs, local storage, and caches, and expose domain models to the rest of the app. In this project, repositories are the boundary where transport/storage failures are mapped into Arrow `Either` results.

Key decisions
- Use Arrow `Either` at repository boundaries.
- Wrap throwing code with `Either.catch { ... }` and map exceptions to a sealed error type.
- Keep mapping between DTO/entity and domain close to repositories.
- Prefer vertical-slice ownership: repositories live in feature `impl` modules and implement contracts declared in feature `api` modules when cross-feature usage is needed.

Interfaces, implementations, and factories
- Implement interfaces with a private/internal class named `<InterfaceName>Impl` and expose a top-level factory function named exactly like the interface that returns the interface type. Wiring modules call the factory.

## Interfaces vs Concrete

- Define repository interfaces in `:features:<feature>:api` when other features consume them or when you need substitution in tests at the module boundary.
- Use concrete classes in `:features:<feature>:impl` for the actual implementation and keep them internal to the feature when possible.

## Error Modeling with Arrow

Define a feature-level error model for repository failures:

```kotlin
sealed interface RepoError {
  data object Network : RepoError
  data class Http(val code: Int, val message: String?) : RepoError
  data object NotFound : RepoError
  data object Unauthorized : RepoError
  data class Unknown(val cause: Throwable) : RepoError
}
```

Map exceptions to errors in one place (helpers):

```kotlin
fun Throwable.toRepoError(): RepoError = when (this) {
  is ClientRequestException -> RepoError.Http(response.status.value, message)
  is ServerResponseException -> RepoError.Http(response.status.value, message)
  is UnauthorizedException -> RepoError.Unauthorized
  is NotFoundException -> RepoError.NotFound
  is IOException, is TimeoutCancellationException -> RepoError.Network
  else -> RepoError.Unknown(this)
}
```

## Return Types

- One-shot operations: `suspend fun op(...): Either<RepoError, DomainModel>`
- Streams: prefer `Flow<DomainModel>` for stable local streams; for network-backed streams use `Flow<Either<RepoError, DomainModel>>` or expose a cached `Flow<DomainModel>` with a separate refresh call returning `Either`.

## Typical Implementation Pattern (Impl + Factory)

```kotlin
class JobRepositoryImpl(
  private val api: JobApiService,
  private val dao: SavedJobDao
) : JobRepository { // Declared in :features:jobs:api when cross-feature

  override suspend fun getJobs(page: Int, limit: Int): Either<RepoError, List<Job>> =
    Either.catch {
      val response = api.getJobs(GetJobsRequest(page, limit))
      response.jobs.map { it.asDomain() }
    }.mapLeft { it.toRepoError() }

  override suspend fun saveJob(job: Job): Either<RepoError, Unit> =
    Either.catch {
      api.saveJob(SaveJobRequest(job.id))
      dao.upsert(SavedJobEntity.from(job))
      Unit
    }.mapLeft { it.toRepoError() }

  fun markJobAsSeen(jobId: String) {
    // Local-only, no Either needed
    dao.markSeen(jobId)
  }
}
```

Factory and wiring
```kotlin
// Top-level factory (public), returns the interface type
fun JobRepository(api: JobApiService, dao: SavedJobDao): JobRepository = JobRepositoryImpl(api, dao)

// :features:jobs:wiring/src/commonMain/.../JobsWiring.kt
@Provides fun provideJobRepository(api: JobApiService, dao: SavedJobDao): JobRepository = JobRepository(api, dao)
```

Notes
- API services throw exceptions. Repositories wrap with `Either.catch` and map errors.
- Keep repository methods small and composable; push DTO→domain mapping here.

Cancellation
- Never swallow `CancellationException`. Prefer `Either.catch` for wrapping throwing blocks — Arrow already treats cancellation and other non‑recoverable exceptions appropriately.

Monad comprehensions (preferred when orchestrating steps)
```kotlin
// Using Arrow either {} DSL for clarity
suspend fun submitAndCache(job: Job): Either<RepoError, JobId> = either {
  val saved: Unit = saveJob(job).bind()
  val refreshed: List<Job> = getJobs(page = 0, limit = 20).bind()
  refreshed.first { it.id == job.id }.id
}
```

## Offline‑first and Single Source of Truth (SSoT)
- Expose a stable, cached `Flow<Domain>` from local storage (DB or in‑memory cache) as the single source of truth.
- Provide an explicit `refresh()` suspend function that fetches from network and updates the local source, returning `Either<RepoError, Unit>`.
- UI layers observe the `Flow` and trigger `refresh()` as needed; this enables offline behavior by default.

Example
```kotlin
interface JobRepository {
  fun stream(): Flow<List<Job>> // backed by local DB/cache
  suspend fun refresh(page: Int, limit: Int): Either<RepoError, Unit>
}

internal class JobRepositoryImpl(
  private val api: JobApiService,
  private val dao: JobDao
) : JobRepository {
  override fun stream(): Flow<List<Job>> = dao.observeAll().map { list -> list.map(JobEntity::asDomain) }

  override suspend fun refresh(page: Int, limit: Int): Either<RepoError, Unit> = Either.catch {
    val response = api.getJobs(GetJobsRequest(page, limit))
    dao.transaction {
      dao.replaceAll(response.jobs.map(JobEntity::from))
    }
    Unit
  }.mapLeft { it.toRepoError() }
}
```

## API Service vs Repository Responsibilities

API Services
- Use Ktor client (or other transport) and return DTOs.
- Throw exceptions for HTTP/IO failures.

Repositories
- Wrap service calls in `Either` and map failures to `RepoError`.
- Orchestrate calls across sources (remote/local/cache).
- Expose domain models to callers.

## Testing

- Unit-test repository behavior with fakes or MockK. Assert on `Either` values using Kotest matchers.
- Use Kotest property testing where applicable (e.g., mapping invariants). Example:

```kotlin
class JobRepositorySpec : StringSpec({
  "saveJob returns Right(Unit) on happy path" {
    // setup mocks
    val repo = JobRepositoryImpl(api, dao)
    repo.saveJob(sampleJob) shouldBe Right(Unit)
  }
})
```

Round‑trip JSON tests
- For modules that parse/emit JSON, favor round‑trip tests to ensure adapters are symmetric:
  - `json -> object -> json`
  - `object -> json -> object`
- Use Kotlinx Serialization, Kotest, and/or AssertK for assertions.

## Anti-Patterns

- Returning `Result` from repositories. Project standard is Arrow `Either`.
- Swallowing errors or returning nulls; always model failure as `Either.Left(RepoError)`.
- Leaking DTOs beyond the repository boundary; map to domain.
