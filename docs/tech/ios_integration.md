# iOS Integration Guide - SwiftUI + KMP ViewModels

> **Status**: ✅ Production Pattern Established  
> **Last Updated**: November 26, 2025  
> **Current Pattern**: Direct Integration (`private var` ViewModel + `@State` for UI state)  
> **Alternative**: Wrapper Pattern (`@ObservableObject` + `@StateObject`) available for complex apps  
> **Note**: This guide covers **iosApp** (native SwiftUI). For iOS Compose app (**iosAppCompose**), see Compose Multiplatform iOS documentation.

---

## Overview

This project has **TWO iOS app options**:

1. **iosApp** (production - this guide): Native SwiftUI UI consuming shared KMP ViewModels via SKIE
2. **iosAppCompose** (experimental): Compose Multiplatform UI sharing @Composable screens with Android/Desktop

**This guide covers iosApp only.** The native SwiftUI approach was deliberately chosen for platform consistency and ecosystem access.

### Architecture Summary

**Current Pattern**: Direct Integration (no separate wrapper layer)

```
┌─────────────────────────────────────────────────────────────┐
│  iOS App (SwiftUI)                                          │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  Views (SwiftUI)                                       │  │
│  │  - PokemonListView                                     │  │
│  │    • private var viewModel (from Koin)                │  │
│  │    • @State var uiState (bridges StateFlow)           │  │
│  │  - PokemonDetailView                                   │  │
│  │  - NavigationStack (native iOS)                        │  │
│  └───────────────────────────────────────────────────────┘  │
│                           │ Direct Access                   │
│                           ▼                                  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  Shared.framework (SKIE-enhanced)                     │  │
│  │  - Koin DI initialization                             │  │
│  │  - Helper functions (getPokemonListViewModel)         │  │
│  │  - StateFlow → AsyncSequence bridging                 │  │
│  │  - KMP ViewModels consumed directly via .task         │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│  KMP Shared Modules                                         │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  Presentation Layer (exported to iOS)                 │  │
│  │  - PokemonListViewModel                               │  │
│  │  - StateFlow<UiState>                                 │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  Data Layer (NOT exported to iOS)                     │  │
│  │  - Repositories, API services, DTOs, mappers          │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  DI Wiring (NOT exported to iOS)                      │  │
│  │  - Koin modules (pokemonListModule)                   │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

**Alternative Pattern Available**: Wrapper Pattern with `@ObservableObject` (see ViewModel Integration Approaches section below)

---

## 🔀 ViewModel Integration Approaches

> **Current Pattern**: Direct Integration (no wrappers)  
> **Status**: ✅ Production - Works well for simple to medium complexity apps  
> **Decision Guide**: Choose approach based on app requirements (see comparison below)

This project supports **TWO patterns** for integrating KMP ViewModels with SwiftUI. Both are valid; choose based on your app's complexity and requirements.

---

### Pattern Comparison Overview

| Aspect | Direct Integration (Current) | Wrapper Pattern |
|--------|------------------------------|-----------------|
| **Boilerplate** | ✅ Minimal (~10 lines per view) | ❌ More (~80 lines per ViewModel) |
| **Lifecycle Management** | ⚠️ ViewModel recreated on View struct recreation | ✅ `@StateObject` preserves ViewModel across recreations |
| **State Preservation** | ❌ Lost on parent updates/navigation changes | ✅ Survives parent updates, navigation changes |
| **SwiftUI Best Practice** | ⚠️ Using `@State` for external state (non-idiomatic) | ✅ `@StateObject` for external state (Apple recommended) |
| **Memory Leaks** | ✅ No leaks (`.task` cancels properly) | ✅ No leaks (`.task` cancels properly) |
| **Testing** | ⚠️ Harder to mock ViewModel | ✅ Easy to mock wrapper in previews |
| **Complexity** | ✅ Simple, direct | ⚠️ Additional abstraction layer |
| **Current Usage** | ✅ PokemonListView, PokemonDetailView | ❌ Not currently used (legacy code exists) |

---

### Pattern 1: Direct Integration (Current Production Pattern)

**When to Use**:
- ✅ Simple to medium complexity apps
- ✅ Linear navigation flows (stack-based)
- ✅ Minimal state preservation requirements
- ✅ Team prioritizes code simplicity over lifecycle guarantees
- ✅ Views don't need to survive parent updates with state intact

**Implementation**:

```swift
// iosApp/Views/PokemonListView.swift (Current)
import SwiftUI
import Shared

struct PokemonListView: View {
    // Direct ViewModel access from Koin
    private var viewModel = KoinIosKt.getPokemonListViewModel()
    
    // @State bridges StateFlow to SwiftUI reactivity
    @State private var uiState: PokemonListUiState = PokemonListUiStateLoading()
    
    var body: some View {
        content
            .onAppear {
                // One-time data load
                if case is PokemonListUiStateLoading = uiState {
                    viewModel.loadInitialPage()
                }
            }
            .task {
                // SKIE: StateFlow → AsyncSequence observation
                // Auto-cancels when view disappears
                for await state in viewModel.uiState {
                    self.uiState = state
                }
            }
    }
    
    @ViewBuilder
    private var content: some View {
        switch uiState {
        case is PokemonListUiStateLoading:
            ProgressView("Loading...")
        case let content as PokemonListUiStateContent:
            // Render content
        case let error as PokemonListUiStateError:
            ErrorView(message: error.message)
        default:
            EmptyView()
        }
    }
}
```

**Parametric ViewModel Example**:

```swift
// iosApp/Views/PokemonDetailView.swift (Current)
struct PokemonDetailView: View {
    let pokemonId: Int
    private var viewModel: PokemonDetailViewModel
    @State private var uiState: PokemonDetailUiState = PokemonDetailUiStateLoading()
    
    init(pokemonId: Int) {
        self.pokemonId = pokemonId
        // Get parametric ViewModel from Koin in init
        viewModel = KoinIosKt.getPokemonDetailViewModel(pokemonId: Int32(pokemonId))
    }
    
    var body: some View {
        content
            .task {
                for await state in viewModel.uiState {
                    uiState = state
                }
            }
    }
}
```

**Pros**:
- ✅ **Minimal boilerplate**: ~10 lines per view (no separate wrapper class)
- ✅ **Direct access**: Call ViewModel methods directly (`viewModel.loadInitialPage()`)
- ✅ **Simple mental model**: One object (ViewModel), one state property
- ✅ **No intermediate layer**: Fewer abstractions to understand
- ✅ **Works with SKIE**: Leverages automatic StateFlow bridging
- ✅ **No memory leaks**: `.task` cancels AsyncSequence properly

**Cons**:
- ❌ **ViewModel recreation**: New ViewModel created when View struct is recreated
- ❌ **State loss scenarios**: Loses state on parent view updates, NavigationStack changes, @Environment changes
- ❌ **Non-idiomatic SwiftUI**: Using `@State` for external state violates Apple guidelines
- ❌ **Harder to test**: Can't easily mock ViewModel in SwiftUI Previews
- ❌ **Performance overhead**: Unnecessary Koin resolution + repository injection on recreation
- ❌ **Advanced navigation issues**: Breaks with tab views, sheets, parent state updates

**Technical Details**:

**SwiftUI View Struct Lifecycle**:
```swift
// SwiftUI recreates View structs in these scenarios:
// 1. Parent view updates @State or @Binding
// 2. NavigationStack path changes (even in parent)
// 3. @Environment values change (colorScheme, locale, etc.)
// 4. Device orientation changes
// 5. Tab switching in TabView

// With Direct Integration:
struct MyView: View {
    private var viewModel = getViewModel()  // ← Recreated on View struct recreation
    @State private var uiState = Loading()  // ← Preserved by SwiftUI
    
    // Result: New ViewModel, state resets, reloads data
}
```

**When View Struct Recreation Causes Issues**:

```swift
// Example: Parent toolbar update triggers View recreation
NavigationStack {
    PokemonListView()  // ← View struct recreated
        .toolbar {
            Button("Filter") { showFilter.toggle() }  // ← Parent state change
        }
}

// Problem: PokemonListView's ViewModel recreated → Loading state → flicker
```

---

### Pattern 2: Wrapper Pattern (Alternative)

**When to Use**:
- ✅ Complex navigation (tabs, sheets, deep stacks)
- ✅ State must survive parent updates
- ✅ Team values SwiftUI best practices over simplicity
- ✅ Need easy mocking for SwiftUI Previews
- ✅ Large-scale production apps with strict lifecycle requirements

**Implementation**:

**Step 1: Create Wrapper Class**

```swift
// iosApp/ViewModels/PokemonListViewModelWrapper.swift
import Foundation
import Shared
import SwiftUI

@MainActor
class PokemonListViewModelWrapper: ObservableObject {
    // @Published triggers SwiftUI re-renders
    @Published var uiState: PokemonListUiState = PokemonListUiStateLoading()
    
    // Private KMP ViewModel (lifecycle managed by wrapper)
    private let viewModel: PokemonListViewModel
    
    init() {
        self.viewModel = KoinIosKt.getPokemonListViewModel()
    }
    
    // Observe StateFlow and update @Published property
    func observeState() async {
        for await state in viewModel.uiState {
            self.uiState = state
        }
    }
    
    // Delegate methods to KMP ViewModel
    func loadInitialPage() {
        viewModel.loadInitialPage()
    }
    
    func loadNextPage() {
        viewModel.loadNextPage()
    }
}
```

**Step 2: Use Wrapper in View**

```swift
// iosApp/Views/PokemonListView.swift
import SwiftUI
import Shared

struct PokemonListView: View {
    // @StateObject manages wrapper lifecycle (survives View recreation)
    @StateObject private var wrapper = PokemonListViewModelWrapper()
    
    var body: some View {
        content
            .onAppear {
                wrapper.loadInitialPage()
            }
            .task {
                await wrapper.observeState()
            }
    }
    
    @ViewBuilder
    private var content: some View {
        switch wrapper.uiState {
        case is PokemonListUiStateLoading:
            ProgressView()
        case let content as PokemonListUiStateContent:
            // Render content
        default:
            EmptyView()
        }
    }
}
```

**Parametric ViewModel Wrapper**:

```swift
// iosApp/ViewModels/PokemonDetailViewModelWrapper.swift
@MainActor
class PokemonDetailViewModelWrapper: ObservableObject {
    @Published var uiState: PokemonDetailUiState = PokemonDetailUiStateLoading()
    private let viewModel: PokemonDetailViewModel
    
    init(pokemonId: Int) {
        self.viewModel = KoinIosKt.getPokemonDetailViewModel(pokemonId: Int32(pokemonId))
    }
    
    func observeState() async {
        for await state in viewModel.uiState {
            self.uiState = state
        }
    }
    
    func retry() {
        viewModel.retry()
    }
}

// Usage in View
struct PokemonDetailView: View {
    let pokemonId: Int
    @StateObject private var wrapper: PokemonDetailViewModelWrapper
    
    init(pokemonId: Int) {
        self.pokemonId = pokemonId
        // Initialize @StateObject in init with wrappedValue
        _wrapper = StateObject(wrappedValue: PokemonDetailViewModelWrapper(pokemonId: pokemonId))
    }
    
    var body: some View {
        content
            .task { await wrapper.observeState() }
    }
}
```

**Pros**:
- ✅ **Lifecycle guarantees**: `@StateObject` preserves wrapper/ViewModel across View recreations
- ✅ **State preservation**: Survives parent updates, navigation changes, environment changes
- ✅ **SwiftUI idiomatic**: `@StateObject` for external state (Apple recommended)
- ✅ **Easy testing**: Mock wrapper in SwiftUI Previews
- ✅ **Clear separation**: Wrapper handles SwiftUI integration, ViewModel handles logic
- ✅ **Production-ready**: Proven pattern for complex apps

**Cons**:
- ❌ **Boilerplate**: ~80 lines per ViewModel (wrapper class + methods)
- ❌ **Additional abstraction**: Two objects per feature (ViewModel + Wrapper)
- ❌ **Method delegation**: Must forward all ViewModel methods to wrapper
- ❌ **Maintenance**: Need to update wrapper when ViewModel API changes

**Technical Details**:

**@StateObject Lifecycle**:
```swift
struct MyView: View {
    @StateObject private var wrapper = ViewModelWrapper()
    // ↑ Created once, survives View struct recreation
    // SwiftUI manages lifecycle, destroys only when View permanently removed
    
    // Result: Same wrapper instance across parent updates, stable state
}
```

---

### Decision Matrix

Use this table to choose the right pattern:

| Requirement | Direct Integration | Wrapper Pattern |
|-------------|-------------------|-----------------|
| **Simple linear navigation** | ✅ Recommended | ⚠️ Overkill |
| **Tab-based navigation** | ❌ State loss on tab switch | ✅ Required |
| **Sheet/modal presentation** | ⚠️ May lose state | ✅ Preserves state |
| **Parent view has @State** | ❌ ViewModel recreated | ✅ Survives |
| **Deep navigation stacks** | ⚠️ Fragile | ✅ Robust |
| **Team new to SwiftUI** | ✅ Simpler to understand | ❌ More complex |
| **Testing in Previews** | ❌ Hard to mock | ✅ Easy to mock |
| **Boilerplate tolerance** | ✅ Low (~10 lines) | ❌ High (~80 lines) |
| **Production large-scale app** | ⚠️ Risky for complex flows | ✅ Safer choice |
| **MVP/POC project** | ✅ Fast iteration | ⚠️ Premature optimization |

---

### Migration Guide

#### Converting Direct Integration → Wrapper Pattern

**When to Migrate**:
- App grows beyond simple navigation
- Users report state loss issues (data reloads unexpectedly)
- Adding tabs, sheets, or complex navigation
- Need better testability

**Steps**:

1. **Create Wrapper Class**:
```swift
// Before: No wrapper
// After: Create iosApp/ViewModels/PokemonListViewModelWrapper.swift

@MainActor
class PokemonListViewModelWrapper: ObservableObject {
    @Published var uiState: PokemonListUiState = PokemonListUiStateLoading()
    private let viewModel: PokemonListViewModel
    
    init() {
        self.viewModel = KoinIosKt.getPokemonListViewModel()
    }
    
    func observeState() async {
        for await state in viewModel.uiState {
            self.uiState = state
        }
    }
    
    // Add delegation methods for all ViewModel actions
    func loadInitialPage() { viewModel.loadInitialPage() }
    func loadNextPage() { viewModel.loadNextPage() }
}
```

2. **Update View**:
```swift
// Before:
struct PokemonListView: View {
    private var viewModel = KoinIosKt.getPokemonListViewModel()
    @State private var uiState: PokemonListUiState = PokemonListUiStateLoading()
    
    var body: some View {
        content
            .task {
                for await state in viewModel.uiState {
                    self.uiState = state
                }
            }
    }
}

// After:
struct PokemonListView: View {
    @StateObject private var wrapper = PokemonListViewModelWrapper()
    
    var body: some View {
        content
            .task {
                await wrapper.observeState()
            }
    }
    
    // Update all references: uiState → wrapper.uiState
    // Update all calls: viewModel.method() → wrapper.method()
}
```

3. **Update References**:
```swift
// Before:
switch uiState {
    case is PokemonListUiStateLoading: ...
}
viewModel.loadInitialPage()

// After:
switch wrapper.uiState {
    case is PokemonListUiStateLoading: ...
}
wrapper.loadInitialPage()
```

4. **Test Migration**:
- [ ] Verify no state loss on parent updates
- [ ] Test tab switching (if applicable)
- [ ] Test sheet presentation/dismissal
- [ ] Verify navigation back/forward preserves state
- [ ] Check SwiftUI Previews still work

#### Converting Wrapper Pattern → Direct Integration

**When to Migrate**:
- App remains simple (no tabs, shallow navigation)
- Team wants less boilerplate
- No state preservation issues reported

**Steps**:

1. **Update View**:
```swift
// Before:
struct PokemonListView: View {
    @StateObject private var wrapper = PokemonListViewModelWrapper()
    
    var body: some View {
        content.task { await wrapper.observeState() }
    }
}

// After:
struct PokemonListView: View {
    private var viewModel = KoinIosKt.getPokemonListViewModel()
    @State private var uiState: PokemonListUiState = PokemonListUiStateLoading()
    
    var body: some View {
        content
            .task {
                for await state in viewModel.uiState {
                    self.uiState = state
                }
            }
    }
}
```

2. **Update References**:
```swift
// Before:
switch wrapper.uiState { ... }
wrapper.loadInitialPage()

// After:
switch uiState { ... }
viewModel.loadInitialPage()
```

3. **Delete Wrapper Class**:
```bash
rm iosApp/iosApp/ViewModels/PokemonListViewModelWrapper.swift
```

4. **Test for Regressions**:
- [ ] Verify basic navigation works
- [ ] Check data loads correctly
- [ ] Test error/retry flows
- [ ] Watch for unexpected reloads (sign of View recreation issues)

---

### Current Project Status

**Active Pattern**: Direct Integration (Pattern 1)

**Files**:
- `iosApp/iosApp/Views/PokemonListView.swift` - Direct integration
- `iosApp/iosApp/Views/PokemonDetailView.swift` - Direct integration (parametric)

**Legacy Files** (not currently used):
- `iosApp/iosApp/ViewModels/PokemonDetailViewModelWrapper.swift` - Example of Wrapper pattern (kept as reference)

**Decision Rationale**:
- Current app has simple navigation (single stack)
- No tabs, sheets, or complex flows
- Team prioritizes rapid iteration and simplicity
- No state preservation issues reported in testing

**Future Considerations**:
- If adding tabs → migrate to Wrapper pattern
- If users report data reloading unexpectedly → migrate to Wrapper pattern
- If app scales beyond 5+ screens with complex navigation → migrate to Wrapper pattern

---

## 🎯 Key Patterns

**SKIE** (Swift Kotlin Interface Enhancer) automatically bridges Kotlin Coroutines to Swift async/await:

- **StateFlow → AsyncSequence**: Automatic bridging, no manual code needed
- **Suspend functions → async**: Native Swift concurrency support
- **Flow → AsyncSequence**: Collect Kotlin flows in Swift

**Configuration** (`shared/build.gradle.kts`):
```kotlin
plugins {
    alias(libs.plugins.skie)
}

// SKIE automatically processes iOS framework exports
```

**Version**: `0.10.8` (compatible with Kotlin 2.2.21)

#### SKIE Automatic Renames for Swift Keywords

**Problem**: Kotlin classes named after Swift keywords get automatically renamed by SKIE.

**Example**: `Type` class (Pokemon type) conflicts with Swift's `Type` protocol.

**SKIE Solution**: Automatically renames to `Type_` (appends underscore).

**Impact in Code**:

```swift
// Kotlin side (unchanged)
data class Type(val name: String, val url: String)

// Swift side (SKIE-renamed)
struct PokemonDetail {
    let types: [Type_]  // Note: Type_ not Type
}

// Using in SwiftUI
ForEach(pokemon.types, id: \.name) { type in
    TypeBadge(type: type)  // type is Type_ instance
}

private func typeGradient(types: [Type_]) -> LinearGradient {
    let type = types.first?.name.lowercased() ?? "normal"
    // Use type.name to access properties
}
```

**Other Common Keyword Collisions**:
- `Type` → `Type_`
- `Error` → `Error_`
- `Result` → `Result_`
- `Self` → `Self_`
- `Protocol` → `Protocol_`

**Debugging Renamed Types**:
```swift
// Check in Swift compiler or Xcode autocomplete
// If a Kotlin class doesn't show up, try appending _
import Shared
let type: Type_ = pokemonDetail.types.first!
```

**Best Practice**: 
- ✅ Check SKIE-generated Swift interfaces when Kotlin types don't compile
- ✅ Search for `_` suffix on missing types
- ✅ Use Xcode autocomplete to discover renamed types
- ❌ Don't manually rename Kotlin classes to avoid Swift keywords (SKIE handles it)

---

### 2. Parametric ViewModels with Koin

**Pattern**: ViewModels with constructor parameters (e.g., `pokemonId`, `userId`)

#### Kotlin Side: Factory with parametersOf

```kotlin
// :features:pokemondetail:presentation - ViewModel with parameter
class PokemonDetailViewModel(
    private val pokemonId: Int,
    private val repository: PokemonDetailRepository,
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) : ViewModel(viewModelScope), UiStateHolder<PokemonDetailUiState, PokemonDetailUiEvent> {
    
    fun loadPokemon() {
        viewModelScope.launch {
            _uiState.value = PokemonDetailUiState.Loading
            repository.getPokemonById(pokemonId).fold(
                ifLeft = { error -> _uiState.value = PokemonDetailUiState.Error(error.message) },
                ifRight = { pokemon -> _uiState.value = PokemonDetailUiState.Content(pokemon) }
            )
        }
    }
}

// :features:pokemondetail:wiring - Koin module with parametersOf
val pokemonDetailModule = module {
    factory { params ->
        PokemonDetailViewModel(
            pokemonId = params.get(),
            repository = get()
        )
    }
}
```

**Key Points**:
- ViewModel takes `pokemonId` as constructor parameter
- Koin `factory` uses `params ->` to receive parameters
- `params.get()` extracts parameter by type (Int in this case)
- Repository injected normally with `get()`

#### iOS Side: Helper Function with parametersOf

```kotlin
// shared/src/iosMain/kotlin/KoinIos.kt
fun getPokemonDetailViewModel(pokemonId: Int): com.minddistrict.multiplatformpoc.features.pokemondetail.presentation.PokemonDetailViewModel {
    return KoinPlatform.getKoin().get { parametersOf(pokemonId) }
}
```

**Key Points**:
- Helper function accepts parameter (pokemonId)
- `get { parametersOf(pokemonId) }` passes parameter to Koin factory
- Return type is fully qualified (avoids Swift name collisions)

#### Swift Side: Wrapper with init Parameter

```swift
// iosApp/ViewModels/PokemonDetailViewModelWrapper.swift
import Foundation
import Shared

@MainActor
class PokemonDetailViewModelWrapper: ObservableObject {
    @Published var uiState: PokemonDetailUiState = PokemonDetailUiStateLoading()
    
    private let viewModel: PokemonDetailViewModel
    
    init(pokemonId: Int) {
        // Get ViewModel from Koin with parameter
        self.viewModel = KoinIosKt.getPokemonDetailViewModel(pokemonId: Int32(pokemonId))
    }
    
    func observeState() async {
        for await state in viewModel.uiState {
            self.uiState = state
        }
    }
    
    func retry() {
        viewModel.retry()
    }
}
```

**Key Points**:
- Wrapper `init` accepts pokemonId parameter
- Cast Swift `Int` to Kotlin `Int32`: `Int32(pokemonId)`
- Call helper function with parameter
- Store ViewModel instance for lifecycle

#### SwiftUI View with Parametric Wrapper

```swift
// iosApp/Views/PokemonDetailView.swift
struct PokemonDetailView: View {
    let pokemonId: Int
    @StateObject private var wrapper: PokemonDetailViewModelWrapper
    
    init(pokemonId: Int) {
        self.pokemonId = pokemonId
        _wrapper = StateObject(wrappedValue: PokemonDetailViewModelWrapper(pokemonId: pokemonId))
    }
    
    var body: some View {
        switch wrapper.uiState {
        case is PokemonDetailUiStateLoading:
            ProgressView("Loading...")
        case let content as PokemonDetailUiStateContent:
            DetailContentView(pokemon: content.pokemon)
        case let error as PokemonDetailUiStateError:
            ErrorView(message: error.message, onRetry: { wrapper.retry() })
        default:
            EmptyView()
        }
    }
    .task {
        await wrapper.observeState()
    }
}
```

**Key Points**:
- View takes pokemonId as init parameter
- `@StateObject` initialized with `StateObject(wrappedValue: ...)` in init
- `_wrapper` (with underscore) accesses property wrapper initializer
- Pass pokemonId to wrapper initializer
- Rest of pattern identical to non-parametric ViewModels

**Complete Flow**:
1. SwiftUI view receives `pokemonId` parameter
2. View creates `@StateObject` wrapper with `pokemonId`
3. Wrapper calls Kotlin helper: `getPokemonDetailViewModel(pokemonId: Int32)`
4. Helper calls Koin: `get { parametersOf(pokemonId) }`
5. Koin factory receives parameter: `params.get()`
6. ViewModel initialized with `pokemonId` and `repository`

---

### 3. Swift String Formatting

**Problem**: Swift string interpolation doesn't support format specifiers like Kotlin.

```swift
// ❌ Swift compile error
let height = "Height: \(pokemon.height/10.0:.1f) m"
// Error: Cannot use specifier with string interpolation
```

**Solution**: Use `String(format:_:)` for formatted output.

```swift
// ✅ Correct Swift syntax
let height = String(format: "%.1f m", Double(pokemon.height) / 10.0)
let weight = String(format: "%.1f kg", Double(pokemon.weight) / 10.0)
let id = String(format: "#%03d", Int(pokemon.id))
```

**Common Format Specifiers**:
- `%.1f` - Float with 1 decimal place
- `%.2f` - Float with 2 decimal places
- `%03d` - Integer with leading zeros (3 digits)
- `%d` - Integer
- `%@` - String (for Swift objects)

**Type Conversions for Formatting**:
```swift
// Kotlin Int32 → Swift Int
String(format: "#%03d", Int(pokemon.id))

// Kotlin Int32 → Swift Double for division
String(format: "%.1f m", Double(pokemon.height) / 10.0)

// Explicit Double wrapper
let heightInMeters = Double(pokemon.height) / 10.0
let heightString = String(format: "%.1f m", heightInMeters)
```

**Why Not Interpolation?**: Swift doesn't support inline format specifiers in string interpolation. For simple concatenation without formatting, interpolation works:

```swift
// ✅ Simple interpolation (no formatting)
let name = "Name: \(pokemon.name)"
let count = "Total: \(pokemon.stats.count)"

// ❌ Formatted interpolation (use String(format:) instead)
let height = "Height: \(pokemon.height/10.0:.1f) m"  // Compile error
```

---

### 2. Koin DI from Swift

#### Pattern: Helper Functions

Directly using Koin's Swift API is complex due to generics. **Solution**: Create Kotlin helper functions.

**Kotlin Side** (`shared/src/iosMain/kotlin/KoinIos.kt`):
```kotlin
package com.minddistrict.multiplatformpoc

import com.minddistrict.multiplatformpoc.core.di.coreModule
import com.minddistrict.multiplatformpoc.features.pokemonlist.wiring.pokemonListModule
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform

/**
 * Initialize Koin for iOS.
 * Call from SwiftUI App's init().
 */
fun initKoin(baseUrl: String) {
    startKoin {
        modules(
            coreModule(baseUrl),
            pokemonListModule
            // Note: Do NOT include platform navigation modules (Android/JVM only)
        )
    }
}

/**
 * Helper to get PokemonListViewModel from Koin.
 * Avoids complex Koin Swift API generics.
 */
fun getPokemonListViewModel(): com.minddistrict.multiplatformpoc.features.pokemonlist.presentation.PokemonListViewModel {
    return KoinPlatform.getKoin().get()
}
```

**Swift Side** (App entry point):
```swift
import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        // Initialize Koin before any views are created
        KoinIosKt.doInitKoin(baseUrl: "https://pokeapi.co/api/v2")
    }
    
    var body: some Scene {
        WindowGroup {
            PokemonListView()
        }
    }
}
```

**Why Helper Functions?**
- ✅ Avoids Koin's complex generic Swift API (`koin.get(qualifier:parameters:)`)
- ✅ Type-safe: Kotlin enforces return types
- ✅ Simple Swift call: `KoinIosKt.getPokemonListViewModel()`
- ✅ No manual casting needed

**When to Create Helper Functions**:
- For every ViewModel you need to inject from Swift
- For any Koin dependencies accessed directly from iOS
- Keep them in `shared/src/iosMain/kotlin/KoinIos.kt`

---

### 3. StateFlow Observation

#### SKIE Automatic Bridging

SKIE automatically converts Kotlin `StateFlow` to Swift `AsyncSequence`, enabling native `for await ... in` syntax:

```swift
// SKIE makes this possible:
for await state in viewModel.uiState {
    // Receive StateFlow emissions
}
```

**Key Points**:
- ✅ SKIE makes `StateFlow` iterable with `for await ... in`
- ✅ Use `.task` modifier for automatic cancellation
- ✅ Properly cancels when view disappears (no memory leaks)

#### Observation Pattern: Direct Integration (Current)

```swift
import SwiftUI
import Shared

struct PokemonListView: View {
    // Direct ViewModel access from Koin
    private var viewModel = KoinIosKt.getPokemonListViewModel()
    
    // @State bridges StateFlow emissions to SwiftUI
    @State private var uiState: PokemonListUiState = PokemonListUiStateLoading()
    
    var body: some View {
        content
            .task {
                // Observe StateFlow via SKIE AsyncSequence
                // Auto-cancels when view disappears
                for await state in viewModel.uiState {
                    self.uiState = state
                }
            }
    }
}
```

**Key Points**:
- ✅ Direct ViewModel access (no wrapper)
- ✅ `@State` holds current UI state for SwiftUI reactivity
- ✅ `.task` lifecycle automatically cancels on view disappear
- ✅ Simple, minimal boilerplate

#### Alternative: Wrapper Pattern

See "ViewModel Integration Approaches" section above for Wrapper pattern with `@ObservableObject` + `@StateObject`.

---

### 4. SwiftUI View Integration

**Current Pattern**: Direct Integration

```swift
import SwiftUI
import Shared

struct PokemonListView: View {
    private var viewModel = KoinIosKt.getPokemonListViewModel()
    @State private var uiState: PokemonListUiState = PokemonListUiStateLoading()
    
    var body: some View {
        NavigationStack {
            // Switch on UI state sealed class
            switch uiState {
            case is PokemonListUiStateLoading:
                ProgressView("Loading Pokémon...")
                
            case let error as PokemonListUiStateError:
                ErrorView(message: error.message)
                
            case let content as PokemonListUiStateContent:
                PokemonGridView(
                    content: content,
                    onLoadMore: { viewModel.loadNextPage() }
                )
                
            default:
                EmptyView()
            }
        }
        .onAppear {
            // Load data on first appear
            if case is PokemonListUiStateLoading = uiState {
                viewModel.loadInitialPage()
            }
        }
        .task {
            // Observe StateFlow - auto-cancels on view disappear
            for await state in viewModel.uiState {
                self.uiState = state
            }
        }
    }
}
```

**Critical Requirements**:
- ✅ Call ViewModel methods directly (e.g., `viewModel.loadInitialPage()`)
- ✅ Observe StateFlow in `.task` modifier (not `.onAppear`)
- ✅ Load initial data in `.onAppear` (one-time action)
- ✅ Switch on sealed class types with `is` and `as`
- ✅ Check current state before loading to avoid redundant calls

---

### 5. Type Conversions

#### Kotlin Int → Swift Int32

Kotlin's `Int` maps to Swift's `Int32`, not `Int`. **Explicit casting required**.

**Problem**:
```swift
// ❌ Compile error: Cannot assign Int32 to Int
scrollPosition = pokemon.id  // pokemon.id is Int32
navigationPath.append(pokemon.id)
```

**Solution**:
```swift
// ✅ Explicit cast
scrollPosition = Int(pokemon.id)
navigationPath.append(Int(pokemon.id))
```

**When to Cast**:
- Assigning Kotlin `Int` to Swift `Int` variables
- Passing Kotlin `Int` to Swift APIs expecting `Int`
- Formatting strings: `String(format: "%03d", Int(pokemon.id))`

**Rule**: Always cast Kotlin numeric types when interfacing with Swift stdlib.

---

## 📋 iOS Module Export Rules

### What Gets Exported to iOS

**Via `:shared` umbrella framework** (`shared/build.gradle.kts`):

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
        implementation(libs.koin.core)
    }
}
```

**Exported Modules** (accessible from Swift):
- ✅ `:features:<feature>:api` — interfaces, domain models, navigation contracts
- ✅ `:features:<feature>:presentation` — ViewModels, UI state sealed classes
- ✅ `:core:domain` — shared domain models
- ✅ `:core:util` — cross-platform utilities

**NOT Exported** (internal to KMP):
- ❌ `:features:<feature>:data` — repositories, API services, DTOs, mappers
- ❌ `:features:<feature>:ui` — Compose UI (Android/Desktop only)
- ❌ `:features:<feature>:wiring` — DI modules (Koin)
- ❌ `:core:designsystem` — Compose components
- ❌ `:core:httpclient` — Ktor client configuration

**Why This Split?**
- iOS needs ViewModels and domain models (shared business logic)
- iOS uses native SwiftUI (not Compose UI)
- iOS accesses repositories via ViewModels (not directly)
- DI wiring happens in Kotlin (iOS just calls helper functions)

---

## 🛠️ Common Patterns

### Pattern 1: Observing Sealed UI States

```swift
switch wrapper.uiState {
case is PokemonListUiStateLoading:
    ProgressView("Loading...")
    
case let error as PokemonListUiStateError:
    VStack {
        Text("Error: \(error.message)")
        Button("Retry") { wrapper.loadInitialPage() }
    }
    
case let content as PokemonListUiStateContent:
    List(content.pokemons, id: \.id) { pokemon in
        PokemonRow(pokemon: pokemon)
    }
    
default:
    EmptyView()
}
```

**Key Points**:
- Use `is` for type checking without binding
- Use `let ... as` for type checking with binding
- Always include `default` case (Swift requirement for sealed classes)

---

### Pattern 2: Infinite Scroll

```swift
ScrollView {
    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())]) {
        ForEach(Array(content.pokemons.enumerated()), id: \.element.id) { index, pokemon in
            PokemonCard(pokemon: pokemon)
                .onAppear {
                    // Trigger load when reaching last 4 items
                    if index >= content.pokemons.count - 4 && content.hasMore && !content.isLoadingMore {
                        wrapper.loadNextPage()
                    }
                }
        }
    }
}
```

**Key Points**:
- Use `enumerated()` to get index and element
- Load next page when user scrolls near bottom (last 4 items)
- Check `hasMore` and `!isLoadingMore` to prevent duplicate loads
- Convert `KotlinArray` to Swift array with `Array()`

---

### Pattern 3: Scroll Position Preservation

```swift
struct PokemonListView: View {
    @State private var scrollPosition: Int?
    
    var body: some View {
        ScrollViewReader { proxy in
            ScrollView {
                LazyVGrid(...) {
                    ForEach(...) { index, pokemon in
                        PokemonCard(pokemon: pokemon) {
                            // Save position before navigating
                            scrollPosition = Int(pokemon.id)
                            // Navigate...
                        }
                        .id(Int(pokemon.id))  // Scroll anchor
                    }
                }
            }
            .onAppear {
                // Restore scroll position on return
                if let position = scrollPosition {
                    proxy.scrollTo(position, anchor: .top)
                }
            }
        }
    }
}
```

**Key Points**:
- Use `ScrollViewReader` to control scroll position
- Assign `.id()` to each card for scroll anchoring
- Save `scrollPosition` before navigation
- Restore position in `.onAppear` when returning

---

### Pattern 4: Native iOS Styling

```swift
// Use iOS semantic colors for automatic dark mode
.foregroundColor(.primary)  // Black in light, white in dark
.foregroundColor(.secondary)  // Gray, adapts to theme
.background(Color(.systemBackground))  // White/black background
.background(Color(.secondarySystemBackground))  // Card backgrounds

// Use iOS system fonts
Text(pokemon.name)
    .font(.headline)  // System font, adapts to user settings
    .fontWeight(.semibold)

// Use iOS-style shapes
RoundedRectangle(cornerRadius: 12)  // iOS standard corner radius

// Use native SF Symbols
Image(systemName: "exclamationmark.triangle")
    .foregroundColor(.red)
```

**Why Semantic Colors?**
- Automatic dark mode support
- Respects user accessibility settings (contrast, reduced transparency)
- Feels native to iOS users

---

## 🚀 Adding a New Feature to iOS

### Checklist

1. **Create ViewModel Helper in Kotlin**
   ```kotlin
   // shared/src/iosMain/kotlin/KoinIos.kt
   fun getJobListViewModel(): com.minddistrict.multiplatformpoc.features.jobs.presentation.JobListViewModel {
       return KoinPlatform.getKoin().get()
   }
   ```

2. **Export Modules in shared/build.gradle.kts**
   ```kotlin
   export(projects.features.jobs.api)
   export(projects.features.jobs.presentation)
   ```

3. **Rebuild iOS Framework**
   ```bash
   ./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
   ```

4. **Create Swift ViewModel Wrapper**
   ```swift
   @MainActor
   class JobListViewModelWrapper: ObservableObject {
       @Published var uiState: JobListUiState = JobListUiStateLoading()
       private let viewModel: JobListViewModel
       
       init() {
           self.viewModel = KoinIosKt.getJobListViewModel()
       }
       
       func observeState() async {
           for await state in viewModel.uiState {
               self.uiState = state
           }
       }
   }
   ```

5. **Create SwiftUI View**
   ```swift
   struct JobListView: View {
       @StateObject private var wrapper = JobListViewModelWrapper()
       
       var body: some View {
           // Switch on uiState...
       }
       .task { await wrapper.observeState() }
   }
   ```

6. **Add to Navigation**
   ```swift
   NavigationStack(path: $navigationPath) {
       // Root view
   }
   .navigationDestination(for: JobRoute.self) { route in
       JobListView()
   }
   ```

---

## 🧪 Testing iOS Integration

### Unit Testing Swift Wrappers

```swift
import XCTest
@testable import iosApp
import Shared

@MainActor
class PokemonListViewModelWrapperTests: XCTestCase {
    var wrapper: PokemonListViewModelWrapper!
    
    override func setUp() async throws {
        KoinIosKt.doInitKoin(baseUrl: "https://test.api")
        wrapper = PokemonListViewModelWrapper()
    }
    
    override func tearDown() {
        KoinIosKt.stopKoin()
    }
    
    func testInitialStateIsLoading() {
        XCTAssertTrue(wrapper.uiState is PokemonListUiStateLoading)
    }
    
    func testLoadInitialPageDelegatesToViewModel() {
        wrapper.loadInitialPage()
        // Verify state changes to content/error
    }
}
```

**Testing Strategy**:
- Initialize Koin before each test
- Stop Koin after each test
- Test wrapper delegates to ViewModel correctly
- Verify state transitions
- Mock network in KMP layer (not Swift)

---

### UI Testing with SwiftUI Previews

```swift
#Preview {
    PokemonListView()
        .onAppear {
            // Mock Koin for preview
            KoinIosKt.doInitKoin(baseUrl: "https://preview.api")
        }
}

#Preview("Loading State") {
    // Show loading state
}

#Preview("Error State") {
    // Show error state
}

#Preview("Content State") {
    // Show populated content
}
```

**Why Multiple Previews?**
- Test all UI states visually
- Faster than running simulator
- Design iteration without rebuilding

---

## 🐛 Troubleshooting

### Issue 1: "No definition found for PokemonListViewModel"

**Cause**: Koin not initialized or module not included.

**Solution**:
```swift
// Verify init is called BEFORE wrapper creation
@main
struct iOSApp: App {
    init() {
        KoinIosKt.doInitKoin(baseUrl: "https://pokeapi.co/api/v2")
    }
}
```

**Check Kotlin Side**:
```kotlin
fun initKoin(baseUrl: String) {
    startKoin {
        modules(
            coreModule(baseUrl),
            pokemonListModule  // ← Ensure module is included
        )
    }
}
```

---

### Issue 2: "Cannot assign Int32 to Int"

**Cause**: Kotlin `Int` maps to Swift `Int32`.

**Solution**:
```swift
// Add explicit cast
scrollPosition = Int(pokemon.id)
```

---

### Issue 3: StateFlow not updating UI

**Cause**: Not calling `observeState()` in `.task` modifier.

**Solution**:
```swift
.task {
    await wrapper.observeState()  // ← Must be in .task, not .onAppear
}
```

**Why?**
- `.task` auto-cancels when view disappears (prevents leaks)
- `.onAppear` doesn't support async/await properly

---

### Issue 4: Framework not found in Xcode

**Cause**: iOS framework not built or Xcode cache stale.

**Solution**:
```bash
# Rebuild framework
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64

# Clean Xcode
rm -rf ~/Library/Developer/Xcode/DerivedData
```

---

### Issue 5: SKIE not bridging StateFlow

**Cause**: SKIE plugin not applied or wrong version.

**Solution**:
```kotlin
// shared/build.gradle.kts
plugins {
    alias(libs.plugins.skie)  // ← Ensure SKIE is applied
}

// gradle/libs.versions.toml
[versions]
skie = "0.10.8"  // ← Compatible with Kotlin 2.2.21
```

---

## 📊 Performance Considerations

### Memory Management

**StateFlow Observation**:
- ✅ Use `.task` modifier (auto-cancels, prevents leaks)
- ❌ Don't manually store `Task` references
- ✅ SKIE handles Kotlin coroutine lifecycle

**ViewModel Lifecycle**:
- ViewModels are created per wrapper instance
- Koin manages ViewModel lifecycle (factory scope)
- SwiftUI manages wrapper lifecycle (`@StateObject`)

---

### Build Times

**iOS Framework Build**: ~1-2 minutes (with SKIE)
- Only rebuild when KMP code changes
- Use Gradle cache (`--build-cache`)
- Xcode incremental builds are fast (~10s)

**Comparison**:
- Android build: 45s (for validation)
- iOS framework: 1-2min (only when needed)
- Xcode incremental: 10s (SwiftUI changes)

**Rule**: Use Android builds for KMP validation, rebuild iOS framework only when deploying to iOS.

---

## 🎯 Best Practices

### ✅ DO

1. **Use SKIE for StateFlow bridging** (automatic, no manual code)
2. **Create Kotlin helper functions** for Koin injection (avoid Swift generics)
3. **Export only API + Presentation** modules to iOS (not data/ui/wiring)
4. **Use native SwiftUI patterns** (NavigationStack, AsyncImage, semantic colors)
5. **Wrap ViewModels in @ObservableObject** (bridge to SwiftUI reactive system)
6. **Call observeState() in .task** (auto-cancellation)
7. **Cast Kotlin Int to Swift Int** (explicit conversions)
8. **Initialize Koin in App init()** (before any views)

### ❌ DON'T

1. **Don't use Koin's generic Swift API directly** (complex, error-prone)
2. **Don't export data/ui/wiring modules** to iOS (internal implementation)
3. **Don't use .onAppear for async observation** (use .task)
4. **Don't manually bridge StateFlow** (SKIE handles it)
5. **Don't build iOS framework for routine validation** (use Android builds)
6. **Don't store Task references manually** (let .task manage lifecycle)
7. **Don't assume Kotlin Int == Swift Int** (always cast)
8. **Don't create Compose UI for iOS** (use native SwiftUI)

---

## 📚 Reference Implementation

See **Pokemon List** feature for complete working example:

**Kotlin (KMP)**:
- `features/pokemonlist/presentation/src/commonMain/kotlin/.../PokemonListViewModel.kt`
- `features/pokemonlist/wiring/src/commonMain/kotlin/.../PokemonListModule.kt`
- `shared/src/iosMain/kotlin/com/minddistrict/multiplatformpoc/KoinIos.kt`
- `shared/build.gradle.kts` (SKIE + exports)

**Swift (iOS)**:
- `iosApp/iosApp/ViewModels/PokemonListViewModelWrapper.swift`
- `iosApp/iosApp/Views/PokemonListView.swift`
- `iosApp/iosApp/Views/PokemonCard.swift`
- `iosApp/iosApp/iOSApp.swift` (Koin initialization)

---

## 🔗 Related Documentation

- [Presentation Layer](./presentation_layer.md) — ViewModel patterns
- [Dependency Injection](./dependency_injection.md) — Koin setup
- [Testing Strategy](./testing_strategy.md) — Testing approach
- [SKIE Documentation](https://skie.touchlab.co/) — Official SKIE docs
- [Kotlin/Native Interop](https://kotlinlang.org/docs/native-objc-interop.html) — Kotlin ↔ Swift types

---

**Last Verified**: November 26, 2025 with:
- Kotlin: 2.2.21
- SKIE: 0.10.8
- Koin: 4.0.1
- Xcode: 15+
- iOS Deployment Target: 15+
