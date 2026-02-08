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

## Critical Guardrails

1. NEVER return `Either` or `Result` from an API service → return raw DTOs and defer error handling to the Repository (reason: maintains layer separation).
2. NEVER leak serialization annotations (`@Serializable`) into domain models → use separate DTO classes in `:data` (reason: decouples domain logic from API changes).
3. NEVER use domain models directly as API response/request types → always define explicit DTOs (reason: prevents API changes from breaking domain logic).
4. NEVER hardcode Dispatchers inside services → use `suspend` functions and let the caller manage the context (reason: improves testability and follows structured concurrency).
5. NEVER skip `@SerialName` → always use explicit JSON key mapping (reason: protects against property renaming/obfuscation and maintains API contract).
6. NEVER share DTOs between features → each feature must own its DTOs in its `:data` module (reason: maintains vertical slice independence and prevents tight coupling).
7. NEVER catch exceptions in API services → let exceptions propagate to the repository layer (reason: repositories use `Either.catch` to establish a consistent error boundary).
8. NEVER use public for implementation classes → use `internal class` with a public factory function (reason: supports Gradle compilation avoidance).

## Essential Workflows

### Workflow 1: Creating a New API Service with Ktor Client

To add a new API service following the vertical slice architecture:

1. **Define the service interface** in the feature's `:data` module:
   ```kotlin
   // features/jobs/data/src/commonMain/.../JobApiService.kt
   interface JobApiService {
       suspend fun getJobs(): JobResponse
   }
   ```

2. **Implement the interface** using an `internal class`:
   ```kotlin
   internal class JobApiServiceImpl(
       private val httpClient: HttpClient
   ) : JobApiService {
       override suspend fun getJobs(): JobResponse = 
           httpClient.get("https://api.example.com/jobs").body()
   }
   ```

3. **Create a public factory function** to expose the service:
   ```kotlin
   fun JobApiService(httpClient: HttpClient): JobApiService = 
       JobApiServiceImpl(httpClient)
   ```

4. **Register in Koin module**:
   ```kotlin
   val jobModule = module {
       factory { JobApiService(httpClient = get()) }
   }
   ```

**Cross-references**: @kmp-data-layer (repository integration), @kmp-di (Koin wiring)

### Workflow 2: Defining DTOs with Kotlinx Serialization

To define type-safe request/response models:

1. **Create data class** for the API response in `remote/dto/`:
   ```kotlin
   @Serializable
   data class JobResponse(
       @SerialName("jobs") val jobs: List<JobDto>
   )
   ```

2. **Use @SerialName for all properties** to protect against obfuscation:
   ```kotlin
   @Serializable
   data class JobDto(
       @SerialName("id") val id: String,
       @SerialName("job_title") val title: String,
       @SerialName("company_name") val company: String
   )
   ```

3. **Add asDomain() mapper** to convert to domain models:
   ```kotlin
   fun JobDto.asDomain(): Job = Job(
       id = id,
       title = title,
       company = company
   )
   ```

**Cross-references**: @kmp-domain (domain models), @kmp-data-layer (mappers)

### Workflow 3: Configuring Ktor Client with ContentNegotiation

To set up the Ktor client for JSON serialization:

1. **Centralize configuration** in `:core:httpclient`:
   ```kotlin
   fun createHttpClient(engine: HttpClientEngine) = HttpClient(engine) {
       install(ContentNegotiation) {
           json(Json {
               ignoreUnknownKeys = true
               isLenient = true
           })
       }
       install(HttpTimeout) {
           requestTimeoutMillis = 15000
       }
   }
   ```

2. **Inject HttpClient** into API services via DI.

**Cross-references**: @kmp-gradle (build configuration), @kmp-di (singleton registration)

## Quick Reference

| Pattern | Purpose | Example |
|---------|---------|---------|
| GET Request | Fetch data | `httpClient.get("url").body<T>()` |
| POST Request | Send data | `httpClient.post("url") { setBody(dto) }.body<T>()` |
| Query Parameters | Filter data | `parameter("key", value)` |
| @SerialName | Map JSON key | `@SerialName("json_key") val property: String` |
| asDomain() | DTO→Domain | `fun Dto.asDomain() = DomainModel(...)` |
| MockEngine | Test API | `HttpClient(MockEngine { respond(...) })` |

## Cross-References

### Skills (by Category)

**Data Layer**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-data-layer | Repository patterns with Either | [SKILL.md](../kmp-data-layer/SKILL.md) |
| @kmp-domain | Domain models and business logic | [SKILL.md](../kmp-domain/SKILL.md) |

**Architecture**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-architecture | Module structure and vertical slicing | [SKILL.md](../kmp-architecture/SKILL.md) |
| @kmp-critical-patterns | Quick reference for 6 core patterns | [SKILL.md](../kmp-critical-patterns/SKILL.md) |

**DI & Infrastructure**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-di | Koin dependency injection configuration | [SKILL.md](../kmp-di/SKILL.md) |
| @kmp-gradle | Convention plugins and build setup | [SKILL.md](../kmp-gradle/SKILL.md) |
| @kmp-developer | General development and refactoring | [SKILL.md](../kmp-developer/SKILL.md) |
| @ktor-backend | Server-side Ktor API implementation | [SKILL.md](../ktor-backend/SKILL.md) |

**Testing**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-testing-patterns | MockEngine and property-based tests | [SKILL.md](../kmp-testing-patterns/SKILL.md) |

### Documents

| Document | Purpose | Link |
| --- | --- | --- |
| conventions.md | Architecture master reference | See @kmp-architecture skill |
| critical_patterns_quick_ref.md | 6 core patterns guide | See @kmp-critical-patterns skill |
| ktor-configuration.md | Detailed Ktor setup guide | [ktor-configuration.md](references/ktor-configuration.md) |
