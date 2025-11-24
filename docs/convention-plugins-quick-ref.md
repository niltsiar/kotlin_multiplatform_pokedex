# Convention Plugins Quick Reference

## Available Plugins

| Plugin | Use For | Exports to iOS? |
|--------|---------|-----------------|
| `convention.kmp.library` | Base KMP library | Depends |
| `convention.android.app` | Android application | No |
| `convention.android.library` | Android library | No |
| `convention.compose.multiplatform` | Compose UI (Android+Desktop) | No |
| `convention.feature.api` | Feature contracts | ✅ Yes |
| `convention.feature.impl` | Feature implementations | ❌ No |
| `convention.feature.wiring` | DI assembly | ❌ No |

## Common Patterns

### Core/Utility Module
```kotlin
plugins {
    id("convention.kmp.library")
}
```

### Feature Module Set
```kotlin
// api/build.gradle.kts
plugins { id("convention.feature.api") }

// impl/build.gradle.kts  
plugins { id("convention.feature.impl") }

// wiring/build.gradle.kts
plugins { id("convention.feature.wiring") }
```

### Compose App Module
```kotlin
plugins {
    id("convention.android.app")
    id("convention.compose.multiplatform")
}
```

## Quick Commands

### Validate Changes
```bash
# Compile plugins
./gradlew :build-logic:convention:build

# Sync project
./gradlew projects

# Validate build (ALWAYS use Android, never iOS)
./gradlew :composeApp:assembleDebug
```

### Create New Feature Module
```bash
# 1. Create structure
mkdir -p features/myfeature/{api,impl,wiring}/src/{commonMain,commonTest}/kotlin

# 2. Create build.gradle.kts files with appropriate plugins

# 3. Register in settings.gradle.kts
include(":features:myfeature:api")
include(":features:myfeature:impl")
include(":features:myfeature:wiring")

# 4. Export API to iOS (in shared/build.gradle.kts)
# sourceSets.commonMain.dependencies {
#     api(projects.features.myfeature.api)
# }
```

## iOS Export Rules

**✅ Export (use `api()` in `:shared`)**:
- `:features:<feature>:api` modules
- `:core:*` modules (domain, util, etc.)

**❌ Never Export**:
- `:features:<feature>:impl` modules
- `:features:<feature>:wiring` modules
- `:composeApp` (Compose UI is Android/Desktop only)

## Platform UI

- **Android/Desktop**: Compose Multiplatform (shared)
- **iOS**: Native SwiftUI (separate)
- **Business Logic**: KMP modules (shared)

## Documentation

- Full guide: `.junie/guides/tech/gradle_convention_plugins_implementation.md`
- Summary: `docs/gradle-convention-plugins-summary.md`
- Guidelines: `.junie/guides/tech/gradle_convention_plugins.md`
