---
name: swiftui-screen
description: This skill should be used when building native iOS UI with SwiftUI backed by KMP ViewModels. Use for iOS screens, StateFlow bridging, and iOS lifecycle management.
---

# SwiftUI Screen Skill

Expert guidance for implementing native iOS SwiftUI screens backed by shared Kotlin Multiplatform ViewModels. Focus on SwiftUI integration, StateFlow bridging, and iOS lifecycle management.

## When to Use

Use this skill when:
- Implementing SwiftUI screens consuming KMP ViewModels
- Bridging StateFlow to SwiftUI with @State properties
- Setting up iOS lifecycle management (onAppear/onDisappear)
- Creating SwiftUI Previews with mock ViewModels
- Working with parametric ViewModels (pokemonId, userId)
- Using SKIE for Kotlin-Swift interop
- Deciding between Direct Integration vs Wrapper pattern

Do NOT use this skill for:
- Shared ViewModel implementation → switch to KMP Mobile Expert Mode
- Compose UI implementation → switch to Compose Screen Implementation Mode
- Product/PRD decisions → switch to Product Design Mode
- Visual design/animations → switch to UI/UX Design Mode

## Mode Detection

| User Request | Mode | Context |
|--------------|------|---------|
| "Create a SwiftUI screen for..." | IMPLEMENTATION_MODE | Building new iOS UI |
| "How do I observe StateFlow?" | STATEFLOW_MODE | Bridging Kotlin flows to SwiftUI |
| "My ViewModel isn't updating the UI" | DEBUG_MODE | Troubleshooting StateFlow bridging |
| "Should I use @StateObject or @ObservedObject?" | PATTERN_MODE | SwiftUI lifecycle decisions |
| "How do I pass pokemonId to ViewModel?" | PARAMETRIC_MODE | Parametric ViewModels |
| "Create a preview for this SwiftUI view" | PREVIEW_MODE | SwiftUI Previews |

## Resource Loading

**MANDATORY - READ ENTIRE FILE**: When implementing parametric ViewModels (screens requiring constructor parameters like pokemonId, userId), you MUST read [resources/parametric-viewmodel-guide.md](resources/parametric-viewmodel-guide.md) (~80 lines) completely.

**Do NOT load** `parametric-viewmodel-guide.md` for simple non-parametric ViewModels (use Workflow 1 only).

---

## Essential Workflows

### Workflow 1: Direct Integration (Current Pattern)

Use Direct Integration for simple to medium complexity apps with linear navigation:

1. **Create SwiftUI View with Direct ViewModel Access**
   ```swift
   import SwiftUI
   import Shared

   struct PokemonListView: View {
       // Direct ViewModel access from Koin
       private var viewModel = KoinIosKt.getPokemonListViewModel()

       // @State bridges StateFlow to SwiftUI
       @State private var uiState: PokemonListUiState = PokemonListUiStateLoading()

       var body: some View {
           content
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

2. **Handle UI States with Switch**
   ```swift
   @ViewBuilder
   private var content: some View {
       switch uiState {
       case is PokemonListUiStateLoading:
           ProgressView("Loading...")

       case let error as PokemonListUiStateError:
           ErrorView(message: error.message) {
               viewModel.loadInitialPage() // Retry
           }

       case let content as PokemonListUiStateContent:
           PokemonListGrid(
               pokemons: content.pokemons,
               onLoadMore: { viewModel.loadNextPage() }
           )

       default:
           EmptyView()
       }
   }
   ```

3. **Test and Validate**
   - Verify StateFlow updates SwiftUI @State correctly
   - Test loading, error, and content states
   - Confirm `.task` cancels on view disappear (no memory leaks)
   - Run iOS build in Xcode: `open iosApp/iosApp.xcodeproj`

---

### Workflow 2: Parametric ViewModels

Use when ViewModels require constructor parameters (e.g., pokemonId, userId):

**MANDATORY**: Read [resources/parametric-viewmodel-guide.md](resources/parametric-viewmodel-guide.md) for complete patterns including Koin setup, type conversions, and testing.

1. **Create Koin Helper with Parameters**
   ```kotlin
   // shared/src/iosMain/kotlin/KoinIos.kt
   fun getPokemonDetailViewModel(pokemonId: Int): PokemonDetailViewModel {
       return KoinPlatform.getKoin().get { parametersOf(pokemonId) }
   }
   ```

2. **Create SwiftUI View with Initialization**
   ```swift
   import SwiftUI
   import Shared

   struct PokemonDetailView: View {
       let pokemonId: Int
       private var viewModel: PokemonDetailViewModel

       @State private var uiState: PokemonDetailUiState = PokemonDetailUiStateLoading()

       init(pokemonId: Int) {
           self.pokemonId = pokemonId
           // Get parametric ViewModel from Koin
           // Cast Swift Int to Kotlin Int32
           viewModel = KoinIosKt.getPokemonDetailViewModel(pokemonId: Int32(pokemonId))
       }

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

3. **Handle Type Conversions**
   ```swift
   // Kotlin Int32 → Swift Int conversion required
   String(format: "#%03d", Int(pokemon.id))  // pokemon.id is Int32
   String(format: "%.1f m", Double(pokemon.height) / 10.0)
   ```

---

### Workflow 3: SwiftUI Previews with Mock Data

Create previews for all UI states to enable visual testing:

1. **Create Mock ViewModel Helper**
   ```swift
   // iosApp/Previews/MockViewModels.swift
   import SwiftUI
   import Shared

   class MockPokemonListViewModel: ObservableObject {
       @Published var uiState: PokemonListUiState = PokemonListUiStateLoading()

       init(state: PokemonListUiState) {
           self.uiState = state
       }
   }
   ```

2. **Create Preview Variants**
   ```swift
   #Preview("Loading") {
       PokemonListView_Previews.LoadingPreview()
   }

   #Preview("Error") {
       PokemonListView_Previews.ErrorPreview()
   }

   #Preview("Content") {
       PokemonListView_Previews.ContentPreview()
   }

   extension PokemonListView_Previews {
       static func LoadingPreview() -> some View {
           PokemonListView()
               .environment(\.mockViewModel, MockPokemonListViewModel(
                   state: PokemonListUiStateLoading()
               ))
       }

       static func ErrorPreview() -> some View {
           PokemonListView()
               .environment(\.mockViewModel, MockPokemonListViewModel(
                   state: PokemonListUiStateError(message: "Network error")
               ))
       }

       static func ContentPreview() -> some View {
           PokemonListView()
               .environment(\.mockViewModel, MockPokemonListViewModel(
                   state: PokemonListUiStateContent(
                       pokemons: persistentListOf(
                           Pokemon(name: "Bulbasaur", detailUrl: "..."),
                           Pokemon(name: "Charmander", detailUrl: "...")
                       ),
                       hasMore: true
                   )
               ))
       }
   }
   ```

3. **Validate Previews in Xcode**
   - Open Xcode: `open iosApp/iosApp.xcodeproj`
   - Navigate to the SwiftUI file
   - Verify all preview states render correctly
   - Check for layout issues at different device sizes

---

## Critical Guardrails

| Rule | Why It Matters | How to Apply |
|------|---------------|--------------|
| **Always use @StateObject for wrapper, @State for ViewModel state** | @StateObject preserves ViewModel across View recreations; @State bridges StateFlow to SwiftUI | Use `@StateObject private var wrapper = ViewModelWrapper()` for wrappers; use `@State private var uiState = Loading()` for StateFlow bridging |
| **Observe StateFlow in .task, not .onAppear** | .task automatically cancels AsyncSequence on view disappear, preventing memory leaks | Use `.task { for await state in viewModel.uiState { self.uiState = state } }` |
| **Cast Kotlin Int32 to Swift Int** | Kotlin's Int maps to Swift's Int32, not Int | Always cast: `Int(pokemon.id)`, `String(format: "%03d", Int(pokemon.id))` |
| **Use String(format:) for formatted output** | Swift string interpolation doesn't support format specifiers | Use `String(format: "%.1f m", value)` not `"\(value:.1f) m"` |
| **Check for SKIE-renamed types** | Kotlin classes named after Swift keywords get `_` suffix | Look for `Type_`, `Error_`, `Result_` instead of `Type`, `Error`, `Result` |
| **Import Shared framework** | All KMP exports are available via Shared framework | Always add `import Shared` at top of SwiftUI files |
| **Never export :data or :wiring to iOS** | These are internal KMP modules; iOS only needs :api and :presentation | Check :shared/build.gradle.kts export list - only :api and :presentation should be exported |

---

## Quick Reference

### Key Patterns

| Pattern | Code Snippet | When to Use |
|---------|--------------|-------------|
| **Direct ViewModel Access** | `private var viewModel = KoinIosKt.getPokemonListViewModel()` | Simple screens, non-parametric |
| **Parametric ViewModel** | `viewModel = KoinIosKt.getPokemonDetailViewModel(pokemonId: Int32(id))` | Screens requiring constructor params |
| **StateFlow Bridging** | `@State private var uiState = Loading()` + `.task { for await state in viewModel.uiState { self.uiState = state } }` | Observing StateFlow in SwiftUI |
| **UI State Switch** | `switch uiState { case is Loading: ProgressView() case let content as Content: ... }` | Rendering sealed class states |
| **Type Casting** | `Int(pokemon.id)` | Kotlin Int32 → Swift Int |
| **String Formatting** | `String(format: "%.1f m", value)` | Formatted numeric output |
| **SKIE Renamed Types** | `let type: Type_ = pokemon.types.first!` | Swift keyword collision types |

### Type Conversion Cheat Sheet

| Kotlin Type | Swift Type | Example |
|-------------|------------|---------|
| `Int` | `Int32` | `let id = Int32(pokemon.id)` |
| `String` | `String` | Direct, no conversion needed |
| `Double` | `Double` | Direct, no conversion needed |
| `Boolean` | `Bool` | Direct, no conversion needed |
| `List<T>` | `KotlinArray<T>` | Use `Array(kotlinArray)` to convert |
| `StateFlow<T>` | `AsyncSequence<T>` | Use `.task { for await state in flow { ... } }` |

### Common SKIE Renamed Types

| Kotlin Class | Swift Name (after SKIE) |
|--------------|------------------------|
| `Type` | `Type_` |
| `Error` | `Error_` |
| `Result` | `Result_` |
| `Self` | `Self_` |
| `Protocol` | `Protocol_` |

### Validation Commands

| Command | Purpose | When to Run |
|---------|---------|-------------|
| `bash -n .claude/skills/swiftui-screen/scripts/validate-swiftui.sh` | Validate script syntax | After creating validation script |
| `./gradlew :composeApp:assembleDebug test --continue` | Primary validation (Android + tests) | Before committing |
| `open iosApp/iosApp.xcodeproj` | Open iOS app in Xcode | When working on iOS features |
| `./gradlew :shared:embedAndSignAppleFrameworkForXcode` | Build iOS framework | Before iOS builds |

---

## Cross-References

### Documentation Links

| Document | Purpose | Location |
|----------|---------|----------|
| iOS Integration Guide | Complete iOS + KMP integration details | [docs/tech/ios_integration.md](../../../docs/tech/ios_integration.md) |
| Architecture + Conventions | Master architecture reference | [docs/tech/conventions.md](../../../docs/tech/conventions.md) |
| Critical Patterns | 6 core patterns (ViewModel, Either, etc.) | [docs/tech/critical_patterns_quick_ref.md](../../../docs/tech/critical_patterns_quick_ref.md) |
| Product Requirements | Feature acceptance criteria | [docs/project/prd.md](../../../docs/project/prd.md) |
| Testing Strategy | Test coverage and patterns | [docs/tech/testing_strategy.md](../../../docs/tech/testing_strategy.md) |

### Related Skills

| Skill | Purpose | When to Switch |
|-------|---------|----------------|
| `kmp-mobile-expert` | Shared ViewModel, repository, and iOS bridging implementation | Implementing shared business logic |
| `compose-screen` | Compose Multiplatform UI implementation (Android/Desktop) | Building Compose screens |
| `ui-ux-designer` | Visual design and animations | Creating custom animations or design systems |

### Reference Implementations

| Feature | Files | Pattern |
|---------|--------|---------|
| Pokemon List | `iosApp/iosApp/Views/PokemonListView.swift` | Direct Integration |
| Pokemon Detail | `iosApp/iosApp/Views/PokemonDetailView.swift` | Parametric ViewModel |
| Pokemon List Grid | `iosApp/iosApp/Views/PokemonListGrid.swift` | Grid layout with infinite scroll |
| Type Badges | `iosApp/iosApp/Views/TypeBadge.swift` | Custom view component |

### Decision Guides

**When to Use Direct Integration vs Wrapper Pattern:**

| Requirement | Direct Integration | Wrapper Pattern |
|-------------|-------------------|-----------------|
| Simple linear navigation | ✅ Recommended | ⚠️ Overkill |
| Tab-based navigation | ❌ State loss on tab switch | ✅ Required |
| Sheet/modal presentation | ⚠️ May lose state | ✅ Preserves state |
| Parent view has @State | ❌ ViewModel recreated | ✅ Survives |
| Deep navigation stacks | ⚠️ Fragile | ✅ Robust |
| Team new to SwiftUI | ✅ Simpler to understand | ❌ More complex |
| Testing in Previews | ❌ Hard to mock | ✅ Easy to mock |
| Boilerplate tolerance | ✅ Low (~10 lines) | ❌ High (~80 lines) |
| Production large-scale app | ⚠️ Risky for complex flows | ✅ Safer choice |
| MVP/POC project | ✅ Fast iteration | ⚠️ Premature optimization |

See [ios_integration.md](../../../docs/tech/ios_integration.md) for complete comparison and migration guide.
