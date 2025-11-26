# Kotlin Multiplatform Guidelines for Junie

**Last Updated:** November 26, 2025

> **Related**: [`.github/copilot-instructions.md`](../.github/copilot-instructions.md) (Copilot), [`AGENTS.md`](../AGENTS.md) (Agents) — kept in sync

> **⚠️ Sync Maintenance**: When updating architectural patterns, ensure AGENTS.md, copilot-instructions.md, and this file stay synchronized. Run Documentation Management Mode monthly to verify.

## Tech Stack

- **Kotlin Multiplatform** — Shared business logic across platforms
- **Compose Multiplatform** — UI for Android + Desktop (JVM) + iOS (experimental)
- **SwiftUI** — Native iOS UI (production) consuming KMP via shared.framework
- **Ktor** — Backend-for-Frontend server (port 8080)
- **Koin** — Dependency injection with Impl+Factory pattern
- **Arrow Either** — Functional error handling at boundaries
- **Navigation 3** — Modular navigation architecture
- **Kotest + MockK + Turbine** — Testing framework (androidUnitTest/)

## Module Structure (Vertical Slicing)

**Feature pattern** (split-by-layer, REQUIRED):
```
:features:<feature>:api          → Contracts (exported to iOS)
:features:<feature>:data         → Network + Data (NOT exported)
:features:<feature>:presentation → ViewModels (exported to iOS)
:features:<feature>:ui           → Compose UI (NOT exported)
:features:<feature>:wiring       → DI assembly (NOT exported)
```

**Core modules** (use sparingly):
- `:core:designsystem` — Reusable Compose components
- `:core:navigation` — Navigation 3 architecture
- `:core:di` — Koin DI core
- `:core:httpclient` — HttpClient config only

**✅ DO**: Each feature owns its complete vertical slice (API services, DTOs, repos, ViewModels, UI)
**❌ DON'T**: Create generic `:core:network` or `:core:data` modules

**✅ DO**: Each feature owns its complete vertical slice (API services, DTOs, repos, ViewModels, UI)
**❌ DON'T**: Create generic `:core:network` or `:core:data` modules

## iOS Export Rules

**Exported via `:shared` umbrella**:
- ✅ `:features:*:api` — Public contracts
- ✅ `:features:*:presentation` — ViewModels (shared with SwiftUI)
- ✅ `:core:*` — Shared infrastructure

**NOT exported**:
- ❌ `:features:*:data` — Internal data layer
- ❌ `:features:*:ui` — Compose UI (platform-specific)
- ❌ `:features:*:wiring` — DI assembly
- ❌ `:composeApp` — Compose framework (Note: iosAppCompose uses this, but native SwiftUI doesn't)

## Essential Commands

**Primary validation** (ALWAYS run first):
```bash
./gradlew :composeApp:assembleDebug test --continue  # ~45 seconds
```

**Dependency updates**:
```bash
./gradlew dependencyUpdates
open build/dependencyUpdates/report.html
```

**iOS builds** (⚠️ 5-10min — avoid unless required):
- Only run when explicitly requested or testing iOS-specific features
- Native SwiftUI: `open iosApp/iosApp.xcodeproj`
- Compose iOS: `open iosAppCompose/iosAppCompose.xcodeproj`

**See**: `.junie/guides/QUICK_REFERENCE.md` for complete command reference

## Critical Patterns

### 1. Dependency Injection (Koin)

See [Impl+Factory Pattern](guides/tech/critical_patterns_quick_ref.md#implfactory-pattern) for complete canonical rules.

**Pattern**: Impl + Factory + Koin wiring
- `internal class XImpl`, `fun X(...): X = XImpl(...)`
- Koin modules use factory functions
- Keep classes DI-agnostic

**Extended examples**: See `.junie/guides/patterns/di_patterns.md`

### 2. Error Handling (Arrow Either)

See [Either Boundary Pattern](guides/tech/critical_patterns_quick_ref.md#either-boundary-pattern) for complete canonical rules.

**Pattern**: `Either<RepoError, T>` at boundaries
- Return `Either<RepoError, T>`, never `Result` or nullable
- Use `Either.catch { }.mapLeft { it.toRepoError() }`
- Sealed error hierarchies per feature
- DTO→domain mapping at boundary

**Extended examples**: See `.junie/guides/patterns/error_handling_patterns.md`

### 3. ViewModels (androidx.lifecycle)

See [ViewModel Pattern](guides/tech/critical_patterns_quick_ref.md#viewmodel-pattern) for complete canonical rules.

**Pattern**: Inject `viewModelScope`, lifecycle-aware loading
- Extend `androidx.lifecycle.ViewModel`
- Pass `viewModelScope` to constructor with default
- Implement `UiStateHolder<S, E>`
- Load in lifecycle callbacks, NOT `init`
- Use `kotlinx.collections.immutable` types
- NEVER store `CoroutineScope` as field

**Extended examples**: See `.junie/guides/patterns/viewmodel_patterns.md`

### 4. Navigation (Navigation 3)

**Pattern**: Route objects + Navigator + EntryProviderInstaller
```kotlin
// ✅ DO: Plain route objects, EntryProviderInstaller in wiring
data class PokemonDetail(val id: Int)  // No @Serializable needed

// ❌ DON'T: Export navigation to iOS
// Navigation is Compose-specific, NOT exported via :shared
```

**See**: `.junie/guides/patterns/navigation_patterns.md`

### 5. Testing (Kotest + MockK + Turbine)

**Mandatory**: Every production file needs tests

**Test location strategy**:
- `androidUnitTest/` — ALL business logic (repos, ViewModels, mappers)
- `commonTest/` — Only simple utilities with NO dependencies

**Property test targets**:
- Mappers: 100% property tests
- Repositories: 40-50% property tests
- ViewModels: 30-40% property tests

```kotlin
// ✅ DO: Use Turbine for flow testing
viewModel.uiState.test {
    awaitItem() shouldBe Loading
    viewModel.start(mockk(relaxed = true))
    testScope.advanceUntilIdle()
    awaitItem().shouldBeInstanceOf<Content>()
    cancelAndIgnoreRemainingEvents()
}

// ❌ DON'T: Use Thread.sleep or delay
Thread.sleep(100)  // Wrong: flaky, slow
delay(100)  // Wrong: flaky
```

**See**: `.junie/guides/patterns/testing_patterns.md`

## Critical DON'Ts (Top 10)
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

- **ViewModels**: See [ViewModel Pattern](guides/tech/critical_patterns_quick_ref.md#viewmodel-pattern) for complete rules on lifecycle-aware constructor injection, viewModelScope parameter, UiStateHolder<S, E>, immutable collections, and OneTimeEventEmitter<E>
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
