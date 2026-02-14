# Repository Pattern Guide

Detailed patterns for implementing repositories with Impl + Factory structure and Either boundaries.

## Table of Contents

1. [Impl + Factory Pattern](#impl--factory-pattern)
2. [Repository Interfaces](#repository-interfaces)
3. [Offline-First Pattern](#offline-first-pattern)
4. [Parametric Repositories](#parametric-repositories)
5. [Complete Example](#complete-example)

## Impl + Factory Pattern

### Why This Pattern?

- **Encapsulation**: Implementation stays internal
- **Flexibility**: Easy to swap implementations
- **Testability**: Interface can be mocked
- **Consistency**: Single pattern across all repositories

### Structure

```
:features:<feature>:api
└── JobRepository.kt          # Interface (public)

:features:<feature>:data
├── JobRepositoryImpl.kt      # Implementation (internal)
└── JobRepository.kt          # Factory function (public)
```

### Implementation File

```kotlin
// :features:jobs:data/JobRepositoryImpl.kt
package com.example.features.jobs.data

import arrow.core.Either
import com.example.features.jobs.api.JobRepository
import com.example.features.jobs.api.RepoError
import com.example.features.jobs.api.domain.Job
import com.example.features.jobs.data.mappers.asDomain

internal class JobRepositoryImpl(
    private val api: JobApiService,
    private val dao: SavedJobDao
) : JobRepository {

    override suspend fun getJobs(page: Int, limit: Int): Either<RepoError, List<Job>> =
        Either.catch {
            val response = api.getJobs(GetJobsRequest(page, limit))
            response.jobs.map { it.asDomain() }
        }.mapLeft { it.toRepoError() }

    override suspend fun saveJob(job: Job): Either<RepoError, Unit> =
        Either.catch {
            api.saveJob(SaveJobRequest(job.id))
            dao.upsert(SavedJobEntity.from(job))
        }.mapLeft { it.toRepoError() }
}
```

### Factory File

```kotlin
// :features:jobs:data/JobRepository.kt
package com.example.features.jobs.data

import com.example.features.jobs.api.JobRepository

fun JobRepository(
    api: JobApiService,
    dao: SavedJobDao
): JobRepository = JobRepositoryImpl(api, dao)
```

### Wiring Module

```kotlin
// :features:jobs:wiring/JobsWiring.kt
val jobsModule = module {
    factory<JobRepository> { JobRepository(api = get(), dao = get()) }
}
```

## Repository Interfaces

### Basic Interface

```kotlin
// :features:jobs:api/JobRepository.kt
package com.example.features.jobs.api

import arrow.core.Either
import com.example.features.jobs.api.domain.Job
import kotlinx.coroutines.flow.Flow

interface JobRepository {
    // One-shot operation
    suspend fun getJobs(page: Int, limit: Int): Either<RepoError, List<Job>>
    
    // One-shot with Unit return
    suspend fun saveJob(job: Job): Either<RepoError, Unit>
    
    // Local-only operations don't need Either
    fun markJobAsSeen(jobId: String)
}
```

### With Offline-First Support

```kotlin
interface JobRepository {
    // Flow from local cache (SSoT)
    fun stream(): Flow<List<Job>>
    
    // Explicit refresh from network
    suspend fun refresh(page: Int, limit: Int): Either<RepoError, Unit>
}
```

## Offline-First Pattern

### Concept

- Local database is the **Single Source of Truth**
- UI observes local data via `Flow<T>`
- Network updates happen via explicit `refresh()` calls
- Repository handles synchronization

### Implementation

```kotlin
interface JobRepository {
    fun stream(): Flow<List<Job>>  // Always from local
    suspend fun refresh(page: Int, limit: Int): Either<RepoError, Unit>
}

internal class JobRepositoryImpl(
    private val api: JobApiService,
    private val dao: JobDao
) : JobRepository {
    
    override fun stream(): Flow<List<Job>> = 
        dao.observeAll().map { list -> 
            list.map(JobEntity::asDomain) 
        }
    
    override suspend fun refresh(page: Int, limit: Int): Either<RepoError, Unit> = 
        Either.catch {
            val response = api.getJobs(GetJobsRequest(page, limit))
            dao.transaction {
                dao.replaceAll(response.jobs.map(JobEntity::from))
            }
        }.mapLeft { it.toRepoError() }
}
```

### Usage in ViewModel

```kotlin
class JobListViewModel(
    private val repository: JobRepository
) : ViewModel() {
    
    // Observe local data (reactive)
    val jobs: StateFlow<List<Job>> = repository.stream()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // Trigger refresh (user action)
    fun onPullToRefresh() {
        viewModelScope.launch {
            repository.refresh(page = 0, limit = 20)
                .fold(
                    ifLeft = { error -> showError(error.toUiMessage()) },
                    ifRight = { /* Success - UI already updated via Flow */ }
                )
        }
    }
}
```

## Parametric Repositories

### Pattern for ID-based Fetching

```kotlin
// Interface
interface PokemonDetailRepository {
    suspend fun getPokemonById(id: Int): Either<RepoError, PokemonDetail>
}

// Implementation
internal class PokemonDetailRepositoryImpl(
    private val api: PokemonDetailApiService
) : PokemonDetailRepository {
    
    override suspend fun getPokemonById(id: Int): Either<RepoError, PokemonDetail> =
        Either.catch {
            api.getPokemonById(id).asDomain()
        }.mapLeft { it.toRepoError() }
}

// Factory
fun PokemonDetailRepository(
    api: PokemonDetailApiService
): PokemonDetailRepository = PokemonDetailRepositoryImpl(api)
```

## Complete Example

### Module Structure

```
:features:pokemondetail:api
├── PokemonDetailRepository.kt    # Interface + RepoError
└── domain/
    └── PokemonDetail.kt          # Domain model

:features:pokemondetail:data
├── PokemonDetailRepositoryImpl.kt
├── PokemonDetailRepository.kt    # Factory
├── PokemonDetailApiService.kt
├── dto/
│   ├── PokemonDetailDto.kt
│   ├── TypeSlotDto.kt
│   ├── StatDto.kt
│   ├── AbilitySlotDto.kt
│   ├── SpritesDto.kt
│   └── NamedResourceDto.kt
└── mappers/
    └── PokemonDetailMappers.kt
```

### API Service

```kotlin
// PokemonDetailApiService.kt
internal class PokemonDetailApiService(
    private val httpClient: HttpClient
) {
    suspend fun getPokemonById(id: Int): PokemonDetailDto {
        return httpClient.get("/pokemon/$id").body()
    }
}

// Factory
internal fun PokemonDetailApiService(
    httpClient: HttpClient
): PokemonDetailApiService = PokemonDetailApiService(httpClient)
```

### Repository Implementation

```kotlin
// PokemonDetailRepositoryImpl.kt
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

// Error mapper
private fun Throwable.toRepoError(): RepoError = when (this) {
    is ClientRequestException -> RepoError.Http(
        response.status.value, 
        message ?: "Client error"
    )
    is ServerResponseException -> RepoError.Http(
        response.status.value, 
        message ?: "Server error"
    )
    is IOException, is TimeoutCancellationException -> RepoError.Network
    else -> RepoError.Unknown(this)
}
```

### Factory

```kotlin
// PokemonDetailRepository.kt
fun PokemonDetailRepository(
    api: PokemonDetailApiService
): PokemonDetailRepository = PokemonDetailRepositoryImpl(api)
```

### Wiring

```kotlin
// :features:pokemondetail:wiring
val pokemonDetailModule = module {
    factory<PokemonDetailApiService> {
        PokemonDetailApiService(httpClient = get())
    }
    factory<PokemonDetailRepository> {
        PokemonDetailRepository(api = get())
    }
}
```

## Key Takeaways

1. **Impl + Factory**: Internal implementation, public factory function
2. **Either Boundaries**: All network operations return `Either<RepoError, T>`
3. **Offline-First**: Local cache as SSoT with explicit refresh
4. **No DTO Leakage**: Map to domain models in repository
5. **Error Mapping**: Centralized exception → RepoError mapping
