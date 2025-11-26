# iOS Integration Guide - SwiftUI + KMP ViewModels

> **Status**: ✅ Production Pattern Established  
> **Last Updated**: December 2025  
> **Pattern**: Native SwiftUI UI consuming shared KMP ViewModels via SKIE  
> **Note**: This guide covers **iosApp** (native SwiftUI). For iOS Compose app (**iosAppCompose**), see Compose Multiplatform iOS documentation.

---

## Overview

This project has **TWO iOS app options**:

1. **iosApp** (production - this guide): Native SwiftUI UI consuming shared KMP ViewModels via SKIE
2. **iosAppCompose** (experimental): Compose Multiplatform UI sharing @Composable screens with Android/Desktop

**This guide covers iosApp only.** The native SwiftUI approach was deliberately chosen for platform consistency and ecosystem access.

### Architecture Summary

```
┌─────────────────────────────────────────────────────────────┐
│  iOS App (SwiftUI)                                          │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  Views (SwiftUI)                                       │  │
│  │  - PokemonListView                                     │  │
│  │  - PokemonDetailView                                   │  │
│  │  - NavigationStack (native iOS)                        │  │
│  └───────────────────────────────────────────────────────┘  │
│                           │                                  │
│                           ▼                                  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  ViewModel Wrappers (@ObservableObject)               │  │
│  │  - PokemonListViewModelWrapper                        │  │
│  │  - Bridge KMP ViewModels to SwiftUI                   │  │
│  └───────────────────────────────────────────────────────┘  │
│                           │                                  │
│                           ▼                                  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  Shared.framework (SKIE-enhanced)                     │  │
│  │  - Koin DI initialization                             │  │
│  │  - Helper functions (getPokemonListViewModel)         │  │
│  │  - StateFlow → AsyncSequence bridging                 │  │
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

---

## 🎯 Key Patterns

### 1. SKIE Integration

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

#### Pattern: SKIE Automatic Bridging + Swift Async/Await

**Swift ViewModel Wrapper**:
```swift
import Foundation
import Shared
import SwiftUI

@MainActor
class PokemonListViewModelWrapper: ObservableObject {
    @Published var uiState: PokemonListUiState = PokemonListUiStateLoading()
    
    private let viewModel: PokemonListViewModel
    
    init() {
        // Get ViewModel from Koin via helper function
        self.viewModel = KoinIosKt.getPokemonListViewModel()
    }
    
    /**
     * Observe StateFlow and update published property.
     * SKIE automatically provides async iteration for StateFlow.
     * Call from SwiftUI .task modifier (auto-cancels on view disappear).
     */
    func observeState() async {
        for await state in viewModel.uiState {
            self.uiState = state
        }
    }
    
    // Delegate method calls to KMP ViewModel
    func loadInitialPage() {
        viewModel.loadInitialPage()
    }
    
    func loadNextPage() {
        viewModel.loadNextPage()
    }
}
```

**Key Points**:
- ✅ SKIE makes `StateFlow` iterable with `for await ... in`
- ✅ Use `.task` modifier for automatic cancellation
- ✅ `@Published` property triggers SwiftUI re-renders
- ✅ `@MainActor` ensures UI updates on main thread

---

### 4. SwiftUI View Integration

**Pattern**: @StateObject + .task lifecycle

```swift
import SwiftUI
import Shared

struct PokemonListView: View {
    @StateObject private var wrapper = PokemonListViewModelWrapper()
    @State private var scrollPosition: Int?
    
    var body: some View {
        NavigationStack {
            // Switch on UI state sealed class
            switch wrapper.uiState {
            case is PokemonListUiStateLoading:
                ProgressView("Loading Pokémon...")
                
            case let error as PokemonListUiStateError:
                ErrorView(message: error.message)
                
            case let content as PokemonListUiStateContent:
                PokemonGridView(
                    content: content,
                    onLoadMore: { wrapper.loadNextPage() },
                    scrollPosition: $scrollPosition
                )
                
            default:
                EmptyView()
            }
        }
        .onAppear {
            wrapper.loadInitialPage()
        }
        .task {
            // SKIE-enabled StateFlow observation
            // Auto-cancels when view disappears
            await wrapper.observeState()
        }
    }
}
```

**Critical Requirements**:
- ✅ Use `@StateObject` for wrapper (not `@ObservedObject` or `@State`)
- ✅ Call `observeState()` in `.task` modifier (not `.onAppear`)
- ✅ Load initial data in `.onAppear` (one-time action)
- ✅ Switch on sealed class types with `is` and `as`

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
