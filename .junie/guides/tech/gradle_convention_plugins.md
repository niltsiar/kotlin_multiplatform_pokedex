# Gradle Convention Plugins Guidelines

Purpose: Standardize and centralize Gradle configuration across modules using Convention Plugins. Keep module build scripts minimal and consistent, enable easy adoption of Compose Multiplatform, Metro DI, Kotest/MockK, and Roborazzi.

## Why Convention Plugins
- Single source of truth for Kotlin, Compose, Android, testing, linting, and DI configuration.
- Reduce copy/paste and drift; make new modules trivial to add.
- Enforce module roles (feature api/data/presentation/wiring) with tailored defaults.

## Repository Layout (example)
```text
build-logic/
  settings.gradle.kts
  build.gradle.kts          # publishes convention plugins to included build
  gradle/libs.versions.toml # version catalog for build-logic
  convention/
    build.gradle.kts
    src/main/kotlin/
      ConventionKmpLibraryPlugin.kt
      ConventionAndroidAppPlugin.kt
      ConventionAndroidLibraryPlugin.kt
      ConventionFeatureApiPlugin.kt
      ConventionFeatureDataPlugin.kt
      ConventionFeaturePresentationPlugin.kt
      ConventionFeatureWiringPlugin.kt
```

Include build-logic in the root `settings.gradle.kts`:
```kotlin
pluginManagement {
  includeBuild("build-logic")
}
```

## Suggested Plugin IDs
- `convention.kmp.library` — Generic Kotlin Multiplatform library
- `convention.android.app` — Android application (Compose enabled)
- `convention.android.library` — Android library (Compose enabled)
- `convention.feature.api` — Feature API (MPP/KMP, no Android plugin by default)
- `convention.feature.data` — Feature Data (MPP/KMP, Ktor/SQL settings optional)
- `convention.feature.presentation` — Feature Presentation (Compose MPP)
- `convention.feature.wiring` — Feature Wiring/Aggregation (MPP, Metro/KSP where needed)

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
        }
      }
    }
  }
}
```

Feature wiring (Metro/KSP):
```kotlin
class ConventionFeatureWiringPlugin : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    pluginManager.apply("convention.kmp.library")
    // If Metro requires KSP, you can apply it here for JVM/Android
    pluginManager.apply("com.google.devtools.ksp")
    // Add metro processors/annotations via catalog
    dependencies.add("kspJvm", libs.metro.ksp)
    dependencies.add("implementation", libs.metro.annotations)
  }
}
```

Version Catalog (snippets in `build-logic/gradle/libs.versions.toml`):
```toml
[versions]
compose-compiler = "1.5.15"

[libraries]
kotest-assertions = "io.kotest:kotest-assertions-core:5.9.1"
kotest-framework  = "io.kotest:kotest-framework-engine:5.9.1"
kotest-property   = "io.kotest:kotest-property:5.9.1"
mockk             = "io.mockk:mockk:1.13.12"
roborazzi-core    = "io.github.takahirom.roborazzi:roborazzi:1.0.0"
roborazzi-compose = "io.github.takahirom.roborazzi:roborazzi-compose:1.0.0"
roborazzi-junit   = "io.github.takahirom.roborazzi:roborazzi-junit-rule:1.0.0"
metro-annotations = "dev.zacsweers.metro:metro-annotations:<version>"
metro-ksp         = "dev.zacsweers.metro:metro-compiler-ksp:<version>"
```

## Applying Plugins in Modules
In a feature presentation module:
```kotlin
plugins { id("convention.feature.presentation") }

dependencies {
  implementation(project(":core:designsystem"))
  implementation(project(":features:profile:api"))
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
