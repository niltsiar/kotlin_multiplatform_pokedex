# Lifecycle Bridging

Complete guide for bridging SwiftUI lifecycle with KMP ViewModels.

## Overview

Kotlin Multiplatform ViewModels implement `DefaultLifecycleObserver` for lifecycle-aware behavior. Since SwiftUI doesn't have a native `LifecycleOwner` concept, we need a bridging mechanism.

**Two Patterns**:
1. **Direct Integration** (current production) - Minimal infrastructure, simple lifecycle calls
2. **Wrapper Pattern** (alternative) - Full lifecycle management with `LifecycleRegistry`

This document covers both patterns.

## Core Concept: DefaultLifecycleObserver

KMP ViewModels implement `DefaultLifecycleObserver` to receive lifecycle events:

```kotlin
class PokemonListViewModel(
    private val repository: PokemonListRepository,
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) : ViewModel(viewModelScope),
    DefaultLifecycleObserver,
    UiStateHolder<PokemonListUiState, PokemonListUiEvent> {

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        loadInitialPage()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        // Stop background work, cancel operations
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        // Cleanup
    }
}
```

## Pattern 1: Direct Integration (Current Production)

### Philosophy

Align with the [official Android KMP ViewModel guide](https://developer.android.com/kotlin/multiplatform/viewmodel). ViewModels implement `DefaultLifecycleObserver`, and we just call the lifecycle methods directly from SwiftUI.

### DummyLifecycleOwner

Since SwiftUI doesn't have a native `LifecycleOwner` concept, we use a simple stub:

```swift
// iosApp/iosApp/Models/DummyLifecycleOwner.swift
import Shared

class DummyLifecycleOwner: Shared.LifecycleOwner {
    private let _lifecycle = Shared.LifecycleRegistry(owner: nil)

    var lifecycle: Shared.Lifecycle {
        return _lifecycle
    }
}
```

This is just to satisfy the API signature. The actual lifecycle management happens through the direct method calls.

### SwiftUI Integration

```swift
// iosApp/iosApp/Views/PokemonListView.swift
import SwiftUI
import Shared

struct PokemonListView: View {
    @StateObject private var owner = IosViewModelStoreOwner()

    private var viewModel: PokemonListViewModel {
        owner.viewModel()
    }

    @State private var uiState: PokemonListUiState = PokemonListUiStateLoading()

    var body: some View {
        content
            .onAppear {
                // Directly call ViewModel lifecycle method
                // (aligned with official Android KMP ViewModel guide)
                viewModel.onStart(owner: DummyLifecycleOwner())
            }
            .onDisappear {
                // Call lifecycle stop method
                viewModel.onStop(owner: DummyLifecycleOwner())
            }
            .task {
                // SKIE: StateFlow → AsyncSequence bridging
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

**Pattern Highlights**:
- ✅ Get ViewModel via Koin helper with stable key
- ✅ Call `viewModel.onStart(owner:)` in `.onAppear` → starts data loading
- ✅ Call `viewModel.onStop(owner:)` in `.onDisappear` → stops background work
- ✅ `.task` handles StateFlow observation (auto-cancels on view disappear)
- ✅ No complex lifecycle infrastructure needed!

### IosViewModelStoreOwner

```kotlin
// shared/src/iosMain/kotlin/.../ios/IosViewModelStoreOwner.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.minddistrict.multiplatformpoc.di.KoinViewModelHelpers

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

### Lifecycle Flow (Direct Integration)

```
View Created → .onAppear → viewModel.onStart() → Load Data
View Hidden → .onDisappear → viewModel.onStop() → Background work stops
View Destroyed → ViewModelStore cleared → ViewModel.onCleared()
```

**Key Benefits**:
- ✅ ViewModels use standard `DefaultLifecycleObserver.onStart()` pattern
- ✅ No platform-specific lifecycle code in ViewModels
- ✅ Consistent with Android/Desktop Compose (uses `LocalLifecycleOwner`)
- ✅ No need for manual `start(lifecycle)` or `repeatOnLifecycle` calls
- ✅ Clean separation: SwiftUI handles view lifecycle, ViewModel handles data lifecycle

### Parametric ViewModels

```swift
// iosApp/iosApp/Views/PokemonDetailView.swift
struct PokemonDetailView: View {
    let pokemonId: Int
    @StateObject private var owner = IosViewModelStoreOwner()

    private var viewModel: PokemonDetailViewModel {
        owner.viewModel(intParam: pokemonId)
    }

    @State private var uiState: PokemonDetailUiState = PokemonDetailUiStateLoading()

    var body: some View {
        content
            .onAppear {
                viewModel.onStart(owner: DummyLifecycleOwner())
            }
            .onDisappear {
                viewModel.onStop(owner: DummyLifecycleOwner())
            }
            .task {
                for await state in viewModel.uiState {
                    self.uiState = state
                }
            }
    }
}
```

## Pattern 2: Wrapper Pattern (Alternative)

### Overview

Wrapper Pattern provides full lifecycle management with `LifecycleRegistry`. It's more complex but offers better state preservation guarantees.

### LifecycleViewModelStoreOwner

```kotlin
// shared/src/iosMain/kotlin/.../ios/LifecycleViewModelStoreOwner.kt
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * Combines ViewModelStoreOwner + LifecycleOwner for iOS SwiftUI.
 * Provides full lifecycle management with LifecycleRegistry.
 */
class LifecycleViewModelStoreOwner(
    private val koin: org.koin.core.Koin = org.koin.mp.KoinPlatform.getKoin()
) : ViewModelStoreOwner,
    LifecycleOwner,
    KoinComponent {

    override val viewModelStore: ViewModelStore = ViewModelStore()
    private val lifecycleRegistry = LifecycleRegistry(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    /**
     * Called from SwiftUI .onAppear
     */
    fun handleOnAppear() {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    /**
     * Called from SwiftUI .onDisappear
     */
    fun handleOnDisappear() {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    /**
     * Get ViewModel and auto-register with lifecycle.
     */
    inline fun <reified T : ViewModel> koinViewModel(
        qualifier: org.koin.core.qualifier.Qualifier? = null,
        noinline parameters: org.koin.core.parameter.ParametersDefinition? = null
    ): T {
        val viewModel = koin.get(
            clazz = T::class,
            qualifier = qualifier,
            parameters = parameters
        )

        // Auto-register DefaultLifecycleObserver ViewModels with lifecycle
        if (viewModel is DefaultLifecycleObserver) {
            lifecycle.addObserver(viewModel)
        }

        return viewModel
    }
}
```

### Auto-Registration Pattern

ViewModels implementing `DefaultLifecycleObserver` are automatically registered with the lifecycle:

```kotlin
// Auto-registration happens in koinViewModel()
if (viewModel is DefaultLifecycleObserver) {
    lifecycle.addObserver(viewModel)
}
```

**Benefits**:
- ✅ ViewModels don't need manual lifecycle setup code
- ✅ Swift views call simple `handleOnAppear()/handleOnDisappear()`
- ✅ Consistent lifecycle behavior across Android/iOS/Desktop

### SwiftUI Integration (Wrapper Pattern)

```swift
// iosApp/iosApp/Views/PokemonListView.swift
import SwiftUI
import Shared

struct PokemonListView: View {
    // Get combined ViewModelStore + LifecycleOwner
    private let owner = KoinIosKt.getViewModelStoreOwner(key: "PokemonListView")

    // Get ViewModel from owner (auto-registers with lifecycle)
    private lazy var viewModel: PokemonListViewModel = {
        owner.koinViewModel()
    }()

    @State private var uiState: PokemonListUiState = PokemonListUiStateLoading()

    var body: some View {
        content
            .onAppear {
                // Notify lifecycle: CREATED → STARTED
                owner.handleOnAppear()
            }
            .onDisappear {
                // Notify lifecycle: STARTED → CREATED
                owner.handleOnDisappear()
            }
            .task {
                // SKIE: StateFlow → AsyncSequence bridging
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
            // Render Pokemon list
            PokemonListContent(pokemons: content.pokemons)
        case let error as PokemonListUiStateError:
            ErrorView(message: error.message)
        default:
            EmptyView()
        }
    }
}
```

### Lifecycle State Transitions (Wrapper Pattern)

**Lifecycle flow**:
```
View Created → owner.handleOnAppear() → STARTED → ViewModel.onStart() → Load Data
View Hidden → owner.handleOnDisappear() → CREATED → Background work continues
View Destroyed → ViewModelStore cleared → ViewModel.onCleared()
```

**Key Benefits**:
- ✅ ViewModels use standard `DefaultLifecycleObserver.onStart()` pattern
- ✅ No platform-specific lifecycle code in ViewModels
- ✅ Consistent with Android/Desktop Compose (uses `LocalLifecycleOwner`)
- ✅ No need for manual `start(lifecycle)` or `repeatOnLifecycle` calls
- ✅ Clean separation: SwiftUI handles view lifecycle, ViewModel handles data lifecycle

## Pattern Comparison

| Aspect | Direct Integration | Wrapper Pattern |
|--------|--------------------|-----------------|
| **Infrastructure** | Minimal (DummyLifecycleOwner) | Full (LifecycleRegistry) |
| **State Preservation** | ❌ Lost on View recreation | ✅ Preserved via @StateObject |
| **Lifecycle Calls** | Manual (viewModel.onStart()) | Automatic (registry state transitions) |
| **Complexity** | ✅ Simple (~10 lines) | ⚠️ More complex (~80 lines) |
| **SwiftUI Best Practice** | ⚠️ Using @State for external state | ✅ @StateObject pattern |
| **Current Status** | ✅ Production | ❌ Alternative (reference only) |

## Lifecycle Event Mapping

| SwiftUI Event | Lifecycle State | ViewModel Method |
|---------------|-----------------|-----------------|
| `.onAppear` | STARTED | `onStart(owner)` |
| `.onDisappear` | CREATED | `onStop(owner)` |
| View destroyed | DESTROYED | `onCleared()` |

## Common Patterns

### Initial Data Load

```kotlin
class PokemonListViewModel(
    private val repository: PokemonListRepository,
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) : ViewModel(viewModelScope),
    DefaultLifecycleObserver,
    UiStateHolder<PokemonListUiState, PokemonListUiEvent> {

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        loadInitialPage()
    }

    private fun loadInitialPage() {
        viewModelScope.launch {
            repository.loadPage(0).fold(
                ifLeft = { _uiState.value = PokemonListUiState.Error(it.message) },
                ifRight = { result -> _uiState.value = PokemonListUiState.Content(result) }
            )
        }
    }
}
```

### Background Work Cancellation

```kotlin
override fun onStop(owner: LifecycleOwner) {
    super.onStop(owner)
    // viewModelScope automatically cancelled when ViewModel is cleared
    // Use this for explicit cleanup if needed
}
```

### Resource Cleanup

```kotlin
override fun onDestroy(owner: LifecycleOwner) {
    super.onDestroy(owner)
    // Clean up resources, close connections, etc.
}
```

## Best Practices

### ✅ DO

1. **Use `onStart()` for data loading** - NOT `init` block
2. **Cancel coroutines in `viewModelScope`** - Automatically cancelled on clear
3. **Use `@StateObject` for `IosViewModelStoreOwner`** - Survives View recreation
4. **Call lifecycle methods from SwiftUI modifiers** - `.onAppear` / `.onDisappear`
5. **Use `.task` for StateFlow observation** - Auto-cancels on view disappear

### ❌ DON'T

1. **Don't do work in `init`** - Use `onStart()` for lifecycle-aware initialization
2. **Don't forget lifecycle method calls** - ViewModel won't receive events
3. **Don't use `@State` for external state** - Use `@StateObject` for ViewModels
4. **Don't create new LifecycleOwner per call** - Reuse instance
5. **Don't manually manage coroutines** - Use `viewModelScope`

## Troubleshooting

### ViewModel Not Receiving Lifecycle Events

**Symptom**: `onStart()` not called, data not loading

**Causes**:
- Forgot to call `viewModel.onStart(owner:)` in `.onAppear`
- Using wrong lifecycle owner instance

**Solution**:
```swift
.onAppear {
    viewModel.onStart(owner: DummyLifecycleOwner())  // ✅ Call this
}
```

### StateFlow Not Updating

**Symptom**: UI not reflecting state changes

**Causes**:
- StateFlow observation in wrong modifier
- Missing `.task` modifier

**Solution**:
```swift
.task {
    for await state in viewModel.uiState {  // ✅ Observe in .task
        self.uiState = state
    }
}
```

### ViewModel Recreated Unexpectedly

**Symptom**: Data reloads on parent view updates

**Causes**:
- Using `@State` instead of `@StateObject` for ViewModel holder
- Direct Integration pattern with complex navigation

**Solution**:
```swift
// ✅ Use @StateObject
@StateObject private var owner = IosViewModelStoreOwner()

// ✅ Or migrate to Wrapper pattern
```

## Validation Commands

```bash
# Build iOS frameworks
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64 :shared:linkDebugFrameworkIosArm64

# Build and test
./gradlew :composeApp:assembleDebug test --continue

# Xcode builds
cd iosApp && xcodebuild -scheme iosApp -sdk iphonesimulator build CODE_SIGN_IDENTITY="" CODE_SIGNING_REQUIRED=NO
```

## References

- [Direct Integration Pattern](direct-integration.md) - Complete Direct Integration guide
- [SwiftUI Patterns](swiftui-patterns.md) - SwiftUI-specific patterns
- [Official Android KMP ViewModel Guide](https://developer.android.com/kotlin/multiplatform/viewmodel)
