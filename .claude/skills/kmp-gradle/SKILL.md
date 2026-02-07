---
name: kmp-gradle
description: Gradle convention plugins and build configuration for Kotlin Multiplatform. Use when (1) Creating or configuring KMP modules, (2) Troubleshooting build errors, (3) Managing KMP targets (Android, JVM, iOS), (4) Updating shared dependencies or test configuration, (5) Working with the build-logic/convention directory.
version: 1.0.0
tags: [gradle, kmp, build, convention-plugins, architecture]
---

# KMP Gradle Convention Plugins

Master reference for the project's Gradle configuration using the Convention Plugin pattern (inspired by Now in Android).

## When to Use This Skill

**MANDATORY**: Use when:
- Creating a new feature or core module
- Modifying `build.gradle.kts` files
- Changing KMP targets or compiler options
- Adding new shared dependencies (Arrow, Ktor, Compose)
- Debugging "Plugin not found" or version catalog errors

## Related Skills

| Skill | Use For |
|-------|---------|
| **@kmp-architecture** | Vertical slicing and module layer definitions |
| **@kmp-di** | Koin configuration (auto-included by wiring plugins) |
| **@kmp-testing-strategy** | Detailed test implementation patterns |

## NEVER

- ❌ **NEVER** run iOS builds for routine validation (they take 5-10 mins). Use the primary validation command instead.
- ❌ **NEVER** mix `core.library` with `feature.base` in the same module.
- ❌ **NEVER** copy-paste target configuration; use the provided plugins.
- ❌ **NEVER** update versions in plugins; use `gradle/libs.versions.toml`.

## Critical Patterns

### 1. Plugin Composition Hierarchy
Plugins are layered to eliminate duplication:
- `feature.base`: Foundation (Targets, Android, Tests, Common Deps)
- `feature.*` (api, data, presentation, wiring): Layer-specific logic + `feature.base`
- `feature.ui`: Compose Multiplatform + `feature.base`

### 2. Shared Utilities
Located in `build-logic/convention/src/main/kotlin/com/minddistrict/multiplatformpoc/`:
- `configureKmpTargets()`: Standard Android/JVM/iOS targets
- `configureTests()`: JUnit Platform + logging
- `configureComposeMultiplatform()`: Compose runtime and material3

### 3. Auto-Included Dependencies
- **Base**: Arrow, Coroutines, Immutable Collections, kotlin-test
- **Data**: Ktor (core, contentNeg, logging), kotlinx-serialization (Json)
- **Presentation**: AndroidX Lifecycle ViewModel (KMP)
- **UI**: Compose Multiplatform full stack

## Decision Matrix: Which Plugin to Use?

| Module Type | Plugin | iOS Export | Common Deps |
|-------------|--------|------------|-------------|
| **Feature API** | `convention.feature.api` | ✅ Yes | ✅ Yes (Base) |
| **Feature Data** | `convention.feature.data` | ❌ No | ✅ Yes (+Ktor/Ser) |
| **Feature Presentation** | `convention.feature.presentation` | ✅ Yes | ✅ Yes (+Lifecycle) |
| **Feature UI** | `convention.feature.ui` | ❌ No* | ✅ Yes (+Compose) |
| **Feature Wiring** | `convention.feature.wiring` | ❌ No | ✅ Yes (Base) |
| **Core Utility** | `convention.core.library` | ✅ Yes | ❌ No (Manual) |

*\*UI is exported to iOS Compose App, but NOT to Native SwiftUI App.*

## Detailed Guides

- [**Plugin Catalog**](references/plugin-catalog.md): Complete list of plugins and provides
- [**Module Creation**](references/module-creation.md): Step-by-step feature setup
- [**Troubleshooting**](references/troubleshooting.md): Common errors and fixes

## Primary Validation Command

**ALWAYS run before committing:**
```bash
./gradlew :composeApp:assembleDebug test --continue
```
