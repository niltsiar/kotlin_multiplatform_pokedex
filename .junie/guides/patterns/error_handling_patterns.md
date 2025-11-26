# Error Handling Patterns (Arrow Either)

**Last Updated:** November 26, 2025

> **Canonical Reference**: See [Either Boundary Pattern](../tech/critical_patterns_quick_ref.md#either-boundary-pattern) for core rules.

> Comprehensive code examples for Arrow Either boundaries, sealed error hierarchies, and exception mapping.

## Core Principle

**Repositories MUST return `Either<RepoError, T>`. NEVER throw, return null, or use `Result`.**

## Either Boundaries at Repository Layer

### Basic Repository Pattern

```kotlin
// :features:pokemon:api - Public contract
interface PokemonRepository {
    suspend fun getById(id: Int): Either<RepoError, Pokemon>
    suspend fun loadPage(offset: Int = 0): Either<RepoError, PokemonPage>
}

// :features:pokemon:data - Implementation
internal class PokemonRepositoryImpl(
    private val api: PokemonApiService
) : PokemonRepository {
    
    override suspend fun getById(id: Int): Either<RepoError, Pokemon> =
        Either.catch {
            val dto = api.getPokemon(id)
            dto.toDomain()
        }.mapLeft { it.toRepoError() }
    
    override suspend fun loadPage(offset: Int): Either<RepoError, PokemonPage> =
        Either.catch {
            val response = api.getPokemonList(limit = 20, offset = offset)
            PokemonPage(
                pokemons = response.results.map { it.toDomain() }.toImmutableList(),
                hasMore = response.next != null
            )
        }.mapLeft { it.toRepoError() }
}
```

## Sealed Error Hierarchies

### Feature-Specific Errors

```kotlin
// :features:pokemon:api - Public error types
sealed interface RepoError {
    data object Network : RepoError
    data class Http(val code: Int, val message: String?) : RepoError
    data object Unauthorized : RepoError
    data object NotFound : RepoError
    data class Unknown(val cause: Throwable) : RepoError
}

// Extension to map UI messages
fun RepoError.toUiMessage(): String = when (this) {
    is RepoError.Network -> "No internet connection"
    is RepoError.Http -> "Server error: $message"
    is RepoError.Unauthorized -> "Please log in again"
    is RepoError.NotFound -> "Item not found"
    is RepoError.Unknown -> "Something went wrong"
}
```

### Domain-Specific Errors

```kotlin
// :features:auth:api - Authentication-specific errors
sealed interface AuthError {
    data object InvalidCredentials : AuthError
    data object AccountLocked : AuthError
    data object EmailNotVerified : AuthError
    data class TooManyAttempts(val retryAfter: Duration) : AuthError
    data object Network : AuthError
    data class Unknown(val cause: Throwable) : AuthError
}

// :features:payment:api - Payment-specific errors
sealed interface PaymentError {
    data object InsufficientFunds : PaymentError
    data object CardExpired : PaymentError
    data object CardDeclined : PaymentError
    data object InvalidAmount : PaymentError
    data object Network : PaymentError
    data class Unknown(val cause: Throwable) : PaymentError
}
```

## Exception Mapping

### HTTP Error Mapping

```kotlin
import io.ktor.client.plugins.*
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException

// :features:pokemon:data - Extension functions
fun Throwable.toRepoError(): RepoError = when (this) {
    // Ktor client exceptions
    is ClientRequestException -> when (response.status.value) {
        401 -> RepoError.Unauthorized
        404 -> RepoError.NotFound
        in 400..499 -> RepoError.Http(response.status.value, message)
        else -> RepoError.Unknown(this)
    }
    
    is ServerResponseException -> 
        RepoError.Http(response.status.value, "Server error: $message")
    
    // Network exceptions
    is ConnectException,
    is SocketTimeoutException,
    is IOException -> RepoError.Network
    
    // Unknown
    else -> RepoError.Unknown(this)
}

// Property-based test for HTTP error mapping
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

### Cancellation Handling (CRITICAL)

```kotlin
import kotlinx.coroutines.CancellationException

// ✅ CORRECT - Either.catch respects cancellation
override suspend fun getJobs(): Either<RepoError, List<Job>> =
    Either.catch {
        // CancellationException is automatically re-thrown
        api.getJobs().map { it.asDomain() }
    }.mapLeft { it.toRepoError() }

// ❌ WRONG - Swallowing CancellationException
override suspend fun getJobs(): Either<RepoError, List<Job>> =
    try {
        Either.Right(api.getJobs().map { it.asDomain() })
    } catch (e: Exception) {  // Catches CancellationException too!
        Either.Left(e.toRepoError())
    }

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
    private val repository: PokemonListRepository,
    viewModelScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )
) : ViewModel(viewModelScope),
    UiStateHolder<PokemonListUiState, PokemonListUiEvent> {
    
    private val _uiState = MutableStateFlow<PokemonListUiState>(
        PokemonListUiState.Loading
    )
    override val uiState: StateFlow<PokemonListUiState> = _uiState
    
    fun start(lifecycle: Lifecycle) {
        viewModelScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.loadPage().fold(
                    ifLeft = { error ->
                        _uiState.value = PokemonListUiState.Error(
                            message = error.toUiMessage()
                        )
                    },
                    ifRight = { page ->
                        _uiState.value = PokemonListUiState.Content(
                            pokemons = page.pokemons,
                            hasMore = page.hasMore,
                            isLoadingMore = false
                        )
                    }
                )
            }
        }
    }
}
```

### Either DSL (Chaining Operations)

```kotlin
import arrow.core.raise.either
import arrow.core.raise.ensure

// Use case with multiple repository calls
class SubmitOrderUseCase(
    private val cartRepo: CartRepository,
    private val inventoryRepo: InventoryRepository,
    private val paymentRepo: PaymentRepository,
    private val orderRepo: OrderRepository
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
        
        // Create order
        val order = orderRepo.create(cart, payment).bind()
        
        // Return receipt
        Receipt(
            orderId = order.id,
            amount = payment.amount,
            items = cart.items
        )
    }
}

sealed interface OrderError {
    data object EmptyCart : OrderError
    data object InvalidTotal : OrderError
    data object ReservationFailed : OrderError
    data object PaymentFailed : OrderError
    data class Unknown(val cause: Throwable) : OrderError
}
```

## Testing Either Boundaries

### Success Path Testing

```kotlin
"should return Right with pokemon on success" {
    coEvery { mockApi.getPokemon(1) } returns PokemonDto(
        id = 1,
        name = "bulbasaur",
        sprites = SpritesDto(
            frontDefault = "https://example.com/1.png"
        ),
        types = listOf(
            TypeSlotDto(
                slot = 1,
                type = TypeDto(name = "grass")
            )
        )
    )
    
    val result = repository.getById(1)
    
    result.shouldBeRight { pokemon ->
        pokemon.id shouldBe 1
        pokemon.name shouldBe "Bulbasaur"
        pokemon.types shouldContain "grass"
    }
}
```

### Error Path Testing

```kotlin
"should return Network error on timeout" {
    coEvery { mockApi.getPokemon(any()) } throws 
        ConnectTimeoutException("Connection timed out")
    
    val result = repository.getById(1)
    
    result.shouldBeLeft { error ->
        error shouldBe RepoError.Network
    }
}

"should return Unauthorized on 401" {
    coEvery { mockApi.getPokemon(any()) } throws 
        ClientRequestException(
            mockk { every { status.value } returns 401 }
        )
    
    val result = repository.getById(1)
    
    result.shouldBeLeft { error ->
        error shouldBe RepoError.Unauthorized
    }
}

// Property-based test for all HTTP error codes
"should map all HTTP errors correctly" {
    checkAll(
        Arb.int(400..499),
        Arb.int(500..599)
    ) { clientCode, serverCode ->
        // Test 4xx codes
        coEvery { mockApi.getPokemon(1) } throws 
            ClientRequestException(
                mockk { every { status.value } returns clientCode }
            )
        
        val result1 = repository.getById(1)
        result1.shouldBeLeft().shouldBeInstanceOf<RepoError.Http>()
        
        // Test 5xx codes
        coEvery { mockApi.getPokemon(2) } throws 
            ServerResponseException(
                mockk { every { status.value } returns serverCode }
            )
        
        val result2 = repository.getById(2)
        result2.shouldBeLeft().shouldBeInstanceOf<RepoError.Http>()
    }
}
```

### Helper Extensions for Testing

```kotlin
// Test helper extensions
fun <L, R> Either<L, R>.shouldBeRight(): R =
    this.getOrNull() ?: fail("Expected Right but was $this")

fun <L, R> Either<L, R>.shouldBeLeft(): L =
    this.swap().getOrNull() ?: fail("Expected Left but was $this")

// Usage with Kotest smart casting
val result = repository.getById(1)

result.shouldBeRight { pokemon ->
    // Smart cast - pokemon is of type Pokemon
    pokemon.id shouldBe 1
    pokemon.name shouldBe "Bulbasaur"
}

result.shouldBeLeft { error ->
    // Smart cast - error is of type RepoError
    error.shouldBeInstanceOf<RepoError.Network>()
}
```

## Anti-Patterns to Avoid

### ❌ DON'T: Return Result Type

```kotlin
// ❌ WRONG - Using Kotlin Result
suspend fun getJobs(): Result<List<Job>> =
    runCatching {
        api.getJobs().map { it.asDomain() }
    }

// ✅ CORRECT - Using Arrow Either
suspend fun getJobs(): Either<RepoError, List<Job>> =
    Either.catch {
        api.getJobs().map { it.asDomain() }
    }.mapLeft { it.toRepoError() }
```

### ❌ DON'T: Return Nullable

```kotlin
// ❌ WRONG - Nullable return (loses error information)
suspend fun getJob(id: String): Job? =
    try {
        api.getJob(id).asDomain()
    } catch (e: Exception) {
        null  // What went wrong? Network? Not found? Unknown?
    }

// ✅ CORRECT - Either with error details
suspend fun getJob(id: String): Either<RepoError, Job> =
    Either.catch {
        api.getJob(id).asDomain()
    }.mapLeft { it.toRepoError() }
```

### ❌ DON'T: Throw Exceptions at Boundaries

```kotlin
// ❌ WRONG - Throwing exceptions from repository
suspend fun getJobs(): List<Job> =
    api.getJobs().map { it.asDomain() }  // Can throw!

// ✅ CORRECT - Catching and mapping to Either
suspend fun getJobs(): Either<RepoError, List<Job>> =
    Either.catch {
        api.getJobs().map { it.asDomain() }
    }.mapLeft { it.toRepoError() }
```

### ❌ DON'T: Swallow CancellationException

```kotlin
// ❌ WRONG - Catching CancellationException
suspend fun getJobs(): Either<RepoError, List<Job>> =
    try {
        Either.Right(api.getJobs().map { it.asDomain() })
    } catch (e: Exception) {  // Catches CancellationException!
        Either.Left(e.toRepoError())
    }

// ✅ CORRECT - Use Either.catch which respects cancellation
suspend fun getJobs(): Either<RepoError, List<Job>> =
    Either.catch {
        api.getJobs().map { it.asDomain() }
    }.mapLeft { it.toRepoError() }
```

## See Also

- `.junie/guides/tech/repository.md` — Complete repository pattern guide
- `.junie/guides/tech/coroutines.md` — Cancellation and structured concurrency
- `patterns/di_patterns.md` — DI patterns with Either
- `patterns/viewmodel_patterns.md` — Using Either in ViewModels
- `patterns/testing_patterns.md` — Testing Either boundaries
