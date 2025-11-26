# Gradle Convention Plugins - Implementation Guide

This document describes the **implemented** convention plugins in this project and how to use them.

## ✅ Implemented Plugins

### Location
All convention plugins are in: `build-logic/convention/src/main/kotlin/`

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
    // ... module plugins with apply false ...
    alias(libs.plugins.benManesVersions)  // ← Dependency update checking
}
```

**Dependency Management** (Root Level):
- **Version Catalog**: `gradle/libs.versions.toml` - All dependency versions
- **Ben Manes Versions Plugin**: Root `build.gradle.kts` - Automated update checking
- **Check updates**: `./gradlew dependencyUpdates`
- **Report**: `build/dependencyUpdates/report.html`
- **Stability Rules**: Stable versions stay stable; unstable upgrade within same minor version only

See `.junie/guides/tech/conventions.md` for detailed dependency management rules.

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
            from(files("../gradle/libs.versions.toml"))  // ← Shares parent version catalog
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
        register("conventionKmpLibrary") {
            id = "convention.kmp.library"
            implementationClass = "ConventionKmpLibraryPlugin"
        }
        register("conventionAndroidApp") {
            id = "convention.android.app"
            implementationClass = "ConventionAndroidAppPlugin"
        }
        register("conventionAndroidLibrary") {
            id = "convention.android.library"
            implementationClass = "ConventionAndroidLibraryPlugin"
        }
        register("conventionComposeMultiplatform") {
            id = "convention.compose.multiplatform"
            implementationClass = "ConventionComposeMultiplatformPlugin"
        }
        register("conventionFeatureApi") {
            id = "convention.feature.api"
            implementationClass = "ConventionFeatureApiPlugin"
        }
        register("conventionFeatureImpl") {
            id = "convention.feature.impl"
            implementationClass = "ConventionFeatureImplPlugin"
        }
        register("conventionFeatureWiring") {
            id = "convention.feature.wiring"
            implementationClass = "ConventionFeatureWiringPlugin"
        }
    }
}
```

---

## 📚 Plugin Reference

### 1. `convention.kmp.library`
Base Kotlin Multiplatform library configuration.

**What it does**:
- Applies `org.jetbrains.kotlin.multiplatform`
- Configures targets: `jvm`, `iosArm64`, `iosSimulatorArm64`, `iosX64`
- Sets JVM target to Java 11
- Adds `kotlin("test")` to `commonTest`

**When to use**: Any pure KMP library (core, utility, domain models)

**Example**:
```kotlin
// core/domain/build.gradle.kts
plugins {
    id("convention.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotest.framework.engine)
        }
    }
}
```

---

### 2. `convention.android.app`
Android application configuration.

**What it does**:
- Applies `com.android.application` and `org.jetbrains.kotlin.android`
- Configures Android SDK versions from version catalog
- Sets Java 11 compilation
- Sets Kotlin JVM target to 11
- Excludes `META-INF/{AL2.0,LGPL2.1}` from packaging

**When to use**: Android application modules

**Example**:
```kotlin
// composeApp/build.gradle.kts
plugins {
    id("convention.android.app")
    id("convention.compose.multiplatform")  // For Compose UI
}

android {
    namespace = "com.example.multiplatformpoc"
    
    defaultConfig {
        applicationId = "com.example.multiplatformpoc"
        versionCode = 1
        versionName = "1.0"
    }
}
```

---

### 3. `convention.android.library`
Android library configuration.

**What it does**:
- Applies `com.android.library` and `org.jetbrains.kotlin.android`
- Configures Android SDK versions from version catalog
- Sets Java 11 compilation
- Sets Kotlin JVM target to 11

**When to use**: Android-specific library modules (not needed for KMP modules)

**Example**:
```kotlin
// android/designsystem/build.gradle.kts
plugins {
    id("convention.android.library")
}

android {
    namespace = "com.example.designsystem"
}

dependencies {
    implementation(libs.androidx.core.ktx)
}
```

---

### 4. `convention.compose.multiplatform`
Compose Multiplatform UI configuration (Android + Desktop JVM + iOS).

**What it does**:
- Applies `org.jetbrains.compose` and `org.jetbrains.kotlin.plugin.compose`
- Adds Compose dependencies to `commonMain`: runtime, foundation, material3, ui, resources, tooling preview
- Adds lifecycle dependencies: ViewModel Compose, Runtime Compose
- Adds Android-specific: Compose preview, Activity Compose
- Adds Desktop/JVM: Desktop current OS runtime
- Adds iOS: Supports iosArm64, iosSimulatorArm64, iosX64 targets
- Adds debug: UI Tooling

**When to use**: Modules with Compose UI (Android + Desktop + iOS Compose)

**Example**:
```kotlin
// composeApp/build.gradle.kts
plugins {
    id("convention.android.app")
    id("convention.compose.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Compose dependencies already included by plugin
        }
    }
}
```

**Note**: This project has TWO iOS apps:
- **iosApp** (production): Native SwiftUI app using :shared framework
- **iosAppCompose** (experimental): Compose Multiplatform iOS app using ComposeApp.framework

---

### 5. `convention.feature.api`
Feature API module (public contracts).

**What it does**:
- Applies `convention.kmp.library` (transitively)
- Provides minimal base for public contracts

**When to use**: Feature API modules (interfaces, domain models, navigation contracts)

**Contents**:
- Public interfaces (repositories, use cases)
- Domain models shared across boundaries
- Navigation contracts

**Exported to iOS**: ✅ YES (via `:shared` umbrella)

**Example**:
```kotlin
// features/jobs/api/build.gradle.kts
plugins {
    id("convention.feature.api")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.arrow.core)  // For Either<Error, T>
        }
        commonTest.dependencies {
            implementation(libs.kotest.framework.engine)
            implementation(libs.kotest.assertions.core)
        }
    }
}
```

---

### 6. `convention.feature.impl`
Feature implementation module (internal implementations).

**What it does**:
- Applies `convention.kmp.library` (transitively)
- Provides base for data layer implementations

**When to use**: Feature implementation modules

**Contents**:
- Internal implementations of API contracts
- Repository implementations
- Data sources (network, database)
- DTO to domain mappers

**Exported to iOS**: ❌ NO (only `:api` modules exported)

**Example**:
```kotlin
// features/jobs/impl/build.gradle.kts
plugins {
    id("convention.feature.impl")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.features.jobs.api)  // Depend on API
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.arrow.core)
        }
        commonTest.dependencies {
            implementation(libs.kotest.framework.engine)
            implementation(libs.mockk)
        }
    }
}
```

---

### Wiring Plugin (Koin DI)

- Composes `convention.feature.base`
- Platform-specific source sets (commonMain, androidMain, jvmMain)
- Koin dependencies added automatically

See working implementation in `build-logic/convention/src/main/kotlin/com/minddistrict/multiplatformpoc/ConventionFeatureWiringPlugin.kt`

**When to use**: Feature wiring modules

**Contents**:
- Koin `module { }` DSL definitions
- Platform-specific navigation wiring (androidMain/jvmMain)
- Dependency aggregation

**Exported to iOS**: ❌ NO (DI wiring is internal)

**Example**:
```kotlin
// features/jobs/wiring/build.gradle.kts
plugins {
    id("convention.feature.wiring")  // Platform-specific source sets for Koin modules
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.features.jobs.api)
            implementation(projects.features.jobs.data)
            // Koin dependencies already included from base
        }
    }
}
```

---

## 🏗️ Creating New Modules

### Example: Create `:features:jobs:api`

**1. Create directory structure**:
```bash
mkdir -p features/jobs/api/src/{commonMain,commonTest,androidMain,iosMain}/kotlin
```

**2. Create `features/jobs/api/build.gradle.kts`**:
```kotlin
plugins {
    id("convention.feature.api")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.arrow.core)
        }
        
        commonTest.dependencies {
            implementation(libs.kotest.framework.engine)
            implementation(libs.kotest.assertions.core)
        }
    }
}
```

**3. Register in `settings.gradle.kts`**:
```kotlin
include(":features:jobs:api")
```

**4. Verify**:
```bash
./gradlew projects
./gradlew :features:jobs:api:build
```

---

## 📱 iOS Export Pattern

To export feature modules to iOS via `:shared` umbrella:

**`shared/build.gradle.kts`**:
```kotlin
kotlin {
    // Existing iOS targets...
    
    sourceSets {
        commonMain.dependencies {
            // Export API and core modules (use api() not implementation())
            api(projects.features.jobs.api)
            api(projects.core.domain)
            api(projects.core.util)
            
            // ❌ Do NOT export to native SwiftUI app (:shared framework):
            // - :impl modules (internal implementations)
            // - :wiring modules (DI assembly)
            // - :composeApp (used by iOS Compose app via ComposeApp.framework instead)
        }
    }
}
```

---

## 🔧 Modifying Convention Plugins

**Location**: `build-logic/convention/src/main/kotlin/`

**After modifying**:
1. Rebuild: `./gradlew :build-logic:convention:build`
2. Sync project: `./gradlew projects`
3. Validate: `./gradlew :composeApp:assembleDebug test --continue`

**⚠️ Never validate with iOS builds** (5-10min) - use Android builds (45s)

---

## 📖 Version Catalog References

Convention plugins read versions from `gradle/libs.versions.toml`:

**Required versions**:
```toml
[versions]
android-compileSdk = "36"
android-targetSdk = "36"
android-minSdk = "24"
agp = "8.9.0"
kotlin = "2.2.20"
compose = "1.9.1"

[libraries]
android-gradlePlugin = { module = "com.android.tools.build:gradle", version.ref = "agp" }
kotlin-gradlePlugin = { module = "org.jetbrains.kotlin:kotlin-gradle-plugin", version.ref = "kotlin" }
compose-gradlePlugin = { module = "org.jetbrains.compose:compose-gradle-plugin", version.ref = "compose" }

# Compose Multiplatform dependencies
androidx-lifecycle-viewmodelCompose = { group = "org.jetbrains.androidx.lifecycle", name = "lifecycle-viewmodel-compose", version = "2.10.0-alpha05" }
androidx-lifecycle-runtimeCompose = { group = "org.jetbrains.androidx.lifecycle", name = "lifecycle-runtime-compose", version = "2.10.0-alpha05" }
androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "androidx-activityCompose" }
```

---

## ✅ Validation Commands

**Compile convention plugins**:
```bash
./gradlew :build-logic:convention:build
```

**Verify project structure**:
```bash
./gradlew projects
```

**Validate Android build (ALWAYS use this for validation)**:
```bash
./gradlew :composeApp:assembleDebug test --continue
```

**Run tests**:
```bash
./gradlew :composeApp:testDebugUnitTest
```

**❌ NEVER run iOS builds for routine validation**:
- iOS builds take 5-10 minutes
- Only run when explicitly needed for iOS-specific work

---

## 🎯 Best Practices

1. **Keep plugins minimal** - Only shared configuration belongs in plugins
2. **Allow module customization** - Modules should extend plugin defaults
3. **Use version catalog** - All versions in `gradle/libs.versions.toml`
4. **Document changes** - Update this file when modifying plugins
5. **Validate frequently** - Run Android build after changes
6. **Avoid iOS builds** - Use Android builds for validation (45s vs 5-10min)
7. **Separate concerns**:
   - API modules: Public contracts only (exported to iOS)
   - Impl modules: Internal implementations (NOT exported to iOS)
   - Wiring modules: DI assembly (NOT exported to iOS)

---

## 🔍 Troubleshooting

### Plugin not found
**Symptom**: `Plugin with id 'convention.kmp.library' not found`

**Solution**: Verify `pluginManagement { includeBuild("build-logic") }` in root `settings.gradle.kts`

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

### Compose dependencies unresolved
**Symptom**: Cannot resolve compose extension in plugin

**Solution**: Ensure plugin order is correct:
```kotlin
// In module build.gradle.kts
plugins {
    id("convention.android.app")
    id("convention.compose.multiplatform")  // After android.app
}
```

---

## 📚 Related Documentation

- **Project conventions**: `.junie/guides/tech/conventions.md`
- **Plugin guidelines**: `.junie/guides/tech/gradle_convention_plugins.md`
- **Version catalog**: `gradle/libs.versions.toml`
- **Copilot instructions**: `.github/copilot-instructions.md`
- **Agent instructions**: `AGENTS.md`
