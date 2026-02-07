# Coroutines & Concurrency Patterns

Coroutine patterns for testable, predictable, lifecycle-aware code.

## Scopes

### ViewModel Scope

```kotlin
class MyViewModel(
    viewModelScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )
) : ViewModel(viewModelScope) {
    fun doWork() {
        viewModelScope.launch {
            // Automatically cancelled when ViewModel is cleared
        }
    }
}
```

**Critical rules**:
- Pass to `ViewModel()` constructor (NOT stored as field)
- Uses `SupervisorJob()` so one failure doesn't cancel siblings
- `Dispatchers.Main.immediate` for immediate UI updates

### Background Scope (Repositories)

```kotlin
class MyRepository(
    private val ioDispatcher: CoroutineDispatcher,
    private val backgroundScope: CoroutineScope
) {
    // Construction via DI
    // backgroundScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    fun startBackgroundWork() {
        backgroundScope.launch {
            // Long-running work
        }
    }
}
```

### Application Scope

For jobs that must outlive screens/features:

```kotlin
class AnalyticsManager(
    private val applicationScope: CoroutineScope
) {
    fun trackEvent(event: AnalyticsEvent) {
        applicationScope.launch {
            // Upload analytics - continues even if screen closes
            uploadEvent(event)
        }
    }
}
```

**Use for**:
- Warm caches
- Analytics upload
- Periodic sync
- Any work that must survive configuration changes

## Dispatchers

### Injection Pattern

```kotlin
interface DispatchersProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

class DefaultDispatchersProvider : DispatchersProvider {
    override val main = Dispatchers.Main
    override val io = Dispatchers.IO
    override val default = Dispatchers.Default
}

class TestDispatchersProvider(
    testDispatcher: TestDispatcher
) : DispatchersProvider {
    override val main = testDispatcher
    override val io = testDispatcher
    override val default = testDispatcher
}
```

**Benefits**:
- Test with `StandardTestDispatcher`/`UnconfinedTestDispatcher`
- No real delays in tests
- Predictable execution order

### Usage in Repository

```kotlin
class PokemonRepository(
    private val apiService: PokemonApiService,
    private val dispatchers: DispatchersProvider,
    private val backgroundScope: CoroutineScope
) {
    suspend fun getPokemon(id: Int): Either<RepoError, Pokemon> =
        withContext(dispatchers.io) {
            Either.catch {
                apiService.getPokemon(id).toDomain()
            }.mapLeft { it.toRepoError() }
        }

    fun warmCache() {
        backgroundScope.launch(dispatchers.io) {
            // Pre-fetch popular Pokemon
        }
    }
}
```

## Structured Concurrency

### Avoid GlobalScope

```kotlin
// ❌ WRONG - Unmanaged scope
GlobalScope.launch {
    // No lifecycle, no cancellation
}

// ✅ CORRECT - Structured scope
class MyRepository(
    private val scope: CoroutineScope
) {
    fun doWork() {
        scope.launch {
            // Scoped, cancellable
        }
    }
}
```

### SupervisorJob

Use `SupervisorJob()` when child coroutines should be independent:

```kotlin
class MyViewModel(
    viewModelScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )
) : ViewModel(viewModelScope) {
    fun loadMultiple() {
        viewModelScope.launch {
            // If this fails, sibling continues
            launch { loadData1() }
            launch { loadData2() }  // Still runs even if loadData1 fails
        }
    }
}
```

**Without SupervisorJob**: One failure cancels all siblings.

## Cancellation & Timeouts

### Proper Cancellation Handling

```kotlin
// ✅ CORRECT - Either.catch respects cancellation
suspend fun fetchData(): Either<RepoError, Data> =
    Either.catch {
        apiService.fetchData()
    }.mapLeft { it.toRepoError() }

// ❌ WRONG - Catches CancellationException
suspend fun fetchData(): Either<RepoError, Data> =
    try {
        Either.Right(apiService.fetchData())
    } catch (e: Exception) {  // Don't do this!
        Either.Left(e.toRepoError())
    }
```

**Rule**: Never catch and swallow `CancellationException`. Use `Either.catch { }` which respects cancellation.

### Timeouts

```kotlin
suspend fun fetchWithTimeout(): Either<RepoError, Data> =
    Either.catch {
        withTimeout(5000) {
            apiService.fetchData()
        }
    }.mapLeft {
        when (it) {
            is TimeoutCancellationException -> RepoError.Timeout
            else -> it.toRepoError()
        }
    }
```

### Propagating Cancellation

```kotlin
class PokemonApiService(
    private val httpClient: HttpClient
) {
    suspend fun getPokemon(id: Int): PokemonDto {
        // Ktor automatically uses coroutineContext for cancellation
        return httpClient.get("/pokemon/$id").body()
    }
}
```

Ktor, Room, and other KMP libraries automatically respect coroutine cancellation.

## Arrow Patterns

### Either in Suspend Functions

```kotlin
class UserRepository {
    suspend fun getUser(id: String): Either<RepoError, User> =
        Either.catch {
            apiService.getUser(id).toDomain()
        }.mapLeft { it.toRepoError() }
}
```

### Monad Comprehensions

For orchestrating multiple repository calls:

```kotlin
class CheckoutUseCase(
    private val cartRepository: CartRepository,
    private val paymentRepository: PaymentRepository,
    private val orderRepository: OrderRepository
) {
    suspend fun checkout(
        userId: String,
        paymentMethodId: String
    ): Either<RepoError, Order> = either {
        // Each bind() short-circuits on Left
        val cart = cartRepository.getCart(userId).bind()
        val payment = paymentRepository.processPayment(
            cart.total,
            paymentMethodId
        ).bind()
        val order = orderRepository.createOrder(
            userId = userId,
            items = cart.items,
            paymentId = payment.id
        ).bind()

        cartRepository.clearCart(userId).bind()

        order
    }
}
```

**Benefits**:
- Linear code flow (not nested callbacks)
- Early exit on first error
- Type-safe error handling

## Testing Coroutines

### TestDispatcher Setup

```kotlin
class MyViewModelTest {
    private lateinit var viewModel: MyViewModel
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @BeforeTest
    fun setup() {
        viewModel = MyViewModel(
            repository = mockRepository,
            viewModelScope = testScope
        )
    }

    @AfterTest
    fun tearDown() {
        testScope.cancel()
    }

    @Test
    fun `loads data successfully`() = testScope.runTest {
        viewModel.loadData()

        viewModel.uiState.test {
            assertEquals(UiState.Loading, awaitItem())
            assertEquals(UiState.Content(data), awaitItem())
        }
    }
}
```

### Turbine for Flow Testing

```kotlin
@Test
fun `emits loading then content`() = runTest {
    viewModel.uiState.test {
        // Initial state
        assertEquals(UiState.Loading, awaitItem())

        // Trigger load
        viewModel.onUiEvent(UiEvent.Load)

        // Loading state
        assertEquals(UiState.Loading, awaitItem())

        // Content state
        assertEquals(UiState.Content(data), awaitItem())

        cancelAndIgnoreRemainingEvents()
    }
}
```

### Testing Time-Based Operations

```kotlin
@Test
fun `retries with delay`() = runTest {
    coEvery { repository.load() } throws NetworkException()

    viewModel.loadWithRetry()

    // Advance time to skip delay
    advanceTimeBy(5000)

    viewModel.uiState.test {
        assertEquals(UiState.Error, awaitItem())
    }
}
```

## Common Patterns

### Launch Multiple Concurrent Requests

```kotlin
fun loadDashboard() {
    viewModelScope.launch {
        val deferredUser = async { userRepository.getUser() }
        val deferredOrders = async { ordersRepository.getOrders() }
        val deferredNotifications = async { notificationsRepository.getUnread() }

        // Wait for all
        val user = deferredUser.await()
        val orders = deferredOrders.await()
        val notifications = deferredNotifications.await()

        // Combine results
        _uiState.value = DashboardUiState(
            user = user,
            orders = orders,
            notifications = notifications
        )
    }
}
```

### Flow Collection in ViewModel

```kotlin
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob())
) : ViewModel(viewModelScope) {

    init {
        viewModelScope.launch {
            settingsRepository.settingsFlow
                .map { it.toUiState() }
                .collect { _uiState.value = it }
        }
    }
}
```

**Note**: Flow collection in `init` is acceptable because it's lifecycle-agnostic (not loading data from network).

### Retry with Exponential Backoff

```kotlin
suspend fun <T> retryWithBackoff(
    times: Int = 3,
    initialDelay: Long = 100,
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            return block()
        } catch (e: IOException) {
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong()
        }
    }
    return block()  // Last attempt
}
```

## Reference Implementations

- `features/pokemonlist/presentation/PokemonListViewModel.kt`
- `features/pokemondetail/presentation/PokemonDetailViewModel.kt`

## Documentation Sources

- [coroutines.md](../../docs/tech/coroutines.md) — Original guidelines
- [@kmp-testing-strategy skill](See @kmp-testing-strategy skill) — Testing patterns
