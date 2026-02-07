# One-Time Events Pattern

Handle navigation, snackbars, toasts, and other one-time UI actions that shouldn't survive configuration changes.

## The Problem

One-time events (like "navigate to detail") shouldn't be part of UI state because:
- They don't represent UI state
- Surviving config changes causes double-navigation
- Composable recomposition triggers them again

**Solution**: EventChannel pattern with Flow consumption.

## Core Pattern

### Interface Definition

```kotlin
// One-time event emitter interface
interface OneTimeEventEmitter<E> {
    val events: Flow<E>
    suspend fun emit(event: E)
}

// Implementation via Channel
class EventChannel<E> : OneTimeEventEmitter<E> {
    private val _events = Channel<E>(Channel.BUFFERED)
    override val events: Flow<E> = _events.receiveAsFlow()

    override suspend fun emit(event: E) {
        _events.send(event)
    }
}
```

### ViewModel Implementation

```kotlin
class LoginViewModel(
    private val authRepository: AuthRepository,
    viewModelScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )
) : ViewModel(viewModelScope),
    UiStateHolder<LoginUiState, LoginUiEvent>,
    OneTimeEventEmitter<LoginNavEvent> by EventChannel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    override val uiState: StateFlow<LoginUiState> = _uiState

    override fun onUiEvent(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.Login -> login(event.email, event.password)
            is LoginUiEvent.SignUpClicked -> {
                viewModelScope.launch {
                    emit(LoginNavEvent.NavigateToSignup)
                }
            }
        }
    }

    private fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            authRepository.login(email, password).fold(
                ifLeft = { error ->
                    _uiState.value = LoginUiState.Error(error.toUiMessage())
                },
                ifRight = { user ->
                    _uiState.value = LoginUiState.Idle
                    emit(LoginNavEvent.NavigateToHome)  // One-time event
                }
            )
        }
    }
}

// UI State (persistent across config changes)
sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Error(val message: String) : LoginUiState
}

// UI Events (user actions)
sealed interface LoginUiEvent {
    data class Login(val email: String, val password: String) : LoginUiEvent
    data object SignUpClicked : LoginUiEvent
}

// One-time Events (navigation, snackbars)
sealed interface LoginNavEvent {
    data object NavigateToHome : LoginNavEvent
    data object NavigateToSignup : LoginNavEvent
    data class ShowSnackbar(val message: String) : LoginNavEvent
}
```

### Compose Consumption

```kotlin
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ CORRECT: Collect one-time events in LaunchedEffect
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LoginNavEvent.NavigateToHome -> onNavigateToHome()
                is LoginNavEvent.NavigateToSignup -> onNavigateToSignup()
                is LoginNavEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    LoginContent(
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
        snackbarHostState = snackbarHostState
    )
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onUiEvent: (LoginUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (uiState) {
            is LoginUiState.Loading -> LoadingIndicator()
            is LoginUiState.Error -> ErrorMessage(uiState.message)
            is LoginUiState.Idle -> LoginForm(onUiEvent)
        }
    }
}
```

## Screen Pattern with One-Time Events

```kotlin
// Main entry point - handles both state and events
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiStateHolder: UiStateHolder<HomeUiState, HomeUiEvent>,
    onNavigate: (destination: Any) -> Unit
) {
    val uiState by uiStateHolder.uiState.collectAsStateWithLifecycle()

    // Collect one-time events if the holder implements OneTimeEventEmitter
    (uiStateHolder as? OneTimeEventEmitter<HomeOneShotEvent>)?.let { emitter ->
        LaunchedEffect(emitter) {
            emitter.events.collect { event ->
                when (event) {
                    is HomeOneShotEvent.NavigateToDetail -> {
                        onNavigate(event.id)
                    }
                    is HomeOneShotEvent.ShowMessage -> {
                        // Show snackbar
                    }
                }
            }
        }
    }

    HomeScreen(
        modifier = modifier,
        uiState = uiState,
        onUiEvent = uiStateHolder::onUiEvent,
        onNavigate = onNavigate
    )
}

// Pure composable for testing and previews
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onUiEvent: (HomeUiEvent) -> Unit,
    onNavigate: (destination: Any) -> Unit
) {
    when (uiState) {
        is HomeUiState.Loading -> Loading()
        is HomeUiState.Error -> ErrorView(
            message = uiState.message
        ) { onUiEvent(HomeUiState.DefaultRetryEvent) }
        is HomeUiState.Content -> ContentList(uiState.items, onUiEvent)
    }
}
```

## Common Event Types

### Navigation Events

```kotlin
sealed interface HomeNavEvent {
    data class NavigateToDetail(val id: String) : HomeNavEvent
    data object NavigateToSettings : HomeNavEvent
    data object NavigateBack : HomeNavEvent
}
```

### Snackbar Events

```kotlin
sealed interface SnackbarEvent {
    data class Show(
        val message: String,
        val actionLabel: String? = null,
        val onAction: (() -> Unit)? = null
    ) : SnackbarEvent

    data object Hide : SnackbarEvent
}
```

### Toast Events

```kotlin
sealed interface ToastEvent {
    data class Show(val message: String) : ToastEvent
}
```

## Error Handling with One-Time Events

```kotlin
class PaymentViewModel(
    private val paymentRepository: PaymentRepository,
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob())
) : ViewModel(viewModelScope),
    UiStateHolder<PaymentUiState, PaymentUiEvent>,
    OneTimeEventEmitter<PaymentEvent> by EventChannel() {

    override fun onUiEvent(event: PaymentUiEvent) {
        when (event) {
            is PaymentUiEvent.SubmitPayment -> processPayment(event.amount)
        }
    }

    private fun processPayment(amount: BigDecimal) {
        viewModelScope.launch {
            _uiState.value = PaymentUiState.Processing

            paymentRepository.processPayment(amount).fold(
                ifLeft = { error ->
                    _uiState.value = PaymentUiState.Ready(amount)
                    // One-time error display
                    emit(PaymentEvent.ShowError(error.toUserMessage()))
                },
                ifRight = { receipt ->
                    _uiState.value = PaymentUiState.Ready(amount)
                    // One-time success, then navigation
                    emit(PaymentEvent.ShowSuccess("Payment successful!"))
                    emit(PaymentEvent.NavigateToReceipt(receipt.id))
                }
            )
        }
    }
}

sealed interface PaymentEvent {
    data class ShowError(val message: String) : PaymentEvent
    data class ShowSuccess(val message: String) : PaymentEvent
    data class NavigateToReceipt(val receiptId: String) : PaymentEvent
}
```

## Testing One-Time Events

```kotlin
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @BeforeTest
    fun setup() {
        viewModel = LoginViewModel(
            authRepository = mockAuthRepository,
            viewModelScope = testScope
        )
    }

    @Test
    fun `emits navigate event on successful login`() = testScope.runTest {
        coEvery { mockAuthRepository.login(any(), any()) } returns Either.Right(mockUser)

        // Collect events
        val events = mutableListOf<LoginNavEvent>()
        val job = launch {
            viewModel.events.toList(events)
        }

        viewModel.onUiEvent(LoginUiEvent.Login("email", "password"))

        // Advance to let coroutines complete
        advanceUntilIdle()

        // Verify event emitted
        assertEquals(1, events.size)
        assertTrue(events[0] is LoginNavEvent.NavigateToHome)

        job.cancel()
    }

    @Test
    fun `does not emit navigate event on failed login`() = testScope.runTest {
        coEvery { mockAuthRepository.login(any(), any()) } returns Either.Left(AuthError.InvalidCredentials)

        val events = mutableListOf<LoginNavEvent>()
        val job = launch {
            viewModel.events.toList(events)
        }

        viewModel.onUiEvent(LoginUiEvent.Login("email", "wrong"))
        advanceUntilIdle()

        // No navigation event on error
        assertEquals(0, events.size)

        job.cancel()
    }
}
```

## Alternative: SharedFlow

For some use cases, `SharedFlow` can be used instead of `Channel`:

```kotlin
class AlternativeEventEmitter<E> : OneTimeEventEmitter<E> {
    private val _events = MutableSharedFlow<E>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events: Flow<E> = _events.asSharedFlow()

    override suspend fun emit(event: E) {
        _events.emit(event)
    }
}
```

**Trade-offs**:
- **Channel**: Guaranteed delivery to single collector, suspends if no collector
- **SharedFlow**: Multiple collectors possible, drops old events on overflow

**Recommendation**: Use `Channel` (EventChannel) for most cases — it's the safer default.

## Anti-Patterns

### ❌ DON'T: Put One-Time Events in State

```kotlin
// ❌ WRONG - Event survives config change
data class HomeUiState(
    val items: List<Item>,
    val navigateToDetail: String? = null  // Event in state!
)

// Problem: After rotation, navigation triggers again
```

### ❌ DON'T: Use StateFlow for Events

```kotlin
// ❌ WRONG - Event can be observed multiple times
private val _navigateEvent = MutableStateFlow<String?>(null)
val navigateEvent: StateFlow<String?> = _navigateEvent

fun onItemClicked(id: String) {
    _navigateEvent.value = id  // Remains set after navigation!
}
```

### ✅ DO: Use Channel/Flow for Events

```kotlin
// ✅ CORRECT - Event consumed once
private val _events = Channel<NavEvent>()
val events = _events.receiveAsFlow()

suspend fun onItemClicked(id: String) {
    _events.send(NavEvent.NavigateToDetail(id))  // Consumed once
}
```

## iOS Integration

One-time events work seamlessly with iOS via SKIE:

```swift
struct LoginView: View {
    private var viewModel: LoginViewModel
    @State private var uiState: LoginUiState = LoginUiStateIdle()
    @State private var showError = false
    @State private var errorMessage = ""

    var body: some View {
        content
            .task {
                // Collect UI state
                for await state in viewModel.uiState {
                    self.uiState = state
                }
            }
            .task {
                // Collect one-time events
                for await event in viewModel.events {
                    switch event {
                    case is LoginNavEventNavigateToHome:
                        // Navigate
                    case let errorEvent as LoginNavEventShowSnackbar:
                        errorMessage = errorEvent.message
                        showError = true
                    default:
                        break
                    }
                }
            }
    }
}
```

## Reference Implementations

- `features/pokemonlist/presentation/PokemonListViewModel.kt`
- `features/pokemondetail/presentation/PokemonDetailViewModel.kt`

## Documentation Sources

- [presentation_layer.md](See @kmp-presentation skill) — Complete presentation guide
- [viewmodel_patterns.md](See @kmp-presentation skill) — Extended examples
