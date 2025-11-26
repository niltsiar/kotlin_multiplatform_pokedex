# Repository Guidelines

## Overview
Repositories coordinate data from remote APIs, local storage, and caches, and expose domain models to the rest of the app. In this project, repositories are the boundary where transport/storage failures are mapped into Arrow `Either` results.

Key decisions
- Use Arrow `Either` at repository boundaries.
- Wrap throwing code with `Either.catch { ... }` and map exceptions to a sealed error type.
- Keep mapping between DTO/entity and domain close to repositories.
- Prefer vertical-slice ownership: repositories live in feature `:data` modules and implement contracts declared in feature `:api` modules when cross-feature usage is needed.

Interfaces, implementations, and factories
- Implement interfaces with a private/internal class named `<InterfaceName>Impl` and expose a top-level factory function named exactly like the interface that returns the interface type. Wiring modules call the factory.

## Interfaces vs Concrete

- Define repository interfaces in `:features:<feature>:api` when other features consume them or when you need substitution in tests at the module boundary.
- Use concrete classes in `:features:<feature>:data` for the actual implementation and keep them internal to the feature when possible.

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

- Unit-test repository behavior with fakes or MockK. Assert on `Either` values using **Kotest Arrow extensions**.
- Use Kotest property testing where applicable (e.g., mapping invariants).

### Preferred Either Assertions (Kotest Arrow)

```kotlin
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight

class JobRepositoryTest : StringSpec({
    "saveJob returns Right on success" {
        coEvery { mockApi.saveJob(any()) } returns Unit
        
        val result = repository.saveJob(sampleJob)
        
        // Returns Unit - no casting needed
        result.shouldBeRight()
    }
    
    "loadJob returns Right with mapped domain" {
        coEvery { mockApi.getJob(1) } returns JobDto(id = 1, title = "Engineer")
        
        val result = repository.loadJob(1)
        
        // Returns unwrapped Job - no casting needed
        val job = result.shouldBeRight()
        job.id shouldBe 1
        job.title shouldBe "Engineer"
    }
    
    "loadJob returns Left on error" {
        coEvery { mockApi.getJob(any()) } throws ConnectTimeoutException("timeout")
        
        val result = repository.loadJob(1)
        
        // Returns unwrapped RepoError - no casting needed
        val error = result.shouldBeLeft()
        error shouldBe RepoError.Network
    }
    
    "loadJob returns Http error" {
        val mockResponse = mockk<HttpResponse> {
            coEvery { status } returns HttpStatusCode.NotFound
        }
        coEvery { mockApi.getJob(any()) } throws 
            ClientRequestException(mockResponse, "Not found")
        
        val result = repository.loadJob(1)
        
        val error = result.shouldBeLeft()
        error.shouldBeInstanceOf<RepoError.Http>()
        error.code shouldBe 404  // Smart cast - no manual cast!
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

---

## Example: Parametric Repository (Pokemon Detail)

### Pattern: Repository with Parameters

Some repositories need parameters in their methods (e.g., fetch by ID). This follows the same Impl + Factory pattern with parameter passing.

**API Layer** (`:features:pokemondetail:api`):
```kotlin
package com.minddistrict.multiplatformpoc.features.pokemondetail.api

import arrow.core.Either
import com.minddistrict.multiplatformpoc.features.pokemondetail.api.domain.PokemonDetail

interface PokemonDetailRepository {
    suspend fun getPokemonById(id: Int): Either<RepoError, PokemonDetail>
}

sealed interface RepoError {
    data object Network : RepoError
    data class Http(val code: Int, val message: String) : RepoError
    data class Unknown(val throwable: Throwable) : RepoError
}
```

**Data Layer** (`:features:pokemondetail:data`):

**API Service** (handles HTTP):
```kotlin
package com.minddistrict.multiplatformpoc.features.pokemondetail.data

import com.minddistrict.multiplatformpoc.features.pokemondetail.data.dto.PokemonDetailDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

internal class PokemonDetailApiService(
    private val httpClient: HttpClient
) {
    suspend fun getPokemonById(id: Int): PokemonDetailDto {
        return httpClient.get("/pokemon/$id").body()
    }
}

// Factory function
internal fun PokemonDetailApiService(httpClient: HttpClient): PokemonDetailApiService =
    PokemonDetailApiService(httpClient)
```

**DTOs** (nested structures):
```kotlin
package com.minddistrict.multiplatformpoc.features.pokemondetail.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PokemonDetailDto(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val types: List<TypeSlotDto>,
    val stats: List<StatDto>,
    val abilities: List<AbilitySlotDto>,
    val sprites: SpritesDto
)

@Serializable
internal data class TypeSlotDto(
    val slot: Int,
    val type: NamedResourceDto
)

@Serializable
internal data class StatDto(
    @SerialName("base_stat") val baseStat: Int,
    val stat: NamedResourceDto
)

@Serializable
internal data class AbilitySlotDto(
    @SerialName("is_hidden") val isHidden: Boolean,
    val ability: NamedResourceDto
)

@Serializable
internal data class SpritesDto(
    @SerialName("front_default") val frontDefault: String?
)

@Serializable
internal data class NamedResourceDto(
    val name: String,
    val url: String
)
```

**Mappers** (nested domain mapping):
```kotlin
package com.minddistrict.multiplatformpoc.features.pokemondetail.data.mappers

import com.minddistrict.multiplatformpoc.features.pokemondetail.api.domain.*
import com.minddistrict.multiplatformpoc.features.pokemondetail.data.dto.*
import kotlinx.collections.immutable.toImmutableList

internal fun PokemonDetailDto.asDomain(): PokemonDetail = PokemonDetail(
    id = id,
    name = name.replaceFirstChar { it.uppercase() },
    height = height,
    weight = weight,
    types = types.sortedBy { it.slot }.map { it.asDomain() }.toImmutableList(),
    stats = stats.map { it.asDomain() }.toImmutableList(),
    abilities = abilities.map { it.asDomain() }.toImmutableList(),
    imageUrl = sprites.frontDefault ?: ""
)

internal fun TypeSlotDto.asDomain(): Type = Type(
    name = type.name.replaceFirstChar { it.uppercase() },
    url = type.url
)

internal fun StatDto.asDomain(): Stat = Stat(
    name = stat.name.replace("-", " ").replaceFirstChar { it.uppercase() },
    value = baseStat
)

internal fun AbilitySlotDto.asDomain(): Ability = Ability(
    name = ability.name.replaceFirstChar { it.uppercase() },
    isHidden = isHidden
)
```

**Repository Implementation**:
```kotlin
package com.minddistrict.multiplatformpoc.features.pokemondetail.data

import arrow.core.Either
import com.minddistrict.multiplatformpoc.features.pokemondetail.api.PokemonDetailRepository
import com.minddistrict.multiplatformpoc.features.pokemondetail.api.RepoError
import com.minddistrict.multiplatformpoc.features.pokemondetail.api.domain.PokemonDetail
import com.minddistrict.multiplatformpoc.features.pokemondetail.data.mappers.asDomain
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.TimeoutCancellationException

internal class PokemonDetailRepositoryImpl(
    private val api: PokemonDetailApiService
) : PokemonDetailRepository {
    override suspend fun getPokemonById(id: Int): Either<RepoError, PokemonDetail> =
        Either.catch {
            api.getPokemonById(id).asDomain()
        }.mapLeft { throwable ->
            throwable.toRepoError()
        }
}

// Factory function
internal fun PokemonDetailRepository(api: PokemonDetailApiService): PokemonDetailRepository =
    PokemonDetailRepositoryImpl(api)

// Error mapper
private fun Throwable.toRepoError(): RepoError = when (this) {
    is ClientRequestException -> RepoError.Http(response.status.value, message ?: "Client error")
    is ServerResponseException -> RepoError.Http(response.status.value, message ?: "Server error")
    is IOException, is TimeoutCancellationException -> RepoError.Network
    else -> RepoError.Unknown(this)
}
```

**Key Patterns**:
- ✅ API service throws exceptions (Ktor default)
- ✅ Repository wraps with `Either.catch { }`
- ✅ Map exceptions to sealed `RepoError` with `.mapLeft { it.toRepoError() }`
- ✅ DTO→domain mapping with extension functions (`.asDomain()`)
- ✅ Nested DTOs map to nested domain models
- ✅ Use `toImmutableList()` for domain collections
- ✅ String transformations (capitalize, replace) in mappers
- ✅ Factory pattern: `fun PokemonDetailRepository(...): PokemonDetailRepository`

**Wiring** (`:features:pokemondetail:wiring`):
```kotlin
val pokemonDetailModule = module {
    factory<PokemonDetailApiService> {
        PokemonDetailApiService(httpClient = get())
    }
    factory<PokemonDetailRepository> {
        PokemonDetailRepository(api = get())
    }
}
```

### Testing Parametric Repository

**Test Structure** (androidUnitTest with Kotest + MockK):
```kotlin
package com.minddistrict.multiplatformpoc.features.pokemondetail.data

import arrow.core.Either
import com.minddistrict.multiplatformpoc.features.pokemondetail.api.RepoError
import com.minddistrict.multiplatformpoc.features.pokemondetail.data.dto.*
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.plugins.ClientRequestException
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.TimeoutCancellationException

class PokemonDetailRepositoryTest : StringSpec({
    lateinit var mockApi: PokemonDetailApiService
    lateinit var repository: PokemonDetailRepository
    
    beforeTest {
        mockApi = mockk()
        repository = PokemonDetailRepository(mockApi)
    }
    
    "getPokemonById returns Right on success" {
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
        
        val result = repository.getPokemonById(25)
        
        val pokemon = result.shouldBeRight()
        pokemon.name shouldBe "Pikachu"
        pokemon.types.size shouldBe 1
        pokemon.types[0].name shouldBe "Electric"
    }
    
    "getPokemonById returns Network error on timeout" {
        coEvery { mockApi.getPokemonById(any()) } throws TimeoutCancellationException("timeout")
        
        val result = repository.getPokemonById(1)
        
        val error = result.shouldBeLeft()
        error shouldBe RepoError.Network
    }
    
    "getPokemonById returns Http error on 404" {
        val mockResponse = mockk<HttpResponse> {
            coEvery { status } returns HttpStatusCode.NotFound
        }
        coEvery { mockApi.getPokemonById(any()) } throws 
            ClientRequestException(mockResponse, "Not found")
        
        val result = repository.getPokemonById(9999)
        
        val error = result.shouldBeLeft()
        error.shouldBeInstanceOf<RepoError.Http>()
        error.code shouldBe 404  // Smart cast!
    }
})
```

**Key Testing Patterns**:
- ✅ Use MockK `coEvery` for suspend functions
- ✅ Use Kotest `shouldBeRight()` for success cases (returns unwrapped value)
- ✅ Use Kotest `shouldBeLeft()` for error cases (returns unwrapped error)
- ✅ Use `shouldBeInstanceOf<T>()` for smart casting error types
- ✅ Test all error paths (network, HTTP status codes, unknown)
- ✅ Verify domain mapping (DTO→Domain transformations)
- ✅ Test nested structures (types, stats, abilities)

---

## Summary

- **Repositories return `Either<RepoError, T>`** at boundaries
- **API services throw exceptions**, repositories catch and map
- **Use sealed error hierarchies** for feature-specific errors
- **DTO→domain mapping** happens in repositories via extension functions
- **Impl + Factory pattern** keeps implementations internal
- **Test with Kotest + MockK** in androidUnitTest for full framework support
- **Parametric repositories** pass parameters to API service methods
- **Nested structures** map through extension functions preserving immutability
