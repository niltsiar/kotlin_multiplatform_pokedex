# Utility Organization Patterns

Project-specific patterns for organizing and wiring utility classes in Kotlin Multiplatform.

## Location Strategy

**Feature-scoped utilities**: Place under each feature module as needed
- Example: `:features:pokemonlist:data/util/` for data-specific helpers
- Example: `:features:pokemonlist:presentation/util/` for presentation-specific helpers

**Shared utilities**: Create focused core modules for 3+ features
- Example: `:core:httpclient` for shared HTTP client configuration
- Example: `:core:designsystem-*` for reusable UI components

**Rule of thumb**: If it serves 1-2 features, keep it in the feature. If it serves 3+ features, create a `:core` module. Duplication is better than premature abstraction.

## Impl + Factory Pattern

**Core principle**: Keep production classes free of DI annotations. Use factory functions and Koin's `module {}` DSL.

```kotlin
// ✅ DO: Interface + Internal Implementation + Public Factory Function
interface TimeFormatter {
    fun formatRelative(instant: Instant): String
}

internal class TimeFormatterImpl(
    private val clock: Clock
) : TimeFormatter {
    override fun formatRelative(instant: Instant): String {
        val now = clock.now()
        val duration = now - instant
        return when {
            duration.inWholeMinutes < 1 -> "just now"
            duration.inWholeHours < 1 -> "${duration.inWholeMinutes}m ago"
            duration.inWholeDays < 1 -> "${duration.inWholeHours}h ago"
            else -> "${duration.inWholeDays}d ago"
        }
    }
}

// Public factory function (DI-agnostic)
fun TimeFormatter(clock: Clock): TimeFormatter = TimeFormatterImpl(clock)
```

**Wire via Koin in wiring module**:
```kotlin
// :features:timeline:wiring/TimelineModule.kt
val timelineModule = module {
    factory<TimeFormatter> { TimeFormatter(clock = get()) }
}
```

## Error Handling with Either.catch

**Utilities should not throw**. Return `Either<Throwable, T>` consistently. Prefer `Either.catch { ... }` in suspend functions to preserve coroutine cancellation.

```kotlin
class FileUtils(private val fileManager: FileManager) {
    suspend fun saveToCache(data: ByteArray, filename: String): Either<Throwable, String> =
        Either.catch { 
            fileManager.writeFile(filename, data)
        }

    suspend fun readFromCache(filename: String): Either<Throwable, ByteArray> =
        Either.catch { 
            fileManager.readFile(filename)
        }
}
```

**Why `Either.catch`?**
- Respects `CancellationException` (doesn't catch it)
- Provides consistent error boundaries across all utility functions
- Prevents leaked coroutines from swallowed cancellations

## Platform Abstraction with Expect/Actual

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
```

```kotlin
// androidMain/util/Platform.kt
actual object Platform {
    actual val name: String = "Android"
    actual val version: String = Build.VERSION.RELEASE
}

actual class FileManager(private val context: Context) {
    actual suspend fun writeFile(filename: String, content: ByteArray): String {
        val file = File(context.cacheDir, filename)
        file.writeBytes(content)
        return file.absolutePath
    }
    // ... other implementations
}
```

**Wire platform-specific instances via Koin**:
```kotlin
// androidMain/wiring/PlatformModule.kt
val platformModule = module {
    single<FileManager> { FileManager(context = get()) }
}
```

## Dependency Injection Patterns

**Injectable utilities**: Design with plain constructors, wire via Koin.

```kotlin
// ✅ DO: Plain constructor, no DI coupling
class BackgroundDispatcherProvider(
    val io: CoroutineDispatcher,
    val default: CoroutineDispatcher
)

// Wire in Koin module
val dispatcherModule = module {
    single { 
        BackgroundDispatcherProvider(
            io = Dispatchers.IO,
            default = Dispatchers.Default
        )
    }
}
```

**Self-contained utilities**: Use `object` for stateless, parameter-free utilities.

```kotlin
object DateTimeUtils {
    fun formatTimestamp(instant: Instant): String {
        // Pure function, no dependencies
        return instant.toString()
    }
    
    fun isToday(instant: Instant, now: Instant = Clock.System.now()): Boolean {
        // Pure function with testable default
        return instant.toLocalDateTime(TimeZone.currentSystemDefault()).date ==
               now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
}
```

**NEVER**: Don't create utilities that depend on specific architectural layers.

```kotlin
// ❌ DON'T: Couples utility to data layer
object RepositoryUtils {
    fun mapToUser(userEntity: UserEntity): User // Too coupled
}
```
