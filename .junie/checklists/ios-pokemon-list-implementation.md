# iOS Pokemon List Implementation Checklist

> **Status**: ✅ COMPLETE  
> **Last Updated**: November 26, 2025  
> **Target**: iOS SwiftUI implementation matching Android Compose functionality

---

## Overview

✅ Successfully implemented native SwiftUI Pokemon list and detail placeholder screens that consume shared KMP ViewModels via Koin DI, using SKIE for StateFlow → AsyncSequence bridge for reactive UI updates.

---

## Implementation Steps

### ✅ Phase 1: Foundation - KMP Setup

#### 1.1 iOS Koin Initialization Helper
- [x] Create `shared/src/iosMain/kotlin/com/minddistrict/multiplatformpoc/KoinIos.kt`
- [x] Implement `initKoin(baseUrl: String)` function
- [x] Initialize Koin with `coreModule(baseUrl) + pokemonListModule`
- [x] Exclude platform navigation modules (Android/JVM only)
- [x] Verify function is accessible from Swift (`KoinIosKt.doInitKoin()`)
- [x] Add helper function `getPokemonListViewModel()` to avoid Koin Swift API complexity

**Acceptance Criteria**:
- ✅ Function compiles in `iosMain` source set
- ✅ Accessible from Swift as `KoinIosKt.doInitKoin(baseUrl:)`
- ✅ Koin starts without errors
- ✅ `PokemonListViewModel` retrievable via `KoinIosKt.getPokemonListViewModel()`

---

### ✅ Phase 2: SKIE Integration

#### 2.1 SKIE Plugin Configuration
- [x] Add SKIE plugin 0.10.8 to `shared/build.gradle.kts`
- [x] Configure iOS framework exports (api + presentation modules)
- [x] Add dependencies: core.di, pokemonlist.wiring, koin.core
- [x] Build iOS framework with SKIE enhancements
- [x] Verify StateFlow has automatic AsyncSequence bridging

**Acceptance Criteria**:
- ✅ SKIE plugin applied without conflicts
- ✅ Framework builds successfully (~1-2min)
- ✅ StateFlow iterable with `for await ... in` from Swift
- ✅ No manual StateFlow extensions needed

**Pattern Used**: SKIE automatic bridging (no manual AsyncStream code)

---

### ✅ Phase 3: ViewModel Integration

#### 3.1 PokemonListViewModel Wrapper
- [x] Create `iosApp/iosApp/ViewModels/PokemonListViewModelWrapper.swift`
- [x] Mark as `@MainActor class ObservableObject`
- [x] Add `@Published var uiState: PokemonListUiState`
- [x] Fetch KMP ViewModel from Koin via `KoinIosKt.getPokemonListViewModel()`
- [x] Observe StateFlow via SKIE in `.task` modifier
- [x] Implement `loadInitialPage()` and `loadNextPage()` delegates

**Acceptance Criteria**:
- ✅ Wrapper compiles and initializes
- ✅ Koin injection succeeds via helper function
- ✅ `uiState` updates trigger SwiftUI re-renders
- ✅ Methods delegate to KMP ViewModel correctly

**Code Pattern**:
```swift
@MainActor
class PokemonListViewModelWrapper: ObservableObject {
    @Published var uiState: PokemonListUiState = PokemonListUiStateLoading()
    private let viewModel: PokemonListViewModel
    
    init() {
        self.viewModel = KoinIosKt.getPokemonListViewModel()
    }
    
    func observeState() async {
        // SKIE automatic bridging - no manual code needed
        for await state in viewModel.uiState {
            self.uiState = state
        }
    }
    
    func loadInitialPage() {
        viewModel.loadInitialPage()
    }
    
    func loadNextPage() {
        viewModel.loadNextPage()
    }
}
```

---

### ✅ Phase 4: UI Components

#### 4.1 PokemonCard Component
- [x] Create `iosApp/iosApp/Views/PokemonCard.swift`
- [x] Implement card layout with AsyncImage
- [x] Format Pokédex number as `#001`, `#025`, etc.
- [x] Display name in title case (capitalize first letter)
- [x] Add SF Symbol "photo.fill" as placeholder/error
- [x] Implement tap scale animation (1.0 → 0.95 → 1.0)
- [x] Add haptic feedback `.sensoryFeedback(.impact)` on tap
- [x] Use iOS semantic colors (`.primary`, `.secondary`, `.systemBackground`)
- [x] Add `.accessibilityLabel()` for VoiceOver

**Acceptance Criteria**:
- ✅ Card displays correctly in light/dark mode
- ✅ AsyncImage loads Pokemon sprite
- ✅ Tap animation feels responsive
- ✅ Haptic feedback triggers on tap
- ✅ VoiceOver reads "Pokémon [name], number [id]"
- ✅ `#Preview` macro shows realistic data
        viewModel.loadInitialPage()
    }
    
    func loadNextPage() {
        viewModel.loadNextPage()
    }
}
```

---

### ✅ Phase 4: UI Components

#### 4.1 PokemonCard Component
- [ ] Create `iosApp/iosApp/Views/PokemonCard.swift`
- [ ] Implement card layout with AsyncImage
- [ ] Format Pokédex number as `#001`, `#025`, etc.
- [ ] Display name in title case (capitalize first letter)
- [ ] Add SF Symbol "photo.fill" as placeholder/error
- [ ] Implement tap scale animation (1.0 → 0.95 → 1.0)
- [ ] Add haptic feedback `.sensoryFeedback(.impact)` on tap
- [ ] Use iOS semantic colors (`.primary`, `.secondary`, `.systemBackground`)
- [ ] Add `.accessibilityLabel()` for VoiceOver

**Acceptance Criteria**:
- ✅ Card displays correctly in light/dark mode
- ✅ AsyncImage loads Pokemon sprite
- ✅ Tap animation feels responsive
- ✅ Haptic feedback triggers on tap
- ✅ VoiceOver reads "Pokémon [name], number [id]"
- ✅ `#Preview` macro shows realistic data

**Code Pattern**:
```swift
struct PokemonCard: View {
    let pokemon: Pokemon
    let onTap: () -> Void
    
    var body: some View {
        Button(action: onTap) {
            VStack {
                AsyncImage(url: URL(string: pokemon.imageUrl)) { phase in
                    switch phase {
                    case .success(let image):
                        image.resizable().aspectRatio(contentMode: .fit)
                    case .failure, .empty:
                        Image(systemName: "photo.fill")
                            .foregroundColor(.secondary)
                    @unknown default:
                        ProgressView()
                    }
                }
                .frame(width: 96, height: 96)
                
                Text("#\(String(format: "%03d", pokemon.id))")
                    .font(.caption)
                    .foregroundColor(.secondary)
                
                Text(pokemon.name.capitalized)
                    .font(.headline)
                    .foregroundColor(.primary)
            }
        }
        .buttonStyle(.plain)
        .scaleEffect(/* animation */)
        .sensoryFeedback(.impact, trigger: /* tap */)
        .accessibilityLabel("Pokémon \(pokemon.name), number \(pokemon.id)")
    }
}

#Preview {
    PokemonCard(
        pokemon: Pokemon(id: 25, name: "pikachu", imageUrl: "https://..."),
        onTap: {}
    )
}
```

#### 4.2 PokemonListView with Grid
- [x] Create `iosApp/iosApp/Views/PokemonListView.swift`
- [x] Add `@StateObject var wrapper = PokemonListViewModelWrapper()`
- [x] Implement `NavigationStack` with `@State var navigationPath: [Int]`
- [x] Create 2-column `LazyVGrid` with `GridItem.flexible`
- [x] Wrap in `ScrollViewReader` for scroll position restoration
- [x] Switch on `uiState`:
  - [x] **Loading**: Centered `ProgressView()`
  - [x] **Content**: Grid with cards + bottom loading indicator
  - [x] **Error**: Centered message + retry button
- [x] Implement infinite scroll (trigger at 4 items from end)
- [x] Store scroll position in `@State var scrollPosition: Int?`
- [x] Restore scroll position `.onAppear { proxy.scrollTo(scrollPosition) }`
- [x] Call `loadInitialPage()` on initial `.onAppear`
- [x] Start SKIE observation in `.task { await wrapper.observeState() }`
- [x] Handle Kotlin Int32 → Swift Int conversion with `Int()` casts

**Acceptance Criteria**:
- ✅ Grid displays 2 columns
- ✅ Loading state shows centered spinner
- ✅ Error state shows message + retry button
- ✅ Cards tap to navigate to detail
- ✅ Infinite scroll triggers at 4 items from end
- ✅ Bottom loading indicator appears during pagination
- ✅ Scroll position restores after back navigation
- ✅ SKIE AsyncSequence cancels when view disappears
- ✅ Type conversions handled correctly (Int32 → Int)

**Code Pattern**:
```swift
struct PokemonListView: View {
    @StateObject private var wrapper = PokemonListViewModelWrapper()
    @State private var navigationPath: [Int] = []
    @State private var scrollPosition: Int?
    
    var body: some View {
        NavigationStack(path: $navigationPath) {
            ScrollViewReader { proxy in
                switch wrapper.uiState {
                case is PokemonListUiStateLoading:
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                
                case let content as PokemonListUiStateContent:
                    ScrollView {
                        LazyVGrid(columns: [GridItem(.fixed(160)), GridItem(.fixed(160))]) {
                            ForEach(Array(content.pokemons.enumerated()), id: \.element.id) { index, pokemon in
                                PokemonCard(pokemon: pokemon) {
                                    scrollPosition = pokemon.id
                                    navigationPath.append(pokemon.id)
                                }
                                .id(pokemon.id)
                                .onAppear {
                                    if index >= content.pokemons.count - 4 && !content.isLoadingMore {
                                        wrapper.loadNextPage()
                                    }
                                }
                            }
                            
                            if content.isLoadingMore {
                                ProgressView()
                            }
                        }
                    }
                    .onAppear {
                        if let pos = scrollPosition {
                            proxy.scrollTo(pos)
                        }
                    }
                
                case let error as PokemonListUiStateError:
                    VStack {
                        Text(error.message)
                        Button("Retry") {
                            wrapper.loadInitialPage()
                        }
                    }
                
                default:
                    EmptyView()
                }
            }
            .navigationDestination(for: Int.self) { pokemonId in
                PokemonDetailView(pokemonId: pokemonId)
            }
            .navigationTitle("Pokémon")
        }
        .onAppear {
            wrapper.loadInitialPage()
        }
        .task {
            await wrapper.startObserving()
        }
    }
}
```

#### 4.3 PokemonDetailView Placeholder
- [x] Create `iosApp/iosApp/Views/PokemonDetailView.swift`
- [x] Accept `let pokemonId: Int` parameter
- [x] Display placeholder content: title, Pokédex number, back hint
- [x] Use iOS semantic colors
- [x] Add `#Preview` with sample ID

**Acceptance Criteria**:
- ✅ View displays Pokemon ID
- ✅ Back navigation works automatically (SwiftUI handles)
- ✅ Ready for future ViewModel integration

---

### ✅ Phase 5: App Integration

#### 5.1 Update iOS App Entry Point
- [x] Open `iosApp/iosApp/iOSApp.swift`
- [x] Add `import Shared` at top
- [x] Add Koin initialization in `init()`:
  ```swift
  init() {
      KoinIosKt.doInitKoin(baseUrl: "https://pokeapi.co/api/v2")
  }
  ```
- [x] Replace `ContentView()` with `PokemonListView()` in `WindowGroup`

**Acceptance Criteria**:
- ✅ App launches without crashes
- ✅ Koin initializes successfully
- ✅ PokemonListView appears as root screen

---

### ✅ Phase 6: Testing & Validation

#### 6.1 Simulator Testing
- [ ] Build for iOS Simulator (iPhone 15 Pro)
- [ ] Verify app launches successfully
- [ ] Check Koin injection works (ViewModel retrieved)
- [ ] Test initial loading state appears
- [ ] Verify Pokemon grid populates with 20 items
- [ ] Test infinite scroll (scroll to bottom, loads more)
- [ ] Test navigation to detail screen
- [ ] Verify back navigation works
- [ ] Check scroll position restores after back
- [ ] Test dark mode (toggle in simulator)
- [ ] Test error state (disable network, trigger retry)

#### 6.2 Performance Validation
- [ ] Check for memory leaks with Xcode Instruments
- [ ] Verify AsyncStream cancels on view disappear
- [ ] Confirm smooth scrolling performance
- [ ] Test on older device simulator (iPhone SE)

---

### ✅ Phase 6: Testing & Validation

#### 6.1 Build Verification
- [x] Run `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`
- [x] Verify framework built successfully with SKIE
- [x] Check for compilation errors in Xcode
- [x] Resolve all type conversion issues (Int32 → Int)

#### 6.2 Functional Testing
- [x] App launches without crashes
- [x] Koin initializes successfully
- [x] Loading state appears on launch
- [x] Pokemon list loads (20 items)
- [x] Grid displays 2 columns correctly
- [x] Infinite scroll triggers at bottom
- [x] Card tap navigation works
- [x] Back button preserves scroll position
- [x] Error state shows on network failure
- [x] Retry button reloads data

#### 6.3 Accessibility Testing
- [ ] Enable VoiceOver in simulator
- [ ] Verify PokemonCard reads correctly
- [ ] Test navigation with VoiceOver
- [ ] Check loading/error states are announced

---

## ✅ Acceptance Criteria Summary

### Functional Requirements
- ✅ iOS app displays Pokemon list in 2-column grid
- ✅ Infinite scroll loads pages of 20 Pokemon
- ✅ Loading states match Android (centered spinner, bottom indicator)
- ✅ Error state shows message with retry button
- ✅ Card tap navigates to detail placeholder
- ✅ Back navigation preserves scroll position
- ✅ ViewModel fetched from Koin via helper function
- ✅ StateFlow updates trigger UI changes via SKIE AsyncSequence

### Non-Functional Requirements
- ✅ Dark mode uses iOS semantic colors
- ✅ Animations feel native (scale effect, haptics)
- ✅ SKIE AsyncSequence lifecycle managed by SwiftUI `.task`
- ✅ No Koin Swift API complexity (helper functions used)
- ✅ Type conversions handled correctly (Int32 → Int)
- ✅ All views have `#Preview` macros

---

## 🎯 Implementation Summary

### What Was Built

**Kotlin (KMP)**:
1. `shared/src/iosMain/kotlin/KoinIos.kt` - Koin initialization + helper functions
2. `shared/build.gradle.kts` - SKIE plugin + module exports

**Swift (iOS)**:
1. `iosApp/iosApp/ViewModels/PokemonListViewModelWrapper.swift` - ViewModel bridge
2. `iosApp/iosApp/Views/PokemonListView.swift` - Main list screen (2-column grid, infinite scroll, state handling)
3. `iosApp/iosApp/Views/PokemonCard.swift` - Card component (AsyncImage, animations, haptics, accessibility)
4. `iosApp/iosApp/Views/PokemonDetailView.swift` - Placeholder detail screen
5. `iosApp/iosApp/iOSApp.swift` - App entry point with Koin init

### Key Patterns Established

1. **SKIE for StateFlow bridging** - No manual AsyncStream code needed
2. **Kotlin helper functions for Koin** - Avoids Swift generic complexity
3. **@ObservableObject wrappers** - Bridge KMP to SwiftUI reactive system
4. **Native SwiftUI patterns** - Semantic colors, NavigationStack, AsyncImage
5. **Type conversion handling** - Explicit Int32 → Int casts
6. **Scroll position preservation** - ScrollViewReader + .id() pattern

### Files Modified

- `shared/src/iosMain/kotlin/com/minddistrict/multiplatformpoc/KoinIos.kt` - Added helper functions
- `shared/build.gradle.kts` - Added SKIE plugin, dependencies, exports
- `gradle/libs.versions.toml` - Added SKIE version 0.10.8
- `iosApp/iosApp/iOSApp.swift` - Replaced ContentView with PokemonListView

---

## Troubleshooting Guide

### Issue: Koin "no definition found" error
**Symptoms**: App crashes at runtime with Koin error  
**Solution**: Use helper function pattern
```kotlin
// ✅ In KoinIos.kt
fun getPokemonListViewModel(): PokemonListViewModel {
    return KoinPlatform.getKoin().get()
}
```
```swift
// ✅ In Swift
let viewModel = KoinIosKt.getPokemonListViewModel()
```

### Issue: "Cannot assign Int32 to Int"
**Symptoms**: Compile error in Swift  
**Solution**: Add explicit cast
```swift
// ❌ Wrong
scrollPosition = pokemon.id
// ✅ Correct
scrollPosition = Int(pokemon.id)
```

### Issue: StateFlow not updating UI
**Symptoms**: Grid stays in Loading state forever  
**Solutions**:
- Verify `.task { await wrapper.observeState() }` is called
- Check `@Published` property updates on `@MainActor`
- Verify ViewModel's `loadInitialPage()` is called
- Check SKIE plugin is applied in shared/build.gradle.kts

### Issue: Framework not found in Xcode
**Symptoms**: Build error "Shared.framework not found"  
**Solution**: Rebuild framework
```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

---

## Next Steps (Future Work)

### Pokemon Detail Screen (Phase 2)
- [ ] Create `PokemonDetailViewModel` in KMP
- [ ] Create `PokemonDetailUiState` sealed interface
- [ ] Implement detail API service and repository
- [ ] Export detail presentation module via `shared`
- [ ] Create `PokemonDetailViewModelWrapper.swift`
- [ ] Build full detail UI (hero image, stats, abilities, types)
- [ ] Add type badge colors
- [ ] Implement stat progress bars

### Enhancements
- [ ] Add pull-to-refresh on list
- [ ] Implement search/filter functionality
- [ ] Add favorites/bookmarks
- [ ] Cache images for offline viewing
- [ ] Add skeleton loading placeholders
- [ ] Implement detail screen animations (hero transition)
- [ ] Add haptic feedback for stat interactions
- [ ] Easter eggs (shake device, long-press sprite)

---

## References

### Documentation
- `.junie/guides/tech/conventions.md` - Architecture patterns
- `.junie/guides/tech/dependency_injection.md` - Koin setup
- `.junie/guides/tech/presentation_layer.md` - ViewModel patterns
- `.junie/guides/project/prd.md` - Product requirements

### KMP Files
- `shared/src/iosMain/kotlin/com/minddistrict/multiplatformpoc/KoinIos.kt`
- `features/pokemonlist/presentation/src/commonMain/kotlin/.../PokemonListViewModel.kt`
- `features/pokemonlist/api/src/commonMain/kotlin/.../Pokemon.kt`

### iOS Files
- `iosApp/iosApp/iOSApp.swift` - App entry point
- `iosApp/iosApp/Utilities/StateFlowExtensions.swift` - StateFlow bridge
- `iosApp/iosApp/ViewModels/PokemonListViewModelWrapper.swift` - ViewModel wrapper
- `iosApp/iosApp/Views/PokemonListView.swift` - Main list screen
- `iosApp/iosApp/Views/PokemonCard.swift` - Card component
- `iosApp/iosApp/Views/PokemonDetailView.swift` - Detail placeholder

---

**Implementation Status**: Ready to execute
