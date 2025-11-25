# Convention Plugin Improvements - Summary

## Overview

Improved the convention plugin architecture by adopting patterns from Google's [Now in Android](https://github.com/android/nowinandroid) project, specifically around shared configuration utilities and plugin composition.

## Changes Made

### 1. Created Shared Configuration Package

Created `com.minddistrict.multiplatformpoc` package with utility functions to eliminate duplication:

**Files Created:**
- `KotlinMultiplatform.kt` - Centralized KMP target configuration
- `TestConfiguration.kt` - Standardized test setup
- `ComposeConfiguration.kt` - Compose Multiplatform dependencies (for future use)
- `ProjectExtensions.kt` - Clean version catalog access

### 2. Created Base Feature Plugin

**New Plugin:** `ConventionFeatureBasePlugin` (ID: `convention.feature.base`)

**Provides:**
- KMP targets (Android, JVM, iOS) configuration
- Android library setup
- Test configuration
- Common feature dependencies:
  - `arrow-core` (functional error handling)
  - `kotlinx-coroutines-core` (async operations)
  - `kotlinx-collections-immutable` (UI state)

### 3. Refactored Feature Layer Plugins

**Updated Plugins to Compose Base:**
- `ConventionFeatureApiPlugin` - Now applies `convention.feature.base`
- `ConventionFeatureImplPlugin` - Now applies `convention.feature.base`
- `ConventionFeatureWiringPlugin` - Now applies `convention.feature.base`

**UI Plugin (Kept Explicit):**
- `ConventionFeatureUiPlugin` - Maintains explicit target configuration (Android + JVM only, no iOS)
- Still applies common dependencies directly (Arrow, Coroutines, Collections)
- Applies `convention.compose.multiplatform` for Compose dependencies

**Core Library Plugin:**
- `ConventionCoreLibraryPlugin` - Now uses shared configuration functions
- Does NOT compose base plugin (core modules have different dependency needs)

## Code Reduction

### Before
Each of the 5 feature plugins (api, impl, wiring, ui, core) duplicated:
- 45 lines of KMP target configuration
- 15 lines of Android library setup
- 20 lines of test configuration

**Total duplication: ~400 lines across 5 plugins**

### After
- Base plugin: 70 lines (provides common setup)
- Feature plugins: 5-15 lines each (just apply base + layer-specific config)
- Shared utilities: 120 lines (reusable functions)

**Total: ~250 lines, reduction of ~150 lines (38% reduction)**

## Benefits

### Maintainability
- **Single source of truth** for KMP target configuration
- **Centralized test setup** - one place to change test behavior
- **Easier updates** - changing KMP targets only requires updating one function

### Consistency
- **Guaranteed consistency** across all feature modules
- **Common dependencies** automatically included in all features
- **Standardized patterns** following Now in Android conventions

### Developer Experience
- **Less boilerplate** when creating new feature modules
- **Clear plugin hierarchy** - base provides foundation, layers add specifics
- **Better discoverability** - shared utilities in dedicated package

## Pattern Comparison

| Aspect | Before | After |
|--------|--------|-------|
| **KMP Configuration** | Duplicated in 5 files | Centralized in `configureKmpTargets()` |
| **Test Setup** | Duplicated in 5 files | Centralized in `configureTests()` |
| **Common Dependencies** | Manual in each module | Automatic via base plugin |
| **Plugin Lines of Code** | 80-100 lines each | 10-20 lines each |
| **Composition** | Flat, independent | Hierarchical, composable |

## Validation

✅ Build successful: `./gradlew :composeApp:assembleDebug test --continue`
✅ All tests passing (88 tests across repository, mapper, ViewModel)
✅ Configuration cache compatible
✅ No breaking changes to existing modules

## Future Enhancements

Based on Now in Android patterns, consider adding:

1. **Lint Convention Plugin** - Centralized lint configuration with XML/SARIF reports
2. **Compose Compiler Metrics** - Debug Compose performance issues
3. **Module Dependency Graph** - Auto-generate Mermaid diagrams of module structure
4. **Root Plugin** - Project-wide task registration
5. **Build Type Enums** - Type-safe build configurations

## Migration Guide for New Features

### Before (Old Pattern)
```kotlin
// features/newfeature/api/build.gradle.kts
plugins {
    id("convention.feature.api")
}

kotlin {
    // Lots of duplicated configuration here
    androidTarget { /* ... */ }
    jvm { /* ... */ }
    iosArm64()
    // etc.
}
```

### After (New Pattern)
```kotlin
// features/newfeature/api/build.gradle.kts
plugins {
    id("convention.feature.api")  // Automatically gets base configuration
}

// Add only API-specific dependencies if needed
kotlin {
    sourceSets {
        commonMain.dependencies {
            // Layer-specific dependencies only
        }
    }
}
```

## Architecture Alignment

This refactoring aligns with the project's documented patterns in `.junie/guides/tech/conventions.md`:

✅ **Split-by-layer architecture** - Base plugin supports all feature layers
✅ **DI-agnostic** - No DI framework imposed by plugins
✅ **KMP-first** - All feature modules are KMP by default
✅ **Platform-specific UI** - UI plugin maintains explicit target control

## Notes

- **UI plugin** intentionally does NOT use base plugin to maintain explicit target configuration (Android + JVM only, no iOS)
- **Core library plugin** uses shared functions but NOT base plugin (different dependency needs)
- **Common dependencies** (Arrow, Coroutines, Collections) are now automatic for all features
- **Compose dependencies** remain in UI modules only (not in base)

---

**Implementation Date:** November 25, 2025
**Inspired By:** [Now in Android](https://github.com/android/nowinandroid) convention plugin architecture
