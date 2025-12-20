# Convention Plugins Guide

**Last Updated:** November 27, 2025

> **Purpose**: Complete guide to convention plugins in this Kotlin Multiplatform project. Covers architecture, available plugins, usage patterns, and implementation details.

> **Related**: See [critical_patterns_quick_ref.md](critical_patterns_quick_ref.md#convention-plugins-pattern) for canonical pattern rules.

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Available Plugins](#available-plugins)
4. [Usage Guide](#usage-guide)
5. [Creating New Modules](#creating-new-modules)
6. [Implementation Details](#implementation-details)
7. [Troubleshooting](#troubleshooting)

---

## Overview

### Why Convention Plugins?

**Benefits**:
- ✅ Single source of truth for Kotlin, Compose, Android, testing, and linting configuration
- ✅ Reduce copy/paste and configuration drift
- ✅ Make new modules trivial to add (~38% code reduction)
- ✅ Enforce module roles (feature api/data/presentation/ui/wiring) with tailored defaults
- ✅ Enable easy adoption of Compose Multiplatform, Koin DI, Kotest/MockK, Navigation 3

**Inspired by**: [Google's Now in Android](https://github.com/android/nowinandroid) patterns

### Architecture Principles

Following Now in Android patterns (November 2025 update):

1. **Shared configuration utilities** in `com.minddistrict.multiplatformpoc` package
2. **Base plugin composition** to eliminate duplication
3. **Extension functions** for version catalog access
4. **Platform-specific source sets** for targeted configuration

### Dependency Management vs Convention Plugins

**Convention plugins**: Module-level build configuration (targets, source sets, testing frameworks)

**Dependency version management**: Root project level
- **Version Catalog**: `gradle/libs.versions.toml` - centralized dependency versions
- **Ben Manes Versions Plugin**: Root `build.gradle.kts` - automated update checking with stability rules
- **Check updates**: `./gradlew dependencyUpdates`
- **Report**: `build/dependencyUpdates/report.html`

Convention plugins **consume** the version catalog but do NOT manage version updates.

---

## Architecture

### Repository Layout

```text
build-logic/
  settings.gradle.kts                    # ← Shares parent version catalog
  build.gradle.kts                       # ← Publishes convention plugins
  convention/
    build.gradle.kts                     # ← Plugin registration
    src/main/kotlin/
      # Plugin files
      ConventionKmpLibraryPlugin.kt
      ConventionAndroidAppPlugin.kt
      ConventionAndroidLibraryPlugin.kt
      ConventionFeatureBasePlugin.kt     # ← Base plugin for features
      ConventionFeatureApiPlugin.kt
      ConventionFeatureDataPlugin.kt     # ← Data layer (networking/serialization)
      ConventionFeaturePresentationPlugin.kt  # ← ViewModels & presentation deps
      ConventionFeatureUiPlugin.kt
      ConventionFeatureWiringPlugin.kt
      ConventionCoreLibraryPlugin.kt
      ConventionComposeMultiplatformPlugin.kt
      ConventionKmpAndroidAppPlugin.kt  # ← Android app with KMP targets
      
      # Shared configuration utilities (Nov 2025)
      com/minddistrict/multiplatformpoc/
        KotlinMultiplatform.kt           # ← configureKmpTargets()
        TestConfiguration.kt             # ← configureTests()
        ComposeConfiguration.kt          # ← configureComposeMultiplatform()
        ProjectExtensions.kt             # ← libs property, getVersion(), getLibrary()
```

### Build System Setup

**Root `settings.gradle.kts`**:
```kotlin
pluginManagement {
    includeBuild("build-logic")  // ← Makes convention plugins available
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
```

**Root `build.gradle.kts`**:
```kotlin
plugins {
    alias(libs.plugins.benManesVersions)  // ← Dependency update checking
}
```

**`build-logic/settings.gradle.kts`**:
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))  // ← Shares parent catalog
        }
    }
}

include(":convention")
```

**`build-logic/convention/build.gradle.kts`**:
```kotlin
plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.compose.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("conventionFeatureBase") {
            id = "convention.feature.base"
            implementationClass = "ConventionFeatureBasePlugin"
        }
        // ... other plugins ...
    }
}
```

### Shared Configuration Utilities

**`KotlinMultiplatform.kt`** - Centralized KMP target configuration (Android autodetect, JVM 11, optional iOS):
```kotlin
internal fun Project.configureKmpTargets(
    extension: KotlinMultiplatformExtension,
    includeIos: Boolean = true,
) {
    extension.apply {
        // Android target only when Android plugin is applied
        val hasAndroid = pluginManager.hasPlugin("com.android.library") ||
            pluginManager.hasPlugin("com.android.application")
        if (hasAndroid) {
            androidTarget {
                compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
            }
        }

        jvm { compilerOptions { jvmTarget.set(JvmTarget.JVM_11) } }

        if (includeIos) {
            iosArm64(); iosSimulatorArm64(); iosX64()
        }

        sourceSets.apply {
            commonTest.dependencies { implementation(libs.getLibrary("kotlin-test")) }
        }
    }
}
```

**`TestConfiguration.kt`** - Standardized test setup:
```kotlin
internal fun Project.configureTests() {
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
        }
    }
}
```

**`ProjectExtensions.kt`** - Clean version catalog access:
```kotlin
val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun VersionCatalog.getLibrary(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).get()

fun VersionCatalog.getVersion(alias: String): String =
    findVersion(alias).get().toString()
```

### Plugin Composition Hierarchy (Final)

```
convention.feature.base                               # ← Base for all features
├── Provides: KMP targets, tests, common deps
├── Dependencies: Arrow, Coroutines, Immutable Collections
│
├─→ convention.feature.api (composes base)            # ← Public contracts
├─→ convention.feature.data (composes base)           # ← Networking + serialization
├─→ convention.feature.presentation (composes base)   # ← ViewModels & lifecycle
└─→ convention.feature.wiring (composes base)         # ← DI assembly

convention.feature.ui (composes base + compose)       # ← Compose UI
├── Applies: convention.feature.base
├── Applies: convention.compose.multiplatform
└── Inherits common deps from base + adds Compose deps

convention.core.library (standalone)             # ← Core modules
└── Uses: configureKmpTargets(), configureTests()
```

---

## Available Plugins (Final)

### Base Plugin

#### `convention.feature.base`
**Base plugin for all feature modules**

```kotlin
plugins {
    id("convention.feature.base")
}
```

**Provides**:
- KMP targets: Android, JVM, iOS (iosArm64, iosSimulatorArm64, iosX64)
- Android library configuration (compileSdk, minSdk from version catalog)
- Test configuration (JUnit Platform, logging)
- Common dependencies:
  - `arrow-core` (functional error handling)
  - `kotlinx-coroutines-core` (async operations)
  - `kotlinx-collections-immutable` (UI state)
  - `kotlin-test` (commonTest)

**Use when**: Creating new feature modules (automatically applied by layer plugins)

---

### Feature Layer Plugins

#### `convention.feature.api`
**Public interfaces, domain models, navigation contracts**

```kotlin
plugins {
    id("convention.feature.api")  // Composes: convention.feature.base
}
```

**For**: Feature API modules  
**Exports to iOS**: ✅ Yes (via `:shared` umbrella)  
**Targets**: Android, JVM, iOS  
**Contents**:
- Public interfaces (repositories, use cases)
- Domain models shared across boundaries
- Navigation contracts (route objects)

**Example**:
```kotlin
// features/jobs/api/build.gradle.kts
plugins {
    id("convention.feature.api")
}

// Base dependencies (Arrow, Coroutines, Collections) already included
// Add only API-specific dependencies if needed
```

----

#### `convention.feature.data`
**Internal data layer (repositories, API services, DTOs, mappers)**

```kotlin
plugins { id("convention.feature.data") } // Composes: convention.feature.base

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.features.myfeature.api)
            implementation(projects.core.httpclient)
            // Ktor + kotlinx-serialization are provided by the plugin
        }
        commonTest.dependencies { implementation(libs.kotlin.test) }
    }
}
```

**Provides**:
- Applies Kotlin Serialization plugin
- Adds Ktor core, contentNegotiation, logging, and kotlinx-serialization JSON in commonMain
- Adds OkHttp (androidMain), Java (jvmMain), Darwin (iosMain when present)

**Exports to iOS**: ❌ No

----

#### `convention.feature.presentation`
**Presentation layer (ViewModels and presentation-only deps)**

```kotlin
plugins { id("convention.feature.presentation") } // Composes: convention.feature.base

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.features.myfeature.api)
            implementation(projects.features.myfeature.data)
            // Lifecycle ViewModel KMP APIs provided by the plugin
        }
    }
}
```

**Provides**:
- Adds `androidx.lifecycle:lifecycle-viewmodel` (KMP) to commonMain

**Exports to iOS**: ✅ Yes (ViewModels shared with SwiftUI)

---

#### `convention.feature.ui`
**Compose Multiplatform UI screens**

```kotlin
plugins { id("convention.feature.ui") } // Applies: feature.base + compose.multiplatform
```

**For**: Compose Multiplatform screens (@Composable functions)  
**Exports to iOS**: 
- ✅ Yes (to iOS Compose app via ComposeApp.framework)
- ❌ No (to native SwiftUI app - SwiftUI implements UI separately)

**Targets**: Android, JVM, iOS (inherits from base)
**Note**: Inherits Arrow, Coroutines, Collections from base; adds Compose deps

**iOS Strategy**:
- **iosAppCompose** (experimental): Uses shared Compose UI from :ui modules
- **iosApp** (production): Uses native SwiftUI, accesses ViewModels via :shared framework

**Contents**:
- @Composable functions
- Screen implementations
- UI components

**Example**:
```kotlin
// features/jobs/ui/build.gradle.kts
plugins { id("convention.feature.ui") }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.features.jobs.api)
            implementation(projects.features.jobs.presentation)
            implementation(projects.core.designsystem)
            
            // Compose + common deps automatically included via plugins
        }
    }
}
```

---

#### `convention.feature.wiring`
**Koin DI module DSL, platform-specific wiring**

```kotlin
plugins {
    id("convention.feature.wiring")  // Composes: convention.feature.base
}
```

**For**: Koin DI assembly, platform-specific navigation wiring  
**Exports to iOS**: ❌ No (DI wiring is internal)  
**Targets**: Android, JVM, iOS  
**Configures**: Platform-specific source sets (commonMain, androidMain, jvmMain)  
**Pattern**: `module { }` DSL in platform-specific source sets

**Contents**:
- Koin `module { }` definitions in commonMain (repositories, ViewModels)
- Platform-specific navigation in androidMain/jvmMain (EntryProviderInstaller)
- Dependency aggregation

**Example**:
```kotlin
// features/jobs/wiring/build.gradle.kts
plugins {
    id("convention.feature.wiring")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.features.jobs.api)
            implementation(projects.features.jobs.data)
            implementation(projects.features.jobs.presentation)
            
            // Koin dependencies already included from base
        }
        
        androidMain.dependencies {
            implementation(projects.core.navigation)
        }
        
        jvmMain.dependencies {
            implementation(projects.core.navigation)
        }
    }
}
```

**See**: [koin_di_quick_ref.md](koin_di_quick_ref.md) for complete DI patterns

---

### Other Plugins

#### `convention.core.library`
**Core KMP libraries**

```kotlin
plugins {
    id("convention.core.library")  // Uses shared functions, NOT base
}
```

**For**: Core KMP libraries (httpclient, util, domain)  
**Exports to iOS**: ✅ Yes (via `:shared` umbrella)  
**Targets**: Android, JVM, iOS  
**Note**: Does NOT include common dependencies - add explicitly

---

#### `convention.compose.multiplatform`
**Compose Multiplatform dependencies**

```kotlin
plugins {
    id("convention.compose.multiplatform")
}
```

**For**: Adding Compose Multiplatform dependencies  
**Auto-applied by**: `convention.feature.ui`  
**Provides**: Compose runtime, foundation, material3, ui, resources, preview, lifecycle

---

## Usage Guide

### Dependencies Automatically Included

#### By `convention.feature.base`
✅ `arrow-core` - Functional error handling  
✅ `kotlinx-coroutines-core` - Async operations  
✅ `kotlinx-collections-immutable` - UI state collections  
✅ `kotlin-test` - Testing (in commonTest)

#### By `convention.feature.ui`
✅ All base dependencies (Arrow, Coroutines, Collections)  
✅ `compose-runtime`  
✅ `compose-foundation`  
✅ `compose-material3`  
✅ `compose-ui`  
✅ `compose-components-resources`  
✅ `compose-components-uiToolingPreview`  
✅ `androidx-activity-compose` (Android)  
✅ `compose-ui-tooling` (Android)  
✅ `compose.desktop.currentOs` (JVM)

### When to Use Which Plugin?

| Module Type | Plugin | Exports to iOS | Common Deps Auto-Included |
|-------------|--------|----------------|---------------------------|
| Feature API | `convention.feature.api` | ✅ Yes | ✅ Yes (via base) |
| Feature Data | `convention.feature.data` | ❌ No | ✅ Yes (via base + Ktor/Ser) |
| Feature Presentation | `convention.feature.presentation` | ✅ Yes | ✅ Yes (via base + Lifecycle) |
| Feature UI | `convention.feature.ui` | ❌ No (SwiftUI), ✅ Yes (Compose iOS) | ✅ Yes (base + Compose) |
| Feature Wiring | `convention.feature.wiring` | ❌ No | ✅ Yes (via base) |
| Core Library | `convention.core.library` | ✅ Yes | ❌ No (add explicitly) |

---

## Creating New Modules

### Step-by-Step: Create `:features:myfeature`

#### 1. Create Directory Structure
```bash
mkdir -p features/myfeature/{api,data,presentation,ui,wiring}/src/{commonMain,commonTest}/kotlin
```

#### 2. Create Build Files

**API Module** (`features/myfeature/api/build.gradle.kts`):
```kotlin
plugins {
    id("convention.feature.api")
}

// Arrow, Coroutines, Collections already provided
// Add only API-specific dependencies if needed
```

**Data Module** (`features/myfeature/data/build.gradle.kts`):
```kotlin
plugins {
    id("convention.feature.data")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.features.myfeature.api)
            implementation(projects.core.httpclient)
            
            // Ktor + serialization provided by the plugin
        }
        
        androidUnitTest.dependencies {
            implementation(libs.kotest.assertions)
            implementation(libs.mockk)
        }
    }
}
```

**Presentation Module** (`features/myfeature/presentation/build.gradle.kts`):
```kotlin
plugins {
    id("convention.feature.presentation")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.features.myfeature.api)
            implementation(projects.features.myfeature.data)
            
            // Lifecycle ViewModel added by plugin
        }
        
        androidUnitTest.dependencies {
            implementation(libs.kotest.assertions)
            implementation(libs.mockk)
            implementation(libs.turbine)
        }
    }
}
```

**UI Module** (`features/myfeature/ui/build.gradle.kts`):
```kotlin
plugins {
    id("convention.feature.ui")  // Automatically includes Compose
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.features.myfeature.api)
            implementation(projects.features.myfeature.presentation)
            implementation(projects.core.designsystem)
            implementation(projects.core.navigation)
        }
        
        androidUnitTest.dependencies {
            implementation(libs.roborazzi.core)
            implementation(libs.roborazzi.compose)
        }
    }
}
```

**Wiring Module** (`features/myfeature/wiring/build.gradle.kts`):
```kotlin
plugins {
    id("convention.feature.wiring")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.features.myfeature.api)
            implementation(projects.features.myfeature.data)
            implementation(projects.features.myfeature.presentation)
        }
        
        androidMain.dependencies {
            implementation(projects.features.myfeature.ui)
            implementation(projects.core.navigation)
        }
        
        jvmMain.dependencies {
            implementation(projects.features.myfeature.ui)
            implementation(projects.core.navigation)
        }
    }
}
```

#### 3. Register in `settings.gradle.kts`
```kotlin
include(":features:myfeature:api")
include(":features:myfeature:data")
include(":features:myfeature:presentation")
include(":features:myfeature:ui")
include(":features:myfeature:wiring")
```

#### 4. Export to iOS (if needed)

**`shared/build.gradle.kts`**:
```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            // Export API and presentation (ViewModels for iOS)
            api(projects.features.myfeature.api)
            api(projects.features.myfeature.presentation)
            
            // ❌ Do NOT export:
            // - :data (internal implementation)
            // - :ui (Compose-specific, SwiftUI implements separately)
            // - :wiring (DI assembly)
        }
    }
}
```

#### 5. Verify
```bash
./gradlew projects
./gradlew :features:myfeature:api:build
./gradlew :composeApp:assembleDebug test --continue
```

---

## Implementation Details

### Code Reduction Benefits

#### Before (Old Pattern)
Each of 5 feature plugins duplicated:
- 45 lines of KMP target configuration
- 15 lines of Android library setup
- 20 lines of test configuration

**Total duplication**: ~400 lines across 5 plugins

#### After (New Pattern)
- Base plugin: 70 lines (provides common setup)
- Feature plugins: 5-15 lines each (apply base + layer-specific config)
- Shared utilities: 120 lines (reusable functions)

**Total**: ~250 lines, **reduction of ~150 lines (38%)**

### Benefits Summary

#### Maintainability
✅ **Single source of truth** for KMP target configuration  
✅ **Centralized test setup** - one place to change test behavior  
✅ **Easier updates** - changing KMP targets only requires updating one function

#### Consistency
✅ **Guaranteed consistency** across all feature modules  
✅ **Common dependencies** automatically included  
✅ **Standardized patterns** following Now in Android conventions

#### Developer Experience
✅ **Less boilerplate** when creating new features  
✅ **Clear plugin hierarchy** - base provides foundation, layers add specifics  
✅ **Better discoverability** - shared utilities in dedicated package

---

## Troubleshooting

### Plugin not found
**Symptom**: `Plugin with id 'convention.feature.base' not found`

**Solution**: 
1. Verify `pluginManagement { includeBuild("build-logic") }` in root `settings.gradle.kts`
2. Rebuild: `./gradlew :build-logic:convention:build`
3. Sync: `./gradlew projects`

---

### Version catalog not accessible
**Symptom**: Build error referencing `libs` in convention plugin

**Solution**: Check `build-logic/settings.gradle.kts` references parent version catalog:
```kotlin
versionCatalogs {
    create("libs") {
        from(files("../gradle/libs.versions.toml"))
    }
}
```

---

### "Unresolved reference: getLibrary"
**Symptom**: Cannot resolve extension function in plugin

**Solution**: Ensure the shared extensions exist in build-logic and are imported where used. If your docs tooling forbids `import` lines in code blocks, reference them inline instead: `com.minddistrict.multiplatformpoc.getLibrary`, `getVersion`, `libs`.

---

### UI module includes iOS targets unexpectedly
**Symptom**: UI module builds for iOS when it shouldn't

**Solution**: Verify using `convention.feature.ui` (not `convention.feature.impl`) which explicitly configures Android + JVM + iOS targets. For Android/JVM only, manually configure targets.

---

### Core module includes feature dependencies
**Symptom**: Core module has unwanted dependencies from base plugin

**Solution**: Use `convention.core.library` (not `convention.feature.base`) and add dependencies explicitly

---

## Validation Commands

**Compile convention plugins**:
```bash
./gradlew :build-logic:convention:build
```

**Verify project structure**:
```bash
./gradlew projects
```

**Primary validation (ALWAYS use this)**:
```bash
./gradlew :composeApp:assembleDebug test --continue  # ~45 seconds
```

**Run tests**:
```bash
./gradlew :composeApp:testDebugUnitTest
```

**❌ NEVER run iOS builds for routine validation**:
- iOS builds take 5-10 minutes
- Only run when explicitly needed for iOS-specific work
- Exception: When working on iosAppCompose (Compose iOS)

---

## Related Documentation

- **Canonical pattern rules**: [critical_patterns_quick_ref.md](critical_patterns_quick_ref.md#convention-plugins-pattern)
- **Project conventions**: [conventions.md](conventions.md)
- **Koin DI patterns**: [koin_di_quick_ref.md](koin_di_quick_ref.md)
- **Version catalog**: `gradle/libs.versions.toml`
- **Copilot instructions**: `.github/copilot-instructions.md`
- **Agent instructions**: `AGENTS.md`

---

**Implementation Date**: November 25, 2025  
**Architecture Update**: November 26, 2025  
**Inspired By**: [Now in Android](https://github.com/android/nowinandroid) convention plugin architecture
