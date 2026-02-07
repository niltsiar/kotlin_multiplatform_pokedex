# Export Setup

Complete guide for configuring iOS framework exports for Kotlin Multiplatform.

## Overview

This repo ships **two iOS apps** that consume Kotlin Multiplatform code with **different boundaries**:

| App | UI | Framework | Export Rule |
|-----|-------|-----------|-------------|
| **iosApp** (production) | SwiftUI | Shared.framework | MUST remain Compose-free |
| **iosAppCompose** (experimental) | Compose Multiplatform | ComposeApp.framework | Is allowed to include Compose UI |

**This document covers `Shared.framework` export configuration for iosApp.**

## Core Rule

The core rule for the dual-iOS-app setup:

- `Shared.framework` must export only business logic that the SwiftUI app can consume.
- Compose UI code lives behind `ComposeApp.framework` only.

## What Gets Exported to iOS

### Via `:shared` umbrella framework (`shared/build.gradle.kts`)

```kotlin
kotlin {
    // Export only API and Presentation modules
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"

            // Export public contracts
            export(projects.features.pokemonlist.api)
            export(projects.features.pokemondetail.api)

            // Export presentation (ViewModels + UI state)
            export(projects.features.pokemonlist.presentation)
            export(projects.features.pokemondetail.presentation)

            // Export core utilities
            export(projects.core.domain)
            export(projects.core.util)
        }
    }
}

dependencies {
    commonMain.dependencies {
        // Dependencies for iOS wiring
        api(projects.core.di)
        api(projects.features.pokemonlist.wiring)
        api(projects.features.pokemondetail.wiring)
        implementation(libs.koin.core)
    }
}
```

### Exported Modules (accessible from Swift)

**Accessible from iOS:**
- ✅ `:features:<feature>:api` — interfaces, domain models, navigation contracts
- ✅ `:features:<feature>:presentation` — ViewModels, UI state sealed classes
- ✅ `:core:domain` — shared domain models
- ✅ `:core:util` — cross-platform utilities

### NOT Exported (internal to KMP)

**NOT accessible from iOS:**
- ❌ `:features:<feature>:data` — repositories, API services, DTOs, mappers
- ❌ `:features:<feature>:ui-material` — Compose UI (Android/Desktop only)
- ❌ `:features:<feature>:ui-unstyled` — Compose UI (Android/Desktop only)
- ❌ `:features:<feature>:wiring` — DI modules (Koin)
- ❌ `:core:designsystem` — Compose components
- ❌ `:core:httpclient` — Ktor client configuration

### Why This Split?

- iOS needs ViewModels and domain models (shared business logic)
- iOS uses native SwiftUI (not Compose UI)
- iOS accesses repositories via ViewModels (not directly)
- DI wiring happens in Kotlin (iOS just calls helper functions)

## Complete Export Configuration

### shared/build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.skie)
}

kotlin {
    // Export only API and Presentation modules
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"

            // Export public contracts
            export(projects.features.pokemonlist.api)
            export(projects.features.pokemondetail.api)

            // Export presentation (ViewModels + UI state)
            export(projects.features.pokemonlist.presentation)
            export(projects.features.pokemondetail.presentation)

            // Export core utilities
            export(projects.core.domain)
            export(projects.core.util)

            // Optional: Export lifecycle for ViewModel support
            export(libs.androidx.lifecycle.viewmodel)
            export(libs.androidx.lifecycle.runtime)
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Dependencies for iOS wiring
            api(projects.core.di)
            api(projects.features.pokemonlist.wiring)
            api(projects.features.pokemondetail.wiring)
            implementation(libs.koin.core)
        }

        iosMain.dependencies {
            // iOS-specific dependencies
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime)
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.shared"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
}
```

## Export Dependencies

### Required Exports for iOS

| Dependency | Purpose | Version |
|------------|---------|---------|
| `androidx.lifecycle:runtime` | LifecycleOwner, DefaultLifecycleObserver | 2.8.7 |
| `androidx.lifecycle:viewmodel` | ViewModel, ViewModelStore, SavedStateHandle | 2.8.7 |
| `koin-core` | Dependency injection | 3.5.6 |

### Module Exports

```kotlin
// Export feature APIs
export(projects.features.pokemonlist.api)
export(projects.features.pokemondetail.api)

// Export presentation (ViewModels)
export(projects.features.pokemonlist.presentation)
export(projects.features.pokemondetail.presentation)

// Export core utilities
export(projects.core.domain)
export(projects.core.util)
```

## ComposeApp.framework (for iosAppCompose)

### Export Configuration

```kotlin
// composeApp/build.gradle.kts
kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { target ->
        target.binaries.framework {
            baseName = "ComposeApp"

            // Export everything including Compose UI
            export(projects.features.pokemonlist.api)
            export(projects.features.pokemonlist.presentation)
            export(projects.features.pokemonlist.uiMaterial)
            export(projects.features.pokemonlist.uiUnstyled)

            export(projects.features.pokemondetail.api)
            export(projects.features.pokemondetail.presentation)
            export(projects.features.pokemondetail.uiMaterial)
            export(projects.features.pokemondetail.uiUnstyled)

            // Export design systems
            export(projects.core.designsystemCore)
            export(projects.core.designsystemMaterial)
            export(projects.core.designsystemUnstyled)

            // Export core utilities
            export(projects.core.domain)
            export(projects.core.util)

            // Export Compose dependencies
            export(libs.androidx.compose.runtime)
            export(libs.androidx.compose.foundation)
            export(libs.androidx.compose.material3)
            export(libs.androidx.compose.material3Adaptive)
            export(libs.androidx.navigation.compose)
        }
    }
}
```

**Difference**: `ComposeApp.framework` exports Compose UI modules and Compose dependencies.

## SKIE Configuration

### Plugin Setup

```kotlin
plugins {
    alias(libs.plugins.skie)
}
```

**SKIE Version**: `0.10.8` (compatible with Kotlin 2.2.21)

### What SKIE Provides

- **StateFlow → AsyncSequence**: Automatic bridging, no manual code needed
- **Suspend functions → async**: Native Swift concurrency support
- **Flow → AsyncSequence**: Collect Kotlin flows in Swift
- **Automatic keyword renames**: Handles Swift keyword conflicts (e.g., `Type` → `Type_`)

### SKIE Configuration (Optional)

SKIE works out-of-the-box for most use cases. Advanced configuration can be added:

```kotlin
skie {
    features {
        // Enable StateFlow bridging
        stateFlowEnabled = true

        // Enable Flow bridging
        flowEnabled = true

        // Enable suspend function bridging
        suspendEnabled = true
    }
}
```

## Boundary Validation

### Check Shared.framework is Compose-free

```bash
echo "Checking iosApp framework (must be Compose-free):"
nm -g shared/build/bin/iosArm64/debugFramework/Shared.framework/Shared \
  | grep -i "compose\|navigation" && echo "❌ VIOLATION: Compose/Navigation leaked to iosApp" || echo "✅ iosApp boundary clean"
```

### Check ComposeApp.framework has Compose

```bash
echo "Checking iosAppCompose framework (must have Compose):"
nm -g composeApp/build/bin/iosArm64/debugFramework/ComposeApp.framework/ComposeApp \
  | grep -i "compose" && echo "✅ iosAppCompose has Compose" || echo "❌ ERROR: Missing Compose in iosAppCompose"
```

## Export Guidelines

### ✅ DO Export

- **Feature APIs** - Interfaces, domain models, navigation contracts
- **Feature Presentation** - ViewModels, UI state sealed classes
- **Core Domain** - Shared domain models
- **Core Util** - Cross-platform utilities
- **Lifecycle** - LifecycleOwner, ViewModel, ViewModelStore

### ❌ DON'T Export

- **Feature Data** - Repositories, API services, DTOs, mappers
- **Feature UI** - Compose UI (only for ComposeApp.framework)
- **Feature Wiring** - DI modules (Koin)
- **Design System** - Compose components (only for ComposeApp.framework)
- **HTTP Client** - Ktor client configuration

## Troubleshooting

### Missing Symbols in Swift

**Symptom**: Swift compiler can't find Kotlin class

**Causes**:
- Module not exported
- Class not public (internal)
- SKIE renamed type (keyword conflict)

**Solution**:
1. Check module is exported in `shared/build.gradle.kts`
2. Ensure class is `public` (not `internal`)
3. Check for SKIE keyword renames (append `_`)

### Compose Symbols Leaked to Shared.framework

**Symptom**: Compose symbols found in `Shared.framework`

**Causes**:
- Accidentally exported Compose module
- Transitive dependency pulls in Compose

**Solution**:
1. Check `shared/build.gradle.kts` exports
2. Remove any `:ui-material` or `:ui-unstyled` exports
3. Check dependencies don't export Compose transitively

### ViewModel Not Found

**Symptom**: Swift can't find ViewModel class

**Causes**:
- Presentation module not exported
- ViewModel is internal

**Solution**:
```kotlin
// ✅ Export presentation module
export(projects.features.pokemonlist.presentation)

// ✅ Make ViewModel public
class PokemonListViewModel(
    private val repository: PokemonListRepository,
    // ...
) : ViewModel(...), DefaultLifecycleObserver {
    // ...
}
```

## Best Practices

### ✅ DO

1. **Export only necessary modules** - Minimize framework size
2. **Keep Shared.framework Compose-free** - For iosApp
3. **Export Compose for ComposeApp.framework** - For iosAppCompose
4. **Use SKIE for bridging** - Automatic StateFlow → AsyncSequence
5. **Make exported classes public** - Internal classes not accessible

### ❌ DON'T

1. **Don't export data layer** - Repositories, DTOs should stay internal
2. **Don't export Compose to Shared.framework** - Violates boundary rules
3. **Don't export DI modules** - Wiring happens in Kotlin
4. **Don't export HTTP client** - Ktor configuration stays internal
5. **Don't use internal classes from Swift** - Make them public or don't export

## Validation Commands

### Build iOS frameworks (CLI-friendly)

```bash
# iosApp (Shared.framework)
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64 :shared:linkDebugFrameworkIosArm64

# iosAppCompose (ComposeApp.framework)
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64 :composeApp:linkDebugFrameworkIosArm64
```

### Boundary checks (symbols)

```bash
# Check Shared.framework is Compose-free
nm -g shared/build/bin/iosArm64/debugFramework/Shared.framework/Shared \
  | grep -i "compose\|navigation" && echo "❌ VIOLATION" || echo "✅ Clean"

# Check ComposeApp.framework has Compose
nm -g composeApp/build/bin/iosArm64/debugFramework/ComposeApp.framework/ComposeApp \
  | grep -i "compose" && echo "✅ Has Compose" || echo "❌ Missing Compose"
```

### Xcode builds (milestones)

```bash
# Build iosApp
cd iosApp && xcodebuild -scheme iosApp -sdk iphonesimulator build CODE_SIGN_IDENTITY="" CODE_SIGNING_REQUIRED=NO

# Build iosAppCompose
cd iosAppCompose && xcodebuild -scheme iosAppCompose -sdk iphonesimulator build CODE_SIGN_IDENTITY="" CODE_SIGNING_REQUIRED=NO
```

### Primary validation

```bash
# Build Android app and run tests (always run first)
./gradlew :composeApp:assembleDebug test --continue
```

## References

- [Direct Integration Pattern](direct-integration.md) - Direct Integration guide
- [Lifecycle Bridging](lifecycle-bridging.md) - SwiftUI lifecycle management
- [SwiftUI Patterns](swiftui-patterns.md) - SwiftUI-specific patterns
- [ios_integration.md](See @kmp-ios skill) - Complete iOS integration guide
- [ios_apps_architecture.md](../../docs/tech/ios_apps_architecture.md) - Two iOS apps architecture
