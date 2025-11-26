# Project Guidelines — Kotlin Multiplatform + Compose Multiplatform

This repository is a Kotlin Multiplatform project using:
- **Compose Multiplatform** for Android + Desktop (JVM) UI
- **Native SwiftUI** for iOS UI (consuming shared Kotlin business logic)
- **Ktor** for Backend-for-Frontend (BFF) server

> **Related Documentation**: See also [`.github/copilot-instructions.md`](../.github/copilot-instructions.md) for GitHub Copilot-specific guidance and [`AGENTS.md`](../AGENTS.md) for autonomous agent workflows. These documents are kept in sync.

## Project Overview
- **Languages/Tech**: Kotlin, Compose Multiplatform, SwiftUI, Gradle (Kotlin DSL), Koin DI, Arrow (Either), Kotest, MockK, Roborazzi, Navigation 3
- **Targets**:
  - **Android app**: Compose Multiplatform UI in `:composeApp`
  - **Desktop (JVM) app**: Compose Multiplatform UI in `:composeApp`
  - **iOS app (production)**: Native SwiftUI in `:iosApp` consuming `:shared` framework (business logic only)
  - **iOS app (experimental)**: Compose Multiplatform UI in `:iosAppCompose` sharing Compose code with Android/Desktop
  - **Server**: Ktor-based BFF in `:server`
- **Module Structure**:
  - `:composeApp` — Compose Multiplatform UI for Android + Desktop + iOS (commonMain for shared UI, androidMain/jvmMain/iosMain for platform-specific)
  - `:shared` — iOS umbrella framework that exports other KMP modules (minimal code, mostly Gradle config)
  - `:iosApp` — Native SwiftUI iOS app (production) that imports shared.framework to access KMP modules
  - `:iosAppCompose` — Experimental Compose Multiplatform iOS app that uses Compose UI from `:composeApp`
  - `:server` — Ktor Backend-for-Frontend providing REST APIs for all clients
  - `:build-logic/convention` — Custom Gradle convention plugins
  - **Required feature structure** (split-by-layer for all features):
    - `:features:<feature>:api` — public contracts (interfaces, navigation, shared domain models) - exported to iOS via :shared umbrella
    - `:features:<feature>:data` — network layer (API services, DTOs), data layer (repositories, mappers) - KMP all targets, NOT exported to iOS
    - `:features:<feature>:presentation` — ViewModels, UI state - **KMP all targets, shared with iOS**, exported to iOS via :shared umbrella
    - `:features:<feature>:ui` — Compose UI (@Composable screens) - **Android + JVM only**, NOT exported to iOS
    - `:features:<feature>:wiring` — DI assembly (Koin) - KMP with platform-specific source sets, NOT exported to iOS
    - `:features:<feature>:domain` (optional) — Use cases, validators - only if orchestrating 2+ repositories
  - **Optional shared infrastructure** (use sparingly):
    - `:core:designsystem` — reusable Compose components (Android/Desktop only, NOT exported)
    - `:core:util` — generic utilities used by 3+ features (exported to iOS via :shared)
    - `:core:httpclient` — ONLY HttpClient instance configuration (NOT generic API services)

## Architecture Requirements

**Core Principle**: True vertical slicing - each feature is a complete vertical slice owning ALL its layers end-to-end.

### Vertical Slicing Defined

Each feature contains ALL layers it needs internally:
- Domain models specific to that feature
- Network/API services for that feature's endpoints  
- Data/Repository layer for that feature
- Presentation/UI for that feature's screens
- Navigation contracts for that feature

### Required Module Structure

```
:features:<feature>:api          → Public contracts (interfaces, domain models, navigation)
:features:<feature>:data         → Network + Data layer (API services, repos, DTOs, mappers)
:features:<feature>:presentation → ViewModels, UI state (shared with iOS)
:features:<feature>:ui           → Compose UI (Android + JVM only)
:features:<feature>:wiring       → DI assembly (Koin)
```

**Example:**
```
:features:pokemonlist:data/
  ├── PokemonListApiService.kt      (feature's network layer)
  ├── dto/                        (feature's DTOs)
  └── PokemonListRepositoryImpl.kt

:features:pokemonlist:presentation/
  ├── PokemonListViewModel.kt      (shared across all platforms)
  └── PokemonListUiState.kt        (immutable collections)

:features:pokemonlist:ui/
  └── PokemonListScreen.kt         (Compose UI - Android/JVM only)
```

### Core Modules (Use Sparingly)

**ONLY create :core modules for:**
- ✅ Design system (reusable UI components)
- ✅ Generic utilities used by 3+ features
- ✅ Cross-cutting domain types (User, Error)
- ✅ Platform abstractions (expect/actual)

**DO NOT create :core modules for:**
- ❌ Generic network layer (each feature has its own)
- ❌ Generic repository patterns (each feature implements its own)
- ❌ Generic API services (each feature defines its own)

### Feature Independence

- Features MUST NOT depend on other features' `:data` or `:presentation` modules
- Each feature owns its network layer (API service, DTOs, mappers)
- Each feature defines its own DTOs (even for same backend endpoint)
- Domain models in :api only if shared across features

**Clean Architecture with vertical slices** — each feature must own its code end-to-end
- **Vertical-slice feature modules** must follow api/data/presentation/ui/wiring pattern (see `.junie/guides/tech/conventions.md`)
- **Required patterns**:
  - Impl + Factory: interfaces must be implemented by internal `*Impl` classes, exposed via public factory functions
  - Only `api` modules exposed cross-feature; `data`, `presentation`, `ui`, and `wiring` must remain internal
  - Koin DI: production classes must stay DI-agnostic; wire via Koin `module { }` DSL in wiring modules
  - Arrow Either: repositories must return `Either<RepoError, T>` at boundaries

## Directory Structure
- `composeApp/` — **Compose Multiplatform UI (Android + Desktop ONLY)**
  - `src/commonMain` — Shared Compose UI logic
  - `src/androidMain` — Android-specific UI code and resources
  - `src/jvmMain` — Desktop (JVM) specific UI code
  - `src/commonTest` — Shared UI tests (place Compose tests in `screentest` package)
- `shared/` — **iOS umbrella framework (exports other KMP modules)**
  - `build.gradle.kts` — Configures which modules to export to iOS
  - Exports: `:features:*:api`, `:core:*` modules only
  - Does NOT contain business logic itself (that lives in feature/core modules)
- `features/` — **Feature modules (when created)**
  - `:features:<name>:api` — Public contracts (exported to iOS)
  - `:features:<name>:data` — Network + Data layer (NOT exported)
  - `:features:<name>:presentation` — ViewModels, UI state (exported to iOS)
  - `:features:<name>:ui` — Compose UI (NOT exported)
  - `:features:<name>:wiring` — DI assembly (NOT exported)
  - Business logic, domain models, repositories live here
- `iosApp/` — **Native SwiftUI iOS app (production)**
  - SwiftUI views and iOS-specific UI code
  - Imports `shared.framework` to access KMP modules
- `iosAppCompose/` — **Experimental Compose Multiplatform iOS app**
  - Uses Compose UI shared with Android/Desktop from `:composeApp`
  - Wraps MainViewController from ComposeApp framework
  - Alternative to native SwiftUI approach
- `server/` — **Ktor Backend-for-Frontend**
  - REST API endpoints for all clients
  - Port 8080 (configurable in project constants)

## Prerequisites
- JDK 17+
- Android SDK path configured in local.properties (sdk.dir=/path/to/sdk)
- For iOS: Xcode installed and, optionally, KMM plugin for Android Studio
- Optional: Run KDoctor to verify environment

## Build and Run
### Android
- **Primary validation** (fastest feedback): `./gradlew :composeApp:assembleDebug`
- APK output: `composeApp/build/outputs/apk/debug/composeApp-debug.apk`
- Run from IDE: Open in Android Studio and run the Android configuration
- Useful tasks:
  - SHA1 (Firebase, etc.): `./gradlew :composeApp:signingReport`

### iOS
- **⚠️ CRITICAL**: iOS builds are extremely slow (5-10 minutes). NEVER run iOS builds during routine validation.
- Only execute iOS builds when:
  1. Explicitly requested by user
  2. Testing iOS framework exports (verifying which modules are exposed)
  3. Validating iOS-specific expect/actual implementations in KMP modules
  4. Working on SwiftUI integration with shared.framework
- **Note**: iOS uses native SwiftUI for UI (not Compose). The `:shared` module is an umbrella that exports KMP modules.
- **Integration Pattern**: Direct Integration (private var ViewModel + @State for UI state) is current. See `.junie/guides/tech/ios_integration.md` for complete guide.
- Entry point: `iosApp/iosApp.xcodeproj` (Xcode only)

### iOS Compose (Experimental)
- **Alternative approach**: Uses Compose Multiplatform UI instead of native SwiftUI
- **Shares UI code**: Same Compose screens as Android/Desktop from `:composeApp`
- **Build**: `./gradlew :composeApp:embedAndSignAppleFrameworkForXcode` (5-10 min)
- **Entry point**: `iosAppCompose/iosAppCompose.xcodeproj` (Xcode only)
- **Use case**: When you want 100% UI code sharing across all platforms
- **Trade-off**: Less native iOS feel, experimental Compose iOS support
- **See**: `iosAppCompose/README.md` for detailed setup and comparison with native SwiftUI approach

### Desktop (JVM)
- Run: `./gradlew :composeApp:run` or use IDE run configuration
- Desktop-specific code lives in `composeApp/src/jvmMain`

### Server
- Ktor-based backend application
- Run: `./gradlew :server:run` or use IDE run configuration

## Testing

**MANDATORY**: All production code MUST have tests. See `.junie/guides/tech/testing_strategy.md` for complete enforcement rules.

### Test Enforcement Summary

**Core Rule**: NO CODE WITHOUT TESTS

| Production Code | Test Required | Test Location |
|----------------|---------------|---------------|
| Repository | ✅ MANDATORY | androidUnitTest/ |
| ViewModel | ✅ MANDATORY | androidUnitTest/ |
| Mapper | ✅ MANDATORY | androidUnitTest/ |
| Use Case | ✅ MANDATORY | androidUnitTest/ |
| API Service | ✅ MANDATORY | androidUnitTest/ |
| @Composable | ✅ MANDATORY | @Preview + Screenshot |
| Simple Utility | ✅ MANDATORY | commonTest/ |

### Test Frameworks
- **Framework**: Kotest (assertions, property-based testing) - androidUnitTest/ only
- **Mocking**: MockK (JVM-based, runs via Robolectric) - androidUnitTest/ only
- **Screenshot**: Roborazzi (Robolectric-based, JVM tests)
- **Location**: 
  - **PRIMARY**: `androidUnitTest/` for ALL business logic (repositories, ViewModels, mappers, use cases)
  - **MINIMAL**: `commonTest/` for simple utilities with NO dependencies
  - **RARE**: `iosTest/` for platform-specific expect/actual implementations

### Test Commands
- **Shared unit tests**: `./gradlew :composeApp:testDebugUnitTest` (or relevant target-specific tasks)
- **Android UI tests** on device (if any under `composeApp/src/commonTest/screentest`): `./gradlew :composeApp:connectedDebugAndroidTest`
- **Screenshot tests** (Roborazzi): 
  - Record baselines: `./gradlew recordRoborazziDebug`
  - Verify against baselines: `./gradlew verifyRoborazziDebug`
- **iOS tests/builds**: Do NOT run by default. Only execute iOS-specific tests/builds if explicitly required or requested.

### Minimum Test Coverage (Per File)

**Repositories:**
- ✅ Success path (returns Right)
- ✅ Error paths (Network, Http, Unknown)
- ✅ All error types tested

**ViewModels:**
- ✅ Initial state
- ✅ Loading → Success flow
- ✅ Loading → Error flow
- ✅ Event handling

**Mappers:**
- ✅ Property-based tests (data preservation)
- ✅ Edge cases (empty, null, boundaries)

**@Composable:**
- ✅ At least one @Preview with realistic data
- ✅ Recommended: Multiple previews for different states
- See `.junie/guides/tech/testing_strategy.md` for comprehensive testing guidelines

## How to Validate Changes
1. **Primary validation command** (must run first): `./gradlew :composeApp:assembleDebug`
2. **Run relevant tests for changed modules**:
   - Unit tests: `./gradlew :composeApp:testDebugUnitTest`
   - Screenshot tests: `./gradlew verifyRoborazziDebug`
   - Android UI tests (device required): `./gradlew :composeApp:connectedDebugAndroidTest`
3. **iOS policy**: NEVER run iOS builds during routine validation (5-10 min builds). Only execute iOS-specific tasks when explicitly required or requested.

## Key Technical Decisions

See `.junie/guides/tech/conventions.md` for comprehensive conventions. Critical patterns:

- **ViewModels**: Must extend `androidx.lifecycle.ViewModel`; pass `viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)` as constructor parameter to superclass; NEVER store `CoroutineScope` as field; do NOT perform work in `init`; load data in lifecycle-aware callbacks (not `init`); use `kotlinx.collections.immutable` types in UI state; implement `OneTimeEventEmitter<E>` via delegation for one-time events
- **Repositories**: Return `Either<RepoError, T>`; use `Either.catch { ... }.mapLeft { it.toRepoError() }`; map DTOs to domain at boundary
- **Navigation**: Use Navigation 3 (`org.jetbrains.androidx.navigation3:navigation3-ui`); define contracts in `api`, route objects in feature modules
- **DI**: Koin with no annotations on classes; wire via `module { }` DSL in wiring modules
- **No empty use cases**: Call repositories directly from presentation when no orchestration needed
- **Immutable UI state**: Use `kotlinx.collections.immutable` types (`ImmutableList`, `ImmutableMap`)
- **Impl + Factory pattern**: Interfaces implemented by internal `*Impl` classes, exposed via public factory functions
- **Code organization**: Keep common code in `commonMain`; isolate platform code in platform-specific source sets
- **Design system**: Create `:core:designsystem` module for reusable Compose components, theming, design tokens

## Troubleshooting
- **First run**: May download Compose and JetBrains JDK; builds can take longer
- **Android build failures**: Verify `local.properties` contains `sdk.dir` and JDK 17+ is used
- **iOS builds**: Extremely slow (5-10 min); NEVER run iOS build/app during routine checks unless explicitly required
- **Koin DI issues**: See [koin_di_quick_ref.md](.junie/guides/tech/koin_di_quick_ref.md) for troubleshooting
- **Arrow Either**: Never catch and swallow `CancellationException`; use `Either.catch` which respects cancellation

## Product Requirements (PRD & User Flows & UI/UX)
When implementing product features, behavior, UX, or addressing acceptance criteria, you must consult the project's product documentation located at `.junie/guides/project` folder:
- **PRIMARY REFERENCE - Always consult first:**
  - `.junie/guides/project/prd.md` — **canonical product requirements, scope, constraints, and acceptance criteria. This is the definitive source for implementation decisions.**
- Supporting documentation:
  - `.junie/guides/project/user_flow.md` — end-to-end user journeys and step-by-step flow details
  - `.junie/guides/project/onboarding.md` — onboarding-specific flows and content
  - `.junie/guides/project/ui_ux.md` — UI/UX guidelines, animations
  - `.junie/guides/project/easter_eggs_and_mini_games_for_clipugc.md` — interactive features and gamification elements

How to use these documents when implementing:
- **MUST start with PRD first** — it defines the core product scope and requirements for any implementation work.
- Implementation must follow facts from PRD/user flows, not assumptions. Cite the document and section heading when helpful.
- If there is a conflict:
  - PRD defines scope, data rules, and acceptance criteria.
  - User flows define the sequence, screen states, and UX details.
  - Call out any discrepancies explicitly and ask for clarification.
- If a detail is missing or ambiguous:
  - Ask concise clarifying questions before implementing.
  - If an immediate decision is required, state assumptions clearly and mark them as assumptions.
- Update the documents with new information as needed.

Implementation requirements:
- Map requirements to modules:
  - `:features:<feature>:data` — data layer must be derived from PRD API requirements
  - `:features:<feature>:presentation` — ViewModels and UI state must be derived from PRD and user flows
  - `:features:<feature>:ui` — Compose screens must be derived from PRD and user flows
  - `:core:designsystem` — reusable UI components must be generic and parameterized
  - `iosApp` — platform wrapper and integrations
  - `server` — backend APIs and business logic
- UI states, empty/loading/error cases, and copy must be derived from PRD/user_flow where specified. Do not invent behavior not grounded in docs.
- Reusable components must be generic and parameterized; document with KDoc

Validation & testing requirements:
- Acceptance criteria and test scenarios must be derived from PRD sections. Reference them in test method names or comments when practical.
- UI tests must mirror the steps from `.junie/guides/project/user_flow.md`.

Important notes:
- Must keep sensitive or internal doc content out of public-facing code comments unless strictly necessary.
- If the documents appear outdated relative to the code, flag this immediately and ask for an update.

## Project Conventions Enforcement

**CRITICAL**: After any code generation, modification, or refactoring, you MUST validate compliance with project conventions. This is non-negotiable.

### Enforcement Checklist

Run through this checklist after implementing any code:

#### 1. Architecture Validation
- [ ] Code follows Clean Architecture with vertical slices
- [ ] Features are properly modularized (api/data/presentation/ui/wiring pattern)
- [ ] Only `api` modules are exposed to other features
- [ ] `data`, `presentation`, `ui`, and `wiring` modules remain internal

#### 2. Interface Pattern Enforcement (CRITICAL)
Every interface (repositories, services, use cases) MUST follow the Impl + Factory Function pattern:
- [ ] Implementation class named `<InterfaceName>Impl` (internal/private)
- [ ] Public top-level factory function named exactly like the interface
- [ ] Factory function returns the interface type

**Example**:
```kotlin
// Interface
interface JobRepository { ... }

// Implementation (internal)
internal class JobRepositoryImpl(...) : JobRepository { ... }

// Factory function (public)
fun JobRepository(...): JobRepository = JobRepositoryImpl(...)
```

#### 3. Dependency Injection Compliance
- [ ] Production classes are free of DI annotations
- [ ] Wiring modules use Koin `module { }` DSL with `single`/`factory` functions returning interface types
- [ ] Wiring modules properly aggregate dependencies
- [ ] Koin modules are loaded in platform-specific source sets (commonMain for data, androidMain/jvmMain for UI)

#### 4. Repository Boundary Rules
- [ ] Repositories return `Either<RepoError, T>` using Arrow
- [ ] API services throw exceptions and expose DTOs
- [ ] Repositories map DTOs to domain models
- [ ] Use `Either.catch { }.mapLeft { it.toRepoError() }` pattern
- [ ] NEVER use Kotlin `Result` or nulls for error signaling at boundaries

#### 5. Presentation Layer Standards
- [ ] ViewModels extend `androidx.lifecycle.ViewModel`
- [ ] NO work in `init` blocks
- [ ] NO stored `CoroutineScope` field - use `viewModelScope` parameter with default
- [ ] Implement `UiStateHolder<S, E>` interface
- [ ] One-time events via `OneTimeEventEmitter<E>` delegated to `EventChannel<E>`
- [ ] Use `SavedStateHandle` for state restoration
- [ ] Expose immutable collections only (`kotlinx.collections.immutable`)
- [ ] NO empty/pass-through use cases - call repositories directly unless orchestration is needed

#### 6. Testing Requirements (MANDATORY)

**NO CODE WITHOUT TESTS** - See `.junie/guides/tech/testing_strategy.md`

- [ ] Every production file has a corresponding test file
- [ ] Tests are in correct location:
  - [ ] Repositories → androidUnitTest/
  - [ ] ViewModels → androidUnitTest/
  - [ ] Mappers → androidUnitTest/ with property-based tests
  - [ ] Use Cases → androidUnitTest/
  - [ ] API Services → androidUnitTest/
  - [ ] Simple utilities → commonTest/
  - [ ] @Composable → @Preview in same file + screenshot test
- [ ] Minimum coverage per file type:
  - [ ] Repositories: success + all error paths
  - [ ] ViewModels: initial, loading, success, error states
  - [ ] Mappers: property-based data preservation tests
- [ ] Kotest as primary framework (androidUnitTest/)
- [ ] MockK for Android mocking (androidUnitTest/)
- [ ] Property-based tests for parsers, mappers, invariants (use `checkAll`/`forAll`)
- [ ] JSON modules have round-trip tests (json→object→json, object→json→object)
- [ ] Roborazzi screenshot tests with baselines in `composeApp/src/test/snapshots`
- [ ] All tests pass before PR: `./gradlew testDebugUnitTest`

#### 7. Testing Strategy (Mobile-First)
- [ ] Primary tests in `androidUnitTest/` to leverage Kotest and MockK
- [ ] Android tests validate ALL shared business logic
- [ ] `commonTest/` only for platform-agnostic utilities (no dependencies)
- [ ] Property-based testing with Kotest's `checkAll`/`forAll`
- [ ] MockK for powerful mocking (repositories, APIs)
- [ ] Roborazzi for screenshot tests (Android/Robolectric)
- [ ] Focus on mobile scenarios (primary target)
- [ ] iOS compiles same code (type safety guaranteed)
- [ ] Native-specific code uses expect/actual (minimal, isolated)

**Testing Trade-offs:**
- ✅ Full Kotest + MockK in Android tests
- ✅ Faster feedback (seconds vs iOS minutes)
- ✅ Tests cover shared KMP business logic
- ✅ Mobile-first (Android validates, iOS shares same code)
- ⚠️ Native-only edge cases test separately (expect/actual)

#### 8. Navigation & Lifecycle
- [ ] Navigation 3 for Compose Multiplatform
- [ ] Navigation contracts in feature `api`, implementations in feature modules
- [ ] Dispatchers are injected (IO/Default), never hardcoded
- [ ] Use structured concurrency and cancellation-aware IO

#### 8. UI Components & Previews (MANDATORY)
- [ ] Every @Composable function has a @Preview
- [ ] Preview shows realistic data (not empty/null states)
- [ ] Preview is annotated with `@Preview` from `org.jetbrains.compose.ui.tooling.preview.Preview`
- [ ] Previews should be private functions named `<ComponentName>Preview`
- [ ] For SwiftUI views, include `#Preview` macro
- [ ] Complex screens have multiple previews (loading, content, error states)

#### 9. Module Structure & Naming
- [ ] Features follow `:features:<feature>:api`, `:features:<feature>:data`, `:features:<feature>:presentation`, `:features:<feature>:ui`, `:features:<feature>:wiring`
- [ ] Shared modules: `:core:<domain>` (kept minimal)
- [ ] iOS umbrella exports only `api` and `presentation` modules (ViewModels for iOS)
- [ ] Appropriate convention plugins applied (`convention.feature.api`, `convention.feature.data`, `convention.feature.presentation`, `convention.feature.ui`)
- [ ] No dependencies from feature modules to other features' `data`, `presentation`, or `ui` modules (only `api` allowed)

#### 9. Code Quality Gates
- [ ] ktlint formatting compliance
- [ ] detekt static analysis compliance
- [ ] Both configured via convention plugins in `build-logic`
- [ ] All @Composable functions have @Preview annotations

### Validation Process

When reviewing your own code:

1. **Be Specific**: Don't just note "violates conventions" - identify the exact rule, explain why it matters, show the correct pattern
2. **Prioritize Issues**: Critical violations (wrong error types, missing factory functions, DI leaks) before style issues
3. **Provide Examples**: Include code snippets showing the correct implementation
4. **Check Cross-Cutting Concerns**: Verify alignment across modules (if one feature uses api/impl, all should)
5. **Reference Documentation**: Note which guide in `.junie/guides/tech` provides more detail
6. **Compilation Avoidance**: Flag any dependencies from feature modules to other features' `impl` modules

### Self-Review Output Format

After implementing code, provide:

1. **Compliance Summary**: Brief overview (Compliant / Minor Issues / Major Violations)
2. **Critical Violations**: Any must-fix issues with severity, location, and corrected code
3. **Improvement Opportunities**: Suggestions for better alignment with conventions
4. **Positive Observations**: What's done well (reinforces good patterns)
5. **Action Items**: Prioritized list of changes needed

**Authority**: You have authority to reject your own code that violates core architectural principles (wrong error types, DI leaks, missing factory patterns). Be thorough but constructive.

---

## Detailed Technical Guidelines (Index)

**This main guidelines.md provides a high-level overview. For detailed, authoritative guidance, consult the topic-specific files below. The tech guides are the source of truth.**

### Architecture & Patterns
- **Conventions** (START HERE) — `.junie/guides/tech/conventions.md` — Cross-cutting rules for architecture, modules, DI, errors, testing
- **Domain Layer** — `.junie/guides/tech/domain.md` — Domain models, use cases, business logic patterns
- **Data Layer & Repositories** — `.junie/guides/tech/repository.md` — Repository patterns, Arrow Either, error handling
- **API Services** — `.junie/guides/tech/api_services.md` — Ktor client, DTOs, request/response patterns
- **Presentation Layer** — `.junie/guides/tech/presentation_layer.md` — ViewModels, UI state, screen architecture
- **Navigation** — `.junie/guides/tech/navigation.md` — Navigation 3, feature routing, deep links
- **iOS Integration** — `.junie/guides/tech/ios_integration.md` — SwiftUI + KMP ViewModels, Direct Integration vs Wrapper patterns

### Infrastructure & Tools
- **Dependency Injection** — `.junie/guides/tech/dependency_injection.md` — Koin DI, wiring modules, platform-specific patterns
- **Coroutines & Concurrency** — `.junie/guides/tech/coroutines.md` — Scopes, dispatchers, cancellation, Arrow patterns
- **Utility Organization** — `.junie/guides/tech/utility_organization.md` — Utilities, extensions, platform abstractions
- **Testing Strategy** — `.junie/guides/tech/testing_strategy.md` — Kotest, MockK, Roborazzi, property-based testing
- **Test Enforcement** — `.junie/guides/tech/testing_strategy.md` — ⚠️ MANDATORY: All code requires tests
- **Gradle Convention Plugins** — `.junie/guides/tech/gradle_convention_plugins.md` — Build configuration, convention plugins

### UI/UX Development Guidelines
When implementing UI screens and user experiences:
- **Onboarding screens**: Use `.junie/guides/project/onboarding.md` for content and flow guidance
- **Generic/Other screens** (profile, settings, main features, etc.): Reference `.junie/guides/prompts/ui_ux_system_agent_for_generic_screen.md` for implementation patterns and creative UI development guidance
- **Overall UI/UX strategy**: Use `.junie/guides/prompts/uiux_agent_system_prompt.md` for high-level design direction and screen planning

Extending:
- Add a new .md file under .junie/guides/ for each new topic (e.g., state_management.md, persistence.md, navigation.md).
- Keep each file scoped, actionable, and aligned with Product Knowledge docs located in `.junie/guides/project`.
- Optionally add a bullet entry here, but Junie should proactively search .junie/guides/ for relevant topics.
