# Convention Plugin Quick Reference - Updated

## Available Convention Plugins

### Base Plugin
```kotlin
plugins {
    id("convention.feature.base")
}
```
**Provides:**
- KMP targets: Android, JVM, iOS
- Android library configuration
- Test configuration (JUnit Platform, logging)
- Common dependencies: Arrow, Coroutines, Immutable Collections

**Use when:** Creating new feature modules (api, data, presentation, wiring)

---

### Feature Layer Plugins

#### Feature API Plugin
```kotlin
plugins {
    id("convention.feature.api")  // Composes: convention.feature.base
}
```
**For:** Public interfaces, domain models, navigation contracts  
**Exports to iOS:** ✅ Yes (via :shared umbrella)  
**Targets:** Android, JVM, iOS

#### Feature Implementation Plugin
```kotlin
plugins {
    id("convention.feature.impl")  // Composes: convention.feature.base
}
```
**For:** 
- **Data modules:** Repositories, API services, DTOs, mappers
- **Presentation modules:** ViewModels, UI state

**Exports to iOS:** 
- Data: ❌ No
- Presentation: ✅ Yes (via :shared umbrella)

**Targets:** Android, JVM, iOS

#### Feature UI Plugin
```kotlin
plugins {
    id("convention.feature.ui")  // Does NOT compose base (explicit targets)
    // Automatically applies: convention.compose.multiplatform
}
```
**For:** Compose Multiplatform screens (@Composable functions)  
**Exports to iOS:** ❌ No (iOS uses native SwiftUI)  
**Targets:** Android, JVM only (no iOS)  
**Note:** Includes Arrow, Coroutines, Collections directly

#### Feature Wiring Plugin
```kotlin
plugins {
    id("convention.feature.wiring")  // Composes: convention.feature.base
}
```
**For:** Metro DI @Provides functions, dependency graphs  
**Exports to iOS:** ❌ No  
**Targets:** Android, JVM, iOS

---

### Other Plugins

#### Core Library Plugin
```kotlin
plugins {
    id("convention.core.library")  // Uses shared functions, NOT base
}
```
**For:** Core KMP libraries (httpclient, database, util)  
**Targets:** Android, JVM, iOS  
**Note:** Does NOT include common dependencies - add explicitly

#### KMP Library Plugin
```kotlin
plugins {
    id("convention.kmp.library")
}
```
**For:** Pure KMP libraries without Android  
**Targets:** JVM, iOS (no Android)

#### Compose Multiplatform Plugin
```kotlin
plugins {
    id("convention.compose.multiplatform")
}
```
**For:** Adding Compose Multiplatform dependencies  
**Auto-applied by:** `convention.feature.ui`

---

## Shared Configuration Functions

These are used internally by plugins but can be called directly if needed:

### `configureKmpTargets()`
```kotlin
import com.minddistrict.multiplatformpoc.configureKmpTargets

extensions.configure<KotlinMultiplatformExtension> {
    configureKmpTargets(this, includeIos = true)
}
```
**Configures:** Android, JVM, and optionally iOS targets with JVM 11

### `configureTests()`
```kotlin
import com.minddistrict.multiplatformpoc.configureTests

configureTests()  // Sets up JUnit Platform and test logging
```

### `libs` Extension
```kotlin
import com.minddistrict.multiplatformpoc.libs

val libs = libs  // Cleaner access to version catalog
implementation(libs.getLibrary("arrow-core"))
```

---

## Creating New Feature Modules

### API Module
```kotlin
// features/myfeature/api/build.gradle.kts
plugins {
    id("convention.feature.api")
}

// Add API-specific dependencies if needed
kotlin {
    sourceSets {
        commonMain.dependencies {
            // Only if this API needs extra dependencies
        }
    }
}
```

### Data Module
```kotlin
// features/myfeature/data/build.gradle.kts
plugins {
    id("convention.feature.impl")
    alias(libs.plugins.kotlinx.serialization)  // If using serialization
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.features.myfeature.api)
            implementation(projects.core.httpclient)
            
            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.json)
            
            // Arrow, Coroutines, Collections are already provided by base plugin
        }
    }
}
```

### Presentation Module
```kotlin
// features/myfeature/presentation/build.gradle.kts
plugins {
    id("convention.feature.impl")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.features.myfeature.api)
            implementation(projects.features.myfeature.data)
            
            // Lifecycle & ViewModel
            implementation(libs.androidx.lifecycle.viewmodel)
            
            // Arrow, Coroutines, Collections are already provided by base plugin
        }
    }
}
```

### UI Module
```kotlin
// features/myfeature/ui/build.gradle.kts
plugins {
    id("convention.feature.ui")  // Automatically includes Compose
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.features.myfeature.api)
            implementation(projects.features.myfeature.presentation)
            
            // Compose dependencies are automatically included
            // Arrow, Coroutines, Collections are automatically included
        }
    }
}
```

### Wiring Module
```kotlin
// features/myfeature/wiring/build.gradle.kts
plugins {
    id("convention.feature.wiring")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.features.myfeature.api)
            implementation(projects.features.myfeature.data)
            implementation(projects.features.myfeature.presentation)
            
            // TODO: Metro DI dependencies when added
        }
    }
}
```

---

## Dependencies Automatically Included

### By `convention.feature.base`
✅ `arrow-core` - Functional error handling  
✅ `kotlinx-coroutines-core` - Async operations  
✅ `kotlinx-collections-immutable` - UI state collections  
✅ `kotlin-test` - Testing (in commonTest)

### By `convention.feature.ui`
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

---

## Plugin Composition Hierarchy

```
convention.feature.base
├── convention.feature.api (composes base)
├── convention.feature.impl (composes base)
│   ├── Used by: :features:*/data
│   └── Used by: :features:*/presentation
└── convention.feature.wiring (composes base)

convention.feature.ui (standalone, does NOT compose base)
├── Applies: convention.compose.multiplatform
└── Includes: Arrow, Coroutines, Collections directly

convention.core.library (standalone, uses shared functions)
└── Uses: configureKmpTargets(), configureTests()
```

---

## When to Use Which Plugin?

| Module Type | Plugin | Exports to iOS |
|-------------|--------|----------------|
| Feature API | `convention.feature.api` | ✅ Yes |
| Feature Data | `convention.feature.impl` | ❌ No |
| Feature Presentation | `convention.feature.impl` | ✅ Yes |
| Feature UI | `convention.feature.ui` | ❌ No |
| Feature Wiring | `convention.feature.wiring` | ❌ No |
| Core Library | `convention.core.library` | ✅ Yes |
| Pure KMP | `convention.kmp.library` | N/A |

---

## Benefits of New Structure

### For Developers
✅ Less boilerplate when creating new features  
✅ Automatic dependency inclusion (Arrow, Coroutines, Collections)  
✅ Guaranteed consistency across modules  
✅ Clear plugin hierarchy

### For Maintenance
✅ Single source of truth for KMP targets  
✅ Centralized test configuration  
✅ Easy to update all modules at once  
✅ ~38% reduction in duplicated code

---

## Migration from Old Pattern

### Before (Old Pattern - Manual Configuration)
```kotlin
plugins {
    id("convention.feature.api")
}

kotlin {
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
    }
    jvm {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
    }
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.arrow.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.collections.immutable)
        }
    }
}
```

### After (New Pattern - Automatic via Base Plugin)
```kotlin
plugins {
    id("convention.feature.api")  // That's it! Base config automatic
}

// Only add module-specific dependencies if needed
```

---

## Common Patterns

### Adding Layer-Specific Dependencies
```kotlin
plugins {
    id("convention.feature.impl")  // Gets base dependencies
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Add only layer-specific dependencies
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
```

### Excluding iOS from a Module
```kotlin
// Use convention.feature.ui for Compose UI modules (Android + JVM only)
// Or manually configure targets in other cases
```

### Testing Configuration
```kotlin
// Testing is automatically configured by base plugin
// Just add test dependencies:
kotlin {
    sourceSets {
        androidUnitTest.dependencies {
            implementation(libs.kotest.assertions)
            implementation(libs.mockk)
        }
    }
}
```

---

## Troubleshooting

### Issue: "Unresolved reference: getLibrary"
**Solution:** Import at top of file:
```kotlin
import getLibrary
import getVersion
```

### Issue: "Plugin not found: convention.feature.base"
**Solution:** Ensure you've synced Gradle after plugin registration in `build-logic/convention/build.gradle.kts`

### Issue: UI module includes iOS targets
**Solution:** Use `convention.feature.ui` which explicitly configures Android + JVM only

### Issue: Core module includes feature dependencies
**Solution:** Use `convention.core.library` (not `convention.feature.base`) and add dependencies explicitly

---

**Last Updated:** November 25, 2025  
**See Also:** `docs/convention-plugins-improvements.md` for implementation details
