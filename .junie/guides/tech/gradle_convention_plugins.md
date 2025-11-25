# Gradle Convention Plugins Guidelines

Purpose: Standardize and centralize Gradle configuration across modules using Convention Plugins. Keep module build scripts minimal and consistent, enable easy adoption of Compose Multiplatform, Metro DI, Kotest/MockK, Roborazzi, Navigation 3, and code quality tools (detekt/ktlint).

## Why Convention Plugins
- Single source of truth for Kotlin, Compose, Android, testing, linting, and DI configuration.
- Reduce copy/paste and drift; make new modules trivial to add.
- Enforce module roles (feature api/data/presentation/wiring) with tailored defaults.

## Architecture (Updated November 2025)

Following patterns from [Now in Android](https://github.com/android/nowinandroid), we use:
1. **Shared configuration utilities** in `com.minddistrict.multiplatformpoc` package
2. **Base plugin composition** to eliminate duplication
3. **Extension functions** for version catalog access

See `convention_plugins_quick_ref.md` and `convention_plugins_improvements.md` for details.

## Dependency Management vs Convention Plugins

**Convention plugins** are for module-level build configuration (targets, source sets, testing frameworks).

**Dependency version management** is handled at the **root project level**:
- **Version Catalog**: `gradle/libs.versions.toml` - centralized dependency versions
- **Ben Manes Versions Plugin**: Root `build.gradle.kts` - automated update checking with stability rules
- **Check updates**: `./gradlew dependencyUpdates` - see available dependency and Gradle wrapper updates
- **Report**: `build/dependencyUpdates/report.html`

Convention plugins consume the version catalog but do NOT manage version updates themselves.

## Repository Layout (current)
```text
build-logic/
  settings.gradle.kts
  build.gradle.kts          # publishes convention plugins to included build
  convention/
    build.gradle.kts
    src/main/kotlin/
      # Plugin files
      ConventionKmpLibraryPlugin.kt
      ConventionAndroidAppPlugin.kt
      ConventionAndroidLibraryPlugin.kt
      ConventionFeatureBasePlugin.kt     # ← Base plugin for features
      ConventionFeatureApiPlugin.kt
      ConventionFeatureImplPlugin.kt     # ← Used by data & presentation
      ConventionFeatureUiPlugin.kt
      ConventionFeatureWiringPlugin.kt
      ConventionCoreLibraryPlugin.kt
      
      # Shared configuration utilities
      com/minddistrict/multiplatformpoc/
        KotlinMultiplatform.kt           # ← configureKmpTargets()
        TestConfiguration.kt             # ← configureTests()
        ComposeConfiguration.kt          # ← configureComposeMultiplatform()
        ProjectExtensions.kt             # ← libs property, getVersion(), getLibrary()
```

Include build-logic in the root `settings.gradle.kts`:
```kotlin
pluginManagement {
  includeBuild("build-logic")
}
```

## Current Plugin IDs
- `convention.kmp.library` — Generic Kotlin Multiplatform library
- `convention.android.app` — Android application (Compose enabled)
- `convention.android.library` — Android library (Compose enabled)
- `convention.compose.multiplatform` — Compose Multiplatform dependencies
- `convention.feature.base` — **Base plugin for all feature modules** (provides KMP targets, tests, common deps)
- `convention.feature.api` — Feature API modules (composes base)
- `convention.feature.impl` — Feature data/presentation modules (composes base)
- `convention.feature.ui` — Feature UI Layer (MPP Android+JVM; Compose Multiplatform)
- `convention.feature.wiring` — Feature Wiring/Aggregation (MPP, Metro DI with K2 compiler plugin)
- `convention.core.library` — Core/Shared libraries (MPP, all targets)
- `convention.feature.api` — Feature API (MPP/KMP, no Android plugin by default)
- `convention.feature.data` — Feature Data (MPP/KMP, Ktor/SQL settings optional)
- `convention.feature.presentation` — Feature Presentation (Compose MPP)
- `convention.feature.wiring` — Feature Wiring/Aggregation (MPP, Metro/KSP where needed)
- `convention.quality` — Code quality (detekt + ktlint) applied repository‑wide

## Common Configuration in Plugins (examples)

KMP baseline (in `ConventionKmpLibraryPlugin.kt`):
```kotlin
class ConventionKmpLibraryPlugin : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    pluginManager.apply("org.jetbrains.kotlin.multiplatform")
    extensions.configure<KotlinMultiplatformExtension>("kotlin") {
      jvm()
      iosArm64(); iosSimulatorArm64(); iosX64()
      sourceSets {
        val commonMain by getting {
          dependencies {
            // Compose MPP UI can be added in presentation plugin
          }
        }
        val commonTest by getting {
          dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotest.assertions)
            implementation(libs.kotest.framework)
            implementation(libs.kotest.property)
          }
        }
        val jvmTest by getting {
          dependencies {
            implementation(libs.mockk)
            implementation(libs.roborazzi.core)
            implementation(libs.roborazzi.compose)
            implementation(libs.roborazzi.junit)
          }
        }
      }
    }
  }
}
```

Android app/library (Compose) baseline:
```kotlin
class ConventionAndroidAppPlugin : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    pluginManager.apply("com.android.application")
    pluginManager.apply("org.jetbrains.kotlin.android")
    pluginManager.apply("org.jetbrains.compose")
    extensions.configure<ApplicationExtension>("android") {
      compileSdk = 35
      defaultConfig { minSdk = 24; targetSdk = 35 }
      testOptions { unitTests.isIncludeAndroidResources = true }
      buildFeatures { compose = true }
      composeOptions { kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get() }
    }
    dependencies.add("implementation", platform(libs.compose.bom))
  }
}
```

Feature presentation (Compose MPP):
```kotlin
class ConventionFeaturePresentationPlugin : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    pluginManager.apply("convention.kmp.library")
    pluginManager.apply("org.jetbrains.compose")
    extensions.configure<KotlinMultiplatformExtension>("kotlin") {
      jvm() // for Desktop
      sourceSets.named("commonMain") {
        dependencies {
          implementation(compose.runtime)
          implementation(compose.foundation)
          implementation(compose.material3)
          // Navigation 3 UI for MPP
          implementation(libs.navigation3.ui)
          // Immutable collections and datetime for UI state
          implementation(libs.kotlinx.immutable)
          implementation(libs.kotlinx.datetime)
          // Lifecycle ViewModel (KMP)
          implementation(libs.lifecycle.viewmodel)
        }
      }
    }
  }
}
```

Feature wiring (Metro DI):
```kotlin
class ConventionFeatureWiringPlugin : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    pluginManager.apply("convention.kmp.library")
    // Metro uses Kotlin compiler plugin (NOT KSP)
    pluginManager.apply("dev.zacsweers.metro")
    // Metro runtime is added automatically by the plugin
  }
}
```

**Note**: Metro uses the K2 compiler plugin for code generation, NOT KSP. See [metro_di_quick_ref.md](metro_di_quick_ref.md) for patterns.

Code quality (detekt + ktlint):
```kotlin
class ConventionQualityPlugin : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    pluginManager.apply("io.gitlab.arturbosch.detekt")
    pluginManager.apply("org.jlleitschuh.gradle.ktlint")

    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension>("detekt") {
      buildUponDefaultConfig = true
      allRules = false
      config.setFrom(files("${rootDir}/config/detekt/detekt.yml"))
    }

    extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension>("ktlint") {
      android.set(true)
      outputToConsole.set(true)
      ignoreFailures.set(false)
    }
  }
}
```

Version Catalog (snippets in `build-logic/gradle/libs.versions.toml`):
```toml
[versions]
compose-compiler = "1.5.15"
compose-mpp = "1.10.0-beta02" # CMP plugin version
navigation3 = "1.0.0-alpha05"
lifecycle-nav3 = "2.10.0-alpha05"
lifecycle = "2.10.0-alpha05"
adaptive-nav3 = "1.3.0-alpha02"
kotlinx-immutable = "0.3.7"
kotlinx-datetime = "0.6.1"
assertk = "0.28.0"

[libraries]
kotest-assertions = "io.kotest:kotest-assertions-core:5.9.1"
kotest-framework  = "io.kotest:kotest-framework-engine:5.9.1"
kotest-property   = "io.kotest:kotest-property:5.9.1"
kotest-assertions-json = "io.kotest:kotest-assertions-json:5.9.1"
kotest-assertions-kotlinx-datetime = "io.kotest:kotest-assertions-kotlinx-datetime:5.9.1"
mockk             = "io.mockk:mockk:1.13.12"
assertk-jvm       = "com.willowtreeapps.assertk:assertk-jvm:${versions.assertk}"
roborazzi-core    = "io.github.takahirom.roborazzi:roborazzi:1.0.0"
roborazzi-compose = "io.github.takahirom.roborazzi:roborazzi-compose:1.0.0"
roborazzi-junit   = "io.github.takahirom.roborazzi:roborazzi-junit-rule:1.0.0"
metro-annotations = "dev.zacsweers.metro:metro-annotations:<version>"
metro-ksp         = "dev.zacsweers.metro:metro-compiler-ksp:<version>"
kotlinx-immutable = "org.jetbrains.kotlinx:kotlinx-collections-immutable:${versions.kotlinx-immutable}"
kotlinx-datetime  = "org.jetbrains.kotlinx:kotlinx-datetime:${versions.kotlinx-datetime}"
navigation3-ui    = "org.jetbrains.androidx.navigation3:navigation3-ui:${versions.navigation3}"
lifecycle-viewmodel-navigation3 = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3:${versions.lifecycle-nav3}"
adaptive-navigation3 = "org.jetbrains.compose.material3.adaptive:adaptive-navigation3:${versions.adaptive-nav3}"
lifecycle-viewmodel = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel:${versions.lifecycle}"
lifecycle-viewmodel-testing = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-testing:${versions.lifecycle}"
```

## Applying Plugins in Modules
In a feature presentation module:
```kotlin
plugins { id("convention.feature.presentation") }

dependencies {
  implementation(project(":core:designsystem"))
  implementation(project(":features:profile:api"))
  implementation(libs.navigation3.ui)
  implementation(libs.kotlinx.immutable)
  implementation(libs.kotlinx.datetime)
}
```

In a feature wiring module:
```kotlin
plugins { id("convention.feature.wiring") }

dependencies {
  implementation(project(":features:profile:impl")) // assemble impls here
  implementation(project(":features:profile:api"))
}
```

In the Android app module:
```kotlin
plugins { id("convention.android.app") }

android { namespace = "com.example.app" }

dependencies {
  implementation(project(":core:designsystem"))
  implementation(project(":wiring:navigation"))
  implementation(libs.navigation3.ui)
}
```

## Roborazzi Tasks (reference)
- Record: `./gradlew recordRoborazziDebug`
- Compare: `./gradlew compareRoborazziDebug`
- Verify: `./gradlew verifyRoborazziDebug`
- Desktop (optional): `recordRoborazziDesktop`, `compareRoborazziDesktop`, `verifyRoborazziDesktop`

## Policies
- All modules must apply one of our convention plugins; do not duplicate configuration in module scripts.
- The `shared` iOS umbrella exports only `api` modules; do not export `impl`, layer-specific, or `wiring` modules.
- Keep production classes DI-agnostic; assemble dependencies in wiring modules with provider functions.

## Notes
- Compose Multiplatform 1.10.0‑beta02 deprecates plugin dependency aliases like `compose.ui` in favor of direct coordinates. Prefer using the version catalog entries above.
- Apply `convention.quality` at the root to enable detekt and ktlint for all subprojects.
