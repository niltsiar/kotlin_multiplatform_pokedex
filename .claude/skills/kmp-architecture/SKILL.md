---
name: kmp-architecture
description: "Kotlin Multiplatform architecture patterns for vertical slice organization, module structure, and feature boundaries. Use when: (1) Designing new feature module structure, (2) Deciding between :core vs :features modules, (3) Understanding split-by-layer patterns, (4) Setting up multi-UI theme architecture (Material + Unstyled), (5) Planning module dependencies and iOS export boundaries"
---

# KMP Architecture Skill

Architecture patterns for organizing Kotlin Multiplatform code with true vertical slicing and clear module boundaries.

## When to Load This Skill

**MANDATORY**: Load this skill when working on:
- Creating new feature modules and deciding layer structure
- Determining what belongs in `:core` vs `:features` modules
- Setting up dual-UI theme architecture (Material Design 3 + Compose Unstyled)
- Planning module dependencies and export boundaries for iOS
- Migrating from horizontal layers to vertical slices

**Do NOT use for**: ViewModel implementation details → use @kmp-mobile-expert, Repository patterns → use @kmp-data-layer, DI configuration → use @kmp-di, Product requirements → use @product-designer

**Conditional Loading**:
| Task | Reference | Load When |
|------|-----------|-----------|
| Module layer details | [module-structure.md](references/module-structure.md) | Creating new feature modules |
| Vertical slicing principles | [vertical-slicing.md](references/vertical-slicing.md) | Understanding feature boundaries |
| Core module decisions | [core-modules.md](references/core-modules.md) | Deciding :core vs :features |

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

## Related Skills

| Skill | Use For |
|-------|---------|
| @kmp-mobile-expert | ViewModel patterns, repository Either handling, iOS export |
| @kmp-data-layer | Repository implementation, DTO mapping, error handling |
| @kmp-di | Koin module configuration, wiring patterns, scope management |
| @compose-screen | Material/Unstyled UI implementation, @Preview |
| @swiftui-screen | iOS SwiftUI consuming KMP ViewModels |

## Documentation Sources

| Document | Purpose | Tokens |
|----------|---------|--------|
| [conventions.md](../../../docs/tech/conventions.md) | Master architecture reference | ~3000 |
| [architecture_patterns.md](../../../docs/patterns/architecture_patterns.md) | Code examples and patterns | ~2000 |
| [critical_patterns_quick_ref.md](../../../docs/tech/critical_patterns_quick_ref.md) | 6 core patterns | ~1500 |

**Internal references**:
- [module-structure.md](references/module-structure.md) - Detailed layer breakdown
- [vertical-slicing.md](references/vertical-slicing.md) - Principles and benefits
- [core-modules.md](references/core-modules.md) - When to create :core modules

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

### Reference Implementation

Study `features/pokemonlist/` for complete 8-module implementation demonstrating all patterns.
