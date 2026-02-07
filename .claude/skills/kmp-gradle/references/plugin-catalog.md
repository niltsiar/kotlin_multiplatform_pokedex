# Plugin Catalog

Detailed list of available convention plugins and their specific use cases.

## Feature Base Plugin

### `convention.feature.base`
**The foundation for all feature modules.**

- **Provides**:
  - KMP targets: Android, JVM, iOS (Arm64, Simulator Arm64, X64)
  - Android library configuration (compileSdk, minSdk from version catalog)
  - Standardized test configuration (JUnit Platform, logging)
- **Auto-included Dependencies**:
  - `arrow-core`
  - `kotlinx-coroutines-core`
  - `kotlinx-collections-immutable`
  - `kotlin-test` (in commonTest)
- **Use Case**: Automatically applied by layer plugins. Use directly only for custom feature-related modules.

---

## Feature Layer Plugins

### `convention.feature.api`
**Public contracts and domain models.**

- **Composes**: `convention.feature.base`
- **Exports to iOS**: ✅ Yes
- **Contents**: Repository interfaces, domain models, navigation routes.
- **Decision Rule**: Every feature MUST have an `:api` module.

### `convention.feature.data`
**Repositories, API services, and DTOs.**

- **Composes**: `convention.feature.base`
- **Provides**: Ktor (core, contentNegotiation, logging), kotlinx-serialization (Json).
- **Exports to iOS**: ❌ No
- **Decision Rule**: Use for networking, database, and repository implementations.

### `convention.feature.presentation`
**ViewModels and UI State.**

- **Composes**: `convention.feature.base`
- **Provides**: `androidx.lifecycle:lifecycle-viewmodel` (KMP).
- **Exports to iOS**: ✅ Yes (ViewModels shared with SwiftUI)
- **Decision Rule**: Use for all ViewModel logic and UI state management.

### `convention.feature.ui`
**Compose Multiplatform screens.**

- **Composes**: `convention.feature.base` + `convention.compose.multiplatform`
- **Provides**: Full Compose Multiplatform stack.
- **Exports to iOS**: 
  - ✅ Yes (to iOS Compose app)
  - ❌ No (to native SwiftUI app)
- **Decision Rule**: Use for any module containing `@Composable` screens.

### `convention.feature.wiring`
**DI assembly and platform navigation registration.**

- **Composes**: `convention.feature.base`
- **Provides**: Koin dependencies.
- **Exports to iOS**: ❌ No
- **Decision Rule**: Use for Koin `module { }` definitions and Navigation 3 `EntryProviderInstaller`.

---

## Core & Utility Plugins

### `convention.core.library`
**Generic utilities and shared infrastructure.**

- **Standalone**: Uses shared functions, but does NOT apply `feature.base`.
- **Exports to iOS**: ✅ Yes
- **Decision Rule**: Use for `:core` modules that should NOT inherit feature dependencies (Arrow, etc.). Add dependencies explicitly.

### `convention.compose.multiplatform`
**Raw Compose Multiplatform configuration.**

- **Provides**: Runtime, foundation, material3, resources, preview.
- **Decision Rule**: Internal use, auto-applied by `feature.ui`.

### `convention.kmp.android.app`
**Main Android application configuration.**

- **Provides**: KMP targets + Android application setup.
- **Use Case**: Used by `:composeApp`.
