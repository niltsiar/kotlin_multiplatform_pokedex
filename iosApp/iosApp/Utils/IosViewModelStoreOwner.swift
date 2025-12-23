import SwiftUI
import Shared

/// iOS-specific ViewModelStore owner for SwiftUI integration.
///
/// Aligned with official Android KMP ViewModel guide:
/// https://developer.android.com/kotlin/multiplatform/viewmodel#connect-viewmodel
///
/// This class implements `ObservableObject` for SwiftUI lifecycle management.
/// When used as a `@StateObject`, SwiftUI automatically manages its lifecycle,
/// calling `deinit` when the view is removed.
///
/// Note: This class does NOT conform to ViewModelStoreOwner protocol directly
/// to avoid Swift/Kotlin interop issues. It simply holds a ViewModelStore.
///
/// Usage:
/// ```swift
/// struct MyView: View {
///     @StateObject private var owner = IosViewModelStoreOwner()
///     
///     // Non-parametric ViewModel (type inferred from variable type)
///     private var listViewModel: PokemonListViewModel {
///         owner.viewModel()
///     }
///     
///     // Parametric ViewModel with Int parameter
///     private var detailViewModel: PokemonDetailViewModel {
///         owner.viewModel(intParam: pokemonId)
///     }
///     
///     var body: some View {
///         // ... use viewModel
///         .onAppear {
///             viewModel.onStart(owner: Shared.DummyLifecycleOwner())
///         }
///         .onDisappear {
///             viewModel.onStop(owner: Shared.DummyLifecycleOwner())
///         }
///     }
/// }
/// ```
class IosViewModelStoreOwner: ObservableObject {
    private let viewModelStore: Shared.Lifecycle_viewmodelViewModelStore
    
    init() {
        self.viewModelStore = Shared.Lifecycle_viewmodelViewModelStore()
    }
    
    /// Retrieve any non-parametric ViewModel.
    ///
    /// Uses ObjCClass to pass type information to Kotlin.
    /// ViewModelStore acts as a cache.
    ///
    /// - Returns: The requested ViewModel instance
    func viewModel<T: Shared.Lifecycle_viewmodelViewModel>() -> T {
        return Shared.KoinViewModelHelpersKt.getViewModel(
            viewModelStore: viewModelStore,
            viewModelClass: T.self
        ) as! T
    }
    
    /// Retrieve any parametric ViewModel with an Int parameter.
    ///
    /// Uses ObjCClass to pass type information to Kotlin.
    /// ViewModelStore caches by "ClassName:param" key.
    ///
    /// - Parameter intParam: The integer parameter (e.g., pokemonId)
    /// - Returns: The requested ViewModel instance
    func viewModel<T: Shared.Lifecycle_viewmodelViewModel>(intParam: Int) -> T {
        return Shared.KoinViewModelHelpersKt.getViewModelWithInt(
            viewModelStore: viewModelStore,
            viewModelClass: T.self,
            intParam: Int32(intParam)
        ) as! T
    }
    
    /// Called automatically when SwiftUI deinitializes this @StateObject.
    /// Clears all ViewModels, triggering their `onCleared()` methods.
    deinit {
        viewModelStore.clear()
    }
}


