---
name: ktor-backend
description: This skill should be used when creating Ktor server endpoints, BFF APIs, and backend services. Use for REST APIs, request validation, and server features.
---

## When to Use

Use this skill when:

- Creating new Ktor server endpoints and routes
- Implementing REST APIs and BFF (Backend-for-Frontend) services
- Adding request validation with kotlinx.serialization
- Designing API versioning strategies
- Writing TestApplication integration tests
- Setting up OpenAPI/Swagger documentation
- Implementing authentication and authorization middleware
- Adding server features like CORS, compression, logging

Do NOT use this skill for:
- Client-side HTTP requests (use KMP Mobile Expert for `HttpClient` setup)
- Compose UI implementation (use compose-screen skill)
- Shared business logic (use kmp-mobile-expert skill)
- Database schema design (this project uses PokéAPI, no database layer yet)

## Essential Workflows

### Workflow 1: Create New REST Endpoint

To add a new Ktor endpoint following API conventions:

1. **Define request/response models** with `@Serializable`:
   ```kotlin
   @Serializable
   data class PokemonListRequest(val limit: Int, val offset: Int)

   sealed interface PokemonListResponse {
       @Serializable
       data class Success(
           val pokemons: List<Pokemon>,
           val count: Int,
           val next: String?
       ) : PokemonListResponse

       @Serializable
       data class Error(val message: String, val code: Int) : PokemonListResponse
   }
   ```

2. **Create route in `routing` block** with proper grouping:
   ```kotlin
   fun Application.module() {
       routing {
           route("/api/v1") {
               route("/pokemon") {
                   get {
                       val request = call.receive<PokemonListRequest>()
                       val response = getPokemonList(request)
                       call.respond(HttpStatusCode.OK, response)
                   }

                   get("/{id}") {
                       val id = call.parameters["id"]?.toIntOrNull()
                           ?: return@get call.respond(HttpStatusCode.BadRequest, PokemonListResponse.Error("Invalid ID", 400))
                       val response = getPokemonById(id)
                       call.respond(HttpStatusCode.OK, response)
                   }
           }
       }
   }
   ```

3. **Add request validation** before processing:
   ```kotlin
   fun Application.configureValidation() {
       install(ContentNegotiation) {
           json(Json {
               ignoreUnknownKeys = true
               prettyPrint = true
           })
       }
   }
   ```

4. **Write TestApplication integration test**:
   ```kotlin
   class PokemonRouteTest : FunSpec({
       test("GET /api/v1/pokemon returns list") {
           withTestApplication({
               configureRouting()
               configureSerialization()
           }) {
               with(handleRequest(HttpMethod.Get, "/api/v1/pokemon?limit=20&offset=0")) {
                   response.status() shouldBe HttpStatusCode.OK
                   val content = response.content!!
                   content shouldContain "\"pokemons\":"
               }
           }
       }
   })
   ```

5. **Validate endpoint with script**:
   ```bash
   .claude/skills/ktor-backend/scripts/validate-endpoint.sh server/src/main/kotlin/com/minddistrict/multiplatformpoc/routes/PokemonRoutes.kt
   ```

### Workflow 2: Add API Versioning

To implement API versioning strategy:

1. **Route by version prefix**:
   ```kotlin
   routing {
       // v1 API - stable
       route("/api/v1") {
           pokemonRoutes()  // Existing stable endpoints
       }

       // v2 API - beta/new features
       route("/api/v2") {
           pokemonRoutesV2()  // New endpoints with breaking changes
       }
   }
   ```

2. **Use sealed classes for version-specific responses**:
   ```kotlin
   // v1 response (stable)
   @Serializable
   data class PokemonResponseV1(val name: String, val url: String)

   // v2 response (with additional fields)
   @Serializable
   data class PokemonResponseV2(
       val id: Int,
       val name: String,
       val types: List<String>,
       val sprites: Sprites
   )
   ```

3. **Deprecate old versions** with headers:
   ```kotlin
   route("/api/v1") {
       intercept(ApplicationCallPipeline.Call) {
           call.response.headers.append("X-API-Deprecated", "true")
           call.response.headers.append("X-API-Version", "v2 available at /api/v2")
           proceed()
       }
   }
   ```

### Workflow 3: Set Up OpenAPI Documentation

To add Swagger/OpenAPI documentation:

1. **Add Ktor Swagger plugin** to dependencies:
   ```kotlin
   // server/build.gradle.kts
   dependencies {
       implementation(libs.ktor.swagger)
   }
   ```

2. **Configure Swagger plugin**:
   ```kotlin
   fun Application.configureSwagger() {
       install(Swagger) {
           swagger {
               info {
                   title = "Pokédex API"
                   version = "1.0.0"
                   description = "Kotlin Multiplatform Pokédex Backend-for-Frontend API"
               }
               schemes listOf(Scheme.HTTP, Scheme.HTTPS)
           }
       }
   }

   routing {
       swaggerUI(path = "swagger", swaggerFile = "openapi.json")
   }
   ```

3. **Access documentation** at `http://localhost:8080/swagger`

## Critical Guardrails

| Rule | Why It Matters | Enforcement |
|------|----------------|-------------|
| Always use `route()` blocks for grouping | Provides logical API structure and path hierarchy | Validation script checks for `route(` presence |
| Never mix HTTP methods in same route | Violates REST principles, causes routing conflicts | Use separate `get`, `post`, `put`, `delete` blocks |
| Always validate path parameters | Prevents NPE and malformed requests | Use `toIntOrNull()` with error responses |
| Always respond with appropriate status codes | API contracts depend on proper HTTP semantics | Use `HttpStatusCode.OK`, `BadRequest`, `NotFound`, etc. |
| Always write TestApplication tests | Ensures endpoints work before deployment | Required for all new routes |
| Never expose internal exceptions | Security risk, leaks implementation details | Use sealed response types with error messages |

## Quick Reference

### Validation Commands

| Command | Purpose | When to Run |
|---|---|---|
| `./gradlew :server:run` | Run Ktor server locally | During development |
| `./gradlew :server:test` | Run server integration tests | After adding endpoints |
| `.claude/skills/ktor-backend/scripts/validate-endpoint.sh <file>` | Validate endpoint conventions | After creating routes |
| `bash -n <script>` | Check shell script syntax | Before committing scripts |

### Ktor Routing Patterns

| Pattern | Example | Use Case |
|---------|---------|----------|
| Simple GET | `get("/") { call.respondText("Hello") }` | Basic endpoints |
| Path parameter | `get("/{id}") { val id = call.parameters["id"] }` | Resource lookup |
| Query parameter | `get("/search") { val q = call.request.queryParameters["q"] }` | Search/filter |
| Nested routes | `route("/api/v1") { route("/pokemon") { } }` | API versioning |
| POST with body | `post { val body = call.receive<Request>() }` | Create resources |

### Request/Response Templates

**GET endpoint:**
```kotlin
get("/pokemon/{id}") {
    val id = call.parameters["id"]?.toIntOrNull()
        ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            ErrorResponse(message = "Invalid ID", code = 400)
        )

    val pokemon = repository.getPokemonById(id)
    call.respond(HttpStatusCode.OK, pokemon)
}
```

**POST endpoint:**
```kotlin
post("/pokemon") {
    val request = try {
        call.receive<CreatePokemonRequest>()
    } catch (e: Exception) {
        return@post call.respond(
            HttpStatusCode.BadRequest,
            ErrorResponse(message = "Invalid request body", code = 400)
        )
    }

    val pokemon = repository.createPokemon(request)
    call.respond(HttpStatusCode.Created, pokemon)
}
```

**Error response:**
```kotlin
@Serializable
data class ErrorResponse(
    val message: String,
    val code: Int,
    val details: Map<String, String>? = null
)
```

## Cross-References

| Document | Purpose | Link |
|---|---|---|
| Architecture + conventions | Master reference for architecture and patterns | [conventions.md](../../../docs/tech/conventions.md) |
| Critical patterns | 6 core patterns including Either boundary | [critical_patterns_quick_ref.md](../../../docs/tech/critical_patterns_quick_ref.md) |
| Testing strategy | Kotest, MockK, Turbine for integration tests | [testing_strategy.md](../../../docs/tech/testing_strategy.md) |
| Ktor documentation | Official Ktor server documentation | https://ktor.io/docs/ |
| Version catalog | Dependency versions for Ktor plugins | [libs.versions.toml](../../../gradle/libs.versions.toml) |

### Reference Implementation

**Current server setup:**
- [Application.kt](../../../server/src/main/kotlin/com/minddistrict/multiplatformpoc/Application.kt) - Basic Ktor server with Netty
- [server/build.gradle.kts](../../../server/build.gradle.kts) - Server build configuration

**Examples to reference:**
- Ktor routing: `Application.kt` shows basic `route()` and `get()` usage
- Serialization: Add `ContentNegotiation` plugin with `kotlinx.serialization`
- Testing: Use `TestApplication` for integration tests

### Recommended Ktor Plugins

| Plugin | Purpose | Version Catalog Key |
|--------|---------|---------------------|
| ContentNegotiation | JSON serialization/deserialization | `ktor.serverContentNegotiation` |
| Serialization | kotlinx.serialization support | `ktor.serializationJson` |
| StatusPages | Global error handling | `ktor.serverStatusPages` |
| CallLogging | Request/response logging | `ktor.serverCallLogging` |
| CORS | Cross-Origin Resource Sharing | `ktor.serverCors` |
| Compression | Gzip compression | `ktor.serverCompression` |
| Swagger | OpenAPI documentation | `ktor.swagger` |
