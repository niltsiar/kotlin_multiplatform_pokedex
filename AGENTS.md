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
  - `:features:pokemonlist` — FULLY IMPLEMENTED with split-by-layer pattern (:api, :data, :presentation, :ui, :wiring)
- **What's documented**: Comprehensive architecture in `.junie/guides/`
- **Reference implementation**: Use `pokemonlist` feature as reference for new features
- **Your job**: Implement new features following pokemonlist pattern, or extend existing modules

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
2. **Identify task type**: Product design? UI/UX? Implementation? Testing?
3. **Choose agent mode**: Consult specialized prompts if needed
4. Search .junie/guides/ for relevant patterns
5. Check current module structure: ./gradlew projects
6. Identify if you need new modules or modify existing
```

**Task Type Decision Tree:**
```
Is this about product features/requirements?
  → YES: Use Product Design Mode (.junie/guides/prompts/product_designer_agent_system_prompt.md)
  
Is this about visual design/animations/screens?
  → YES: Use UI/UX Design Mode (.junie/guides/prompts/uiux_agent_system_prompt.md)
  → Reference: animation_example_guides.md, easter_eggs_and_mini_games_guide.md
  
Is this about onboarding copy/flow?
  → YES: Use Onboarding Design Mode (.junie/guides/prompts/onboarding_agent_system_prompt.md)
  
Is this about user journeys/navigation?
  → YES: Use User Flow Planning Mode (.junie/guides/prompts/user_flow_agent_system_prompt.md)
  
Is this about implementing UI from specs?
  → YES: Use Screen Implementation Mode (.junie/guides/prompts/ui_ux_system_agent_for_generic_screen.md)
  
Is this about repositories/ViewModels/tests/technical?
  → YES: Use Standard Development Mode (this document)
```

### 2. Validate Before Starting
```bash
# ALWAYS run Android build + ALL tests (fastest feedback):
./gradlew :composeApp:assembleDebug test --continue

# Check for dependency updates periodically:
./gradlew dependencyUpdates

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
- [ ] Android build + ALL tests pass: `./gradlew :composeApp:assembleDebug test --continue`
- [ ] No iOS builds run (unless specifically required)
- [ ] Dependencies added to `gradle/libs.versions.toml`

---

## 📋 Architecture Patterns (CRITICAL)

### Split-by-Layer Pattern

**Each feature is split into focused layer modules:**

```
:features:pokemonlist:api           → Public contracts only
:features:pokemonlist:data          → Network + Data layer (all KMP targets)
:features:pokemonlist:presentation  → ViewModels, UI state (all KMP targets, exported to iOS)
:features:pokemonlist:ui            → Compose UI screens (Android + JVM only)
:features:pokemonlist:wiring        → DI assembly (platform-specific source sets)
```

**Key Rules:**
1. Each feature has its own network layer (API service, DTOs) in `:data` module
2. Each feature has its own data layer (repositories, mappers) in `:data` module
3. Each feature has its own presentation (ViewModels, UI state) in `:presentation` module - **shared with iOS**
4. Each feature has its own Compose UI in `:ui` module - **Android + JVM only, NOT exported to iOS**
5. Wiring uses platform-specific source sets: `commonMain` provides repos/ViewModels, `androidMain`/`jvmMain` provide UI

**DO NOT create generic :core:network or :core:data modules**
- ❌ `:core:network:api` (generic network layer)
- ✅ `:features:pokemonlist:data/PokemonListApiService.kt`

**Shared infrastructure ONLY for:**
- Design system (UI components, theme)
- Generic utilities (3+ features use it)
- Platform abstractions (expect/actual)

### Dependency Injection: Metro (Implemented in pokemonlist)
**Classes are DI-agnostic. Wire via `@Provides` in wiring modules with platform-specific source sets.**

```kotlin
// :features:jobs:api - Public contract
interface JobRepository {
  suspend fun getJobs(): Either<RepoError, List<Job>>
}

// :features:jobs:data - Internal implementation
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

// :features:jobs:wiring/commonMain - DI assembly for all platforms
@Provides
fun provideJobRepository(api: JobApiService): JobRepository = 
  JobRepository(api)

// :features:jobs:wiring/androidMain - DI assembly for Android UI
@Provides
fun provideJobsScreen(): @Composable () -> Unit = { JobsScreen() }
```

**Why**: Gradle compilation avoidance, hides implementations, simplifies testing, enables platform-specific wiring

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

## 🧪 Testing Strategy: Mobile-First Approach

### ⚠️ TEST ENFORCEMENT: MANDATORY

**NO CODE WITHOUT TESTS** - See `.junie/test-enforcement-agent.md`

Every production code file MUST have a corresponding test file. Tests are not optional—they are part of the feature implementation.

**Quick Enforcement Rules:**
| Production Code | Test Location | Framework |
|----------------|---------------|-----------|
| Repository | androidTest/ | Kotest + MockK |
| ViewModel | androidTest/ | Kotest + MockK |
| Mapper | androidTest/ | Kotest properties |
| Use Case | androidTest/ | Kotest + MockK |
| @Composable | Same file | @Preview + Roborazzi |
| Simple Utility | commonTest/ | kotlin-test |

**Before marking code complete:**
1. ✅ Test file created
2. ✅ Minimum coverage (success + errors)
3. ✅ Tests pass
4. ✅ Preview added (for UI)

### Primary Testing: Android Test Sources

**Why Android Tests for Business Logic:**
- ❌ Kotest doesn't support iOS/Native targets
- ❌ MockK doesn't support iOS/Native targets  
- ✅ Both fully support Android (JVM-based tests)
- ✅ Android is primary mobile target
- ✅ iOS shares same Kotlin code (type safety guarantees)
- ✅ Fast feedback (seconds vs iOS minutes)

**Testing Location Strategy:**
```kotlin
// ✅ PRIMARY: androidTest/ for business logic
features/pokemonlist/impl/src/androidTest/kotlin/
├── data/
│   ├── PokemonRepositoryTest.kt      // Full Kotest + MockK
│   ├── PokemonMappersTest.kt         // Property tests
│   └── PokemonApiServiceTest.kt      // Mocked HTTP
└── presentation/
    └── PokemonViewModelTest.kt       // ViewModel tests

// ⚠️ MINIMAL: commonTest/ for simple utilities only
features/pokemonlist/impl/src/commonTest/kotlin/
└── utils/
    └── UrlUtilsTest.kt               // kotlin-test only, no deps
```

### Kotest + MockK Pattern (Android Tests)

```kotlin
// androidTest/kotlin/.../PokemonRepositoryTest.kt
class PokemonRepositoryTest : StringSpec({
    lateinit var mockApi: PokemonListApiService
    lateinit var repository: PokemonListRepository
    
    beforeTest {
        mockApi = mockk()
        repository = PokemonListRepository(mockApi)
    }
    
    "should return Right on success" {
        coEvery { mockApi.getPokemonList(20, 0) } returns PokemonListDto(
            count = 1292,
            next = "https://...",
            previous = null,
            results = listOf(
                PokemonSummaryDto("bulbasaur", "https://.../1/")
            )
        )
        
        val result = repository.loadPage()
        
        result.shouldBeRight { page ->
            page.pokemons shouldHaveSize 1
            page.pokemons.first().name shouldBe "Bulbasaur"
        }
    }
    
    "should return Network error on timeout" {
        coEvery { mockApi.getPokemonList(any(), any()) } throws 
            ConnectTimeoutException("Timeout")
        
        val result = repository.loadPage()
        
        result.shouldBeLeft { error ->
            error shouldBe RepoError.Network
        }
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

## 🎨 Creative UI/UX Implementation Guide

### When Building Delightful UIs

**Always consult these guides before implementing UI:**
- `.junie/guides/prompts/animation_example_guides.md` — Motion patterns and transitions
- `.junie/guides/prompts/easter_eggs_and_mini_games_guide.md` — Interactive surprises

### Animation Categories & Examples

#### Screen Transitions
```kotlin
// Circular Reveal (from animation guide)
AnimatedContent(
    targetState = currentScreen,
    transitionSpec = {
        // Expands from tapped element with spring physics
        fadeIn() + scaleIn(initialScale = 0.3f) with
        fadeOut() + scaleOut(targetScale = 1.5f)
    }
) { screen -> /* content */ }

// Ink Spill / Liquid Flow
// Use viscous easing for realistic feel
```

**Available Patterns**: Circular Reveal, Origami Fold, Portal Warp, Puzzle Piece, Ink Spill, Ripple Gate

#### Button Micro-Interactions
```kotlin
// Micro-Bounce with Overshoot (from animation guide)
Button(
    onClick = { },
    modifier = Modifier
        .graphicsLayer {
            scaleX = animatedScale
            scaleY = animatedScale
        }
) {
    // Shrink → expand with spring overshoot
}

// Confetti Burst on Success
// Physics-driven particles with gravity and bounce
```

**Available Patterns**: Micro-Bounce, Shape Shifter, Rocket Launch, Confetti Burst, Ripple Feedback

#### List Animations
```kotlin
// Staggered Entrance (from animation guide)
LazyColumn {
    itemsIndexed(items) { index, item ->
        AnimatedVisibility(
            visible = true,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ).plus(fadeIn())
        ) {
            ItemCard(item)
        }
    }
}
```

**Available Patterns**: Staggered Entrance, Bubble Pop, Magnet Shuffle, Parallax Scrolling, Comic POW

#### Gesture-Based Interactions
```kotlin
// Pull-to-Refresh Morph (from animation guide)
PullRefreshIndicator(
    refreshing = isRefreshing,
    state = pullRefreshState,
    modifier = Modifier
        .graphicsLayer {
            // Goo stretch effect
            scaleY = stretchProgress
            rotationZ = wobbleRotation
        }
)

// Elastic Edge on Overscroll
// Springy bounce with dampened velocity
```

**Available Patterns**: Pull-to-Refresh Morph, Liquid Swipe, Elastic Edge, Portal Drag, Magnetic Attraction

### Easter Eggs & Interactive Surprises

#### Device-Based Interactions
```kotlin
// From easter_eggs_and_mini_games_guide.md

// Tilt Detection
val sensorManager = LocalContext.current.getSystemService<SensorManager>()
LaunchedEffect(Unit) {
    // Register accelerometer
    // On tilt → mascot slides, ink spills
}

// Shake Detection
// On shake → particle storm, mascot dance

// Long Press Secrets
Modifier.pointerInput(Unit) {
    detectTapGestures(
        onLongPress = { 
            // Icons wiggle, particles burst, secret messages
        }
    )
}
```

#### Time-Sensitive Triggers
```kotlin
// From easter_eggs_and_mini_games_guide.md

val currentTime = LocalTime.now()
when {
    currentTime.hour == 3 && currentTime.minute == 33 -> {
        // Cosmic starburst animation
    }
    currentTime.hour == 12 && currentTime.minute == 34 -> {
        // Rainbow confetti
    }
    isUserBirthday -> {
        // Mascot waves
    }
}
```

#### Hidden Touch Patterns
```kotlin
// Konami code or custom gesture sequences
var gestureSequence by remember { mutableStateOf(listOf<Gesture>()) }

Box(modifier = Modifier.pointerInput(Unit) {
    detectDragGestures { change, dragAmount ->
        // Track swipe pattern
        if (gestureSequence.matches(SECRET_PATTERN)) {
            // Unlock hidden theme, mini-game, or animation
        }
    }
})
```

#### Mini-Games & Playful Interactions
```kotlin
// Hidden mascot that moves across UI
var mascotPosition by remember { mutableStateOf(Offset.Zero) }

AnimatedVisibility(visible = mascotVisible) {
    Image(
        painter = painterResource(R.drawable.mascot),
        contentDescription = null,
        modifier = Modifier
            .offset { IntOffset(mascotPosition.x.toInt(), mascotPosition.y.toInt()) }
            .pointerInput(Unit) {
                detectTapGestures {
                    // "Catch" the mascot
                    onMascotCaught()
                }
            }
    )
}

// Physics-based playground on device shake
// UI elements fly, bounce, attract each other
```

### Tiered Easter Egg Strategy

**From easter_eggs_and_mini_games_guide.md:**

1. **Common & Subtle** (10% discovery rate)
   - Tiny winks, trail particles, small icon dances
   - Example: Button ripple leaves temporary sparkles

2. **Rare & Magical** (1% discovery rate)
   - Mascot dances, screen-wide transformations, mini-games
   - Example: 10x rapid tap → fireworks burst

3. **Legendary & Context-Sensitive** (0.1% discovery rate)
   - Time-based, tilt-based, collectible chains
   - Example: Swipe Konami code → unlock retro theme
   - Example: Shake during full moon → cosmic animation

### Implementation Checklist

When implementing creative UI:
- [ ] Consulted animation_example_guides.md for motion patterns
- [ ] Considered easter eggs from easter_eggs_and_mini_games_guide.md
- [ ] Added @Preview for all states (loading, success, error, empty)
- [ ] Implemented with spring physics for natural feel
- [ ] Added haptic feedback where appropriate
- [ ] Used velocity-based animations for gesture responsiveness
- [ ] Considered accessibility (reduced motion preferences)
- [ ] Tested on different device sizes
- [ ] Added subtle audio cues (optional)
- [ ] Balanced delight with performance

### AI Prompt Template for UI Implementation

When in **Screen Implementation Mode**, use this template:

```
Generate a Compose Multiplatform screen for [SCREEN_NAME] based on [MARKDOWN_FILE].

Requirements:
- Extract content from markdown (no placeholders)
- Create multiple UI variations (e.g., minimal, playful, premium)
- Include animations from animation_example_guides.md:
  - Screen transitions: [specify type, e.g., Circular Reveal]
  - Button interactions: [specify type, e.g., Micro-Bounce]
  - List animations: [specify type, e.g., Staggered Entrance]
- Consider easter eggs from easter_eggs_and_mini_games_guide.md:
  - [specify if relevant, e.g., tilt interaction, time-based trigger]
- Add @Preview for each variation with realistic data
- Implement with spring physics and dopamine triggers
- Ensure accessibility (color contrast, reduced motion)
```

---

## 🔧 Common Tasks & Solutions

### Checking for Dependency Updates
```bash
# Check for available dependency updates
./gradlew dependencyUpdates

# View detailed report
open build/dependencyUpdates/report.html
```

**Stability Rules** (configured in root `build.gradle.kts`):
- ✅ Stable versions (e.g., `2.8.4`) stay stable—won't upgrade to `2.9.0-alpha01`
- ✅ Unstable versions (e.g., `2.9.0-alpha01`) upgrade within same major.minor only:
  - `2.9.0-alpha01` → `2.9.0-alpha03` ✅ (same major.minor)
  - `2.9.0-alpha01` → `2.9.0-beta01` ✅ (same major.minor)
  - `2.9.0-rc02` → `2.9.0` ✅ (same major.minor)
  - `2.9.0-alpha01` → `2.10.0-alpha01` ❌ (different minor)
  - `2.9.0-alpha01` → `3.0.0-alpha01` ❌ (different major)
  - `2.9.0-alpha01` → `3.9.0-alpha01` ❌ (different major)
- ✅ Unstable versions upgrade to ANY stable version:
  - `2.9.0-alpha02` → `3.1.1` ✅ (stable release)
  - `1.0.0-rc02` → `1.0.0` ✅ (stable release)
- ✅ Gradle wrapper updates also checked

### Adding a New Dependency
```bash
# 1. Check if newer version exists
./gradlew dependencyUpdates

# 2. Add to gradle/libs.versions.toml
[versions]
arrow = "1.2.0"

[libraries]
arrow-core = { module = "io.arrow-kt:arrow-core", version.ref = "arrow" }

# 3. Add to module's build.gradle.kts
commonMain.dependencies {
  implementation(libs.arrow.core)
}

# 4. Sync and validate
./gradlew :composeApp:assembleDebug test --continue
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
              Purpose: Aggregates :features:*:api, :features:*:presentation, :core:* modules
              Note: Contains minimal/no business logic itself

:iosApp      → Native SwiftUI iOS app
  ├── SwiftUI views        ← iOS-specific UI implementation
  └── import Shared        → Accesses KMP modules via shared.framework

:server      → Ktor Backend-for-Frontend (BFF)
  └── src/main/kotlin      ← REST API for all clients
```

### Feature Module Pattern
```
:features:<feature>:api     → Public contracts (exported to iOS via :shared)
:features:<feature>:data    → Network + Data layer (NOT exported to iOS)
:features:<feature>:presentation → ViewModels, UI state (exported to iOS via :shared)
:features:<feature>:ui      → Compose UI screens (NOT exported to iOS)
:features:<feature>:wiring  → DI assembly (NOT exported to iOS)

:core:domain           → Shared domain models (exported to iOS via :shared)
:core:util             → Shared utilities (exported to iOS via :shared)

# When creating: consult .junie/guides/tech/conventions.md first
# iOS export rule: Only :api, :presentation, and :core:* modules in :shared umbrella
```

---

## 🎯 Decision Matrix

### When to Create a New Module?
```
IF defining cross-feature contracts → :features:<name>:api (export to iOS via :shared)
IF implementing data layer         → :features:<name>:data (do NOT export to iOS)
IF implementing ViewModels         → :features:<name>:presentation (export to iOS via :shared)
IF implementing Compose UI         → :features:<name>:ui (do NOT export to iOS)
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
./gradlew :composeApp:assembleDebug test --continue

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

### Specialized Agent Prompts
- **Product Designer Agent**: `.junie/guides/prompts/product_designer_agent_system_prompt.md`
- **UI/UX Agent**: `.junie/guides/prompts/uiux_agent_system_prompt.md`
- **Onboarding Agent**: `.junie/guides/prompts/onboarding_agent_system_prompt.md`
- **User Flow Agent**: `.junie/guides/prompts/user_flow_agent_system_prompt.md`
- **Generic Screen UI Agent**: `.junie/guides/prompts/ui_ux_system_agent_for_generic_screen.md`
- **Animation Guides**: `.junie/guides/prompts/animation_example_guides.md`
- **Easter Eggs & Mini-Games**: `.junie/guides/prompts/easter_eggs_and_mini_games_guide.md`

---

## 🎭 Specialized Agent Modes

When working on specific types of tasks, agents should adopt specialized modes by consulting the relevant system prompts:

### Product Design Mode
**When to use**: Creating or refining product requirements, defining features, analyzing competitors

**Prompt**: `.junie/guides/prompts/product_designer_agent_system_prompt.md`

**Key responsibilities**:
- Turn raw ideas into structured PRDs
- Define core features with measurable requirements
- Identify USPs and competitive positioning
- Specify technical and design constraints

### UI/UX Design Mode
**When to use**: Planning screen layouts, designing user flows, creating visual directions

**Prompt**: `.junie/guides/prompts/uiux_agent_system_prompt.md`

**Key responsibilities**:
- Design screen structures with delight factors
- Plan animations and micro-interactions
- Create multiple design direction options
- Consider emotional journey and dopamine triggers
- **Always reference animation guides** for creative motion patterns

**Animation Resources**:
- `.junie/guides/prompts/animation_example_guides.md` — Screen transitions, button animations, list effects, gestures
- `.junie/guides/prompts/easter_eggs_and_mini_games_guide.md` — Device interactions (tilt, shake), hidden patterns, mini-games

### Onboarding Design Mode
**When to use**: Creating onboarding flows, writing copy, designing welcome screens

**Prompt**: `.junie/guides/prompts/onboarding_agent_system_prompt.md`

**Key responsibilities**:
- Hook users emotionally from first screen
- Show problem → solution → benefit progression
- Write benefit-driven, relatable copy
- Design soft paywall hybrid screens
- Provide detailed illustration descriptions

### User Flow Planning Mode
**When to use**: Mapping navigation paths, defining screen sequences, planning user journeys

**Prompt**: `.junie/guides/prompts/user_flow_agent_system_prompt.md`

**Key responsibilities**:
- Map complete user journeys from launch to goal
- Define all screens with purposes and actions
- Specify primary and secondary navigation
- Cover edge cases and error states

### Screen Implementation Mode
**When to use**: Building UI screens in Compose Multiplatform from markdown specifications

**Prompt**: `.junie/guides/prompts/ui_ux_system_agent_for_generic_screen.md`

**Key responsibilities**:
- Create production-ready Compose Multiplatform code
- Implement multiple UI variations in single file
- Add animations, transitions, micro-interactions
- Include @Preview annotations for all variations
- Extract content from markdown specs
- Reference animation and easter egg guides for creative implementations

**Implementation Pattern**:
```kotlin
// Create modular, reusable composables
@Composable
fun ScreenVariation1(data: ScreenData, modifier: Modifier = Modifier) { }

@Composable
fun ScreenVariation2(data: ScreenData, modifier: Modifier = Modifier) { }

@Preview
@Composable
private fun ScreenVariation1Preview() { }

@Preview
@Composable
private fun ScreenVariation2Preview() { }
```

### Mode Selection Guidelines

**Product questions** → Product Design Mode
- "Define the app features"
- "What should the MVP include?"
- "Who are our competitors?"

**Design questions** → UI/UX Design Mode
- "Design the home screen"
- "What should the visual style be?"
- "Add delightful animations"

**Copy/messaging questions** → Onboarding Design Mode
- "Write onboarding screens"
- "Create welcome messages"
- "Design paywall screen"

**Navigation questions** → User Flow Planning Mode
- "Map the user journey"
- "Plan screen sequences"
- "Define navigation structure"

**Implementation questions** → Screen Implementation Mode
- "Implement the onboarding screen"
- "Create UI from onboarding.md"
- "Build the paywall interface"

**Technical questions** → Standard Development Mode (this document)
- "Implement repository"
- "Create ViewModel"
- "Write tests"

---

## 🎓 Learning Path for New Agents

### Day 1: Foundation
1. Read this file completely
2. Read `.junie/guides/tech/conventions.md`
3. Run `./gradlew projects` to see structure
4. Run `./gradlew :composeApp:assembleDebug test --continue` successfully
5. **Review specialized agent prompts** in `.junie/guides/prompts/`

### Day 2: Technical Patterns
1. Study Arrow Either in `.junie/guides/tech/repository.md`
2. Study ViewModel pattern in `.junie/guides/tech/presentation_layer.md`
3. Review existing code in `:composeApp` and `:server`

### Day 3: Design & UX Patterns
1. Read animation guides in `.junie/guides/prompts/animation_example_guides.md`
2. Review easter egg patterns in `.junie/guides/prompts/easter_eggs_and_mini_games_guide.md`
3. Study UI/UX agent prompt for screen design principles
4. Understand how to create delightful, dopamine-triggering UIs

### Day 4: Practice
1. Write a simple repository with Either
2. Write a ViewModel following the pattern
3. Write Kotest tests for both
4. Create a UI screen with @Preview and animations
5. Run `./gradlew :composeApp:testDebugUnitTest`

### Ongoing
- Reference `.junie/guides/` for specific patterns
- Check `.junie/guides/project/prd.md` for requirements
- **Switch to specialized agent modes** for product/design/flow tasks
- Use animation guides for creative UI implementations
- Validate all changes with Android build
- Never run iOS builds unless required

---

## ✅ Success Criteria

**You're effective when you can**:
- [ ] Implement repositories returning `Either<RepoError, T>`
- [ ] Create ViewModels following lifecycle-aware pattern
- [ ] Write Kotest tests with proper assertions
- [ ] Add dependencies via version catalog
- [ ] Validate changes with `./gradlew :composeApp:assembleDebug test --continue`
- [ ] Find answers in `.junie/guides/` documentation
- [ ] Avoid common pitfalls (scope storage, init work, iOS builds)
- [ ] **Switch to appropriate agent mode** for non-technical tasks
- [ ] **Reference animation guides** when implementing UI
- [ ] **Create delightful UIs** with micro-interactions and easter eggs

**You're ready for complex tasks when you can**:
- [ ] Create new feature modules (api/impl/wiring)
- [ ] Implement Metro DI patterns
- [ ] Write property-based tests
- [ ] Create Roborazzi screenshot tests
- [ ] Implement Navigation 3 contracts
- [ ] Design sealed error hierarchies
- [ ] **Design product requirements** using Product Design Mode
- [ ] **Plan UI/UX flows** with animation and delight factors
- [ ] **Implement screens from markdown** using Screen Implementation Mode
- [ ] **Create onboarding flows** with emotional engagement

---

## 💡 Pro Tips

### Technical Excellence
1. **Always search `.junie/guides/` first** before asking questions
2. **Android build is your friend** - run it often (45s feedback)
3. **iOS builds are your enemy** - avoid unless absolutely necessary (5-10min)
4. **Version catalog is king** - all dependencies go there first
5. **Either is your boundary** - repositories always return it
6. **ViewModels are lifecycle-aware** - never init work
7. **Kotest is your safety net** - write tests as you code
8. **Patterns are documented** - follow them exactly

### Design & UX Excellence
9. **Use specialized agent modes** - switch context for product/design/flow tasks
10. **Animation guides are essential** - reference them for every UI implementation
11. **Easter eggs add magic** - consider device interactions, hidden patterns, mini-games
12. **Every @Composable needs @Preview** - with realistic data, not placeholders
13. **Delight factors matter** - think dopamine triggers, micro-interactions, emotional journey
14. **Multiple variations help** - loading, error, empty, success states need previews
15. **Extract from markdown** - screen implementation mode uses .md files as source of truth

---

## 🛡️ Project Conventions Enforcement

**MANDATORY**: After implementing any code, you MUST validate compliance with project conventions. This ensures consistency and quality across the codebase.

### Post-Implementation Validation Checklist

#### 1. Architecture Compliance
- [ ] **Vertical slices**: Features properly modularized (api/impl/wiring)
- [ ] **Module visibility**: Only `api` modules exposed; `impl`/`wiring` internal
- [ ] **iOS exports**: Only `:api` and `:core:*` in `:shared` umbrella (never `:impl` or `:wiring`)
- [ ] **No cross-impl dependencies**: Features depend only on other features' `api` modules

#### 2. Interface Pattern (CRITICAL - Always Validate)
**Every interface MUST follow Impl + Factory Function pattern**:

```kotlin
// ✅ CORRECT
interface JobRepository { ... }
internal class JobRepositoryImpl(...) : JobRepository { ... }
fun JobRepository(...): JobRepository = JobRepositoryImpl(...)

// ❌ WRONG
class JobRepository(...) { ... }  // Missing interface
class JobRepositoryImpl(...) : JobRepository  // Public impl
```

- [ ] Implementation class: `<InterfaceName>Impl` (internal/private)
- [ ] Factory function: `fun <InterfaceName>(...): <InterfaceName> = <InterfaceName>Impl(...)`
- [ ] Factory is public, implementation is internal

#### 3. Repository Boundary Rules (CRITICAL)
- [ ] Returns `Either<RepoError, T>` (NEVER `Result`, nullable, or throws)
- [ ] Uses `Either.catch { }.mapLeft { it.toRepoError() }` pattern
- [ ] Defines sealed error hierarchy per feature
- [ ] Maps DTOs to domain at boundary
- [ ] Never swallows `CancellationException`

**Example**:
```kotlin
// ✅ CORRECT
override suspend fun getJobs(): Either<RepoError, List<Job>> =
  Either.catch {
    api.getJobs().jobs.map { it.asDomain() }
  }.mapLeft { it.toRepoError() }

// ❌ WRONG
suspend fun getJobs(): List<Job> = api.getJobs().jobs  // Throws
suspend fun getJobs(): List<Job>? = try { ... } catch { null }  // Nullable
suspend fun getJobs(): Result<List<Job>> = ...  // Result type
```

#### 4. ViewModel Pattern (CRITICAL)
- [ ] Extends `androidx.lifecycle.ViewModel`
- [ ] `viewModelScope` passed to superclass constructor (default value provided)
- [ ] NO `CoroutineScope` stored as field
- [ ] NO work in `init` block
- [ ] Implements `UiStateHolder<S, E>`
- [ ] Uses `kotlinx.collections.immutable` types
- [ ] Lifecycle-aware data loading

**Example**:
```kotlin
// ✅ CORRECT
class HomeViewModel(
  private val repo: JobRepository,
  viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) : ViewModel(viewModelScope), UiStateHolder<HomeUiState, HomeUiEvent> {
  
  fun start(lifecycle: Lifecycle) {
    viewModelScope.launch {
      lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
        loadData()
      }
    }
  }
}

// ❌ WRONG
class HomeViewModel : ViewModel() {
  private val scope = CoroutineScope(SupervisorJob())  // Field storage
  init { loadData() }  // Work in init
}
```

#### 5. Dependency Injection (Metro)
- [ ] Production classes free of DI annotations
- [ ] Wiring modules use `@Provides` functions
- [ ] `@Provides` functions return interface types
- [ ] Factory functions used in wiring modules
- [ ] Graph structure correct (`AppGraph`, scopes)

#### 6. Testing Requirements
- [ ] Kotest tests written in `commonTest/` (or platform-specific)
- [ ] MockK for JVM/Android (fakes for Native)
- [ ] Property-based tests for parsers/mappers (`checkAll`, `forAll`)
- [ ] Repository tests cover success and error cases
- [ ] ViewModel tests cover all UI states
- [ ] Screenshot tests (Roborazzi) for UI components

#### 7. Navigation Contracts (When Applicable)
- [ ] Contracts in `:api` module
- [ ] Implementations in `:impl` module
- [ ] Wired in `:wiring` module
- [ ] Navigation 3 artifacts used

#### 8. Module Structure
- [ ] Correct naming: `:features:<feature>:api/impl/wiring`
- [ ] Convention plugins applied (`convention.feature.api`, etc.)
- [ ] No direct feature-to-feature `impl` dependencies
- [ ] Compilation avoidance respected

#### 9. Code Quality
- [ ] Dependencies in `gradle/libs.versions.toml`
- [ ] Type-safe project accessors used (`projects.shared`, `libs.arrow.core`)
- [ ] ktlint formatting (configured via convention plugins)
- [ ] detekt static analysis (configured via convention plugins)
- [ ] All @Composable functions have @Preview annotations
- [ ] All SwiftUI Views have #Preview macros

#### 10. Build Validation
- [ ] Android build + ALL tests pass: `./gradlew :composeApp:assembleDebug test --continue`
- [ ] Unit tests pass: `./gradlew :composeApp:testDebugUnitTest`
- [ ] No iOS builds run (unless explicitly required)

### Self-Review Process

After implementing code, perform this self-review:

1. **Identify Pattern**: What pattern does this code follow? (Repository, ViewModel, Navigation, etc.)
2. **Check Critical Rules**: 
   - Impl + Factory? ✅/❌
   - Either boundary? ✅/❌
   - ViewModel lifecycle? ✅/❌
3. **Validate Dependencies**: Are modules correctly structured?
4. **Run Tests**: Do tests cover critical paths?
5. **Build Validation**: Does Android build pass?

### Reporting Violations

If you find violations in your own code:

**Format**:
```
❌ VIOLATION: [Pattern Name]
Location: [File:Line]
Issue: [Specific problem]
Fix: [Code showing correct implementation]
```

**Example**:
```
❌ VIOLATION: Impl + Factory Pattern
Location: JobRepositoryImpl.kt:15
Issue: JobRepositoryImpl is public; should be internal
Fix:
  // Change:
  class JobRepositoryImpl(...) : JobRepository
  // To:
  internal class JobRepositoryImpl(...) : JobRepository
```

### Severity Levels

- **CRITICAL**: Wrong error types, missing factory pattern, DI leaks, work in init
- **HIGH**: Missing tests, wrong module dependencies, missing immutable types
- **MEDIUM**: Naming conventions, documentation gaps
- **LOW**: Code style, minor optimizations

**Fix CRITICAL and HIGH violations immediately**. MEDIUM and LOW can be noted for later improvement.

### Self-Validation Output Template

After implementing code, provide:

```
## Compliance Review

### Summary
[Compliant / Minor Issues / Major Violations]

### Critical Violations (Fix Immediately)
[List with location, issue, fix]

### High Priority Issues
[List with location, issue, fix]

### Improvement Opportunities
[Suggestions for better alignment]

### Positive Observations
[What follows conventions well]

### Build Validation
- [ ] Android build: ./gradlew :composeApp:assembleDebug test --continue
- [ ] Unit tests: ./gradlew :composeApp:testDebugUnitTest
```

### Quick Reference: Common Violations

| Violation | Correct Pattern |
|-----------|----------------|
| `class XImpl : X` (public) | `internal class XImpl : X` |
| Missing factory function | `fun X(...): X = XImpl(...)` |
| `suspend fun get(): T?` | `suspend fun get(): Either<RepoError, T>` |
| `private val scope = ...` | `viewModelScope: CoroutineScope` param |
| `init { loadData() }` | `fun start(lifecycle: Lifecycle) { ... }` |
| `_state: MutableStateFlow<List<T>>` | `_state: MutableStateFlow<ImmutableList<T>>` |
| Empty use case | Call repository directly from ViewModel |
| `:impl` exported to iOS | Only `:api` and `:core:*` in `:shared` |
| @Composable without @Preview | Add `@Preview` with realistic data |
| SwiftUI View without #Preview | Add `#Preview` with realistic data |
| Manual cast after `shouldBeInstanceOf` | Use smart cast directly (see kotest-smart-casting-quick-ref.md) |

### Common Testing Pitfalls

**Manual casting after type-checking matchers** - Kotest matchers provide smart casting through compiler contracts.

See `.junie/guides/tech/kotest-smart-casting-quick-ref.md` for complete guide.

**Quick checklist**:
- ✅ No manual casts after `shouldBeInstanceOf<T>()`
- ✅ Use return value of `shouldBeLeft()` / `shouldBeRight()`
- ✅ No safe calls (`?.`) after `shouldNotBeNull()`
- ✅ IDE warnings about unnecessary casts should be fixed

---

## UI Development Standards (MANDATORY)

### Compose Multiplatform Previews

**Rule**: Every @Composable function MUST have a corresponding @Preview.

**Requirements:**
1. Use `@Preview` from `org.jetbrains.compose.ui.tooling.preview.Preview`
2. Preview function should be private
3. Named `<ComponentName>Preview`
4. Show realistic data (not empty/null)
5. Wrap in MaterialTheme or your theme
6. Multiple previews for complex components (different states)

**Example:**
```kotlin
@Composable
fun PokemonCard(
    pokemon: Pokemon,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Column {
            AsyncImage(model = pokemon.imageUrl, contentDescription = pokemon.name)
            Text(text = pokemon.name)
        }
    }
}

@Preview
@Composable
private fun PokemonCardPreview() {
    MaterialTheme {
        PokemonCard(
            pokemon = Pokemon(
                id = 25,
                name = "Pikachu",
                imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png"
            )
        )
    }
}

@Preview
@Composable  
private fun PokemonCardLongNamePreview() {
    MaterialTheme {
        PokemonCard(
            pokemon = Pokemon(
                id = 1,
                name = "Bulbasaur with very long name",
                imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png"
            )
        )
    }
}
```

### SwiftUI Previews

**Rule**: Every SwiftUI View MUST have a #Preview.

**Requirements:**
1. Use Swift's `#Preview` macro
2. Show realistic data
3. Can have multiple previews for different states

**Example:**
```swift
struct PokemonCard: View {
    let pokemon: Pokemon
    
    var body: some View {
        VStack {
            AsyncImage(url: URL(string: pokemon.imageUrl))
                .frame(width: 96, height: 96)
            Text("#\(pokemon.id)")
                .font(.caption)
            Text(pokemon.name)
                .font(.headline)
        }
        .padding()
        .background(Color.gray.opacity(0.1))
        .cornerRadius(8)
    }
}

#Preview {
    PokemonCard(pokemon: Pokemon(
        id: 25,
        name: "Pikachu",
        imageUrl: "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png"
    ))
}

#Preview("Long Name") {
    PokemonCard(pokemon: Pokemon(
        id: 1,
        name: "Bulbasaur with very long name",
        imageUrl: "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png"
    ))
}
```

### Screen-Level Previews

For screens with ViewModels, create preview-specific versions:

```kotlin
@Preview
@Composable
private fun PokemonListScreenLoadingPreview() {
    MaterialTheme {
        PokemonListContent(
            uiState = PokemonListUiState.Loading,
            onLoadMore = {}
        )
    }
}

@Preview
@Composable
private fun PokemonListScreenContentPreview() {
    MaterialTheme {
        PokemonListContent(
            uiState = PokemonListUiState.Content(
                pokemons = persistentListOf(
                    Pokemon(1, "Bulbasaur", "..."),
                    Pokemon(2, "Ivysaur", "..."),
                    Pokemon(3, "Venusaur", "...")
                ),
                isLoadingMore = false,
                hasMore = true
            ),
            onLoadMore = {}
        )
    }
}

@Preview
@Composable
private fun PokemonListScreenErrorPreview() {
    MaterialTheme {
        PokemonListContent(
            uiState = PokemonListUiState.Error("Network error"),
            onLoadMore = {}
        )
    }
}
```

### Why Previews are Mandatory

1. **Fast Development**: See changes instantly without running the app
2. **Design Validation**: Verify UI matches specs
3. **Edge Cases**: Test different states (loading, error, empty, long text)
4. **Documentation**: Previews serve as live documentation
5. **Team Collaboration**: Designers/PMs can review UI without building

### Preview Validation

Before committing code, verify:
- [ ] Every @Composable has at least one @Preview
- [ ] Every SwiftUI View has at least one #Preview
- [ ] Previews show realistic data
- [ ] Complex components have previews for different states
- [ ] Previews compile and render correctly

---

**Remember**: This is a POC. Patterns are documented but not fully implemented. Your job is to implement them correctly following the guides, or work within the existing structure. When in doubt, consult `.junie/guides/tech/conventions.md` first.

**Enforcement is mandatory**: After any code change, run through the validation checklist. This ensures consistency and prevents technical debt.
