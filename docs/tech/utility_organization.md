# Utility Organization Guidelines

Last Updated: November 26, 2025

Purpose: Establish consistent patterns for organizing utility classes, extensions, and cross-cutting concerns in Compose Multiplatform projects.

## Location and Structure
- Feature-scoped utilities: place under each feature module as needed, e.g. `:features:<feature>:impl/src/commonMain/kotlin/.../util/` (data/presentation-specific) 
- Shared utilities: prefer a focused core module (e.g., `:core:util`) with `src/commonMain/.../util/` and platform-specific actuals in `src/androidMain` / `src/iosMain` when necessary
- Organize by functional domain, not by technical type

## Package Organization

### Functional Domain Structure
Organize utilities by what they do, not what they are:

```text
util/
├── analytics/          # Analytics and tracking utilities
├── extensions/         # Extension functions
├── file/               # File operations and management
├── inappreview/        # In-app review functionality
├── logging/            # Logging infrastructure
└── Platform.kt         # Platform-specific abstractions
```

### Cross-Cutting Concerns
Group related functionality together:

```text
// util/logging/
├── Logger.kt           # Logger interface and AppLogger
├── LogLevel.kt         # Log level definitions
└── LogFormatter.kt     # Log formatting utilities

// util/analytics/
├── AnalyticsEvent.kt   # Event definitions
├── AnalyticsTracker.kt # Tracking interface
└── EventLogger.kt      # Analytics implementation

// util/extensions/
├── StringExtensions.kt
├── FlowExtensions.kt
└── ComposeExtensions.kt
```

## Utility Design Patterns

### Singleton or Injectable Class for Stateful Utilities
Prefer plain constructors (no DI annotations) for utilities that depend on other services. Wire them via DI provider functions in wiring modules. For global singletons, expose via DI. Utilities should not throw — return `Either<Throwable, T>` consistently.

```kotlin
// DI-agnostic class. Provide instances via DI wiring modules.
class AppLogger(
    private val loggers: Set<Logger>
) : Logger {
    override fun d(message: String, throwable: Throwable?, tag: String?) {
        loggers.forEach { it.d(message, throwable, tag) }
    }
}

object AnalyticsManager { // self-contained process-wide holder
    private val trackers = mutableListOf<AnalyticsTracker>()

    fun initialize(vararg trackers: AnalyticsTracker) {
        this.trackers.clear()
        this.trackers.addAll(trackers)
    }
}
```

### Extension Functions for Behavioral Extensions
Group extension functions by the type they extend:

```kotlin
// StringExtensions.kt
fun String.isValidEmail(): Boolean = 
    android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.truncate(maxLength: Int): String =
    if (length <= maxLength) this else take(maxLength - 3) + "..."

// FlowExtensions.kt
fun <T> Flow<T>.throttleLatest(periodMillis: Long): Flow<T> =
    conflate().onEach { delay(periodMillis) }

// ComposeExtensions.kt
fun Modifier.fillWidthOfParent(percentage: Float = 1f): Modifier =
    this.fillMaxWidth(percentage)
```

### Utility Classes for Stateless Operations
Use regular classes for complex operations that don't need global state. Return `Either<Throwable, T>` from suspend functions instead of throwing. Prefer `Either.catch { ... }` to wrap potentially-throwing code because it respects `CancellationException` and other non-recoverable cases.

```kotlin
class DataValidator {
    fun validateEmail(email: String): ValidationResult {
        // Validation logic
    }
    
    fun validatePassword(password: String): ValidationResult {
        // Validation logic
    }
}

class FileUtils(private val fm: FileManager) {
    suspend fun saveToCache(data: ByteArray, filename: String): Either<Throwable, String> =
        Either.catch { fm.writeFile(filename, data) }

    suspend fun readFromCache(filename: String): Either<Throwable, ByteArray> =
        Either.catch { fm.readFile(filename) }
}
```

## Platform Abstraction

### Expect/Actual Pattern
Use expect/actual for platform-specific utilities:

```kotlin
// commonMain/util/Platform.kt
expect object Platform {
    val name: String
    val version: String
}

expect class FileManager {
    suspend fun writeFile(filename: String, content: ByteArray): String
    suspend fun readFile(filename: String): ByteArray
    fun deleteFile(filename: String): Boolean
}

// androidMain/util/Platform.kt
actual object Platform {
    actual val name: String = "Android"
    actual val version: String = Build.VERSION.RELEASE
}

actual class FileManager(private val context: Context) {
    actual suspend fun writeFile(filename: String, content: ByteArray): String {
        // Android implementation
    }
    // ... other actuals
}
```

### Platform-Specific Utilities
Place platform-specific utilities in appropriate source sets:

```kotlin
// androidMain/util/AndroidUtils.kt
object AndroidUtils {
    fun getVersionCode(context: Context): Long {
        // Android-specific implementation
    }
    
    fun openAppSettings(context: Context) {
        // Android-specific implementation
    }
}

// iosMain/util/IOSUtils.kt
object IOSUtils {
    fun getVersionString(): String {
        // iOS-specific implementation
    }
    
    fun openAppSettings() {
        // iOS-specific implementation
    }
}
```

## Dependency Integration

### Injectable Utilities
Design utilities with plain constructors so Metro can wire them via provider functions. Avoid hardcoding dispatchers; inject them for testability.

```kotlin
class BackgroundDispatcherProvider(
    val io: CoroutineDispatcher,
    val default: CoroutineDispatcher
)

class DataValidator()
```

### Self-Contained Utilities
Some utilities should be self-contained and not require DI:

```kotlin
object DateTimeUtils {
    fun formatRelativeTime(instant: Instant): String { /* use kotlinx-datetime */ }
    fun isToday(instant: Instant): Boolean { /* pure */ }
}

object CryptoUtils {
    fun generateHash(input: String): String {
        // Pure function, no dependencies
    }
}
```

## Error Handling in Utilities

### Consistent Error Patterns
Utilities should not throw; return `Either<Throwable, T>` consistently. Prefer `Either.catch { ... }` in suspend functions so coroutine cancellation is preserved.

```kotlin
class NetworkUtils(private val logger: Logger) {
    suspend fun checkConnection(): Either<Throwable, Boolean> = Either.catch {
        // Network check logic
        true
    }.also { if (it.isLeft()) logger.e("Network check failed", it.swap().getOrNull()) }
}

class SafeFileSaver(private val logger: Logger) {
    suspend fun saveFile(filename: String, data: ByteArray): Either<Throwable, String> = Either.catch {
        // File save logic
        "savedPath"
    }.onLeft { e -> logger.e("File save failed", e) }
}
```

### Graceful Degradation
Design utilities to handle failures gracefully:

```kotlin
object AnalyticsManager {
    fun trackEvent(event: AnalyticsEvent) {
        try {
            trackers.forEach { it.track(event) }
        } catch (e: Exception) {
            AppLogger.e("Analytics tracking failed", e)
            // Continue execution, don't crash the app
        }
    }
}
```

## Performance Considerations

### Lazy Initialization
Use lazy initialization for expensive utilities:

```kotlin
object CacheManager {
    private val cache by lazy {
        LruCache<String, Any>(maxSize = 100)
    }
    
    fun get(key: String): Any? = cache.get(key)
    fun put(key: String, value: Any) = cache.put(key, value)
}

class ImageLoader {
    private val httpClient by lazy {
        HttpClient {
            // Expensive initialization
        }
    }
}
```

### Memory Management
Consider memory impact of utility classes:

```kotlin
// Prefer stateless utilities
object StringUtils {
    fun formatCurrency(amount: Double): String {
        // No state, memory efficient
    }
}

// Be careful with caching utilities
class ResourceCache {
    private val cache = ConcurrentHashMap<String, ByteArray>()
    
    fun clearCache() {
        cache.clear() // Provide cleanup methods
    }
}
```

## Testing Utilities

### Testable Design
Design utilities to be easily testable:

```kotlin
class DataProcessor(
    private val validator: DataValidator = DataValidator(),
    private val logger: Logger = AppLogger
) {
    fun processData(input: String): ProcessResult {
        logger.d("Processing data: ${input.take(10)}...")
        if (!validator.isValid(input)) {
            return ProcessResult.Invalid
        }
        // Processing logic
    }
}

// Easy to test with mocks
@Test
fun testDataProcessor() {
    val mockValidator = mockk<DataValidator>()
    val mockLogger = mockk<Logger>()
    val processor = DataProcessor(mockValidator, mockLogger)
    // Test implementation
}
```

### Test Utilities
Create utilities specifically for testing:

```kotlin
// commonTest/util/TestUtils.kt
object TestUtils {
    fun createTestUser(id: String = "test"): User = User(
        id = id,
        email = "test@example.com",
        displayName = "Test User"
    )
    
    fun createTestAiInfluencer(id: Long = 1L): AiInfluencerModel = 
        AiInfluencerModel(
            id = id,
            prompt = "Test prompt",
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
}
```

## Documentation and Naming

### Clear Naming Conventions
Use descriptive names that indicate purpose:

```kotlin
// Good
object NetworkConnectivityChecker
class EmailValidator
fun String.toSlug(): String

// Avoid generic names
object Utils
class Helper
fun String.convert(): String
```

### Comprehensive Documentation
Document utility classes thoroughly:

```kotlin
/**
 * Manages application-wide logging with support for multiple log destinations.
 *
 * Receives all Logger implementations via Metro multibinding (Set<Logger>) and
 * delegates log calls to all registered loggers.
 *
 * Usage:
 * val appLogger: Logger = di.appGraph.logger // or injected where needed
 * appLogger.d("Debug message")
 */
class AppLogger(
    private val loggers: Set<Logger>
) : Logger {
    override fun d(message: String, throwable: Throwable?, tag: String?) {
        loggers.forEach { it.d(message, throwable, tag) }
    }
    override fun e(message: String, throwable: Throwable?, tag: String?) {
        loggers.forEach { it.e(message, throwable, tag) }
    }
}
```

## Integration with Architecture

### Layer Independence
Keep utilities independent of specific architectural layers:

```kotlin
// Good - can be used by any layer
object DateTimeFormatter {
    fun formatTimestamp(instant: Instant): String
}

// Avoid - couples utility to specific layer
object RepositoryUtils {
    fun mapToUser(userEntity: UserEntity): User // Too coupled
}
```

### Cross-Layer Utilities
Design utilities that serve multiple layers appropriately:

```kotlin
// Serves both data and presentation layers
object ErrorMessageMapper {
    fun mapToUserMessage(exception: Throwable): String {
        return when (exception) {
            is NetworkException -> "Check your internet connection"
            is AuthException -> "Please sign in again"
            else -> "Something went wrong"
        }
    }
}
```

### Wiring Utilities via DI (example)
Provide utilities via Metro in wiring modules while keeping classes DI-agnostic. Use Impl + Factory pattern for interfaces as well.

```kotlin
// :features:logging:wiring/src/commonMain/.../LoggingWiring.kt
@Provides fun provideLoggers(console: ConsoleLogger, crash: CrashLogger): Set<Logger> = setOf(console, crash)
@Provides fun provideAppLogger(loggers: Set<Logger>): Logger = AppLogger(loggers)

// Dispatcher provider
@Provides fun provideDispatchers(io: CoroutineDispatcher, default: CoroutineDispatcher): BackgroundDispatcherProvider =
  BackgroundDispatcherProvider(io, default)

// Impl + Factory pattern for a utility interface
interface TimeFormatter { fun relative(instant: Instant): String }
internal class TimeFormatterImpl(private val clock: Clock) : TimeFormatter {
  override fun relative(instant: Instant): String = /* ... */ ""
}
fun TimeFormatter(clock: Clock): TimeFormatter = TimeFormatterImpl(clock)

@Provides fun provideTimeFormatter(clock: Clock): TimeFormatter = TimeFormatter(clock)

// EventChannel helper is shared in :core:util for ViewModel one-time events
// to enable delegation of OneTimeEventEmitter<E>.
// :core:util/src/commonMain/.../EventChannel.kt
interface OneTimeEventEmitter<E> {
  val events: Flow<E>
  suspend fun emit(event: E)
}

class EventChannel<E> : OneTimeEventEmitter<E> {
  private val channel = Channel<E>(Channel.BUFFERED)
  override val events: Flow<E> = channel.receiveAsFlow()
  override suspend fun emit(event: E) = channel.send(event)
}
```
