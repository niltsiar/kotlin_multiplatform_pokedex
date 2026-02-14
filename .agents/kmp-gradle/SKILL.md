---
name: kmp-gradle
description: Gradle convention plugins and build configuration for Kotlin Multiplatform. Use when (1) Creating or configuring KMP modules, (2) Troubleshooting build errors, (3) Managing KMP targets (Android, JVM, iOS), (4) Updating shared dependencies or test configuration, (5) Working with the build-logic/convention directory.
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

## Decision Framework

Before configuring Gradle modules, ask yourself:

1. **Which convention plugin should I use?**
   - `:api` module → `convention.feature.api` (contracts, domain models)
   - `:data` module → `convention.feature.data` (repos, DTOs, mappers)
   - `:presentation` module → `convention.feature.presentation` (ViewModels)
   - `:ui-*` module → `convention.feature.ui` (Compose screens)
   - `:wiring*` module → `convention.feature.wiring` (Koin DI)
   - `:core` module → `convention.core.library` or `convention.kmp.library`

2. **What dependencies are needed?**
   - Use version catalog: `libs.arrow.core`, `libs.koin.core`, etc.
   - Use project references: `implementation(projects.core.domain)`
   - NEVER use hardcoded versions in build.gradle.kts

3. **What KMP targets are required?**
   - Convention plugins auto-configure: Android, iOS, JVM (Desktop)
   - Platform-specific code → Use source sets: `androidMain`, `iosMain`, `jvmMain`
   - Shared code → Use `commonMain` for maximum reuse

## Essential Workflows

### Workflow 1: Creating a New Feature Module with Convention Plugins

To add a new vertical slice feature following the standard 5-module structure:

1. **Create directory structure**:
   ```bash
   mkdir -p features/myfeature/{api,data,presentation,ui,wiring}/src/{commonMain,commonTest}/kotlin
   ```

2. **Apply layer-specific plugins** in each module's `build.gradle.kts`:
   ```kotlin
   // features/myfeature/api/build.gradle.kts
   plugins { id("convention.feature.api") }

   // features/myfeature/data/build.gradle.kts
   plugins { id("convention.feature.data") }
   kotlin {
       sourceSets {
           commonMain.dependencies {
               implementation(projects.features.myfeature.api)
               implementation(projects.core.httpclient)
           }
       }
   }
   ```

3. **Register in `settings.gradle.kts`**:
   ```kotlin
   include(":features:myfeature:api")
   include(":features:myfeature:data")
   include(":features:myfeature:presentation")
   include(":features:myfeature:ui")
   include(":features:myfeature:wiring")
   ```

4. **Export to iOS** via `shared/build.gradle.kts`:
   ```kotlin
   commonMain.dependencies {
       api(projects.features.myfeature.api)
       api(projects.features.myfeature.presentation)
   }
   ```
   *Cross-reference: @kmp-architecture for vertical slice principles.*

### Workflow 2: Configuring KMP Targets (Android/JVM/iOS)

All standard targets are configured via `configureKmpTargets()` in `build-logic`:

1. **Standard Plugin Usage**:
   ```kotlin
   // In a convention plugin implementation
   kotlin {
       configureKmpTargets(extension = this, includeIos = true)
   }
   ```

2. **Manual Target Configuration** (if avoiding plugins):
   ```kotlin
   // build.gradle.kts
   kotlin {
       androidTarget { compilerOptions { jvmTarget.set(JvmTarget.JVM_11) } }
       jvm { compilerOptions { jvmTarget.set(JvmTarget.JVM_11) } }
       iosArm64(); iosSimulatorArm64(); iosX64()
   }
   ```
   *Cross-reference: @kmp-commands for target-specific validation.*

### Workflow 3: Adding Shared Dependencies via Version Catalog

Centralize all dependency versions in `gradle/libs.versions.toml`:

1. **Define version and library**:
   ```toml
   # gradle/libs.versions.toml
   [versions]
   kotlinx-serialization = "1.7.3"

   [libraries]
   kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
   ```

2. **Consume in module**:
   ```kotlin
   // build.gradle.kts
   kotlin {
       sourceSets {
           commonMain.dependencies {
               implementation(libs.kotlinx.serialization.json)
           }
       }
   }
   ```
   *Cross-reference: @kmp-developer for general dependency management.*

### Workflow 4: Troubleshooting Plugin Not Found Errors

If Gradle fails to locate a convention plugin:

1. **Verify build-logic inclusion** in `settings.gradle.kts`:
   ```kotlin
   pluginManagement {
       includeBuild("build-logic")
   }
   ```

2. **Force-build logic modules**:
   ```bash
   ./gradlew :build-logic:convention:build
   ```

3. **Verify registration** in `build-logic/convention/build.gradle.kts`:
   ```kotlin
   gradlePlugin {
       plugins {
           register("featureApi") {
               id = "convention.feature.api"
               implementationClass = "ConventionFeatureApiPlugin"
           }
       }
   }
   ```
   *Cross-reference: @kmp-commands for build troubleshooting.*

## Critical Guardrails

1. **NEVER run iOS builds for routine validation** → Use `./gradlew :composeApp:assembleDebug test --continue` instead (saves 5-10 mins).
2. **NEVER mix `core.library` with `feature.base`** → Use `core.library` for pure infrastructure to avoid inheriting feature dependencies like Arrow (prevents dependency bloat).
3. **NEVER copy-paste target configuration** → Always use provided convention plugins (ensures consistency across all 40+ modules).
4. **NEVER update versions in plugins** → Always use `gradle/libs.versions.toml` (single source of truth for versions).
5. **NEVER export implementation modules to iOS** → Only export `:api` and `:presentation` modules in `shared/build.gradle.kts` (keeps the iOS framework lean).
6. **NEVER use star imports in Gradle files** → Always use explicit imports for clarity and to prevent naming collisions (enforced by .editorconfig).
7. **NEVER add feature-specific dependencies to `feature.base`** → Add them to layer-specific plugins or local `build.gradle.kts` (maintains clear layer boundaries).
8. **NEVER skip validation after modifying `build-logic`** → Always run `./gradlew :build-logic:convention:build` to catch compilation errors in plugins.

## Troubleshooting Common Build Issues

### Unresolved Reference Errors (Despite Correct Imports)

**Symptom:**
```
e: file:///path/to/BaseTokens.kt:51:47 Unresolved reference 'RoundedCornerShape'
e: file:///path/to/BaseTokens.kt:59:17 Unresolved reference 'dp'
```

**Cause:** Gradle build cache corruption from previous failed builds.

**Solution:**
```bash
./gradlew clean :composeApp:assembleDebug test --continue
```

**Why:** Stale dependency resolution cache prevents proper import resolution. Clean build clears cache.

**Prevention:** Run clean build after multiple consecutive failed builds or when seeing import errors on standard library types.

---

### "Unresolved reference 'generated'" for Compose Resources

**Symptom:**
```kotlin
import multiplatformpoc.core.designsystem_core.generated.resources.Res
// Error: Unresolved reference 'generated'
```

**Cause:** Library module resources not configured for public access.

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

---

### Navigation Provider "Unresolved reference" Errors

**Symptom:**
```kotlin
PokemonListScreenUnstyled(viewModel = ...)
// Error: Unresolved reference 'PokemonListScreenUnstyled'
```

**Cause:** Screen function naming convention mismatch.

**Correct Pattern:**
- ✅ `{Feature}UnstyledScreen` (e.g., `PokemonListUnstyledScreen`)
- ✅ `{Feature}MaterialScreen` (e.g., `PokemonListMaterialScreen`)
- ❌ `{Feature}ScreenUnstyled` (wrong suffix order)

**Solution:**
```kotlin
// ✅ CORRECT imports and usage
import ...ui.unstyled.PokemonListUnstyledScreen
PokemonListUnstyledScreen(viewModel = ...)

import ...ui.material.PokemonListMaterialScreen  
PokemonListMaterialScreen(viewModel = ...)
```

**Why:** Consistent naming convention: `{Adjective}{Noun}` not `{Noun}{Adjective}`.

---

## Quick Reference

### Convention Plugin Selection Guide

| Module Type | Plugin | iOS Export | Includes |
|-------------|--------|------------|----------|
| **Feature API** | `convention.feature.api` | ✅ Yes | Base (Arrow, Coroutines) |
| **Feature Data** | `convention.feature.data` | ❌ No | Base + Ktor, Serialization |
| **Feature Presentation** | `convention.feature.presentation` | ✅ Yes | Base + Lifecycle ViewModel |
| **Feature UI** | `convention.feature.ui` | ❌ No* | Base + Compose Multiplatform |
| **Feature Wiring** | `convention.feature.wiring` | ❌ No | Base + Koin DI |
| **Core Utility** | `convention.core.library` | ✅ Yes | KMP Targets only (No Base) |

*\*UI is exported to iOS Compose App, but NOT to Native SwiftUI App.*

### Common Gradle Commands

| Task | Command | When to Use |
|------|---------|-------------|
| **Primary Validation** | `./gradlew :composeApp:assembleDebug test --continue` | Before every commit |
| **Build Logic** | `./gradlew :build-logic:convention:build` | After modifying plugins |
| **Check Updates** | `./gradlew dependencyUpdates` | Monthly maintenance |
| **Clean Build** | `./gradlew clean` | When cache is corrupted |
| **Sync Project** | `./gradlew projects` | After `settings.gradle.kts` changes |

### Version Catalog Access Patterns

| Pattern | Access Code |
|---------|-------------|
| **Library** | `libs.ktor.client.core` |
| **Bundle** | `libs.bundles.kotest` |
| **Version** | `libs.versions.kotlin.get()` |
| **Plugin** | `alias(libs.plugins.kotlinx.serialization)` |

## Cross-References

### Skills (by Category)

**Architecture**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-architecture | Module structure, vertical slicing, feature boundaries | [SKILL.md](../kmp-architecture/SKILL.md) |
| @kmp-critical-patterns | 6 core patterns quick reference | [SKILL.md](../kmp-critical-patterns/SKILL.md) |

**Layer Implementation**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-data-layer | Repository patterns, Either<RepoError, T> | [SKILL.md](../kmp-data-layer/SKILL.md) |
| @kmp-presentation | ViewModels, UI state management | [SKILL.md](../kmp-presentation/SKILL.md) |
| @kmp-api-services | Ktor Client, DTOs, API service patterns | [SKILL.md](../kmp-api-services/SKILL.md) |
| @kmp-di | Koin dependency injection patterns | [SKILL.md](../kmp-di/SKILL.md) |

**Development & Build**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-commands | CLI reference, build commands, validation scripts | [SKILL.md](../kmp-commands/SKILL.md) |
| @kmp-developer | General KMP development patterns | [SKILL.md](../kmp-developer/SKILL.md) |
| @kmp-testing-strategy | Testing philosophy, coverage guidelines | [SKILL.md](../kmp-testing-strategy/SKILL.md) |

### Documents

| Document | Purpose | Link |
| --- | --- | --- |
| Convention Plugins Guide | Comprehensive plugin reference | See @kmp-gradle skill |
| Plugin Catalog | Detailed list of available plugins | [plugin-catalog.md](references/plugin-catalog.md) |
| Module Creation | Step-by-step feature setup guide | [module-creation.md](references/module-creation.md) |
| Troubleshooting | Common Gradle errors and fixes | [troubleshooting.md](references/troubleshooting.md) |
