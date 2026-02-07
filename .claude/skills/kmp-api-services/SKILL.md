---
name: kmp-api-services
description: KMP API service patterns using Ktor, focusing on type-safe DTOs, serialization, and repository integration. Use for HTTP networking, DTO design, and remote data mapping.
version: 1.0.0
tags: [kmp, networking, ktor, api, dto, serialization]
---

# KMP API Services

API service patterns for Kotlin Multiplatform using Ktor to keep remote APIs structured, testable, and decoupled from domain models.

## When to Use This Skill

- Implementing or modifying **API services** (Ktor Client).
- Defining **Request/Response DTOs** with Kotlinx Serialization.
- Mapping remote data to domain models via `asDomain()`.
- Configuring **Ktor Client** (engines, JSON, logging, timeouts).
- Triggers: "API service", "Ktor", "remote", "HTTP", "DTO", "request/response", "serialization".

## Related Skills

- **@kmp-data-layer**: Handles the `Either<RepoError, T>` boundary and repository implementation.
- **@kmp-architecture**: Guidelines for module structure and vertical slicing.

## Critical Patterns

### 1. API Service Boundary
API services return raw data or DTOs. They NEVER return `Result` or `Either`. Error handling is deferred to the Repository layer.

```kotlin
interface JobApiService {
    suspend fun getJobs(request: GetJobsRequest): GetJobsResponse
}
```

### 2. DTO Naming & Serialization
- Use `Request` and `Response` suffixes.
- Always use `@Serializable` and `@SerialName`.

```kotlin
@Serializable
data class JobResponse(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String
) {
    fun asDomain(): Job = Job(id = id, title = title)
}
```

### 3. Repository Integration
Repositories wrap API calls in `Either.catch` and map results.

```kotlin
override suspend fun getJobs(): Either<RepoError, List<Job>> = 
    Either.catch {
        api.getJobs(GetJobsRequest()).jobs.map { it.asDomain() }
    }.mapLeft { it.toRepoError() }
```

## NEVER

- **NEVER** return `Either` or `Result` from an API service.
- **NEVER** leak serialization annotations (`@Serializable`) into domain models.
- **NEVER** use domain models directly as API response/request types.
- **NEVER** hardcode Dispatchers inside services; use `suspend`.

## Implementation Details

- **Location**: Feature `:data` modules under `remote/apiservices`.
- **DTOs**: Placed in `remote/request` and `remote/response`.
- **Transport**: Centralized Ktor Client creation with `ContentNegotiation`.
- **Testing**: Use `MockEngine` for Ktor and provide stubbed implementations.

See [references/ktor-configuration.md](references/ktor-configuration.md) for client setup and [references/testing.md](references/testing.md) for MockEngine patterns.
