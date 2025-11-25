# Copilot Instructions for Kotlin Multiplatform POC

## Project Overview

This is a **Kotlin Multiplatform project** with **Compose Multiplatform UI for Android and Desktop (JVM)**, and a **native SwiftUI app for iOS** that consumes shared business logic. The codebase is in **early POC stage**—only skeleton modules exist with minimal implementation. Comprehensive architecture patterns are documented in `.junie/guides/` but NOT yet implemented.

### Current Module Structure
```
:composeApp   → Compose Multiplatform UI (Android + Desktop JVM)
              ├── commonMain: Shared Compose UI code
              ├── androidMain: Android-specific implementations
              └── jvmMain: Desktop-specific implementations

:shared       → iOS umbrella framework
              └── Exports: Other KMP modules (:features:*:api, :core:*) to iOS
              Note: Does NOT contain business logic itself

:iosApp       → Native SwiftUI iOS app
              └── Consumes: shared.framework to access KMP modules

:server       → Ktor backend (Netty on port 8080) - Backend-for-Frontend (BFF)
```

### Required Architecture
Vertical-slice modularization with api/impl/wiring pattern:
- `:features:<feature>:api` → Public contracts (interfaces, navigation, models) - exported to iOS
- `:features:<feature>:impl` → ALL layers (network, data, domain, presentation, UI) - NOT exported
- `:features:<feature>:wiring` → Metro DI assembly (NOT exported)

**Critical**: True vertical slicing means each feature owns ALL its layers:
- Network layer (API services, DTOs) lives in the feature's :impl module
- Data layer (repositories, mappers) lives in the feature's :impl module  
- Domain logic (use cases, validators) lives in the feature's :impl module
- Presentation (ViewModels, UI) lives in the feature's :impl module

**DO NOT create generic :core:network or :core:data modules**. Each feature is self-contained.

**⚠️ CRITICAL**: Feature modules must follow this pattern. Always consult `.junie/guides/tech/conventions.md` before creating new modules or implementing patterns.

**Current State**: Only skeleton modules exist (composeApp, shared, server, build-logic). Create feature modules following the required architecture as needed.

## Build & Validation Workflow

### Primary Validation Command
```bash
# Always validate with Android build first (fastest feedback)
./gradlew :composeApp:assembleDebug
```

### Platform-Specific Commands
```bash
# Desktop/JVM
./gradlew :composeApp:run

# Ktor Server (port 8080)
./gradlew :server:run

# Tests
./gradlew :composeApp:testDebugUnitTest              # Unit tests
./gradlew recordRoborazziDebug                       # Record UI baselines
./gradlew verifyRoborazziDebug                       # Verify against baselines
```

### iOS Build Policy ⚠️
**NEVER run iOS builds during routine validation.** iOS builds are extremely slow (5-10min). Only execute when:
1. Explicitly requested by user
2. Testing iOS framework exports (verifying which modules are exposed)
3. Validating iOS-specific expect/actual implementations in KMP modules
4. Working on SwiftUI integration with shared.framework

**Note**: iOS uses native SwiftUI for UI (not Compose). The `:shared` module is an umbrella that exports KMP modules.

Entry point: `iosApp/iosApp.xcodeproj` (Xcode only)

### Test Configuration
- **Framework**: Kotest (assertions, property-based testing)
- **Mocking**: MockK (JVM/Android only—use fakes for Native)
- **Screenshot**: Roborazzi (Robolectric-based, JVM tests)
- **Location**: Tests live in `commonTest/` unless platform-specific

## Critical Project Conventions

### Dependency Injection (Metro)
**Pattern**: Classes stay DI-agnostic; wiring happens in separate modules
```kotlin
// :features:jobs:api
interface JobRepository {
  suspend fun getJobs(): Either<RepoError, List<Job>>
}

// :features:jobs:impl (internal)
internal class JobRepositoryImpl(...) : JobRepository { ... }
fun JobRepository(...): JobRepository = JobRepositoryImpl(...)  // Factory

// :features:jobs:wiring
@Provides
fun provideJobRepository(...): JobRepository = JobRepository(...)
```
**Why**: Enables Gradle compilation avoidance, keeps implementations hidden, simplifies testing

### Error Handling (Arrow Either)
**Pattern**: Repositories return `Either<RepoError, T>`, never throw or return null
```kotlin
override suspend fun getJobs(): Either<RepoError, List<Job>> =
  Either.catch {
    api.getJobs().jobs.map { it.asDomain() }
  }.mapLeft { it.toRepoError() }
```
**Define sealed errors per feature**:
```kotlin
sealed interface RepoError {
  data object Network : RepoError
  data class Http(val code: Int, val message: String?) : RepoError
  data object Unauthorized : RepoError
  data class Unknown(val cause: Throwable) : RepoError
}
```

### ViewModels (androidx.lifecycle)
**Pattern**: Must extend `ViewModel`, pass `viewModelScope` as constructor parameter
```kotlin
class HomeViewModel(
  private val repo: JobRepository,
  viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) : ViewModel(viewModelScope),
    UiStateHolder<HomeUiState, HomeUiEvent> {
  
  private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
  override val uiState: StateFlow<HomeUiState> = _uiState
  
  // ⚠️ Do NOT perform work in init
  fun start(lifecycle: Lifecycle) {
    viewModelScope.launch {
      lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
        repo.getJobs().fold(
          ifLeft = { _uiState.value = HomeUiState.Error(it.toUiMessage()) },
          ifRight = { jobs -> _uiState.value = HomeUiState.Content(jobs.toImmutableList()) }
        )
      }
    }
  }
}
```
**Requirements**:
- Use `kotlinx.collections.immutable` types in UI state
- Never store `CoroutineScope` as field—pass to superclass constructor
- For one-time events: implement `OneTimeEventEmitter<E>` via delegation

### Navigation (Navigation 3)
**Pattern**: Contracts in `:api`, implementations in `:impl`, wired in `:wiring`
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
**Rule**: Call repositories directly from ViewModels unless orchestrating multiple repos or applying business rules
```kotlin
// ❌ Don't create pass-through use cases
class GetUserUseCase(private val repo: UserRepository) {
  suspend operator fun invoke(id: String) = repo.getUser(id)  // Useless layer
}

// ✅ Call repository directly
class ProfileViewModel(private val repo: UserRepository, ...) {
  fun load(id: String) = viewModelScope.launch {
    repo.getUser(id).fold(...)
  }
}
```

## Key Technical Documentation

**Start here for implementation guidance** (`.junie/guides/tech/`):
1. **`conventions.md`** — Master reference: architecture, modules, DI, testing rules
2. **`dependency_injection.md`** — Metro setup, @Provides patterns, graph extensions
3. **`repository.md`** — Either boundaries, DTO→domain mapping, error handling
4. **`presentation_layer.md`** — ViewModel lifecycle, UiStateHolder pattern, event handling
5. **`navigation.md`** — Navigation 3 contracts, routing, deep links
6. **`testing_strategy.md`** — Kotest assertions, property-based tests, Roborazzi

**Product requirements** (`.junie/guides/project/`):
- **`prd.md`** — Features, acceptance criteria, business rules
- `user_flow.md`, `onboarding.md`, `ui_ux.md` — User journeys and UX specs

## Common Patterns & Conventions

### Code Organization
- **Compose UI (Android/Desktop)**: `composeApp/src/commonMain/` for shared UI
- **Platform-specific UI**: `androidMain/`, `jvmMain/` (iOS uses SwiftUI separately)
- **Business logic**: In feature modules (`:features:<feature>:api`, `:features:<feature>:impl`) and core modules
- **iOS umbrella**: `shared/` module exports other KMP modules to iOS (minimal code, mostly Gradle config)
- **expect/actual pattern**: In feature/core modules, e.g., `shared/src/.../Platform.kt` for platform abstractions

```kotlin
// Example in any KMP module (not necessarily in :shared)
// commonMain
expect fun getPlatform(): Platform

// androidMain
actual fun getPlatform(): Platform = AndroidPlatform()

// iosMain (exported via :shared umbrella to SwiftUI app)
actual fun getPlatform(): Platform = IOSPlatform()
```

### Version Management
- **Catalog**: `gradle/libs.versions.toml` (centralized dependency versions)

### Gradle Configuration
- **All build files**: Kotlin DSL (`build.gradle.kts`)
- **Type-safe accessors**: Enabled via `TYPESAFE_PROJECT_ACCESSORS`
- **Project structure**:
  ```kotlin
  implementation(libs.androidx.lifecycle.viewmodelCompose)  // Libs catalog
  implementation(projects.shared)                          // Project accessor
  ```

### Integration Points
- **Server (BFF)**: Ktor backend on port 8080 provides APIs for all clients
  - Defined in: Shared constants module or configuration
  - Used by: Android app, Desktop app, iOS app
  - Purpose: Backend-for-Frontend, aggregation, business logic

- **:shared Umbrella ↔ iOS SwiftUI**: 
  - `:shared` exports: Only `:features:<feature>:api` and `:core:*` modules to iOS
  - Never export: `:impl`, `:wiring`, or UI modules (`:composeApp`)
  - iOS accesses: KMP business logic via shared.framework
  - iOS implements: Native SwiftUI views, calls exported KMP APIs
  
- **Compose UI (Android/Desktop)**:
  - Shared: Common Compose UI in `composeApp/commonMain`
  - Platform-specific: `androidMain`, `jvmMain` for platform adaptations
  
- **Platform abstractions**: Use expect/actual in feature/core modules for iOS platform APIs

## Development Workflow Best Practices

### Before Writing Code
1. **Check existing patterns**: Search `.junie/guides/tech/` for relevant guidelines
2. **Verify module structure**: Run `./gradlew projects` to see current modules
3. **Review conventions**: Reference `.junie/guides/tech/conventions.md` for patterns

### When Implementing Features
1. **Start with Android validation**: `./gradlew :composeApp:assembleDebug` (fastest feedback)
2. **Test incrementally**: Run unit tests after each logical change
3. **Avoid premature optimization**: Implement working code first, optimize if needed
4. **Don't create feature modules prematurely**: Only 3 modules exist (composeApp, server, shared)

### When Adding Dependencies
1. **Use version catalog**: Add to `gradle/libs.versions.toml`
2. **Check compatibility**: Ensure KMP support for multiplatform modules
3. **Prefer common dependencies**: Add to `commonMain` unless platform-specific

### UI Development Requirements (MANDATORY)

#### Compose Multiplatform
- **Every @Composable must have a @Preview**
- Use `@Preview` from `org.jetbrains.compose.ui.tooling.preview.Preview`
- Preview function should be private and named `<ComponentName>Preview`
- Show realistic data in previews (not empty states)
- Complex screens need multiple previews for different states

**Example:**
```kotlin
@Composable
fun PokemonCard(pokemon: Pokemon, modifier: Modifier = Modifier) {
    Card { /* ... */ }
}

@Preview
@Composable
private fun PokemonCardPreview() {
    MaterialTheme {
        PokemonCard(
            pokemon = Pokemon(
                id = 25,
                name = "Pikachu",
                imageUrl = "https://example.com/pikachu.png"
            )
        )
    }
}
```

#### SwiftUI
- **Every View must have a #Preview**
- Use Swift's `#Preview` macro
- Show realistic data in previews

**Example:**
```swift
struct PokemonCard: View {
    let pokemon: Pokemon
    var body: some View { /* ... */ }
}

#Preview {
    PokemonCard(pokemon: Pokemon(
        id: 25,
        name: "Pikachu",
        imageUrl: "https://example.com/pikachu.png"
    ))
}
```

### Testing Strategy
- **Unit tests first**: Write tests in `androidTest/` to leverage Kotest and MockK
- **Mobile-first approach**: Android is the primary mobile target, tests cover shared logic
- **Property-based testing**: Use Kotest's `checkAll`/`forAll` for invariants
- **MockK for dependencies**: Available in Android tests for powerful mocking
- **Screenshot tests**: Use Roborazzi (Robolectric-based, Android tests)
- **Test Location**: Use `androidTest/` for business logic tests (repository, mappers, use cases)
- **Common tests**: Only for platform-agnostic utilities that need no mocking

**Why Android Tests for Business Logic:**
- ✅ Full Kotest support (assertions, property testing, framework)
- ✅ MockK available for powerful mocking
- ✅ Faster than iOS builds (seconds vs minutes)
- ✅ Tests cover shared KMP code (runs on JVM, validates all logic)
- ✅ Mobile is the primary target (Android + iOS share code)
- ✅ JVM desktop is convenience, not primary target

**Trade-off Rationale:**
- Android tests validate ALL shared business logic
- iOS compiles the same Kotlin code (type safety guarantees)
- Native-specific code uses expect/actual (minimal, well-isolated)
- Testing on Android JVM validates multiplatform Kotlin behavior
- Focus testing effort on mobile scenarios (primary use case)

### Error Handling Checklist
- ✅ Repositories return `Either<RepoError, T>`
- ✅ Use `Either.catch { }` to wrap throwing code
- ✅ Map exceptions with `.mapLeft { it.toRepoError() }`
- ✅ Define sealed error hierarchies per feature
- ❌ Never swallow `CancellationException`
- ❌ Don't use `Result` or nullable returns at boundaries

### ViewModel Checklist
- ✅ Extend `androidx.lifecycle.ViewModel`
- ✅ Pass `viewModelScope` as constructor parameter
- ✅ Use `StateFlow` for UI state
- ✅ Load data in lifecycle-aware callbacks (not `init`)
- ✅ Use `kotlinx.collections.immutable` types
- ❌ Don't store `CoroutineScope` as field
- ❌ Don't perform work in constructor/init

### When Stuck
1. **Search documentation**: Use grep/search in `.junie/guides/`
2. **Find examples**: Look for similar patterns in tech guides
3. **Check PRD**: Verify requirements in `.junie/guides/project/prd.md`
4. **Ask for clarification**: Note gaps for user feedback

## Project Conventions Enforcement

**CRITICAL**: After any code generation or modification, validate compliance with project conventions.

### Self-Validation Checklist

Run through this after implementing code:

#### Architecture & Patterns
- [ ] Clean Architecture with vertical slices maintained
- [ ] Only `api` modules exposed cross-feature; `impl`/`wiring` internal
- [ ] Impl + Factory pattern: `internal class XImpl`, `fun X(...): X = XImpl(...)`
- [ ] No feature-to-feature `impl` dependencies (only `api` allowed)

#### Critical Patterns
- [ ] Repositories return `Either<RepoError, T>` (never `Result` or nullable)
- [ ] Use `Either.catch { }.mapLeft { it.toRepoError() }` pattern
- [ ] ViewModels extend `androidx.lifecycle.ViewModel`
- [ ] ViewModels pass `viewModelScope` to constructor (never stored as field)
- [ ] NO work in `init` blocks (use lifecycle-aware callbacks)
- [ ] Expose immutable collections (`kotlinx.collections.immutable`)

#### Dependency Injection
- [ ] Production classes free of DI annotations
- [ ] Wiring modules use `@Provides` functions returning interfaces
- [ ] Metro DI conventions followed

#### Testing
- [ ] Kotest tests written in `commonTest/`
- [ ] Property-based tests for parsers/mappers (`checkAll`/`forAll`)
- [ ] MockK for JVM/Android; fakes for Native
- [ ] Screenshot tests with Roborazzi where applicable

#### Build & Quality
- [ ] Dependencies added to `gradle/libs.versions.toml`
- [ ] Convention plugins applied appropriately
- [ ] Code passes ktlint/detekt (configured via convention plugins)
- [ ] Android build validates: `./gradlew :composeApp:assembleDebug`
- [ ] All @Composable functions have @Preview

### UI Preview Requirements (MANDATORY)

**Compose Multiplatform:**
- Every @Composable MUST have a @Preview
- Preview shows realistic data
- Named `<ComponentName>Preview` and private

**SwiftUI:**
- Every View MUST have a #Preview
- Preview shows realistic data

### Quick Validation

After coding, ask yourself:
1. Does this follow the Impl + Factory pattern?
2. Does the repository return `Either<RepoError, T>`?
3. Does the ViewModel follow lifecycle-aware pattern?
4. Are tests written (Kotest)?
5. Is the Android build green?

**If any answer is NO**: Fix before proceeding.

Consult `.junie/guides/tech/conventions.md` for detailed rules.
