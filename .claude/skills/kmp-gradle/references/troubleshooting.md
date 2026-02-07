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
