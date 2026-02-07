# Module Creation Workflow

Step-by-step guide for creating a new vertical slice feature.

## 1. Create Directory Structure
Use a single command to create the standard 5-module structure:

```bash
mkdir -p features/myfeature/{api,data,presentation,ui,wiring}/src/{commonMain,commonTest}/kotlin
```

## 2. Implement Build Files

### API Module (`api/build.gradle.kts`)
```kotlin
plugins {
    id("convention.feature.api")
}
```

### Data Module (`data/build.gradle.kts`)
```kotlin
plugins {
    id("convention.feature.data")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.features.myfeature.api)
            implementation(projects.core.httpclient)
        }
    }
}
```

### Presentation Module (`presentation/build.gradle.kts`)
```kotlin
plugins {
    id("convention.feature.presentation")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.features.myfeature.api)
            implementation(projects.features.myfeature.data)
        }
    }
}
```

### UI Module (`ui/build.gradle.kts`)
```kotlin
plugins {
    id("convention.feature.ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.features.myfeature.api)
            implementation(projects.features.myfeature.presentation)
            implementation(projects.core.designsystem)
        }
    }
}
```

### Wiring Module (`wiring/build.gradle.kts`)
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
            // Implementation dependencies for wiring
            androidMain.dependencies { implementation(projects.features.myfeature.ui) }
            jvmMain.dependencies { implementation(projects.features.myfeature.ui) }
        }
    }
}
```

## 3. Register Modules
Add the modules to the root `settings.gradle.kts`:

```kotlin
include(":features:myfeature:api")
include(":features:myfeature:data")
include(":features:myfeature:presentation")
include(":features:myfeature:ui")
include(":features:myfeature:wiring")
```

## 4. Export for iOS
In `shared/build.gradle.kts`, export only what the native app needs:

```kotlin
commonMain.dependencies {
    api(projects.features.myfeature.api)
    api(projects.features.myfeature.presentation)
}
```

## 5. Validation
Run the primary validation command:

```bash
./gradlew :composeApp:assembleDebug test --continue
```
