# API Services Guidelines

Purpose: Keep remote APIs well-structured, testable, and decoupled from domain models.

- Transport: Ktor
  - Use Ktor Client for HTTP transport across platforms.
  - Centralize Ktor client creation (engine, JSON, logging, timeouts, retries) in a single provider/factory.
  - Configure content negotiation with Kotlinx Serialization; do not leak serialization annotations into domain models.

- Location (feature modules)
  - Place API service interfaces and implementations in feature `impl` modules, e.g. `:features:<feature>:impl/src/commonMain/kotlin/.../data/remote/apiservices`.
  - Keep request/response DTOs under `.../data/remote/request` and `.../data/remote/response` respectively.
  - Request and Response classes MUST have `Request` and `Response` suffixes in their class names.
  - All request and response classes MUST use `@Serializable` and `@SerialName` annotations for JSON serialization.
  - For platform-specific transport code, use expect/actual or platform source sets (androidMain, iosMain), keeping the interface in commonMain.

- Interfaces & Calls
  - Prefer small, cohesive interfaces grouped by bounded contexts (e.g., AuthService, MediaService).
  - Use suspending functions for network calls. Avoid callbacks.
  - Represent responses with DTOs tailored to transport, not domain.
  - Return raw data types, not `Result`/`Either` wrappers. Let repositories handle error mapping and return Arrow `Either` at the boundary.

- Error Handling
  - Normalize low-level errors (timeouts, IO, HTTP status) into a consistent error model at the data boundary.
  - Let exceptions propagate to the repository layer. Don't wrap in `Result`/`Either` at the API service level.

## Implementation Patterns

### API Service Interface
```kotlin
interface JobApiService {
    suspend fun getJobs(request: GetJobsRequest): GetJobsResponse
    suspend fun saveJob(request: SaveJobRequest): SaveJobResponse
}
```

### Response Classes with Domain Mapping
Response classes should include asDomain() methods for converting DTOs to domain models:

```kotlin
@Serializable
data class JobResponse(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    // ... other fields
) {
    fun asDomain(): Job {
        return Job(
            id = id,
            title = title,
            // ... map all fields
        )
    }
}
```

### Repository Integration (Arrow Either)
```kotlin
class JobRepositoryImpl(
    private val jobApiService: JobApiService
) : JobRepository {
    override suspend fun getJobs(page: Int, limit: Int): Either<RepoError, List<Job>> =
        Either.catch {
            val request = GetJobsRequest(page = page, limit = limit)
            val response = jobApiService.getJobs(request)
            response.jobs.map { it.asDomain() }
        }.mapLeft { it.toRepoError() }
}
```

  
- Coroutines & Threading
  - API service methods are `suspend` and may be called from any dispatcher. Do not hardcode dispatchers inside services; delegate to callers (repositories/UI) to choose threading.
  - Propagate coroutineContext to Ktor calls for cancellation support.
 
- Configuration
  - Centralize base URL(s), API keys, timeouts, and retry policy in a single configuration point.
  - Keep secrets out of source control; read from environment, local.properties, or secure keystores.
 
- Testing
  - Provide fakes or stubbed implementations for tests. Avoid hitting real network in unit tests.
  - Use contract tests for serialization/mapping where practical.
  - For Ktor, prefer MockEngine to simulate responses and errors.
  - Favor JSON round‑trip tests for DTOs: `json -> object -> json` and `object -> json -> object` to verify adapters are symmetric.

- Alignment with Product Docs
  - Ensure endpoints, payloads, and status handling align with .junie/guides/project/prd.md. When UX requires specific error messages or states, follow .junie/guides/project/user_flow.md. Surface discrepancies explicitly.
