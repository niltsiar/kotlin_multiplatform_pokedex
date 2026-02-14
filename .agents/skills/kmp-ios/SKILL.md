---
name: kmp-ios
description: "iOS integration patterns for SwiftUI + KMP ViewModels with Direct Integration, lifecycle bridging, export configuration, and iOS-specific patterns. Use when: (1) Creating iOS Views consuming KMP ViewModels, (2) Setting up lifecycle bridging between SwiftUI and KMP, (3) Configuring framework exports for iOS, (4) Implementing iOS-specific patterns like Koin DI helpers, (5) Troubleshooting iOS-KMP integration issues"
---

# KMP iOS Skill

iOS integration patterns for native SwiftUI consuming shared Kotlin Multiplatform ViewModels.

## When to Use This Skill

**MANDATORY**: Load this skill when working on:
- Creating or modifying iOS SwiftUI Views in `iosApp/iosApp`
- Implementing lifecycle bridging between SwiftUI and KMP
- Configuring `:shared` framework exports for iOS
- Creating Koin DI helper functions for iOS
- Troubleshooting iOS-KMP integration issues (StateFlow, SKIE, type conversions)

**Do NOT use for**: ViewModel implementation → use @kmp-presentation, Repository implementation → use @kmp-data-layer, DI configuration → use @kmp-di

## Mode Detection

| User Request | Reference File | Load When |
|--------------|----------------|-----------|
| "Create SwiftUI View" / "Implement iOS screen" | [swiftui-patterns.md](references/swiftui-patterns.md) | MANDATORY - Read before implementing |
| "Setup iOS ViewModel" / "Direct Integration" | [direct-integration.md](references/direct-integration.md) | MANDATORY - Read before implementing |
| "Configure lifecycle" / "Lifecycle bridging" | [lifecycle-bridging.md](references/lifecycle-bridging.md) | MANDATORY - Read before implementing |
| "Export to iOS" / "Configure :shared" | [export-setup.md](references/export-setup.md) | MANDATORY - Read before setting up exports |

**MANDATORY - READ ENTIRE FILE**: Before implementing SwiftUI Views consuming KMP ViewModels, you MUST read [swiftui-patterns.md](references/swiftui-patterns.md) (~519 lines) for StateFlow observation and platform patterns.

**MANDATORY - READ ENTIRE FILE**: Before setting up Direct Integration pattern, you MUST read [direct-integration.md](references/direct-integration.md) (~392 lines) for IosViewModelStoreOwner and lifecycle patterns.

**Do NOT load** `export-setup.md` for SwiftUI View implementation - only load when configuring :shared framework exports.
**Do NOT load** `lifecycle-bridging.md` unless working on custom lifecycle integration patterns.

## Core Principle: Direct Integration

**Philosophy**: Align with the [official Android KMP ViewModel guide](https://developer.android.com/kotlin/multiplatform/viewmodel). ViewModels implement `DefaultLifecycleObserver`, and SwiftUI calls lifecycle methods directly.

**Key Pattern**:
```swift
struct PokemonListView: View {
    @StateObject private var owner = IosViewModelStoreOwner()

    private var viewModel: PokemonListViewModel {
        owner.viewModel()  // Type inferred
    }

    var body: some View {
        content
            .onAppear { viewModel.onStart(owner: DummyLifecycleOwner()) }
            .onDisappear { viewModel.onStop(owner: DummyLifecycleOwner()) }
            .task {
                for await state in viewModel.uiState {
                    self.uiState = state
                }
            }
    }
}
```

## Architecture Overview

### Two iOS Apps

| App | UI | Framework | Purpose |
|-----|-------|-----------|---------|
| **iosApp** (production) | SwiftUI | Shared.framework | Native iOS UI consuming KMP ViewModels |
| **iosAppCompose** (experimental) | Compose Multiplatform | ComposeApp.framework | Shares Compose UI with Android/Desktop |

**This skill covers iosApp only** (native SwiftUI approach).

### Module Structure

```
:shared/src/
├── iosMain/kotlin/
│   ├── KoinIos.kt                    # Helper functions for iOS
│   ├── IosViewModelStoreOwner.kt     # ViewModelStore provider
│   └── KoinViewModelHelpers.kt       # Generic ViewModel retrieval
└── commonMain/kotlin/                # Shared ViewModels (exported)

iosApp/iosApp/
├── Views/
│   ├── PokemonListView.swift         # SwiftUI Views
│   └── PokemonDetailView.swift
├── Models/
│   └── DummyLifecycleOwner.swift     # Lifecycle stub
└── iOSApp.swift                      # App entry point
```

### Key iOS Components

| Component | Purpose | Location |
|-----------|---------|----------|
| `IosViewModelStoreOwner` | Provides ViewModelStore, manages lifecycle | `shared/src/iosMain/kotlin/` |
| `DummyLifecycleOwner` | Minimal stub for API signature | `iosApp/iosApp/Models/` |
| `KoinViewModelHelpers.kt` | Generic ViewModel retrieval functions | `shared/src/iosMain/kotlin/` |
| SKIE | Bridges StateFlow → AsyncSequence | Plugin configuration |

## iOS Architecture Patterns

This repo ships **two iOS apps** that consume Kotlin Multiplatform code with **different boundaries**.

### Two iOS apps, two frameworks

#### `iosApp` (production, SwiftUI)

- UI: SwiftUI
- Kotlin framework: `Shared.framework`
- Rule: **MUST remain Compose-free** (no Compose UI and no Compose-only navigation symbols)

Framework definition:
- [`shared/build.gradle.kts`](../../../shared/build.gradle.kts)

App Xcode project:
- [`iosApp/iosApp.xcodeproj`](../../../iosApp/iosApp.xcodeproj)

#### `iosAppCompose` (experimental, SwiftUI wrapper around Compose)

- UI: Compose Multiplatform (hosted in SwiftUI)
- Kotlin framework: `ComposeApp.framework`
- Rule: **is allowed to include Compose UI + Compose navigation**

Framework definition:
- [`composeApp/build.gradle.kts`](../../../composeApp/build.gradle.kts)

App Xcode project:
- [`iosAppCompose/iosAppCompose.xcodeproj`](../../../../iosAppCompose/iosAppCompose.xcodeproj)

### Boundary rules (critical)

The core rule for the dual-iOS-app setup:

- `Shared.framework` must export only business logic that the SwiftUI app can consume.
- Compose UI code lives behind `ComposeApp.framework` only.

Related conventions and module guidelines:

- Architecture and module conventions: See @kmp-architecture skill
- iOS integration overview: See @kmp-ios skill
- Navigation conventions: See @kmp-navigation skill

### Validation commands

#### Always run (primary)

```bash
./gradlew :composeApp:assembleDebug test --continue
```

#### iOS frameworks (CLI-friendly)

The `embedAndSignAppleFrameworkForXcode` tasks require Xcode-provided env vars. From CLI, prefer framework link tasks:

```bash
# iosApp (Shared.framework)
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64 :shared:linkDebugFrameworkIosArm64

# iosAppCompose (ComposeApp.framework)
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64 :composeApp:linkDebugFrameworkIosArm64
```

#### Boundary checks (symbols)

```bash
echo "Checking iosApp framework (must be Compose-free):"
nm -g shared/build/bin/iosArm64/debugFramework/Shared.framework/Shared \
  | grep -i "compose\|navigation" && echo "❌ VIOLATION: Compose/Navigation leaked to iosApp" || echo "✅ iosApp boundary clean"

echo "Checking iosAppCompose framework (must have Compose):"
nm -g composeApp/build/bin/iosArm64/debugFramework/ComposeApp.framework/ComposeApp \
  | grep -i "compose" && echo "✅ iosAppCompose has Compose" || echo "❌ ERROR: Missing Compose in iosAppCompose"
```

#### Xcode builds (milestones)

```bash
cd iosApp && xcodebuild -scheme iosApp -sdk iphonesimulator build CODE_SIGN_IDENTITY="" CODE_SIGNING_REQUIRED=NO
cd ../iosAppCompose && xcodebuild -scheme iosAppCompose -sdk iphonesimulator build CODE_SIGN_IDENTITY="" CODE_SIGNING_REQUIRED=NO
```

## Decision Framework

Before implementing iOS integration, ask yourself:

1. **What needs to be exported to iOS?**
   - ViewModels (`:presentation`) → YES, export via `:shared` framework
   - Domain models (`:api`) → YES, export for contracts
   - Repositories (`:data`) → NO, implementation stays in KMP
   - UI (`:ui-*`) → NO, iOS uses native SwiftUI

2. **How should ViewModels be consumed?**
   - Use `IosViewModelStoreOwner` for lifecycle management
   - Use `@StateObject` in SwiftUI for ViewModel ownership
   - Collect StateFlow with `@Published` wrapper or AsyncStream
   - Handle one-time events via Combine or async/await

3. **What build validation is needed?**
   - Always test iOS framework export: `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`
   - Verify Swift can see exported types (check `:shared` dependencies)
   - NEVER export `:data`, `:ui`, `:wiring` modules

## Critical Guardrails

1. **NEVER export `:data`, `:ui-*`, `:wiring*` to iOS** → only export `:api` and `:presentation` to maintain a strict boundary and prevent framework bloat.
2. **NEVER use KMP ViewModels directly without lifecycle bridging** → use `DisposableEffectObserverBridge` (or manual appear/disappear calls) to ensure `onStart()` and `onStop()` are triggered.
3. **NEVER create iOS-specific ViewModels wrapping KMP ViewModels** → use the Direct Integration pattern with `@StateObject` and `IosViewModelStoreOwner` to minimize boilerplate.
4. **NEVER skip `onStart()` observation** → ViewModels rely on lifecycle events for initialization; skipping them means data will never load.
5. **NEVER expose `MutableStateFlow` to Swift** → only expose immutable `StateFlow` to ensure state can only be modified from within the Kotlin layer.
6. **NEVER let iOS code depend on Compose UI** → maintain a strict boundary at the presentation layer; iOS must use native SwiftUI views.
7. **NEVER manually manage ViewModel lifecycle** → use `@StateObject` + `DisposableEffect` (or direct integration with `IosViewModelStoreOwner`) to let SwiftUI manage instance lifetime.

## Validation Commands

```bash
# Build iOS frameworks (CLI-friendly)
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64 :shared:linkDebugFrameworkIosArm64

# Boundary checks (verify Shared.framework is Compose-free)
echo "Checking iosApp framework (must be Compose-free):"
nm -g shared/build/bin/iosArm64/debugFramework/Shared.framework/Shared \
  | grep -i "compose\|navigation" && echo "❌ VIOLATION" || echo "✅ Clean"

# Build and test (primary)
./gradlew :composeApp:assembleDebug test --continue

# Xcode builds (milestones)
cd iosApp && xcodebuild -scheme iosApp -sdk iphonesimulator build CODE_SIGN_IDENTITY="" CODE_SIGNING_REQUIRED=NO
```

## Cross-References

### Skills (by Category)

**Architecture**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-architecture | Module structure, vertical slicing, feature boundaries | [SKILL.md](../kmp-architecture/SKILL.md) |
| @kmp-critical-patterns | 6 core patterns quick reference (ViewModel, Either, Impl+Factory, Navigation, Testing, Plugins) | [SKILL.md](../kmp-critical-patterns/SKILL.md) |

**Layer Implementation**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-presentation | ViewModels, lifecycle, SavedStateHandle, UI state management | [SKILL.md](../kmp-presentation/SKILL.md) |
| @kmp-data-layer | Repository patterns, Either<RepoError, T>, DTO mapping | [SKILL.md](../kmp-data-layer/SKILL.md) |
| @kmp-domain | Domain models, use cases, domain exceptions | [SKILL.md](../kmp-domain/SKILL.md) |
| @kmp-di | Koin dependency injection patterns and configuration | [SKILL.md](../kmp-di/SKILL.md) |

**Platform & Navigation**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-ios | SwiftUI + KMP ViewModels Direct Integration, iOS export | [SKILL.md](../kmp-ios/SKILL.md) |
| @swiftui-screen | Building SwiftUI iOS screens | [SKILL.md](../swiftui-screen/SKILL.md) |
| @kmp-navigation | Navigation 3 modular architecture, scoped routes | [SKILL.md](../kmp-navigation/SKILL.md) |
| @kmp-desktop | Desktop (JVM) SavedStateHandle, Koin, platform-specific patterns | [SKILL.md](../kmp-desktop/SKILL.md) |

**Development & Quality**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-developer | General KMP development, vertical slice workflows | [SKILL.md](../kmp-developer/SKILL.md) |
| @kmp-mobile-expert | Shared business logic, ViewModels, repositories | [SKILL.md](../kmp-mobile-expert/SKILL.md) |
| @kmp-testing-strategy | Testing philosophy, coverage guidelines | [SKILL.md](../kmp-testing-strategy/SKILL.md) |

### Documents

| Document | Purpose | Link |
| --- | --- | --- |
| Architecture + conventions | Master reference for architecture, modules, DI | See @kmp-architecture skill |
| iOS integration | SwiftUI + KMP ViewModels Direct Integration details | See @kmp-ios skill |
| iOS official patterns | Official pattern quick reference | See @kmp-ios skill |
| Dependency injection | Koin patterns and troubleshooting | See @kmp-di skill |

