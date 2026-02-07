---
name: kmp-architecture
description: "Kotlin Multiplatform architecture patterns for vertical slice organization, module structure, and feature boundaries. Use when: (1) Designing new feature module structure, (2) Deciding between :core vs :features modules, (3) Understanding split-by-layer patterns, (4) Setting up multi-UI theme architecture (Material + Unstyled), (5) Planning module dependencies and iOS export boundaries"
---

# KMP Architecture Skill

Architecture patterns for organizing Kotlin Multiplatform code with true vertical slicing and clear module boundaries.

## When to Use

Use this skill when working on:
- Designing new feature module structure and layer organization
- Deciding between creating a `:core` module vs keeping logic in `:features`
- Planning module dependencies and cross-feature interactions
- Setting up dual-UI theme architecture (Material Design 3 + Compose Unstyled)
- Configuring iOS framework exports via `:shared` framework
- Migrating from horizontal layers (shared network/data) to vertical slices

Do NOT use for:
- ViewModel implementation details → use @kmp-presentation
- Repository implementation patterns → use @kmp-data-layer
- Koin DI configuration details → use @kmp-di
- Product requirements or PRD creation → use @product-designer

**Conditional Loading**:
| Task | Reference | Load When |
|------|-----------|-----------|
| Module layer details | [module-structure.md](references/module-structure.md) | Creating new feature modules |
| Vertical slicing principles | [vertical-slicing.md](references/vertical-slicing.md) | Understanding feature boundaries |
| Core module decisions | [core-modules.md](references/core-modules.md) | Deciding :core vs :features |

## Module Structure Overview

All features use **split-by-layer** architecture with 8 standard modules:

| Module | Purpose | KMP Targets | iOS Export |
|--------|---------|-------------|------------|
| `:api` | Public contracts, interfaces, navigation | All | ✅ Yes |
| `:data` | API services, DTOs, repositories | All | ❌ No |
| `:presentation` | ViewModels, UI state | All | ✅ Yes |
| `:ui-material` | Material Design 3 Compose UI | Android + JVM + iOS Compose | ❌ No |
| `:ui-unstyled` | Compose Unstyled UI | Android + JVM + iOS Compose | ❌ No |
| `:wiring` | Business DI (repos, ViewModels) | All | ❌ No |
| `:wiring-ui-material` | Material navigation registration | Android + JVM + iOS Compose | ❌ No |
| `:wiring-ui-unstyled` | Unstyled navigation registration | Android + JVM + iOS Compose | ❌ No |

**Example**: `features/pokemonlist/` contains all 8 modules above with complete implementation.

## Vertical Slicing Principle

**Core Rule**: Each feature owns ALL its layers end-to-end. Features are self-contained vertical slices.

```
┌─────────────────────────────────────────┐
│  Feature: Pokemon List                  │
├─────────────────────────────────────────┤
│  :api        → Repository interface     │
│  :data       → API service, DTOs, impl  │
│  :presentation → ViewModel, UI state    │
│  :ui-*       → Compose screens          │
│  :wiring*    → DI assembly              │
└─────────────────────────────────────────┘
```

**Benefits**:
- Compilation avoidance: Changes to Pokemon Detail don't recompile Pokemon List
- Team autonomy: Features developed independently
- Clear boundaries: All code for a feature lives in one place
- Testability: Self-contained with explicit dependencies

**NEVER share**: API services, DTOs, repository implementations between features. Each feature defines its own, even if calling the same backend endpoint.

## Core Module Guidelines

**ONLY create `:core` modules for**:
1. **Truly generic utilities** used by 3+ features (date formatters, string utils)
2. **Design system** (reusable UI components, theme, tokens)
3. **Cross-cutting domain models** (User, Error types used everywhere)
4. **Platform abstractions** (expect/actual for platform APIs)

**NEVER create `:core` modules for**:
- ❌ Generic network layer (each feature has its own HttpClient config)
- ❌ Generic repository base classes (each feature implements its own)
- ❌ Generic database layer (each feature manages its own data)
- ❌ Generic API service interfaces (each feature defines its own)

**Rule of thumb**: If it serves 1-2 features, put it in the feature. If it serves 3+ features, consider :core. Duplication is better than premature abstraction.

**MANDATORY**: Before creating a :core module, read [core-modules.md](references/core-modules.md).

## Feature Module Boundaries

### Dependency Rules

```
:features:profile:data  →  :features:auth:api     ✅ OK (public API)
:features:profile:data  →  :features:auth:data    ❌ NEVER (implementation)
```

### iOS Export Boundaries

**NEVER export to iOS via `:shared` framework**:
- `:features:*:data` - Implementation details
- `:features:*:ui-*` - Compose UI (iOS uses SwiftUI)
- `:features:*:wiring*` - DI assembly

**ALWAYS export to iOS**:
- `:features:*:api` - Contracts for iOS to implement against
- `:features:*:presentation` - ViewModels for iOS SwiftUI consumption
- `:core:*` - Shared utilities and domain types

## Multi-UI Theme Architecture

For dual-theme support (Material + Unstyled):

1. **Scope markers in design system**:
   - `MaterialScope` in `:core:designsystem-material`
   - `UnstyledScope` in `:core:designsystem-unstyled`

2. **Separate wiring-ui modules**:
   - `:wiring-ui-material` scoped to `MaterialScope`
   - `:wiring-ui-unstyled` scoped to `UnstyledScope`

3. **Both loaded simultaneously** in app - Koin Navigation 3 manages scope automatically

## Utility Organization Patterns

### Location and Structure
- Feature-scoped utilities: place under each feature module as needed, e.g. `:features:<feature>:impl/src/commonMain/kotlin/.../util/` (data/presentation-specific) 
- Shared utilities: prefer a focused core module (e.g., `:core:util`) with `src/commonMain/.../util/` and platform-specific actuals in `src/androidMain` / `src/iosMain` when necessary
- Organize by functional domain, not by technical type

### Package Organization

#### Functional Domain Structure
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

#### Cross-Cutting Concerns
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

### Utility Design Patterns

#### Singleton or Injectable Class for Stateful Utilities
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

#### Extension Functions for Behavioral Extensions
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

#### Utility Classes for Stateless Operations
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

### Platform Abstraction

#### Expect/Actual Pattern
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

#### Platform-Specific Utilities
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

### Dependency Integration

#### Injectable Utilities
Design utilities with plain constructors so Metro can wire them via provider functions. Avoid hardcoding dispatchers; inject them for testability.

```kotlin
class BackgroundDispatcherProvider(
    val io: CoroutineDispatcher,
    val default: CoroutineDispatcher
)

class DataValidator()
```

#### Self-Contained Utilities
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

### Error Handling in Utilities

#### Consistent Error Patterns
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

#### Graceful Degradation
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

### Performance Considerations

#### Lazy Initialization
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

#### Memory Management
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

### Testing Utilities

#### Testable Design
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

#### Test Utilities
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

### Documentation and Naming

#### Clear Naming Conventions
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

#### Comprehensive Documentation
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

### Integration with Architecture

#### Layer Independence
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

#### Cross-Layer Utilities
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

#### Wiring Utilities via DI (example)
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

## Essential Workflows

### Workflow 1: Create New Feature Module (Vertical Slice)

To add a new feature following the vertical slice architecture:

1. **Create directory structure** in `features/<feature>/`:
   - `api/`, `data/`, `presentation/`, `ui-material/`, `ui-unstyled/`, `wiring/`, `wiring-ui-material/`, `wiring-ui-unstyled/`.
2. **Apply convention plugins** in each module's `build.gradle.kts`:
   - `:api` → `id("convention.feature.api")`
   - `:data` → `id("convention.feature.data")`
   - `:presentation` → `id("convention.feature.presentation")`
   - `:ui-*` → `id("convention.feature.ui")`
   - `:wiring*` → `id("convention.feature.wiring")`
3. **Define public contracts** in `:api`:
   - Create repository interface and Navigation 3 route objects.
4. **Implement data layer** in `:data`:
   - Create internal repository implementation class.
   - Create public factory function (e.g., `fun FeatureRepository(...): FeatureRepository`).
   - Define feature-specific API service and DTOs.
5. **Create presentation layer** in `:presentation`:
   - Implement `ViewModel` with `SavedStateHandle` and `viewModelScope` support.
   - Define `UiState` sealed hierarchy.
6. **Implement UI** in `:ui-material` and `:ui-unstyled`:
   - Build Compose screens and add `@Preview` for all states.
7. **Assemble DI** in `:wiring`:
   - Define Koin module registering the implementation classes.
8. **Register navigation** in `:wiring-ui-*`:
   - Map routes to screens within `MaterialScope` and `UnstyledScope`.

### Workflow 2: Decide :core vs :features

Follow the **3-Feature Rule** and decision matrix:

1. **Identify the concern**: Is it generic infrastructure or business logic?
2. **Apply decision matrix**:
   - **Generic Utilities** (Date, String): Use `:core:util` if 3+ features need it.
   - **Design System**: Always in `:core:designsystem-*`.
   - **Domain models**: Keep in the feature's `:api` unless 3+ features share it (then `:core:domain`).
   - **Platform Abstractions**: Use `:core:platform` for `expect/actual` patterns.
3. **Avoid the "Common" trap**: Don't create a `:core:common` for "everything else". Use specific, descriptive module names.
4. **Prefer Duplication**: If only 2 features share a DTO or small utility, duplicate it to maintain vertical slice independence.

### Workflow 3: Add Cross-Feature Dependency

To use logic from Feature A (e.g., `auth`) in Feature B (e.g., `profile`):

1. **Verify Interface Availability**: Ensure the required repository interface or domain model is public in `features/auth/api`.
2. **Declare Dependency**: Add the `:api` dependency in Feature B's consuming module (usually `:data` or `:presentation`):
   ```kotlin
   // features/profile/data/build.gradle.kts
   dependencies {
       implementation(projects.features.auth.api)
   }
   ```
3. **Inject via Koin**: Request the dependency in Feature B's wiring module:
   ```kotlin
   // features/profile/wiring/ProfileModule.kt
   val profileModule = module {
       factory { ProfileRepository(authRepository = get()) }
   }
   ```
4. **Enforce Boundaries**: Never allow `profile` to depend on `auth:data`. If `auth:api` doesn't have what you need, refactor `auth` to expose it via its public API contract.

## Critical Guardrails

1. **NEVER depend on implementation modules**: Features must only depend on the `:api` of other features. No cross-dependencies on `:data`, `:presentation`, or `:ui`.
2. **NEVER export implementation to iOS**: Only `:api` and `:presentation` modules should be exported via the `:shared` framework to keep the iOS umbrella framework lean.
3. **NEVER create :core for 1-2 features**: Follow the 3-feature rule. Duplication is cheaper than the wrong abstraction.
4. **NEVER share DTOs between features**: Each feature defines its own DTOs in its `:data` module, even if calling the same backend API endpoint.
5. **NEVER create empty use cases**: Call repositories directly from ViewModels. Create `:domain` and use cases only for orchestrating 2+ repositories or complex business rules.
6. **NEVER do work in ViewModel init**: Override `onStart(owner)` to trigger initial data loading. This ensures network calls only happen when the UI is active and lifecycle-aware.
7. **NEVER swallow CancellationException**: Ensure `Either.catch` or manual try-catch blocks allow cancellation to propagate, preventing leaked coroutines.
8. **NEVER use star imports**: Always use explicit imports to prevent naming collisions and improve code readability (enforced by .editorconfig).
9. **NEVER share database instances**: Features should manage their own persistence layer to maintain independence and avoid global schema migrations.

## Cross-References

### Related Skills
| Skill | Purpose | Link |
|-------|---------|------|
| @kmp-presentation | ViewModel lifecycle, SavedStateHandle, UI state | [SKILL.md](../kmp-presentation/SKILL.md) |
| @kmp-data-layer | Repository patterns, DTO mapping, RepoError | [SKILL.md](../kmp-data-layer/SKILL.md) |
| @kmp-di | Koin module configuration, parameter injection | [SKILL.md](../kmp-di/SKILL.md) |
| @kmp-navigation | Navigation 3 routes, scoped navigation providers | [SKILL.md](../kmp-navigation/SKILL.md) |
| @kmp-ios | SwiftUI + KMP integration, Direct Integration pattern | [SKILL.md](../kmp-ios/SKILL.md) |

### Documentation
| Document | Purpose | Link |
|----------|---------|------|
| [conventions.md](See @kmp-architecture skill for architecture patterns) | Master architecture reference | [Read](See @kmp-architecture skill for architecture patterns) |
| [architecture_patterns.md](See @kmp-architecture skill) | Code examples and structural patterns | [Read](See @kmp-architecture skill) |
| [critical_patterns_quick_ref.md](See @kmp-critical-patterns skill) | 6 core patterns for rapid development | [Read](See @kmp-critical-patterns skill) |
| [module-structure.md](references/module-structure.md) | Detailed layer breakdown (8-module pattern) | [Read](references/module-structure.md) |
| [vertical-slicing.md](references/vertical-slicing.md) | Principles and benefits of vertical slicing | [Read](references/vertical-slicing.md) |
| [core-modules.md](references/core-modules.md) | Guidelines for creating :core modules | [Read](references/core-modules.md) |

### Reference Implementation
Study the `features/pokemonlist/` modules for a complete implementation of all 8 layers:
- **API**: `PokemonListRepository.kt` and navigation routes
- **Data**: `PokemonListRepositoryImpl.kt`, `ApiService.kt`, and mappers
- **Presentation**: `PokemonListViewModel.kt` and `UiState.kt`
- **UI**: Material and Unstyled screen implementations
- **Wiring**: Koin module registration and Navigation 3 entry providers

## Quick Reference

### Module Naming

```
:features:<feature>:api              ✅
:features:<feature>:data             ✅
:features:<feature>:presentation     ✅
:features:<feature>:ui-material      ✅
:features:<feature>:ui-unstyled      ✅
:features:<feature>:wiring           ✅
:features:<feature>:wiring-ui-*      ✅

:pokemonlist                         ❌ Missing :features prefix
:features:pokemon-list               ❌ Hyphenated (use lowercase)
:features:pokemonList                ❌ CamelCase (use lowercase)
:features:pokemonlist:impl           ❌ Use :data, :presentation
```

### Package Naming

Convert dashes to dots: `:features:pokemonlist:ui-material` → `features.pokemonlist.ui.material`

### Validation Commands

```bash
# Build and test (always run before committing)
./gradlew :composeApp:assembleDebug test --continue

# Check module dependencies
./gradlew :features:<feature>:api:dependencies --configuration commonMain

# Verify iOS export configuration
./gradlew :shared:dependencies --configuration iosMain
```
