# Testing API Services

## Ktor MockEngine

Use `MockEngine` to simulate network responses and errors without hitting real endpoints.

```kotlin
val mockEngine = MockEngine { request ->
    when (request.url.encodedPath) {
        "/jobs" -> respond(
            content = """{"jobs": [{"id": "1", "title": "Dev"}]}""",
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
        else -> respondError(HttpStatusCode.NotFound)
    }
}

val client = createHttpClient(mockEngine)
val apiService = JobApiServiceImpl(client)
```

## DTO Round-Trip Tests

Verify that serialization and deserialization are symmetric.

```kotlin
"JobResponse should be symmetric" {
    val response = JobResponse(id = "1", title = "Developer")
    val json = Json.encodeToString(response)
    val decoded = Json.decodeFromString<JobResponse>(json)
    decoded shouldBe response
}
```

## Error Simulation

Test how the service handles HTTP errors.

```kotlin
val errorEngine = MockEngine { _ ->
    respond(
        content = "Internal Server Error",
        status = HttpStatusCode.InternalServerError
    )
}
```
