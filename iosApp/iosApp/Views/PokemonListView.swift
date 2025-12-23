import SwiftUI
import Shared

/**
 * Main Pokemon list screen displaying a grid of Pokemon cards.
 * 
 * Features:
 * - 2-column grid layout (fixed columns, iOS native feel)
 * - Infinite scroll (loads more at 4-item threshold)
 * - Three UI states: Loading, Content, Error
 * - Scroll position preservation on navigation return
 * - Navigation to detail screen
 * - AsyncStream lifecycle management via .task
 * 
 * This view integrates with the KMP PokemonListViewModel directly,
 * observing StateFlow updates and triggering data loads.
 */

struct PokemonListView: View {
    @StateObject private var owner = IosViewModelStoreOwner()
    
    // Computed property delegates to generic viewModel() (ViewModelStore cached)
    private var viewModel: PokemonListViewModel {
        owner.viewModel()
    }
    
    @State private var navigationPath: [Int] = []
    @State private var scrollPosition: Int?
    @State private var uiState: PokemonListUiState = PokemonListUiStateLoading()
    
    var body: some View {
        NavigationStack(path: $navigationPath) {
            content
                .navigationTitle("Pokémon")
                .navigationDestination(for: Int.self) { pokemonId in
                    PokemonDetailView(pokemonId: pokemonId)
                }
        }
        .onAppear {
            // Restore UX state from ViewModel (survives SwiftUI view recreation).
            if scrollPosition == nil {
                scrollPosition = viewModel.restoredScrollAnchorPokemonId?.intValue
                    ?? viewModel.restoredLastSelectedPokemonId?.intValue
            }

            // Directly call ViewModel lifecycle method (aligned with official Android KMP ViewModel guide)
            viewModel.onStart(owner: Shared.DummyLifecycleOwner())
        }
        .onDisappear {
            // Call lifecycle stop method
            viewModel.onStop(owner: Shared.DummyLifecycleOwner())
        }
        .task {
            // Observe StateFlow - automatically cancels when view disappears
            for await state in viewModel.uiState {
                self.uiState = state
            }
        }
    }
    
    @ViewBuilder
    private var content: some View {
        switch uiState {
        case is PokemonListUiStateLoading:
            loadingView
            
        case let content as PokemonListUiStateContent:
            gridView(content: content)
            
        case let error as PokemonListUiStateError:
            errorView(message: error.message)
            
        default:
            EmptyView()
        }
    }
    
    // MARK: - Loading State
    
    private var loadingView: some View {
        VStack(spacing: 16) {
            ProgressView()
                .scaleEffect(1.5)
            Text("Loading Pokémon...")
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .accessibilityElement(children: .combine)
        .accessibilityLabel("Loading Pokémon")
    }
    
    // MARK: - Grid Content
    
    private func gridView(content: PokemonListUiStateContent) -> some View {
        ScrollViewReader { proxy in
            ScrollView {
                LazyVGrid(
                    columns: [
                        GridItem(.flexible(), spacing: 16),
                        GridItem(.flexible(), spacing: 16)
                    ],
                    spacing: 16
                ) {
                    // Pokemon cards
                    ForEach(Array(content.pokemons.enumerated()), id: \.element.id) { index, pokemon in
                        PokemonCard(pokemon: pokemon) {
                            // Save scroll position before navigating
                            let id = Int(pokemon.id)
                            scrollPosition = id

                            // Persist selection + anchor for full state restoration.
                            viewModel.onPokemonSelected(pokemonId: Int32(id))
                            viewModel.onScrollAnchorPokemonIdChanged(pokemonId: Int32(id))

                            // Navigate to detail
                            navigationPath.append(id)
                        }
                        .id(Int(pokemon.id))
                        .onAppear {
                            // Trigger infinite scroll when approaching end (4 items threshold)
                            let pokemonCount = Int(content.pokemons.count)
                            if index >= pokemonCount - 4 &&
                               !content.isLoadingMore &&
                               content.hasMore {
                                viewModel.loadNextPage()
                            }
                        }
                    }
                    
                    // Bottom loading indicator during pagination
                    if content.isLoadingMore {
                        HStack {
                            Spacer()
                            ProgressView()
                                .padding()
                            Spacer()
                        }
                        .gridCellColumns(2)
                    }
                }
                .padding(16)
            }
            .onAppear {
                // Restore scroll position when returning from detail
                if let position = scrollPosition {
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                        withAnimation {
                            proxy.scrollTo(position, anchor: .center)
                        }
                    }
                }
            }
        }
    }
    
    // MARK: - Error State
    
    private func errorView(message: String) -> some View {
        VStack(spacing: 16) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 50))
                .foregroundColor(.orange)
            
            Text("Oops!")
                .font(.title2)
                .fontWeight(.bold)
            
            Text(message)
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal)
            
            Button(action: {
                viewModel.loadInitialPage()
            }) {
                Text("Retry")
                    .font(.headline)
                    .foregroundColor(.white)
                    .padding(.horizontal, 32)
                    .padding(.vertical, 12)
                    .background(Color.blue)
                    .cornerRadius(8)
            }
            .padding(.top, 8)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .accessibilityElement(children: .combine)
        .accessibilityLabel("Error: \(message). Retry button available.")
    }
}

// MARK: - Previews

#Preview("Loading") {
    PokemonListView()
}

#Preview("Dark Mode") {
    PokemonListView()
        .preferredColorScheme(.dark)
}
