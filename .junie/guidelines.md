# Project Guidelines — Kotlin + Compose Multiplatform

This repository is a Kotlin Multiplatform project using Jetpack Compose Multiplatform to target Android, iOS, Desktop (JVM), and Server.

## Project Overview
- **Languages/Tech**: Kotlin, Compose Multiplatform, Gradle (Kotlin DSL), Metro DI, Arrow (Either), Kotest, MockK, Roborazzi, Navigation 3
- **Targets**:
  - Android app in composeApp
  - Desktop (JVM) app in composeApp
  - iOS app in iosApp (Swift/Xcode wrapper around shared code)
  - Server in server (Ktor-based backend)
- **Module Structure**:
  - `composeApp` — application code shared across platforms (commonMain) + platform-specific source sets (androidMain, jvmMain)
  - `shared` — iOS umbrella framework that exports required public contracts
  - `server` — Ktor server application
  - `iosApp` — Xcode project that embeds the shared code
  - `:features:<feature>:api` — public contracts (interfaces, navigation contracts, shared domain models)
  - `:features:<feature>:impl` — private implementations (repositories, data sources, UI implementations)
  - `:features:<feature>:wiring` — DI assembly and aggregation
  - `:core:designsystem` — reusable Compose components, theming, design tokens
  - `:core:util` — shared utilities
  - `:core:domain:api` — shared domain types

## Architecture
- **Clean Architecture with vertical slices** — each feature owns its code end-to-end
- **Vertical-slice feature modules** following api/impl/wiring pattern (see `.junie/guides/tech/conventions.md`)
- **Key patterns**:
  - Impl + Factory: interfaces implemented by internal `*Impl` classes, exposed via public factory functions
  - Only `api` modules exposed cross-feature; `impl` and `wiring` remain internal
  - Metro DI: production classes stay DI-agnostic; wire via `@Provides` functions in wiring modules
  - Arrow Either: repositories return `Either<RepoError, T>` at boundaries

## Directory Structure
- `composeApp/`
  - `src/commonMain` — shared business/UI logic in Kotlin/Compose
  - `src/androidMain` — Android-specific code and resources
  - `src/jvmMain` — Desktop (JVM) specific code
  - `src/commonTest` — shared tests (place UI/Compose tests in `screentest` package)
- `shared/` — iOS umbrella framework module
- `server/` — Ktor server application
- `iosApp/` — Xcode project/workspace and iOS assets

## Prerequisites
- JDK 17+
- Android SDK path configured in local.properties (sdk.dir=/path/to/sdk)
- For iOS: Xcode installed and, optionally, KMM plugin for Android Studio
- Optional: Run KDoctor to verify environment

## Build and Run
### Android
- Build debug APK: `./gradlew :composeApp:assembleDebug`
- APK output: `composeApp/build/outputs/apk/debug/composeApp-debug.apk`
- Run from IDE: Open in Android Studio and run the Android configuration
- Useful tasks:
  - SHA1 (Firebase, etc.): `./gradlew :composeApp:signingReport`

### iOS
- Note: iOS builds are slow. Junie must NOT run the iOS app or trigger iOS builds during routine validation or checks.
- Only run iOS-specific build/run tasks if the issue explicitly requires iOS behavior or the User asks for it.
- Entry point: iosApp Xcode workspace wraps shared code. Run from Xcode when explicitly needed.

### Desktop (JVM)
- Run: `./gradlew :composeApp:run` or use IDE run configuration
- Desktop-specific code lives in `composeApp/src/jvmMain`

### Server
- Ktor-based backend application
- Run: `./gradlew :server:run` or use IDE run configuration

## Testing
- **Framework**: Kotest (primary), MockK (mocking), Roborazzi (screenshot testing)
- **Shared unit tests**: `./gradlew :composeApp:testDebugUnitTest` (or relevant target-specific tasks)
- **Android UI tests** on device (if any under `composeApp/src/commonTest/screentest`): `./gradlew :composeApp:connectedDebugAndroidTest`
- **Screenshot tests** (Roborazzi): `./gradlew recordRoborazziDebug`, `./gradlew verifyRoborazziDebug`
- **iOS tests/builds**: Do NOT run by default. Only execute iOS-specific tests/builds if the issue explicitly requires iOS behavior or when the User requests it.
- See `.junie/guides/tech/testing_strategy.md` for comprehensive testing guidelines

## How to Validate Changes
1. **Run relevant tests for changed modules**:
   - Non-UI changes: `./gradlew :composeApp:testDebugUnitTest` or `./gradlew :features:<feature>:impl:jvmTest`
   - Android UI/logic (device required): `./gradlew :composeApp:connectedDebugAndroidTest`
   - Screenshot tests: `./gradlew verifyRoborazziDebug`
2. **iOS policy**: Do NOT run iOS app, builds, or tests during routine validation. Only run iOS-specific tasks if explicitly required or requested.
3. **If tests are absent**: Run `./gradlew :composeApp:assembleDebug` as minimum compilation check

## Key Technical Decisions

See `.junie/guides/tech/conventions.md` for comprehensive conventions. Critical patterns:

- **ViewModels**: Extend `androidx.lifecycle.ViewModel`; pass `viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)` parameter; do NOT perform work in `init`; load on lifecycle callbacks
- **Repositories**: Return `Either<RepoError, T>`; use `Either.catch { ... }.mapLeft { it.toRepoError() }`; map DTOs to domain at boundary
- **Navigation**: Use Navigation 3 (`org.jetbrains.androidx.navigation3:navigation3-ui`); define contracts in `api`, implementations in `impl`
- **DI**: Metro with no annotations on classes; wire via `@Provides` in wiring modules
- **No empty use cases**: Call repositories directly from presentation when no orchestration needed
- **Immutable UI state**: Use `kotlinx.collections.immutable` types (`ImmutableList`, `ImmutableMap`)
- **Impl + Factory pattern**: Interfaces implemented by internal `*Impl` classes, exposed via public factory functions
- **Code organization**: Keep common code in `commonMain`; isolate platform code in platform-specific source sets
- **Design system**: Create `:core:designsystem` module for reusable Compose components, theming, design tokens

## Troubleshooting
- **First run**: May download Compose and JetBrains JDK; builds can take longer
- **Android build failures**: Verify `local.properties` contains `sdk.dir` and JDK 17+ is used
- **iOS builds**: Significantly slower; avoid running iOS build/app during routine checks unless explicitly required
- **Metro DI issues**: Ensure KSP is configured correctly; check generated code in `build/generated/ksp`
- **Arrow Either**: Never catch and swallow `CancellationException`; use `Either.catch` which respects cancellation

## Product Knowledge (PRD & User Flows & UI/UX))
When asked about product behavior, UX, feature scope, acceptance criteria, copy, or edge cases, Junie must leverage the project’s product documentation located at `.junie/guides/project` folder:
- **PRIMARY REFERENCE - Always consult first:**
  - `.junie/guides/project/prd.md` — **canonical product requirements, scope, constraints, and acceptance criteria. This is the definitive source for implementation decisions.**
- Supporting documentation:
  - `.junie/guides/project/user_flow.md` — end-to-end user journeys and step-by-step flow details
  - `.junie/guides/project/onboarding.md` — onboarding-specific flows and content
  - `.junie/guides/project/ui_ux.md` — UI/UX guidelines, animations
  - `.junie/guides/project/paywall.md` — paywall and monetization flow guidance
  - `.junie/guides/project/easter_eggs_and_mini_games_for_clipugc.md` — interactive features and gamification elements

How to use these documents when answering:
- **ALWAYS start with PRD first** — it defines the core product scope and requirements for any implementation work.
- Prefer facts from PRD/user flows over assumptions. Tailor answers to the specified feature/flow and cite the document and section heading when helpful.
- If there is a conflict:
  - PRD defines scope, data rules, and acceptance criteria.
  - User flows define the sequence, screen states, and UX details.
  - Call out any discrepancies explicitly and propose a resolution or ask for clarification.
- If a detail is missing or ambiguous:
  - Ask concise clarifying questions.
  - If an immediate answer is required, state assumptions clearly and mark them as assumptions.
- If needed you can update the documents with new information.

Implementation alignment tips:
- Map requirements to modules:
  - `:features:<feature>:impl` — feature logic/screens derived from PRD and user flows
  - `:core:designsystem` — reusable UI components; keep them generic and parameterized
  - `iosApp` — platform wrapper and integrations
  - `server` — backend APIs and business logic
- Derive UI states, empty/loading/error cases, and copy from PRD/user_flow where specified. Avoid inventing behavior not grounded in docs.
- For reusable components: keep them generic and parameterized; document with KDoc

Validation & testing from docs:
- Derive acceptance criteria and test scenarios from PRD sections. Reference them in test method names or comments when practical.
- For UI flows, mirror the steps from `.junie/guides/project/user_flow.md` in UI tests.

Notes:
- Keep sensitive or internal doc content out of public-facing code comments unless strictly necessary.
- If the documents appear outdated relative to the code, flag this in the PR description and ask for an update.

## Detailed Technical Guidelines (Index)

**This main guidelines.md provides a high-level overview. For detailed, authoritative guidance, consult the topic-specific files below. The tech guides are the source of truth.**

### Architecture & Patterns
- **Conventions** (START HERE) — `.junie/guides/tech/conventions.md` — Cross-cutting rules for architecture, modules, DI, errors, testing
- **Domain Layer** — `.junie/guides/tech/domain.md` — Domain models, use cases, business logic patterns
- **Data Layer & Repositories** — `.junie/guides/tech/repository.md` — Repository patterns, Arrow Either, error handling
- **API Services** — `.junie/guides/tech/api_services.md` — Ktor client, DTOs, request/response patterns
- **Presentation Layer** — `.junie/guides/tech/presentation_layer.md` — ViewModels, UI state, screen architecture
- **Navigation** — `.junie/guides/tech/navigation.md` — Navigation 3, feature routing, deep links

### Infrastructure & Tools
- **Dependency Injection** — `.junie/guides/tech/dependency_injection.md` — Metro DI, wiring modules, graph patterns
- **Coroutines & Concurrency** — `.junie/guides/tech/coroutines.md` — Scopes, dispatchers, cancellation, Arrow patterns
- **Utility Organization** — `.junie/guides/tech/utility_organization.md` — Utilities, extensions, platform abstractions
- **Testing Strategy** — `.junie/guides/tech/testing_strategy.md` — Kotest, MockK, Roborazzi, property-based testing
- **Gradle Convention Plugins** — `.junie/guides/tech/gradle_convention_plugins.md` — Build configuration, convention plugins

### UI/UX Development Guidelines
When implementing UI screens and user experiences:
- **Onboarding screens**: Use `.junie/guides/project/onboarding.md` for content and flow guidance
- **Paywall screens**: Use `.junie/guides/project/paywall.md` for content and flow guidance (if exists)
- **Generic/Other screens** (profile, settings, main features, etc.): Reference `.junie/guides/prompts/ui_ux_system_agent_for_generic_screen.md` for implementation patterns and creative UI development guidance
- **Overall UI/UX strategy**: Use `.junie/guides/prompts/uiux_agent_system_prompt.md` for high-level design direction and screen planning

Extending:
- Add a new .md file under .junie/guides/ for each new topic (e.g., state_management.md, persistence.md, navigation.md).
- Keep each file scoped, actionable, and aligned with Product Knowledge docs located in `.junie/guides/project`.
- Optionally add a bullet entry here, but Junie should proactively search .junie/guides/ for relevant topics.
