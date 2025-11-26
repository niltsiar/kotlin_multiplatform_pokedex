# Quick Reference Guide

Last Updated: November 26, 2025

> Fast lookup for commands, tables, API references, and common patterns.

## Essential Commands

### Primary Validation (ALWAYS RUN FIRST)
```bash
# Android build + ALL tests (fastest feedback: ~45 seconds)
./gradlew :composeApp:assembleDebug test --continue
```

### Dependency Management
```bash
# Check for available updates
./gradlew dependencyUpdates

# View detailed report
open build/dependencyUpdates/report.html
```

**Stability Rules** (configured in root `build.gradle.kts`):
- ✅ Stable versions stay stable (e.g., `2.8.4` won't upgrade to `2.9.0-alpha01`)
- ✅ Unstable versions upgrade within same major.minor:
  - `2.9.0-alpha01` → `2.9.0-alpha03` ✅ (same major.minor)
  - `2.9.0-alpha01` → `2.9.0-beta01` ✅ (same major.minor)
  - `2.9.0-alpha01` → `2.10.0-alpha01` ❌ (different minor)
- ✅ Unstable versions upgrade to ANY stable version:
  - `2.9.0-alpha02` → `3.1.1` ✅ (stable release)

### Platform-Specific Commands
```bash
# Desktop/JVM
./gradlew :composeApp:run

# Ktor Server (port 8080)
./gradlew :server:run

# Android build
./gradlew :composeApp:assembleDebug

# Unit tests
./gradlew :composeApp:testDebugUnitTest

# Screenshot tests
./gradlew recordRoborazziDebug              # Record baselines
./gradlew verifyRoborazziDebug              # Verify against baselines
./gradlew compareRoborazziDebug             # Compare screenshots
```

### iOS Build Policy ⚠️
**NEVER run iOS builds during routine validation** (5-10min builds).

Only execute when:
1. Explicitly requested by user
2. Testing iOS framework exports
3. Validating iOS-specific expect/actual implementations
4. Working on SwiftUI integration with shared.framework

**iOS Commands** (use sparingly):
```bash
# Build shared framework for iOS
./gradlew :shared:embedAndSignAppleFrameworkForXcode

# Open iOS projects in Xcode
open iosApp/iosApp.xcodeproj                # Native SwiftUI app (production)
open iosAppCompose/iosAppCompose.xcodeproj  # Compose iOS app (experimental)
```

### Gradle Utility Commands
```bash
# Show module structure
./gradlew projects

# Show dependency tree
./gradlew :composeApp:dependencies

# Check for dependency conflicts
./gradlew :composeApp:dependencyInsight --dependency arrow-core

# Refresh dependencies
./gradlew --refresh-dependencies

# Clean build
./gradlew clean
```

## Test Enforcement Matrix

| Production Code | Test Location | Framework | Property Tests Required |
|----------------|---------------|-----------|------------------------|
| Repository | androidUnitTest/ | Kotest + MockK + Turbine | HTTP error ranges, ID preservation |
| ViewModel | androidUnitTest/ | Kotest + MockK + Turbine | State transitions with random data |
| Mapper | androidUnitTest/ | Kotest properties | Data preservation invariants |
| Use Case | androidUnitTest/ | Kotest + MockK | Business rule validation |
| API Service | androidUnitTest/ | Kotest + MockK | HTTP mocking |
| @Composable | Same file | @Preview + Roborazzi | N/A |
| Simple Utility | commonTest/ | kotlin-test | Input/output validation |

**Coverage Targets:**
- Overall: 30-40% of tests should be property-based
- Mappers: 100% property tests
- Repositories: 40-50% property tests
- ViewModels: 30-40% property tests

## Module Structure Reference

### Current Modules
```
:composeApp           → Compose Multiplatform UI (Android + Desktop + iOS)
:shared               → iOS umbrella framework (exports other modules)
:iosApp               → Native SwiftUI iOS app (production)
:iosAppCompose        → Compose Multiplatform iOS app (experimental)
:server               → Ktor Backend-for-Frontend (BFF)
```

### Feature Module Pattern
```
:features:<feature>:api           → Public contracts (exported to iOS)
:features:<feature>:data          → Network + Data layer (NOT exported)
:features:<feature>:presentation  → ViewModels, UI state (exported to iOS)
:features:<feature>:ui            → Compose UI screens (NOT exported)
:features:<feature>:wiring        → DI assembly (NOT exported)
```

### Core Modules (Use Sparingly)
```
:core:designsystem   → Material 3 theme, reusable Compose components
:core:navigation     → Navigation 3 modular architecture
:core:di             → Koin DI core module
:core:httpclient     → Ktor HttpClient configuration
:core:util           → Generic utilities (3+ features use it)
```

## iOS Export Rules

**Exported to iOS** (via `:shared` umbrella):
- ✅ `:features:*:api` — Public contracts
- ✅ `:features:*:presentation` — ViewModels, UI state
- ✅ `:core:*` modules — Shared infrastructure

**NOT Exported to iOS**:
- ❌ `:features:*:data` — Internal data layer
- ❌ `:features:*:ui` — Compose UI (Android/Desktop/iOS Compose only)
- ❌ `:features:*:wiring` — DI assembly
- ❌ `:composeApp` — Compose UI framework

**Note:** Native SwiftUI iosApp doesn't use Compose UI. iosAppCompose (experimental) uses Compose UI from :composeApp.

## Turbine API Quick Reference

| Method | Use Case | Example |
|--------|----------|---------|
| `awaitItem()` | Get next emission (fails if none) | `val item = awaitItem()` |
| `skipItems(n)` | Skip n emissions | `skipItems(2)` |
| `expectNoEvents()` | Assert no emissions occurred | `expectNoEvents()` |
| `cancelAndIgnoreRemainingEvents()` | Clean teardown | Always call at end |
| `.test { }` | Turbine test block for flows | `flow.test { /* assertions */ }` |

**Pattern:**
```kotlin
viewModel.uiState.test {
    awaitItem() shouldBe Loading
    viewModel.start(mockk(relaxed = true))
    testScope.advanceUntilIdle()
    awaitItem().shouldBeInstanceOf<Content>()
    cancelAndIgnoreRemainingEvents()
}
```

## Common Violations & Fixes

| Violation | Correct Pattern | See |
|-----------|----------------|-----|
| `class XImpl : X` (public) | `internal class XImpl : X` | `patterns/di_patterns.md` |
| Missing factory function | `fun X(...): X = XImpl(...)` | `patterns/di_patterns.md` |
| `suspend fun get(): T?` | `suspend fun get(): Either<RepoError, T>` | `patterns/error_handling_patterns.md` |
| `private val scope = ...` | `viewModelScope: CoroutineScope` param | `patterns/viewmodel_patterns.md` |
| `init { loadData() }` | `fun start(lifecycle: Lifecycle) { ... }` | `patterns/viewmodel_patterns.md` |
| `_state: MutableStateFlow<List<T>>` | `_state: MutableStateFlow<ImmutableList<T>>` | `patterns/viewmodel_patterns.md` |
| Empty use case | Call repository directly from ViewModel | `patterns/architecture_patterns.md` |
| `:data`, `:ui` exported to iOS | Only `:api`, `:presentation`, `:core:*` | `patterns/architecture_patterns.md` |
| @Composable without @Preview | Add `@Preview` with realistic data | `patterns/testing_patterns.md` |
| Manual cast after `shouldBeInstanceOf` | Use smart cast directly | `tech/kotest_smart_casting_quick_ref.md` |
| Thread.sleep() in tests | Use Turbine + testScope | `patterns/testing_patterns.md` |

## Critical DON'Ts (Top 10)

1. ❌ **NEVER run iOS builds** unless explicitly required (5-10min builds)
2. ❌ **NEVER store `CoroutineScope` as field** in ViewModels (pass to constructor)
3. ❌ **NEVER perform work in `init`** blocks in ViewModels (use lifecycle callbacks)
4. ❌ **NEVER return `Result` or nullable** from repositories (use `Either<RepoError, T>`)
5. ❌ **NEVER swallow `CancellationException`** (use `Either.catch` which handles it)
6. ❌ **NEVER create empty pass-through** use cases (call repos directly)
7. ❌ **NEVER export `:data`, `:ui`, or `:wiring`** to iOS (only `:api`, `:presentation`, `:core:*`)
8. ❌ **NEVER put business logic in `:shared`** itself (it's an umbrella; logic goes in feature/core modules)
9. ❌ **NEVER add DI annotations** to production classes (wire in wiring modules)
10. ❌ **NEVER omit @Preview** for @Composable functions (MANDATORY)

## Decision Matrices

### When to Create a New Module?
```
IF defining cross-feature contracts → :features:<name>:api (export to iOS)
IF implementing data layer         → :features:<name>:data (do NOT export)
IF implementing ViewModels         → :features:<name>:presentation (export to iOS)
IF implementing Compose UI         → :features:<name>:ui (do NOT export)
IF wiring dependencies             → :features:<name>:wiring (do NOT export)
IF shared utilities (3+ features)  → :core:util (export to iOS)
IF common domain models            → :core:domain (export to iOS)
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
IF platform-specific API access    → Use expect/actual in feature/core modules
IF platform-specific UI:
  - Android/Desktop               → Use Compose source sets (androidMain, jvmMain)
  - iOS Production                → Use SwiftUI in :iosApp (separate from Compose)
  - iOS Experimental              → Use Compose in :iosAppCompose (shares UI)
IF shared business logic           → Use commonMain in feature/core modules
IF simple constants                → Use commonMain in appropriate module
```

### When to Remove Redundant Tests?
```
1. Does a property test cover this scenario?        → Remove concrete test
2. Is this an edge case not covered by properties?  → Keep concrete test
3. Does this test document important behavior?      → Keep but add comment
4. Is this test redundant with another test?        → Merge or remove
```

## Version Catalog Reference

Add dependencies to `gradle/libs.versions.toml`:

```toml
[versions]
arrow = "1.2.0"
koin = "4.0.0"

[libraries]
arrow-core = { module = "io.arrow-kt:arrow-core", version.ref = "arrow" }
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }

[plugins]
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
```

Use in `build.gradle.kts`:
```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.arrow.core)
            implementation(libs.koin.core)
        }
    }
}
```

## Minimum Test Coverage (Per File)

**Repositories:**
- ✅ Success path (returns Right)
- ✅ Error paths (Network, Http, Unknown)
- ✅ All error types tested
- ✅ Property tests for HTTP code ranges

**ViewModels:**
- ✅ Initial state
- ✅ Loading → Success flow
- ✅ Loading → Error flow
- ✅ Event handling
- ✅ Property tests for state transitions

**Mappers:**
- ✅ Property-based tests (data preservation)
- ✅ Edge cases (empty, null, boundaries)
- ✅ Round-trip tests

**@Composable:**
- ✅ At least one @Preview with realistic data
- ✅ Recommended: Multiple previews for different states (loading, error, content)

## See Also

**Pattern Files:**
- `patterns/architecture_patterns.md` — Split-by-layer, modules, convention plugins
- `patterns/di_patterns.md` — Koin DI, Impl+Factory, wiring
- `patterns/error_handling_patterns.md` — Either boundaries, sealed errors
- `patterns/viewmodel_patterns.md` — Lifecycle-aware, parametric, pagination
- `patterns/navigation_patterns.md` — Navigation 3, route objects, animations
- `patterns/testing_patterns.md` — Kotest+MockK, property tests, Turbine

**Tech Guides:**
- `tech/conventions.md` — Master reference (start here)
- `tech/dependency_injection.md` — Comprehensive Koin guide
- `tech/repository.md` — Repository patterns, Either, error handling
- `tech/presentation_layer.md` — ViewModel lifecycle, UI state
- `tech/navigation.md` — Navigation 3 complete guide
- `tech/testing_strategy.md` — Testing enforcement, property tests
- `tech/ios_integration.md` — iOS SwiftUI + KMP patterns

**Project Guides:**
- `project/prd.md` — Product requirements (PRIMARY REFERENCE)
- `project/user_flow.md` — User journeys and flows
- `project/ui_ux.md` — UI/UX guidelines

**Main Documentation:**
- `AGENTS.md` — Autonomous agent workflows
- `.github/copilot-instructions.md` — GitHub Copilot context
- `.junie/guidelines.md` — Junie AI assistant reference
