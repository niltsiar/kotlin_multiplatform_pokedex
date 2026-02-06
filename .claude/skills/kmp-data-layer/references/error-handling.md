# Error Handling with Arrow Either

Comprehensive guide to error handling patterns using Arrow Either in repositories.

## Table of Contents

1. [RepoError Sealed Class](#repoerror-sealed-class)
2. [Either Boundaries](#either-boundaries)
3. [Exception Mapping](#exception-mapping)
4. [Cancellation Handling](#cancellation-handling)
5. [Using Either in ViewModels](#using-either-in-viewmodels)
6. [Domain-Specific Errors](#domain-specific-errors)

## RepoError Sealed Class

### Basic Structure

```kotlin
// :features:<feature>:api - Public error contract
sealed interface RepoError {
    data object Network : RepoError
    data class Http(val code: Int, val message: String?) : RepoError
    data object NotFound : RepoError
    data object Unauthorized : RepoError
    data class Unknown(val cause: Throwable) : RepoError
}
```

### With UI Messages

```kotlin
// Extension for user-friendly messages
fun RepoError.toUiMessage(): String = when (this) {
    is RepoError.Network -> "No internet connection. Please check your network."
    is RepoError.Http -> "Server error: $message"
    is RepoError.Unauthorized -> "Session expired. Please log in again."
    is RepoError.NotFound -> "Item not found."
    is RepoError.Unknown -> "Something went wrong. Please try again."
}

// Extension for logging
fun RepoError.toLogMessage(): String = when (this) {
    is RepoError.Network -> "Network error"
    is RepoError.Http -> "HTTP ${code}: $message"
    is RepoError.Unauthorized -> "Unauthorized"
    is RepoError.NotFound -> "Not found"
    is RepoError.Unknown -> "Unknown error: ${cause.message}"
}
```

## Either Boundaries

### Basic Pattern

```kotlin
override suspend fun getJobs(): Either<RepoError, List<Job>> =
    Either.catch {
        api.getJobs().map { it.asDomain() }
    }.mapLeft { it.toRepoError() }
```

### What's Happening

1. **Either.catch { }**: Executes block, catches exceptions, returns `Either<Throwable, T>`
2. **mapLeft { }**: Transforms `Throwable` to `RepoError`
3. **Result**: `Either<RepoError, List<Job>>`

### Success Path
```kotlin
val result: Either<RepoError, List<Job>> = repository.getJobs()

result.fold(
    ifLeft = { error -> handleError(error) },
    ifRight = { jobs -> displayJobs(jobs) }
)
```

## Exception Mapping

### HTTP Error Mapping

```kotlin
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException

fun Throwable.toRepoError(): RepoError = when (this) {
    // Ktor client exceptions - 4xx
    is ClientRequestException -> when (response.status.value) {
        401 -> RepoError.Unauthorized
        404 -> RepoError.NotFound
        in 400..499 -> RepoError.Http(response.status.value, message)
        else -> RepoError.Unknown(this)
    }
    
    // Ktor client exceptions - 5xx
    is ServerResponseException -> 
        RepoError.Http(response.status.value, "Server error: $message")
    
    // Network exceptions
    is ConnectException,
    is SocketTimeoutException,
    is IOException -> RepoError.Network
    
    // Unknown
    else -> RepoError.Unknown(this)
}
```

### Testing Error Mapping

```kotlin
"should map 401 to Unauthorized" {
    val exception = ClientRequestException(
        mockk { every { status.value } returns 401 }
    )
    
    val error = exception.toRepoError()
    
    error shouldBe RepoError.Unauthorized
}

"should map all 4xx codes to Http error" {
    checkAll(Arb.int(400..499)) { code ->
        val exception = ClientRequestException(
            mockk { every { status.value } returns code }
        )
        
        val error = exception.toRepoError()
        
        when (code) {
            401 -> error shouldBe RepoError.Unauthorized
            404 -> error shouldBe RepoError.NotFound
            else -> {
                error.shouldBeInstanceOf<RepoError.Http>()
                error.code shouldBe code
            }
        }
    }
}
```

## Cancellation Handling

### CORRECT: Either.catch Respects Cancellation

```kotlin
// ✅ CORRECT - CancellationException is automatically re-thrown
override suspend fun getJobs(): Either<RepoError, List<Job>> =
    Either.catch {
        api.getJobs().map { it.asDomain() }
    }.mapLeft { it.toRepoError() }
```

### WRONG: Manual catch with Cancellation

```kotlin
// ❌ WRONG - Catches CancellationException too!
override suspend fun getJobs(): Either<RepoError, List<Job>> =
    try {
        Either.Right(api.getJobs().map { it.asDomain() })
    } catch (e: Exception) {
        Either.Left(e.toRepoError())  // DON'T DO THIS
    }
```

### CORRECT: Manual catch with explicit cancellation

```kotlin
// ✅ CORRECT - Manual try-catch with cancellation check
override suspend fun getJobs(): Either<RepoError, List<Job>> =
    try {
        Either.Right(api.getJobs().map { it.asDomain() })
    } catch (e: CancellationException) {
        throw e  // Always re-throw
    } catch (e: Exception) {
        Either.Left(e.toRepoError())
    }
```

## Using Either in ViewModels

### Fold Pattern

```kotlin
class PokemonListViewModel(
    private val repository: PokemonListRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<PokemonListUiState>(Loading)
    val uiState: StateFlow<PokemonListUiState> = _uiState
    
    private fun loadInitialPage() {
        viewModelScope.launch {
            repository.loadPage().fold(
                ifLeft = { error ->
                    _uiState.value = Error(message = error.toUiMessage())
                },
                ifRight = { page ->
                    _uiState.value = Content(
                        pokemons = page.pokemons,
                        hasMore = page.hasMore
                    )
                }
            )
        }
    }
}
```

### Either DSL (Chaining)

```kotlin
import arrow.core.raise.either
import arrow.core.raise.ensure

class SubmitOrderUseCase(
    private val cartRepo: CartRepository,
    private val inventoryRepo: InventoryRepository,
    private val paymentRepo: PaymentRepository
) {
    suspend operator fun invoke(): Either<OrderError, Receipt> = either {
        // Get cart (binding Either)
        val cart = cartRepo.current().bind()
        
        // Validate
        ensure(cart.items.isNotEmpty()) { OrderError.EmptyCart }
        ensure(cart.total > 0) { OrderError.InvalidTotal }
        
        // Reserve inventory
        inventoryRepo.reserve(cart.items).bind()
        
        // Process payment
        val payment = paymentRepo.charge(cart.total).bind()
        
        // Return receipt
        Receipt(orderId = payment.id, amount = payment.amount)
    }
}
```

### bind() Explained

- `bind()` extracts the right value from `Either<L, R>` or short-circuits with left
- Works inside `either { }` DSL
- If any `.bind()` returns `Left`, the entire block returns that `Left`

## Domain-Specific Errors

### Authentication Errors

```kotlin
// :features:auth:api
sealed interface AuthError {
    data object InvalidCredentials : AuthError
    data object AccountLocked : AuthError
    data object EmailNotVerified : AuthError
    data class TooManyAttempts(val retryAfter: Duration) : AuthError
    data object Network : AuthError
    data class Unknown(val cause: Throwable) : AuthError
}

interface AuthRepository {
    suspend fun login(email: String, password: String): Either<AuthError, User>
    suspend fun register(email: String, password: String): Either<AuthError, User>
}
```

### Payment Errors

```kotlin
// :features:payment:api
sealed interface PaymentError {
    data object InsufficientFunds : PaymentError
    data object CardExpired : PaymentError
    data object CardDeclined : PaymentError
    data object InvalidAmount : PaymentError
    data object Network : PaymentError
    data class Unknown(val cause: Throwable) : PaymentError
}

interface PaymentRepository {
    suspend fun processPayment(amount: Money, card: Card): Either<PaymentError, Receipt>
}
```

### Generic vs Specific Errors

| Scenario | Error Type | Example |
|----------|-----------|---------|
| Generic data fetching | `RepoError` | `getPokemonById()` |
| Authentication | `AuthError` | `login()` |
| Payment | `PaymentError` | `processPayment()` |
| Feature-specific | Custom sealed | `JobPostingError` |

## Anti-Patterns

### DON'T: Return Result

```kotlin
// ❌ WRONG
suspend fun getJobs(): Result<List<Job>> =
    runCatching { api.getJobs().map { it.asDomain() } }

// ✅ CORRECT
suspend fun getJobs(): Either<RepoError, List<Job>> =
    Either.catch { api.getJobs().map { it.asDomain() } }
        .mapLeft { it.toRepoError() }
```

### DON'T: Return Nullable

```kotlin
// ❌ WRONG - Loses error information
suspend fun getJob(id: String): Job? =
    try {
        api.getJob(id).asDomain()
    } catch (e: Exception) {
        null  // What went wrong?
    }

// ✅ CORRECT
suspend fun getJob(id: String): Either<RepoError, Job> =
    Either.catch { api.getJob(id).asDomain() }
        .mapLeft { it.toRepoError() }
```

### DON'T: Throw from Repository

```kotlin
// ❌ WRONG
suspend fun getJobs(): List<Job> =
    api.getJobs().map { it.asDomain() }  // Can throw!

// ✅ CORRECT
suspend fun getJobs(): Either<RepoError, List<Job>> =
    Either.catch { api.getJobs().map { it.asDomain() } }
        .mapLeft { it.toRepoError() }
```
