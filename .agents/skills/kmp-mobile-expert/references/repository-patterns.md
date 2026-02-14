# Repository Patterns

Detailed repository implementation patterns for Kotlin Multiplatform.

## Either Boundary Pattern

All repositories MUST return `Either<RepoError, T>` with proper error mapping.

### Complete Example

```kotlin
// Define sealed error hierarchy in :api module
sealed interface RepoError {
    data class Network(val cause: Throwable) : RepoError
    data class Http(val code: Int, val message: String?) : RepoError
    data class Unknown(val cause: Throwable) : RepoError
}

// Repository interface (in :api module)
interface PokemonListRepository {
    suspend fun loadPage(limit: Int, offset: Int): Either<RepoError, PokemonPage>
}

// Implementation with factory function (in :data module)
internal class PokemonListRepositoryImpl(
    private val apiService: PokemonListApiService
) : PokemonListRepository {
    
    override suspend fun loadPage(limit: Int, offset: Int): Either<RepoError, PokemonPage> =
        withContext(Dispatchers.IO) {
            catch({
                val dto = apiService.getPokemonList(limit, offset)
                Either.Right(dto.toDomain())
            }) { throwable ->
                Either.Left(throwable.toRepoError())
            }
        }
}

// Public factory function
fun PokemonListRepository(apiService: PokemonListApiService): PokemonListRepository =
    PokemonListRepositoryImpl(apiService)

// Error mapping extension
private fun Throwable.toRepoError(): RepoError = when (this) {
    is ClientRequestException -> RepoError.Http(response.status.value, message)
    is ServerResponseException -> RepoError.Http(response.status.value, message)
    is HttpRequestTimeoutException,
    is ConnectTimeoutException,
    is SocketTimeoutException -> RepoError.Network
    else -> RepoError.Unknown(this)
}
```

## Key Requirements

1. **Interface in `:api`**: Repository interfaces belong in `:api` module
2. **Implementation in `:data`**: `internal class <Interface>Impl` pattern
3. **Factory function**: Public function `fun <Interface>(...): <Interface>`
4. **Either return type**: Never `Result`, never nullable, never throwing
5. **DTO mapping**: Map DTOs to domain models at repository boundary
6. **Error mapping**: Use `catch { }` with `.mapLeft { it.toRepoError() }`
7. **Cancellation**: `Either.catch` automatically respects `CancellationException`

## Common Imports

```kotlin
import arrow.core.Either
import arrow.core.raise.catch
```

## Error Handling Edge Cases

| Scenario | Handling |
|----------|----------|
| Network timeout | Map to `RepoError.Network` |
| HTTP 4xx/5xx | Map to `RepoError.Http` with status code |
| Unexpected exception | Map to `RepoError.Unknown` |
| CancellationException | Automatically propagated by `Either.catch` |

## Testing Pattern

```kotlin
class PokemonListRepositorySpec : StringSpec({
    "returns Right on success" {
        coEvery { api.getPokemonList(20, 0) } returns mockDto
        val result = repository.loadPage(20, 0)
        result.shouldBeRight()
    }

    "returns Left on network error" {
        coEvery { api.getPokemonList(any(), any()) } throws IOException()
        val result = repository.loadPage(20, 0)
        result.shouldBeLeft().shouldBeInstanceOf<RepoError.Network>()
    }
})
```
