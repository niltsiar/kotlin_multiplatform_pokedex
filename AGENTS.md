# AI Agent Instructions for Kotlin Multiplatform POC

> **Purpose**: This document provides autonomous AI agents with the essential context, patterns, and workflows needed to work effectively on this Kotlin Multiplatform project.

---

## 🎯 Project Context

### What You're Working On
A **Kotlin Multiplatform** project with:
- **Compose Multiplatform** for Android + Desktop (JVM) UI
- **Native SwiftUI** for iOS UI (consuming shared Kotlin business logic)
- **Ktor server** as Backend-for-Frontend (BFF)

Currently in **early POC stage** with skeleton modules only.

### Current Reality vs. Documentation
- **What exists**: 
  - `:composeApp` — Compose Multiplatform UI (Android + Desktop)
  - `:shared` — iOS umbrella framework (exports other KMP modules to iOS)
  - `:server` — Ktor backend (BFF for all clients)
  - `:iosApp` — Native SwiftUI app (imports shared.framework to access KMP modules)
- **What's documented**: Comprehensive architecture in `.junie/guides/`
- **Gap**: Feature modules, DI wiring, navigation—all planned but NOT implemented
- **Your job**: Implement patterns following documented conventions, or work within the existing skeleton

**Platform UI Strategy**:
- Android/Desktop: Shared Compose Multiplatform UI
- iOS: Native SwiftUI (separate implementation, accesses KMP business logic via shared.framework)

### Critical Files
```
.junie/guides/tech/conventions.md       ← Master reference (start here)
.junie/guides/tech/dependency_injection.md
.junie/guides/tech/repository.md
.junie/guides/tech/presentation_layer.md
.junie/guides/tech/testing_strategy.md
.junie/guides/project/prd.md           ← Product requirements
gradle/libs.versions.toml              ← All dependency versions
settings.gradle.kts                     ← Module structure
```

---

## 🚀 Quick Start Workflow

### 1. Understand the Task
```bash
# First actions for any task:
1. Read task requirements completely
2. Search .junie/guides/ for relevant patterns
3. Check current module structure: ./gradlew projects
4. Identify if you need new modules or modify existing
```

### 2. Validate Before Starting
```bash
# ALWAYS run Android build first (fastest feedback):
./gradlew :composeApp:assembleDebug

# ⚠️ NEVER run iOS builds unless explicitly required
# iOS builds take 5-10 minutes and are rarely necessary
```

### 3. Implementation Pattern
```
Research → Plan → Implement → Test → Validate
    ↓         ↓         ↓        ↓        ↓
 .junie/   Write    Add code  Kotest   Android
  guides    TODO              tests     build
```

### 4. Completion Checklist
- [ ] Code follows conventions in `.junie/guides/tech/conventions.md`
- [ ] Unit tests written (Kotest in `commonTest/`)
- [ ] Android build passes: `./gradlew :composeApp:assembleDebug`
- [ ] No iOS builds run (unless specifically required)
- [ ] Dependencies added to `gradle/libs.versions.toml`

---

## 📋 Architecture Patterns (CRITICAL)

### Dependency Injection: Metro (Not Implemented Yet)
**Classes are DI-agnostic. Wire via `@Provides` in wiring modules.**

```kotlin
// :features:jobs:api - Public contract
interface JobRepository {
  suspend fun getJobs(): Either<RepoError, List<Job>>
}

// :features:jobs:impl - Internal implementation
internal class JobRepositoryImpl(
  private val api: JobApiService
) : JobRepository {
  override suspend fun getJobs(): Either<RepoError, List<Job>> =
    Either.catch {
      api.getJobs().jobs.map { it.asDomain() }
    }.mapLeft { it.toRepoError() }
}

// Public factory function (Impl + Factory pattern)
fun JobRepository(api: JobApiService): JobRepository = 
  JobRepositoryImpl(api)

// :features:jobs:wiring - DI assembly
@Provides
fun provideJobRepository(api: JobApiService): JobRepository = 
  JobRepository(api)
```

**Why**: Gradle compilation avoidance, hides implementations, simplifies testing

### Error Handling: Arrow Either (Required)
**Repositories MUST return `Either<RepoError, T>`. NEVER throw, return null, or use `Result`.**

```kotlin
// Define sealed errors per feature
sealed interface RepoError {
  data object Network : RepoError
  data class Http(val code: Int, val message: String?) : RepoError
  data object Unauthorized : RepoError
  data class Unknown(val cause: Throwable) : RepoError
}

// Map exceptions at repository boundary
fun Throwable.toRepoError(): RepoError = when (this) {
  is ClientRequestException -> RepoError.Http(response.status.value, message)
  is IOException -> RepoError.Network
  else -> RepoError.Unknown(this)
}

// Repository implementation
override suspend fun getJobs(): Either<RepoError, List<Job>> =
  Either.catch {
    val response = api.getJobs()
    response.jobs.map { it.asDomain() }
  }.mapLeft { it.toRepoError() }
```

### ViewModels: androidx.lifecycle (Required Rules)
**ALL ViewModels MUST follow this pattern exactly:**

```kotlin
class HomeViewModel(
  private val repository: JobRepository,
  viewModelScope: CoroutineScope = CoroutineScope(
    SupervisorJob() + Dispatchers.Main.immediate
  )
) : ViewModel(viewModelScope),  // ← Pass to superclass constructor
    UiStateHolder<HomeUiState, HomeUiEvent> {
  
  private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
  override val uiState: StateFlow<HomeUiState> = _uiState
  
  // ⚠️ NEVER perform work in init {}
  
  // Load data in lifecycle-aware callbacks
  fun start(lifecycle: Lifecycle) {
    viewModelScope.launch {
      lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
        repository.getJobs().fold(
          ifLeft = { error -> 
            _uiState.value = HomeUiState.Error(error.toUiMessage()) 
          },
          ifRight = { jobs -> 
            _uiState.value = HomeUiState.Content(jobs.toImmutableList())
          }
        )
      }
    }
  }
  
  override fun onUiEvent(event: HomeUiEvent) {
    when (event) {
      is HomeUiEvent.Refresh -> refresh()
      is HomeUiEvent.ItemClicked -> handleClick(event.id)
    }
  }
}
```

**Critical Requirements**:
- ✅ Extend `androidx.lifecycle.ViewModel`
- ✅ Pass `viewModelScope` as constructor parameter
- ✅ Use `kotlinx.collections.immutable` types in UI state
- ✅ Load data in lifecycle callbacks, NOT `init`
- ❌ NEVER store `CoroutineScope` as a field
- ❌ NEVER perform work in constructor or `init`

### Navigation: Navigation 3 (Planned)
**Contracts in `:api`, implementations in `:impl`, wiring in `:wiring`**

```kotlin
// :features:profile:api
interface ProfileEntry {
  val route: String
  fun build(userId: String): String
}

// :features:profile:impl
internal class ProfileEntryImpl : ProfileEntry {
  override val route = "profile/{userId}"
  override fun build(userId: String) = "profile/$userId"
}

// :features:profile:wiring
@Provides
fun provideProfileEntry(): ProfileEntry = ProfileEntryImpl()
```

### No Empty Use Cases
**Call repositories directly from ViewModels unless orchestrating multiple repos.**

```kotlin
// ❌ DON'T: Empty pass-through use case
class GetUserUseCase(private val repo: UserRepository) {
  suspend operator fun invoke(id: String) = repo.getUser(id)
}

// ✅ DO: Call repository directly
class ProfileViewModel(
  private val repo: UserRepository,
  viewModelScope: CoroutineScope = ...
) : ViewModel(viewModelScope) {
  fun load(userId: String) = viewModelScope.launch {
    repo.getUser(userId).fold(
      ifLeft = { /* handle error */ },
      ifRight = { /* update UI */ }
    )
  }
}

// ✅ DO: Use case with actual business logic
class SubmitOrderUseCase(
  private val cartRepo: CartRepository,
  private val paymentRepo: PaymentRepository,
  private val inventoryRepo: InventoryRepository
) {
  suspend operator fun invoke(): Either<RepoError, Receipt> = either {
    val cart = cartRepo.current().bind()
    ensure(cart.items.isNotEmpty()) { /* error */ }
    inventoryRepo.reserve(cart.items).bind()
    paymentRepo.charge(cart.total).bind()
  }
}
```

---

## 🧪 Testing Strategy

### Kotest + MockK Pattern
```kotlin
// commonTest/kotlin/.../JobRepositorySpec.kt
class JobRepositorySpec : StringSpec({
  val api = mockk<JobApiService>()
  val repo = JobRepositoryImpl(api)
  
  "getJobs returns Right on success" {
    coEvery { api.getJobs() } returns JobsResponse(
      jobs = listOf(JobDto(id = "1", title = "Engineer"))
    )
    
    val result = repo.getJobs()
    
    result.shouldBeRight()
    result.getOrNull()!!.size shouldBe 1
  }
  
  "getJobs returns Network error on IOException" {
    coEvery { api.getJobs() } throws IOException("Connection failed")
    
    val result = repo.getJobs()
    
    result.shouldBeLeft()
    result.swap().getOrNull() shouldBe RepoError.Network
  }
})

// Helper extensions
fun <L, R> Either<L, R>.shouldBeRight(): R = 
  this.getOrNull() ?: fail("Expected Right but was $this")

fun <L, R> Either<L, R>.shouldBeLeft(): L = 
  this.swap().getOrNull() ?: fail("Expected Left but was $this")
```

### Property-Based Testing
```kotlin
"dto to domain preserves id and title" {
  checkAll(Arb.uuid(), Arb.string(1..64)) { id, title ->
    val dto = JobDto(id = id.toString(), title = title)
    val domain = dto.asDomain()
    
    domain.id shouldBe dto.id
    domain.title shouldBe dto.title
  }
}
```

### Screenshot Tests (Roborazzi)
```kotlin
@RunWith(AndroidJUnit4::class)
class HomeScreenScreenshotTest {
  @get:Rule val compose = createComposeRule()
  
  @Test fun recordHomeScreen() {
    compose.setContent {
      HomeScreen(
        uiState = HomeUiState.Content(sampleItems()),
        onUiEvent = {},
        onNavigate = {}
      )
    }
    captureRoboImage("home/HomeScreen_content.png")
  }
}
```

**Test Commands**:
```bash
./gradlew :composeApp:testDebugUnitTest    # Run unit tests
./gradlew recordRoborazziDebug             # Record baselines
./gradlew verifyRoborazziDebug             # Verify screenshots
```

---

## 🔧 Common Tasks & Solutions

### Adding a New Dependency
```bash
# 1. Add to gradle/libs.versions.toml
[versions]
arrow = "1.2.0"

[libraries]
arrow-core = { module = "io.arrow-kt:arrow-core", version.ref = "arrow" }

# 2. Add to module's build.gradle.kts
commonMain.dependencies {
  implementation(libs.arrow.core)
}

# 3. Sync and validate
./gradlew :composeApp:assembleDebug
```

### Creating expect/actual Implementations
```kotlin
// commonMain/Platform.kt
expect class Platform() {
  val name: String
}

// androidMain/Platform.android.kt
actual class Platform {
  actual val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

// Example in any KMP module (e.g., :core:util or a feature module)
// iosMain/Platform.ios.kt
actual class Platform {
  actual val name: String = UIDevice.currentDevice.systemName()
}

// This module is exported via :shared umbrella → shared.framework
// Consumed by SwiftUI app in iosApp/

// jvmMain/Platform.jvm.kt
actual class Platform {
  actual val name: String = "Java ${System.getProperty("java.version")}"
}
```

### Working with Shared Constants
```kotlin
// Example: Constants in a KMP module (e.g., :core:config)
// core/config/src/commonMain/kotlin/Constants.kt
const val SERVER_PORT = 8080
const val API_TIMEOUT_MS = 30_000L

// Usage in server (Ktor BFF)
fun main() {
  embeddedServer(Netty, port = SERVER_PORT) { ... }.start()
}

// Usage in Android/Desktop (Compose app)
val client = HttpClient {
  install(HttpTimeout) {
    requestTimeoutMillis = API_TIMEOUT_MS
  }
}

// Usage in iOS (SwiftUI app)
// :core:config must be exported via :shared umbrella
import Shared
let port = ConstantsKt.SERVER_PORT  // Accessed via shared.framework
```

---

## 🚨 Critical Don'ts

### ❌ NEVER Do These
1. **Run iOS builds** unless explicitly required (5-10min builds)
2. **Store `CoroutineScope` as field** in ViewModels (pass to constructor)
3. **Perform work in `init`** blocks in ViewModels (use lifecycle callbacks)
4. **Return `Result` or nullable** from repositories (use `Either<RepoError, T>`)
5. **Swallow `CancellationException`** (use `Either.catch` which handles it)
6. **Create empty pass-through** use cases (call repos directly)
7. **Export `:impl` or `:wiring`** to iOS via `:shared` (only export `:api` and `:core:*` modules)
8. **Export `:composeApp`** to iOS via `:shared` (Compose UI is Android/Desktop only)
9. **Add DI annotations** to production classes (wire in wiring modules)
10. **Put business logic in `:shared`** itself (it's an umbrella; logic goes in feature/core modules)

### ⚠️ Common Pitfalls
```kotlin
// ❌ BAD: Storing scope as field
class MyViewModel : ViewModel() {
  private val scope = CoroutineScope(SupervisorJob())  // WRONG
}

// ✅ GOOD: Pass to superclass constructor
class MyViewModel(
  viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob())
) : ViewModel(viewModelScope)  // RIGHT

// ❌ BAD: Work in init
class MyViewModel(...) : ViewModel(...) {
  init {
    loadData()  // WRONG - runs immediately
  }
}

// ✅ GOOD: Lifecycle-aware loading
class MyViewModel(...) : ViewModel(...) {
  fun start(lifecycle: Lifecycle) {
    viewModelScope.launch {
      lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
        loadData()  // RIGHT - lifecycle aware
      }
    }
  }
}
```

---

## 📦 Module Structure Guide

### Current Modules (Existing)
```
:composeApp  → Compose Multiplatform UI (Android/Desktop ONLY)
  ├── commonMain/kotlin    ← Shared Compose UI code
  ├── androidMain/kotlin   ← Android-specific UI
  ├── jvmMain/kotlin       ← Desktop-specific UI
  └── commonTest/kotlin    ← Shared UI tests

:shared      → iOS umbrella framework (exports other modules)
  └── build.gradle.kts     ← Configures which modules to export to iOS
              Purpose: Aggregates :features:*:api, :core:* modules
              Note: Contains minimal/no business logic itself

:iosApp      → Native SwiftUI iOS app
  ├── SwiftUI views        ← iOS-specific UI implementation
  └── import Shared        → Accesses KMP modules via shared.framework

:server      → Ktor Backend-for-Frontend (BFF)
  └── src/main/kotlin      ← REST API for all clients
```

### Planned Feature Modules (Not Yet Created)
```
:features:jobs:api     → Public contracts (exported to iOS via :shared)
:features:jobs:impl    → Internal implementations (NOT exported to iOS)
:features:jobs:wiring  → DI assembly (NOT exported to iOS)

:core:domain           → Shared domain models (exported to iOS via :shared)
:core:util             → Shared utilities (exported to iOS via :shared)

# When creating: consult .junie/guides/tech/conventions.md first
# iOS export rule: Only :api and :core:* modules in :shared umbrella
```

---

## 🎯 Decision Matrix

### When to Create a New Module?
```
IF defining cross-feature contracts → :features:<name>:api (export to iOS via :shared)
IF implementing feature logic      → :features:<name>:impl (do NOT export to iOS)
IF wiring dependencies             → :features:<name>:wiring (do NOT export to iOS)
IF shared utilities                → :core:util (export to iOS via :shared)
IF common domain models            → :core:domain (export to iOS via :shared)
IF iOS umbrella configuration      → :shared (exports other modules, minimal code)
ELSE modify existing modules
```

### When to Create a Use Case?
```
IF orchestrating 2+ repositories   → Create use case
IF applying business rules         → Create use case
IF single repository call only     → Call directly from ViewModel
```

### When to Use expect/actual?
```
IF platform-specific API access    → Use expect/actual in KMP modules (feature/core modules)
IF platform-specific UI:
  - Android/Desktop UI            → Use Compose source sets (androidMain, jvmMain) in :composeApp
  - iOS UI                        → Use SwiftUI in :iosApp (separate from Compose)
IF shared business logic           → Use commonMain in feature/core modules
IF simple constants                → Use commonMain in appropriate module (e.g., :core:config)
IF iOS framework configuration     → Configure in :shared build.gradle.kts (export declarations)
```

---

## 🔍 Debugging & Troubleshooting

### Build Failures
```bash
# 1. Check Gradle sync
./gradlew --refresh-dependencies

# 2. Clean build
./gradlew clean
./gradlew :composeApp:assembleDebug

# 3. Check version catalog
cat gradle/libs.versions.toml

# 4. Verify module structure
./gradlew projects
```

### Test Failures
```bash
# Run with stacktrace
./gradlew :composeApp:testDebugUnitTest --stacktrace

# Run specific test
./gradlew :composeApp:testDebugUnitTest --tests "*.JobRepositorySpec"
```

### Dependency Issues
```bash
# Show dependency tree
./gradlew :composeApp:dependencies

# Check for conflicts
./gradlew :composeApp:dependencyInsight --dependency arrow-core
```

---

## 📚 Reference Quick Links

### Essential Documentation
- **Master conventions**: `.junie/guides/tech/conventions.md`
- **DI patterns**: `.junie/guides/tech/dependency_injection.md`
- **Repository layer**: `.junie/guides/tech/repository.md`
- **ViewModels**: `.junie/guides/tech/presentation_layer.md`
- **Testing**: `.junie/guides/tech/testing_strategy.md`
- **Navigation**: `.junie/guides/tech/navigation.md`

### Product Documentation
- **Requirements**: `.junie/guides/project/prd.md`
- **User flows**: `.junie/guides/project/user_flow.md`
- **UX specs**: `.junie/guides/project/ui_ux.md`

### Build Configuration
- **Versions**: `gradle/libs.versions.toml`
- **Modules**: `settings.gradle.kts`
- **Properties**: `gradle.properties`

---

## 🎓 Learning Path for New Agents

### Day 1: Foundation
1. Read this file completely
2. Read `.junie/guides/tech/conventions.md`
3. Run `./gradlew projects` to see structure
4. Run `./gradlew :composeApp:assembleDebug` successfully

### Day 2: Patterns
1. Study Arrow Either in `.junie/guides/tech/repository.md`
2. Study ViewModel pattern in `.junie/guides/tech/presentation_layer.md`
3. Review existing code in `:composeApp` and `:server`

### Day 3: Practice
1. Write a simple repository with Either
2. Write a ViewModel following the pattern
3. Write Kotest tests for both
4. Run `./gradlew :composeApp:testDebugUnitTest`

### Ongoing
- Reference `.junie/guides/` for specific patterns
- Check `.junie/guides/project/prd.md` for requirements
- Validate all changes with Android build
- Never run iOS builds unless required

---

## ✅ Success Criteria

**You're effective when you can**:
- [ ] Implement repositories returning `Either<RepoError, T>`
- [ ] Create ViewModels following lifecycle-aware pattern
- [ ] Write Kotest tests with proper assertions
- [ ] Add dependencies via version catalog
- [ ] Validate changes with `./gradlew :composeApp:assembleDebug`
- [ ] Find answers in `.junie/guides/` documentation
- [ ] Avoid common pitfalls (scope storage, init work, iOS builds)

**You're ready for complex tasks when you can**:
- [ ] Create new feature modules (api/impl/wiring)
- [ ] Implement Metro DI patterns
- [ ] Write property-based tests
- [ ] Create Roborazzi screenshot tests
- [ ] Implement Navigation 3 contracts
- [ ] Design sealed error hierarchies

---

## 💡 Pro Tips

1. **Always search `.junie/guides/` first** before asking questions
2. **Android build is your friend** - run it often (45s feedback)
3. **iOS builds are your enemy** - avoid unless absolutely necessary (5-10min)
4. **Version catalog is king** - all dependencies go there first
5. **Either is your boundary** - repositories always return it
6. **ViewModels are lifecycle-aware** - never init work
7. **Kotest is your safety net** - write tests as you code
8. **Patterns are documented** - follow them exactly

---

**Remember**: This is a POC. Patterns are documented but not fully implemented. Your job is to implement them correctly following the guides, or work within the existing structure. When in doubt, consult `.junie/guides/tech/conventions.md` first.
