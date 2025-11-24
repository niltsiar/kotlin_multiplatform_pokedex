# Project Guidelines — Kotlin + Compose Multiplatform

This repository is a Kotlin Multiplatform project using Jetpack Compose Multiplatform to target Android and iOS. It also includes a JVM desktop showcase for the Design System.

## Project Overview
- Languages/Tech: Kotlin, Compose Multiplatform, Gradle (Kotlin DSL)
- Targets:
  - Android app in composeApp
  - iOS app in iosApp (Swift/Xcode wrapper around shared code)
  - JVM desktop showcase in designsystem/src/jvmMain (for component gallery/playground)
- Modules:
  - composeApp — application code shared across platforms (commonMain) + platform-specific source sets
  - designsystem — reusable UI components and utilities, shared across platforms
  - iosApp — Xcode project that embeds the shared code
  - docs — product and UX documentation
  - distribution — release-related assets (e.g., Android keystore, what’s new)
  - scripts — helper build/release scripts

## Directory Structure (high level)
- composeApp/
  - src/commonMain — shared business/UI logic in Kotlin/Compose
  - src/androidMain — Android-specific code and resources
  - src/iosMain — iOS-specific glue code
  - src/commonTest — shared tests. If it is Ui/Compose related tests, it should be in screentest package.
- designsystem/
  - src/commonMain — shared design system components
  - src/androidMain, src/iosMain — platform specifics
  - src/jvmMain — desktop entry for previewing components (Main.kt)
- iosApp/ — Xcode project/workspace and iOS assets

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

### JVM Desktop (Design System showcase)
- Entry point: designsystem/src/jvmMain/kotlin/Main.kt
- Run from IDE as a standard Kotlin application to preview components

## Testing
- Android UI tests on device (if any under composeApp/src/commonTest/screentest): `./gradlew :composeApp:connectedDebugAndroidTest`
- Shared unit tests (if any under composeApp/src/commonTest): `./gradlew :composeApp:testDebugUnitTest` (or relevant target-specific tasks)
- iOS tests/builds: Do NOT run by default. Only execute iOS-specific tests/builds if the issue explicitly requires iOS behavior or when the User requests it.

## How Junie should validate changes
1. Prefer running the most relevant tests for changed modules:
   - For non ui related changes run: `:composeApp:testDebugUnitTest`
   - For Android UI/logic: run `:composeApp:connectedDebugAndroidTest` (device required)
2. Do NOT run the iOS app, iOS builds, or iOS tests during routine validation. Only run iOS-specific tasks if the issue explicitly requires iOS behavior or the User asks for it.
3. If tests are absent for the area, at minimum run a build to ensure compilation:
   - `./gradlew :composeApp:assembleDebug` (Android-only build for faster feedback)
4. For UI-only changes in the design system, optionally run the JVM desktop Main.kt from IDE to visually verify components.
5. If only designsystem module is changed, it shouldn't run tests - just ensure compilation with build tasks.

## Code Style & Conventions
- Kotlin idiomatic style; follow platform-specific Compose best practices
- Keep common code in commonMain; isolate platform code in platform-specific source sets
- Prefer small, previewable composables in designsystem
- Document public APIs and compose components with KDoc when feasible

## CI/CD Notes
- Distribution assets live under distribution/
- No CI configuration is enforced here; use Gradle tasks above locally

## Troubleshooting
- First run may download Compose and JetBrains JDK; builds can take longer
- If Android tasks fail, verify local.properties contains sdk.dir and JDK 17+ is used
- iOS builds are significantly slower; avoid running iOS build/app during routine checks unless explicitly required for the issue.

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
  - composeApp: feature logic/screens derived from PRD and user flows
  - designsystem: reusable UI components; keep them generic and parameterized
  - iosApp: platform wrapper and integrations
- Derive UI states, empty/loading/error cases, and copy from PRD/user_flow where specified. Avoid inventing behavior not grounded in docs.

Validation & testing from docs:
- Derive acceptance criteria and test scenarios from PRD sections. Reference them in test method names or comments when practical.
- For UI flows, mirror the steps from `.junie/guides/project/user_flow.md` in UI tests.

Notes:
- Keep sensitive or internal doc content out of public-facing code comments unless strictly necessary.
- If the documents appear outdated relative to the code, flag this in the PR description and ask for an update.

## Modular Guidelines Files (Index)
In addition to this main guidelines.md, Junie must consult topic-specific guidelines located under .junie/guides/ when working on relevant areas. These smaller files keep guidance focused and easier to maintain.

Current topics:
- Domain Layer Guidelines — .junie/guides/tech/domain.md
- Data Layer & Repository Guidelines — .junie/guides/tech/repository.md
- API Services Guidelines — .junie/guides/tech/api_services.md
- Presentation Layer Guidelines — .junie/guides/tech/presentation_layer.md
- Navigation Guidelines — .junie/guides/tech/navigation.md
- Dependency Injection Guidelines — .junie/guides/tech/dependency_injection.md
- Utility Organization Guidelines — .junie/guides/tech/utility_organization.md
- Testing Strategy Guidelines — .junie/guides/tech/testing_strategy.md
- Coroutines & Concurrency — .junie/guides/tech/coroutines.md
- Conventions for Modular Guidelines — .junie/guides/tech/conventions.md

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
