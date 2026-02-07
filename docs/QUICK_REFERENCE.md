# Quick Reference Guide

Last Updated: February 7, 2026

> Fast lookup for commands, tables, API references, and common patterns.

## 🎨 Phase 2 Redesign Status

**Completed (Steps 1-8):**
- ✅ Step 1: NavigationProvider naming + Core token foundation
- ✅ Step 2: Theme token systems with delegation  
- ✅ Step 3: Google Sans Flex typography
- ✅ Step 4: Shared component abstraction layer
- ✅ Step 5: Motion preference + predictive back
- ✅ Step 6: Material screens redesign (8 components)
- ✅ Step 7: Unstyled screens redesign (8 components + navigation + hover fixes)
- ✅ Step 8: SwiftUI design system with theme tokens

**Next: Step 9** - Comprehensive unit tests

**Documentation:**
- Implementation references: [CODE_REFERENCES.md](CODE_REFERENCES.md)
- Troubleshooting: [TROUBLESHOOTING.md](TROUBLESHOOTING.md)

## Agent Selector (Skill Routing)

Use these specialized skills for task routing (located in `.claude/skills/`). Load skills with `@skill-name` when needed.

### Architecture & Core
| Skill | Use When |
|-------|----------|
| **@kmp-architecture** | Module structure, vertical slice organization, feature boundaries |
| **@kmp-critical-patterns** | Quick reference for 6 core patterns (ViewModel, Either, Navigation, Testing) |

### Layer Implementation
| Skill | Use When |
|-------|----------|
| **@kmp-presentation** | ViewModels, UI state management, SavedStateHandle |
| **@kmp-data-layer** | Repository patterns with Either<RepoError,T>, error handling |
| **@kmp-domain** | Domain models, immutable data classes, use cases |
| **@kmp-api-services** | Ktor Client patterns, DTOs, API service boundaries |
| **@kmp-di** | Koin dependency injection, parametric injection |

### Platform & UI
| Skill | Use When |
|-------|----------|
| **@kmp-ios** | SwiftUI + KMP ViewModels integration, lifecycle bridging |
| **@swiftui-screen** | Building native iOS UI with SwiftUI |
| **@compose-screen** | Building Compose UI screens (Android, Desktop, Material + Unstyled) |
| **@kmp-navigation** | Navigation 3 modular architecture, scoped routes |
| **@kmp-desktop** | Desktop (JVM) patterns, SavedStateHandle on Desktop |

### Design & Testing
| Skill | Use When |
|-------|----------|
| **@kmp-design-systems** | Design tokens, Material 3, icon strategy |
| **@kmp-compose-unstyled** | Headless component patterns for Unstyled screens |
| **@ui-ux-designer** | Visual design, animations, interaction patterns |
| **@kmp-testing-strategy** | Testing philosophy, coverage analysis, test planning |
| **@kmp-testing-patterns** | Kotest, MockK, Turbine, property-based tests |

### Build & Commands
| Skill | Use When |
|-------|----------|
| **@kmp-gradle** | Gradle convention plugins, module creation |
| **@kmp-commands** | CLI reference card, validation commands |

### Development & Quality
| Skill | Use When |
|-------|----------|
| **@kmp-developer** | General Kotlin Multiplatform development |
| **@kmp-mobile-expert** | Shared business logic with ViewModels, repositories |
| **@ktor-backend** | Ktor server endpoints, BFF APIs, backend services |
| **@product-designer** | Writing PRD, defining acceptance criteria |
| **@user-flows** | Mapping user journeys, navigation contracts |
| **@onboarding** | Designing first-run experience, welcome screens |
| **@docs-maintainer** | Documentation maintenance, link validation |

**For complete decision trees and detailed routing, see [AGENTS.md](../AGENTS.md).**

## Essential Commands

**For complete CLI reference, see [@kmp-commands skill](.claude/skills/kmp-commands/SKILL.md).**

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

### Commits & Changelog
```bash
# Commit with Conventional Commits format (required)
git commit -m "type(scope): description"

# Types: feat, fix, docs, test, build, refactor, chore
# Examples:
git commit -m "feat(pokemonlist): add search functionality"
git commit -m "refactor(navigation): align package with folder structure"
git commit -m "docs(conventions): update testing requirements"

# Regenerate CHANGELOG.md from commits (using git-cliff)
git cliff -o CHANGELOG.md

# Preview changelog without writing
git cliff --dry-run
```

**CHANGELOG Policy:**
- ❌ **DO NOT manually edit CHANGELOG.md** — it's auto-generated by git-cliff
- ✅ Use proper Conventional Commits format — git-cliff parses these automatically
- ✅ Regenerate changelog before releases: `git cliff -o CHANGELOG.md`
- ✅ Commit types map to changelog sections:
  - `feat` → ✨ Features
  - `fix` → 🐛 Bug Fixes
  - `docs` → 📝 Documentation
  - `test` → ✅ Tests
  - `build` → 🔧 Build System
  - `refactor` → ♻️ Refactoring
  - `chore` → 🧹 Chores

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

## SavedStateHandle Quick Reference

**Purpose:** Persist ViewModel state across configuration changes and process death.

**Platform Support:**
- ✅ Android: Full native support
- ✅ Desktop/JVM: Manual creation
- ⚠️ iOS: App state restoration (different lifecycle than Android)

**Current Pattern (Delegate - SavedState 1.4.0+):**
```kotlin
import androidx.lifecycle.serialization.saved

class MyViewModel(
    private val repository: MyRepository,
    private val savedStateHandle: SavedStateHandle,  // ← Always inject
    viewModelScope: CoroutineScope = ...
) : ViewModel(viewModelScope), DefaultLifecycleObserver {
    
    // ✨ Single line - automatic persistence
    private var state by savedStateHandle.saved { MyState() }
    
    fun updateData() {
        state = state.copy(...)  // ← Automatically persisted on write!
        // No persistState() call needed
    }
}
```

**Why This Works:**
- ✅ **93% code reduction** - Eliminates ~28 lines of boilerplate per ViewModel
- ✅ **Automatic persistence** - State saved on every property write
- ✅ **Type-safe** - Uses kotlinx.serialization internally
- ✅ **KMP compatible** - Works in `commonMain` source set
- ✅ **No manual calls** - No `persistState()` functions or key management

**Critical: Use Correct Import!**
```kotlin
// ✅ CORRECT - For SavedStateHandle delegate
import androidx.lifecycle.serialization.saved

// ❌ WRONG - For SavedStateRegistryOwner (different use case)
import androidx.savedstate.serialization.saved
```

**Requirements:**
- AndroidX SavedState 1.4.0+
- AndroidX Lifecycle 2.10.0-alpha07+
- State type must be `@Serializable`

**Legacy Pattern (Manual - Pre-1.4.0):**
```kotlin
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class MyViewModel(
    private val repository: MyRepository,
    private val savedStateHandle: SavedStateHandle,
    viewModelScope: CoroutineScope = ...
) : ViewModel(viewModelScope), DefaultLifecycleObserver {
    
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    
    // Manual restoration from SavedStateHandle
    private var state: MyState = savedStateHandle
        .get<String>(KEY_STATE)
        ?.let { json.decodeFromString(it) }
        ?: MyState()
    
    private fun persistState() {
        savedStateHandle[KEY_STATE] = json.encodeToString(state)
    }
    
    fun updateData() {
        state = state.copy(...)
        persistState()  // ← Must call manually after every mutation
    }
    
    companion object {
        private const val KEY_STATE = "state"
    }
}
```

**Koin Wiring:**
```kotlin
// Android (auto-provides)
viewModel { PokemonListViewModel(get(), SavedStateHandle()) }

// Desktop/JVM (manual creation)
viewModel { PokemonListViewModel(get(), SavedStateHandle()) }
```

**Testing:**
```kotlin
beforeTest {
    viewModel = MyViewModel(
        repository = mockRepository,
        savedStateHandle = SavedStateHandle(),  // ← Always provide
        viewModelScope = testScope
    )
}
```

**Reference Implementations:**
- [PokemonListViewModel.kt](../features/pokemonlist/presentation/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/presentation/PokemonListViewModel.kt) (delegate pattern)
- [PokemonDetailViewModel.kt](../features/pokemondetail/presentation/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/presentation/PokemonDetailViewModel.kt) (delegate pattern)
- [DI Patterns Guide](patterns/di_patterns.md#savedstatehandle-in-viewmodels)

## Library Resources Quick Reference

**Problem:** Compose resources in library modules (like `:core:designsystem-core`) are NOT automatically accessible.

**Solution (3 steps in library module's build.gradle.kts):**

```kotlin
// 1. Add dependency
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.components.resources)  // CRITICAL!
        }
    }
}

// 2. Enable public Res class
compose.resources {
    publicResClass = true  // Default internal won't work!
}

// 3. Android namespace determines package
android {
    namespace = "com.minddistrict.multiplatformpoc.core.designsystem.core"
}
```

**Generated package name:** Namespace with dots → underscores:
- Input: `com.minddistrict.multiplatformpoc.core.designsystem.core`
- Output: `multiplatformpoc.core.designsystem_core.generated.resources`

**Usage:**
```kotlin
import multiplatformpoc.core.designsystem_core.generated.resources.Res
import multiplatformpoc.core.designsystem_core.generated.resources.ic_icon_name
import org.jetbrains.compose.resources.painterResource

Icon(
    painter = painterResource(Res.drawable.ic_icon_name),
    contentDescription = "Description",
    tint = MaterialTheme.colorScheme.onSurface
)
```

**See:** [material_icons_strategy.md](tech/material_icons_strategy.md) for complete guide

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
:features:<feature>:api                  → Public contracts (exported to iOS)
:features:<feature>:data                 → Network + Data layer (NOT exported)
:features:<feature>:presentation         → ViewModels, UI state (exported to iOS)
:features:<feature>:ui-material          → Material Design 3 UI (NOT exported)
:features:<feature>:ui-unstyled          → Compose Unstyled UI (NOT exported)
:features:<feature>:wiring               → Business logic DI (exported to iOS)
:features:<feature>:wiring-ui-material   → Material navigation (NOT exported)
:features:<feature>:wiring-ui-unstyled   → Unstyled navigation (NOT exported)
```

### Core Modules (Use Sparingly)
```
:core:designsystem-core       → Shared theme utilities
:core:designsystem-material   → Material 3 theme + MaterialScope
:core:designsystem-unstyled   → Unstyled theme + UnstyledScope
:core:navigation              → Navigation 3 modular architecture
:core:di                      → Koin DI core module
:core:di-ui                   → Koin UI utilities
:core:httpclient              → Ktor HttpClient configuration
```

### Creating a New Feature with Material/Unstyled Variants

**Step 1: Create module structure**
```bash
mkdir -p features/<feature>/{api,data,presentation,ui-material,ui-unstyled,wiring,wiring-ui-material,wiring-ui-unstyled}/src/commonMain/kotlin
```

**Step 2: Define navigation route in :api**
```kotlin
// :features:<feature>:api/navigation/<Feature>Entry.kt
package com.minddistrict.multiplatformpoc.features.<feature>.navigation

data class <Feature>Detail(val id: Int)  // Parametric route
object <Feature>List                      // Simple route
```

**Step 3: Implement UI variants**
```kotlin
// :features:<feature>:ui-material
@Composable
fun <Feature>Screen(...) {
    // Material Design 3 implementation
}

// :features:<feature>:ui-unstyled
@Composable
fun <Feature>ScreenUnstyled(...) {
    UnstyledTheme {  // Always wrap in UnstyledTheme
        // Compose Unstyled implementation
    }
}
```

**Step 4: Create scoped navigation wiring**
```kotlin
// :features:<feature>:wiring-ui-material/build.gradle.kts
commonMain.dependencies {
    implementation(projects.core.designsystemMaterial)  // For MaterialScope
    implementation(projects.features.<feature>.uiMaterial)
}

// Navigation provider
import com.minddistrict.multiplatformpoc.core.designsystem.material.MaterialScope

val <feature>NavigationModule = module {
    scope<MaterialScope> {
        navigation<<Feature>List> { route ->
            <Feature>Screen(...)
        }
    }
}

// :features:<feature>:wiring-ui-unstyled - similar but with UnstyledScope
```

**Step 5: Register in App.kt**
```kotlin
KoinApplication(
    configuration = koinConfiguration {
        modules(
            coreModule +
            <feature>Module +                        // Business logic
            <feature>NavigationModule +              // Material UI
            <feature>NavigationUnstyledModule +      // Unstyled UI
            ...
        )
    }
)
```

**Critical Rules for Multi-Theme Features**:
- ✅ Scope markers (`MaterialScope`, `UnstyledScope`) MUST come from design system modules
- ✅ Use `scope<MaterialScope>` in wiring-ui-material modules
- ✅ Use `scope<UnstyledScope>` in wiring-ui-unstyled modules
- ✅ Always wrap Unstyled screens in `UnstyledTheme { }`
- ✅ Load both navigation modules simultaneously in App
- ❌ Never create scope markers in feature modules (causes circular dependencies)
- ❌ Never manually create/manage Koin scopes in composables

### Core Modules (Use Sparingly)
```
:core:designsystem-core       → Shared theme utilities
:core:designsystem-material   → Material 3 theme + MaterialScope
:core:designsystem-unstyled   → Unstyled theme + UnstyledScope
:core:navigation              → Navigation 3 modular architecture
:core:di                      → Koin DI core module
:core:di-ui                   → Koin UI utilities
:core:httpclient              → Ktor HttpClient configuration
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
    viewModel.loadInitialPage()  // Call public methods directly
    testScope.advanceUntilIdle()
    awaitItem().shouldBeInstanceOf<Content>()
    cancelAndIgnoreRemainingEvents()
}

// Separate test for lifecycle integration
"onStart should trigger loading" {
    val viewModel = createViewModel()
    val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.STARTED)
    
    viewModel.onStart(lifecycleOwner)
    testScope.advanceUntilIdle()
    
    viewModel.uiState.value shouldBeInstanceOf<Content>()
}
```

## Common Violations & Fixes

| Violation | Correct Pattern | See |
|-----------|----------------|-----|
| `class XImpl : X` (public) | `internal class XImpl : X` | `patterns/di_patterns.md` |
| Missing factory function | `fun X(...): X = XImpl(...)` | `patterns/di_patterns.md` |
| `suspend fun get(): T?` | `suspend fun get(): Either<RepoError, T>` | `patterns/error_handling_patterns.md` |
| `private val scope = ...` | `viewModelScope: CoroutineScope` param | `patterns/viewmodel_patterns.md` |
| `init { loadData() }` | `override fun onStart(owner: LifecycleOwner) { ... }` | `patterns/viewmodel_patterns.md` |
| `_state: MutableStateFlow<List<T>>` | `_state: MutableStateFlow<ImmutableList<T>>` | `patterns/viewmodel_patterns.md` |
| `androidx.compose.ui.text.TextStyle(...)` | Import `TextStyle`, use `TextStyle(...)` | `tech/conventions.md` |
| `kotlinx.collections.immutable.persistentListOf(...)` | Import `persistentListOf`, use `persistentListOf(...)` | `tech/conventions.md` |
| `val x: com.example.MyClass` | Import `MyClass`, use `val x: MyClass` | `tech/conventions.md` |
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
10. ❌ **NEVER use star imports or FQN in code**:
    - ❌ `import com.example.*` — Use explicit imports
    - ❌ `androidx.compose.ui.text.TextStyle(...)` — Import `TextStyle` first
    - ❌ `kotlinx.collections.immutable.persistentListOf(...)` — Import `persistentListOf` first
    - ❌ `val x: com.example.MyClass` — Import `MyClass` first
    - ✅ Use: `import x.y.ClassName` then write `ClassName()`
11. ❌ **NEVER omit @Preview** for @Composable functions (MANDATORY)

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
- `tech/material_icons_strategy.md` — Vector Drawable XML icons, Rounded Filled style

**Project Guides:**
- `project/prd.md` — Product requirements (PRIMARY REFERENCE)
- `project/user_flow.md` — User journeys and flows
- `project/ui_ux.md` — UI/UX guidelines

**Main Documentation:**
- `AGENTS.md` — Agent routing and skill selection
- `llms.txt` — AI discovery index
- `docs/README.md` — Complete documentation index
