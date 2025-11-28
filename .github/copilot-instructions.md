# Copilot Instructions for Kotlin Multiplatform POC

**Last Updated:** November 27, 2025

> **Related Documentation**: See also [`.junie/guidelines.md`](../.junie/guidelines.md) for high-level project guidelines and [`AGENTS.md`](../AGENTS.md) for autonomous agent workflows. These documents are kept in sync.

> **⚠️ Sync Maintenance**: When updating architectural patterns, ensure AGENTS.md, guidelines.md, and this file stay synchronized. Run Documentation Management Mode monthly to verify.

> Quick Index: Specialized Agents (jump links)
> - Product Design Mode — .junie/guides/prompts/product_designer_agent_system_prompt.md
> - UI/UX Design Mode — .junie/guides/prompts/uiux_agent_system_prompt.md
> - Screen Implementation Mode — .junie/guides/prompts/ui_ux_system_agent_for_generic_screen.md
> - SwiftUI Screen Agent — .junie/guides/prompts/ui_ux_system_agent_for_swiftui_screen.md
> - Onboarding Design Mode — .junie/guides/prompts/onboarding_agent_system_prompt.md
> - User Flow Planning Mode — .junie/guides/prompts/user_flow_agent_system_prompt.md
> - Testing Strategy Mode — .junie/guides/prompts/testing_agent_system_prompt.md
> - Backend Development Mode — .junie/guides/prompts/backend_agent_system_prompt.md
> - Documentation Management Mode — .junie/guides/prompts/documentation_agent_system_prompt.md
> - Standard Development Mode — see “Task Type Decision Tree” in `AGENTS.md`
>
> For a browsable list with one‑line purposes, see `.junie/guides/prompts/README.md`.

## Project Overview

This is a **Kotlin Multiplatform project** with **Compose Multiplatform UI for Android and Desktop (JVM)**, and a **native SwiftUI app for iOS** that consumes shared business logic. The project has **pokemonlist and pokemondetail features fully implemented** with design system, Navigation 3, and iOS integration operational. Additional architecture patterns are documented in `.junie/guides/` for future features.

### Current Module Structure
```
:composeApp   → Compose Multiplatform UI (Android + Desktop JVM)
              ├── commonMain: Shared Compose UI code
              ├── androidMain: Android-specific implementations
              └── jvmMain: Desktop-specific implementations

:shared       → iOS umbrella framework
              └── Exports: :features:*:api, :features:*:presentation, :core:* to iOS
              Note: Does NOT contain business logic itself

:iosApp       → Native SwiftUI iOS app
              └── Consumes: shared.framework to access KMP modules

:server       → Ktor backend (Netty on port 8080) - Backend-for-Frontend (BFF)

:features:pokemonlist:api    → Public contracts - IMPLEMENTED ✅
:features:pokemonlist:data   → Network + Data layer - IMPLEMENTED ✅
:features:pokemonlist:presentation → ViewModels, UI state - IMPLEMENTED ✅
:features:pokemonlist:ui     → Compose UI screens - IMPLEMENTED ✅
:features:pokemonlist:wiring → Koin DI assembly - IMPLEMENTED ✅
```

### Required Architecture
Split-by-layer modularization with api/data/presentation/ui/wiring pattern:
- `:features:<feature>:api` → Public contracts (interfaces, navigation, models) - exported to iOS
- `:features:<feature>:data` → Network + Data layer (API services, DTOs, repositories, mappers) - KMP all targets, NOT exported
- `:features:<feature>:presentation` → ViewModels, UI state - **KMP all targets, shared with iOS**, exported to iOS
- `:features:<feature>:ui` → Compose UI screens - **Android + JVM only**, NOT exported to iOS
- `:features:<feature>:wiring` → Koin DI assembly - KMP with platform-specific source sets, NOT exported

**Critical**: Split-by-layer architecture with platform-specific UI:
- Data layer (network, repositories) in `:data` module - all KMP targets
- ViewModels and UI state in `:presentation` module - **shared across all platforms including iOS**
- Compose UI in `:ui` module - Android + JVM only (iOS uses native SwiftUI)
- Wiring provides repos and ViewModels in `commonMain`, UI bindings in platform-specific source sets

**DO NOT mix Compose UI with platform-agnostic presentation code**. Keep them in separate modules.

**⚠️ CRITICAL**: Feature modules must follow this pattern. Always consult `.junie/guides/tech/conventions.md` before creating new modules or implementing patterns.

**Current State**: `pokemonlist` feature fully implemented with split-by-layer pattern. Use it as reference when creating new features.

### Convention Plugin Architecture
Following [Now in Android](https://github.com/android/nowinandroid) patterns with shared configuration utilities and base plugin composition. See `.junie/guides/tech/convention_plugins_guide.md` for complete guide (consolidated November 26, 2025).

## Build & Validation Workflow

### Primary Validation Command
```bash
# Always validate with Android build + ALL tests (fastest feedback)
./gradlew :composeApp:assembleDebug test --continue
```

### Dependency Management
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


**iOS Integration**: Direct Integration pattern (private var ViewModel + @State for UI state) is current. See `.junie/guides/tech/ios_integration.md` for complete guide including alternative Wrapper pattern for complex apps.

Entry point: `iosApp/iosApp.xcodeproj` (Xcode only)

### Test Configuration
- **Framework**: Kotest (assertions, property-based testing)
- **Mocking**: MockK (JVM/Android only—use fakes for Native)
- **Screenshot**: Roborazzi (Robolectric-based, JVM tests)
- **Location**: Tests live in `commonTest/` unless platform-specific

## Critical Project Conventions

### Dependency Injection (Koin)

See [Impl+Factory Pattern](../.junie/guides/tech/critical_patterns_quick_ref.md#implfactory-pattern) for canonical rules.

**Quick**: `internal class XImpl`, `fun X(...): X = XImpl(...)`, Koin uses factory functions.

**Extended examples**: See `.junie/guides/patterns/di_patterns.md`

### Error Handling (Arrow Either)

See [Either Boundary Pattern](../.junie/guides/tech/critical_patterns_quick_ref.md#either-boundary-pattern) for canonical rules.

**Quick**: Return `Either<RepoError, T>`, use `Either.catch { }.mapLeft { it.toRepoError() }`, sealed errors, DTO→domain mapping.

**Extended examples**: See `.junie/guides/patterns/error_handling_patterns.md`

### ViewModels (androidx.lifecycle)

See [ViewModel Pattern](../.junie/guides/tech/critical_patterns_quick_ref.md#viewmodel-pattern) for canonical rules.

**Quick**: Extend `ViewModel`, pass `viewModelScope` to constructor, implement `UiStateHolder<S, E>`, no init work, lifecycle loading, immutable collections.

**Extended examples**: See `.junie/guides/patterns/viewmodel_patterns.md`

### Navigation (Navigation 3)
**Pattern**: Route objects in `:api`, UI in `:ui`, wiring in platform-specific source sets
- Route objects are plain Kotlin objects/data classes (no @Serializable)
- Navigator manages explicit back stack
- Platform-specific wiring provides EntryProviderInstallers via Koin

**See**: `.junie/guides/patterns/navigation_patterns.md` for complete examples

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

**Pattern Library** (`.junie/guides/patterns/`):
- **`architecture_patterns.md`** — Split-by-layer, module structure, convention plugins, iOS exports
- **`di_patterns.md`** — Koin DI, Impl+Factory, wiring modules, testing
- **`error_handling_patterns.md`** — Either boundaries, sealed errors, exception mapping
- **`viewmodel_patterns.md`** — Lifecycle-aware, parametric, pagination, SavedStateHandle
- **`navigation_patterns.md`** — Navigation 3, route objects, animations
- **`testing_patterns.md`** — Kotest+MockK, property tests, Turbine, Roborazzi

**Quick Reference**:
- **`QUICK_REFERENCE.md`** — Commands, tables, API references, decision matrices

**Detailed Tech Guides** (`.junie/guides/tech/`):
1. **`conventions.md`** — Master reference: architecture, modules, DI, testing rules
2. **`dependency_injection.md`** — Koin setup, module DSL patterns, platform-specific wiring
3. **`repository.md`** — Either boundaries, DTO→domain mapping, error handling
4. **`presentation_layer.md`** — ViewModel lifecycle, UiStateHolder pattern, event handling
5. **`navigation.md`** — Navigation 3 contracts, routing, deep links
6. **`testing_strategy.md`** — Kotest assertions, property-based tests, Roborazzi
7. **`ios_integration.md`** — iOS SwiftUI + KMP ViewModels patterns, Direct Integration vs Wrapper

**Product requirements** (`.junie/guides/project/`):
- **`prd.md`** — Features, acceptance criteria, business rules
- `user_flow.md`, `onboarding.md`, `ui_ux.md` — User journeys and UX specs

## Common Patterns & Conventions

### Code Organization
- **Compose UI (Android/Desktop)**: `composeApp/src/commonMain/` for shared UI
- **Platform-specific UI**: `androidMain/`, `jvmMain/` (iOS uses SwiftUI separately)
- **Business logic**: In feature modules (`:features:<feature>:api`, `:features:<feature>:data`, `:features:<feature>:presentation`) and core modules
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
  - `:shared` exports: Only `:features:<feature>:api`, `:features:<feature>:presentation`, and `:core:*` modules to iOS
  - Never export: `:data`, `:ui`, `:wiring`, or UI modules (`:composeApp`)
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
1. **Start with Android validation**: `./gradlew :composeApp:assembleDebug test --continue` (fastest feedback)
2. **Test incrementally**: Run unit tests after each logical change
3. **Avoid premature optimization**: Implement working code first, optimize if needed
4. **Follow split-by-layer pattern**: Create :api, :data, :presentation, :ui, :wiring modules for each feature

### When Adding Dependencies
1. **Use version catalog**: Add to `gradle/libs.versions.toml`
2. **Check for updates first**: Run `./gradlew dependencyUpdates` to see available versions
3. **Check compatibility**: Ensure KMP support for multiplatform modules
4. **Prefer common dependencies**: Add to `commonMain` unless platform-specific

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

### Test Enforcement (MANDATORY)

**NO CODE WITHOUT TESTS** - See `.junie/guides/tech/testing_strategy.md`

Before marking any code complete:
- ✅ Every production file has a test file
- ✅ Tests are in correct location (androidUnitTest/ for business logic)
- ✅ Minimum coverage achieved (success + error paths)
- ✅ @Composable functions have @Preview
- ✅ All tests pass
- ✅ No manual casts after type-checking matchers (see kotest_smart_casting_quick_ref.md)

**Automatic Rejection Criteria:**
- ❌ Repository without tests
- ❌ ViewModel without tests
- ❌ Mapper without property-based tests
- ❌ @Composable without @Preview
- ❌ Modified code without updated tests
- ❌ Manual cast after `shouldBeInstanceOf`, `shouldBeLeft`, or other smart-casting matchers

### Testing Strategy

See [Testing Pattern](../.junie/guides/tech/critical_patterns_quick_ref.md#testing-pattern) for complete canonical rules.

**Quick summary**: Use Kotest + MockK in `androidUnitTest/` for business logic, 30-40% property-based test coverage, Turbine for flows (NEVER Thread.sleep), smart casting with Kotest matchers.

**Detailed guide**: See `.junie/guides/tech/testing_strategy.md`

**Why Android Unit Tests:**
- ✅ Full Kotest + MockK support (assertions, property testing, powerful mocking)
- ✅ Fast execution on JVM (Robolectric-based, seconds not minutes)
- ✅ Tests validate ALL shared KMP business logic
- ✅ Type safety guarantees iOS compatibility

### Error Handling Checklist

See [Either Boundary Pattern](../.junie/guides/tech/critical_patterns_quick_ref.md#either-boundary-pattern) for complete rules on repositories, Either.catch, error mapping, and sealed hierarchies.

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

## Commits & Changelog

### Conventional Commits (Required)
All commits must follow Conventional Commits format for automatic changelog generation:
```bash
git commit -m "type(scope): description"
```

**Commit Types:**
- `feat` → New feature (✨ Features in changelog)
- `fix` → Bug fix (🐛 Bug Fixes)
- `docs` → Documentation (📝 Documentation)
- `test` → Tests (✅ Tests)
- `build` → Build system (🔧 Build System)
- `refactor` → Code refactoring (♻️ Refactoring)
- `chore` → Maintenance (🧹 Chores)

**Examples:**
```bash
git commit -m "feat(pokemonlist): add search functionality"
git commit -m "refactor(navigation): align package with folder structure"
git commit -m "docs(conventions): update testing requirements"
```

### CHANGELOG Policy
- ❌ **DO NOT manually edit CHANGELOG.md** — it's auto-generated by git-cliff
- ✅ Use proper Conventional Commits format — git-cliff parses these automatically
- ✅ Regenerate changelog before releases: `git cliff -o CHANGELOG.md`
- ✅ Preview without writing: `git cliff --dry-run`

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
- [ ] Wiring modules use `module { }` DSL
- [ ] Koin DI conventions followed

#### Testing
- [ ] Kotest tests written in `commonTest/`
- [ ] Property-based tests for parsers/mappers (`checkAll`/`forAll`)
- [ ] MockK for JVM/Android; fakes for Native
- [ ] Screenshot tests with Roborazzi where applicable

#### Build & Quality
- [ ] Dependencies added to `gradle/libs.versions.toml`
- [ ] Convention plugins applied appropriately
- [ ] Code passes ktlint/detekt (configured via convention plugins)
- [ ] Android build + ALL tests validate: `./gradlew :composeApp:assembleDebug test --continue`
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
