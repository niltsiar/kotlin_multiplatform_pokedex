# Utility Organization Guidelines

Purpose: Establish consistent patterns for organizing utility classes, extensions, and cross-cutting concerns in Compose Multiplatform projects.

## Location and Structure
- Base utilities: `composeApp/src/commonMain/kotlin/com/<org>/<app>/util/`
- Platform-specific utilities: `composeApp/src/androidMain/kotlin/.../util/` and `composeApp/src/iosMain/kotlin/.../util/`
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

### Singleton or Injected Class for Stateful Utilities
Prefer constructor-injected classes for utilities that depend on other services. For global singletons, expose via DI.

```kotlin
// Metro DI will inject a Set<Logger> via multibinding
class AppLogger @Inject constructor(
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
Use regular classes for complex operations that don't need global state:

```kotlin
class DataValidator {
    fun validateEmail(email: String): ValidationResult {
        // Validation logic
    }
    
    fun validatePassword(password: String): ValidationResult {
        // Validation logic
    }
}

class FileUtils {
    suspend fun saveToCache(data: ByteArray, filename: String): String {
        // File operations that may throw; repositories catch and map to Either
        TODO()
    }

    suspend fun readFromCache(filename: String): ByteArray {
        // File operations that may throw; repositories catch and map to Either
        TODO()
    }
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
    suspend fun writeFile(filename: String, content: ByteArray): Result<String>
    suspend fun readFile(filename: String): Result<ByteArray>
    fun deleteFile(filename: String): Boolean
}

// androidMain/util/Platform.kt
actual object Platform {
    actual val name: String = "Android"
    actual val version: String = Build.VERSION.RELEASE
}

actual class FileManager(private val context: Context) {
    actual suspend fun writeFile(filename: String, content: ByteArray): Result<String> {
        // Android implementation
    }
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
Design utilities with constructor injection so Metro can wire them. Avoid hardcoding dispatchers; inject them for testability.

```kotlin
class BackgroundDispatcherProvider @Inject constructor(
    val io: CoroutineDispatcher,
    val default: CoroutineDispatcher
)

class DataValidator @Inject constructor()
```

### Self-Contained Utilities
Some utilities should be self-contained and not require DI:

```kotlin
object DateTimeUtils {
    fun formatRelativeTime(instant: Instant): String {
        // Pure function, no dependencies
    }
    
    fun isToday(instant: Instant): Boolean {
        // Pure function, no dependencies
    }
}

object CryptoUtils {
    fun generateHash(input: String): String {
        // Pure function, no dependencies
    }
}
```

## Error Handling in Utilities

### Consistent Error Patterns
Use consistent error handling across utilities:

```kotlin
class NetworkUtils @Inject constructor(private val logger: Logger) {
    suspend fun checkConnection(): Boolean {
        return try {
            // Network check logic
            true
        } catch (e: Exception) {
            logger.e("Network check failed", e)
            false
        }
    }
}

class FileUtils @Inject constructor(private val logger: Logger) {
    suspend fun saveFile(filename: String, data: ByteArray): String {
        return try {
            // File save logic
            "savedPath"
        } catch (e: IOException) {
            logger.e("File save failed", e)
            throw FileOperationException("Failed to save file", e)
        }
    }
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
class AppLogger @Inject constructor(
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
