# Troubleshooting Gradle Convention Plugins

Common issues and their solutions.

## Plugin Not Found
**Error**: `Plugin with id 'convention.feature.base' not found`

**Causes**:
- Missing `includeBuild("build-logic")` in root `settings.gradle.kts`.
- Gradle sync issue.

**Fix**:
1. Verify root `settings.gradle.kts`.
2. Run `./gradlew :build-logic:convention:build`.
3. Run `./gradlew projects` to force sync.

---

## Version Catalog Not Accessible
**Error**: Build error referencing `libs` inside a convention plugin.

**Fix**: Ensure `build-logic/settings.gradle.kts` explicitly references the parent version catalog:
```kotlin
versionCatalogs {
    create("libs") {
        from(files("../gradle/libs.versions.toml"))
    }
}
```

---

## Unresolved Reference: `getLibrary` or `libs`
**Error**: Cannot resolve extension functions in the plugin file.

**Fix**: Convention plugins must import the utilities from the `com.minddistrict.multiplatformpoc` package. If imports are restricted, use the fully qualified name.

---

## Unexpected iOS Targets
**Symptom**: A module is building for iOS when it should be platform-specific.

**Fix**:
- `convention.feature.base` and its derivatives always include iOS targets by default.
- If you need a module that is ONLY Android/JVM, do not use the feature plugins; instead, use `convention.android.library` or configure targets manually.

---

## core Module Dependency Bloat
**Symptom**: A `:core` module has Arrow, Coroutines, and other feature dependencies it doesn't need.

**Fix**: Use `convention.core.library` instead of `convention.feature.base`. The core plugin provides KMP targets and testing setup but does NOT include common feature dependencies.

---

## Unresolved Reference Errors (Despite Correct Imports)

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

## "Unresolved reference 'generated'" for Compose Resources

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

## Navigation Provider "Unresolved reference" Errors

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

## Before Debugging Import Errors

1. **Check actual file:** Verify imports are present
2. **Try clean build first:** Often resolves stale cache
3. **Check package names:** Verify generated resource packages
