# Direct Integration Pattern

Complete guide for Direct Integration pattern - SwiftUI consuming KMP ViewModels with minimal infrastructure.

## Overview

**Direct Integration** is the production pattern for iOS SwiftUI apps consuming Kotlin Multiplatform ViewModels. It provides a simple, direct approach with minimal boilerplate (~10 lines per view).

**Key Features**:
- ✅ Minimal boilerplate (~10 lines per view)
- ✅ Direct ViewModel access from Koin
- ✅ Simple mental model: One object (ViewModel), one state property
- ✅ No intermediate layer (wrapper)
- ✅ Works with SKIE StateFlow → AsyncSequence bridging
- ✅ No memory leaks (`.task` cancels properly)

## Pattern Comparison

| Aspect | Direct Integration | Wrapper Pattern |
|--------|--------------------|-----------------|
| **Boilerplate** | ✅ Minimal (~10 lines per view) | ❌ More (~80 lines per ViewModel) |
| **Lifecycle Management** | ⚠️ ViewModel recreated on View struct recreation | ✅ `@StateObject` preserves ViewModel |
| **State Preservation** | ❌ Lost on parent updates/navigation changes | ✅ Survives parent updates, navigation |
| **SwiftUI Best Practice** | ⚠️ Using `@State` for external state (non-idiomatic) | ✅ `@StateObject` for external state |
| **Memory Leaks** | ✅ No leaks (`.task` cancels properly) | ✅ No leaks (`.task` cancels properly) |
| **Testing** | ⚠️ Harder to mock ViewModel | ✅ Easy to mock wrapper in previews |
| **Complexity** | ✅ Simple, direct | ⚠️ Additional abstraction layer |
| **Current Usage** | ✅ PokemonListView, PokemonDetailView | ❌ Not currently used |

## When to Use Direct Integration

✅ **Use when**:
- Simple to medium complexity apps
- Linear navigation flows (stack-based)
- Minimal state preservation requirements
- Team prioritizes code simplicity over lifecycle guarantees
- Views don't need to survive parent updates with state intact

❌ **Avoid when**:
- Complex navigation (tabs, sheets, deep stacks)
- State must survive parent updates
- Team values SwiftUI best practices over simplicity
- Need easy mocking for SwiftUI Previews
- Large-scale production apps with strict lifecycle requirements

## Implementation

### Simple ViewModel (Non-Parametric)

```swift
// iosApp/iosApp/Views/PokemonListView.swift
import SwiftUI
import Shared

struct PokemonListView: View {
    @StateObject private var owner = IosViewModelStoreOwner()

    private var viewModel: PokemonListViewModel {
        owner.viewModel()  // Generic function infers type
    }

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
            PokemonListContent(pokemons: content.pokemons)
        case let error as PokemonListUiStateError:
            ErrorView(message: error.message)
        default:
            EmptyView()
        }
    }
}
```

### Parametric ViewModel (with Parameters)

```swift
// iosApp/iosApp/Views/PokemonDetailView.swift
import SwiftUI
import Shared

struct PokemonDetailView: View {
    let pokemonId: Int
    @StateObject private var owner = IosViewModelStoreOwner()

    private var viewModel: PokemonDetailViewModel {
        owner.viewModel(intParam: pokemonId)
    }

    @State private var uiState: PokemonDetailUiState = PokemonDetailUiStateLoading()

    var body: some View {
        content
            .task {
                for await state in viewModel.uiState {
                    self.uiState = state
                }
            }
    }

    @ViewBuilder
    private var content: some View {
        switch uiState {
        case is PokemonDetailUiStateLoading:
            ProgressView("Loading...")
        case let content as PokemonDetailUiStateContent:
            DetailContentView(pokemon: content.pokemon)
        case let error as PokemonDetailUiStateError:
            ErrorView(message: error.message, onRetry: { viewModel.retry() })
        default:
            EmptyView()
        }
    }
}
```

## Kotlin Side Setup

### ViewModel Implementation

```kotlin
// features/pokemondetail/presentation/PokemonDetailViewModel.kt
class PokemonDetailViewModel(
    private val pokemonId: Int,
    private val repository: PokemonDetailRepository,
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) : ViewModel(viewModelScope),
    DefaultLifecycleObserver,
    UiStateHolder<PokemonDetailUiState, PokemonDetailUiEvent> {

    private val _uiState = MutableStateFlow<PokemonDetailUiState>(PokemonDetailUiState.Loading)
    override val uiState: StateFlow<PokemonDetailUiState> = _uiState

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        loadPokemon()
    }

    private fun loadPokemon() {
        viewModelScope.launch {
            repository.getPokemonById(pokemonId).fold(
                ifLeft = { error -> _uiState.value = PokemonDetailUiState.Error(error.message) },
                ifRight = { pokemon -> _uiState.value = PokemonDetailUiState.Content(pokemon) }
            )
        }
    }
}
```

### Koin DI Configuration

```kotlin
// features/pokemondetail/wiring/PokemonDetailModule.kt
val pokemonDetailModule = module {
    factory { params ->
        PokemonDetailViewModel(
            pokemonId = params.get(),
            repository = get()
        )
    }
}
```

## Swift Side Setup

### IosViewModelStoreOwner

```kotlin
// shared/src/iosMain/kotlin/.../ios/IosViewModelStoreOwner.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.minddistrict.multiplatformpoc.di.KoinViewModelHelpers
import kotlinx.cinterop.ObjCClass

/**
 * Minimal ViewModelStoreOwner for iOS SwiftUI integration.
 * Provides ViewModelStore that survives SwiftUI view recreation.
 */
class IosViewModelStoreOwner : ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()

    /**
     * Generic ViewModel retrieval using ObjCClass for type inference.
     * Type inferred from Swift variable declaration.
     */
    fun <T : ViewModel> viewModel(): T {
        return KoinViewModelHelpers.getViewModel(
            viewModelStore = viewModelStore
        )
    }

    /**
     * Generic ViewModel retrieval with Int parameter.
     * Used for parametric ViewModels (e.g., PokemonDetailViewModel).
     */
    fun <T : ViewModel> viewModel(intParam: Int): T {
        return KoinViewModelHelpers.getViewModelWithInt(
            viewModelStore = viewModelStore,
            param = intParam
        )
    }

    /**
     * Generic ViewModel retrieval with String parameter.
     */
    fun <T : ViewModel> viewModel(stringParam: String): T {
        return KoinViewModelHelpers.getViewModelWithString(
            viewModelStore = viewModelStore,
            param = stringParam
        )
    }
}
```

### KoinViewModelHelpers

```kotlin
// shared/src/iosMain/kotlin/.../ios/KoinViewModelHelpers.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import org.koin.mp.KoinPlatform
import kotlin.reflect.KClass

/**
 * Generic helper functions for retrieving ViewModels from Koin.
 * Uses ObjCClass for Swift type inference.
 */
object KoinViewModelHelpers {

    /**
     * Retrieve non-parametric ViewModel from Koin.
     * Type inferred from Swift variable declaration.
     */
    fun <T : ViewModel> getViewModel(viewModelStore: ViewModelStore): T {
        return KoinPlatform.getKoin().get(
            parameters = { parametersOf(viewModelStore) }
        )
    }

    /**
     * Retrieve ViewModel with Int parameter from Koin.
     * Used for parametric ViewModels (e.g., PokemonDetailViewModel).
     */
    fun <T : ViewModel> getViewModelWithInt(
        viewModelStore: ViewModelStore,
        param: Int
    ): T {
        return KoinPlatform.getKoin().get(
            parameters = { parametersOf(viewModelStore, param) }
        )
    }

    /**
     * Retrieve ViewModel with String parameter from Koin.
     */
    fun <T : ViewModel> getViewModelWithString(
        viewModelStore: ViewModelStore,
        param: String
    ): T {
        return KoinPlatform.getKoin().get(
            parameters = { parametersOf(viewModelStore, param) }
        )
    }
}
```

## Pros and Cons

### Pros

- ✅ **Minimal boilerplate**: ~10 lines per view (no separate wrapper class)
- ✅ **Direct access**: Call ViewModel methods directly (`viewModel.loadInitialPage()`)
- ✅ **Simple mental model**: One object (ViewModel), one state property
- ✅ **No intermediate layer**: Fewer abstractions to understand
- ✅ **Works with SKIE**: Leverages automatic StateFlow bridging
- ✅ **No memory leaks**: `.task` cancels AsyncSequence properly

### Cons

- ❌ **ViewModel recreation**: New ViewModel created when View struct is recreated
- ❌ **State loss scenarios**: Loses state on parent view updates, NavigationStack changes, @Environment changes
- ❌ **Non-idiomatic SwiftUI**: Using `@State` for external state violates Apple guidelines
- ❌ **Harder to test**: Can't easily mock ViewModel in SwiftUI Previews
- ❌ **Performance overhead**: Unnecessary Koin resolution + repository injection on recreation
- ❌ **Advanced navigation issues**: Breaks with tab views, sheets, parent state updates

## Technical Details: SwiftUI View Struct Lifecycle

SwiftUI recreates View structs in these scenarios:
1. Parent view updates @State or @Binding
2. NavigationStack path changes (even in parent)
3. @Environment values change (colorScheme, locale, etc.)
4. Device orientation changes
5. Tab switching in TabView

With Direct Integration:
```swift
struct MyView: View {
    private var viewModel = getViewModel()  // ← Recreated on View struct recreation
    @State private var uiState = Loading()  // ← Preserved by SwiftUI

    // Result: New ViewModel, state resets, reloads data
}
```

## When View Struct Recreation Causes Issues

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

## Decision Matrix

Use this table to decide if Direct Integration is right for your app:

| Requirement | Direct Integration | Wrapper Pattern |
|-------------|--------------------|-----------------|
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

## Migration Guide

### Converting Direct Integration → Wrapper Pattern

**When to Migrate**:
- App grows beyond simple navigation
- Users report state loss issues (data reloads unexpectedly)
- Adding tabs, sheets, or complex navigation
- Need better testability

See [lifecycle-bridging.md](lifecycle-bridging.md) for Wrapper pattern implementation.

## Current Project Status

**Active Pattern**: Direct Integration

**Files**:
- `iosApp/iosApp/Views/PokemonListView.swift` - Direct integration
- `iosApp/iosApp/Views/PokemonDetailView.swift` - Direct integration (parametric)

**Legacy Files** (not currently used):
- `iosApp/iosApp/ViewModels/PokemonDetailViewModelWrapper.swift` - Example of Wrapper pattern

**Decision Rationale**:
- Current app has simple navigation (single stack)
- No tabs, sheets, or complex flows
- Team prioritizes rapid iteration and simplicity
- No state preservation issues reported in testing

**Future Considerations**:
- If adding tabs → migrate to Wrapper pattern
- If users report data reloading unexpectedly → migrate to Wrapper pattern
- If app scales beyond 5+ screens with complex navigation → migrate to Wrapper pattern
