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

## Quick Reference

### Non-Parametric ViewModel

```swift
struct PokemonListView: View {
    @StateObject private var owner = IosViewModelStoreOwner()

    private var viewModel: PokemonListViewModel {
        owner.viewModel()  // Generic function infers type
    }

    @State private var uiState: PokemonListUiState = PokemonListUiStateLoading()

    var body: some View {
        // State switching, .onAppear, .onDisappear, .task
    }
}
```

### Parametric ViewModel (with Int)

```swift
struct PokemonDetailView: View {
    let pokemonId: Int
    @StateObject private var owner = IosViewModelStoreOwner()

    private var viewModel: PokemonDetailViewModel {
        owner.viewModel(intParam: pokemonId)
    }

    @State private var uiState: PokemonDetailUiState = PokemonDetailUiStateLoading()

    var body: some View {
        // Same pattern, with pokemonId parameter
    }
}
```

### StateFlow Observation

```swift
.task {
    // SKIE: StateFlow → AsyncSequence bridging
    for await state in viewModel.uiState {
        self.uiState = state
    }
}
```

### Type Conversions

```swift
// Kotlin Int32 → Swift Int
String(format: "#%03d", Int(pokemon.id))

// Kotlin Int32 → Swift Double for formatting
String(format: "%.1f m", Double(pokemon.height) / 10.0)
```

## Reference Loading Guide

| Task | Reference | Load When |
|------|-----------|-----------|
| Direct Integration pattern | [direct-integration.md](references/direct-integration.md) | Setting up SwiftUI + KMP ViewModels |
| Lifecycle bridging details | [lifecycle-bridging.md](references/lifecycle-bridging.md) | Implementing SwiftUI lifecycle with KMP |
| SwiftUI patterns | [swiftui-patterns.md](references/swiftui-patterns.md) | Creating iOS Views, observing StateFlow |
| Export configuration | [export-setup.md](references/export-setup.md) | Configuring `:shared` framework exports |

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

## Essential Workflows

### Workflow 1: Create SwiftUI View consuming KMP ViewModel

To create a new SwiftUI view that uses a shared KMP ViewModel:

1. **Define the View**: Use `@StateObject` with `IosViewModelStoreOwner` to ensure the ViewModel Store survives view recreations.
2. **Retrieve ViewModel**: Access the ViewModel through the owner's helper function, letting type inference handle the resolution.
3. **Setup UI State**: Create a `@State` variable to hold the UI state (bridged from `StateFlow`).
4. **Implement Body**: Use a `switch` statement or similar to render different states based on the `uiState`.

```swift
struct MyNewView: View {
    @StateObject private var owner = IosViewModelStoreOwner()
    
    private var viewModel: MyViewModel {
        owner.viewModel()  // Generic function infers type
    }
    
    @State private var uiState: MyUiState = MyUiStateLoading()
    
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

### Workflow 2: Bridge KMP Lifecycle to SwiftUI

To ensure ViewModels correctly handle data loading and cleanup, manually bridge the lifecycle or use the `DisposableEffectObserverBridge` pattern:

1. **Observe Lifecycle**: In `.onAppear`, call `viewModel.onStart(owner: DummyLifecycleOwner())`. This triggers the ViewModel's `onStart` logic (e.g., initial data load).
2. **Unobserve Lifecycle**: In `.onDisappear`, call `viewModel.onStop(owner: DummyLifecycleOwner())` to notify the ViewModel that the view is no longer visible.
3. **ViewModel Integration**: Ensure the ViewModel implements `DefaultLifecycleObserver` to receive these events.

### Workflow 3: Configure Framework Exports

To make KMP modules available to Swift, configure the `:shared` framework exports in `shared/build.gradle.kts`:

1. **Identify modules**: Only export layers needed by iOS (contracts and presentation).
2. **Configure `:shared`**: Update `shared/build.gradle.kts` to `export` modules in the `binaries.framework` block.
3. **Update dependencies**: Add modules as `api` dependencies in `commonMain`.

```kotlin
target.binaries.framework {
    baseName = "Shared"
    export(projects.features.pokemonlist.api)
    export(projects.features.pokemonlist.presentation)
}
```

### Workflow 4: Handle StateFlow in SwiftUI

SKIE automatically bridges `StateFlow` to `AsyncSequence`. Use the `AsyncStream` bridging pattern with the `.task` modifier:

1. **Use `.task`**: This modifier is tied to the view's lifecycle and auto-cancels when the view is destroyed.
2. **Iterate with `for await`**: Collect emissions from the ViewModel's `uiState` directly in Swift.

```swift
.task {
    for await state in viewModel.uiState {
        self.uiState = state
    }
}
```

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
| Architecture + conventions | Master reference for architecture, modules, DI | [conventions.md](See @kmp-architecture skill for architecture patterns) |
| iOS integration | SwiftUI + KMP ViewModels Direct Integration details | [ios_integration.md](See @kmp-ios skill) |
| iOS official patterns | Official pattern quick reference | [ios_official_pattern_guide.md](../../docs/tech/ios_official_pattern_guide.md) |
| Dependency injection | Koin patterns and troubleshooting | [dependency_injection.md](See @kmp-di skill) |
| Product requirements | Feature acceptance criteria | [prd.md](../../docs/project/prd.md) |

