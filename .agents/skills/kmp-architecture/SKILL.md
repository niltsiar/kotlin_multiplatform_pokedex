---
name: kmp-architecture
description: "Kotlin Multiplatform architecture patterns for vertical slice organization, module structure, and feature boundaries. Use when: (1) Designing new feature module structure, (2) Deciding between :core vs :features modules, (3) Understanding split-by-layer patterns, (4) Setting up multi-UI theme architecture (Material + Unstyled), (5) Planning module dependencies and iOS export boundaries"
---

# KMP Architecture Skill

Architecture patterns for organizing Kotlin Multiplatform code with true vertical slicing and clear module boundaries.

## When to Use

Use this skill when working on:
- Designing new feature module structure and layer organization
- Deciding between creating a `:core` module vs keeping logic in `:features`
- Planning module dependencies and cross-feature interactions
- Setting up dual-UI theme architecture (Material Design 3 + Compose Unstyled)
- Configuring iOS framework exports via `:shared` framework
- Migrating from horizontal layers (shared network/data) to vertical slices

Do NOT use for:
- ViewModel implementation details → use @kmp-presentation
- Repository implementation patterns → use @kmp-data-layer
- Koin DI configuration details → use @kmp-di
- Product requirements or PRD creation → use @product-designer

## Mode Detection

| User Request | Reference File | Load When |
|--------------|----------------|-----------|
| "Create new feature module" | [module-structure.md](references/module-structure.md) | MANDATORY - Read before implementing |
| "Decide :core vs :features" | [core-modules.md](references/core-modules.md) | MANDATORY - Read before implementing |
| "Vertical slicing principles" | [vertical-slicing.md](references/vertical-slicing.md) | MANDATORY - Read before implementing |
| "Implement utility class" | [utility-patterns.md](references/utility-patterns.md) | MANDATORY - Read before implementing |

**MANDATORY - READ ENTIRE FILE**: Before creating new feature modules, you MUST read [module-structure.md](references/module-structure.md) (~150 lines) for complete 8-module pattern.

**MANDATORY - READ ENTIRE FILE**: Before implementing utility classes, you MUST read [utility-patterns.md](references/utility-patterns.md) (~140 lines) for Koin-compatible utility patterns.

**Do NOT load** `utility-patterns.md` for architecture decisions, module structure, or feature planning.
**Do NOT load** `module-structure.md` for utility implementation.

## Module Structure Overview

All features use **split-by-layer** architecture with 8 standard modules:

| Module | Purpose | KMP Targets | iOS Export |
|--------|---------|-------------|------------|
| `:api` | Public contracts, interfaces, navigation | All | ✅ Yes |
| `:data` | API services, DTOs, repositories | All | ❌ No |
| `:presentation` | ViewModels, UI state | All | ✅ Yes |
| `:ui-material` | Material Design 3 Compose UI | Android + JVM + iOS Compose | ❌ No |
| `:ui-unstyled` | Compose Unstyled UI | Android + JVM + iOS Compose | ❌ No |
| `:wiring` | Business DI (repos, ViewModels) | All | ❌ No |
| `:wiring-ui-material` | Material navigation registration | Android + JVM + iOS Compose | ❌ No |
| `:wiring-ui-unstyled` | Unstyled navigation registration | Android + JVM + iOS Compose | ❌ No |

**Example**: `features/pokemonlist/` contains all 8 modules above with complete implementation.

## Vertical Slicing Principle

**Core Rule**: Each feature owns ALL its layers end-to-end. Features are self-contained vertical slices.

```
┌─────────────────────────────────────────┐
│  Feature: Pokemon List                  │
├─────────────────────────────────────────┤
│  :api        → Repository interface     │
│  :data       → API service, DTOs, impl  │
│  :presentation → ViewModel, UI state    │
│  :ui-*       → Compose screens          │
│  :wiring*    → DI assembly              │
└─────────────────────────────────────────┘
```

**Benefits**:
- Compilation avoidance: Changes to Pokemon Detail don't recompile Pokemon List
- Team autonomy: Features developed independently
- Clear boundaries: All code for a feature lives in one place
- Testability: Self-contained with explicit dependencies

**NEVER share**: API services, DTOs, repository implementations between features. Each feature defines its own, even if calling the same backend endpoint.

## Core Module Guidelines

**ONLY create `:core` modules for**:
1. **Truly generic utilities** used by 3+ features (date formatters, string utils)
2. **Design system** (reusable UI components, theme, tokens)
3. **Cross-cutting domain models** (User, Error types used everywhere)
4. **Platform abstractions** (expect/actual for platform APIs)

**NEVER create `:core` modules for**:
- ❌ Generic network layer (each feature has its own HttpClient config)
- ❌ Generic repository base classes (each feature implements its own)
- ❌ Generic database layer (each feature manages its own data)
- ❌ Generic API service interfaces (each feature defines its own)

**Rule of thumb**: If it serves 1-2 features, put it in the feature. If it serves 3+ features, consider :core. Duplication is better than premature abstraction.

**MANDATORY**: Before creating a :core module, read [core-modules.md](references/core-modules.md).

## Feature Module Boundaries

### Dependency Rules

```
:features:profile:data  →  :features:auth:api     ✅ OK (public API)
:features:profile:data  →  :features:auth:data    ❌ NEVER (implementation)
```

### iOS Export Boundaries

**NEVER export to iOS via `:shared` framework**:
- `:features:*:data` - Implementation details
- `:features:*:ui-*` - Compose UI (iOS uses SwiftUI)
- `:features:*:wiring*` - DI assembly

**ALWAYS export to iOS**:
- `:features:*:api` - Contracts for iOS to implement against
- `:features:*:presentation` - ViewModels for iOS SwiftUI consumption
- `:core:*` - Shared utilities and domain types

## Multi-UI Theme Architecture

For dual-theme support (Material + Unstyled):

1. **Scope markers in design system**:
   - `MaterialScope` in `:core:designsystem-material`
   - `UnstyledScope` in `:core:designsystem-unstyled`

2. **Separate wiring-ui modules**:
   - `:wiring-ui-material` scoped to `MaterialScope`
   - `:wiring-ui-unstyled` scoped to `UnstyledScope`

3. **Both loaded simultaneously** in app - Koin Navigation 3 manages scope automatically

## Essential Workflows

### Workflow 1: Create New Feature Module (Vertical Slice)

To add a new feature following the vertical slice architecture:

1. **Create directory structure** in `features/<feature>/`:
   - `api/`, `data/`, `presentation/`, `ui-material/`, `ui-unstyled/`, `wiring/`, `wiring-ui-material/`, `wiring-ui-unstyled/`.
2. **Apply convention plugins** in each module's `build.gradle.kts`:
   - `:api` → `id("convention.feature.api")`
   - `:data` → `id("convention.feature.data")`
   - `:presentation` → `id("convention.feature.presentation")`
   - `:ui-*` → `id("convention.feature.ui")`
   - `:wiring*` → `id("convention.feature.wiring")`
3. **Define public contracts** in `:api`:
   - Create repository interface and Navigation 3 route objects.
4. **Implement data layer** in `:data`:
   - Create internal repository implementation class.
   - Create public factory function (e.g., `fun FeatureRepository(...): FeatureRepository`).
   - Define feature-specific API service and DTOs.
5. **Create presentation layer** in `:presentation`:
   - Implement `ViewModel` with `SavedStateHandle` and `viewModelScope` support.
   - Define `UiState` sealed hierarchy.
6. **Implement UI** in `:ui-material` and `:ui-unstyled`:
   - Build Compose screens and add `@Preview` for all states.
7. **Assemble DI** in `:wiring`:
   - Define Koin module registering the implementation classes.
8. **Register navigation** in `:wiring-ui-*`:
   - Map routes to screens within `MaterialScope` and `UnstyledScope`.

### Workflow 2: Decide :core vs :features

Follow the **3-Feature Rule** and decision matrix:

1. **Identify the concern**: Is it generic infrastructure or business logic?
2. **Apply decision matrix**:
   - **Generic Utilities** (Date, String): Use `:core:util` if 3+ features need it.
   - **Design System**: Always in `:core:designsystem-*`.
   - **Domain models**: Keep in the feature's `:api` unless 3+ features share it (then `:core:domain`).
   - **Platform Abstractions**: Use `:core:platform` for `expect/actual` patterns.
3. **Avoid the "Common" trap**: Don't create a `:core:common` for "everything else". Use specific, descriptive module names.
4. **Prefer Duplication**: If only 2 features share a DTO or small utility, duplicate it to maintain vertical slice independence.

### Workflow 3: Add Cross-Feature Dependency

To use logic from Feature A (e.g., `auth`) in Feature B (e.g., `profile`):

1. **Verify Interface Availability**: Ensure the required repository interface or domain model is public in `features/auth/api`.
2. **Declare Dependency**: Add the `:api` dependency in Feature B's consuming module (usually `:data` or `:presentation`):
   ```kotlin
   // features/profile/data/build.gradle.kts
   dependencies {
       implementation(projects.features.auth.api)
   }
   ```
3. **Inject via Koin**: Request the dependency in Feature B's wiring module:
   ```kotlin
   // features/profile/wiring/ProfileModule.kt
   val profileModule = module {
       factory { ProfileRepository(authRepository = get()) }
   }
   ```
4. **Enforce Boundaries**: Never allow `profile` to depend on `auth:data`. If `auth:api` doesn't have what you need, refactor `auth` to expose it via its public API contract.

## Critical Guardrails

1. **NEVER depend on implementation modules**: Features must only depend on the `:api` of other features. No cross-dependencies on `:data`, `:presentation`, or `:ui`.
2. **NEVER export implementation to iOS**: Only `:api` and `:presentation` modules should be exported via the `:shared` framework to keep the iOS umbrella framework lean.
3. **NEVER create :core for 1-2 features**: Follow the 3-feature rule. Duplication is cheaper than the wrong abstraction.
4. **NEVER share DTOs between features**: Each feature defines its own DTOs in its `:data` module, even if calling the same backend API endpoint.
5. **NEVER create empty use cases**: Call repositories directly from ViewModels. Create `:domain` and use cases only for orchestrating 2+ repositories or complex business rules.
6. **NEVER do work in ViewModel init**: Override `onStart(owner)` to trigger initial data loading. This ensures network calls only happen when the UI is active and lifecycle-aware.
7. **NEVER swallow CancellationException**: Ensure `Either.catch` or manual try-catch blocks allow cancellation to propagate, preventing leaked coroutines.
8. **NEVER use star imports**: Always use explicit imports to prevent naming collisions and improve code readability (enforced by .editorconfig).
9. **NEVER share database instances**: Features should manage their own persistence layer to maintain independence and avoid global schema migrations.

## Cross-References

### Related Skills
| Skill | Purpose | Link |
|-------|---------|------|
| @kmp-presentation | ViewModel lifecycle, SavedStateHandle, UI state | [SKILL.md](../kmp-presentation/SKILL.md) |
| @kmp-data-layer | Repository patterns, DTO mapping, RepoError | [SKILL.md](../kmp-data-layer/SKILL.md) |
| @kmp-di | Koin module configuration, parameter injection | [SKILL.md](../kmp-di/SKILL.md) |
| @kmp-navigation | Navigation 3 routes, scoped navigation providers | [SKILL.md](../kmp-navigation/SKILL.md) |
| @kmp-ios | SwiftUI + KMP integration, Direct Integration pattern | [SKILL.md](../kmp-ios/SKILL.md) |

### Documentation
| Document | Purpose | Link |
|----------|---------|------|
| [module-structure.md](references/module-structure.md) | Detailed layer breakdown (8-module pattern) | [Read](references/module-structure.md) |
| [vertical-slicing.md](references/vertical-slicing.md) | Principles and benefits of vertical slicing | [Read](references/vertical-slicing.md) |
| [core-modules.md](references/core-modules.md) | Guidelines for creating :core modules | [Read](references/core-modules.md) |
| [@kmp-critical-patterns](../kmp-critical-patterns/SKILL.md) | 6 core patterns for rapid development | [Read](../kmp-critical-patterns/SKILL.md) |

### Reference Implementation
Study the `features/pokemonlist/` modules for a complete implementation of all 8 layers:
- **API**: `PokemonListRepository.kt` and navigation routes
- **Data**: `PokemonListRepositoryImpl.kt`, `ApiService.kt`, and mappers
- **Presentation**: `PokemonListViewModel.kt` and `UiState.kt`
- **UI**: Material and Unstyled screen implementations
- **Wiring**: Koin module registration and Navigation 3 entry providers

## Quick Reference

### Module Naming

```
:features:<feature>:api              ✅
:features:<feature>:data             ✅
:features:<feature>:presentation     ✅
:features:<feature>:ui-material      ✅
:features:<feature>:ui-unstyled      ✅
:features:<feature>:wiring           ✅
:features:<feature>:wiring-ui-*      ✅

:pokemonlist                         ❌ Missing :features prefix
:features:pokemon-list               ❌ Hyphenated (use lowercase)
:features:pokemonList                ❌ CamelCase (use lowercase)
:features:pokemonlist:impl           ❌ Use :data, :presentation
```

### Package Naming

Convert dashes to dots: `:features:pokemonlist:ui-material` → `features.pokemonlist.ui.material`

### Validation Commands

```bash
# Build and test (always run before committing)
./gradlew :composeApp:assembleDebug test --continue

# Check module dependencies
./gradlew :features:<feature>:api:dependencies --configuration commonMain

# Verify iOS export configuration
./gradlew :shared:dependencies --configuration iosMain
```
