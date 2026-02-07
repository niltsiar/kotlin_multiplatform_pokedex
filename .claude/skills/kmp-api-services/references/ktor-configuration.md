# Ktor Configuration Patterns

## Centralized Client Factory

Centralize Ktor client creation to ensure consistent logging, timeouts, and serialization across the app.

```kotlin
fun createHttpClient(engine: HttpClientEngine): HttpClient {
    return HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        
        install(Logging) {
            level = LogLevel.INFO
        }
        
        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 15000
        }
    }
}
```

## Platform Engines

- **Android**: `OkHttp`
- **iOS**: `Darwin`
- **Desktop**: `CIO` or `OkHttp`

## Best Practices

- **Timeouts**: Always set explicit timeouts to avoid hanging requests.
- **Serialization**: Use `ignoreUnknownKeys = true` to remain resilient to API changes.
- **Base URL**: Inject base URLs via configuration or DI rather than hardcoding.
