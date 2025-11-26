import Foundation
import Shared
import SwiftUI

/**
 * SwiftUI wrapper for PokemonDetailViewModel from KMP.
 * 
 * This class bridges the Kotlin ViewModel to SwiftUI's reactive system:
 * - Fetches the ViewModel from Koin DI with pokemonId parameter
 * - Observes StateFlow using SKIE's AsyncSequence bridging
 * - Exposes @Published property for SwiftUI observation
 * - Delegates retry action to KMP ViewModel
 * 
 * Usage:
 * ```swift
 * struct PokemonDetailView: View {
 *     @StateObject private var wrapper: PokemonDetailViewModelWrapper
 *     
 *     init(pokemonId: Int) {
 *         _wrapper = StateObject(wrappedValue: PokemonDetailViewModelWrapper(pokemonId: pokemonId))
 *     }
 *     
 *     var body: some View {
 *         // Use wrapper.uiState to render UI
 *     }
 *     .task {
 *         await wrapper.observeState()
 *     }
 * }
 * ```
 */
@MainActor
class PokemonDetailViewModelWrapper: ObservableObject {
    /**
     * Published UI state that triggers SwiftUI re-renders.
     * Starts in Loading state, updates as StateFlow emits.
     */
    @Published var uiState: PokemonDetailUiState = PokemonDetailUiStateLoading()
    
    /**
     * The KMP ViewModel instance retrieved from Koin.
     */
    private let viewModel: PokemonDetailViewModel
    
    /**
     * Initializes the wrapper by fetching PokemonDetailViewModel from Koin.
     * 
     * Note: Koin must be initialized (via KoinIosKt.doInitKoin) before
     * this wrapper is created, typically in the App's init().
     * 
     * @param pokemonId The ID of the Pokemon to load details for
     */
    init(pokemonId: Int) {
        // Retrieve ViewModel from Koin DI using helper function with pokemonId
        self.viewModel = KoinIosKt.getPokemonDetailViewModel(pokemonId: Int32(pokemonId))
    }
    
    /**
     * Observes the ViewModel's StateFlow and updates the published uiState.
     * 
     * Uses SKIE's automatic StateFlow → AsyncSequence bridging.
     * This should be called from a SwiftUI .task modifier, which automatically
     * cancels when the view disappears (preventing memory leaks).
     */
    func observeState() async {
        // SKIE automatically provides async iteration for StateFlow
        for await state in viewModel.uiState {
            self.uiState = state
        }
    }
    
    /**
     * Retries loading Pokemon detail after an error.
     * 
     * Call this from an error state retry button.
     */
    func retry() {
        viewModel.retry()
    }
}
