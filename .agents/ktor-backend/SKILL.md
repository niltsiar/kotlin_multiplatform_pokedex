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
- Client-side HTTP requests (use @kmp-api-services for Ktor Client and API service patterns)
- Compose UI implementation (use compose-screen skill)
- Shared business logic (use kmp-mobile-expert skill)
- Database schema design (this project uses PokéAPI, no database layer yet)

## Mode Detection

| User Request | Reference File | Load When |
|--------------|----------------|-----------|
| "Create endpoint", "REST API", "routing", "API versioning", "request validation" | [endpoint-patterns.md](references/endpoint-patterns.md) | MANDATORY - Read before implementing endpoints |

**MANDATORY - READ ENTIRE FILE**: Before implementing Ktor endpoints, you MUST read [endpoint-patterns.md](references/endpoint-patterns.md) (~180 lines) for complete routing, versioning, and validation patterns.

**Do NOT load** for OpenAPI/Swagger setup or server feature configuration (CORS, compression, logging) - those remain in main SKILL.md.

## Related Skills

- **@kmp-api-services**: Client-side HTTP patterns with Ktor Client, DTOs, and repository integration
- **@kmp-data-layer**: Repository implementation with Either<RepoError, T> error boundaries
- **@kmp-testing-patterns**: Kotest and MockEngine patterns for API service testing
- **@kmp-architecture**: Module structure, vertical slicing, and feature boundaries
- **@kmp-critical-patterns**: Quick reference for 6 core patterns (ViewModel, Either, Testing, etc.)
- **@kmp-domain**: Domain models and data classes consumed by server endpoints
- **@kmp-gradle**: Gradle convention plugins and server build configuration
- **@kmp-commands**: CLI commands for server validation and testing

## Decision Framework

Before implementing Ktor endpoints, ask yourself:

1. **What HTTP method and route structure?**
   - GET for reads → `/api/v1/resources/{id}`
   - POST for creates → `/api/v1/resources` with request body
   - PUT for updates → `/api/v1/resources/{id}` with request body
   - DELETE for deletes → `/api/v1/resources/{id}`
   - Use versioned routes (`/api/v1/`) for API stability

2. **How should errors be handled?**
   - Validation errors → `400 Bad Request` with error details
   - Not found → `404 Not Found`
   - Server errors → `500 Internal Server Error` (log details, return generic message)
   - Use `call.respond(HttpStatusCode.X, ErrorResponse(...))`

3. **What testing is needed?**
   - Use `testApplication { }` for endpoint tests
   - Test success path + all error cases (validation, not found, server error)
   - Verify response serialization with `@Serializable` models
   - Run server tests: `./gradlew :server:test`

## Essential Workflows

### Workflow 1: Create New REST Endpoint

**MANDATORY**: Read [endpoint-patterns.md](references/endpoint-patterns.md) for complete details.

Quick steps:
1. Define `@Serializable` request/response models with sealed interfaces for success/error
2. Create nested `route()` blocks with version prefix (`/api/v1`)
3. Add parameter validation with `toIntOrNull()` and error responses
4. Write `TestApplication` integration tests
5. Validate with script: `.agents/ktor-backend/scripts/validate-endpoint.sh`

See [endpoint-patterns.md](references/endpoint-patterns.md) for full code examples and templates.

### Workflow 2: Add API Versioning

**MANDATORY**: Read [endpoint-patterns.md](references/endpoint-patterns.md) for complete versioning strategy.

Quick steps:
1. Route by version prefix (`/api/v1`, `/api/v2`)
2. Use version-specific sealed response types
3. Add deprecation headers to old versions

See [endpoint-patterns.md](references/endpoint-patterns.md) for full implementation.

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

### NEVER List

| NEVER Do | Why | Correct Pattern |
|----------|-----|-----------------|
| **NEVER** use string concatenation for route paths | Creates brittle, error-prone routing | Use nested `route()` blocks for hierarchy |
| **NEVER** expose internal exceptions to clients | Security risk, leaks implementation details | Use sealed response types with sanitized error messages |
| **NEVER** mix multiple HTTP methods in same route block | Violates REST principles, causes routing conflicts | Separate `get { }`, `post { }`, `put { }`, `delete { }` blocks |
| **NEVER** skip validation of path/query parameters | Causes NPE and malformed request handling | Always use `toIntOrNull()` with fallback error responses |
| **NEVER** write endpoints without TestApplication tests | Undetected bugs reach production | Write integration tests for every route |
| **NEVER** use hardcoded strings for route paths | Makes refactoring difficult and error-prone | Define route constants or use typed route objects |
| **NEVER** return 200 OK for error responses | Breaks API contracts and client error handling | Use proper status codes: `BadRequest`, `NotFound`, `InternalServerError` |
| **NEVER** ignore content negotiation setup | Client receives unexpected formats | Always configure `ContentNegotiation` with JSON |

### Best Practices

| Rule | Why It Matters | Enforcement |
|------|----------------|-------------|
| Always use `route()` blocks for grouping | Provides logical API structure and path hierarchy | Validation script checks for `route(` presence |
| Always validate path parameters | Prevents NPE and malformed requests | Use `toIntOrNull()` with error responses |
| Always respond with appropriate status codes | API contracts depend on proper HTTP semantics | Use `HttpStatusCode.OK`, `BadRequest`, `NotFound`, etc. |
| Always write TestApplication tests | Ensures endpoints work before deployment | Required for all new routes |

## Quick Reference

### Validation Commands

| Command | Purpose | When to Run |
|---|---|---|
| `./gradlew :server:run` | Run Ktor server locally | During development |
| `./gradlew :server:test` | Run server integration tests | After adding endpoints |
| `.agents/ktor-backend/scripts/validate-endpoint.sh <file>` | Validate endpoint conventions | After creating routes |
| `bash -n <script>` | Check shell script syntax | Before committing scripts |

### Ktor Routing Patterns

| Pattern | Example | Use Case |
|---------|---------|----------|
| Simple GET | `get("/") { call.respondText("Hello") }` | Basic endpoints |
| Path parameter | `get("/{id}") { val id = call.parameters["id"] }` | Resource lookup |
| Query parameter | `get("/search") { val q = call.request.queryParameters["q"] }` | Search/filter |
| Nested routes | `route("/api/v1") { route("/pokemon") { } }` | API versioning |
| POST with body | `post { val body = call.receive<Request>() }` | Create resources |

**For complete endpoint templates**: See [endpoint-patterns.md](references/endpoint-patterns.md)

## Cross-References

| Document | Purpose | Link |
|---|---|---|
| Architecture + conventions | Master reference for architecture and patterns | [@kmp-architecture](../kmp-architecture/SKILL.md) |
| Critical patterns | 6 core patterns including Either boundary | [@kmp-critical-patterns](../kmp-critical-patterns/SKILL.md) |
| Testing strategy | Kotest, MockK, Turbine for integration tests | [@kmp-testing-strategy skill](See @kmp-testing-strategy skill) |
| Ktor documentation | Official Ktor server documentation | https://ktor.io/docs/ |
| Version catalog | Dependency versions for Ktor plugins | [libs.versions.toml](../../gradle/libs.versions.toml) |
| Module structure | Feature boundaries and layer organization | [@kmp-architecture skill](../kmp-architecture/SKILL.md) |
| Critical patterns | Quick reference for core patterns | [@kmp-critical-patterns skill](../kmp-critical-patterns/SKILL.md) |
| Domain models | Immutable data classes | [@kmp-domain skill](../kmp-domain/SKILL.md) |
| Gradle configuration | Convention plugins and server build | [@kmp-gradle skill](../kmp-gradle/SKILL.md) |
| Server commands | CLI validation and testing | [@kmp-commands skill](../kmp-commands/SKILL.md) |

### Reference Implementation

**Current server setup:**
- [Application.kt](../../server/src/main/kotlin/com/minddistrict/multiplatformpoc/Application.kt) - Basic Ktor server with Netty
- [server/build.gradle.kts](../../server/build.gradle.kts) - Server build configuration

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
