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

## Related Skills

| Skill | Use For |
|-------|---------|
| @kmp-presentation | ViewModel implementation (Kotlin side) |
| @kmp-mobile-expert | ViewModels, repositories, iOS integration |
| @kmp-architecture | Module structure and vertical slicing |
| @swiftui-screen | Building SwiftUI iOS screens |
| @kmp-di | Koin DI configuration |

## Documentation Sources

| Document | Purpose | Tokens |
|----------|---------|--------|
| [ios_integration.md](../../../docs/tech/ios_integration.md) | Complete iOS integration guide | ~19000 |
| [ios_official_pattern_guide.md](../../../docs/tech/ios_official_pattern_guide.md) | Official pattern quick reference | ~4000 |
| [ios_apps_architecture.md](../../../docs/tech/ios_apps_architecture.md) | Two iOS apps architecture | ~2000 |

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

## Critical Checklist

- [ ] Use `@StateObject` for `IosViewModelStoreOwner` (survives View recreation)
- [ ] Call `viewModel.onStart(owner:)` in `.onAppear`
- [ ] Call `viewModel.onStop(owner:)` in `.onDisappear`
- [ ] Observe StateFlow in `.task` modifier (auto-cancels)
- [ ] Cast Kotlin `Int32` → Swift `Int` when needed
- [ ] Export only `:api` and `:presentation` modules to iOS
- [ ] Use helper functions from `KoinViewModelHelpers.kt`
- [ ] Use `DummyLifecycleOwner` for lifecycle method calls

## Anti-Patterns to Avoid

| ❌ DON'T | ✅ DO |
|----------|-------|
| Direct Koin `get()` calls in Swift | Use helper functions from Kotlin |
| Store ViewModel as `@State` | Use `@StateObject` for `IosViewModelStoreOwner` |
| Forget lifecycle method calls | Always call `onStart/onStop` in appear/disappear |
| Use string interpolation for formatting | Use `String(format:_:)` |
| Export Compose UI to Shared.framework | Keep Shared.framework Compose-free |
