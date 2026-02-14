# Ktor Endpoint Patterns

Complete guide for creating REST endpoints, API versioning, and request/response handling.

## Workflow 1: Create New REST Endpoint

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
   .agents/ktor-backend/scripts/validate-endpoint.sh server/src/main/kotlin/com/minddistrict/multiplatformpoc/routes/PokemonRoutes.kt
   ```

## Workflow 2: Add API Versioning

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

## Request/Response Templates

### GET endpoint:
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

### POST endpoint:
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

### Error response:
```kotlin
@Serializable
data class ErrorResponse(
    val message: String,
    val code: Int,
    val details: Map<String, String>? = null
)
```
