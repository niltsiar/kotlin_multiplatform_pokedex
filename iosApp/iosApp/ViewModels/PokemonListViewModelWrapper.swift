import Foundation
import Shared
import SwiftUI

/**
 * SwiftUI wrapper for PokemonListViewModel from KMP.
 * 
 * This class bridges the Kotlin ViewModel to SwiftUI's reactive system:
 * - Fetches the ViewModel from Koin DI
 * - Observes StateFlow using native Kotlin Coroutines .values AsyncSequence
 * - Exposes @Published property for SwiftUI observation
 * - Delegates method calls to KMP ViewModel
 * 
 * Usage:
 * ```swift
 * struct PokemonListView: View {
 *     @StateObject private var wrapper = PokemonListViewModelWrapper()
 *     
 *     var body: some View {
 *         // Use wrapper.uiState to render UI
 *     }
 *     .onAppear {
 *         wrapper.loadInitialPage()
 *     }
 *     .task {
 *         await wrapper.observeState()
 *     }
 * }
 * ```
 */
@MainActor
class PokemonListViewModelWrapper: ObservableObject {
    /**
     * Published UI state that triggers SwiftUI re-renders.
     * Starts in Loading state, updates as StateFlow emits.
     */
    @Published var uiState: PokemonListUiState = PokemonListUiStateLoading()
    
    /**
     * The KMP ViewModel instance retrieved from Koin.
     */
    private let viewModel: PokemonListViewModel
    
    /**
     * Initializes the wrapper by fetching PokemonListViewModel from Koin.
     * 
     * Note: Koin must be initialized (via KoinIosKt.doInitKoin) before
     * this wrapper is created, typically in the App's init().
     */
    init() {
        // Retrieve ViewModel from Koin DI using helper function
        // This avoids dealing with Koin's complex Swift API
        self.viewModel = KoinIosKt.getPokemonListViewModel()
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
     * Loads the initial page of Pokemon (first 20 items).
     * 
     * Call this when the list view appears for the first time.
     */
    func loadInitialPage() {
        viewModel.loadInitialPage()
    }
    
    /**
     * Loads the next page of Pokemon for infinite scroll.
     * 
     * Call this when the user scrolls near the bottom of the list
     * (e.g., when last 4 items appear).
     */
    func loadNextPage() {
        viewModel.loadNextPage()
    }
}
