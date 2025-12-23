# iOS ViewModel Pattern - Quick Reference

**Last Updated**: December 22, 2025  
**Status**: ✅ Production Pattern  
**Reference**: [Official Android KMP ViewModel Guide](https://developer.android.com/kotlin/multiplatform/viewmodel)

> **Note**: This is a quick reference. See [ios_integration.md](ios_integration.md) for complete documentation.

## Pattern Summary

iOS ViewModel integration following the official Android KMP ViewModel guide with Koin DI:

```swift
// SwiftUI View
@StateObject private var owner = IosViewModelStoreOwner()

private var viewModel: PokemonListViewModel {
    owner.viewModel()  // Type inferred from variable type
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
```

## Key Components

**Swift Side**:
- `IosViewModelStoreOwner` - SwiftUI ObservableObject with ViewModelStore
- `@StateObject` - Automatic lifecycle management (deinit clears ViewModels)
- `DummyLifecycleOwner` - Minimal stub for API signature

**Kotlin Side**:
- `KoinViewModelHelpers.kt` - Generic helpers using ObjCClass
- `getViewModel()` / `getViewModelWithInt()` - Type-safe ViewModel retrieval
- Koin DI integration with `parametersOf()`

## Non-Parametric ViewModel

```swift
struct PokemonListView: View {
    @StateObject private var owner = IosViewModelStoreOwner()
    
    private var viewModel: PokemonListViewModel {
        owner.viewModel()  // Generic function infers type
    }
}
```

## Parametric ViewModel (with Int)

```swift
struct PokemonDetailView: View {
    let pokemonId: Int
    @StateObject private var owner = IosViewModelStoreOwner()
    
    private var viewModel: PokemonDetailViewModel {
        owner.viewModel(intParam: pokemonId)
    }
}
```

## Benefits

- ✅ **Simpler**: 50% less infrastructure code vs previous pattern
- ✅ **Official**: Aligns with Android KMP ViewModel guide
- ✅ **Type-safe**: Generic Swift functions with compile-time checking
- ✅ **Automatic**: SwiftUI @StateObject manages lifecycle
- ✅ **Clean**: Clear separation (Swift=UI, Kotlin=business logic)

## See Also

- [Complete iOS Integration Guide](ios_integration.md)
- [KMP Mobile Expert Agent](../agent-prompts/kmp_mobile_expert_agent_system_prompt_DELTA.md)
